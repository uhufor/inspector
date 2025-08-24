package com.uhufor.inspector.engine

import android.app.Activity
import android.graphics.PointF
import android.view.View
import com.uhufor.inspector.TraverseType
import com.uhufor.inspector.config.ConfigProvider
import com.uhufor.inspector.util.SwipeGestureDetector
import com.uhufor.inspector.util.WindowManagerUtils

internal class InspectorEngine(
    private val configProvider: ConfigProvider,
    private val topActivityProvider: () -> Activity?,
    private val onSelectionChanged: (SelectionState?) -> Unit,
    private val invalidator: () -> Unit,
) {
    var selection: SelectionState? = null
        private set

    var primarySelection: SelectionState? = null
        private set

    var secondarySelection: SelectionState? = null
        private set

    var measurementMode: MeasurementMode = MeasurementMode.Normal
        private set

    var allElements: List<SelectionState> = emptyList()
        private set

    var offsetOnScreen: PointF = PointF()
        private set

    val traverseType: TraverseType
        get() = configProvider.getConfig().traverseType

    private val dfsElements: MutableList<SelectionState> = mutableListOf()

    private fun getDecorView(): View? {
        return runCatching {
            WindowManagerUtils.getDecorViews()?.lastOrNull()
        }.getOrNull() ?: topActivityProvider()?.window?.decorView
    }

    fun handleTap(x: Float, y: Float) {
        val rootView = getDecorView() ?: return

        findElementAt(rootView, x.toInt(), y.toInt())?.let { selection ->
            when (measurementMode) {
                MeasurementMode.Normal -> this.selection = selection
                MeasurementMode.Relative -> secondarySelection = selection
            }
            notifyOnChangeSelectionState()
            invalidate()
        }
    }

    fun handleLongPress(x: Float, y: Float) {
        val rootView = getDecorView() ?: return

        findElementAt(rootView, x.toInt(), y.toInt())?.let { selection ->
            if (measurementMode == MeasurementMode.Relative &&
                primarySelection?.id == selection.id
            ) {
                measurementMode = MeasurementMode.Normal
                primarySelection = null
                secondarySelection = null
                this.selection = selection
            } else {
                measurementMode = MeasurementMode.Relative
                primarySelection = selection
                secondarySelection = null
                this.selection = null
            }
            notifyOnChangeSelectionState()
            invalidate()
        }
    }

    fun handleSwipe(direction: SwipeGestureDetector.GestureDirection) {
        when (measurementMode) {
            MeasurementMode.Normal -> {
                val from = this.selection ?: return

                getNextSelection(direction, from)?.let { selection ->
                    this.selection = selection
                    notifyOnChangeSelectionState()
                    invalidate()
                }
            }

            MeasurementMode.Relative -> {
                val from = this.secondarySelection ?: this.primarySelection ?: return

                getNextSelection(direction, from)?.let { selection ->
                    this.secondarySelection = selection
                    notifyOnChangeSelectionState()
                    invalidate()
                }
            }
        }
    }

    private fun getNextSelection(
        direction: SwipeGestureDetector.GestureDirection,
        from: SelectionState,
    ): SelectionState? = when (traverseType) {
        TraverseType.HIERARCHICAL -> ::getNextHierarchicalSelection
        TraverseType.DFS -> ::getNextDfsSelection
    }(direction, from)

    private fun getNextHierarchicalSelection(
        direction: SwipeGestureDetector.GestureDirection,
        from: SelectionState,
    ): SelectionState? = when (direction) {
        SwipeGestureDetector.GestureDirection.UP -> {
            findParentOf(from)
        }

        SwipeGestureDetector.GestureDirection.DOWN -> {
            findChildrenOf(from).firstOrNull()
        }

        SwipeGestureDetector.GestureDirection.LEFT -> {
            findPreviousSiblingOf(from)
        }

        SwipeGestureDetector.GestureDirection.RIGHT -> {
            findNextSiblingOf(from)
        }
    }

    private fun getNextDfsSelection(
        direction: SwipeGestureDetector.GestureDirection,
        from: SelectionState,
    ): SelectionState? = when (direction) {
        SwipeGestureDetector.GestureDirection.UP -> {
            findParentOf(from)
        }

        SwipeGestureDetector.GestureDirection.DOWN -> {
            findChildrenOf(from).firstOrNull()
        }

        SwipeGestureDetector.GestureDirection.LEFT -> {
            val currentIndex = dfsElements.indexOfFirst { it.id == from.id }
            if (currentIndex > 0) {
                dfsElements[currentIndex - 1]
            } else {
                null
            }
        }

        SwipeGestureDetector.GestureDirection.RIGHT -> {
            val currentIndex = dfsElements.indexOfFirst { it.id == from.id }
            if (currentIndex != -1 && currentIndex < dfsElements.size - 1) {
                dfsElements[currentIndex + 1]
            } else {
                null
            }
        }
    }

    private fun findElementAt(rootView: View, x: Int, y: Int): SelectionState? {
        return ComposeHitTester.hitTest(rootView, x, y) ?: ViewHitTester.hitTest(rootView, x, y)
    }

    fun scanAllElements() {
        val rootView: View = getDecorView() ?: return

        val location = IntArray(2)
        rootView.getLocationOnScreen(location)
        offsetOnScreen.set(location[0].toFloat(), location[1].toFloat())

        val elements = mutableListOf<SelectionState>()

        elements.addAll(ComposeHitTester.scanAllElements(rootView))
        elements.addAll(ViewHitTester.scanAllElements(rootView))

        allElements = elements
        dfsElements.addAll(buildDfsOrderedList())
        notifyOnChangeSelectionState()
        invalidate()
    }

    fun clearScan() {
        allElements = emptyList()
        dfsElements.clear()
        selection = null
        primarySelection = null
        secondarySelection = null
        measurementMode = MeasurementMode.Normal
        notifyOnChangeSelectionState()
        invalidate()
    }

    fun getRelativeDistances(): List<Distance> {
        val primary = primarySelection?.bounds ?: return emptyList()
        val secondary = secondarySelection?.bounds ?: return emptyList()

        return RelativeMeasurement.calculateDistances(primary, secondary)
    }

    private fun invalidate() {
        invalidator()
    }

    private fun notifyOnChangeSelectionState() {
        onSelectionChanged(
            when (measurementMode) {
                MeasurementMode.Normal -> selection
                MeasurementMode.Relative -> getRelativeSelection()
            }
        )
    }

    private fun getRelativeSelection(): SelectionState? {
        val primary = primarySelection?.bounds ?: return null
        val secondary = secondarySelection?.bounds ?: return null

        return primarySelection?.run {
            copy(
                properties = properties.copy(
                    distance = RelativeMeasurement.getRelativeBounds(primary, secondary)
                )
            )
        }
    }

    private fun List<SelectionState>.sortForHierarchy(): List<SelectionState> {
        return this.sortedWith(compareBy({ it.bounds.top }, { it.bounds.left }))
    }

    private fun findParentOf(element: SelectionState?): SelectionState? {
        if (element?.parentBounds == null) return null
        return allElements.find { it.bounds == element.parentBounds }
    }

    private fun findChildrenOf(element: SelectionState?): List<SelectionState> {
        if (element == null) return emptyList()
        return allElements
            .asSequence()
            .filter {
                it.parentBounds == element.bounds
            }
            .filterNot {
                it.parentBounds == it.bounds
            }
            .toList()
            .sortForHierarchy()
    }

    private fun findSiblingsOf(element: SelectionState?): List<SelectionState> {
        if (element == null) return emptyList()
        val parent = findParentOf(element) ?: return emptyList()
        return findChildrenOf(parent)
    }

    private fun findPreviousSiblingOf(element: SelectionState?): SelectionState? {
        if (element == null) return null
        val siblings = findSiblingsOf(element)
        val currentIndex = siblings.indexOfFirst { it.id == element.id }
        return if (currentIndex > 0) siblings[currentIndex - 1] else null
    }

    private fun findNextSiblingOf(element: SelectionState?): SelectionState? {
        if (element == null) return null
        val siblings = findSiblingsOf(element)
        val currentIndex = siblings.indexOfFirst { it.id == element.id }
        return if (currentIndex != -1 && currentIndex < siblings.size - 1) siblings[currentIndex + 1] else null
    }

    private fun buildDfsOrderedList(): List<SelectionState> {
        if (allElements.isEmpty()) return emptyList()

        val orderedList = mutableListOf<SelectionState>()
        val visitedGlobal = mutableSetOf<Int>()
        val roots = allElements.filter { findParentOf(it) == null }.sortForHierarchy()

        fun dfsVisit(currentElement: SelectionState) {
            if (currentElement.id in visitedGlobal) {
                return
            }
            visitedGlobal.add(currentElement.id)

            orderedList.add(currentElement)
            val children = findChildrenOf(currentElement)
            for (child in children) {
                dfsVisit(child)
            }
        }

        for (root in roots) {
            dfsVisit(root)
        }
        return orderedList
    }
}

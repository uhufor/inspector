package com.uhufor.inspector

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.core.content.getSystemService
import com.uhufor.inspector.config.Config
import com.uhufor.inspector.config.ConfigProvider
import com.uhufor.inspector.engine.InspectorEngine
import com.uhufor.inspector.engine.SelectionState
import com.uhufor.inspector.ui.FloatingDetailsView
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.util.ActivityTracker
import com.uhufor.inspector.util.AnchorView
import com.uhufor.inspector.util.checkPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object Inspector {
    private lateinit var applicationContext: Context
    private var windowManager: WindowManager? = null
    private var overlayCanvas: OverlayCanvas? = null
    private var inspectorEngine: InspectorEngine? = null
    private var floatingTrigger: FloatingTrigger? = null
    private var floatingDetailsView: FloatingDetailsView? = null

    private var installed = false
    var isInspectionEnabled: Boolean = false
        private set

    private val config: Config = Config()
    private val configProvider = object : ConfigProvider {
        override fun getConfig(): Config {
            return config
        }
    }

    private val positionRectChangeListener = object : AnchorView.OnPositionRectChangeListener {
        override fun onPositionRectChange(rect: Rect) {
            floatingDetailsView?.updateSticky(rect)
        }
    }

    private val selectionState: MutableStateFlow<SelectionState?> = MutableStateFlow(null)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var selectionChangedJob: Job? = null

    @MainThread
    fun install(context: Context) {
        if (installed) return
        applicationContext = context.applicationContext

        if (!checkPermission(applicationContext)) {
            return
        }

        windowManager = applicationContext.getSystemService()
        ActivityTracker.register(applicationContext)
        installed = true
    }

    @MainThread
    fun enableInspection() {
        if (!installed || isInspectionEnabled) return
        val activity = ActivityTracker.top ?: return

        val engine = InspectorEngine(
            configProvider = configProvider,
            topActivityProvider = { ActivityTracker.top },
            onSelectionChanged = { selectionState.value = it },
            invalidator = { overlayCanvas?.invalidate() },
        )
        inspectorEngine = engine

        val canvas = OverlayCanvas(
            context = activity,
        ).apply {
            setConfigProvider(configProvider)
            setEngine(engine)
            backKeyListener = object : OverlayCanvas.BackKeyListener {
                override fun onBackPressed() {
                    disableInspection()
                    floatingTrigger?.refreshEnableState()
                }
            }
        }
        overlayCanvas = canvas

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        windowManager?.addView(overlayCanvas, params)

        inspectorEngine?.scanAllElements()
        floatingTrigger?.bringToFront()
        startObservingSelectionChanges()

        isInspectionEnabled = true
    }

    @MainThread
    fun disableInspection() {
        if (!installed || !isInspectionEnabled) return

        runCatching {
            windowManager?.removeView(overlayCanvas)
        }
        overlayCanvas = null
        floatingDetailsView?.uninstall()
        floatingDetailsView = null
        selectionChangedJob?.cancel()
        selectionChangedJob = null
        inspectorEngine?.clearScan()
        inspectorEngine = null
        selectionState.value = null

        isInspectionEnabled = false
    }

    @MainThread
    fun toggleInspection() {
        if (isInspectionEnabled) {
            disableInspection()
        } else {
            enableInspection()
        }
    }

    @MainThread
    fun refresh() {
        if (!installed || !isInspectionEnabled) return
        inspectorEngine?.scanAllElements()
    }

    private fun startObservingSelectionChanges() {
        selectionChangedJob?.cancel()
        selectionChangedJob = coroutineScope.launch {
            combine(
                selectionState,
                config.unitModeFlow,
                config.showDetailsViewFlow
            ) { selectionState, unitMode, showDetailsView ->
                selectionState.takeIf { showDetailsView } to unitMode
            }.collectLatest { (selectionState, unitMode) ->
                handleSelectionChanged(selectionState, unitMode)
            }
        }
    }

    private fun handleSelectionChanged(selectionState: SelectionState?, unitMode: UnitMode) {
        if (selectionState != null) {
            var isNeedToBringToFront = false
            if (floatingDetailsView == null) {
                floatingDetailsView = FloatingDetailsView(applicationContext)
                isNeedToBringToFront = true
            }
            floatingDetailsView?.setOnRefresh { refresh() }
            floatingDetailsView?.install(selectionState = selectionState, unitMode = unitMode)
            floatingTrigger?.requestUpdateAnchor()
            if (isNeedToBringToFront) {
                floatingTrigger?.bringToFront()
            }
        } else {
            floatingDetailsView?.uninstall()
            floatingDetailsView = null
        }
    }

    fun setUnitMode(mode: UnitMode) {
        if (config.unitMode == mode) return

        config.unitMode = mode
        overlayCanvas?.invalidate()
    }

    fun getUnitMode(): UnitMode {
        return config.unitMode
    }

    fun setTraverseType(type: TraverseType) {
        if (config.traverseType == type) return

        config.traverseType = type
    }

    fun getTraverseType(): TraverseType {
        return config.traverseType
    }

    fun enableDetailsView(enabled: Boolean) {
        if (config.showDetailsView == enabled) return

        config.showDetailsView = enabled
    }

    val isDetailsViewEnabled: Boolean
        get() = config.showDetailsView

    @MainThread
    fun setDetailsViewFocusable(focusable: Boolean) {
        floatingDetailsView?.setFocusable(focusable)
    }

    @MainThread
    fun showFloatingTrigger() {
        if (!installed) return

        if (floatingTrigger == null) {
            floatingTrigger = FloatingTrigger(
                context = applicationContext,
                inspector = this,
                positionRectChangeListener = positionRectChangeListener,
            )
        }

        floatingTrigger?.install()
    }

    @MainThread
    fun hideFloatingTrigger() {
        floatingTrigger?.uninstall()
        floatingTrigger = null
    }
}

package com.uhufor.inspector

import android.app.Application
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.core.content.getSystemService
import com.uhufor.inspector.engine.InspectorEngine
import com.uhufor.inspector.engine.MeasurementMode
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.util.ActivityTracker
import com.uhufor.inspector.util.checkPermission

object Inspector : ConfigProvider {
    private lateinit var applicationContext: Context
    private var windowManager: WindowManager? = null
    private var overlayCanvas: OverlayCanvas? = null
    private var inspectorEngine: InspectorEngine? = null
    private var floatingTrigger: FloatingTrigger? = null

    private var installed = false
    var isInspectionEnabled: Boolean = false
        private set

    private val _config: Config = Config()

    override fun getConfig(): Config = _config

    @MainThread
    fun install(app: Application) {
        if (installed) return
        applicationContext = app.applicationContext

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

        val engine = InspectorEngine(topActivityProvider = { ActivityTracker.top }) {
            overlayCanvas?.invalidate()
        }
        inspectorEngine = engine

        val canvas = OverlayCanvas(
            context = activity,
        ).apply {
            setConfigProvider(this@Inspector)
            setEngine(engine)
            backKeyListener = object : OverlayCanvas.BackKeyListener {
                override fun onBackPressed() {
                    disableInspection()
                }
            }
        }
        overlayCanvas = canvas

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.START or Gravity.TOP
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        windowManager?.addView(overlayCanvas, params)

        inspectorEngine?.scanAllElements()
        floatingTrigger?.bringToFront()
        isInspectionEnabled = true
        floatingTrigger?.updateInspectorState(true, _config.unitMode)
    }

    @MainThread
    fun disableInspection() {
        if (!installed || !isInspectionEnabled) return
        overlayCanvas?.let {
            windowManager?.removeView(it)
        }

        overlayCanvas = null
        inspectorEngine?.clearScan()
        inspectorEngine = null
        isInspectionEnabled = false
        floatingTrigger?.updateInspectorState(false, _config.unitMode)
    }

    @MainThread
    fun toggleInspection() {
        if (isInspectionEnabled) {
            disableInspection()
        } else {
            enableInspection()
        }
    }

    fun setUnitMode(mode: UnitMode) {
        if (_config.unitMode == mode) return
        _config.unitMode = mode
        overlayCanvas?.invalidate()
        floatingTrigger?.updateInspectorState(isInspectionEnabled, _config.unitMode)
    }

    fun getCurrentMeasurementMode(): MeasurementMode? {
        return inspectorEngine?.measurementMode
    }

    fun handleTap(x: Float, y: Float) {
        inspectorEngine?.handleTap(x, y)
    }

    fun handleLongPress(x: Float, y: Float) {
        inspectorEngine?.handleLongPress(x, y)
    }

    @MainThread
    fun showFloatingTrigger() {
        if (!installed) return

        if (floatingTrigger == null) {
            floatingTrigger = FloatingTrigger(
                context = applicationContext,
                inspector = this
            )
        }

        floatingTrigger?.install()
        floatingTrigger?.updateInspectorState(isInspectionEnabled, _config.unitMode)
    }

    @MainThread
    fun hideFloatingTrigger() {
        floatingTrigger?.uninstall()
        floatingTrigger = null
    }
}

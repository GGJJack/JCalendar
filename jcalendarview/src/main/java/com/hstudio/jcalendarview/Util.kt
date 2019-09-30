package com.hstudio.jcalendarview

import android.os.Build
import android.view.View
import java.util.concurrent.atomic.AtomicInteger

internal object Util {

    private val sNextGeneratedId = lazy { AtomicInteger(1) }

    internal fun makeViewId(): Int {
        if (Build.VERSION.SDK_INT < 17) {
            while (true) {
                val result = sNextGeneratedId.value.get()
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                var newValue = result + 1
                if (newValue > 0x00FFFFFF)
                    newValue = 1 // Roll over to 1, not 0.
                if (sNextGeneratedId.value.compareAndSet(result, newValue)) {
                    return result
                }
            }
        } else {
            return View.generateViewId()
        }
    }

    internal fun setViewHeight(view: View, height: Int) {
        val params = view.layoutParams
        params.height = height
        view.layoutParams = params
    }
}
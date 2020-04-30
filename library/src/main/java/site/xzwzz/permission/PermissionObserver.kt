package site.xzwzz.permission

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import site.xzwzz.permission.entity.RequestEntry

internal class PermissionObserver(val entity: RequestEntry, val lifecycle: Lifecycle) :
    LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            entity.runnable.run()
            lifecycle.removeObserver(this)
        }
    }
}
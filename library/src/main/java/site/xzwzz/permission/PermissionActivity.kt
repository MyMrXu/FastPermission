package site.xzwzz.permission

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import site.xzwzz.permission.callback.PermissionCallback

internal class PermissionActivity : AppCompatActivity(), PermissionCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View(this)
        view.setBackgroundColor(Color.TRANSPARENT)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setContentView(view)
        val permission = intent.getStringArrayExtra("permission")!!

        FastPermission.request(this, permission, this)
    }

    override fun onPause() {
        overridePendingTransition(0, 0)
        super.onPause()
    }


    override fun onGranted() {
        finish()
    }

    override fun onDenied(perms: List<String>) {
        finish()
    }
}
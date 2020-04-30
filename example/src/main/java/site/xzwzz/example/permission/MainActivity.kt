package site.xzwzz.example.permission

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import site.xzwzz.permission.FastPermission
import site.xzwzz.permission.callback.PermissionCallback

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_request.setOnClickListener {
            requestPermission()
        }
    }

    private fun requestPermission() {
        FastPermission.request(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            object : PermissionCallback {
                override fun onGranted() {

                }

                override fun onDenied(perms: List<String>) {

                }
            })
    }
}

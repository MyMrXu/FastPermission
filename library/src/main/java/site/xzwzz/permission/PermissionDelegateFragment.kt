package site.xzwzz.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.collection.SparseArrayCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleEventObserver
import site.xzwzz.permission.callback.PermissionCallback
import site.xzwzz.permission.entity.RequestEntry
import java.util.*

internal class PermissionDelegateFragment : Fragment() {
    private val callbacks: SparseArrayCompat<RequestEntry>? = SparseArrayCompat()
    override fun onDetach() {
        super.onDetach()
        popAll()
        //        getLifecycle().removeObserver();
    }

    /**
     * 请求操作必须在OnAttach后调用
     *
     * @param entry 请求包装对象
     */
    private fun pushStack(entry: RequestEntry) {
        callbacks!!.put(entry.hashCode(), entry)
        val observer: LifecycleEventObserver = PermissionObserver(entry, this.lifecycle)
        this.lifecycle.addObserver(observer)
    }

    /**
     * 结束任务，在集合中移除
     *
     * @param entry 要移除的请求包装对象
     */
    private fun popStack(entry: RequestEntry) {
        callbacks!!.remove(entry.hashCode())
    }

    /**
     * 移除所有callback
     */
    private fun popAll() {
        if (callbacks != null && callbacks.size() > 0) {
            callbacks.clear()
        }
    }

    /**
     * 批量申请权限
     *
     * @param context  上下文
     * @param callback 权限允许、拒绝回调
     * @param perms    要申请的权限数组
     */
    fun requestPermission(
        context: Context,
        callback: PermissionCallback?,
        perms: Array<String>
    ) {
        if (!isNeedCheck(context)) {
            callback!!.onGranted()
            return
        }
        pushStack(
            RequestEntry.newBuilder().withCallback(callback)
                .withRunnable { //只申请用户未允许的权限
                    val unGrantedList: MutableList<String> =
                        ArrayList()
                    for (permission in perms) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                permission
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            unGrantedList.add(permission)
                        }
                    }
                    if (unGrantedList.size > 0) {
                        requestPermissions(
                            unGrantedList.toTypedArray(), REQUEST_CODE
                        )
                    } else {
                        callback?.onGranted()
                    }
                }.build()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.size > 0 && callbacks != null && callbacks.size() > 0) {
                var i = 0
                while (i < callbacks.size()) {
                    val entry = callbacks.valueAt(i)
                    val callback = entry.callback
                    //找出拒绝的权限
                    val deniedList: MutableList<String> =
                        ArrayList()
                    var j = 0
                    while (j < grantResults.size) {
                        val grantResult = grantResults[j]
                        val permission = permissions[j]
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            deniedList.add(permission)
                        }
                        j++
                    }
                    //已全部允许
                    if (deniedList.isEmpty()) {
                        callback.onGranted()
                    } else {
                        callback.onDenied(deniedList)
                    }
                    popStack(entry)
                    i++
                }
            }
            else -> {
            }
        }
    }

    companion object {
        //权限回调的标识
        private const val REQUEST_CODE = 0X0122
        fun newInstance(): PermissionDelegateFragment {
            return PermissionDelegateFragment()
        }

        /**
         * 判断是否需要检查权限。运行版本大于6.0并且使用该库的targetSdkVersion大于23时才检查
         *
         * @return 返回true代表版本号大于6.0需要检查权限
         */
        fun isNeedCheck(context: Context): Boolean {
            val targetSdkVersion = context.applicationInfo.targetSdkVersion
            return Build.VERSION.SDK_INT >= 23 && targetSdkVersion >= 23
        }

        /**
         * 检查指定权限是否已经获取
         */
        fun isAccept(context: Context, permission: String?): Boolean {
            return if (!isNeedCheck(context)) {
                true
            } else {
                (isNeedCheck(context)
                        && ContextCompat.checkSelfPermission(
                    context,
                    permission!!
                )
                        == PackageManager.PERMISSION_GRANTED)
            }
        }

        /**
         * 跳转到应用的设置界面
         */
        fun goToAppDetailSetting(context: Context) {
            val localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            localIntent.data = Uri.fromParts(
                "package",
                context.packageName,
                null
            )
            context.startActivity(localIntent)
        }
    }

    init {
        retainInstance = true
    }
}
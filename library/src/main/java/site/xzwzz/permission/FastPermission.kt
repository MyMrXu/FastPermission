package site.xzwzz.permission

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import site.xzwzz.permission.callback.PermissionCallback
import java.lang.reflect.Field
import java.lang.reflect.Method


object FastPermission {
    private val mContext by lazy {
        getApplicationContext() ?: getApplicationInner()!!
    }

    /**
     * 检查指定权限是否已经获取
     */
    fun isAccept(vararg permission: String): Boolean {
        permission.forEach {
            if (!PermissionDelegateFragment.isAccept(mContext, it)) {
                return false
            }
        }
        return true
    }

    /**
     * 使用Activity申请权限
     *
     * @param activity 要注入权限申请代理的FragmentActivity
     * @param callback 权限申请 成功、失败回调
     * @param perms    权限列表数组
     */
    fun request(
        activity: FragmentActivity,
        perms: Array<String>,
        callback: PermissionCallback
    ) {
        val delegate = findDelegate(activity)
        delegate?.requestPermission(activity, callback, perms)
    }

    /**
     * 使用Fragment申请权限
     *
     * @param fragment 使用的Fragment
     * @param callback 权限申请 成功、失败回调
     * @param perms    权限列表数组
     */
    fun request(
        fragment: Fragment,
        perms: Array<String>,
        callback: PermissionCallback
    ) {
        val activity: FragmentActivity? = fragment.activity
        if (activity != null && !activity.isFinishing) {
            val delegate = findDelegate(activity)
            delegate?.requestPermission(activity, callback, perms)
        }
    }

    fun request(
        permissions: Array<String>,
        callback: PermissionCallback
    ) {
        //不是Activity或Fragment就启用自身的Activity
        val intent = Intent(mContext, PermissionActivity::class.java)
        intent.putExtra("permission", permissions)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        mContext.startActivity(intent)
    }

    /**
     * 构建申请权限用的隐藏的fragment
     */
    private fun findDelegate(activity: FragmentActivity): PermissionDelegateFragment? {
        return find(activity)
    }

    /**
     * 添加隐藏权限fragment
     */
    private fun find(activity: FragmentActivity?): PermissionDelegateFragment? {
        var fragment: PermissionDelegateFragment? = null
        if (activity != null && !activity.isFinishing) {
            val fm = activity.supportFragmentManager
            fragment =
                fm.findFragmentByTag(DELEGATE_FRAGMENT_TAG) as PermissionDelegateFragment?
            if (fragment == null) {
                fragment = PermissionDelegateFragment.newInstance()
                fm.beginTransaction()
                    .add(fragment, DELEGATE_FRAGMENT_TAG)
                    .commitAllowingStateLoss()
            }
        }
        return fragment
    }

    private val DELEGATE_FRAGMENT_TAG =
        PermissionDelegateFragment::class.java.simpleName + "Tag"

    private fun getApplicationContext(): Context? {
        var application: Application? = null
        val activityThreadClass: Class<*>
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread")
            val appField: Field = activityThreadClass
                .getDeclaredField("mInitialApplication")
            // Object object = activityThreadClass.newInstance();
            val method: Method = activityThreadClass.getMethod(
                "currentActivityThread", *arrayOfNulls(0)
            )
            // 得到当前的ActivityThread对象
            val localObject: Any = method.invoke(null, null as Array<Any?>?)
            appField.isAccessible = true
            application = appField.get(localObject) as Application
            return application
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getApplicationInner(): Context? {
        return try {
            val activityThread =
                Class.forName("android.app.ActivityThread")
            val currentApplication =
                activityThread.getDeclaredMethod("currentApplication")
            val currentActivityThread =
                activityThread.getDeclaredMethod("currentActivityThread")
            val current = currentActivityThread.invoke(null as Any?)
            val app = currentApplication.invoke(current)
            app as Application
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }
}
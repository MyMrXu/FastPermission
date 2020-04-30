package site.xzwzz.permission.callback

interface PermissionCallback {
    /**
     * 权限允许
     */
    fun onGranted()

    /**
     * 权限拒绝
     * @param perms 被拒绝的权限集合
     */
    fun onDenied(perms: List<String>)
}
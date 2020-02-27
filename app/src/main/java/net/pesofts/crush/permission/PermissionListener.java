package net.pesofts.crush.permission;

/**
 * Created by tommykim on 15. 10. 2..
 */
public interface PermissionListener {

    void onGranted();
    void onDenied();

}

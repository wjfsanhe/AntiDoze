package com.qiyi.vr.antidoze;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class unLockService extends Service {
    private final String TAG = "unLockService";
    private IDeviceIdleController mDeviceIdle = null;
    public unLockService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String packageName = intent.getExtras().getString("package");
        String action = intent.getExtras().getString("action");
        /* we use packageName and action to performa unlock op.
            example:
            package  com.aa.bb
            action   add (remove)
        */
        Log.d(TAG, "Packege" + packageName + " ==> " + action);
        updateDeviceIdleWhiteList(packageName, action);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateDeviceIdleWhiteList(String packageName, String action) {
        mDeviceIdle = getDeviceIdleService();

        try {
            if (action.equals("add")){
                if (packageName != null){
                    Log.d(TAG, "add " + packageName + " to doze whitelist!");
                    mDeviceIdle.addPowerSaveWhitelistApp(packageName);
                }
            }
            if (action.equals("remove")){
                if (packageName != null){
                    Log.d(TAG, "remove " + packageName + " from doze whitelist!");
                    mDeviceIdle.removePowerSaveWhitelistApp(packageName);
                }
            }

            if (mDeviceIdle.isPowerSaveWhitelistApp(packageName)){
                Log.d(TAG, "[update]package " + packageName + "is in doze whitelist");
            }else{
                Log.d(TAG, "[update]package " + packageName + "is not in doze whitelist");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private IDeviceIdleController getDeviceIdleService() {
        try {
            Class serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getMethod("getService", new Class[] {String.class});
            Log.d(TAG, "calling getService method");
            if(getService != null) {
                Object result = getService.invoke(serviceManager, new Object[]{"deviceidle"});
                if(result != null) {
                    IBinder binder = (IBinder) result;
                    Log.d(TAG, "got IDeviceIdleController Service !");
                    return IDeviceIdleController.Stub.asInterface(binder);
                }else{
                    Log.d(TAG, "Can't find IDeviceIdleController service");
                }
            }else{
                Log.d(TAG, "Can't find getService method");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}

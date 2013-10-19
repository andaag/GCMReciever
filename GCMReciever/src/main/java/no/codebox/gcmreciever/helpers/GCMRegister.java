package no.codebox.gcmreciever.helpers;


import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.otto.Bus;
import no.codebox.gcmreciever.events.LogMessage;
import no.codebox.gcmreciever.events.RegisterEvent;

public class GCMRegister {
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static final String TAG = GCMRegister.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private final Activity activity;
    private final Bus bus;
    private final Context context;

    private RegisterTask registerTask = null;
    private GoogleCloudMessaging gcm;
    private String regid;

    public GCMRegister(Activity activity, Bus bus) {
        this.activity = activity;
        this.bus = bus;
        context = activity.getApplicationContext();
    }

    public void checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                throw new IllegalStateException("Device not supported");
            }
        }
    }

    public void registerIfNeccesary() {
        checkPlayServices();
        gcm = GoogleCloudMessaging.getInstance(context);
        regid = getRegistrationId(context);

        if (regid.isEmpty()) {
            if (registerTask == null) {
                //@todo : hardcoded senderid.
                registerTask = new RegisterTask("989137962554");
            }
        } else {
            bus.post(new LogMessage("Already registered, regId : " + regid));
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences() {
        return activity.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getGCMPreferences();
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private class RegisterTask extends AsyncTask<Void, Void, Exception> {
        private String senderId;

        public RegisterTask(String senderId) {
            bus.post(new LogMessage("Registering with GCM"));
            this.senderId = senderId;
            execute();
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(context);
            }
            try {
                regid = gcm.register(senderId);
            } catch (IOException e) {
                regid = null;
                return e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            bus.post(new LogMessage("Registered!"));
            bus.post(new LogMessage("RegId : " + regid));
            registerTask = null;
            storeRegistrationId(regid);
            bus.post(new RegisterEvent(regid, e));
        }
    }


}

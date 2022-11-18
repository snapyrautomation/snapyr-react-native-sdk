package com.reactnativesnapyrrnsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public abstract class SnapyrRnReceiverBase extends BroadcastReceiver {
    public static final String EVENT_NOTIFICATION_RECEIVED = "snapyrDidReceiveNotification";
    public static final String EVENT_NOTIFICATION_RESPONSE = "snapyrDidReceiveNotificationResponse";

    protected void sendToReact(Context context, String rnEventName, WritableMap rnEventPayload) {
        // JS emit needs to happen on main / UI thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                // Borrowed from React Native's HeadlessJsTaskService - see
                // https://github.com/facebook/react-native/blob/8bd3edec88148d0ab1f225d2119435681fbbba33/ReactAndroid/src/main/java/com/facebook/react/HeadlessJsTaskService.java#L94-L113
                final ReactInstanceManager reactInstanceManager =
                        ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
                ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
                if (reactContext == null) {
                    // React context may not be defined yet when this is called from early activity initialization, or when firing while the app is not running (e.g. from an incoming push notification). In that case, register a one-off initialization listener, fire when ready, then deregister to ensure no duplicate calls are made.
                    reactInstanceManager.addReactInstanceEventListener(
                            new ReactInstanceManager.ReactInstanceEventListener() {
                                @Override
                                public void onReactContextInitialized(ReactContext reactContext) {
                                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                            .emit(rnEventName, rnEventPayload);
                                    reactInstanceManager.removeReactInstanceEventListener(this);
                                }
                            });
                    // Init the React context - this allows JS code to run, so the emitted events can be received on the JS side. NB if starting from non-running app state, only app-level JS code will run - i.e. typically `index.js` code will run but not `App.jsx`, which is run within an Activity
                    reactInstanceManager.createReactContextInBackground();
                } else {
                    // React context is ready - send the event immediately
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(rnEventName, rnEventPayload);
                }
            }
        });
    }
}

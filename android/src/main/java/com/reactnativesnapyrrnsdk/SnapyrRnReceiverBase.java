package com.reactnativesnapyrrnsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.text.MessageFormat;

public abstract class SnapyrRnReceiverBase extends BroadcastReceiver {

  protected void sendToReact(Context context, String rnEventName, WritableMap rnEventPayload) {
    // We need to run this on the main thread, as the React code assumes that is true.
    // Namely, DevServerHelper constructs a Handler() without a Looper, which triggers:
    // "Can't create handler inside thread that has not called Looper.prepare()"
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      public void run() {

        // Construct and load our normal React JS code bundle
        final ReactInstanceManager reactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();



//        String dataString = intent.getDataString();
//        String dataExtraString = intent.getStringExtra("deepLinkUrl");
//        if (dataString == null || dataString.equals("")) {
//          dataString = dataExtraString;
//        }
//
//        Log.e("Snapyr.RN.Receivers", MessageFormat.format("SnapyrRnReceiverBase: dataString: {0}", dataString));
//
//        if (dataString == null || dataString == "") {
//          Log.e("Snapyr.RN.Receivers", "SnapyrRnReceiverBase: received intent but no data string.");
//          return;
//        }
//
//        final String finalDataString = dataString;

        if (reactContext != null) {
          reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(rnEventName, rnEventPayload);
          Log.e("Snapyr.RN.Receivers", MessageFormat.format("SnapyrRnReceiverBase: SEND JS EVENT!", null));
        } else {
          // React context may not be defined yet when this is called from `onStart` due to lifecycle
          // ordering. In that case, register a one-off initialization listener, fire when ready, then
          // deregister. Future calls to `sendIntentDataToReact` should have access to the normal
          // React context instance
          Log.d("Snapyr.RN.Receivers", "SnapyrRnReceiverBase: React instance not ready yet; registering callback...");
          reactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
            @Override
            public void onReactContextInitialized(ReactContext context) {
              Log.d("Snapyr.RN.Receivers", "onReactContextInitialized call");
              context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(rnEventName, rnEventPayload);
              reactInstanceManager.removeReactInstanceEventListener(this);
            }
          });
          if (!reactInstanceManager.hasStartedCreatingInitialContext()) {
            Log.e("Snapyr.RN.Receivers", "NOT!!!!! hasStartedCreatingInitialContext");
          }
        }

        return;
      }
    });
  }

//  private void sendIntentDataToReact(Intent intent) {
//    String dataString = intent.getDataString();
//    if (dataString == null || dataString == "") {
//      Log.d("notifTest", "received intent but no data string.");
//      return;
//    }
//
//    ReactInstanceManager reactInstanceManager = getReactNativeHost().getReactInstanceManager();
//    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
//    if (reactContext != null) {
//      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//        .emit("deepLinkUrlReceived", dataString);
//    } else {
//      // React context may not be defined yet when this is called from `onStart` due to lifecycle
//      // ordering. In that case, register a one-off initialization listener, fire when ready, then
//      // deregister. Future calls to `sendIntentDataToReact` should have access to the normal
//      // React context instance
//      Log.d("notifTest", "React instance not ready yet; registering callback...");
//      reactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
//        @Override
//        public void onReactContextInitialized(ReactContext context) {
//          Log.d("notifTest", "onReactContextInitialized call");
//          context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//            .emit("deepLinkUrlReceived", dataString);
//          reactInstanceManager.removeReactInstanceEventListener(this);
//        }
//      });
//    }
//  }
}

package com.reactnativesnapyrrnsdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import android.content.BroadcastReceiver;

import com.facebook.react.ReactApplication;
  import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.snapyr.sdk.notifications.SnapyrNotification;
//  import androidx.core.app.RemoteInput;

import java.text.MessageFormat;

public class SnapyrRnDeeplinkReceiver extends SnapyrRnReceiverBase {
  @Override
  public void onReceive(final Context context, Intent intent) {

    Log.e("XXX", MessageFormat.format("SnapyrRnDeeplinkReceiver FIRED!!!!!! {0}", intent));

    SnapyrNotification snapyrNotification = intent.getParcelableExtra("snapyrNotification");
//    Bundle bundle = new Bundle();
//    bundle.putParcelable("snapyrNotification", snapyrNotification);

    this.sendToReact(context, "deepLinkUrlReceived", Arguments.makeNativeMap(snapyrNotification.asValueMap()));
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

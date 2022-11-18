package com.reactnativesnapyrrnsdk;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.snapyr.sdk.inapp.InAppCallback;
import com.snapyr.sdk.inapp.InAppMessage;

public class SnapyrRnInAppHandler implements InAppCallback {
  public static final String EVENT_INAPP_MESSAGE = "snapyrInAppMessage";
  private static SnapyrRnInAppHandler singleton;
  private ReactApplicationContext reactApplicationContext;

  private SnapyrRnInAppHandler(ReactApplicationContext context) {
    this.reactApplicationContext = context;
  }

  public static SnapyrRnInAppHandler getInstance(ReactApplicationContext context) {
    if (singleton == null) {
      singleton = new SnapyrRnInAppHandler(context);
    } else if (singleton.reactApplicationContext != context) {
      singleton.reactApplicationContext = context;
    }

    return singleton;
  }

  @Override
  public void onAction(InAppMessage message) {
    WritableNativeMap map = Arguments.makeNativeMap(message.asValueMap());
    this.reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(EVENT_INAPP_MESSAGE, map);
  }
}

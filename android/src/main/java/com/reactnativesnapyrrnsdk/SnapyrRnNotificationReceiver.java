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
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.snapyr.sdk.notifications.SnapyrNotification;

import java.text.MessageFormat;

public class SnapyrRnNotificationReceiver extends SnapyrRnReceiverBase {
  @Override
  public void onReceive(final Context context, Intent intent) {

    Log.e("XXX", MessageFormat.format("SnapyrRnNotificationReceiver FIRED!!!!!! {0}", intent));

    SnapyrNotification snapyrNotification = intent.getParcelableExtra("snapyrNotification");
//    Bundle bundle = new Bundle();
//    bundle.putParcelable("snapyrNotification", snapyrNotification);

    this.sendToReact(context, "snapyrDidReceiveNotification", Arguments.makeNativeMap(snapyrNotification.asValueMap()));
  }
}

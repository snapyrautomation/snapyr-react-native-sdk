package com.reactnativesnapyrrnsdk;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.snapyr.sdk.notifications.SnapyrNotification;

public class SnapyrRnNotificationTappedReceiver extends SnapyrRnReceiverBase {
  @Override
  public void onReceive(final Context context, Intent intent) {
    SnapyrNotification snapyrNotification = intent.getParcelableExtra("snapyr.notification");
    if (snapyrNotification == null) {
      Log.e("Snapyr", "SnapyrRnNotificationTappedReceiver: fired, but missing `snapyr.notification`");
      return;
    }

    this.sendToReact(context, EVENT_NOTIFICATION_RESPONSE, Arguments.makeNativeMap(snapyrNotification.asValueMap()));
  }
}

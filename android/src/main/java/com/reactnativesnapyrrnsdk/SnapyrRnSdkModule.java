package com.reactnativesnapyrrnsdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.MessageFormat;
import java.util.Map;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;


import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.snapyr.sdk.Snapyr;
import com.snapyr.sdk.Traits;
import com.snapyr.sdk.Properties;
import com.snapyr.sdk.ValueMap;
import com.snapyr.sdk.http.ConnectionFactory;
import com.snapyr.sdk.inapp.InAppConfig;
import com.snapyr.sdk.inapp.InAppMessage;
import com.snapyr.sdk.notifications.SnapyrNotification;

@ReactModule(name = SnapyrRnSdkModule.NAME)
public class SnapyrRnSdkModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {
    public static final String NAME = "SnapyrRnSdk";
    private Boolean snapyrConfigured = false;
    private Boolean activityCallbacksReplayed = false;
  private WritableMap initialIntentMap = null;

  public SnapyrRnSdkModule() {
      super();
      Log.e("Snapyr.RN", "CONSTRUCTOR WITHOUT CONTEXT!!!");
    }


  @Override
  protected void finalize() throws Throwable {
    Log.e("Snapyr.RN", "FINALIZE!");
    super.finalize();
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    Log.d("Snapyr.RN", "getConstants()");
    return super.getConstants();
  }

  @Override
  public void initialize() {
    Log.d("Snapyr.RN", "initialize()");
    if (this.initialIntentMap != null) {
      Log.d("Snapyr.RN", "Post-configure intent send...");
      this.getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("initialIntentTest", this.initialIntentMap);
      this.initialIntentMap = null;
    }
    super.initialize();
  }

  @Override
  public boolean canOverrideExistingModule() {
    Log.d("Snapyr.RN", "canOverrideExistingModule()");
    return super.canOverrideExistingModule();
  }

  @Override
  public void onCatalystInstanceDestroy() {
    Log.d("Snapyr.RN", "onCatalystInstanceDestroy()");
    super.onCatalystInstanceDestroy();
  }

  @Override
  public boolean hasConstants() {
    Log.d("Snapyr.RN", "hasConstants()");
    return super.hasConstants();
  }

  @Override
  public void invalidate() {
    Log.d("Snapyr.RN", "invalidate()");
    super.invalidate();
  }

  private Context getCurrentContext() {
    Context context = this.getCurrentActivity();
    if (context == null) {
      context = this.getReactApplicationContext();
    }
    return context;
  }

  public SnapyrRnSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d("Snapyr.RN", "CONSTRUCTOR...");
        reactContext.addLifecycleEventListener(this);
        reactContext.addActivityEventListener(this);
        // Not using instance here but calling this ensures it has this current context
        SnapyrRnInAppHandler.getInstance(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
      Log.d("Snapyr.RN", "getName()");
        return NAME;
    }

    // See https://reactnative.dev/docs/native-modules-android

    @ReactMethod
    public void configure(String withKey, ReadableMap options, Promise promise) {
      try {
        Snapyr.Builder builder = new Snapyr.Builder(this.getCurrentContext(), withKey)
        .flushQueueSize(1) // makes every event flush to network immediately
        .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically
        .recordScreenViews() // Enable this to record screen views automatically
        .enableSnapyrPushHandling() // enable push for Android
        .configureInAppHandling(
          new InAppConfig()
            .setActionCallback(
              SnapyrRnInAppHandler.getInstance(this.getReactApplicationContext())
//              new InAppCallback() {
//                @Override
//                public void onAction(InAppMessage inAppMessage) {
//                  handleInAppMessage(inAppMessage);
//                }
//              }
            ));

        // DEV ONLY REMOVE THIS
        builder.logLevel(Snapyr.LogLevel.VERBOSE);

        if (options != null && options.hasKey("snapyrEnvironment")) {
          try {
            ConnectionFactory.Environment env = ConnectionFactory.Environment.values()[options.getInt("snapyrEnvironment")];
            builder.snapyrEnvironment(env);
          } catch (Exception e) {
            Log.e("Snapyr", "Invalid environment provided");
          }
        }

        Snapyr snapyr = builder.build();

        // Set the initialized instance as a globally accessible instance.
        Snapyr.setSingletonInstance(snapyr);
        snapyrConfigured = true;
        this.replayActivityCallbacks();
        Log.e("Snapyr.RN", "Configure completed successfully.");



        promise.resolve(withKey);
      } catch(Exception e) {
        Log.d("Snapyr", "Config errored:", e);
        promise.reject("Error initializing Snapyr", e);
      }
    }

    @ReactMethod
    public void identify(String userId, ReadableMap traitsMap, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          Log.d("Snapyr", "Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Traits traits = new Traits();
        if (traitsMap != null) {
          traits.putAll(traitsMap.toHashMap());
        }
        Log.d("Snapyr", "identify" + userId);
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        Log.d("Snapyr", inst.toString());
        inst.identify(userId, traits, null);
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on identify");
        promise.reject("Error on identify", e);
      }
    }


    @ReactMethod
    public void track(String eventName, ReadableMap props, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          Log.d("Snapyr", "Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Properties properties = new Properties();
        if (props != null) {
          properties.putAll(props.toHashMap());
        }

        Log.d("Snapyr", "snapyr track " + eventName);
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.track(eventName, properties, null);
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on track");
        promise.reject("Error on track", e);
      }
    }

    @ReactMethod
    public void setPushNotificationToken(String token, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          Log.d("Snapyr", "Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Log.d("Snapyr", "snapyr setPushNotificationToken: " + token);
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.setPushNotificationToken(token);
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on setPushNotificationToken");
        promise.reject("Error on setPushNotificationToken", e);
      }
    }

    @ReactMethod
    public void pushNotificationReceived(ReadableMap props, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          Log.d("Snapyr", "Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Properties properties = new Properties();
        if (props != null) {
          properties.putAll(props.toHashMap());
        }

        Log.d("Snapyr", "snapyr pushNotificationReceived");
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.pushNotificationReceived(properties);
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on pushNotificationReceived");
        promise.reject("Error on pushNotificationReceived", e);
      }
    }

    @ReactMethod
    public void pushNotificationTapped(ReadableMap props, @Nullable String actionId, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          Log.d("Snapyr", "Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Properties properties = new Properties();
        if (props != null) {
          properties.putAll(props.toHashMap());
        }

        Log.d("Snapyr", "snapyr pushNotificationTapped");
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.pushNotificationClicked(properties);
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on pushNotificationTapped");
        promise.reject("Error on pushNotificationTapped", e);
      }
    }

    @ReactMethod
    public void trackInAppMessageImpression(String actionToken, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Log.d("Snapyr", "snapyr trackInAppMessageImpression: " + actionToken);
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.trackInAppMessageImpression(actionToken);
        promise.resolve(null);
      } catch (Exception e) {
        promise.reject("Error on trackInAppMessageImpression", e);
      }
    }

    @ReactMethod
    public void trackInAppMessageClick(String actionToken, ReadableMap props, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Properties properties = new Properties();
        if (props != null) {
          properties.putAll(props.toHashMap());
        }

        Log.d("Snapyr", "snapyr trackInAppMessageClick: " + actionToken);
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.trackInAppMessageClick(actionToken, properties);
        promise.resolve(null);
      } catch (Exception e) {
        promise.reject("Error on trackInAppMessageClick", e);
      }
    }

    @ReactMethod
    public void trackInAppMessageDismiss(String actionToken, Promise promise) {
      try {
        if (!Snapyr.Valid()) {
          promise.reject("Snapyr SDK has not yet been configured. Call `configure()` before using this method.");
          return;
        }

        Log.d("Snapyr", "snapyr trackInAppMessageDismiss: " + actionToken);
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        inst.trackInAppMessageDismiss(actionToken);
        promise.resolve(null);
      } catch (Exception e) {
        promise.reject("Error on trackInAppMessageDismiss", e);
      }
    }

    @ReactMethod
    public void reset(Promise promise) {
      try {
        Snapyr inst = Snapyr.with(this.getCurrentContext());
        Snapyr.clearSingleton();
        inst.shutdown();
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on reset");
        promise.reject("Error on reset", e);
      }
    }

    /**
     Native Android -> React Native events (trigger RN callbacks from native)
     */

    @ReactMethod
    public void addListener(String eventName) {
      Log.d("Snapyr.RN", "addListener(): " + eventName);
      // Set up any upstream listeners or background tasks as necessary
      // Stub - nothing to do here so far but method signature is required
    }

    @ReactMethod
    public void removeListeners(Integer count) {
      Log.d("Snapyr.RN", "removeListeners(): " + count);
      // Remove upstream listeners, stop unnecessary background tasks
      // Stub - nothing to do here so far but method signature is required
    }

    private void handleInAppMessage(InAppMessage message) {
//      Arguments.
      WritableNativeMap map = Arguments.makeNativeMap(message.asValueMap());
      this.getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("snapyrInAppMessage", map);
    }

    /**
     * React Native typically starts the main activity before intializing native modules like this one.
     * As a result, Snapyr's activity lifecycle callbacks will be registered after some lifecycle events
     * are already finished, and those callbacks won't be automatically triggered for the main activity.
     * After we have the activity AND Snapyr is initialized, manually "replay" the activity lifecycle
     * events up to "resume" to ensure Snapyr tracks them and knows about the activity.
     * NB Snapyr will handle deduplication of these calls automatically
     */
    public void replayActivityCallbacks() {
      Activity activity = this.getCurrentActivity();
      if (!snapyrConfigured || activityCallbacksReplayed || activity == null) {
        return;
      }

      Snapyr inst = Snapyr.with(activity);

      inst.replayLifecycleOnActivityCreated(activity, null);
      inst.replayLifecycleOnActivityStarted(activity);
      inst.replayLifecycleOnActivityResumed(activity);
      this.activityCallbacksReplayed = true;
    }

    public void checkInitialActivityIntent() {
      Activity activity = this.getCurrentActivity();
      if (activity == null) {
        Log.e("Snapyr.RN", "checkInitialActivityIntent: NO ACTIVITY???");
        return;
      }
      Intent intent = activity.getIntent();
      if (intent == null) {
        Log.e("Snapyr.RN", "checkInitialActivityIntent: NO INTENT???");
        return;
      }
      Log.d("Snapyr.RN", "checkInitialActivityIntent: proceeding...");

      SnapyrNotification snapyrNotification;
//      try {
      snapyrNotification = intent.getParcelableExtra("snapyr.notification");
      WritableNativeMap snapyrNotifMap = (snapyrNotification != null) ? Arguments.makeNativeMap(snapyrNotification.asValueMap()) : null;
//    } catch (Exception e) {}
      intent.removeExtra("snapyr.notification");
      WritableMap intentExtrasMap = (intent.getExtras() != null) ? Arguments.fromBundle(intent.getExtras()) : null;

      WritableMap map = Arguments.createMap();

//      Bundle bundle = new Bundle();
      map.putMap("snapyr.snapyrNotification", snapyrNotifMap);
      map.putMap("snapyr.intent.extras", intentExtrasMap);
      map.putString("snapyr.intent.data", intent.getDataString());
      map.putString("snapyr.intent.action", intent.getAction());
      map.putString("snapyr.intent.package", intent.getPackage());
//      map.putString("snapyr.intent.componentToString", intent.getComponent().toString());
//      map.putString("snapyr.intent.componentFlattenToString", intent.getComponent().flattenToString());
      map.putString("snapyr.intent.componentGetClassName", intent.getComponent().getClassName());

      this.initialIntentMap = map.copy();

//      this.getReactApplicationContext()
//        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//        .emit("initialIntentTest", map);
    }

    @Override
    public void onHostResume() {
      // Called when React Native moves the main activity to ready state. "Replay" should generally
      // take place in {@link #configure()}, but try again here just in case resume occurs after
      // initialization.
      Log.d("Snapyr.RN", "onHostResume()");
      this.replayActivityCallbacks();
      this.checkInitialActivityIntent();
    }

    @Override
    public void onHostPause() {
      Log.d("Snapyr.RN", "onHostPause()");
      // stub for LifecycleEventListener interface
    }

    @Override
    public void onHostDestroy() {
      Log.d("Snapyr.RN", "onHostDestroy()");
      // stub for LifecycleEventListener interface
    }

    @Override
    public void onActivityResult(Activity activity, int i, int i1, @Nullable Intent intent) {
      Log.d("Snapyr.RN", MessageFormat.format("onActivityResult:\n\tactivity: {0}\n\ti: {1}\n\ti1: {2}\n\tintent: {3}", activity, i, i1, intent));
    }

    @Override
    public void onNewIntent(Intent intent) {
      SnapyrNotification snapyrNotification = intent.getParcelableExtra("snapyr.notification");
      ValueMap notifMap = (snapyrNotification != null) ? snapyrNotification.asValueMap() : null;
      Log.d("Snapyr.RN", MessageFormat.format("onNewIntent:\n\tintent: {0}\n\tsnapyrNotif: {1}", intent, notifMap));
      String launchStatus = intent.getStringExtra("snapyr.launchStatus");
      if (launchStatus != null && launchStatus.startsWith("normal")) {
        Log.i("Snapyr.RN", "onNewIntent: launch status normal: " + launchStatus);
        if (snapyrNotification == null) {
          Log.e("Snapyr.RN", "onNewIntent: NO SNAPYRNOTIFICATION");
          return;
        }
        WritableNativeMap map = Arguments.makeNativeMap(snapyrNotification.asValueMap());
        this.getReactApplicationContext()
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("onNewIntentTest", map);
      } else {
        Log.e("Snapyr.RN", "onNewIntent: NON-NORMAL LAUNCH STATUS: " + launchStatus);
      }
    }
}

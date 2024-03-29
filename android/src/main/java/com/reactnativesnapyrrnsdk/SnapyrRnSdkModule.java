package com.reactnativesnapyrrnsdk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.snapyr.sdk.Properties;
import com.snapyr.sdk.Snapyr;
import com.snapyr.sdk.Traits;
import com.snapyr.sdk.http.ConnectionFactory;
import com.snapyr.sdk.inapp.InAppConfig;

@ReactModule(name = SnapyrRnSdkModule.NAME)
public class SnapyrRnSdkModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String NAME = "SnapyrRnSdk";
    private Boolean snapyrConfigured = false;
    private Boolean activityCallbacksReplayed = false;
    private WritableMap initialIntentMap = null;

    private Context getCurrentContext() {
      Context context = this.getCurrentActivity();
      if (context == null) {
        context = this.getReactApplicationContext();
      }
      return context;
    }

    public SnapyrRnSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
        // Not using instance here but calling this ensures it has this current context
        SnapyrRnInAppHandler.getInstance(reactContext);
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    // See https://reactnative.dev/docs/native-modules-android

    @ReactMethod
    public void configure(String withKey, ReadableMap options, Promise promise) {
      try {
        Snapyr.Builder builder = new Snapyr.Builder(this.getCurrentContext(), withKey)
        .enableSnapyrPushHandling() // enable push for Android
        .configureInAppHandling(
          new InAppConfig()
            .setActionCallback(
              SnapyrRnInAppHandler.getInstance(this.getReactApplicationContext())
            ));

        if (options != null) {
          if (options.hasKey("trackApplicationLifecycleEvents") && options.getBoolean("trackApplicationLifecycleEvents")) {
            builder.trackApplicationLifecycleEvents(); // Enable this to record certain application events automatically
          }

          if (options.hasKey("recordScreenViews") && options.getBoolean("recordScreenViews")) {
            builder.recordScreenViews(); // Enable this to record screen views automatically
          }

          if (options.hasKey("debug") && options.getBoolean("debug")) {
            builder.logLevel(Snapyr.LogLevel.INFO);
          }

          if (options.hasKey("flushQueueSize")) {
            builder.flushQueueSize(options.getInt("flushQueueSize"));
          } else {
            builder.flushQueueSize(1); // default - makes every event flush to network immediately
          }

          if (options.hasKey("snapyrEnvironment")) {
            try {
              ConnectionFactory.Environment env = ConnectionFactory.Environment.values()[options.getInt("snapyrEnvironment")];
              builder.snapyrEnvironment(env);
            } catch (Exception e) {
              Log.e("Snapyr", "Invalid environment provided");
            }
          }
        }

        Snapyr snapyr = builder.build();

        // Set the initialized instance as a globally accessible instance.
        Snapyr.setSingletonInstance(snapyr);
        snapyrConfigured = true;
        this.replayActivityCallbacks();
        promise.resolve(withKey);
      } catch (Exception e) {
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
      // Set up any upstream listeners or background tasks as necessary
      // Stub - nothing to do here so far but method signature is required
    }

    @ReactMethod
    public void removeListeners(Integer count) {
      // Remove upstream listeners, stop unnecessary background tasks
      // Stub - nothing to do here so far but method signature is required
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

    @Override
    public void onHostResume() {
      // Called when React Native moves the main activity to ready state. "Replay" should generally
      // take place in {@link #configure()}, but try again here just in case resume occurs after
      // initialization.
      this.replayActivityCallbacks();
    }

    @Override
    public void onHostPause() {
      // stub for LifecycleEventListener interface
    }

    @Override
    public void onHostDestroy() {
      // stub for LifecycleEventListener interface
    }
}

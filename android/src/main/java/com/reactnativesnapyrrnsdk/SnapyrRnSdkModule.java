package com.reactnativesnapyrrnsdk;

import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Objects;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;


import com.snapyr.sdk.Snapyr;
import com.snapyr.sdk.Traits;
import com.snapyr.sdk.Properties;
import com.snapyr.sdk.http.ConnectionFactory;

@ReactModule(name = SnapyrRnSdkModule.NAME)
public class SnapyrRnSdkModule extends ReactContextBaseJavaModule {
    public static final String NAME = "SnapyrRnSdk";
    static volatile Snapyr singleton = null;

    public SnapyrRnSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
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
        Snapyr.Builder builder = new Snapyr.Builder(this.getReactApplicationContext().getApplicationContext(), withKey)
        .flushQueueSize(1) // makes every event flush to network immediately
        .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically
        .recordScreenViews() // Enable this to record screen views automatically
        .enableSnapyrPushHandling(); // enable push for Android

        if (options.hasKey("snapyrEnvironment")) {
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
        promise.resolve(withKey);
      } catch(Exception e) {
        Log.d("Snapyr", "Config errored");
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
        if (!Objects.isNull(traitsMap)) {
          traits.putAll(traitsMap.toHashMap());
        }
        Log.d("Snapyr", "identify" + userId);
        Snapyr inst = Snapyr.with(this.getReactApplicationContext().getApplicationContext());
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
        if (!Objects.isNull(props)) {
          properties.putAll(props.toHashMap());
        }
        Log.d("Snapyr", "snapyr track " + eventName);
        Snapyr inst = Snapyr.with(this.getReactApplicationContext().getApplicationContext());
        inst.track(eventName, properties, null);
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on track");
        promise.reject("Error on track", e);
      }
    }

    @ReactMethod
    public void reset(Promise promise) {
      try {
        Snapyr inst = Snapyr.with(this.getReactApplicationContext().getApplicationContext());
        Snapyr.clearSingleton();
        inst.shutdown();
        promise.resolve(null);
      } catch (Exception e) {
        Log.d("Snapyr", "Error on reset");
        promise.reject("Error on reset", e);
      }
    }
}

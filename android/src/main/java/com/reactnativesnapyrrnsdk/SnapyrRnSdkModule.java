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


    // Example method
    // See https://reactnative.dev/docs/native-modules-android
    @ReactMethod
    public void multiply(int a, int b, Promise promise) {
        promise.resolve(a * b);
    }

    @ReactMethod
    public void configure(String withKey, ReadableMap options, Promise promise) {
      Snapyr snapyr = new Snapyr.Builder(this.getReactApplicationContext().getApplicationContext(), withKey)
      .enableDevEnvironment()
      .trackApplicationLifecycleEvents() // Enable this to record certain application events automatically
      .recordScreenViews() // Enable this to record screen views automatically
      .enableSnapyrPushHandling() // enable push for Android
      .build();

      // enable dev here

      // Snapyr snapyr = builder.build();

      // Set the initialized instance as a globally accessible instance.
      try {
        Snapyr.setSingletonInstance(snapyr);
      } catch(Exception ex) {
        Log.d("Snapyr", "Config errored");
      }
      promise.resolve(withKey);
    }

    @ReactMethod
    public void identify(String userId, ReadableMap traitsMap, Promise promise) {
      Traits traits = new Traits();
      if (!Objects.isNull(traitsMap)) {
        traits.putAll(traitsMap.toHashMap());
      }
      Log.d("Snapyr", "identify" + userId);
      Snapyr inst = Snapyr.with(this.getReactApplicationContext().getApplicationContext());
      Log.d("Snapyr", inst.toString());
      inst.identify(userId, traits, null);
      inst.flush();
      //Toast.makeText(this.getReactApplicationContext(),"Identified " + userId, Toast.LENGTH_LONG);
      promise.resolve(null);
    }


    @ReactMethod
    public void track(String eventName, ReadableMap props, Promise promise) {
      Properties properties = new Properties();
      if (!Objects.isNull(props)) {
        properties.putAll(props.toHashMap());
      }
      Log.d("Snapyr", "snapyr track " + eventName);
      Snapyr inst = Snapyr.with(this.getReactApplicationContext().getApplicationContext());
      inst.track(eventName, properties, null);
      inst.flush();
      promise.resolve(null);
    }

    public static native int nativeMultiply(int a, int b);
}

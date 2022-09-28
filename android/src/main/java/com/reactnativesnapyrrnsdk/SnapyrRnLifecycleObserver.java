package com.reactnativesnapyrrnsdk;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class SnapyrRnLifecycleObserver implements ActivityLifecycleCallbacks, DefaultLifecycleObserver {
  public SnapyrRnLifecycleObserver() {
    Log.e("Snapyr", "Lifecycle observer constructed 3...");
  }
  
  @Override
  public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    Log.e("Snapyr", "RN Lifecycle: created");
  }

  @Override
  public void onActivityStarted(@NonNull Activity activity) {
    Log.e("Snapyr", "RN Lifecycle: started");
  }

  @Override
  public void onActivityResumed(@NonNull Activity activity) {
    Log.e("Snapyr", "RN Lifecycle: resumed");
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity) {
    Log.e("Snapyr", "RN Lifecycle: paused");
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity) {
    Log.e("Snapyr", "RN Lifecycle: stopped");
  }

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    Log.e("Snapyr", "RN Lifecycle: save instance state");
  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity) {
    Log.e("Snapyr", "RN Lifecycle: destroyed");
  }


  // App-level

  /**
   * Notifies that {@code ON_CREATE} event occurred.
   * <p>
   * This method will be called after the {@link LifecycleOwner}'s {@code onCreate}
   * method returns.
   *
   * @param owner the component, whose state was changed
   */
  @Override
  public void onCreate(@NonNull LifecycleOwner owner) {
    Log.e("Snapyr", "RN Lifecycle APP: create");
  }

  /**
   * Notifies that {@code ON_START} event occurred.
   * <p>
   * This method will be called after the {@link LifecycleOwner}'s {@code onStart} method returns.
   *
   * @param owner the component, whose state was changed
   */
  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    Log.e("Snapyr", "RN Lifecycle APP: start");
  }

  /**
   * Notifies that {@code ON_RESUME} event occurred.
   * <p>
   * This method will be called after the {@link LifecycleOwner}'s {@code onResume}
   * method returns.
   *
   * @param owner the component, whose state was changed
   */
  @Override
  public void onResume(@NonNull LifecycleOwner owner) {
    Log.e("Snapyr", "RN Lifecycle APP: resume");
  }

  /**
   * Notifies that {@code ON_PAUSE} event occurred.
   * <p>
   * This method will be called before the {@link LifecycleOwner}'s {@code onPause} method
   * is called.
   *
   * @param owner the component, whose state was changed
   */
  @Override
  public void onPause(@NonNull LifecycleOwner owner) {
    Log.e("Snapyr", "RN Lifecycle APP: pause");
  }

  /**
   * Notifies that {@code ON_STOP} event occurred.
   * <p>
   * This method will be called before the {@link LifecycleOwner}'s {@code onStop} method
   * is called.
   *
   * @param owner the component, whose state was changed
   */
  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    Log.e("Snapyr", "RN Lifecycle APP: stop");
  }

  /**
   * Notifies that {@code ON_DESTROY} event occurred.
   * <p>
   * This method will be called before the {@link LifecycleOwner}'s {@code onDestroy} method
   * is called.
   *
   * @param owner the component, whose state was changed
   */
  @Override
  public void onDestroy(@NonNull LifecycleOwner owner) {
    Log.e("Snapyr", "RN Lifecycle APP: destroy");
  }
}

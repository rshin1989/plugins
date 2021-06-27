// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.navermaps;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.naver.maps.map.NaverMap;
import com.naver.maps.map.util.FusedLocationSource;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter;

/**
 * Plugin for controlling a set of NaverMap views to be shown as overlays on top of the Flutter
 * view. The overlay should be hidden during transformations or while Flutter is rendering on top of
 * the map. A Texture drawn using NaverMap bitmap snapshots can then be shown instead of the
 * overlay.
 */
public class NaverMapsPlugin implements FlutterPlugin, ActivityAware {

  @Nullable private Lifecycle lifecycle;

  private static final String VIEW_TYPE = "plugins.flutter.io/naver_maps";

  @SuppressWarnings("deprecation")
  public static void registerWith(
      final io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
    final Activity activity = registrar.activity();
    if (activity == null) {
      // When a background flutter view tries to register the plugin, the registrar has no activity.
      // We stop the registration process as this plugin is foreground only.
      return;
    }
    if (activity instanceof LifecycleOwner) {
      registrar
          .platformViewRegistry()
          .registerViewFactory(
              VIEW_TYPE,
              new NaverMapFactory(
                  registrar.messenger(),
                  new LifecycleProvider() {
                    @Override
                    public Lifecycle getLifecycle() {
                      return ((LifecycleOwner) activity).getLifecycle();
                    }
                  }));
    } else {
      registrar
          .platformViewRegistry()
          .registerViewFactory(
              VIEW_TYPE,
              new NaverMapFactory(registrar.messenger(), new ProxyLifecycleProvider(activity)));
    }
  }

  public NaverMapsPlugin() {}

  // FlutterPlugin

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
      Log.d("NaverMap", "onAttachedToEngine()");
    binding
        .getPlatformViewRegistry()
        .registerViewFactory(
            VIEW_TYPE,
            new NaverMapFactory(
                binding.getBinaryMessenger(),
                new LifecycleProvider() {
                  @Nullable
                  @Override
                  public Lifecycle getLifecycle() {
                    return lifecycle;
                  }
                }));
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
      Log.d("NaverMap", "onDetachedFromEngine()");
  }

  // ActivityAware

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
      Log.d("NaverMap", "onAttachedToActivity()");

      Log.d("NaverMap", "로케이션 소스를 생성합니다.");
      if (NaverMapController.locationSource == null) {
        NaverMapController.locationSource = new FusedLocationSource(binding.getActivity(), NaverMapController.LOCATION_PERMISSION_REQUEST_CODE);
      }

    lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding);
  }

  @Override
  public void onDetachedFromActivity() {
      Log.d("NaverMap", "onDetachedFromActivity()");
    lifecycle = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
      Log.d("NaverMap", "onReattachedToActivityForConfigChanges()");
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
      Log.d("NaverMap", "onReattachedToActivityForConfigChanges()");
    onDetachedFromActivity();
  }

  /**
   * This class provides a {@link LifecycleOwner} for the activity driven by {@link
   * ActivityLifecycleCallbacks}.
   *
   * <p>This is used in the case where a direct Lifecycle/Owner is not available.
   */
  private static final class ProxyLifecycleProvider
      implements ActivityLifecycleCallbacks, LifecycleOwner, LifecycleProvider {

    private final LifecycleRegistry lifecycle = new LifecycleRegistry(this);
    private final int registrarActivityHashCode;

    private ProxyLifecycleProvider(Activity activity) {
      this.registrarActivityHashCode = activity.hashCode();
      activity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d("NaverMap", "onActivityCreated()");
      if (activity.hashCode() != registrarActivityHashCode) {
        return;
      }
      lifecycle.handleLifecycleEvent(Event.ON_CREATE);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("NaverMap", "onActivityStarted()");
      if (activity.hashCode() != registrarActivityHashCode) {
        return;
      }
      lifecycle.handleLifecycleEvent(Event.ON_START);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d("NaverMap", "onActivityResumed()");
      if (activity.hashCode() != registrarActivityHashCode) {
        return;
      }
      lifecycle.handleLifecycleEvent(Event.ON_RESUME);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d("NaverMap", "onActivityPaused()");
      if (activity.hashCode() != registrarActivityHashCode) {
        return;
      }
      lifecycle.handleLifecycleEvent(Event.ON_PAUSE);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d("NaverMap", "onActivityStopped()");
      if (activity.hashCode() != registrarActivityHashCode) {
        return;
      }
      lifecycle.handleLifecycleEvent(Event.ON_STOP);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d("NaverMap", "onActivityDestroyed()");
      if (activity.hashCode() != registrarActivityHashCode) {
        return;
      }
      activity.getApplication().unregisterActivityLifecycleCallbacks(this);
      lifecycle.handleLifecycleEvent(Event.ON_DESTROY);


    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
      return lifecycle;
    }
  }
}

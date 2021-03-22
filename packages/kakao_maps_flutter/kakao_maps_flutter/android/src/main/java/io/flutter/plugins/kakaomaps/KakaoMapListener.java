// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package android.src.main.java.io.flutter.plugins.kakaomaps;

import com.naver.maps.map.NaverMap;

interface KakaoMapListener
        extends NaverMap.OnCameraChangeListener,
        NaverMap.OnCameraIdleListener,
        NaverMap.OnIndoorSelectionChangeListener,
        NaverMap.OnMapClickListener,
        NaverMap.OnMapLongClickListener,
        NaverMap.OnMapDoubleTapListener,
        NaverMap.OnMapTwoFingerTapListener,
        NaverMap.OnOptionChangeListener,
        NaverMap.OnSymbolClickListener,
        NaverMap.SnapshotReadyCallback,
        NaverMap.OnLocationChangeListener {
}

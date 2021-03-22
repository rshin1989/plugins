// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.navermaps;

import com.naver.maps.map.NaverMap;

interface NaverMapListener
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

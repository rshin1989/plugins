// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.kakaomaps;

import com.naver.maps.map.KakaoMap;

interface KakaoMapListener
        extends KakaoMap.OnCameraChangeListener,
        KakaoMap.OnCameraIdleListener,
        KakaoMap.OnIndoorSelectionChangeListener,
        KakaoMap.OnMapClickListener,
        KakaoMap.OnMapLongClickListener,
        KakaoMap.OnMapDoubleTapListener,
        KakaoMap.OnMapTwoFingerTapListener,
        KakaoMap.OnOptionChangeListener,
        KakaoMap.OnSymbolClickListener,
        KakaoMap.SnapshotReadyCallback,
        KakaoMap.OnLocationChangeListener {
}

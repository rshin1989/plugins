// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// ignore_for_file: public_member_api_docs
// ignore_for_file: unawaited_futures

import 'package:flutter/material.dart';
import 'dart:ui' as ui;
import 'package:flutter/services.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:naver_maps_flutter/naver_maps_flutter.dart';

import 'page.dart';

class MarkerIconsPage extends NaverMapExampleAppPage {
  MarkerIconsPage() : super(const Icon(Icons.image), 'Marker icons');

  @override
  Widget build(BuildContext context) {
    return const MarkerIconsBody();
  }
}

class MarkerIconsBody extends StatefulWidget {
  const MarkerIconsBody();

  @override
  State<StatefulWidget> createState() => MarkerIconsBodyState();
}

const LatLng _kMapCenter = LatLng(37.3591784, 127.1048319);

class MarkerIconsBodyState extends State<MarkerIconsBody> {
  NaverMapController? controller;
  BitmapDescriptor? _markerIcon;

  @override
  Widget build(BuildContext context) {
    _createMarkerImageFromAsset(context);
    // _createMarkerImageFromSvgAsset(context);
    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Center(
          child: SizedBox(
            width: 350.0,
            height: 300.0,
            child: NaverMap(
              initialCameraPosition: const CameraPosition(
                target: _kMapCenter,
                zoom: 7.0,
              ),
              markers: <Marker>{_createMarker()},
              onMapCreated: _onMapCreated,
            ),
          ),
        )
      ],
    );
  }

  Marker _createMarker() {
    if (_markerIcon != null) {
      return Marker(
        markerId: MarkerId("marker_1"),
        position: _kMapCenter,
        icon: _markerIcon!,
      );
    } else {
      return Marker(
        markerId: MarkerId("marker_1"),
        position: _kMapCenter,
      );
    }
  }

  Future<void> _createMarkerImageFromSvgAsset(BuildContext context) async {
    String assetName = 'assets/ic_marker.svg';
    // Read SVG file as String
    String svgString =
        await DefaultAssetBundle.of(context).loadString(assetName);
    // Create DrawableRoot from SVG String
    DrawableRoot svgDrawableRoot =
        await svg.fromSvgString(svgString, assetName);

    // toPicture() and toImage() don't seem to be pixel ratio aware, so we calculate the actual sizes here
    MediaQueryData queryData = MediaQuery.of(context);
    double devicePixelRatio = queryData.devicePixelRatio;

    // where 32 is your SVG's original width
    double width = 32 * devicePixelRatio;
    double height = 32 * devicePixelRatio; // same thing

    // Convert to ui.Picture
    ui.Picture picture = svgDrawableRoot.toPicture(size: Size(width, height));

    // Convert to ui.Image. toImage() takes width and height as parameters
    // you need to find the best size to suit your needs and take into account the
    // screen DPI
    ui.Image image = await picture.toImage(width.toInt(), height.toInt());
    ByteData? bytes = await image.toByteData(format: ui.ImageByteFormat.png);
    BitmapDescriptor bitmapDescriptior = BitmapDescriptor.fromBytes(
      bytes!.buffer.asUint8List(),
    );
    _updateBitmap(bitmapDescriptior);
  }

  Future<void> _createMarkerImageFromAsset(BuildContext context) async {
    if (_markerIcon == null) {
      final ImageConfiguration imageConfiguration =
          createLocalImageConfiguration(context, size: Size.square(48));
      BitmapDescriptor.fromAssetImage(
              imageConfiguration, 'assets/red_square.png')
          .then(_updateBitmap);
    }
  }

  void _updateBitmap(BitmapDescriptor bitmap) {
    setState(() {
      _markerIcon = bitmap;
    });
  }

  void _onMapCreated(NaverMapController controllerParam) {
    setState(() {
      controller = controllerParam;
    });
  }
}

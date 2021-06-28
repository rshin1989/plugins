package io.flutter.plugins.kakao.deeplink.kakao_deeplink_flutter

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** KakaoDeeplinkFlutterPlugin */
class KakaoDeeplinkFlutterPlugin: FlutterPlugin, MethodCallHandler {
  private lateinit var channel : MethodChannel

  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "kakao_deeplink_flutter")
    channel.setMethodCallHandler(this)

    if (NaviClient.instance.isKakaoNaviInstalled(context)) {
      Log.d("KakaoDeepLinkFlutterPlugin", "카카오내비 앱으로 길안내 가능")
    } else {
      Log.d("KakaoDeepLinkFlutterPlugin", "카카오내비 미설치: 웹 길안내 사용 권장")
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}

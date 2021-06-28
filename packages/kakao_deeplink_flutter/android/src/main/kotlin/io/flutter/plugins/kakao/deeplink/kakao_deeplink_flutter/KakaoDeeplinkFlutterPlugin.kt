package io.flutter.plugins.kakao.deeplink.kakao_deeplink_flutter

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.navi.NaviClient
import com.kakao.sdk.navi.model.CoordType
import com.kakao.sdk.navi.model.Location
import com.kakao.sdk.navi.model.NaviOption

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONObject

/** KakaoDeeplinkFlutterPlugin */
class KakaoDeeplinkFlutterPlugin: FlutterPlugin, MethodCallHandler {
  private val COMMAND_CHANNEL = "io.flutter.plugins.kakao.deeplink/command"

  private lateinit var methodChannel : MethodChannel

  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.applicationContext

    KakaoSdk.init(context, "d1afa5ae53d538d426ef7ae845890510")

    methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, COMMAND_CHANNEL)
    methodChannel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "isKaKaoNaviInstalled" -> {
        var isKakaoNaviInstalled = NaviClient.instance.isKakaoNaviInstalled(context)
        result.success(isKakaoNaviInstalled)
      }
      "navigateTo" -> {
        val arguments = JSONObject(call.arguments.toString())
        val placeName: String = arguments.get("placeName") as String
        val latitude: String = arguments.get("latitude") as String
        val longitude: String = arguments.get("longitude") as String

        // 카카오내비 앱으로 길안내
        startActivity(context, NaviClient.instance.navigateIntent(
            Location(placeName, longitude, latitude),
            NaviOption(coordType = CoordType.WGS84)
          ), null)
      }
      else -> {
        result.notImplemented()
      }
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel.setMethodCallHandler(null)
  }
}

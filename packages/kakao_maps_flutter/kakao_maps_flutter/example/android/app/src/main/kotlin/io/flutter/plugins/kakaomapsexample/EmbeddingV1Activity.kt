package io.flutter.plugins.kakaomapsexample

import android.os.Bundle;
import android.os.PersistableBundle
import io.flutter.app.FlutterActivity
import io.flutter.plugins.kakaomaps.KakaoMapsPlugin

class EmbeddingV1Activity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        KakaoMapsPlugin.registerWith(registrarFor("io.flutter.plugins.kakaomaps.KakaoMapsPlugin"))
    }
}
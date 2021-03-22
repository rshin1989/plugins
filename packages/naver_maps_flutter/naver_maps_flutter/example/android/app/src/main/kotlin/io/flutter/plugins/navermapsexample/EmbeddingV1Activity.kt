package io.flutter.plugins.navermapsexample

import android.os.Bundle;
import android.os.PersistableBundle
import io.flutter.app.FlutterActivity
import io.flutter.plugins.navermaps.NaverMapsPlugin

class EmbeddingV1Activity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        NaverMapsPlugin.registerWith(registrarFor("io.flutter.plugins.navermaps.NaverMapsPlugin"))
    }
}
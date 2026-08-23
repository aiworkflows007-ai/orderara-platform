package com.orderara.partner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.orderara.partner.ui.navigation.PartnerNavGraph
import com.orderara.partner.ui.theme.OrderAraPartnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrderAraPartnerTheme(darkTheme = true) {
                PartnerNavGraph()
            }
        }
    }
}

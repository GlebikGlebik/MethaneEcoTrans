package com.methane.eco.trans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.compose.rememberNavController
import com.methane.eco.trans.theme.METTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.methane.eco.trans.presentation.enterscreen.EnterScreen
import com.methane.eco.trans.presentation.regscreen.RegistrationScreen

val segoe_ui_bold = FontFamily(
    Font(R.font.segoe_ui_bold)
)

val segoe_ui = FontFamily(
    Font(R.font.segoe_ui)
)

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            METTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "EnterScreen"){
                    composable("EnterScreen") { EnterScreen(navController) }
                    composable("RegistrationScreen") { RegistrationScreen(navController) }
                    composable("MainScreen") {MainScreen(navController)}
                    composable("ProfileScreen") {ProfileScreen(navController)}
                    composable("HistoryScreen") {HistoryScreen(navController)}
                }

            }
        }
    }

}

fun isDateValid(date: String): Boolean {
    val regex = """^(0[1-9]|[12][0-9]|3[01])\.(0[1-9]|1[012])\.\d{4}$""".toRegex()
    return date.matches(regex)
}


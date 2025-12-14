package com.app.punchinapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.app.punchinapplication.coachmark.CircleRevealEffect
import com.app.punchinapplication.coachmark.CoachMarkScope
import com.app.punchinapplication.coachmark.NoCoachMarkButtons
import com.app.punchinapplication.di.appModule
import com.app.punchinapplication.presentation.navigation.NavGraph
import com.app.punchinapplication.presentation.screen.ActionButton
import com.app.punchinapplication.ui.theme.PunchInApplicationTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Koin only if not already started
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
                modules(appModule)
            }
        }

        enableEdgeToEdge()
        
        // Check login state to determine start destination
        val prefs = getSharedPreferences("session_prefs", MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        val startDestination = if (isLoggedIn) {
            "home"
        } else {
            "login"
        }
        
        setContent {
            PunchInApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
/**
 * Example: FloatingActionButton with Coach Mark
 * Uses the generic withCoachMark wrapper
 */
@Composable fun CoachMarkScope.CoachMarkFloatingActionButton(onClick: () -> Unit = {}) {
    withCoachMark(
        position = 1,
        title = "Punch-in Data",
        description = "Click on this and check out the punch data This is a list of all punch-in lat lng data",
        revealEffect = CircleRevealEffect(),
        backgroundCoachStyle = NoCoachMarkButtons
    ) {
        FloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.outline_list),
                contentDescription = "phone"
            )
        }
    }
}

@Composable fun CoachMarkScope.CoachMarkTextView(text: String, description: String, position: Int) {
    withCoachMark(
        position = position,
        title = text,
        description = description,
        revealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
        backgroundCoachStyle = NoCoachMarkButtons
    ) {
        Text(
            text,
            fontSize = 20.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable fun CoachMarkScope.CoachMarkActionButton(onClick: () -> Unit = {},text: String, description: String, position: Int, icon: ImageVector, enabled : Boolean) {
    withCoachMark(
        position = position,
        title = text,
        description = description,
        revealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
        backgroundCoachStyle = NoCoachMarkButtons
    ) {
        ActionButton(
            icon = icon,
            text = text,
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Coach Mark IconButton with drawable resource icon
 * Use this when you have a drawable resource ID (R.drawable.*)
 */
@Composable fun CoachMarkScope.CoachMarkIconButton(
    onClick: () -> Unit = {},
    text: String, 
    description: String, 
    position: Int, 
    iconResId: Int
) {
    withCoachMark(
        position = position,
        title = text,
        description = description,
        revealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
        backgroundCoachStyle = NoCoachMarkButtons
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = text
            )
        }
    }
}
@Composable
fun CoachMarkScope.withCoachMark(
    position: Int,
    title: String = "",
    description: String = "",
    revealEffect: com.app.punchinapplication.coachmark.RevealEffect = com.app.punchinapplication.coachmark.DefaultRevealEffect,
    backgroundCoachStyle: com.app.punchinapplication.coachmark.CoachStyle = NoCoachMarkButtons,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.addTarget(
            position = position,
            revealEffect = revealEffect,
            backgroundCoachStyle = backgroundCoachStyle,
            content = {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 20.dp,
                        vertical = 10.dp
                    ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.animated_insightful_bulb),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )

                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = description,
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        )
    ) {
        content()
    }
}

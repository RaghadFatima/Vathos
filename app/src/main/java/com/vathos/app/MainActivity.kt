
package com.vathos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vathos.app.navigation.VathosNavigation
import com.vathos.app.ui.theme.VathosTheme
import com.vathos.app.ui.viewmodel.SettingsViewModel

private val Emerald = Color(0xFF10B981)
private val DarkBackground = Color(0xFF0B1411)
private val DarkCard = Color(0xFF14221C)
private val MutedText = Color(0xFFA7B8AF)
private val WhiteText = Color(0xFFF8FAFC)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vathosApplication = application as VathosApplication

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = vathosApplication.container.settingsViewModelFactory
            )

            val preferences by settingsViewModel.preferences.collectAsState()

            VathosTheme(
                themePreference = preferences.theme
            ) {
                VathosNavigation()
            }
        }
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF14251D),
                        DarkBackground,
                        Color(0xFF080E0C)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 38.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Brand logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        color = Emerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    color = Emerald,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "VATHOS",
                color = WhiteText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PRIVATE ON-DEVICE AI",
                color = Emerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(46.dp))

            // Privacy badge
            Row(
                modifier = Modifier
                    .background(
                        color = Emerald.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Emerald,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "BUILT AROUND PRIVACY",
                    color = Emerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Private AI for\nYour Business",
                color = WhiteText,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 43.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your business documents deserve a more private way to work with AI.",
                color = MutedText,
                fontSize = 15.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Main privacy message
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Emerald.copy(alpha = 0.14f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Emerald,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your data. Your device.",
                            color = WhiteText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "Designed for private, on-device AI workflows without relying on cloud processing.",
                            color = MutedText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "A smarter way to work privately",
                color = WhiteText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(15.dp))

            FeatureCard(
                icon = {
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = Emerald,
                        modifier = Modifier.size(23.dp)
                    )
                },
                title = "100% Offline by Design",
                description = "Work with your business information without depending on cloud AI services."
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = Emerald,
                        modifier = Modifier.size(23.dp)
                    )
                },
                title = "Privacy First",
                description = "Keep sensitive business information under your control with an on-device approach."
            )

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                icon = {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = Emerald,
                        modifier = Modifier.size(23.dp)
                    )
                },
                title = "Understand Documents Locally",
                description = "Designed to help summarize PDFs and explore business documents using local AI."
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Get Started button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald,
                    contentColor = Color(0xFF06251A)
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "PRIVATE AI  •  BUSINESS FOCUSED  •  ON-DEVICE",
                color = MutedText.copy(alpha = 0.75f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard.copy(alpha = 0.95f)
        )
    ) {
        Row(
            modifier = Modifier.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Emerald.copy(alpha = 0.12f),
                        RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = WhiteText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = description,
                    color = MutedText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

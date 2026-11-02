package com.traxxiontech.syncplayer.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traxxiontech.syncplayer.R
import com.traxxiontech.syncplayer.ui.components.FrostedGlassCard
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicButton
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glassColors = LocalGlassmorphicColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Card
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(2.5.dp, glassColors.glowColor, CircleShape)
                            .background(glassColors.surfaceGlassSecondary)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_sync_player_logo),
                            contentDescription = "Sync Player Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Sync Player",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "Your Complete Offline Media Experience",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = glassColors.glowColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "A high-performance offline media application featuring synchronized lyrics auto-scroll, tactile equalizer with acoustic bass boost, hardware-accelerated video cinema with gestures, photo gallery with EXIF details, offline peer transfer, and an encrypted media vault.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(glassColors.surfaceGlassSecondary)
                            .border(1.dp, glassColors.borderGlass.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Version 1.0.0 Pro • Build 2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Developer Identity Card
        item {
            Text(
                text = "Developer & Creator",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Vincent Ganiza",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Developer",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Traxxion Tech",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glassColors.glowColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Proudly Made in Zimbabwe 🇿🇼\n© 2026 All Rights Reserved",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SkeuomorphicButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Experience Sync Player — Your Complete Offline Media Experience with synchronized lyrics & equalizer. Created by Vincent Ganiza (Traxxion Tech) in Zimbabwe 🇿🇼. Visit: https://traxxiontech.com"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Sync Player"))
                        },
                        glowing = true
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Sync Player", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Clickable Contact Actions
        item {
            Text(
                text = "Contact & Links",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        item {
            ContactActionCard(
                icon = Icons.Filled.Language,
                title = "Official Website",
                subtitle = "traxxiontech.com",
                accentColor = Color(0xFF00E5FF),
                onClick = { openExternalUrl(context, "https://traxxiontech.com") }
            )
        }

        item {
            ContactActionCard(
                icon = Icons.Filled.OpenInBrowser,
                title = "GitHub Repository",
                subtitle = "github.com/LGT09",
                accentColor = Color(0xFFB388FF),
                onClick = { openExternalUrl(context, "https://github.com/LGT09") }
            )
        }

        item {
            ContactActionCard(
                icon = Icons.Filled.Email,
                title = "Email Developer",
                subtitle = "traxxiontech@gmail.com",
                accentColor = Color(0xFFFF5252),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:traxxiontech@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Sync Player Inquiries")
                    }
                    runCatching { context.startActivity(intent) }
                }
            )
        }

        item {
            ContactActionCard(
                icon = Icons.Filled.Share,
                title = "WhatsApp Chat",
                subtitle = "+263 716 857 999",
                accentColor = Color(0xFF00E676),
                onClick = {
                    openExternalUrl(context, "https://wa.me/263716857999?text=Hello%20Vincent,%20I%20am%20using%20Sync%20Player")
                }
            )
        }

        item {
            ContactActionCard(
                icon = Icons.Filled.Call,
                title = "Telephone Call",
                subtitle = "+263 716 857 999",
                accentColor = Color(0xFFFFD700),
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:+263716857999")
                    }
                    runCatching { context.startActivity(intent) }
                }
            )
        }

        item {
            ContactActionCard(
                icon = Icons.Filled.Language,
                title = "Facebook Page",
                subtitle = "Traxxion Tech",
                accentColor = Color(0xFF1877F2),
                onClick = {
                    openExternalUrl(context, "https://www.facebook.com/traxxiontech")
                }
            )
        }
    }
}

@Composable
fun ContactActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f))
                        .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(glassColors.surfaceGlassSecondary)
                    .border(1.dp, glassColors.glowColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Open",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.glowColor
                )
            }
        }
    }
}

fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: $url", Toast.LENGTH_SHORT).show()
    }
}

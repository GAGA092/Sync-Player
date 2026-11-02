package com.traxxiontech.syncplayer.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.traxxiontech.syncplayer.model.ThemeType
import com.traxxiontech.syncplayer.model.UserProfile
import com.traxxiontech.syncplayer.ui.components.FrostedGlassCard
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicButton
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors
import com.traxxiontech.syncplayer.ui.theme.ThemeEngine

@Composable
fun SettingsScreen(
    userProfile: UserProfile,
    onUpdateProfile: (UserProfile) -> Unit,
    onThemeChanged: (ThemeType) -> Unit,
    onSetNewVaultPin: (String) -> Unit,
    onRescanLibrary: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glassColors = LocalGlassmorphicColors.current

    var fullName by remember(userProfile.fullName) { mutableStateOf(userProfile.fullName) }
    var username by remember(userProfile.username) { mutableStateOf(userProfile.username) }
    var avatarUriString by remember(userProfile.avatarUriString) { mutableStateOf(userProfile.avatarUriString) }
    var selectedPreset by remember(userProfile.avatarPreset) { mutableIntStateOf(userProfile.avatarPreset) }
    var currentTheme by remember(userProfile.favoriteTheme) { mutableStateOf(userProfile.favoriteTheme) }

    var gaplessPlayback by remember { mutableStateOf(true) }
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var showPinDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUriString = uri.toString()
            val updated = userProfile.copy(avatarUriString = uri.toString())
            onUpdateProfile(updated)
            Toast.makeText(context, "Avatar photo updated", Toast.LENGTH_SHORT).show()
        }
    }

    val presetColors = listOf(
        Pair("Cyan Wave", Color(0xFF00E5FF)),
        Pair("Neon Magenta", Color(0xFFFF007F)),
        Pair("Gold Crown", Color(0xFFFFD700)),
        Pair("Emerald Pulse", Color(0xFF00E676)),
        Pair("Cosmic Violet", Color(0xFF7C4DFF)),
        Pair("Sunset Amber", Color(0xFFFF7A00))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "App Settings ⚙️",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Customize profile, active theme, security and audio engine",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // 1. Profile & Avatar Editor Section
        item {
            Text(
                text = "Profile & Identity",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val activeColor = presetColors[selectedPreset.coerceIn(0, presetColors.lastIndex)].second

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .border(3.dp, glassColors.glowColor, CircleShape)
                            .background(activeColor)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUriString.isNullOrEmpty()) {
                            AsyncImage(
                                model = avatarUriString,
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SkeuomorphicButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AddPhotoAlternate,
                                contentDescription = "Pick Photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (avatarUriString != null) "Change Photo" else "Upload Profile Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Or choose avatar preset color:", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(presetColors.indices.toList()) { index ->
                            val color = presetColors[index].second
                            val isSelected = selectedPreset == index && avatarUriString == null
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        selectedPreset = index
                                        avatarUriString = null
                                        val updated = userProfile.copy(avatarPreset = index, avatarUriString = null)
                                        onUpdateProfile(updated)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            unfocusedBorderColor = glassColors.borderGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        prefix = { Text("@", color = glassColors.glowColor) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            unfocusedBorderColor = glassColors.borderGlass,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SkeuomorphicButton(
                        onClick = {
                            val updated = userProfile.copy(
                                fullName = fullName.trim(),
                                username = username.trim(),
                                avatarPreset = selectedPreset,
                                avatarUriString = avatarUriString
                            )
                            onUpdateProfile(updated)
                            Toast.makeText(context, "Profile Saved", Toast.LENGTH_SHORT).show()
                        },
                        glowing = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        // 2. Theme Engine Selector
        item {
            Text(
                text = "Theme & Appearance",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Palette, contentDescription = "Theme", tint = glassColors.glowColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Active Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                        Text(currentTheme.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = glassColors.glowColor)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ThemeType.values()) { theme ->
                            val isSelected = currentTheme == theme
                            val (_, themeColors) = ThemeEngine.getColorScheme(theme, isSystemDark = true)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) themeColors.glowColor else glassColors.borderGlass.copy(alpha = 0.4f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(if (isSelected) themeColors.surfaceGlassSecondary else glassColors.surfaceGlass)
                                    .clickable {
                                        currentTheme = theme
                                        onThemeChanged(theme)
                                        onUpdateProfile(userProfile.copy(favoriteTheme = theme))
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = theme.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) themeColors.glowColor else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Vault Security Settings
        item {
            Text(
                text = "Privacy & Vault",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = "Vault PIN", tint = glassColors.glowColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Vault Security PIN", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("4-digit passcode for encrypted media vault", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }

                    SkeuomorphicButton(onClick = { showPinDialog = true }) {
                        Text("Change PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = glassColors.glowColor)
                    }
                }
            }
        }

        // 4. Audio & Playback Options
        item {
            Text(
                text = "Playback Engine",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Gapless Playback", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Continuous audio transition without silence", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = gaplessPlayback,
                            onCheckedChange = { gaplessPlayback = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = glassColors.glowColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Hardware Acceleration", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("GPU decoding for smooth 4K/60fps playback", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = hardwareAcceleration,
                            onCheckedChange = { hardwareAcceleration = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = glassColors.glowColor)
                        )
                    }
                }
            }
        }

        // 5. Library Rescan & About Link
        item {
            Text(
                text = "System & About",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        item {
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onRescanLibrary()
                        Toast.makeText(context, "Rescanning device storage...", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rescan", tint = glassColors.glowColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Rescan Media Index", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                    Text("Run", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glassColors.glowColor)
                }
            }
        }

        item {
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAbout() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = "About", tint = glassColors.glowColor)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("About Sync Player & Developer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Vincent Ganiza (Traxxion Tech) • Zimbabwe 🇿🇼", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                    Text("View", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = glassColors.glowColor)
                }
            }
        }
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newPin.length != 4) {
                            errorMsg = "PIN must be 4 digits"
                        } else if (newPin != confirmPin) {
                            errorMsg = "PINs do not match"
                        } else {
                            onSetNewVaultPin(newPin)
                            showPinDialog = false
                            Toast.makeText(context, "Vault PIN Changed", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save", color = glassColors.glowColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            },
            title = {
                Text("Change Vault PIN", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        label = { Text("New 4-digit PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4) confirmPin = it },
                        label = { Text("Confirm PIN") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = glassColors.glowColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMsg?.let {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = it, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            containerColor = Color(0xFF131722)
        )
    }
}

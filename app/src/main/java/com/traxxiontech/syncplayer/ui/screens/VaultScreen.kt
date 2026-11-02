package com.traxxiontech.syncplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.traxxiontech.syncplayer.model.MediaType
import com.traxxiontech.syncplayer.model.VaultMediaItem
import com.traxxiontech.syncplayer.security.VaultLockType
import com.traxxiontech.syncplayer.ui.components.FrostedGlassCard
import com.traxxiontech.syncplayer.ui.components.SkeuomorphicButton
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors

@Composable
fun VaultScreen(
    vaultItems: List<VaultMediaItem>,
    isConfigured: Boolean,
    lockType: VaultLockType,
    isBiometricEnabled: Boolean,
    onSetupVault: (VaultLockType, String, Boolean) -> Unit,
    onVerifyCredential: (String) -> Boolean,
    onRestoreItem: (String) -> Unit,
    onPlayAudio: ((VaultMediaItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassmorphicColors.current

    var isUnlocked by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableIntStateOf(0) } // 0: All, 1: Music, 2: Videos, 3: Photos
    var showSetupDialog by remember { mutableStateOf(false) }

    // If never configured, show setup flow
    var activeConfigured by remember(isConfigured) { mutableStateOf(isConfigured) }
    var activeLockType by remember(lockType) { mutableStateOf(lockType) }
    var activeBiometric by remember(isBiometricEnabled) { mutableStateOf(isBiometricEnabled) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("vault_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!activeConfigured) {
            // First time setup view
            VaultFirstTimeSetup(
                onComplete = { type, cred, bio ->
                    onSetupVault(type, cred, bio)
                    activeConfigured = true
                    activeLockType = type
                    activeBiometric = bio
                    isUnlocked = true
                }
            )
        } else if (!isUnlocked) {
            // Locked screen
            VaultUnlockView(
                lockType = activeLockType,
                isBiometricEnabled = activeBiometric,
                onVerify = { cred ->
                    val success = onVerifyCredential(cred)
                    if (success) isUnlocked = true
                    success
                },
                onBiometricUnlock = {
                    isUnlocked = true
                }
            )
        } else {
            // Unlocked Vault View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = "Unlocked",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vault Unlocked 🔒",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "AES-256 Military Grade Encrypted • ${vaultItems.size} items",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                Row {
                    IconButton(onClick = { showSetupDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Key,
                            contentDescription = "Change Lock",
                            tint = glassColors.glowColor
                        )
                    }
                    IconButton(onClick = { isUnlocked = false }) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Lock Vault",
                            tint = Color(0xFFFF5252)
                        )
                    }
                }
            }

            // Categories
            TabRow(
                selectedTabIndex = selectedCategory,
                containerColor = Color.Transparent,
                contentColor = glassColors.glowColor,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedCategory]),
                        color = glassColors.glowColor
                    )
                },
                divider = {}
            ) {
                listOf("All (${vaultItems.size})", "Music", "Videos", "Photos").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedCategory == index,
                        onClick = { selectedCategory = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == index) glassColors.glowColor else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            val filteredItems = when (selectedCategory) {
                1 -> vaultItems.filter { it.type == MediaType.AUDIO }
                2 -> vaultItems.filter { it.type == MediaType.VIDEO }
                3 -> vaultItems.filter { it.type == MediaType.PHOTO }
                else -> vaultItems
            }

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = "Empty",
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Vault is Empty",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Lock sensitive photos, songs, or videos from the Media Manager. They will be encrypted with AES-256 and removed from device galleries.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                when (selectedCategory) {
                    3 -> {
                        // Photos Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredItems) { item ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(glassColors.surfaceGlassSecondary)
                                        .border(1.dp, glassColors.borderGlass.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                ) {
                                    if (item.uri != null) {
                                        AsyncImage(
                                            model = item.uri,
                                            contentDescription = item.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Filled.Album,
                                            contentDescription = "Locked Photo",
                                            tint = glassColors.glowColor,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .clickable { onRestoreItem(item.id) }
                                            .padding(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.LockOpen,
                                            contentDescription = "Restore Item",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredItems) { item ->
                                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(glassColors.surfaceGlassSecondary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            val icon = when (item.type) {
                                                MediaType.AUDIO -> Icons.Filled.MusicNote
                                                MediaType.VIDEO -> Icons.Filled.Videocam
                                                MediaType.PHOTO -> Icons.Filled.Album
                                            }
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = item.type.name,
                                                tint = glassColors.glowColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "🔒 AES-256 Encrypted • ${item.type.name}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF00E676)
                                            )
                                        }

                                        if (item.type == MediaType.AUDIO && onPlayAudio != null) {
                                            IconButton(onClick = { onPlayAudio(item) }) {
                                                Icon(
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = glassColors.glowColor
                                                )
                                            }
                                        }

                                        IconButton(onClick = { onRestoreItem(item.id) }) {
                                            Icon(
                                                imageVector = Icons.Filled.LockOpen,
                                                contentDescription = "Restore Item to Device",
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSetupDialog) {
        VaultChangeCredentialDialog(
            currentType = activeLockType,
            onDismiss = { showSetupDialog = false },
            onSave = { type, cred, bio ->
                onSetupVault(type, cred, bio)
                activeLockType = type
                activeBiometric = bio
                showSetupDialog = false
            }
        )
    }
}

@Composable
fun VaultFirstTimeSetup(
    onComplete: (VaultLockType, String, Boolean) -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current

    var selectedType by remember { mutableStateOf(VaultLockType.PIN) }
    var credentialInput by remember { mutableStateOf("") }
    var confirmInput by remember { mutableStateOf("") }
    var patternDots by remember { mutableStateOf(listOf<Int>()) }
    var enableBiometrics by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(glassColors.surfaceGlassSecondary)
                .border(2.dp, glassColors.glowColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Secure Vault",
                tint = glassColors.glowColor,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to Secure Vault",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "First-time setup detected. Choose your secret security lock type to protect your personal files.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Lock Type Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(glassColors.surfaceGlass)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LockTypeChip("PIN", Icons.Filled.Pin, selectedType == VaultLockType.PIN) {
                selectedType = VaultLockType.PIN
                credentialInput = ""
                confirmInput = ""
                errorMsg = null
            }
            LockTypeChip("Password", Icons.Filled.Password, selectedType == VaultLockType.PASSWORD) {
                selectedType = VaultLockType.PASSWORD
                credentialInput = ""
                confirmInput = ""
                errorMsg = null
            }
            LockTypeChip("Pattern", Icons.Filled.Pattern, selectedType == VaultLockType.PATTERN) {
                selectedType = VaultLockType.PATTERN
                patternDots = emptyList()
                errorMsg = null
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedType) {
            VaultLockType.PIN -> {
                OutlinedTextField(
                    value = credentialInput,
                    onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) credentialInput = it },
                    label = { Text("Enter 4 to 6-digit PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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
                    value = confirmInput,
                    onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) confirmInput = it },
                    label = { Text("Confirm PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glassColors.glowColor,
                        unfocusedBorderColor = glassColors.borderGlass,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            VaultLockType.PASSWORD -> {
                OutlinedTextField(
                    value = credentialInput,
                    onValueChange = { credentialInput = it },
                    label = { Text("Enter Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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
                    value = confirmInput,
                    onValueChange = { confirmInput = it },
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glassColors.glowColor,
                        unfocusedBorderColor = glassColors.borderGlass,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            VaultLockType.PATTERN -> {
                Text(
                    text = "Tap 4 or more dots to create your pattern sequence:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                PatternGrid(
                    selectedDots = patternDots,
                    onDotClicked = { dot ->
                        if (!patternDots.contains(dot)) {
                            patternDots = patternDots + dot
                        }
                    },
                    onReset = { patternDots = emptyList() }
                )
            }
        }

        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMsg!!,
                color = Color(0xFFFF5252),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Biometrics option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(glassColors.surfaceGlassSecondary)
                .clickable { enableBiometrics = !enableBiometrics }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = enableBiometrics,
                onCheckedChange = { enableBiometrics = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = glassColors.glowColor,
                    checkmarkColor = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = "Biometrics",
                tint = glassColors.glowColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Enable Biometric Unlock (Fingerprint/Face)",
                fontSize = 12.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SkeuomorphicButton(
            onClick = {
                when (selectedType) {
                    VaultLockType.PIN -> {
                        if (credentialInput.length < 4) {
                            errorMsg = "PIN must be at least 4 digits"
                        } else if (credentialInput != confirmInput) {
                            errorMsg = "PINs do not match"
                        } else {
                            onComplete(selectedType, credentialInput, enableBiometrics)
                        }
                    }

                    VaultLockType.PASSWORD -> {
                        if (credentialInput.length < 4) {
                            errorMsg = "Password must be at least 4 characters"
                        } else if (credentialInput != confirmInput) {
                            errorMsg = "Passwords do not match"
                        } else {
                            onComplete(selectedType, credentialInput, enableBiometrics)
                        }
                    }

                    VaultLockType.PATTERN -> {
                        if (patternDots.size < 4) {
                            errorMsg = "Connect at least 4 dots"
                        } else {
                            val cred = patternDots.joinToString("-")
                            onComplete(selectedType, cred, enableBiometrics)
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Lock & Initialize Vault",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun VaultUnlockView(
    lockType: VaultLockType,
    isBiometricEnabled: Boolean,
    onVerify: (String) -> Boolean,
    onBiometricUnlock: () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current

    var enteredCred by remember { mutableStateOf("") }
    var enteredPattern by remember { mutableStateOf(listOf<Int>()) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(glassColors.surfaceGlassSecondary)
                .border(2.dp, glassColors.glowColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Security,
                contentDescription = "Vault Locked",
                tint = glassColors.glowColor,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Vault is Locked",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (lockType) {
                VaultLockType.PIN -> "Enter your security PIN"
                VaultLockType.PASSWORD -> "Enter your vault password"
                VaultLockType.PATTERN -> "Draw your secret unlock pattern"
            },
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.65f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (lockType) {
            VaultLockType.PIN -> {
                // Pin dots
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredCred.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) glassColors.glowColor else glassColors.skeuoShadow)
                                .border(1.dp, if (isError) Color.Red else glassColors.borderGlass, CircleShape)
                        )
                    }
                }

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Incorrect Credential", color = Color(0xFFFF5252), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Keypad
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val keyRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫")
                    )
                    keyRows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            row.forEach { digit ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(glassColors.surfaceGlass)
                                        .border(1.dp, glassColors.borderGlass.copy(alpha = 0.3f), CircleShape)
                                    .clickable {
                                        isError = false
                                        when (digit) {
                                            "C" -> enteredCred = ""
                                            "⌫" -> if (enteredCred.isNotEmpty()) enteredCred = enteredCred.dropLast(1)
                                            else -> {
                                                if (enteredCred.length < 8) {
                                                    enteredCred += digit
                                                    if (enteredCred.length >= 4) {
                                                        if (onVerify(enteredCred)) {
                                                            // Success
                                                        } else if (enteredCred.length == 4) {
                                                            isError = true
                                                            enteredCred = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = digit, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            VaultLockType.PASSWORD -> {
                OutlinedTextField(
                    value = enteredCred,
                    onValueChange = {
                        enteredCred = it
                        isError = false
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glassColors.glowColor,
                        unfocusedBorderColor = glassColors.borderGlass,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Incorrect Password", color = Color(0xFFFF5252), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                SkeuomorphicButton(
                    onClick = {
                        val ok = onVerify(enteredCred)
                        if (!ok) isError = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Unlock Vault",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            VaultLockType.PATTERN -> {
                PatternGrid(
                    selectedDots = enteredPattern,
                    onDotClicked = { dot ->
                        if (!enteredPattern.contains(dot)) {
                            enteredPattern = enteredPattern + dot
                        }
                    },
                    onReset = {
                        enteredPattern = emptyList()
                        isError = false
                    }
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Incorrect Pattern", color = Color(0xFFFF5252), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                SkeuomorphicButton(
                    onClick = {
                        val cred = enteredPattern.joinToString("-")
                        val ok = onVerify(cred)
                        if (!ok) {
                            isError = true
                            enteredPattern = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Confirm Pattern",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Biometric Unlock button
        if (isBiometricEnabled) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(glassColors.surfaceGlassSecondary)
                    .border(1.dp, glassColors.glowColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onBiometricUnlock() }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Biometric Unlock",
                        tint = glassColors.glowColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Unlock with Biometrics",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun PatternGrid(
    selectedDots: List<Int>,
    onDotClicked: (Int) -> Unit,
    onReset: () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val isSelected = selectedDots.contains(index)
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) glassColors.glowColor.copy(alpha = 0.35f) else glassColors.surfaceGlass)
                                .border(
                                    2.dp,
                                    if (isSelected) glassColors.glowColor else glassColors.borderGlass.copy(alpha = 0.4f),
                                    CircleShape
                                )
                                .clickable { onDotClicked(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) glassColors.glowColor else Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onReset) {
            Text("Clear Pattern (${selectedDots.size} connected)", color = glassColors.glowColor, fontSize = 12.sp)
        }
    }
}

@Composable
fun LockTypeChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) glassColors.glowColor.copy(alpha = 0.3f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) glassColors.glowColor else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) glassColors.glowColor else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun VaultChangeCredentialDialog(
    currentType: VaultLockType,
    onDismiss: () -> Unit,
    onSave: (VaultLockType, String, Boolean) -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current
    var selectedType by remember { mutableStateOf(currentType) }
    var newCred by remember { mutableStateOf("") }
    var enableBiometrics by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Vault Security Lock", fontWeight = FontWeight.Bold, color = Color.White)
        },
        text = {
            Column {
                Text(
                    text = "Select new lock type and enter your new credential:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LockTypeChip("PIN", Icons.Filled.Pin, selectedType == VaultLockType.PIN) {
                        selectedType = VaultLockType.PIN
                        newCred = ""
                    }
                    LockTypeChip("Password", Icons.Filled.Password, selectedType == VaultLockType.PASSWORD) {
                        selectedType = VaultLockType.PASSWORD
                        newCred = ""
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = newCred,
                    onValueChange = { newCred = it },
                    label = { Text(if (selectedType == VaultLockType.PIN) "New 4-Digit PIN" else "New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glassColors.glowColor,
                        unfocusedBorderColor = glassColors.borderGlass,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enableBiometrics,
                        onCheckedChange = { enableBiometrics = it },
                        colors = CheckboxDefaults.colors(checkedColor = glassColors.glowColor)
                    )
                    Text("Enable Biometric Backup", fontSize = 12.sp, color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newCred.isNotBlank()) {
                        onSave(selectedType, newCred, enableBiometrics)
                    }
                }
            ) {
                Text("Save", color = glassColors.glowColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF131722)
    )
}

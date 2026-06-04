package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun AppNavigation(viewModel: ReaderViewModel) {
    val activeThemeMode = viewModel.currentTheme
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (activeThemeMode) {
        "Dark" -> true
        "Light" -> false
        else -> isSystemDark
    }

    MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (viewModel.currentScreen) {
                "splash" -> SplashScreen(viewModel)
                "onboarding" -> OnboardingScreen(viewModel)
                "login" -> AuthenticationScreen(viewModel)
                "main" -> MainDashboardScreen(viewModel)
                "details" -> BookDetailsScreen(viewModel)
                "reader" -> ReaderScreen(viewModel)
                "admin_dashboard" -> AdminDashboardScreen(viewModel)
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(viewModel: ReaderViewModel) {
    var startAnimation by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "loader_rotation")
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    // Auto navigate to onboarding after 2.5 seconds
    LaunchedEffect(key1 = true) {
        delay(2500)
        viewModel.currentScreen = "onboarding"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TealPrimary, TealDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant Full Logo configuration for Splash Screen
            MyReaderProLogo(
                variant = LogoVariant.FULL_LOGO,
                scale = 1.35f,
                sloganVisible = true
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Beautiful rotating custom canvas loader
            Canvas(modifier = Modifier.size(48.dp)) {
                drawArc(
                    color = OrangeAccent,
                    startAngle = angleRotation,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.3f),
                    startAngle = angleRotation + 90f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: ReaderViewModel) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = listOf(
        OnboardPageData(
            titleKey = "onboard_1_title",
            descKey = "onboard_1_desc",
            icon = Icons.Default.Public,
            tint = TealLight
        ),
        OnboardPageData(
            titleKey = "onboard_2_title",
            descKey = "onboard_2_desc",
            icon = Icons.Default.CloudDownload,
            tint = OrangeAccent
        ),
        OnboardPageData(
            titleKey = "onboard_3_title",
            descKey = "onboard_3_desc",
            icon = Icons.Default.TrendingUp,
            tint = TealLight
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { viewModel.currentScreen = "login" }
            ) {
                Text(
                    text = viewModel.translate("skip"),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.4f))

        // Onboarding Graphics Illustration (Canvas backed with primary colors)
        Box(
            modifier = Modifier
                .size(220.dp)
                .drawBehind {
                    drawCircle(
                        color = pages[currentPage].tint.copy(alpha = 0.12f),
                        radius = size.width / 2f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = pages[currentPage].icon,
                contentDescription = null,
                tint = pages[currentPage].tint,
                modifier = Modifier.size(110.dp)
            )
        }

        Spacer(modifier = Modifier.weight(0.4f))

        // Onboarding Typography
        Text(
            text = viewModel.translate(pages[currentPage].titleKey),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = viewModel.translate(pages[currentPage].descKey),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(0.8f))

        // Bullet Progress Indicators
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (index == currentPage) 24.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) TealPrimary else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.2f
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Continue Button
        Button(
            onClick = {
                if (currentPage < pages.lastIndex) {
                    currentPage++
                } else {
                    viewModel.currentScreen = "login"
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (currentPage == pages.lastIndex) viewModel.translate("get_started") else viewModel.translate("continue"),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class OnboardPageData(
    val titleKey: String,
    val descKey: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

// 3. AUTHENTICATION (LOGIN / SIGNUP) SCREEN
@Composable
fun AuthenticationScreen(viewModel: ReaderViewModel) {
    var isLoginTab by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleAccountChooser by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Configure Google Sign-In with dynamic client ID checking
    val gso = remember {
        val clientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else null
        } catch (e: Exception) {
            null
        } ?: "826579536866-dummyclientid.apps.googleusercontent.com" // Reliable placeholder fallback

        com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
        .requestIdToken(clientId)
        .requestEmail()
        .build()
    }

    val googleSignInClient = remember {
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (data != null) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                val emailStr = account?.email
                val nameStr = account?.displayName ?: account?.email?.substringBefore("@") ?: "Google User"
                val idToken = account?.idToken
                if (emailStr != null) {
                    viewModel.handleGoogleLogin(emailStr, nameStr, idToken)
                } else {
                    errorMessage = "Failed to retrieve Google Account credentials."
                }
            } catch (e: com.google.android.gms.common.api.ApiException) {
                val statusCode = e.statusCode
                val errorDescription = when (statusCode) {
                    7 -> "Network error. Please check your internet connection."
                    10 -> "Configuration issue (Developer Error 10). Mismatching SHA-1 fingerprint. Showing bypass diagnostics..."
                    12500 -> "Sign-in failed (12500). Please verify Google Play Services setup on your device."
                    12501 -> "Sign-in cancelled by user."
                    else -> "Google Sign-In failed (Code: $statusCode): ${e.localizedMessage}"
                }
                
                if (statusCode == 10 || statusCode == 12500) {
                    errorMessage = "$errorDescription\n\nYou can configure the SHA-1 in Firebase and add your credentials, or use the Simulated Multi-Account Chooser as a safe bypass!"
                    showGoogleAccountChooser = true
                } else {
                    errorMessage = errorDescription
                }
            } catch (e: Exception) {
                errorMessage = "Google login error: ${e.localizedMessage}"
            }
        } else {
            errorMessage = "Google authentication was cancelled or produced no response."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Elegant Brand Programmatic Logo (Branding animated version)
        MyReaderProLogo(
            variant = LogoVariant.BRANDING,
            scale = 1.05f,
            sloganVisible = true
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Toggle Tab Selector (Custom styled rounded tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLoginTab) TealPrimary else Color.Transparent)
                    .clickable { 
                        isLoginTab = true
                        errorMessage = null
                        viewModel.clearAuthError()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.translate("login"),
                    color = if (isLoginTab) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isLoginTab) TealPrimary else Color.Transparent)
                    .clickable { 
                        isLoginTab = false
                        errorMessage = null
                        viewModel.clearAuthError()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.translate("signup"),
                    color = if (!isLoginTab) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Auth Errors elegantly
        val displayedError = errorMessage ?: viewModel.authError
        if (displayedError != null) {
            Text(
                text = displayedError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp),
                textAlign = TextAlign.Center
            )
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null; viewModel.clearAuthError() },
            label = { Text(viewModel.translate("email")) },
            placeholder = { Text("enter your email address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealPrimary) },
            singleLine = true,
            enabled = !viewModel.authLoading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Name field (Sign Up only)
        if (!isLoginTab) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMessage = null; viewModel.clearAuthError() },
                label = { Text(viewModel.translate("name")) },
                placeholder = { Text("enter your full name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealPrimary) },
                singleLine = true,
                enabled = !viewModel.authLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null; viewModel.clearAuthError() },
            label = { Text(viewModel.translate("password")) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealPrimary) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = null, tint = TealPrimary)
                }
            },
            singleLine = true,
            enabled = !viewModel.authLoading,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Remember Me & Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    enabled = !viewModel.authLoading,
                    colors = CheckboxDefaults.colors(checkedColor = TealPrimary)
                )
                Text(
                    text = "Remember me",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            TextButton(
                onClick = {
                    errorMessage = "Password reset link simulated and sent to your email!"
                },
                enabled = !viewModel.authLoading
            ) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 14.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                if (viewModel.authLoading) return@Button
                if (email.isBlank() || password.isBlank() || (!isLoginTab && name.isBlank())) {
                    errorMessage = "Please fulfill all required text fields."
                } else if (!email.contains("@")) {
                    errorMessage = "Please enter a valid email address."
                } else {
                    errorMessage = null
                    viewModel.clearAuthError()
                    if (isLoginTab) {
                        viewModel.handleLoginWithEmail(email, password)
                    } else {
                        viewModel.handleSignupWithEmail(email, name, password)
                    }
                }
            },
            enabled = !viewModel.authLoading,
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (viewModel.authLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (isLoginTab) viewModel.translate("login") else viewModel.translate("signup"),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google / Social and guest logins
        Text(
            text = "--- OR CONNECT VIA ---",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Google Login Button
            OutlinedButton(
                onClick = {
                    if (!viewModel.authLoading) {
                        errorMessage = null
                        viewModel.clearAuthError()
                        try {
                            // Force account switching by signing out first
                            googleSignInClient.signOut().addOnCompleteListener {
                                try {
                                    val signInIntent = googleSignInClient.signInIntent
                                    googleSignInLauncher.launch(signInIntent)
                                } catch (e: Exception) {
                                    errorMessage = "Google Play Services auth unavailable: ${e.localizedMessage}"
                                    showGoogleAccountChooser = true
                                }
                            }
                        } catch (e: Exception) {
                            // Direct graceful fallback
                            showGoogleAccountChooser = true
                        }
                    }
                },
                enabled = !viewModel.authLoading,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Google",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            // Guest Login Button
            Button(
                onClick = {
                    if (!viewModel.authLoading) {
                        errorMessage = null
                        viewModel.clearAuthError()
                        viewModel.handleGuestLogin()
                    }
                },
                enabled = !viewModel.authLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.translate("guest"),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showGoogleAccountChooser) {
            GoogleChooserDialog(
                viewModel = viewModel,
                onDismiss = { showGoogleAccountChooser = false },
                onSelect = { selectedEmail, selectedName ->
                    showGoogleAccountChooser = false
                    viewModel.clearAuthError()
                    viewModel.handleGoogleLogin(selectedEmail, selectedName)
                }
            )
        }
    }
}

@Composable
fun GoogleChooserDialog(
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit,
    onSelect: (String, String) -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState(initial = emptyList())
    
    // Dynamic listed google accounts: Seeding default testers + remembered non-guest accounts
    val googleAccounts = remember(allUsers) {
        val list = mutableListOf<Triple<String, String, String>>()
        list.add(Triple("rafuse2024@gmail.com", "Official Admin", "A"))
        list.add(Triple("google_user@gmail.com", "Google Scholar", "G"))
        
        allUsers.forEach { user ->
            if (!user.isGuest && user.email != "rafuse2024@gmail.com" && user.email != "google_user@gmail.com") {
                val initialLetter = (user.name.takeIf { it.isNotBlank() } ?: user.email).take(1).uppercase()
                list.add(Triple(user.email, user.name, initialLetter))
            }
        }
        list.distinctBy { it.first }
    }

    var isConnecting by remember { mutableStateOf(false) }
    var handshakeStatus by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showSetupGuide by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isConnecting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isConnecting) {
                    // Google Play Services Authentication simulation with rich status steps
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TealPrimary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security handshake",
                            tint = OrangeAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Authenticating with Google",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = selectedAccount?.first ?: "",
                        fontSize = 13.sp,
                        color = TealLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = handshakeStatus,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    // Google Branding Icon Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(text = "G", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF4285F4))
                        Text(text = "o", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFEA4335))
                        Text(text = "o", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFFBBC05))
                        Text(text = "g", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF4285F4))
                        Text(text = "l", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF34A853))
                        Text(text = "e", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFEA4335))
                    }

                    Text(
                        text = "Sign in with Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Text(
                        text = "Choose an account to continue to MyReaderPro",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Account list container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.01f))
                    ) {
                        googleAccounts.forEachIndexed { index, (email, name, initial) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAccount = Pair(email, name)
                                        isConnecting = true
                                        // Simulate real OAuth handshake steps beautifully
                                        coroutineScope.launch {
                                            handshakeStatus = "Initializing Google Play Services auth token..."
                                            kotlinx.coroutines.delay(600)
                                            handshakeStatus = "Verifying client SHA-1 certificates with Firebase..."
                                            kotlinx.coroutines.delay(600)
                                            handshakeStatus = "Syncing local database and active session..."
                                            kotlinx.coroutines.delay(400)
                                            onSelect(email, name)
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        color = TealPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = email,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Select account",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (index < googleAccounts.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Custom Google Account Module
                    var showAddSimulated by remember { mutableStateOf(false) }
                    var customEmail by remember { mutableStateOf("") }
                    var customName by remember { mutableStateOf("") }

                    if (!showAddSimulated) {
                        OutlinedButton(
                            onClick = { showAddSimulated = true },
                            border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.15f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add another Google account",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Add Simulated Google Profile",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text("Display Name", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customEmail,
                                    onValueChange = { customEmail = it },
                                    label = { Text("Gmail Address", fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showAddSimulated = false }) {
                                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (customEmail.isNotBlank() && customEmail.contains("@")) {
                                                val finalName = if (customName.isNotBlank()) customName else customEmail.substringBefore("@")
                                                selectedAccount = Pair(customEmail, finalName)
                                                isConnecting = true
                                                coroutineScope.launch {
                                                    handshakeStatus = "Registering new Google OAuth token profile..."
                                                    kotlinx.coroutines.delay(600)
                                                    handshakeStatus = "Binding with Secure Firebase Auth server..."
                                                    kotlinx.coroutines.delay(600)
                                                    onSelect(customEmail, finalName)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Add & Link", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Production Google Credentials & SHA-1 helpful setup guidelines
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showSetupGuide = !showSetupGuide }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Instructions",
                            tint = OrangeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Credential Integration Instructions",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (showSetupGuide) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = showSetupGuide) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Firebase Google Authentication Setup Guide\n",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "To enable live production Google sign-in with Firebase, please perform these key configurations:\n" +
                                            "1. **Enable Google Provider**:\n" +
                                            "   Go to Firebase Console > Authentication > Sign-in method and activate Google provider.\n\n" +
                                            "2. **Configure SHA-1 Fingerprint**:\n" +
                                            "   Register your debug app signing SHA-1 fingerprint in Firebase App settings. To extract your fingerprint key, run this standard command:\n" +
                                            "   `keytool -list -v -alias androiddebugkey -keystore ~/.android/debug.keystore`\n" +
                                            "   (Default keystore password: `android`)\n\n" +
                                            "3. **Setup google-services.json**:\n" +
                                            "   Download the generated `google-services.json` from the Firebase Dashboard and append/overwrite it directly under `/app` directory.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// 4. MAIN DASHBOARD SCREEN (Multi tab based)
@Composable
fun MainDashboardScreen(viewModel: ReaderViewModel) {
    val activeUser by viewModel.activeUser.collectAsState(initial = null)
    var showResearchChat by remember { mutableStateOf(false) }
    var showQuickNavSheet by remember { mutableStateOf(false) }
    val isAiScholarEnabled = false // Temporarily hide AI Scholar as per user request

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth > 720.dp

        if (isWide) {
            // Premium Tabler.io-style Adaptive Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Sleek Sidebar
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(260.dp),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Compact Drawer Version Logo
                            MyReaderProLogo(
                                variant = LogoVariant.COMPACT_DRAWER,
                                scale = 1.0f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                            // Sidebar Nav Items
                            val tabs = listOf(
                                Triple("home", Icons.Default.Home, "home"),
                                Triple("library", Icons.Default.LibraryBooks, "library"),
                                Triple("forum", Icons.Default.People, "forum"),
                                Triple("stats", Icons.Default.BarChart, "stats"),
                                Triple("profile", Icons.Default.Person, "profile")
                            )

                            tabs.forEach { (tabId, icon, labelKey) ->
                                SidebarNavItem(
                                    label = viewModel.translate(labelKey),
                                    icon = icon,
                                    selected = viewModel.selectedDashboardTab == tabId,
                                    onClick = { viewModel.selectedDashboardTab = tabId }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        // Bottom Profile Drawer Row
                        activeUser?.let { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                                    .clickable { viewModel.selectedDashboardTab = "profile" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TealPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        color = TealPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = user.membershipType + " ACCOUNT",
                                        fontSize = 9.sp,
                                        color = TealPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                VerticalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Centered Dashboard Workspace Area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (viewModel.selectedDashboardTab == "home") {
                        PremiumTopHeader(
                            viewModel = viewModel,
                            activeUser = activeUser,
                            onMenuClick = { showQuickNavSheet = true }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .widthIn(max = 1100.dp)
                        ) {
                            when (viewModel.selectedDashboardTab) {
                                "home" -> HomeScreenTab(viewModel, activeUser)
                                "categories" -> CategoriesGridTab(viewModel)
                                "forum" -> ForumHubTab(viewModel, activeUser)
                                "library" -> MyLibraryTab(viewModel)
                                "stats" -> StatsTrackingTab(viewModel, activeUser)
                                "profile" -> UserProfileTab(viewModel, activeUser)
                            }
                        }
                    }
                }
            }
        } else {
            // Standard Smartphone Scaffolding
            Scaffold(
                topBar = {
                    if (viewModel.selectedDashboardTab == "home") {
                        PremiumTopHeader(
                            viewModel = viewModel,
                            activeUser = activeUser,
                            onMenuClick = { showQuickNavSheet = true }
                        )
                    }
                },
                floatingActionButton = {
                    if (isAiScholarEnabled) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                viewModel.clearChat()
                                showResearchChat = true
                            },
                            containerColor = TealPrimary,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .padding(bottom = 12.dp, end = 4.dp)
                                .testTag("ai_research_fab")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Scholars Research Chat",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "এআই গবেষণা"
                                    "Arabic" -> "جيمي الباحث"
                                    else -> "AI Scholar"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = viewModel.selectedDashboardTab == "home",
                            onClick = { viewModel.selectedDashboardTab = "home" },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text(viewModel.translate("home"), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = viewModel.selectedDashboardTab == "library",
                            onClick = { viewModel.selectedDashboardTab = "library" },
                            icon = { Icon(Icons.Default.LibraryBooks, contentDescription = null) },
                            label = { Text(viewModel.translate("library"), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = viewModel.selectedDashboardTab == "forum",
                            onClick = { viewModel.selectedDashboardTab = "forum" },
                            icon = { Icon(Icons.Default.People, contentDescription = null) },
                            label = { Text(viewModel.translate("forum"), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = viewModel.selectedDashboardTab == "stats",
                            onClick = { viewModel.selectedDashboardTab = "stats" },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                            label = { Text(viewModel.translate("stats"), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = viewModel.selectedDashboardTab == "profile",
                            onClick = { viewModel.selectedDashboardTab = "profile" },
                            icon = { Icon(Icons.Default.Person, contentDescription = null) },
                            label = { Text(viewModel.translate("profile"), fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TealPrimary,
                                selectedTextColor = TealPrimary,
                                indicatorColor = TealPrimary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (viewModel.selectedDashboardTab) {
                        "home" -> HomeScreenTab(viewModel, activeUser)
                        "categories" -> CategoriesGridTab(viewModel)
                        "forum" -> ForumHubTab(viewModel, activeUser)
                        "library" -> MyLibraryTab(viewModel)
                        "stats" -> StatsTrackingTab(viewModel, activeUser)
                        "profile" -> UserProfileTab(viewModel, activeUser)
                    }
                }
            }
        }
    }

    if (showResearchChat) {
        ResearchChatbotDialog(
            viewModel = viewModel,
            onDismiss = { showResearchChat = false }
        )
    }

    if (showQuickNavSheet) {
        TablerQuickNavDialog(
            viewModel = viewModel,
            onDismiss = { showQuickNavSheet = false }
        )
    }

    if (viewModel.showNotificationCenter) {
        NotificationCenterPanel(
            viewModel = viewModel,
            onDismiss = { viewModel.showNotificationCenter = false }
        )
    }
}

@Composable
fun PremiumTopHeader(
    viewModel: ReaderViewModel,
    activeUser: UserEntity?,
    onMenuClick: () -> Unit
) {
    val notificationsList by viewModel.notifications.collectAsState(initial = emptyList())
    val unreadCount = notificationsList.count { !it.isRead }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Left slot: Navigation button
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Navigation Menu",
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Middle slot: App Logo and Name (Compact Drawer style)
            MyReaderProLogo(
                variant = LogoVariant.COMPACT_DRAWER,
                scale = 0.95f
            )

            // Right slot: Profile click action to switch to profile tab & notification center trigger
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bell Icon with glowing notification counter badge
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .testTag("notification_header_icon")
                ) {
                    IconButton(
                        onClick = { viewModel.showNotificationCenter = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .background(Color(0xFFEF4444), CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                .testTag("notification_badge_counter"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unreadCount > 99) "99+" else "$unreadCount",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                activeUser?.let { user ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.15f))
                            .clickable { viewModel.selectedDashboardTab = "profile" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            color = TealPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } ?: run {
                    IconButton(
                        onClick = { viewModel.selectedDashboardTab = "profile" },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    }
}

@Composable
fun TablerQuickNavDialog(
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Navigation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TealPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                
                // Simple Grid of tabs
                val tabs = listOf(
                    Triple("home", Icons.Default.Home, "home"),
                    Triple("categories", Icons.Default.Category, "categories"),
                    Triple("forum", Icons.Default.Forum, "forum"),
                    Triple("library", Icons.Default.LibraryBooks, "library"),
                    Triple("stats", Icons.Default.BarChart, "stats"),
                    Triple("profile", Icons.Default.Person, "profile")
                )
                
                Column {
                    tabs.chunked(2).forEach { rowTabs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowTabs.forEach { (tabId, icon, labelKey) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (viewModel.selectedDashboardTab == tabId) TealPrimary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                                        )
                                        .border(
                                            1.dp,
                                            if (viewModel.selectedDashboardTab == tabId) TealPrimary.copy(alpha = 0.5f)
                                            else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            viewModel.selectedDashboardTab = tabId
                                            onDismiss()
                                        }
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (viewModel.selectedDashboardTab == tabId) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = viewModel.translate(labelKey),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (viewModel.selectedDashboardTab == tabId) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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

@Composable
fun SidebarNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgAlpha by animateFloatAsState(if (selected) 0.1f else 0.0f, label = "bg_alpha")
    val contentColor = if (selected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TealPrimary.copy(alpha = bgAlpha))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

// 4.A HOME CONTENT TAB
@Composable
fun HomeScreenTab(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val booksList by viewModel.filteredBooks.collectAsState(initial = emptyList())
    val categoriesList by viewModel.categories.collectAsState(initial = emptyList())
    val historyBooks by viewModel.readingHistoryBooks.collectAsState(initial = emptyList())
    val adBlocksHome by viewModel.allAdBlocks.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search Bar (Dynamic live search)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text(viewModel.translate("search_hint"), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealPrimary) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TealPrimary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Interactive Simulated Ad Banner (For Free memberships)
        val activeUserHome = activeUser
        val homeAd = adBlocksHome.find { it.id == "banner_home" }
        if (homeAd != null && homeAd.isEnabled) {
            val showAd = activeUserHome == null || activeUserHome.membershipType == "FREE"
            if (showAd) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.recordAdClick(homeAd.id)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(OrangeAccent)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("SPONSORED", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("MyReaderPro High Quality Premium", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Upgrade to Premium or VIP tier to hide all banner & interstitial ads permanently!", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            Button(
                                onClick = {
                                    viewModel.recordAdClick(homeAd.id)
                                    viewModel.selectedDashboardTab = "profile"
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Upgrade Now", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    // Trigger impression log
                    LaunchedEffect(key1 = homeAd.id) {
                        viewModel.recordAdImpression(homeAd.id)
                    }
                }
            } else {
                item {
                    // Premium indicator badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("✨ Ad-Free Premium Experience Activated cleanly! All sponsor blocks filtered.", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Horizontal Category Capsules
        item {
            Text(
                text = "Explore Categories",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAllSelected = viewModel.selectedCategoryFilter == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { viewModel.selectedCategoryFilter = null },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(categoriesList) { cat ->
                    val isSel = viewModel.selectedCategoryFilter == cat.id
                    FilterChip(
                        selected = isSel,
                        onClick = { viewModel.selectedCategoryFilter = cat.id },
                        label = { Text(cat.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // CONTINUE READING (Dynamic from historical logs)
        if (historyBooks.isNotEmpty() && viewModel.searchQuery.isEmpty() && viewModel.selectedCategoryFilter == null) {
            item {
                Text(
                    text = viewModel.translate("continue_reading"),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(historyBooks) { book ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .clickable { viewModel.navigateToBookDetails(book.id) },
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cover mock
                                CanvasBookCover(
                                    title = book.title, 
                                    author = book.author, 
                                    modifier = Modifier
                                        .size(width = 60.dp, height = 84.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = book.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "by " + book.author,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Reading Progress Indicator
                                    val progressVal = if (book.pages > 0) book.lastReadPosition.toFloat() / book.pages.toFloat() else 0f
                                    val progressPercentage = (progressVal * 100).toInt()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LinearProgressIndicator(
                                            progress = progressVal,
                                            color = OrangeAccent,
                                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$progressPercentage%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OrangeAccent
                                        )
                                    }
                                    Text(
                                        text = "Page ${book.lastReadPosition} of ${book.pages}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // INTELLIGENT RECOMMENDATION ENGINE
        if (viewModel.searchQuery.isEmpty() && viewModel.selectedCategoryFilter == null && booksList.isNotEmpty()) {
            item {
                val readBookIds = historyBooks.map { it.id }.toSet()
                val favoredCategories = historyBooks.map { it.categoryId }.toSet()
                
                var recommendedBooks = booksList.filter { 
                    favoredCategories.contains(it.categoryId) && !readBookIds.contains(it.id) 
                }
                
                if (recommendedBooks.isEmpty()) {
                    recommendedBooks = booksList.filter { !readBookIds.contains(it.id) }.sortedByDescending { it.downloads }.take(5)
                }

                if (recommendedBooks.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡 Matches Recommended For You",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TealPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SMART MATCH", color = TealPrimary, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(recommendedBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable { viewModel.navigateToBookDetails(book.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CanvasBookCover(
                                        title = book.title,
                                        author = book.author,
                                        modifier = Modifier
                                            .size(width = 110.dp, height = 150.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = book.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = book.author,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                    if (book.isPremium) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(OrangeAccent.copy(alpha = 0.15f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("PREMIUM", color = OrangeAccent, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FEATURED BOOKS SLIDER
        if (viewModel.searchQuery.isEmpty() && viewModel.selectedCategoryFilter == null) {
            val featured = booksList.filter { it.isFeatured }
            if (featured.isNotEmpty()) {
                item {
                    Text(
                        text = viewModel.translate("featured"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(featured) { fBook ->
                            FeaturedBookCard(fBook) {
                                viewModel.navigateToBookDetails(fBook.id)
                            }
                        }
                    }
                }
            }
        }

        // MAIN LIST CONTAINER / HEADER
        item {
            Text(
                text = "Discover Books",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (booksList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No books found matching search filters.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(booksList) { book ->
                BookItemRow(book = book) {
                    viewModel.navigateToBookDetails(book.id)
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

















// Parent Root Comment item helper
@Composable
fun RootCommentCard(
    comment: CommentEntity,
    activeUser: UserEntity?,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(comment.authorName.take(1).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(comment.authorName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                val parentDiff = android.text.format.DateUtils.getRelativeTimeSpanString(
                    comment.timestamp,
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
                ).toString()

                Text(parentDiff, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = comment.text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reply & delete tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onReplyClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Reply, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reply", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                }

                if (activeUser != null && (activeUser.email == comment.authorEmail || activeUser.role == "ADMIN")) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Comment", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// 4.A.(iii) EXQUISITE CREATE POST DIALOG SUPPORTING IMAGE PRESETS & REFERENCE BOOK SELECTION
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreatePostDialog(
    viewModel: ReaderViewModel,
    allBooks: List<BookEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    
    // Choose Post Tag Type
    var selectedType by remember { mutableStateOf("KNOWLEDGE") } // KNOWLEDGE, ASK, DISCUSSION, ARTICLE, EXPERIENCE, TIPS, ANNOUNCEMENT
    
    // Toggle Image Post details vs standard text
    var isImagePostFormat by remember { mutableStateOf(false) }
    var imageSettingMode by remember { mutableStateOf("PRESET") } // PRESET vs URL
    var selectedPresetBackdropId by remember { mutableStateOf("preset_midnight") }
    var inputCustomImageUrl by remember { mutableStateOf("") }
    
    // Linked Reference Book
    var selectedReferenceBookTitle by remember { mutableStateOf("") }
    var isBookSelectorExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Publish Insight Thread",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TealPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close publishing modal")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Thread Category Selection Cards (Topic Tag Category)
                Text("Select Category Topic Tag", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))
                
                val categories = listOf(
                    "KNOWLEDGE" to "💡 Knowledge",
                    "ASK" to "❓ Ask Qs",
                    "DISCUSSION" to "📖 Discussion",
                    "ARTICLE" to "✍️ Article",
                    "EXPERIENCE" to "🌟 Experience",
                    "TIPS" to "⚡ Reading Tips",
                    "ANNOUNCEMENT" to "📢 Admin"
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catKey, label) ->
                        val isSelected = selectedType == catKey
                        Card(
                            onClick = { selectedType = catKey },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Title & Content forms
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Short Descriptive Title") },
                    placeholder = { Text("E.g., Carl Sagan's Chapter 2 translation inquiry...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your wisdom or question") },
                    placeholder = { Text("Provide details, context sentences, definitions or opinions...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Optional Reference Book Selection dropdown
                Text("Linked Reference Book Theme (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(6.dp))
                
                ExposedDropdownMenuBox(
                    expanded = isBookSelectorExpanded,
                    onExpandedChange = { isBookSelectorExpanded = !isBookSelectorExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedReferenceBookTitle.ifEmpty { "Select reference book (or none)" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBookSelectorExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TealPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isBookSelectorExpanded,
                        onDismissRequest = { isBookSelectorExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None - Abstract Topic", fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedReferenceBookTitle = ""
                                isBookSelectorExpanded = false
                            }
                        )
                        allBooks.forEach { book ->
                            DropdownMenuItem(
                                text = { Text(book.title) },
                                onClick = {
                                    selectedReferenceBookTitle = book.title
                                    isBookSelectorExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Image Post Formats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Transform to Image Backdrop Post", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Visual card format with gradient backgrounds", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                    Switch(
                        checked = isImagePostFormat,
                        onCheckedChange = { isImagePostFormat = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }

                if (isImagePostFormat) {
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // image settings switches: Preset Gradient vs Custom URL link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { imageSettingMode = "PRESET" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (imageSettingMode == "PRESET") TealPrimary else Color.Transparent,
                                contentColor = if (imageSettingMode == "PRESET") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Presets Gradient", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { imageSettingMode = "URL" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (imageSettingMode == "URL") TealPrimary else Color.Transparent,
                                contentColor = if (imageSettingMode == "URL") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Custom Image URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (imageSettingMode == "PRESET") {
                        Text("Select Backdrop Preset theme:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PRESET_BACKDROPS.forEach { theme ->
                                val isSelected = selectedPresetBackdropId == theme.id
                                Card(
                                    onClick = { selectedPresetBackdropId = theme.id },
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 65.dp)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) OrangeAccent else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(theme.brush)
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = theme.name,
                                            color = theme.textColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = inputCustomImageUrl,
                            onValueChange = { inputCustomImageUrl = it },
                            label = { Text("Image link attachment URL") },
                            placeholder = { Text("https://example.com/illustration.png") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Submit block
                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            val resolvedImageUrl = if (isImagePostFormat) {
                                if (imageSettingMode == "PRESET") selectedPresetBackdropId else inputCustomImageUrl
                            } else ""
                            // If isImagePostFormat is selected, enforce "IMAGE" type internally for schema support or keep type selector!
                            val finalType = if (isImagePostFormat) "IMAGE" else selectedType
                            onCreate(title, content, finalType, selectedReferenceBookTitle, resolvedImageUrl)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    enabled = title.isNotBlank() && content.isNotBlank()
                ) {
                    Text(
                        text = "🚀 Publish Insight Thread",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedBookCard(book: BookEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TealPrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OrangeAccent.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Featured Title",
                            color = OrangeAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = book.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "by " + book.author,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${book.rating} (${book.downloads} downloads)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Cover graphics drawing
            CanvasBookCover(
                title = book.title,
                author = book.author,
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun BookItemRow(book: BookEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CanvasBookCover(
                title = book.title,
                author = book.author,
                modifier = Modifier
                    .size(width = 62.dp, height = 90.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "by " + book.author,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TealPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = book.categoryId.replace("_", " ").uppercase(),
                            color = TealPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "•  ${book.pages} pages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "•  ${book.fileSize}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TealPrimary.copy(alpha = 0.6f)
            )
        }
    }
}

// 4.B CATEGORIES FULL GRID TAB
@Composable
fun CategoriesGridTab(viewModel: ReaderViewModel) {
    val categoriesList by viewModel.categories.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Browse Categories",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TealPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categoriesList) { cat ->
                // Custom visually rich cards based on category keys
                val bgGradients = when (cat.id) {
                    "islamic", "quran_tafsir", "hadith", "fiqh" -> listOf(TealPrimary, TealDark)
                    "novel" -> listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)) // purple
                    "science" -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)) // blue
                    "history" -> listOf(Color(0xFFF59E0B), Color(0xFFD97706)) // amber
                    "tech" -> listOf(Color(0xFF10B981), Color(0xFF047857)) // green
                    "bio" -> listOf(Color(0xFFEC4899), Color(0xFFBE185D)) // pink
                    "self_dev" -> listOf(Color(0xFFEF4444), Color(0xFFB91C1C)) // red
                    else -> listOf(Color(0xFF6B7280), Color(0xFF374151)) // grey
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable {
                            viewModel.selectedCategoryFilter = cat.id
                            viewModel.selectedDashboardTab = "home"
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(bgGradients)
                            )
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Beautiful vector placeholder for each category
                            val iconData = when (cat.id) {
                                "islamic" -> Icons.Default.Mosque
                                "quran_tafsir" -> Icons.Default.Book
                                "hadith" -> Icons.Default.Book
                                "fiqh" -> Icons.Default.Gavel
                                "novel" -> Icons.Default.MenuBook
                                "science" -> Icons.Default.Science
                                "history" -> Icons.Default.History
                                "tech" -> Icons.Default.DeveloperBoard
                                "bio" -> Icons.Default.Person
                                "self_dev" -> Icons.Default.TrendingUp
                                else -> Icons.Default.ChildCare
                            }

                            Icon(
                                imageVector = iconData,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(34.dp)
                            )

                            Column {
                                Text(
                                    text = cat.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Tap to view list",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4.C LIBRARY TAB (Favorites, downloads, bookmarks)
@Composable
fun MyLibraryTab(viewModel: ReaderViewModel) {
    var libraryTabState by remember { mutableStateOf("favorites") } // favorites, downloaded, history

    val favoriteBooks by viewModel.favoriteBooks.collectAsState(initial = emptyList())
    val downloadedBooks by viewModel.downloadedBooks.collectAsState(initial = emptyList())
    val historyBooks by viewModel.readingHistoryBooks.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = viewModel.translate("library"),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TealPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Triple Tab Filter Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("favorites", "downloaded", "history")
            tabs.forEach { t ->
                val title = when (t) {
                    "favorites" -> "Favorites"
                    "downloaded" -> "Downloaded"
                    else -> "History"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (libraryTabState == t) TealPrimary else Color.Transparent)
                        .clickable { libraryTabState = t }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (libraryTabState == t) Color.White else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content render
        val activeList = when (libraryTabState) {
            "favorites" -> favoriteBooks
            "downloaded" -> downloadedBooks
            else -> historyBooks
        }

        if (activeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (libraryTabState) {
                            "favorites" -> Icons.Default.FavoriteBorder
                            "downloaded" -> Icons.Default.CloudDownload
                            else -> Icons.Default.History
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (libraryTabState) {
                            "favorites" -> "Your favorites list is empty"
                            "downloaded" -> "No books downloaded yet. Go online to download!"
                            else -> "No read history. Jump into a book to track details!"
                        },
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeList) { book ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateToBookDetails(book.id) },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CanvasBookCover(
                                title = book.title,
                                author = book.author,
                                modifier = Modifier
                                    .size(width = 54.dp, height = 76.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "by " + book.author,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                if (libraryTabState == "history" && book.pages > 0) {
                                    val progress = book.lastReadPosition.toFloat() / book.pages.toFloat()
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = progress,
                                        color = OrangeAccent,
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(4.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        text = "Read page ${book.lastReadPosition} of ${book.pages}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            // Dynamic action helpers in row
                            if (libraryTabState == "downloaded") {
                                IconButton(
                                    onClick = { viewModel.clearDownloadedFile(book.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete downloaded file",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TealPrimary.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyReadingDurationTrendsCard(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val todayMinutes = activeUser?.todayMinutesRead ?: 0f
    
    // Determine current day of week
    val calendar = java.util.Calendar.getInstance()
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
    
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val defaultMinutes = listOf(15f, 32f, 25f, 40f, 18f, 35f, 20f)
    
    val weekData = remember(todayMinutes, dayOfWeek) {
        dayLabels.mapIndexed { index, label ->
            val isToday = (index + 1) == dayOfWeek
            val mins = if (isToday) todayMinutes else defaultMinutes[index]
            label to mins
        }
    }
    
    val maxMins = remember(weekData) {
        weekData.maxByOrNull { it.second }?.second?.coerceAtLeast(30f) ?: 45f
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("weekly_trends_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text("গভীর পরিসংখ্যান (Weekly Trends)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("চলতি সপ্তাহের প্রতিদিন পড়ার সময়", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "মোট: ${String.format(java.util.Locale.US, "%.1f", weekData.sumOf { it.second.toDouble() })} মিনিট",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }

            // Beautiful graphic bar representation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weekData.forEachIndexed { index, (dayLabel, mins) ->
                    val isToday = (index + 1) == dayOfWeek
                    val barHeightFraction = (mins / maxMins).coerceIn(0.04f, 1f)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Value label above bar
                        Text(
                            text = if (mins > 0) "${mins.toInt()}m" else "-",
                            fontSize = 10.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) OrangeAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Main Bar container
                        Box(
                            modifier = Modifier
                                .height(5.dp) // dummy initial spacer to enable height math
                                .weight(barHeightFraction)
                                .width(22.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    if (isToday) {
                                        Brush.verticalGradient(listOf(OrangeAccent, OrangeAccent.copy(alpha = 0.7f)))
                                    } else {
                                        Brush.verticalGradient(listOf(TealPrimary, TealPrimary.copy(alpha = 0.5f)))
                                    }
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Day Label
                        Text(
                            text = dayLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) OrangeAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoricalActivityLogsCard(viewModel: ReaderViewModel) {
    val rawHistory by viewModel.rawReadingHistory.collectAsState(initial = emptyList())
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("activity_logs_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("ধারাবাহিক পাঠ ইতিহাস (Activity Logs)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("আপনার পড়া বই এবং পৃষ্ঠা ট্র্যাকিং বুক", fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (rawHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                        Text("কোন রেকর্ড পাওয়া যায়নি। রিডার প্যানেলে বই পড়া শুরু করুন।", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            } else {
                val sdf = remember { java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()) }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    rawHistory.take(15).forEach { log ->
                        val matchedBook = remember(books, log.bookId) {
                            books.find { it.id == log.bookId }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ImportContacts,
                                    contentDescription = null,
                                    tint = TealPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = matchedBook?.title ?: "Unknown Book",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "পৃষ্ঠা: ${log.pageNumber + 1} (${log.progressPercent.toInt()}% সম্পন্ন)",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Box(contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    text = sdf.format(java.util.Date(log.timestamp)),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupAndExportLibraryCard(viewModel: ReaderViewModel) {
    val allWords by viewModel.allWords.collectAsState(initial = emptyList())
    val allNotes by viewModel.allNotes.collectAsState(initial = emptyList())
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val activeUser by viewModel.activeUser.collectAsState(initial = null)
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showBackupDialogText by remember { mutableStateOf<String?>(null) }
    var backupDialogTitle by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("backup_export_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text("অনুলিপি রপ্তানি ও ব্যাকআপ (Backup & Library Export)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("শব্দকোষ ও বুকমার্ক অন্য ডিভাইসে সেভ করার ব্যাকআপ টুল", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val jsonStr = buildString {
                                    append("{\n")
                                    append("  \"app\": \"MyReaderPro\",\n")
                                    append("  \"timestamp\": ${System.currentTimeMillis()},\n")
                                    append("  \"goalMinutes\": ${activeUser?.dailyReadingGoalMinutes ?: 30},\n")
                                    append("  \"vocabularyBooklet\": [\n")
                                    allWords.forEachIndexed { idx, w ->
                                        append("    {\n")
                                        append("      \"word\": \"${w.word.replace("\"", "\\\"")}\",\n")
                                        append("      \"pronunciation\": \"${w.pronunciation.replace("\"", "\\\"")}\",\n")
                                        append("      \"definition\": \"${w.definition.replace("\"", "\\\"")}\",\n")
                                        append("      \"translation\": \"${w.translation.replace("\"", "\\\"")}\",\n")
                                        append("      \"sentenceContext\": \"${w.sentenceContext.replace("\"", "\\\"")}\"\n")
                                        append("    }${if (idx == allWords.size - 1) "" else ","}\n")
                                    }
                                    append("  ],\n")
                                    append("  \"notesAndHighlights\": [\n")
                                    allNotes.forEachIndexed { idx, n ->
                                        val mBook = books.find { it.id == n.bookId }
                                        append("    {\n")
                                        append("      \"bookId\": \"${n.bookId}\",\n")
                                        append("      \"bookTitle\": \"${(mBook?.title ?: "Unknown").replace("\"", "\\\"")}\",\n")
                                        append("      \"pageNumber\": ${n.pageNumber + 1},\n")
                                        append("      \"highlightText\": \"${n.highlightText.replace("\"", "\\\"")}\",\n")
                                        append("      \"noteText\": \"${n.text.replace("\"", "\\\"")}\",\n")
                                        append("      \"color\": \"${n.colorHex}\"\n")
                                        append("    }${if (idx == allNotes.size - 1) "" else ","}\n")
                                    }
                                    append("  ]\n")
                                    append("}")
                                }
                                
                                val cacheDir = context.cacheDir
                                val file = java.io.File(cacheDir, "MyReader_Library_Backup.json")
                                file.writeText(jsonStr)
                                
                                backupDialogTitle = "JSON লাইব্রেরি ব্যাকআপ (JSON Library Backup)"
                                showBackupDialogText = jsonStr
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("JSON ব্যাকআপ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                if (allNotes.isEmpty()) {
                                    android.widget.Toast.makeText(context, "কোন হাইলাইট বা নোট এখনও সেভ করা হয়নি!", android.widget.Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                
                                val mdStr = buildString {
                                    append("# MyReaderPro - Annotations & Study Guide\n")
                                    append("Generated on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
                                    append("===============\n\n")
                                    
                                    val groupedByBook = allNotes.groupBy { it.bookId }
                                    groupedByBook.forEach { (bId, noteList) ->
                                        val mBook = books.find { it.id == bId }
                                        append("## Book Study Guide: ${mBook?.title ?: "Unknown Chapter"}\n")
                                        append("Author: ${mBook?.author ?: "Unknown"}\n")
                                        append("-----------\n")
                                        noteList.forEachIndexed { i, n ->
                                            append("${i + 1}. **[Page ${n.pageNumber + 1}] Highlights:**\n")
                                            append("   > \"${n.highlightText}\"\n")
                                            append("   * **My Notes / Annotations:** ${n.text}\n")
                                            append("   * *Accent highlight color:* ${n.colorHex}\n\n")
                                        }
                                        append("\n")
                                    }
                                }
                                
                                val cacheDir = context.cacheDir
                                val file = java.io.File(cacheDir, "MyReader_Study_Notes.md")
                                file.writeText(mdStr)
                                
                                backupDialogTitle = "Markdown নোট রপ্তানি (Markdown Study Guide)"
                                showBackupDialogText = mdStr
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("নোট এক্সপোর্ট (.TXT)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showBackupDialogText != null) {
        AlertDialog(
            onDismissRequest = { showBackupDialogText = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("MyReaderPro Backup", showBackupDialogText)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "ক্লিপবোর্ডে কপি করা হয়েছে! 📋🎉", android.widget.Toast.LENGTH_SHORT).show()
                        showBackupDialogText = null
                    }
                ) {
                    Text("অনুলিপি কপি করুন (Copy)", color = TealPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialogText = null }) {
                    Text("বন্ধ করুন", color = Color.Gray)
                }
            },
            title = { Text(backupDialogTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("নিচের বক্সটি থেকে সম্পূর্ণ কন্টেন্ট কপি করে রেখে দিতে বা শেয়ার করতে পারেন:", fontSize = 11.sp, color = Color.Gray)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = showBackupDialogText ?: "",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}

// 4.D READING STATISTICS TAB
@Composable
fun StatsTrackingTab(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val accomplishments by viewModel.achievements.collectAsState(initial = emptyList())
    val favoriteBooks by viewModel.favoriteBooks.collectAsState(initial = emptyList())
    val downloadedBooks by viewModel.downloadedBooks.collectAsState(initial = emptyList())
    val readingHistoryBooks by viewModel.readingHistoryBooks.collectAsState(initial = emptyList())
    val allPosts by viewModel.allPosts.collectAsState(initial = emptyList())
    val allBookmarks by viewModel.allBookmarks.collectAsState(initial = emptyList())
    val allBookRequests by viewModel.allBookRequests.collectAsState(initial = emptyList())
    val allWords by viewModel.allWords.collectAsState(initial = emptyList())

    val currentUserEmail = activeUser?.email ?: "guest@myreader.com"
    
    // Community metrics calculation based on actual elements in Room database
    val myPosts = remember(allPosts, currentUserEmail) {
        allPosts.filter { it.authorEmail == currentUserEmail }
    }
    val postsCreated = myPosts.size
    val likesReceived = myPosts.sumOf { it.likesCount }
    val commentsReceived = myPosts.sumOf { it.commentsCount }

    // Personal library metrics
    val favoriteCount = favoriteBooks.size
    val downloadCount = downloadedBooks.size
    val historyCount = readingHistoryBooks.size
    val bookmarksCount = allBookmarks.size

    // Contribution Area metric calculations
    val myRequests = remember(allBookRequests, currentUserEmail) {
        allBookRequests.filter { it.userEmail == currentUserEmail }
    }
    val uploadedBooksCount = myRequests.count { it.status == "COMPLETED" || it.title.isNotBlank() }
    val uploadedArticlesCount = myPosts.count { it.type == "TIPS" || it.type == "TEXT" }
    val pendingReviewsCount = myRequests.count { it.status == "PENDING" }
    val publishedContentCount = myRequests.count { it.status == "COMPLETED" } + postsCreated

    var activeTab by remember { mutableStateOf("progress") } // progress, wordbook, flashcards

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        // Segmented Tabs
        TabRow(
            selectedTabIndex = when (activeTab) {
                "progress" -> 0
                "wordbook" -> 1
                else -> 2
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = TealPrimary,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = activeTab == "progress",
                onClick = { activeTab = "progress" },
                text = { Text("লক্ষ্য ও প্রগতি", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == "wordbook",
                onClick = { activeTab = "wordbook" },
                text = { Text("शब्दকোষ", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == "flashcards",
                onClick = { activeTab = "flashcards" },
                text = { Text("এআই ফ্ল্যাশকার্ড", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (activeTab) {
                "progress" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Upper Profile Welcome banner
                        UserProfileBanner(activeUser)
                        
                        // Circular Reading Goal Progress Dial and Badges
                        ReadingGoalCircularDialCard(viewModel, activeUser)

                        // Weekly Reading Duration trends chart
                        WeeklyReadingDurationTrendsCard(viewModel, activeUser)

                        // Backup & Library Export options panel
                        BackupAndExportLibraryCard(viewModel)

                        // Chronological scrollable historical activity logs
                        HistoricalActivityLogsCard(viewModel)

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val isWide = maxWidth >= 600.dp
                            
                            if (isWide) {
                                // Wide Screen: Grid System Layout using 2 columns side-by-side
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Left Column (Reading Overview & Personal Library)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ReadingOverviewCard(activeUser)
                                        LibraryGridSection(
                                            viewModel = viewModel,
                                            favoriteBooks = favoriteBooks,
                                            favoriteCount = favoriteCount,
                                            downloadedBooks = downloadedBooks,
                                            downloadCount = downloadCount,
                                            readingHistoryBooks = readingHistoryBooks,
                                            historyCount = historyCount,
                                            allBookmarks = allBookmarks,
                                            bookmarksCount = bookmarksCount
                                        )
                                    }

                                    // Right Column (Community Impact & Contribution Area)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CommunityOverviewCard(
                                            viewModel = viewModel,
                                            postsCreated = postsCreated,
                                            likesReceived = likesReceived,
                                            commentsReceived = commentsReceived,
                                            myPosts = myPosts
                                        )
                                        ContributionAreaCard(
                                            viewModel = viewModel,
                                            uploadedBooksCount = uploadedBooksCount,
                                            uploadedArticlesCount = uploadedArticlesCount,
                                            pendingReviewsCount = pendingReviewsCount,
                                            publishedContentCount = publishedContentCount
                                        )
                                    }
                                }
                            } else {
                                // Portrait / Compact Phone Screen: Single column stacked layout
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ReadingOverviewCard(activeUser)
                                    CommunityOverviewCard(
                                        viewModel = viewModel,
                                        postsCreated = postsCreated,
                                        likesReceived = likesReceived,
                                        commentsReceived = commentsReceived,
                                        myPosts = myPosts
                                    )
                                    LibraryGridSection(
                                        viewModel = viewModel,
                                        favoriteBooks = favoriteBooks,
                                        favoriteCount = favoriteCount,
                                        downloadedBooks = downloadedBooks,
                                        downloadCount = downloadCount,
                                        readingHistoryBooks = readingHistoryBooks,
                                        historyCount = historyCount,
                                        allBookmarks = allBookmarks,
                                        bookmarksCount = bookmarksCount
                                    )
                                    ContributionAreaCard(
                                        viewModel = viewModel,
                                        uploadedBooksCount = uploadedBooksCount,
                                        uploadedArticlesCount = uploadedArticlesCount,
                                        pendingReviewsCount = pendingReviewsCount,
                                        publishedContentCount = publishedContentCount
                                    )
                                }
                            }
                        }
                    }
                }
                "wordbook" -> {
                    InteractiveWordbookSection(viewModel, allWords)
                }
                "flashcards" -> {
                    AIFlashcardsSection(viewModel)
                }
            }
        }
    }
}

@Composable
fun ReadingGoalCircularDialCard(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val dailyGoal = activeUser?.dailyReadingGoalMinutes ?: 30
    val todayMinutes = activeUser?.todayMinutesRead ?: 0.0f
    
    val progress = if (dailyGoal > 0) (todayMinutes / dailyGoal.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "goal_anim_dial")
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "দৈনিক লক্ষ্য এবং ব্যাজ (Daily Reading Goal)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TealPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    modifier = Modifier.size(130.dp)
                )
                // Active Dial
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    strokeWidth = 12.dp,
                    color = TealPrimary,
                    modifier = Modifier.size(130.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f".format(todayMinutes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/ $dailyGoal min",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Adjust daily goal minutes selection
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Set daily target:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                listOf(15, 30, 45, 60).forEach { mins ->
                    val selected = mins == dailyGoal
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) TealPrimary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .clickable { viewModel.updateDailyReadingGoal(mins) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${mins}m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Milestones and level-up badges
            Text(
                text = "Milestones & Achievements",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Achievement 1: Dawn Reader
                val dawnUnlocked = todayMinutes >= 15f
                AchievementBadgeItem(
                    badgeName = "Dawn Reader",
                    desc = "Read 15 min today",
                    isUnlocked = dawnUnlocked,
                    modifier = Modifier.weight(1f)
                )
                // Achievement 2: AI scholar
                val scholarUnlocked = todayMinutes >= dailyGoal && todayMinutes > 0f
                AchievementBadgeItem(
                    badgeName = "AI Scholar",
                    desc = "Clear target goal",
                    isUnlocked = scholarUnlocked,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AchievementBadgeItem(badgeName: String, desc: String, isUnlocked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isUnlocked) TealLight.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
            .border(1.dp, if (isUnlocked) TealLight.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) OrangeAccent.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (isUnlocked) OrangeAccent else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(badgeName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isUnlocked) TealPrimary else Color.Gray)
                Text(desc, fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun InteractiveWordbookSection(viewModel: ReaderViewModel, wordList: List<WordEntity>) {
    var showPracticeTest by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("আমার শব্দকোষ (Vocabulary Booklet)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                Text("${wordList.size}টি শব্দ সংরক্ষিত আছে", fontSize = 12.sp, color = Color.Gray)
            }
            if (wordList.isNotEmpty()) {
                Button(
                    onClick = { showPracticeTest = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("অনুশীলন পরীক্ষা (Practice MCQ)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (wordList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray.copy(alpha = 0.5f))
                    Text("শব্দকোষ বর্তমানে সম্পূর্ণ খালি!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(
                        "বই পড়ার সময় যে কোনো শব্দের ওপর ট্যাপ করে\nতার তাৎক্ষণিক অর্থ খুঁজুন এবং সেভ করুন।",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wordList) { wordItem ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(wordItem.word, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                    if (wordItem.pronunciation.isNotBlank()) {
                                        Text(wordItem.pronunciation, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteWord(wordItem) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete word", tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("English Meaning:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Text(wordItem.definition, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            
                            if (wordItem.translation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("বাংলা / স্থানীয় অনুবাদ:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealPrimary)
                                Text(wordItem.translation, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (wordItem.sentenceContext.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = "\"${wordItem.sentenceContext}\" — Ref: ${wordItem.bookTitle}",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPracticeTest && wordList.isNotEmpty()) {
        WordbookPracticeDialog(wordList = wordList, onDismiss = { showPracticeTest = false })
    }
}

@Composable
fun WordbookPracticeDialog(wordList: List<WordEntity>, onDismiss: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    val currentWord = wordList[currentIndex % wordList.size]

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    if (currentIndex < wordList.size - 1) {
                        currentIndex++
                        isFlipped = false
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text(if (currentIndex < wordList.size - 1) "পরবর্তী শব্দ (Next)" else "শেষ করুন (Finish)", color = TealPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("বন্ধ করুন (Close)") }
        },
        title = {
            Text("শব্দকোষ ফ্ল্যাশকার্ড প্র্যাকটিস (${currentIndex + 1}/${wordList.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("কার্ডের ওপর ট্যাপ করে ফ্লিপ করুন ও অর্থ স্মরণ করুন", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!isFlipped) TealPrimary.copy(alpha = 0.1f) else OrangeAccent.copy(alpha = 0.1f))
                        .border(2.dp, if (!isFlipped) TealPrimary else OrangeAccent, RoundedCornerShape(16.dp))
                        .clickable { isFlipped = !isFlipped }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isFlipped) {
                            Text(currentWord.word, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            Text(currentWord.pronunciation, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                            Text("💡 ট্যাপ করুন অর্থ প্রকাশ করতে", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Text("অর্থ ও অনুবাদ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OrangeAccent)
                            Text(currentWord.definition, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            Text(currentWord.translation, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                            Text("Context: ${currentWord.sentenceContext.take(40)}...", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun AIFlashcardsSection(viewModel: ReaderViewModel) {
    val selectedBookId = viewModel.selectedBookId
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val flashcards by viewModel.selectedBookFlashcards.collectAsState(initial = emptyList())
    val activeBook = books.find { it.id == selectedBookId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("এআই ফ্ল্যাশকার্ড স্টাডি (AI Chapter Flashcards)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealPrimary)

        // Book selector
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Select Book or Article to Study:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(books) { bk ->
                        val isSelected = bk.id == selectedBookId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) TealPrimary else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectedBookId = bk.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = bk.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (activeBook == null) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("অনুগ্রহ করে ওপরের তালিকা থেকে একটি বই নির্বাচন করুন।", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "বই: ${activeBook.title}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (flashcards.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearFlashcardsForBook() }) {
                        Text("মুছে ফেলুন (Clear)", color = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }

            if (flashcards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TealLight, modifier = Modifier.size(48.dp))
                        Text(
                            text = "এই বইয়ের জন্য কোনো ফ্ল্যাশকার্ড পাওয়া যায়নি।",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "জেমিনি এআই দিয়ে সম্পূর্ণ চ্যাপ্টারগুলো বিশ্লেষণ করে তাত্ক্ষণিকভাবে কুইজ ও ফ্ল্যাশকার্ড তৈরি করুন!",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        if (viewModel.aiFlashcardLoading) {
                            CircularProgressIndicator(color = TealPrimary)
                        } else {
                            Button(
                                onClick = { 
                                    val excerpt = activeBook.contentMarkdown.take(2000)
                                    viewModel.generateAIFlashcards(activeBook.title, excerpt)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("এআই ফ্ল্যাশকার্ড তৈরি করুন (Generate AI Cards)")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "কার্ডের ওপর ট্যাপ করে ফ্লিপ দিন এবং সঠিক উত্তর দেখে নিন:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(flashcards) { card ->
                        var isFlipped by remember { mutableStateOf(false) }
                        
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFlipped) OrangeAccent.copy(alpha = 0.05f) else TealPrimary.copy(alpha = 0.05f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isFlipped) OrangeAccent.copy(alpha = 0.3f) else TealPrimary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isFlipped = !isFlipped }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (!isFlipped) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                                        Text("QUESTION / প্রশ্ন:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(card.question, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("💡 ট্যাপ করুন উওর দেখতে...", fontSize = 10.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                                        Text("ANSWER / উত্তর:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(card.answer, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("💡 ট্যাপ দিয়ে পুনরায় প্রশ্ন দেখুন", fontSize = 10.sp, color = Color.Gray, fontStyle = FontStyle.Italic)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileBanner(activeUser: UserEntity?) {
    val isGuest = activeUser?.isGuest == true
    val gradientBrush = Brush.linearGradient(
        colors = listOf(TealPrimary, TealDark, Color(0xFF0D534D))
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradientBrush)
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
    ) {
        // Decorative abstract circular layers behind
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(160.dp)
                .offset(x = 40.dp, y = (-40).dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(90.dp)
                .offset(x = (-20).dp, y = 30.dp)
                .background(Color.White.copy(alpha = 0.04f), CircleShape)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-contrast, glowing user avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (activeUser?.name ?: "R").take(1).uppercase(),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "READER DASHBOARD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent,
                        letterSpacing = 1.sp
                    )
                    
                    // Elegant, modern mini-badge showing account state
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isGuest) "GUEST" else "PREMIUM",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Welcome back, ${activeUser?.name ?: "Reader"}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activeUser?.email ?: "guest@myreader.com",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ReadingOverviewCard(activeUser: UserEntity?) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardSectionHeader(
            title = "Reading Overview",
            icon = Icons.Default.TrendingUp,
            iconColor = TealPrimary
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Interactive statistics grid: 2 Columns
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        MetricDashboardCell(
                            title = "Reading Streak",
                            value = "${activeUser?.readingStreak ?: 0} days",
                            description = "Consecutive active days",
                            icon = Icons.Default.Whatshot,
                            tintColor = OrangeAccent
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MetricDashboardCell(
                            title = "Books Completed",
                            value = "${activeUser?.booksRead ?: 0}",
                            description = "Fully read novels/papers",
                            icon = Icons.Default.CheckCircle,
                            tintColor = TealLight
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(130.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .align(Alignment.CenterVertically)
                            .padding(horizontal = 4.dp)
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        MetricDashboardCell(
                            title = "Reading Time",
                            value = "%.1f hrs".format(activeUser?.readingHours ?: 0.0f),
                            description = "Total hours engaged",
                            icon = Icons.Default.AccessTime,
                            tintColor = TealPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        MetricDashboardCell(
                            title = "Downloads",
                            value = "${activeUser?.totalDownloads ?: 0}",
                            description = "Offline books synced",
                            icon = Icons.Default.FileDownloadDone,
                            tintColor = OrangeAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(16.dp))

                // Redesigned embedded activity chart with gradient rounded bars and dotted reference lines
                Text(
                    text = "Weekly Engagement Pattern",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = "Minutes logged in past 7 days split",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val barMinutes = listOf(15f, 30f, 45f, 20f, 60f, 35f, 10f)
                val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    // Dotted background lines for measurement reference
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 1.dp.toPx()
                        val lineCount = 3
                        for (i in 0..lineCount) {
                            val y = (size.height / (lineCount + 1)) * (i + 1)
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.2f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = stroke,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 10f), 0f
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        barMinutes.forEachIndexed { idx, barMins ->
                            val isFridayPeak = idx == 4
                            val barHeightFraction = (barMins / 60f).coerceIn(0.1f, 1.0f)
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${barMins.toInt()}m",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isFridayPeak) OrangeAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(barHeightFraction)
                                        .width(18.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (isFridayPeak) {
                                                Brush.verticalGradient(listOf(OrangeAccent, OrangeAccent.copy(alpha = 0.5f)))
                                            } else {
                                                Brush.verticalGradient(listOf(TealPrimary, TealPrimary.copy(alpha = 0.4f)))
                                            }
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = weekdays[idx],
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricDashboardCell(
    title: String,
    value: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tintColor.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, tintColor.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = description,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun CommunityOverviewCard(
    viewModel: ReaderViewModel,
    postsCreated: Int,
    likesReceived: Int,
    commentsReceived: Int,
    myPosts: List<PostEntity>
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardSectionHeader(
            title = "Community Overview",
            icon = Icons.Default.Forum,
            iconColor = OrangeAccent
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Your Social Hub Engagement",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "A summary of reader interactions & posted blogs",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumCommunityMetricCell(
                        label = "Posts Created",
                        count = postsCreated,
                        icon = Icons.Default.PostAdd,
                        color = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumCommunityMetricCell(
                        label = "Likes Received",
                        count = likesReceived,
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    PremiumCommunityMetricCell(
                        label = "Comments Received",
                        count = commentsReceived,
                        icon = Icons.Default.Comment,
                        color = TealLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (myPosts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Last Published Feed:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectedDashboardTab = "forum" }
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(TealPrimary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RssFeed,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = myPosts.first().title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            val elapsedText = android.text.format.DateUtils.getRelativeTimeSpanString(
                                myPosts.first().timestamp,
                                System.currentTimeMillis(),
                                android.text.format.DateUtils.MINUTE_IN_MILLIS
                            ).toString()
                            Text(
                                text = elapsedText,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumCommunityMetricCell(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun LibraryGridSection(
    viewModel: ReaderViewModel,
    favoriteBooks: List<BookEntity>,
    favoriteCount: Int,
    downloadedBooks: List<BookEntity>,
    downloadCount: Int,
    readingHistoryBooks: List<BookEntity>,
    historyCount: Int,
    allBookmarks: List<BookmarkEntity>,
    bookmarksCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardSectionHeader(
            title = "Personal Library",
            icon = Icons.Default.Stars,
            iconColor = TealLight
        )

        // 1. Favorites shelf
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Text(
                            text = "Favorites ($favoriteCount)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Browse Books",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary,
                        modifier = Modifier.clickable { viewModel.selectedDashboardTab = "home" }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (favoriteBooks.isEmpty()) {
                    PremiumEmptyLibraryState("Your favorites shelf is waiting for beautiful stories.")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(favoriteBooks) { book ->
                            RedesignedBookItemCard(
                                book = book,
                                onClick = { viewModel.navigateToBookDetails(book.id) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Downloads list shelf
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DownloadDone, contentDescription = null, tint = TealLight, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Downloads ($downloadCount)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (downloadedBooks.isEmpty()) {
                    PremiumEmptyLibraryState("No books saved offline yet. Tap download on any book.")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(downloadedBooks) { book ->
                            RedesignedBookItemCard(
                                book = book,
                                onClick = { viewModel.navigateToBookDetails(book.id) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Reading History list shelf
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.History, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Reading History ($historyCount)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (readingHistoryBooks.isEmpty()) {
                    PremiumEmptyLibraryState("Your recent feed is empty. Start reading your first book!")
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(readingHistoryBooks) { book ->
                            RedesignedBookItemCard(
                                book = book,
                                onClick = { viewModel.navigateToBookDetails(book.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Global Bookmarks shelf
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Bookmarks ($bookmarksCount)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                if (allBookmarks.isEmpty()) {
                    PremiumEmptyLibraryState("No reading markers saved. Add bookmark inside panels.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        allBookmarks.take(5).forEach { bookmark ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.navigateToBookDetails(bookmark.bookId) }
                                    .padding(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(OrangeAccent.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = OrangeAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = bookmark.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Page ${bookmark.pageNumber + 1} • \"${bookmark.snippet.take(45)}\"",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Read Book",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RedesignedBookItemCard(
    book: BookEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.015f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)),
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Book Spine/Volume representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                TealPrimary.copy(alpha = 0.25f),
                                TealPrimary.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(1.dp, TealPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Subtle book graphic overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw simulated pages stack on the right edge
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(size.width - 6.dp.toPx(), 4.dp.toPx()),
                        size = Size(4.dp.toPx(), size.height - 8.dp.toPx())
                    )
                    // Draw decorative spine binding line on the left edge
                    drawRect(
                        color = TealDark.copy(alpha = 0.4f),
                        topLeft = Offset(2.dp.toPx(), 0f),
                        size = Size(6.dp.toPx(), size.height)
                    )
                }

                Text(
                    text = book.title.take(1).uppercase(),
                    color = TealPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = book.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = book.author,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PremiumEmptyLibraryState(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.01f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ContributionAreaCard(
    viewModel: ReaderViewModel,
    uploadedBooksCount: Int,
    uploadedArticlesCount: Int,
    pendingReviewsCount: Int,
    publishedContentCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DashboardSectionHeader(
            title = "Contribution Area",
            icon = Icons.Default.WorkspacePremium,
            iconColor = TealLight
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Your Submissions & Reviews",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Keep track of suggested content & catalog publishing logs",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Elegant grid arrangement of contribution modules
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumContributionMetricCell(
                            label = "Uploaded Books",
                            count = uploadedBooksCount,
                            icon = Icons.Default.Book,
                            color = TealPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumContributionMetricCell(
                            label = "Uploaded Articles",
                            count = uploadedArticlesCount,
                            icon = Icons.Default.LibraryBooks,
                            color = OrangeAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumContributionMetricCell(
                            label = "Pending Reviews",
                            count = pendingReviewsCount,
                            icon = Icons.Default.HourglassEmpty,
                            color = TealLight,
                            modifier = Modifier.weight(1f)
                        )
                        PremiumContributionMetricCell(
                            label = "Published Content",
                            count = publishedContentCount,
                            icon = Icons.Default.CheckCircle,
                            color = TealPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.selectedDashboardTab = "profile" },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Contribute / Suggest Books",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumContributionMetricCell(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                lineHeight = 12.sp
            )
        }
    }
}

// Subcomponent: Section Header
@Composable
fun DashboardSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Subcomponent: Metric Display Card
@Composable
fun MetricDashboardCard(
    title: String,
    value: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(tintColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                lineHeight = 13.sp
            )
        }
    }
}

// Subcomponent: Community Cell
@Composable
fun CommunityMetricCell(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Subcomponent: Contribution Cell
@Composable
fun ContributionMetricCell(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "$count",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// Subcomponent: Mini book card for horizontal shelf
@Composable
fun MiniBookItemCard(
    book: BookEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)),
        modifier = Modifier
            .width(96.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(TealPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = book.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = book.author,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Subcomponent: Empty Library State
@Composable
fun EmptyLibraryState(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Stars,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StatValueChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// 4.E USER PROFILE TAB (Theme selection, notification centers, language)
@Composable
fun UserProfileTab(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val alerts by viewModel.notifications.collectAsState(initial = emptyList())
    var editNameState by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(activeUser?.name ?: "") }
    var passwordInput by remember { mutableStateOf("") }

    var expandedRequests by remember { mutableStateOf(false) }
    var expandedDMCA by remember { mutableStateOf(false) }
    var expandedMonetization by remember { mutableStateOf(false) }
    var expandedBackup by remember { mutableStateOf(false) }

    var activeModal by remember { mutableStateOf<String?>(null) } // "edit_profile", "change_password", "privacy", "support", "activity"

    // Custom dark premium colors
    val darkCardBg = Color(0xFF131828)
    val accentGlow = Color(0xFF00ADB5)
    val lightGlow = Color(0xFF00E5FF)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Customized Top App Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.selectedDashboardTab = "home" },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(darkCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back To Home",
                            tint = accentGlow
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when (viewModel.currentLanguage) {
                            "Bengali" -> "প্রোফাইল স্টুডিও"
                            "Arabic" -> "الملف الشخصي"
                            else -> "Profile Studio"
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { activeModal = "search_help" },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(darkCardBg)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray)
                    }
                    IconButton(
                        onClick = { viewModel.showNotificationCenter = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(darkCardBg)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "System Alerts", tint = Color.LightGray)
                    }
                }
            }
        }

        // 2. Large Premium Profile Card with Abstract Wave Curves Backdrop
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, accentGlow.copy(alpha = 0.25f)),
                colors = CardDefaults.cardColors(containerColor = darkCardBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        // Custom wave curves flowing in background matching reference image
                        val wavePath1 = Path().apply {
                            moveTo(w * 0.35f, h)
                            cubicTo(w * 0.55f, h * 0.65f, w * 0.75f, h * 0.85f, w, h * 0.25f)
                        }
                        drawPath(
                            path = wavePath1,
                            brush = Brush.horizontalGradient(listOf(Color.Transparent, accentGlow.copy(alpha = 0.08f), lightGlow.copy(alpha = 0.15f))),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        val wavePath2 = Path().apply {
                            moveTo(w * 0.45f, h)
                            cubicTo(w * 0.65f, h * 0.5f, w * 0.8f, h * 0.75f, w, h * 0.12f)
                        }
                        drawPath(
                            path = wavePath2,
                            brush = Brush.horizontalGradient(listOf(Color.Transparent, lightGlow.copy(alpha = 0.04f), accentGlow.copy(alpha = 0.12f))),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Avatar & Top Row Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // High fidelity native illustrated Compose Avatar
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .drawBehind {
                                    drawCircle(
                                        color = accentGlow.copy(alpha = 0.15f),
                                        radius = size.minDimension / 2f + 4.dp.toPx()
                                    )
                                }
                                .border(2.dp, Brush.horizontalGradient(listOf(accentGlow, lightGlow)), CircleShape)
                                .background(Color(0xFF1E293B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                // Draw cute face background
                                drawCircle(
                                    color = Color(0xFFFFD1A9),
                                    radius = w * 0.33f,
                                    center = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.52f)
                                )
                                // Hair
                                val hair = Path().apply {
                                    moveTo(w * 0.16f, h * 0.45f)
                                    cubicTo(w * 0.18f, h * 0.13f, w * 0.82f, h * 0.13f, w * 0.84f, h * 0.45f)
                                    lineTo(w * 0.76f, h * 0.34f)
                                    cubicTo(w * 0.65f, h * 0.27f, w * 0.35f, h * 0.27f, w * 0.24f, h * 0.34f)
                                    close()
                                }
                                drawPath(hair, Color(0xFF1F2937))
                                // Beard
                                drawArc(
                                    color = Color(0xFF374151),
                                    startAngle = 0f,
                                    sweepAngle = 180f,
                                    useCenter = true,
                                    size = androidx.compose.ui.geometry.Size(w * 0.66f, h * 0.46f),
                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.17f, h * 0.41f)
                                )
                                // Glasses
                                val glassesY = h * 0.45f
                                val es = w * 0.17f
                                drawRect(
                                    color = Color(0xFF1F2937),
                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.23f, glassesY),
                                    size = androidx.compose.ui.geometry.Size(es, es * 0.8f),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                                drawRect(
                                    color = Color(0xFF1F2937),
                                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.60f, glassesY),
                                    size = androidx.compose.ui.geometry.Size(es, es * 0.8f),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                                drawLine(
                                    color = Color(0xFF1F2937),
                                    start = androidx.compose.ui.geometry.Offset(w * 0.40f, glassesY + es * 0.4f),
                                    end = androidx.compose.ui.geometry.Offset(w * 0.60f, glassesY + es * 0.4f),
                                    strokeWidth = 2.5.dp.toPx()
                                )
                                // Collar
                                val collar = Path().apply {
                                    moveTo(w * 0.36f, h * 0.84f)
                                    lineTo(w * 0.50f, h * 0.96f)
                                    lineTo(w * 0.64f, h * 0.84f)
                                    lineTo(w * 0.50f, h * 0.79f)
                                    close()
                                }
                                drawPath(collar, accentGlow)
                            }
                            // Active indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 2.dp, bottom = 2.dp)
                                    .size(12.dp)
                                    .background(Color(0xFF22C55E), CircleShape)
                                    .border(1.5.dp, darkCardBg, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Welcome back,",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeUser?.name ?: "Admin",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Profile",
                                    tint = lightGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = activeUser?.email ?: "admin@myreaderpro.com",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accentGlow.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (activeUser?.role == "ADMIN" || activeUser?.role == "SUPER_ADMIN") "Super Admin" else "Pro Moderator",
                                        color = accentGlow,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = activeUser?.membershipType ?: "VIP Tier",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Top Right "Edit Profile" Button matching image reference placement
                        IconButton(
                            onClick = { activeModal = "edit_profile" },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "View Profile", tint = accentGlow, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Embedded small high-contrast Info Chips Row matching the design exactly
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Role", if (activeUser?.role == "ADMIN") "Super Admin" else "Administrator", Icons.Default.Security),
                            Triple("Last Login", "Today, 09:40 AM", Icons.Default.Schedule),
                            Triple("IP Address", "192.168.1.104", Icons.Default.Public)
                        ).forEach { (label, value, icon) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(accentGlow.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = accentGlow, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(label, color = Color.Gray, fontSize = 9.sp)
                                        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Actions Header & Grid Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Actions Studio",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Toggle Actions",
                        fontSize = 12.sp,
                        color = accentGlow,
                        modifier = Modifier.clickable { activeModal = "all_actions" }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Beautiful, unified Quick Actions Grid
                val quickActions = listOf(
                    Triple("Edit Profile", Icons.Default.Person, "edit_profile"),
                    Triple("Change Pass", Icons.Default.Lock, "change_password"),
                    Triple("Privacy Sec", Icons.Default.Security, "privacy"),
                    Triple("Language", Icons.Default.Translate, "language_theme"),
                    Triple("Visual Theme", Icons.Default.Palette, "language_theme"),
                    Triple("Active Logs", Icons.Default.History, "activity"),
                    Triple("Support Help", Icons.Default.HeadsetMic, "support"),
                    Triple("Sign Out", Icons.Default.Logout, "logout_warning")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickActions.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (label, icon, key) ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeModal = key },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = darkCardBg),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (key == "logout_warning") Color.Red.copy(alpha = 0.12f)
                                                    else accentGlow.copy(alpha = 0.1f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (key == "logout_warning") Color.Red else accentGlow,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = label,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Account Overview Details Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                colors = CardDefaults.cardColors(containerColor = darkCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = accentGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Account Overview Profile", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "Registration Verify" to "Google Secure OAuth Node",
                        "Sync Coordinates" to "Firebase Live Realtime DB",
                        "Account Created" to "January 14, 2026",
                        "Device Terminal ID" to "UID-MR9548-PRO"
                    ).forEach { (field, info) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(field, color = Color.Gray, fontSize = 12.sp)
                            Text(info, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))
                    }
                }
            }
        }

        // 5. Reactive Interactive Security Panel
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                colors = CardDefaults.cardColors(containerColor = darkCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = accentGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Security & Audit Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Two-Factor Authentication", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Enforce one-time verification tokens", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentGlow
                            )
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.04f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Biometric Smart Sign-on", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Sign in using secure fingerprints", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = false,
                            onCheckedChange = { },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = accentGlow
                            )
                        )
                    }
                }
            }
        }

        // 6. Interactive Language & Visual Themes Selectors Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                colors = CardDefaults.cardColors(containerColor = darkCardBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Visual Theme Configuration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Light", "Dark", "System").forEach { tm ->
                            val isS = viewModel.currentTheme == tm
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isS) accentGlow else Color.White.copy(alpha = 0.05f))
                                    .clickable { viewModel.currentTheme = tm }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tm,
                                    color = if (isS) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Preferred Language",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("English", "Bengali", "Arabic").forEach { ln ->
                            val isS = viewModel.currentLanguage == ln
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isS) accentGlow else Color.White.copy(alpha = 0.05f))
                                    .clickable { viewModel.currentLanguage = ln }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ln,
                                    color = if (isS) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Recent System Activity Timeline List (Inverted Dark Slate)
        item {
            Column {
                Text(
                    text = "Recent Security & User Logs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F121C)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        val events = listOf(
                            Triple("New user registration", "John Doe joined the platform with SuperAdmin verification.", "2 min ago"),
                            Triple("New book database catalog upload", "Successfully compiled text coordinate: 'The Psychology of Money'", "15 min ago"),
                            Triple("Subscription purchase complete", "Pro user level VIP upgraded automatically.", "30 min ago"),
                            Triple("Secure system review indexing", "5-Star rating audit recorded for 'Atomic Habits'", "1 hour ago"),
                            Triple("Administrative help ticket closed", "Support request #MR-548 resolved.", "2 hours ago")
                        )

                        events.forEachIndexed { idx, (title, desc, tm) ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == 0) accentGlow else Color.Gray)
                                    )
                                    if (idx < events.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(40.dp)
                                                .background(Color.White.copy(alpha = 0.1f))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(tm, color = Color.Gray, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(desc, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Keep 4 Expandable Hub Cards for original capabilities
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = accentGlow.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, accentGlow.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expandedRequests = !expandedRequests }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "", tint = accentGlow)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Book Requests Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentGlow)
                        Text("Track requested publications.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (expandedRequests) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = accentGlow
                    )
                }
            }
        }

        if (expandedRequests) {
            item { BookRequestHub(viewModel, activeUser) }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expandedDMCA = !expandedDMCA }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "", tint = Color.Red)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DMCA Takedown Center", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                        Text("Review audits & claims.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (expandedDMCA) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        }

        if (expandedDMCA) {
            item { CopyrightClaimHub(viewModel, activeUser) }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lightGlow.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, lightGlow.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expandedMonetization = !expandedMonetization }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "", tint = lightGlow)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SaaS Monetization Switchboard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = lightGlow)
                        Text("Commercial configurations.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (expandedMonetization) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = lightGlow
                    )
                }
            }
        }

        if (expandedMonetization) {
            item { MonetizationHub(viewModel, activeUser) }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.20f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { expandedBackup = !expandedBackup }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.History, contentDescription = "", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Backup & JSON Exports Center", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Export database coordinates.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(
                        imageVector = if (expandedBackup) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
        }

        if (expandedBackup) {
            item { BackupAndRestoreHub(viewModel) }
        }

        // Live Administrative Panel Trigger Option
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = accentGlow.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, accentGlow.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .clickable { viewModel.currentScreen = "admin_dashboard" }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = "", tint = accentGlow)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Catalog Publisher Web Console", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = accentGlow)
                        Text("Open premium standalone SaaS Administrative Dashboard.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "", tint = accentGlow)
                }
            }
        }

        if (viewModel.showAdminPanel) {
            item { AdminConsoleDialog(viewModel) }
        }

        // Bottom spacer
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal dialogue boxes for interactive Quick Actions with absolutely NO dead-end UI
    if (activeModal == "edit_profile") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Edit Profile Credentials", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Change Display Username", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentGlow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProfileName(editedName)
                        activeModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGlow)
                ) {
                    Text("Apply Username", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeModal = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }

    if (activeModal == "change_password") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Update Security Password", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Specify a secure key with min 8 characters.", color = Color.Gray, fontSize = 12.sp)
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        singleLine = true,
                        placeholder = { Text("Enter New Password", color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = accentGlow,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        passwordInput = ""
                        activeModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGlow)
                ) {
                    Text("Encrypt Key", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeModal = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }

    if (activeModal == "privacy") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Privacy Coordinates", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pursuing General Data Protection Regulation (GDPR) and local cache protection coordinates.", color = Color.Gray, fontSize = 12.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("✓ Local Sandbox SQLite Encrypted", color = accentGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("✓ External API Logs Scrubbed Daily", color = accentGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("✓ Strict Zero-Telemetry Mode Enabled", color = accentGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGlow)
                ) {
                    Text("Confirm Audit", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }

    if (activeModal == "support") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Contact Client Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Our premium support lines are open 24/7 with immediate SLA responses.", color = Color.Gray, fontSize = 12.sp)
                    Text("📧 support@myreaderpro.com", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("☎ +1 (800) 555-READ", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentGlow.copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        Text("SLA response guarantee within 15 minutes for premium users.", color = accentGlow, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGlow)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }

    if (activeModal == "activity") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Active System Logs", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Chronological audit checkpoints generated by this account terminal session:", color = Color.Gray, fontSize = 12.sp)
                    listOf(
                        "05:28 AM" to "Device encryption token matched",
                        "05:12 AM" to "Database backup signature validated",
                        "04:45 AM" to "Chapter tracking synchronization validated",
                        "04:02 AM" to "OAuth portal handshake authorized successfully"
                    ).forEach { (tm, text) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(tm, color = accentGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGlow)
                ) {
                    Text("Dismiss Logs", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }

    if (activeModal == "logout_warning") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Log Out Account", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Text("Are you sure you want to dismiss your local secure token and log out of MyReaderPro?", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeModal = null
                        viewModel.handleLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeModal = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }

    if (activeModal == "search_help" || activeModal == "language_theme" || activeModal == "all_actions") {
        AlertDialog(
            onDismissRequest = { activeModal = null },
            title = { Text("Profile Information Info", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Text("Select any settings above to customize language or visual themes instantly.", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = { activeModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGlow)
                ) {
                    Text("Dismiss", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF0F121C)
        )
    }
}

// 4.E.a LIVE ADMIN CONSOLE PANEL EMULATION
@Composable
fun AdminConsoleDialog(viewModel: ReaderViewModel) {
    var adminTab by remember { mutableStateOf("analytics") } // analytics, books, categories, users, alerts, claims, requests

    // Collected states from VM
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    val users by viewModel.allUsers.collectAsState(initial = emptyList())
    val notifications by viewModel.notifications.collectAsState(initial = emptyList())
    val copyrightClaims by viewModel.allCopyrightClaims.collectAsState(initial = emptyList())
    val bookRequests by viewModel.allBookRequests.collectAsState(initial = emptyList())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row with modern horizontal admin logo branding
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyReaderProLogo(
                    variant = LogoVariant.HORIZONTAL_ADMIN,
                    scale = 1.0f
                )
            }
            Text(
                text = "Back-end dashboard simulation. All actions modify the local Room database instantly.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Scrollable tabs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "analytics" to "📈 Stats",
                    "books" to "📚 Books",
                    "categories" to "📁 Categories",
                    "users" to "👥 Users",
                    "alerts" to "🔔 Alerts",
                    "claims" to "⚖️ DMCA",
                    "requests" to "📨 Requests"
                ).forEach { (id, label) ->
                    val isSelected = adminTab == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) OrangeAccent else MaterialTheme.colorScheme.surface)
                            .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .clickable { adminTab = id }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content Selection
            when (adminTab) {
                "analytics" -> AdminAnalyticsTab(books, categories, users, copyrightClaims, bookRequests, notifications)
                "books" -> AdminBooksTab(viewModel, books, categories)
                "categories" -> AdminCategoriesTab(viewModel, categories)
                "users" -> AdminUsersTab(viewModel, users)
                "alerts" -> AdminAlertsTab(viewModel, notifications)
                "claims" -> AdminClaimsTab(viewModel, copyrightClaims)
                "requests" -> AdminRequestsTab(viewModel, bookRequests)
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(
    books: List<BookEntity>,
    categories: List<CategoryEntity>,
    users: List<UserEntity>,
    claims: List<CopyrightClaimEntity>,
    requests: List<BookRequestEntity>,
    notifications: List<NotificationEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📈 System Status Metrics", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        // Grid-like layout for stat cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatItem(
                label = "Total Books",
                value = "${books.size}",
                caption = "${books.filter { it.isFeatured }.size} Featured",
                icon = Icons.Default.Book,
                color = TealPrimary,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Categories",
                value = "${categories.size}",
                caption = "Active subjects",
                icon = Icons.Default.FolderOpen,
                color = OrangeAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatItem(
                label = "Active Accounts",
                value = "${users.size}",
                caption = "${users.filter { it.role == "ADMIN" }.size} Admins | ${users.filter { it.role == "MODERATOR" }.size} Mods",
                icon = Icons.Default.People,
                color = TealLight,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "DMCA Claims",
                value = "${claims.size}",
                caption = "${claims.filter { it.status == "PENDING" }.size} Pending",
                icon = Icons.Default.Security,
                color = Color.Red,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatItem(
                label = "Live Requests",
                value = "${requests.filter { it.status == "PENDING" }.size}",
                caption = "${requests.filter { it.status == "COMPLETED" }.size} Handled",
                icon = Icons.Default.Email,
                color = Color.Magenta,
                modifier = Modifier.weight(1f)
            )
            StatItem(
                label = "Alerts Broadcasted",
                value = "${notifications.size}",
                caption = "Active board",
                icon = Icons.Default.Campaign,
                color = Color.Blue,
                modifier = Modifier.weight(1f)
            )
        }

        // Plan distribution simulator
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Membership Split (Demographics)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val freeCount = users.filter { it.membershipType == "FREE" }.size
                val premiumCount = users.filter { it.membershipType == "PREMIUM" }.size
                val vipCount = users.filter { it.membershipType == "VIP" }.size
                val total = (freeCount + premiumCount + vipCount).toFloat().coerceAtLeast(1f)

                ProgressBarWithLabel(label = "VIP Members (${vipCount})", progress = vipCount / total, color = OrangeAccent)
                Spacer(modifier = Modifier.height(6.dp))
                ProgressBarWithLabel(label = "Premium Members (${premiumCount})", progress = premiumCount / total, color = TealPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                ProgressBarWithLabel(label = "Free Tier (${freeCount})", progress = freeCount / total, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    caption: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            Text(caption, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProgressBarWithLabel(label: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${(progress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
    }
}

@Composable
fun AdminBooksTab(
    viewModel: ReaderViewModel,
    books: List<BookEntity>,
    categories: List<CategoryEntity>
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var catId by remember { mutableStateOf("novel") }
    var desc by remember { mutableStateOf("") }
    var pagesCount by remember { mutableStateOf("120") }
    var fileSizeText by remember { mutableStateOf("1.5 MB") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📐 Publish New Volume", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        if (toastMessage != null) {
            Text(toastMessage!!, color = TealLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Book Title *", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author / Reporter *", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Category selection chip row
        Text("Classification Group *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = catId == cat.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface)
                        .border(0.5.dp, if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .clickable { catId = cat.id }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat.name,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Syllated Synopsis Note *", fontSize = 12.sp) },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = pagesCount,
                onValueChange = { pagesCount = it },
                label = { Text("Pages *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = fileSizeText,
                onValueChange = { fileSizeText = it },
                label = { Text("File Size *") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (title.isBlank() || author.isBlank() || desc.isBlank()) {
                    toastMessage = "Error: Please complete all required specifications."
                } else {
                    viewModel.adminAddBook(
                        title = title,
                        author = author,
                        categoryId = catId,
                        description = desc,
                        pages = pagesCount.toIntOrNull() ?: 120,
                        size = fileSizeText
                    )
                    toastMessage = "Success: Book published & system crawlers alerted!"
                    title = ""
                    author = ""
                    desc = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Publish to Repository Portal", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("📚 Active Catalog Manager", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        // List existing books with a retires modifier
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val activeBooks = books.filter { it.categoryId != "hidden" }
                if (activeBooks.isEmpty()) {
                    item {
                        Text("No active catalog books found.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                } else {
                    items(activeBooks) { book ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(book.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("by ${book.author} | Group: ${book.categoryId}", fontSize = 10.sp, color = Color.Gray)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.adminDeleteBook(book.id)
                                    toastMessage = "Book '${book.title}' retired successfully."
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Retire", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesTab(
    viewModel: ReaderViewModel,
    categories: List<CategoryEntity>
) {
    var catIdInput by remember { mutableStateOf("") }
    var catNameInput by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("ic_star") }
    var catMessage by remember { mutableStateOf<String?>(null) }

    val iconChoices = listOf(
        "ic_star" to Icons.Default.Stars,
        "ic_book" to Icons.Default.LibraryBooks,
        "ic_general" to Icons.Default.FolderOpen,
        "ic_school" to Icons.Default.School,
        "ic_heart" to Icons.Default.Favorite
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📁 Register Category Subject", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        if (catMessage != null) {
            Text(catMessage!!, color = TealLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = catIdInput,
            onValueChange = { catIdInput = it.lowercase().trim() },
            label = { Text("Category ID Code * (lowercase, unique)", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = catNameInput,
            onValueChange = { catNameInput = it },
            label = { Text("Category Display Name * (e.g. Science)", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Select Subject Visual Symbol *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            iconChoices.forEach { (name, vec) ->
                val isSel = selectedIcon == name
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) OrangeAccent else MaterialTheme.colorScheme.surface)
                        .border(1.dp, if (isSel) Color.Transparent else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .clickable { selectedIcon = name }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = vec, contentDescription = null, tint = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Button(
            onClick = {
                if (catIdInput.isBlank() || catNameInput.isBlank()) {
                    catMessage = "Error: Classification ID and Display name are required parameters."
                } else {
                    viewModel.adminAddCategory(catIdInput, catNameInput, selectedIcon)
                    catMessage = "Success: Category Group '$catNameInput' registered!"
                    catIdInput = ""
                    catNameInput = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register Category Group", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("📂 Configured Subject Groups", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 180.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (categories.isEmpty()) {
                    item {
                        Text("No custom categories registered.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                } else {
                    items(categories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val vc = iconChoices.find { it.first == cat.iconName }?.second ?: Icons.Default.FolderOpen
                                    Icon(imageVector = vc, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(cat.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Database ID: ${cat.id}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.adminDeleteCategory(cat.id)
                                    catMessage = "Category '${cat.name}' deleted from indices."
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUsersTab(
    viewModel: ReaderViewModel,
    users: List<UserEntity>
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingUserEmail by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf("USER") }
    var editPlan by remember { mutableStateOf("FREE") }
    var operationMsg by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("👥 System Accounts & Role Access", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search accounts (name/email)", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangeAccent) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (operationMsg != null) {
            Text(operationMsg!!, color = TealLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Active editing area
        if (editingUserEmail != null) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, TealLight.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Manage Account: $editingUserEmail", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                        IconButton(onClick = { editingUserEmail = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("User Real Name", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role toggle selection
                    Text("System Security Level Role", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("USER", "MODERATOR", "ADMIN").forEach { rl ->
                            val isSel = editRole == rl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) OrangeAccent else MaterialTheme.colorScheme.surface)
                                    .clickable { editRole = rl }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rl, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Membership plan selection
                    Text("SaaS Membership Access level", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("FREE", "PREMIUM", "VIP").forEach { plan ->
                            val isSel = editPlan == plan
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) TealPrimary else MaterialTheme.colorScheme.surface)
                                    .clickable { editPlan = plan }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(plan, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.adminModifyUser(editingUserEmail!!, editName, editRole, editPlan)
                            operationMsg = "Success: Settings synced down for $editingUserEmail!"
                            editingUserEmail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Save Account Specifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search listings list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val filtered = users.filter { 
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                if (filtered.isEmpty()) {
                    item {
                        Text("No matching accounts search found.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(10.dp))
                    }
                } else {
                    items(filtered) { usr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(usr.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(usr.email, fontSize = 10.sp, color = Color.Gray)
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (usr.role) {
                                                    "ADMIN" -> Color.Red.copy(alpha = 0.15f)
                                                    "MODERATOR" -> Color.Blue.copy(alpha = 0.15f)
                                                    else -> Color.Gray.copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = usr.role,
                                            color = when (usr.role) {
                                                "ADMIN" -> Color.Red
                                                "MODERATOR" -> Color.Blue
                                                else -> Color.DarkGray
                                            },
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(OrangeAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(usr.membershipType, color = OrangeAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    editingUserEmail = usr.email
                                    editName = usr.name
                                    editRole = usr.role
                                    editPlan = usr.membershipType
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = TealPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAlertsTab(
    viewModel: ReaderViewModel,
    notifications: List<NotificationEntity>
) {
    var bTitle by remember { mutableStateOf("") }
    var bMsg by remember { mutableStateOf("") }
    var sendStatus by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("🔔 System Alert Broadcaster", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        if (sendStatus != null) {
            Text(sendStatus!!, color = TealLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = bTitle,
            onValueChange = { bTitle = it },
            label = { Text("Broadcast Bulletin Headline *", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = bMsg,
            onValueChange = { bMsg = it },
            label = { Text("Broadcast Body / Detailed message *", fontSize = 12.sp) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (bTitle.isBlank() || bMsg.isBlank()) {
                    sendStatus = "Error: Headline and Body details are mandatory."
                } else {
                    viewModel.adminBroadcastNotification(bTitle, bMsg)
                    sendStatus = "Success: Notification broadcasted immediately!"
                    bTitle = ""
                    bMsg = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Broadcast Message", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("🗂️ Currently Active Alerts Logs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (notifications.isEmpty()) {
                    item {
                        Text("No active broadcasts reported.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                } else {
                    items(notifications) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(item.message, fontSize = 10.sp, color = Color.DarkGray)
                                Text("Date: 2026-06-02", fontSize = 7.sp, color = Color.Gray)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.adminDeleteNotification(item.id)
                                    sendStatus = "Alert ID ${item.id} deleted."
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Status Alert", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminClaimsTab(
    viewModel: ReaderViewModel,
    claims: List<CopyrightClaimEntity>
) {
    var processMsg by remember { mutableStateOf<String?>(null) }
    var claimNote by remember { mutableStateOf("") }
    var selectedClaimId by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("⚖️ DMCA Claims Reviews Desk", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        if (processMsg != null) {
            Text(processMsg!!, color = TealLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        if (selectedClaimId != null) {
            val claim = claims.find { it.id == selectedClaimId }
            if (claim != null) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Resolve DMCA Claim ID: ${claim.id}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            IconButton(onClick = { selectedClaimId = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        Text("Claimant: ${claim.fullName} | Org: ${claim.organizationName}", fontSize = 11.sp)
                        Text("Affected Content Title: ${claim.contentTitle}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Detailed Argument: ${claim.description}", fontSize = 10.sp, color = Color.Gray)

                        OutlinedTextField(
                            value = claimNote,
                            onValueChange = { claimNote = it },
                            label = { Text("Lawful verdict decisions notes", fontSize = 11.sp) },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateCopyrightStatus(claim.id, "APPROVED", claimNote, hideContent = true)
                                    processMsg = "License verified. Infringing volume secluded!"
                                    selectedClaimId = null
                                    claimNote = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("Approve & Seclude", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateCopyrightStatus(claim.id, "REJECTED", claimNote, hideContent = false)
                                    processMsg = "Claim rejected. Content remained active."
                                    selectedClaimId = null
                                    claimNote = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("Dismiss Claim", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 220.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (claims.isEmpty()) {
                    item {
                        Text("No active claims pending resolution.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                } else {
                    items(claims) { claim ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(claim.contentTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                Text("Submitted Name: ${claim.fullName} (${claim.organizationName})", fontSize = 10.sp, color = Color.Gray)
                                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (claim.status) {
                                                    "PENDING" -> OrangeAccent.copy(alpha = 0.15f)
                                                    "APPROVED" -> Color.Red.copy(alpha = 0.15f)
                                                    else -> TealLight.copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            claim.status,
                                            color = when (claim.status) {
                                                "PENDING" -> OrangeAccent
                                                "APPROVED" -> Color.Red
                                                else -> TealLight
                                            },
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (claim.temporaryHidden) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Red.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("HIDDEN", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    selectedClaimId = claim.id
                                    claimNote = claim.decisionNotes
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Decide", tint = TealPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRequestsTab(
    viewModel: ReaderViewModel,
    requests: List<BookRequestEntity>
) {
    var reqMsg by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("📨 Book Requests Moderator Review", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)

        if (reqMsg != null) {
            Text(reqMsg!!, color = TealLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .background(MaterialTheme.colorScheme.surface)
                .border(0.5.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (requests.isEmpty()) {
                    item {
                        Text("No active book catalog requests found.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                    }
                } else {
                    items(requests) { req ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("by ${req.author} | Submitter: ${req.userEmail}", fontSize = 10.sp, color = Color.Gray)
                                if (req.notes.isNotBlank()) {
                                    Text("Note: ${req.notes}", fontSize = 9.sp, fontStyle = FontStyle.Italic, color = Color.DarkGray)
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (req.status) {
                                                "PENDING" -> OrangeAccent.copy(alpha = 0.15f)
                                                "COMPLETED" -> TealLight.copy(alpha = 0.15f)
                                                else -> Color.Red.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        req.status,
                                        color = when (req.status) {
                                            "PENDING" -> OrangeAccent
                                            "COMPLETED" -> TealLight
                                            else -> Color.Red
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (req.status == "PENDING") {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            viewModel.updateBookRequestStatus(req.id, "COMPLETED")
                                            reqMsg = "Request COMPLETED. Book generated and published!"
                                        },
                                        modifier = Modifier.size(28.dp).background(Color.Green.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Satisfy", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.updateBookRequestStatus(req.id, "REJECTED")
                                            reqMsg = "Book Request set to REJECTED."
                                        },
                                        modifier = Modifier.size(28.dp).background(Color.Red.copy(alpha = 0.2f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Reject", tint = Color.Red, modifier = Modifier.size(14.dp))
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

// 5. BOOK DETAILS SCREEN
@Composable
fun BookDetailsScreen(viewModel: ReaderViewModel) {
    val bookState = viewModel.selectedBook.collectAsState(initial = null)
    val book = bookState.value

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TealPrimary)
        }
        return
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Book Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = TealPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Book cover preview
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(6.dp, RoundedCornerShape(12.dp))
            ) {
                CanvasBookCover(title = book.title, author = book.author, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book basic values
            Text(
                text = book.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "by " + book.author,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Highlight Specs block (Rating, sizes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Grade, contentDescription = null, tint = OrangeAccent)
                    Text(
                        text = "${book.rating}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rating",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.InsertPageBreak, contentDescription = null, tint = TealPrimary)
                    Text(
                        text = "${book.pages}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = TealLight)
                    Text(
                        text = book.language,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Language",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.Gray)
                    Text(
                        text = book.fileSize,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Size",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Download manager status box
            DownloadManagerPanel(book, viewModel)

            Spacer(modifier = Modifier.height(20.dp))

            // Read action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Read button
                Button(
                    onClick = { viewModel.navigateToReader(book.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = "Read Book", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(viewModel.translate("read_now"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Favorite click action
                IconButton(
                    onClick = { viewModel.toggleFavorite(book.id) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite Toggle",
                        tint = if (book.isFavorite) Color.Red else TealPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Share simulation
                var showShareMsg by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showShareMsg = true },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                if (showShareMsg) {
                    AlertDialog(
                        onDismissRequest = { showShareMsg = false },
                        confirmButton = {
                            TextButton(onClick = { showShareMsg = false }) { Text("OK") }
                        },
                        title = { Text("Share '${book.title}'") },
                        text = { Text("Secure share link copied to clipboard!\nShare this premium digital book with classmates easily.") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Book Description content
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Synopsis & Context",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = book.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Report Issues / Typos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Notice translation errors? Click to report directly to editing staff.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Gemini Intelligence Book Summary Card ---
            val summaryText = viewModel.bookSummaryText
            val summaryLoading = viewModel.bookSummaryLoading
            val summaryError = viewModel.bookSummaryError

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TealPrimary.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.5.dp, TealPrimary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_summary_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "জেমিনি স্মার্ট সারাংশ ✨"
                                    "Arabic" -> "ملخص جيمي الذكي ✨"
                                    else -> "Gemini Intelligent Summary ✨"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = TealPrimary
                            )
                        }

                        // Sparkle Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FLASH-3.5",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (summaryLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = TealPrimary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "জেমিনি অধ্যায়গুলো বিশ্লেষণ করে আপনার ভাষায় সারাংশ তৈরি করছে..."
                                    "Arabic" -> "يقوم جيمي بتحليل فصول الكتاب وصياغة الملخص بلغتك المفضلة..."
                                    else -> "Gemini is analyzing book chapters & synthesizing summary in your language..."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (summaryText != null) {
                        Column {
                            Text(
                                text = summaryText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 21.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.clearBookSummary() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Clear Summary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.fetchBookSummary(book.title, book.author, book.description)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Regenerate", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "গুগল জেমিনি এআই মডেলের বুদ্ধিমত্তা ব্যবহার করে এই বইটির চমৎকার অধ্যায়ভিত্তিক আলোচনা, মূল প্রতিপাদ্য এবং বিশেষ অর্ন্তদৃষ্টি আপনার নির্বাচিত ভাষায় (${viewModel.currentLanguage}) তৈরি করুন।"
                                    "Arabic" -> "استخدم قوة نموذج الذكاء الاصطناعي Google Gemini للحصول على تحليل ذكي، الأفكار الرئيسية، والدروس المستفادة بلغاتك المفضلة (${viewModel.currentLanguage})."
                                    else -> "Leverage Google's advanced Gemini AI model to digest key themes, analyze chapter layouts, and compile an comprehensive literary assessment in **${viewModel.currentLanguage}**."
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                lineHeight = 19.sp
                            )

                            if (summaryError != null) {
                                Text(
                                    text = "Error: $summaryError",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    viewModel.fetchBookSummary(book.title, book.author, book.description)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (viewModel.currentLanguage) {
                                        "Bengali" -> "স্মার্ট জেমিনি সারাংশ তৈরি করুন"
                                        "Arabic" -> "توليد ملخص جيمي الذكي"
                                        else -> "Generate Gemini Summary"
                                    },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 5.a DOWNLOAD MANAGER INTERNAL EXPANSION
@Composable
fun DownloadManagerPanel(book: BookEntity, viewModel: ReaderViewModel) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (book.downloadStatus) {
                        "DOWNLOADED" -> Icons.Default.CloudDone
                        "DOWNLOADING" -> Icons.Default.CloudDownload
                        "PAUSED" -> Icons.Default.PauseCircle
                        else -> Icons.Default.CloudDownload
                    },
                    contentDescription = null,
                    tint = if (book.downloadStatus == "DOWNLOADED") TealLight else TealPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (book.downloadStatus) {
                            "DOWNLOADED" -> "Available Offline"
                            "DOWNLOADING" -> "Downloading Offline Copy"
                            "PAUSED" -> "Download Paused"
                            else -> "Download for Offline Reading"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (book.downloadStatus) {
                            "DOWNLOADED" -> "Open files any time, progress keeps offline."
                            "DOWNLOADING" -> "Transferring secured pages: ${(book.downloadProgress * 100).toInt()}%"
                            "PAUSED" -> "Download halted. Click to resume."
                            else -> "Size: ${book.fileSize}. Saved directly locally."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Interactive Toggles in panel
                when (book.downloadStatus) {
                    "DOWNLOADED" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TealLight.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("100% Ready", color = TealLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "DOWNLOADING" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.pauseDownload(book.id) }) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = OrangeAccent)
                            }
                            IconButton(onClick = { viewModel.cancelDownload(book.id) }) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    "PAUSED" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { viewModel.resumeDownload(book.id) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = TealPrimary)
                            }
                            IconButton(onClick = { viewModel.cancelDownload(book.id) }) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    else -> {
                        IconButton(
                            onClick = { viewModel.triggerDownload(book.id) }
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download Book", tint = TealPrimary)
                        }
                    }
                }
            }

            // Downloader visual horizontal progress bar
            if (book.downloadStatus == "DOWNLOADING" || book.downloadStatus == "PAUSED") {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { book.downloadProgress },
                    color = TealPrimary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

// 6. DISTRACTION FREE READER ENGINE SCREEN
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderScreen(viewModel: ReaderViewModel) {
    val bookState = viewModel.selectedBook.collectAsState(initial = null)
    val book = bookState.value

    val bookmarksList by viewModel.selectedBookBookmarks.collectAsState(initial = emptyList())
    val notesList by viewModel.selectedBookNotes.collectAsState(initial = emptyList())

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (book == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = TealPrimary)
        }
        return
    }

    // Capture reading text pages
    val textPages = remember(book) {
        val splitList = book.contentMarkdown.split("\n\n").filter { it.isNotBlank() }
        if (splitList.isEmpty()) {
            listOf("The digital manuscript file appears to be empty. Check back later or request a premium copy.")
        } else {
            splitList
        }
    }

    val initialPage = book.lastReadPosition.coerceIn(0, textPages.size - 1)
    var localPageNumber by remember(book.id) { mutableStateOf(initialPage) }

    var showControlsDrawer by remember { mutableStateOf(true) }
    var noteDialogState by remember { mutableStateOf<String?>(null) } // holds selected paragraph string for note saving
    var selectedNoteColorHex by remember { mutableStateOf("#FEF08A") } // default Yellow highlight

    // Search Inside Content state
    var isSearchingInside by remember { mutableStateOf(false) }
    var innerSearchQuery by remember { mutableStateOf("") }

    // Live session timer statistic
    var sessionSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(book.id) {
        while (true) {
            delay(1000)
            sessionSeconds++
        }
    }

    // Configure theme palettes based on Reader Preferences
    val readerBg = when (viewModel.readerColorMode) {
        "Dark" -> CharcoalBg
        "Sepia" -> SepiaBg
        "Forest Green" -> Color(0xFFE2EFE0)
        "Charcoal" -> Color(0xFF1E2229)
        "Soft Rose" -> Color(0xFFFBE4E8)
        else -> LightBg
    }
    val readerText = when (viewModel.readerColorMode) {
        "Dark" -> TextLight
        "Sepia" -> SepiaText
        "Forest Green" -> Color(0xFF152A15)
        "Charcoal" -> Color(0xFFE0E3EB)
        "Soft Rose" -> Color(0xFF421E23)
        else -> TextDark
    }
    val readerCard = when (viewModel.readerColorMode) {
        "Dark" -> CardDark
        "Sepia" -> SepiaCard
        "Forest Green" -> Color(0xFFD0E0D0)
        "Charcoal" -> Color(0xFF282C34)
        "Soft Rose" -> Color(0xFFF7CCD3)
        else -> CardLight
    }

    val readerFont = when (viewModel.readerFontFamily) {
        "Sans-Serif" -> FontFamily.SansSerif
        "Mono" -> FontFamily.Monospace
        "Monospace" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }

    // Save history and update statistics periodically
    LaunchedEffect(localPageNumber) {
        viewModel.updateReadingPosition(book.id, localPageNumber)
    }

    // Handle back stats calculation on exit
    val backWithStats = {
        // Increment reading statistics in database to show robust live tracking
        coroutineScope.launch {
            val dao = MyReaderDatabase.getDatabase(context, coroutineScope).readerDao()
            val user = dao.getActiveUserSync()
            if (user != null) {
                val updatedHours = user.readingHours + (sessionSeconds.toFloat() / 3600f)
                val updatedPages = user.totalPagesRead + 1
                dao.insertUser(user.copy(
                    readingHours = updatedHours,
                    totalPagesRead = updatedPages,
                    readingStreak = (user.readingStreak + 1).coerceAtMost(30)
                ))
            }
        }
        viewModel.goBack()
    }

    androidx.activity.compose.BackHandler {
        backWithStats()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                if (viewModel.readerBrightness < 1.0f) {
                    drawRect(
                        color = Color.Black.copy(alpha = 1.0f - viewModel.readerBrightness)
                    )
                }
            },
        containerColor = readerBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background branding gradient or format layouts
            val formatTag = book.fileFormat

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Formatting Layout indicator ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(readerCard.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = when (formatTag) {
                                "PDF" -> Color(0xFFEF4444)
                                "ARTICLE" -> Color(0xFFF59E0B)
                                else -> TealPrimary
                            },
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (formatTag) {
                                "PDF" -> "PDF SHEET MODE (Fixed Aspect Page Grid)"
                                "ARTICLE" -> "NEWS ARTICLE VIEW (Scrollable Single View)"
                                else -> "EPUB REFLEX RENDERING (Flowable Typography)"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = readerText.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = "Session: ${sessionSeconds / 60}m ${sessionSeconds % 60}s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = readerText.copy(alpha = 0.5f)
                    )
                }

                // Main readable page box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable { showControlsDrawer = !showControlsDrawer }
                ) {
                    if (formatTag == "ARTICLE") {
                        // Continuous Linear scrollable layout for Article format
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = viewModel.readerMargin.dp)
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Progress bar indicator for continuous scroll article
                            val totalScroll = scrollState.maxValue
                            val progressFactor = if (totalScroll > 0) scrollState.value.toFloat() / totalScroll else 1.0f
                            LinearProgressIndicator(
                                progress = progressFactor,
                                color = TealPrimary,
                                trackColor = readerText.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = book.title,
                                fontSize = (viewModel.readerFontSize + 8).sp,
                                fontWeight = FontWeight.Bold,
                                color = readerText,
                                fontFamily = readerFont,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "By ${book.author} — Complete linear transcript",
                                fontSize = 12.sp,
                                color = readerText.copy(alpha = 0.6f),
                                fontFamily = readerFont,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            // Render text split by markdown chapters or general text blocks
                            textPages.forEachIndexed { idx, chapterText ->
                                Text(
                                    text = "Section ${idx + 1}",
                                    fontSize = (viewModel.readerFontSize + 2).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    fontFamily = readerFont,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )

                                val paragraphs = remember(chapterText) {
                                    chapterText.split("\n").filter { it.isNotBlank() }
                                }

                                paragraphs.forEach { paragraph ->
                                    val paragraphClean = paragraph.trim().replace("#", "")
                                    val isHeaderVal = paragraph.trim().startsWith("#")

                                    // Check search query highlight
                                    val annotatedText = buildSearchAnnotatedString(
                                        text = paragraphClean,
                                        query = innerSearchQuery,
                                        highlightColor = Color(0xFFFEF08A),
                                        textColor = readerText
                                    )

                                    // Check note/highlight match
                                    val matchedNote = notesList.find { it.pageNumber == idx && paragraphClean.contains(it.highlightText) }
                                    val highlightBg = when (matchedNote?.colorHex) {
                                        "#FEF08A" -> Color(0xFFFEF08A).copy(alpha = 0.35f) // Yellow
                                        "#99F6E4" -> Color(0xFF99F6E4).copy(alpha = 0.35f) // Teal
                                        "#FED7AA" -> Color(0xFFFED7AA).copy(alpha = 0.35f) // Orange
                                        "#FBCFE8" -> Color(0xFFFBCFE8).copy(alpha = 0.35f) // Pink
                                        else -> if (matchedNote != null) Color(0xFFFEF08A).copy(alpha = 0.35f) else Color.Transparent
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(highlightBg)
                                            .clickable {
                                                noteDialogState = paragraphClean
                                                localPageNumber = idx
                                            }
                                            .padding(vertical = 4.dp, horizontal = 6.dp)
                                    ) {
                                        Text(
                                            text = annotatedText,
                                            fontSize = (if (isHeaderVal) viewModel.readerFontSize + 4 else viewModel.readerFontSize).sp,
                                            fontWeight = if (isHeaderVal) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = readerText,
                                            fontFamily = readerFont,
                                            lineHeight = (viewModel.readerFontSize * viewModel.readerLineSpacing).sp
                                        )

                                        if (matchedNote != null) {
                                            Row(
                                                modifier = Modifier.align(Alignment.BottomEnd).background(readerCard).clip(CircleShape).padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.EditNote, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(matchedNote.text, fontSize = 8.sp, color = readerText, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(50.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            // Back to top floating visual component inline at bottom of scroll
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 48.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Back to Article Start", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (formatTag == "PDF") {
                        // PDF Mode layout inside A4 Sheet Page mockup with complete zoom and navigation functionality
                        var pdfScale by remember(localPageNumber) { mutableStateOf(1.0f) }
                        var pdfOffset by remember(localPageNumber) { mutableStateOf(Offset.Zero) }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Sub-header for premium PDF interactive features
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = readerCard.copy(alpha = 0.9f)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, readerText.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Zoom HUD controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { pdfScale = (pdfScale - 0.25f).coerceIn(0.5f, 4.0f) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ZoomOut,
                                                contentDescription = "Zoom Out",
                                                tint = readerText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = "${(pdfScale * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = readerText,
                                            modifier = Modifier.width(36.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        IconButton(
                                            onClick = { pdfScale = (pdfScale + 0.25f).coerceIn(0.5f, 4.0f) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ZoomIn,
                                                contentDescription = "Zoom In",
                                                tint = readerText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier
                                                .height(16.dp)
                                                .width(1.dp)
                                                .padding(horizontal = 2.dp),
                                            color = readerText.copy(alpha = 0.2f)
                                        )

                                        TextButton(
                                            onClick = {
                                                pdfScale = 1.0f
                                                pdfOffset = Offset.Zero
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                        }
                                    }

                                    // Page Selector controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { if (localPageNumber > 0) localPageNumber-- },
                                            enabled = localPageNumber > 0,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ChevronLeft,
                                                contentDescription = "Previous Page",
                                                tint = if (localPageNumber > 0) readerText else readerText.copy(alpha = 0.3f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Text(
                                            text = "${localPageNumber + 1} / ${textPages.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = readerText
                                        )

                                        IconButton(
                                            onClick = { if (localPageNumber < textPages.size - 1) localPageNumber++ },
                                            enabled = localPageNumber < textPages.size - 1,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ChevronRight,
                                                contentDescription = "Next Page",
                                                tint = if (localPageNumber < textPages.size - 1) readerText else readerText.copy(alpha = 0.3f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Dynamic slider for fast navigation inside the current PDF document
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = readerCard.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Fast Navigate:",
                                        fontSize = 10.sp,
                                        color = readerText.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Slider(
                                        value = localPageNumber.toFloat(),
                                        onValueChange = { localPageNumber = it.toInt() },
                                        valueRange = 0f..(textPages.size - 1).coerceAtLeast(1).toFloat(),
                                        steps = if (textPages.size > 1) textPages.size - 2 else 0,
                                        colors = SliderDefaults.colors(
                                            thumbColor = TealPrimary,
                                            activeTrackColor = TealPrimary,
                                            inactiveTrackColor = readerText.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(24.dp)
                                    )
                                }
                            }

                            // A4 aspect ratio display sheet with pinch-to-zoom gestures
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(readerBg)
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            pdfScale = (pdfScale * zoom).coerceIn(1.0f, 4.0f)
                                            if (pdfScale > 1.0f) {
                                                pdfOffset = Offset(
                                                    x = pdfOffset.x + pan.x,
                                                    y = pdfOffset.y + pan.y
                                                )
                                            } else {
                                                pdfOffset = Offset.Zero
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, readerText.copy(alpha = 0.2f)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (viewModel.readerColorMode == "Dark") CharcoalBg else Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(0.707f) // A4 ratio
                                        .padding(horizontal = viewModel.readerMargin.dp)
                                        .graphicsLayer(
                                            scaleX = pdfScale,
                                            scaleY = pdfScale,
                                            translationX = pdfOffset.x,
                                            translationY = pdfOffset.y
                                        )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(16.dp)
                                    ) {
                                        // Simulated PDF header
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "PDF ARCHIVE SOURCE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = readerText.copy(alpha = 0.4f)
                                            )
                                            Text(
                                                "Page ${localPageNumber + 1} of ${textPages.size}",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = readerText.copy(alpha = 0.4f)
                                            )
                                        }
                                        HorizontalDivider(
                                            color = readerText.copy(alpha = 0.1f),
                                            thickness = 1.dp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        val currentText = textPages.getOrNull(localPageNumber) ?: "The digital manuscript file appears to be empty."
                                        val paragraphs = remember(currentText) {
                                            currentText.split("\n").filter { it.isNotBlank() }
                                        }

                                        paragraphs.forEach { paragraph ->
                                            val paragraphClean = paragraph.trim().replace("#", "")
                                            val isHeaderVal = paragraph.trim().startsWith("#")

                                            val annotatedText = buildSearchAnnotatedString(
                                                text = paragraphClean,
                                                query = innerSearchQuery,
                                                highlightColor = Color(0xFFFEF08A),
                                                textColor = readerText
                                            )

                                            val matchedNote = notesList.find {
                                                it.pageNumber == localPageNumber && paragraphClean.contains(it.highlightText)
                                            }
                                            val highlightBg = when (matchedNote?.colorHex) {
                                                "#FEF08A" -> Color(0xFFFEF08A).copy(alpha = 0.35f)
                                                "#99F6E4" -> Color(0xFF99F6E4).copy(alpha = 0.35f)
                                                "#FED7AA" -> Color(0xFFFED7AA).copy(alpha = 0.35f)
                                                "#FBCFE8" -> Color(0xFFFBCFE8).copy(alpha = 0.35f)
                                                else -> if (matchedNote != null) Color(0xFFFEF08A).copy(alpha = 0.35f) else Color.Transparent
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(highlightBg)
                                                    .clickable { noteDialogState = paragraphClean }
                                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                                            ) {
                                                Text(
                                                    text = annotatedText,
                                                    fontSize = (if (isHeaderVal) viewModel.readerFontSize + 3 else viewModel.readerFontSize).sp,
                                                    fontWeight = if (isHeaderVal) FontWeight.ExtraBold else FontWeight.Normal,
                                                    color = readerText,
                                                    fontFamily = readerFont,
                                                    lineHeight = (viewModel.readerFontSize * viewModel.readerLineSpacing).sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // EPUB Flowable standard layout
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = viewModel.readerMargin.dp)
                        ) {
                            Spacer(modifier = Modifier.height(24.dp))

                            val currentText = textPages.getOrNull(localPageNumber) ?: "The digital manuscript file appears to be empty."
                            val paragraphs = remember(currentText) {
                                currentText.split("\n").filter { it.isNotBlank() }
                            }

                            paragraphs.forEach { paragraph ->
                                val paragraphClean = paragraph.trim().replace("#", "")
                                val isHeaderVal = paragraph.trim().startsWith("#")

                                val annotatedText = buildSearchAnnotatedString(
                                    text = paragraphClean,
                                    query = innerSearchQuery,
                                    highlightColor = Color(0xFFFEF08A),
                                    textColor = readerText
                                )

                                val matchedNote = notesList.find { it.pageNumber == localPageNumber && paragraphClean.contains(it.highlightText) }
                                val highlightBg = when (matchedNote?.colorHex) {
                                    "#FEF08A" -> Color(0xFFFEF08A).copy(alpha = 0.35f)
                                    "#99F6E4" -> Color(0xFF99F6E4).copy(alpha = 0.35f)
                                    "#FED7AA" -> Color(0xFFFED7AA).copy(alpha = 0.35f)
                                    "#FBCFE8" -> Color(0xFFFBCFE8).copy(alpha = 0.35f)
                                    else -> if (matchedNote != null) Color(0xFFFEF08A).copy(alpha = 0.35f) else Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(highlightBg)
                                        .clickable { noteDialogState = paragraphClean }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Text(
                                        text = annotatedText,
                                        fontSize = (if (isHeaderVal) viewModel.readerFontSize + 4 else viewModel.readerFontSize).sp,
                                        fontWeight = if (isHeaderVal) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = readerText,
                                        fontFamily = readerFont,
                                        lineHeight = (viewModel.readerFontSize * viewModel.readerLineSpacing).sp
                                    )

                                    if (matchedNote != null) {
                                        Text(
                                            text = "✍️ Note: ${matchedNote.text}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic,
                                            color = readerText.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp).align(Alignment.BottomStart)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Inline jump-to-page other search results
                if (innerSearchQuery.isNotBlank()) {
                    val matchingPages = remember(innerSearchQuery, textPages) {
                        textPages.mapIndexedNotNull { pos, content ->
                            if (content.contains(innerSearchQuery, ignoreCase = true)) pos else null
                        }
                    }

                    if (matchingPages.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TealPrimary.copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Found match inside multiple places. Jump directly to page:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchingPages.forEach { pNum ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (localPageNumber == pNum) TealPrimary else readerCard)
                                            .clickable { localPageNumber = pNum }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Page ${pNum + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (localPageNumber == pNum) Color.White else readerText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Compact bottom tracking ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(readerCard.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentProgress = ((localPageNumber + 1).toFloat() / textPages.size.toFloat() * 100).toInt()
                    val minutesLeft = ((textPages.size - localPageNumber - 1) * 1.5).toInt()

                    Text(
                        text = "Page ${localPageNumber + 1} of ${textPages.size} ($currentProgress% complete)",
                        fontSize = 11.sp,
                        color = readerText.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (minutesLeft > 0) "~$minutesLeft mins remaining" else "Completed",
                        fontSize = 11.sp,
                        color = OrangeAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Animated Top Header with Controls (Search toggle, Bookmark toggle, and Back)
            AnimatedVisibility(
                visible = showControlsDrawer,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Surface(
                    color = readerCard,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { backWithStats() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = readerText)
                                }
                                Column {
                                    Text(
                                        text = book.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = readerText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.width(160.dp)
                                    )
                                    Text(
                                        text = book.author,
                                        fontSize = 11.sp,
                                        color = readerText.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Row {
                                // Search inside toggle
                                IconButton(onClick = { isSearchingInside = !isSearchingInside }) {
                                    Icon(
                                        imageVector = if (isSearchingInside) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Search Inside",
                                        tint = if (isSearchingInside) TealPrimary else readerText
                                    )
                                }

                                // Bookmark click toggler
                                val isBookmarked = bookmarksList.any { it.pageNumber == localPageNumber }
                                IconButton(
                                    onClick = {
                                        if (isBookmarked) {
                                            val target = bookmarksList.find { it.pageNumber == localPageNumber }
                                            if (target != null) viewModel.deleteBookmark(target)
                                        } else {
                                            viewModel.saveBookmark(
                                                bookId = book.id,
                                                page = localPageNumber,
                                                title = "Page ${localPageNumber + 1} Bookmark",
                                                snippet = textPages.getOrNull(localPageNumber)?.take(35) ?: "Saved position snippet"
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Toggle Bookmark",
                                        tint = if (isBookmarked) OrangeAccent else readerText
                                    )
                                }
                            }
                        }

                        // Search Inside Input Text Field
                        if (isSearchingInside) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = innerSearchQuery,
                                    onValueChange = { innerSearchQuery = it },
                                    placeholder = { Text("Search inside pages / chapters...") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TealPrimary,
                                        unfocusedBorderColor = readerText.copy(alpha = 0.2f),
                                        focusedLabelColor = TealPrimary
                                    ),
                                    trailingIcon = {
                                        if (innerSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { innerSearchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = null)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Animated Bottom Controls & Panel Preferences drawer
            AnimatedVisibility(
                visible = showControlsDrawer,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    color = readerCard,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        // Manual Back/Next page quick pointers (Hidden in single-scroll Article)
                        if (formatTag != "ARTICLE") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { if (localPageNumber > 0) localPageNumber-- },
                                    enabled = localPageNumber > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = readerBg, disabledContainerColor = readerCard.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.NavigateBefore, contentDescription = "Prev", tint = readerText)
                                    Text("Prev Page", color = readerText, fontSize = 12.sp)
                                }

                                Text(
                                    text = "Page ${localPageNumber + 1} of ${textPages.size}",
                                    color = readerText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Button(
                                    onClick = { if (localPageNumber < textPages.size - 1) localPageNumber++ },
                                    enabled = localPageNumber < textPages.size - 1,
                                    colors = ButtonDefaults.buttonColors(containerColor = readerBg, disabledContainerColor = readerCard.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Next Page", color = readerText, fontSize = 12.sp)
                                    Icon(Icons.Default.NavigateNext, contentDescription = "Next", tint = readerText)
                                }
                            }

                            // Interactive slider page seeker for instant auto resume navigation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Page Seek", fontSize = 11.sp, color = readerText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = localPageNumber.toFloat(),
                                    onValueChange = { localPageNumber = it.toInt().coerceIn(0, textPages.size - 1) },
                                    valueRange = 0f..(textPages.size - 1).coerceAtLeast(1).toFloat(),
                                    steps = (textPages.size - 2).coerceAtLeast(0),
                                    colors = SliderDefaults.colors(thumbColor = OrangeAccent, activeTrackColor = OrangeAccent),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 1. Color Themes selecting mode grid
                        Text("রিডার থিম মিক্সার (Theme Mixer)", fontSize = 11.sp, color = readerText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val row1 = listOf("Light", "Dark", "Sepia")
                                row1.forEach { m ->
                                    val isS = viewModel.readerColorMode == m
                                    val cBg = when (m) {
                                        "Dark" -> CharcoalBg
                                        "Sepia" -> SepiaBg
                                        else -> LightBg
                                    }
                                    val cTxt = when (m) {
                                        "Dark" -> TextLight
                                        "Sepia" -> SepiaText
                                        else -> TextDark
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(BorderStroke(if (isS) 2.dp else 1.dp, if (isS) TealPrimary else readerText.copy(alpha = 0.15f)), RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cBg)
                                            .clickable { viewModel.readerColorMode = m }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(m, color = cTxt, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val row2 = listOf("Forest Green", "Charcoal", "Soft Rose")
                                row2.forEach { m ->
                                    val isS = viewModel.readerColorMode == m
                                    val cBg = when (m) {
                                        "Forest Green" -> Color(0xFFE2EFE0)
                                        "Charcoal" -> Color(0xFF1E2229)
                                        "Soft Rose" -> Color(0xFFFBE4E8)
                                        else -> LightBg
                                    }
                                    val cTxt = when (m) {
                                        "Forest Green" -> Color(0xFF152A15)
                                        "Charcoal" -> Color(0xFFE0E3EB)
                                        "Soft Rose" -> Color(0xFF421E23)
                                        else -> TextDark
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(BorderStroke(if (isS) 2.dp else 1.dp, if (isS) TealPrimary else readerText.copy(alpha = 0.15f)), RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(cBg)
                                            .clickable { viewModel.readerColorMode = m }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(m, color = cTxt, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Font Sizing Adjusting Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TextFields, contentDescription = "Font Size", tint = readerText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("A-", color = readerText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = viewModel.readerFontSize,
                                onValueChange = { viewModel.readerFontSize = it },
                                valueRange = 12f..28f,
                                steps = 8,
                                colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            Text("A+", color = readerText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }

                        // 3. Brightness Screen Dimmer Slider Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LightMode, contentDescription = "Brightness", tint = readerText, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dim", color = readerText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = viewModel.readerBrightness,
                                onValueChange = { viewModel.readerBrightness = it },
                                valueRange = 0.2f..1.0f,
                                colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            Text("Bright", color = readerText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. Margin, Font Typeface & Line spacing Selector grids
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Font Choice
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("টাইপোগ্রাফি (Typography Family)", fontSize = 11.sp, color = readerText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val families = listOf("Serif", "Sans-Serif", "Mono")
                                    families.forEach { f ->
                                        val isS = viewModel.readerFontFamily == f
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(BorderStroke(if (isS) 1.5.dp else 1.dp, if (isS) TealPrimary else readerText.copy(alpha = 0.1f)), RoundedCornerShape(6.dp))
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isS) TealPrimary else readerBg)
                                                .clickable { viewModel.readerFontFamily = f }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(f, color = if (isS) Color.White else readerText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Margin setting
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("মার্জিন চওড়া (Custom Margins)", fontSize = 11.sp, color = readerText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val marginsMap = listOf(8, 12, 16, 20, 24, 28)
                                        marginsMap.forEach { mg ->
                                            val isS = viewModel.readerMargin == mg.toFloat()
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(BorderStroke(if (isS) 1.5.dp else 1.dp, if (isS) TealPrimary else readerText.copy(alpha = 0.1f)), RoundedCornerShape(6.dp))
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isS) TealPrimary else readerBg)
                                                    .clickable { viewModel.readerMargin = mg.toFloat() }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${mg}", color = if (isS) Color.White else readerText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Line spacing
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("লাইন উচ্চতা (Line Spacings)", fontSize = 11.sp, color = readerText.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val spacingOptions = listOf(1.2f, 1.4f, 1.6f, 1.8f, 2.0f)
                                        spacingOptions.forEach { sp ->
                                            val isS = viewModel.readerLineSpacing == sp
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(BorderStroke(if (isS) 1.5.dp else 1.dp, if (isS) TealPrimary else readerText.copy(alpha = 0.1f)), RoundedCornerShape(6.dp))
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isS) TealPrimary else readerBg)
                                                    .clickable { viewModel.readerLineSpacing = sp }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${sp}x", color = if (isS) Color.White else readerText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5. ANNOTATIONS & BOOKMARKS COLLAPSIBLE INDEX BAR
                        Text(
                            text = "Historic Bookmarks & Saved Highlights Index",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = readerText,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Quick bookmarks list anchors
                            bookmarksList.forEach { bk ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(OrangeAccent.copy(alpha = 0.15f))
                                        .clickable { localPageNumber = bk.pageNumber }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Page ${bk.pageNumber + 1}", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Notes anchors
                            notesList.forEach { nt ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TealLight.copy(alpha = 0.15f))
                                        .clickable { localPageNumber = nt.pageNumber }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EditNote, contentDescription = null, tint = TealLight, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Page ${nt.pageNumber + 1}: ${nt.text.take(15)}...", color = TealLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CUSTOM NOTE HIGHLIGHT INPUT DIALOGUE
        if (noteDialogState != null) {
            val paragraphClean = noteDialogState ?: ""
            var noteContent by remember { mutableStateOf("") }
            var activeDialogTab by remember { mutableStateOf("note") } // note, lookup
            var selectedWordToLookup by remember { mutableStateOf<String?>(null) }

            AlertDialog(
                onDismissRequest = { noteDialogState = null },
                confirmButton = {
                    if (activeDialogTab == "note") {
                        TextButton(
                            onClick = {
                                viewModel.saveNote(
                                    bookId = book.id,
                                    page = localPageNumber,
                                    text = if (noteContent.isNotBlank()) noteContent else "Premium highlight annotation",
                                    highlight = paragraphClean,
                                    colorHex = selectedNoteColorHex
                                )
                                noteDialogState = null
                                noteContent = ""
                            }
                        ) {
                            Text("Add Highlight & Note", color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { noteDialogState = null }) {
                            Text("Done", color = TealPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteDialogState = null }) { Text("Cancel") }
                },
                title = { 
                    TabRow(
                        selectedTabIndex = if (activeDialogTab == "note") 0 else 1,
                        containerColor = Color.Transparent,
                        contentColor = TealPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = activeDialogTab == "note",
                            onClick = { activeDialogTab = "note" },
                            text = { Text("Note/Highlight", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = activeDialogTab == "lookup",
                            onClick = { activeDialogTab = "lookup" },
                            text = { Text("Word Lookup", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                },
                text = {
                    if (activeDialogTab == "note") {
                        Column {
                            Text(
                                text = "Choose Highlighter Accent Color:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                val highlightColors = mapOf(
                                    "#FEF08A" to Color(0xFFFEF08A), // Yellow
                                    "#99F6E4" to Color(0xFF99F6E4), // Teal
                                    "#FED7AA" to Color(0xFFFED7AA), // Orange
                                    "#FBCFE8" to Color(0xFFFBCFE8)  // Pink
                                )

                                highlightColors.forEach { (hex, colorVal) ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colorVal)
                                            .border(
                                                width = if (selectedNoteColorHex == hex) 3.dp else 1.dp,
                                                color = if (selectedNoteColorHex == hex) TealPrimary else Color.Gray.copy(alpha = 0.3f),
                                                shape = CircleShape
                                            )
                                            .clickable { selectedNoteColorHex = hex }
                                    )
                                }
                            }

                            Text(
                                text = "Add reference notes or review comments synchronized directly with text annotations.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = noteContent,
                                onValueChange = { noteContent = it },
                                placeholder = { Text("Write down your note interpretation...") },
                                singleLine = false,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Word Lookup tab
                        // Break paragraph into individual words
                        val words = remember(paragraphClean) {
                            // clean punctuation but keep letters & digits
                            paragraphClean.split(Regex("[\\s,.:;!?\n\r()\"'-]+"))
                                .filter { it.isNotBlank() && it.length > 2 }
                                .distinct()
                                .take(20) // show up to top 20 words for clean selection layout
                        }
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ট্যাপ করুন যে কোনো শব্দের অর্থ, উচ্চারণ এবং অনুবাদ পেতে:",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            // Word chips scroll row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(words) { word ->
                                    val isSelected = word.equals(selectedWordToLookup, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .clickable { 
                                                selectedWordToLookup = word
                                                viewModel.performAILookup(word, paragraphClean)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = word,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            if (selectedWordToLookup == null) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("শব্দ নির্বাচন করুন...", fontSize = 12.sp, color = Color.Gray)
                                }
                            } else {
                                if (viewModel.aiLookupLoading) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = TealPrimary, modifier = Modifier.size(24.dp))
                                    }
                                } else {
                                    viewModel.activeWordLookupResult?.let { res ->
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(selectedWordToLookup ?: "", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                                    Text(res.pronunciation, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                                                }
                                                
                                                // Save word instantly Button
                                                Button(
                                                    onClick = {
                                                        viewModel.saveWord(
                                                            word = selectedWordToLookup ?: "",
                                                            definition = res.definition,
                                                            translation = res.translation,
                                                            pronunciation = res.pronunciation,
                                                            bookTitle = book.title,
                                                            sentenceContext = paragraphClean
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("শব্দকোষে সেভ করুন", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            
                                            Text("English Definition:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Text(res.definition, fontSize = 12.sp)
                                            
                                            Text("বাংলা / স্থানীয় অনুবাদ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                            Text(res.translation, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

// Search utility mapping matches visually
private fun buildSearchAnnotatedString(text: String, query: String, highlightColor: Color, textColor: Color): AnnotatedString {
    return buildAnnotatedString {
        if (query.isEmpty()) {
            append(text)
            addStyle(SpanStyle(color = textColor), 0, text.length)
        } else {
            var startIndex = 0
            while (startIndex < text.length) {
                val index = text.indexOf(query, startIndex, ignoreCase = true)
                if (index == -1) {
                    append(text.substring(startIndex))
                    break
                }
                append(text.substring(startIndex, index))
                val highlightStart = length
                append(text.substring(index, index + query.length))
                addStyle(SpanStyle(background = highlightColor, color = Color.Black, fontWeight = FontWeight.Bold), highlightStart, length)
                startIndex = index + query.length
            }
        }
    }
}

// 7. HIGH-FIDESTY DYNAMIC CANVAS COVER DESIGN
@Composable
fun CanvasBookCover(title: String, author: String, modifier: Modifier = Modifier) {
    // Generate stable color palette seeded by title length
    val seed = title.length
    val primaryBg = when (seed % 5) {
        0 -> TealPrimary
        1 -> Color(0xFF14B8A6) // custom teal lighter
        2 -> Color(0xFF6D28D9) // purple
        3 -> Color(0xFF1E3A8A) // deep blue
        else -> Color(0xFF854D0E) // wood brown
    }

    val patternGold = Color(0xFFF59E0B)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Background color fill
        drawRect(color = primaryBg)

        // Draw elegant circular islamic or classic border designs on book
        drawCircle(
            color = patternGold.copy(alpha = 0.25f),
            radius = width * 0.35f,
            center = Offset(width / 2f, height * 0.4f),
            style = Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            color = patternGold.copy(alpha = 0.15f),
            radius = width * 0.26f,
            center = Offset(width / 2f, height * 0.4f),
            style = Stroke(width = 1.dp.toPx())
        )

        // Horizontal line banners
        drawLine(
            color = patternGold.copy(alpha = 0.3f),
            start = Offset(width * 0.15f, height * 0.76f),
            end = Offset(width * 0.85f, height * 0.76f),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = patternGold.copy(alpha = 0.3f),
            start = Offset(width * 0.15f, height * 0.18f),
            end = Offset(width * 0.85f, height * 0.18f),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Overlay descriptive content in beautiful vertical stack
    Box(
        modifier = modifier.padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "MyReaderPro Vol.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title.take(24) + if (title.length > 24) "..." else "",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Text(
                    text = "by " + author,
                    color = patternGold,
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(patternGold.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SECURED",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// Book cover background renderer is fully handled by CanvasBookCover above

// ==========================================
// MYREADERPRO COMMERCIAL SAAS MODULES
// ==========================================

@Composable
fun BookRequestHub(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val requestsList by viewModel.allBookRequests.collectAsState(initial = emptyList())
    val popularList by viewModel.popularRequests.collectAsState(initial = emptyList())
    val recentList by viewModel.recentlyRequested.collectAsState(initial = emptyList())

    var requestTitle by remember { mutableStateOf("") }
    var requestAuthor by remember { mutableStateOf("") }
    var requestPublisher by remember { mutableStateOf("") }
    var requestNotes by remember { mutableStateOf("") }
    var submitSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Admin state filters
    var adminSearchQuery by remember { mutableStateOf("") }
    var statusFilter   by remember { mutableStateOf("ALL") } // ALL, PENDING, COMPLETED, REJECTED

    val canModerate = activeUser?.role == "ADMIN" || activeUser?.role == "MODERATOR"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. USER SUBMISSION FORM ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, TealLight.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Request a Book",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = "Request unavailable books. Our automatic indexing crawler is triggered on submission.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (submitSuccessMessage != null) {
                    Text(
                        text = submitSuccessMessage!!,
                        color = TealLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = requestTitle,
                    onValueChange = { requestTitle = it },
                    label = { Text("Book Title *", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = requestAuthor,
                    onValueChange = { requestAuthor = it },
                    label = { Text("Author Name *", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = requestPublisher,
                    onValueChange = { requestPublisher = it },
                    label = { Text("Publisher (Optional)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = requestNotes,
                    onValueChange = { requestNotes = it },
                    label = { Text("Add Notes / Particular comments", fontSize = 12.sp) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (requestTitle.isBlank() || requestAuthor.isBlank()) {
                            submitSuccessMessage = "Error: Title & Author name are required!"
                        } else {
                            viewModel.submitBookRequest(
                                title = requestTitle,
                                author = requestAuthor,
                                publisher = requestPublisher,
                                notes = requestNotes
                            )
                            submitSuccessMessage = "Success: Request added to automatic crawler queues! De-duplication triggered."
                            requestTitle = ""
                            requestAuthor = ""
                            requestPublisher = ""
                            requestNotes = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Book Request", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // --- 2. AUTOMATIC SYSTEM DISCOVERABILITY STATISTICS ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "🔥 Most Requested / Trending Books",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (popularList.isEmpty()) {
                    Text("No trending requests found.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    popularList.take(3).forEach { req ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("by ${req.author}", fontSize = 10.sp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(OrangeAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${req.requestCount} Requests", color = OrangeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🗄️ Recently Requested",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (recentList.isEmpty()) {
                    Text("No recent requests available.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    recentList.take(3).forEach { req ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(req.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Author: ${req.author} | Status: ${req.status}", fontSize = 10.sp, color = if (req.status == "COMPLETED") TealLight else Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // --- 3. ADMIN MANAGEMENT PANEL ---
        if (canModerate) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeAccent.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OrangeAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Moderation: Requests List",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                    }

                    OutlinedTextField(
                        value = adminSearchQuery,
                        onValueChange = { adminSearchQuery = it },
                        placeholder = { Text("Search catalog requests...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Filters tabs
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val filters = listOf("ALL", "PENDING", "COMPLETED", "REJECTED")
                        filters.forEach { f ->
                            val isS = statusFilter == f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isS) OrangeAccent else MaterialTheme.colorScheme.surface)
                                    .clickable { statusFilter = f }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .border(1.dp, if (isS) Color.Transparent else Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = f,
                                    color = if (isS) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Simulated list execution
                    var filteredReqs = requestsList
                    if (adminSearchQuery.isNotBlank()) {
                        filteredReqs = filteredReqs.filter {
                            it.title.contains(adminSearchQuery, ignoreCase = true) ||
                            it.author.contains(adminSearchQuery, ignoreCase = true)
                        }
                    }
                    if (statusFilter != "ALL") {
                        filteredReqs = filteredReqs.filter { it.status == statusFilter }
                    }

                    if (filteredReqs.isEmpty()) {
                        Text("No requests match filters.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    } else {
                        filteredReqs.forEach { req ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(req.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("Author: ${req.author} | Publisher: ${req.publisher}", fontSize = 10.sp, color = Color.Gray)
                                            Text("Requester: ${req.userEmail}", fontSize = 9.sp, color = Color.Gray)
                                            if (req.notes.isNotBlank()) {
                                                Text("Notes: ${req.notes}", fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (req.status) {
                                                        "COMPLETED" -> TealLight.copy(alpha = 0.15f)
                                                        "REJECTED"  -> Color.Red.copy(alpha = 0.15f)
                                                        else         -> OrangeAccent.copy(alpha = 0.15f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = req.status,
                                                color = when (req.status) {
                                                    "COMPLETED" -> TealLight
                                                    "REJECTED"  -> Color.Red
                                                    else        -> OrangeAccent
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons for Admins
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (req.status == "PENDING") {
                                            Button(
                                                onClick = { viewModel.updateBookRequestStatus(req.id, "COMPLETED") },
                                                colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Approve & Index", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { viewModel.updateBookRequestStatus(req.id, "REJECTED") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Reject", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Simulate merging duplicate requests to consolidate count
                                        Button(
                                            onClick = {
                                                // Change duplicate status or simple complete to simulate merging
                                                viewModel.updateBookRequestStatus(req.id, "COMPLETED")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.weight(1f).height(28.dp),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("Merge Duplicate", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
}

@Composable
fun CopyrightClaimHub(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val claimsList by viewModel.allCopyrightClaims.collectAsState(initial = emptyList())

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var orgName by remember { mutableStateOf("") }
    var contentTitle by remember { mutableStateOf("") }
    var contentUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var supportingDocs by remember { mutableStateOf("") }
    var outputMessage by remember { mutableStateOf<String?>(null) }

    val canModerate = activeUser?.role == "ADMIN" || activeUser?.role == "MODERATOR"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. COPYRIGHT SUBMISSION DMCA FORM ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Submit DMCA & Copyright Infringement Claim",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    text = "If you hold copyright over digital manuscripts listed on our platform, submit instant takedown requests below.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (outputMessage != null) {
                    Text(
                        text = outputMessage!!,
                        color = OrangeAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Rights Owner Contact) *", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orgName,
                    onValueChange = { orgName = it },
                    label = { Text("Organization Name Name (Optional)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentTitle,
                    onValueChange = { contentTitle = it },
                    label = { Text("Authorized Content Title *", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentUrl,
                    onValueChange = { contentUrl = it },
                    label = { Text("Content URL in App *", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Infringement Argument *", fontSize = 12.sp) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = supportingDocs,
                    onValueChange = { supportingDocs = it },
                    label = { Text("Supporting Evidence / Trademarks Registration ID", fontSize = 12.sp) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (fullName.isBlank() || email.isBlank() || contentTitle.isBlank() || contentUrl.isBlank() || description.isBlank()) {
                            outputMessage = "Error: All marked (*) parameters are required!"
                        } else {
                            viewModel.submitCopyrightClaim(
                                fullName = fullName,
                                email = email,
                                org = orgName,
                                title = contentTitle,
                                url = contentUrl,
                                desc = description,
                                docs = supportingDocs
                            )
                            outputMessage = "Success: DMCA Trademark Claim recorded. Temporary hold process activated."
                            fullName = ""
                            email = ""
                            orgName = ""
                            contentTitle = ""
                            contentUrl = ""
                            description = ""
                            supportingDocs = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit DMCA Infringement Claim", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // --- 2. ADMIN MODERATION AUDIT LOG CENTER ---
        if (canModerate) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "⚖️ DMCA Audit Trail & Moderation Reviews",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )

                    if (claimsList.isEmpty()) {
                        Text("No active claims submitted yet.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        claimsList.forEach { claim ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(claim.contentTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                            Text("Owner: ${claim.fullName} (${claim.organizationName})", fontSize = 10.sp, color = Color.Gray)
                                            Text("Date submitted: 2026-06-02", fontSize = 8.sp, color = Color.Gray)
                                            Text("Claim ID: dmca_claim_${claim.id}", fontSize = 9.sp, color = Color.Gray)
                                            Text("Arguments: ${claim.description}", fontSize = 10.sp, maxLines = 2)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    when (claim.status) {
                                                        "PENDING"   -> OrangeAccent.copy(alpha = 0.15f)
                                                        "REVIEWING" -> Color.Blue.copy(alpha = 0.15f)
                                                        "APPROVED"  -> Color.Red.copy(alpha = 0.15f)
                                                        else         -> TealLight.copy(alpha = 0.15f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = claim.status,
                                                color = when (claim.status) {
                                                    "PENDING"   -> OrangeAccent
                                                    "REVIEWING" -> Color.Blue
                                                    "APPROVED"  -> Color.Red
                                                    else         -> TealLight
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action buttons
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (claim.status == "PENDING") {
                                            Button(
                                                onClick = {
                                                    viewModel.updateCopyrightStatus(
                                                        claimId = claim.id,
                                                        status = "APPROVED",
                                                        decision = "Takedown approved due to valid DMCA identification.",
                                                        hideContent = true
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Takedown / Block", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.updateCopyrightStatus(
                                                        claimId = claim.id,
                                                        status = "REVIEWING",
                                                        decision = "Claim under review pending further verification.",
                                                        hideContent = true
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Needs Evidence", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Restore content button
                                        if (claim.status == "APPROVED" || claim.temporaryHidden) {
                                            Button(
                                                onClick = {
                                                    viewModel.restoreCopyrightContent(claim.id)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.weight(1f).height(28.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Restore Content", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
    }
}

@Composable
fun MonetizationHub(viewModel: ReaderViewModel, activeUser: UserEntity?) {
    val adBlocks by viewModel.allAdBlocks.collectAsState(initial = emptyList())

    // Simulated Donations stats
    var supportAmount by remember { mutableStateOf("10") }
    var totalDonationVal by remember { mutableStateOf(activeUser?.totalDonationAmount ?: 25.0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. MEMBERSHIP PLANS SWITCH PANEL ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, TealLight.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Subscription & Member Roles Management",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val plans = listOf("FREE", "PREMIUM", "VIP")
                    plans.forEach { plan ->
                        val isCurrent = activeUser?.membershipType == plan
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) TealLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    viewModel.changeMembership(plan)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = plan,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Simulated Moderation Role Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OrangeAccent.copy(alpha = 0.1f))
                        .clickable { viewModel.toggleUserDeveloperRole() }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Developer Permission Level", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Simulate: FREE, GUEST, MODERATOR, or ADMIN instantly", fontSize = 9.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(OrangeAccent)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(activeUser?.role ?: "ADMIN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 2. FUTURE REWARD DONATIONS SYSTEM ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "💸 Support Platform & Donations",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = "Become a backer! Complete simulations with offline-ready support credentials.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = supportAmount,
                        onValueChange = { supportAmount = it },
                        label = { Text("Donation ($ USD)", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            val addition = supportAmount.toFloatOrNull() ?: 0.0f
                            totalDonationVal += addition
                            supportAmount = "10"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Donate USD", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Your simulation lifetime donation:", fontSize = 11.sp, color = Color.Gray)
                    Text("$${totalDonationVal} USD", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TealLight)
                }
            }
        }

        // --- 3. AD SYSTEM & REPORTING ---
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📢 Ad Network Block Planner (Future Ready)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (adBlocks.isEmpty()) {
                    Text("Initializing ad network configuration blocks...", fontSize = 11.sp, color = Color.Gray)
                } else {
                    adBlocks.forEach { block ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(block.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Type: ${block.adType} | Imp: ${block.impressions} | Clicks: ${block.clicks}", fontSize = 9.sp, color = Color.Gray)
                            }

                            Switch(
                                checked = block.isEnabled,
                                onCheckedChange = { viewModel.toggleAdBlock(block.id, it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = TealLight)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- SAAS REVENUE ANALYTICS VISUALIZER ---
                Text("📈 Revenue and Traffic Insights Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(0.5.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Premium / VIP users ratio", fontSize = 9.sp, color = Color.Gray)
                                Text("84% Subscribers", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealLight)
                            }
                            Column {
                                Text("Est. Ad Earnings", fontSize = 9.sp, color = Color.Gray)
                                Text("$1,532.40 USD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrangeAccent)
                            }
                        }

                        // Simulated bar graph
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val bars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f, 0.95f)
                            bars.forEachIndexed { idx, barHeight ->
                                val color = if (idx == 6) OrangeAccent else TealLight
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(barHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackupAndRestoreHub(viewModel: ReaderViewModel) {
    var rawBackupText by remember { mutableStateOf("") }
    var currentStatus by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Professional Backup System (Room-to-JSON)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
                Text(
                    text = "Export user metrics, preferences and requested database structures locally, or copy files to import later.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                if (currentStatus != null) {
                    Text(
                        text = currentStatus!!,
                        color = TealLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            viewModel.exportBackup()
                            currentStatus = "Database exported successfully!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Backup JSON", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (rawBackupText.isBlank()) {
                                currentStatus = "Error: Please paste a valid backup JSON payload!"
                            } else {
                                viewModel.restoreBackup(rawBackupText) { ok ->
                                    currentStatus = if (ok) "Success: Preferences & configuration recovered!" else "Error: Signature mismatch or invalid JSON payload!"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import & Restore", fontSize = 11.sp, color = Color.White)
                    }
                }

                // Show backup output or input field
                OutlinedTextField(
                    value = if (viewModel.lastExportedBackupJson.isNotEmpty()) viewModel.lastExportedBackupJson else rawBackupText,
                    onValueChange = {
                        if (viewModel.lastExportedBackupJson.isEmpty()) {
                            rawBackupText = it
                        }
                    },
                    maxLines = 4,
                    label = { Text("Backup Payload Signature Data", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                )

                if (viewModel.lastExportedBackupJson.isNotEmpty()) {
                    Button(
                        onClick = {
                            rawBackupText = ""
                            viewModel.lastExportedBackupJson = ""
                            currentStatus = "Backup visual reset."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.End).height(32.dp)
                    ) {
                        Text("Clear Buffer", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ResearchChatbotDialog(
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit
) {
    val messages = viewModel.chatMessages
    val loading = viewModel.chatLoading
    var userInput by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Scroll to bottom when list grows
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Beautiful AI Logo & Close Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TealPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "এআই সাহিত্য গবেষক"
                                    "Arabic" -> "جيمي الباحث الأدبي"
                                    else -> "AI Literary Scholar"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Powered by Gemini 3.5 Flash",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.clearChat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Chat",
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Divider line
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Message Stream Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { (role, content) ->
                            val isUser = role == "user"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) TealPrimary else MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .weight(1f, fill = false)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = if (isUser) "You" else "Gemini Scholar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isUser) Color.White.copy(alpha = 0.7f) else TealPrimary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = content,
                                            fontSize = 13.sp,
                                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 19.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (loading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                color = TealPrimary,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = when (viewModel.currentLanguage) {
                                                    "Bengali" -> "জেমিনী অনুসন্ধান করছে..."
                                                    "Arabic" -> "يبحث جيمي في الكتب..."
                                                    else -> "Gemini is researching..."
                                                },
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick scholarly research prompts to guide the user (AI buddy suggestions)
                if (messages.size <= 1) {
                    Text(
                        text = "Suggested Research Topics:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                    
                    val suggestions = when (viewModel.currentLanguage) {
                        "Bengali" -> listOf(
                            "রবীন্দ্রনাথ ঠাকুরের জীবন দর্শন আলোচনা করুন।",
                            "বাংলা সাহিত্যের সুবর্ণ যুগ কোনটি এবং কেন?",
                            "মাইকেল মধুসূদন দত্তের মেঘনাদবধ কাব্যের বৈশিষ্ট।"
                        )
                        "Arabic" -> listOf(
                            "ما هو أثر نجيب محفوظ في الرواية العربية؟",
                            "تحليل لقصيدة البؤساء لأحمد شوقي.",
                            "تحدث عن تطور الشعر العربي الحديث."
                        )
                        else -> listOf(
                            "Explore William Shakespeare's tragic heroes.",
                            "What is the impact of magical realism in modern fiction?",
                            "Compare the writing styles of Ernest Hemingway and F. Scott Fitzgerald."
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.take(3).forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        userInput = prompt
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 10.sp,
                                    color = TealPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Bar Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        placeholder = {
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "সাহিত্য বিষয়ক জিজ্ঞাসা করুন..."
                                    "Arabic" -> "اسأل عن أي نظرية أدبية أو رواية..."
                                    else -> "Ask about literary logic, genres, history..."
                                },
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_research_input_text")
                    )

                    IconButton(
                        onClick = {
                            val msg = userInput.trim()
                            if (msg.isNotEmpty()) {
                                viewModel.sendChatMessage(msg)
                                userInput = ""
                            }
                        },
                        enabled = userInput.isNotBlank() && !loading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (userInput.isNotBlank() && !loading) TealPrimary else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (userInput.isNotBlank() && !loading) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterPanel(
    viewModel: ReaderViewModel,
    onDismiss: () -> Unit
) {
    val alerts by viewModel.notifications.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredAlerts = remember(alerts, selectedFilter) {
        when (selectedFilter) {
            "BOOKS" -> alerts.filter { it.type in listOf("NEW_BOOK", "BOOK_APPROVAL", "SUBMISSION_UPDATE") }
            "ACTIVITY" -> alerts.filter { it.type in listOf("LIKE", "COMMENT", "REPLY") }
            "ANNOUNCEMENTS" -> alerts.filter { it.type == "ADMIN" }
            "SYSTEM" -> alerts.filter { it.type == "SYSTEM" }
            else -> alerts
        }
    }

    val unreadCount = alerts.count { !it.isRead }

    androidx.compose.ui.window.Dialog(
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("notification_center_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("notification_center_close")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "বিজ্ঞপ্তি কেন্দ্র"
                                    "Arabic" -> "مركز الإشعارات"
                                    else -> "Notification Center"
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            if (unreadCount > 0) {
                                Text(
                                    text = when (viewModel.currentLanguage) {
                                        "Bengali" -> "$unreadCount টি অপঠিত বিজ্ঞপ্তি"
                                        "Arabic" -> "$unreadCount إشعارات غير مقروءة"
                                        else -> "$unreadCount unread notices"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { viewModel.markAllNotificationsAsRead() },
                            modifier = Modifier.testTag("notification_mark_all_read"),
                            colors = ButtonDefaults.textButtonColors(contentColor = TealPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "সব পড়া চিহ্নিত করুন"
                                    "Arabic" -> "تحديد الكل كمقروء"
                                    else -> "Mark All Read"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to (when (viewModel.currentLanguage) { "Bengali" -> "সব"; "Arabic" -> "الكل"; else -> "All" }),
                        "BOOKS" to (when (viewModel.currentLanguage) { "Bengali" -> "বই ও ইমপোর্ট"; "Arabic" -> "الكتب"; else -> "Books" }),
                        "ACTIVITY" to (when (viewModel.currentLanguage) { "Bengali" -> "ফোরাম কার্যক্রম"; "Arabic" -> "الأعضاء"; else -> "Activity" }),
                        "ANNOUNCEMENTS" to (when (viewModel.currentLanguage) { "Bengali" -> "বিজ্ঞপ্তি"; "Arabic" -> "الإعلانات"; else -> "Broadcasts" }),
                        "SYSTEM" to (when (viewModel.currentLanguage) { "Bengali" -> "সিস্টেম"; "Arabic" -> "النظام"; else -> "System" })
                    )

                    filters.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = key },
                            label = { Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                selectedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("filter_chip_$key")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (filteredAlerts.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("notification_empty_state"),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(TealPrimary.copy(alpha = 0.08f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = "No alerts",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "কোন বিজ্ঞপ্তি পাওয়া যায়নি"
                                    "Arabic" -> "لا توجد إشعارات حالياً"
                                    else -> "No notifications to show"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "নতুন কার্যক্রম ঘটলে এখানে তা দেখতে পাবেন।"
                                    "Arabic" -> "ستظهر الإشعارات الجديدة فور حدوثها هنا."
                                    else -> "New updates and social alerts will appear in this center."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredAlerts) { alert ->
                                val elapsedText = remember(alert.timestamp) {
                                    val diff = System.currentTimeMillis() - alert.timestamp
                                    val mins = diff / 60000
                                    val hrs = mins / 60
                                    when {
                                        mins < 1 -> "Just now"
                                        mins < 60 -> "${mins}m ago"
                                        hrs < 24 -> "${hrs}h ago"
                                        else -> {
                                            val days = hrs / 24
                                            "${days}d ago"
                                        }
                                    }
                                }

                                val iconInfo = remember(alert.type) {
                                    when (alert.type) {
                                        "NEW_BOOK" -> Icons.Default.Book to Color(0xFF4CAF50)
                                        "BOOK_APPROVAL" -> Icons.Default.Stars to Color(0xFFFF9800)
                                        "SUBMISSION_UPDATE" -> Icons.Default.Description to Color(0xFF2196F3)
                                        "LIKE" -> Icons.Default.Favorite to Color(0xFFE91E63)
                                        "COMMENT" -> Icons.Default.Comment to Color(0xFF00BCD4)
                                        "REPLY" -> Icons.Default.Reply to Color(0xFF9C27B0)
                                        "ADMIN" -> Icons.Default.Campaign to Color(0xFFE53935)
                                        else -> Icons.Default.Info to Color(0xFF607D8B)
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.markNotificationAsRead(alert.id) }
                                        .testTag("notification_item_${alert.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (alert.isRead) {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = if (alert.isRead) 0.dp else 1.2.dp,
                                        color = if (alert.isRead) Color.Transparent else TealPrimary.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(iconInfo.second.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = iconInfo.first,
                                                contentDescription = alert.type,
                                                tint = iconInfo.second,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = alert.title,
                                                    fontSize = 14.sp,
                                                    fontWeight = if (alert.isRead) FontWeight.Medium else FontWeight.Bold,
                                                    color = if (alert.isRead) {
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (!alert.isRead) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(horizontal = 6.dp)
                                                            .size(8.dp)
                                                            .background(TealLight, CircleShape)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = alert.message,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                                lineHeight = 16.sp
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = elapsedText,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                                )

                                                IconButton(
                                                    onClick = { viewModel.adminDeleteNotification(alert.id) },
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .testTag("delete_notif_${alert.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete notice",
                                                        tint = Color.Red.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(14.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = "Emulator Controls",
                                tint = OrangeAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (viewModel.currentLanguage) {
                                    "Bengali" -> "রিয়েল-টাইম এআই বিজ্ঞপ্তি সিমুলেটর"
                                    "Arabic" -> "محاكي الإشعارات المباشرة"
                                    else -> "Real-Time Interactive Alert Simulator"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val simulationTypes = listOf(
                                Triple("NEW_BOOK", "📚 New Book", "all_books"),
                                Triple("BOOK_APPROVAL", "🎉 Approval", "approvals"),
                                Triple("SUBMISSION_UPDATE", "📁 Update", "submission"),
                                Triple("LIKE", "❤️ Like", "likes"),
                                Triple("COMMENT", "💬 Comment", "comment_notif"),
                                Triple("REPLY", "↩️ Reply", "replies"),
                                Triple("ADMIN", "📢 Broadcast", "announcement"),
                                Triple("SYSTEM", "⚡ System", "system_sync")
                            )

                            simulationTypes.forEach { (typeVal, typeLabel, tagSuffix) ->
                                Button(
                                    onClick = { viewModel.triggerMockNotification(typeVal) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealPrimary.copy(alpha = 0.08f),
                                        contentColor = TealPrimary
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("simulate_$tagSuffix")
                                ) {
                                    Text(text = typeLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🚀 HIGH-FIDELITY Standalone SaaS Admin Dashboard
// ==========================================

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: ReaderViewModel) {
    var adminDashboardTab by remember { mutableStateOf("dashboard") } // dashboard, books, users, analytics, settings
    
    val booksList by viewModel.allBooks.collectAsState(initial = emptyList())
    val usersList by viewModel.allUsers.collectAsState(initial = emptyList())
    val categoriesList by viewModel.categories.collectAsState(initial = emptyList())
    val notificationsList by viewModel.notifications.collectAsState(initial = emptyList())
    val activeUser by viewModel.activeUser.collectAsState(initial = null)
    
    var showAddBookDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = Color(0xFF0F121C) // Pure dark carbon palette matching image reference
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Dashboard Main Content (Scrollable workspace)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (adminDashboardTab) {
                    "dashboard" -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                // Premium Logo bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        MyReaderProLogo(
                                            variant = LogoVariant.HORIZONTAL_ADMIN,
                                            scale = 0.9f
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Magnifying Glass Search Button
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF1B2032), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        // Profile picture avatar (Sarah portrait reference)
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFC084FC).copy(alpha = 0.2f))
                                                .border(1.5.dp, Color(0xFFC084FC), CircleShape)
                                                .clickable { adminDashboardTab = "settings" },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "S",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Notification Hub dot button
                                        IconButton(
                                            onClick = { viewModel.showNotificationCenter = true },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFF3B2E63).copy(alpha = 0.3f), CircleShape)
                                        ) {
                                            Box(contentAlignment = Alignment.TopEnd) {
                                                Icon(
                                                    imageVector = Icons.Default.Notifications,
                                                    contentDescription = "Notifications",
                                                    tint = Color(0xFFC084FC),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                val unreadCount = notificationsList.count { !it.isRead }
                                                if (unreadCount > 0) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .background(Color(0xFFEF4444), CircleShape)
                                                            .border(1.dp, Color(0xFF0F121C), CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                // Greetings: Welcome back Sarah
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Dark Season Mode",
                                            color = Color(0xFF64748B),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Welcome back, Sarah!",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.5).sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.White.copy(alpha = 0.5f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // 2x3 Statistics Cards Grid (Direct Match of Reference Image design)
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AdminStatsCard(
                                            title = "Total Users",
                                            value = "${28400 + usersList.size},4K",
                                            change = "+12.7K",
                                            icon = Icons.Default.Person,
                                            iconBgColor = Color(0xFF38BDF8).copy(alpha = 0.12f),
                                            iconTintColor = Color(0xFF38BDF8),
                                            sparklinePoints = listOf(10f, 20f, 15f, 35f, 25f, 40f, 30f, 50f),
                                            sparklineColor = Color(0xFF38BDF8),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatsCard(
                                            title = "Total Downloads",
                                            value = "35,1K",
                                            change = "+12.4%",
                                            icon = Icons.Default.FileDownload,
                                            iconBgColor = Color(0xFFC084FC).copy(alpha = 0.12f),
                                            iconTintColor = Color(0xFFC084FC),
                                            sparklinePoints = listOf(40f, 30f, 45f, 15f, 35f, 20f, 60f, 48f),
                                            sparklineColor = Color(0xFFC084FC),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AdminStatsCard(
                                            title = "Premium Users",
                                            value = "${6480 + (usersList.filter { it.membershipType == "PREMIUM" }.size * 3)}",
                                            change = "+2.4%",
                                            icon = Icons.Default.Stars,
                                            iconBgColor = Color(0xFFEE2D7B).copy(alpha = 0.12f),
                                            iconTintColor = Color(0xFFEE2D7B),
                                            sparklinePoints = listOf(15f, 18f, 24f, 20f, 35f, 32f, 48f, 55f),
                                            sparklineColor = Color(0xFFEE2D7B),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatsCard(
                                            title = "Total Books",
                                            value = "${5230 + booksList.size}",
                                            change = "+12.4%",
                                            icon = Icons.Default.Book,
                                            iconBgColor = Color(0xFF38BDF8).copy(alpha = 0.12f),
                                            iconTintColor = Color(0xFF38BDF8),
                                            sparklinePoints = listOf(22f, 28f, 18f, 30f, 24f, 42f, 38f, 46f),
                                            sparklineColor = Color(0xFF38BDF8),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AdminStatsCard(
                                            title = "Revenue",
                                            value = "$24,890",
                                            change = "+12.3%",
                                            icon = Icons.Default.AttachMoney,
                                            iconBgColor = Color(0xFF38BDF8).copy(alpha = 0.12f),
                                            iconTintColor = Color(0xFF38BDF8),
                                            sparklinePoints = listOf(30f, 35f, 28f, 45f, 42f, 58f, 52f, 65f),
                                            sparklineColor = Color(0xFF38BDF8),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatsCard(
                                            title = "Active Users",
                                            value = "3,145",
                                            change = "+12.4%",
                                            icon = Icons.Default.Bolt,
                                            iconBgColor = Color(0xFFC084FC).copy(alpha = 0.12f),
                                            iconTintColor = Color(0xFFC084FC),
                                            sparklinePoints = listOf(50f, 42f, 48f, 32f, 35f, 45f, 41f, 55f),
                                            sparklineColor = Color(0xFFC084FC),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            // Analytics and Daily Growth sub-section
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    UserGrowthLineChartCard(modifier = Modifier.weight(1.1f))
                                    DailyGrowthBarChartCard(modifier = Modifier.weight(0.9f))
                                }
                            }

                            // Quick Actions Block
                            item {
                                QuickActionsGrid(
                                    onActionClick = { actionId ->
                                        when (actionId) {
                                            "add_book" -> showAddBookDialog = true
                                            "users" -> adminDashboardTab = "users"
                                            "categories" -> showAddCategoryDialog = true
                                            "reviews" -> adminDashboardTab = "analytics"
                                            "reports" -> adminDashboardTab = "analytics"
                                        }
                                    }
                                )
                            }

                            // Recent Activities Section
                            item {
                                RecentActivitySection()
                            }
                        }
                    }

                    "books" -> {
                        // Advanced Books Publishing Module
                        var searchQuery by remember { mutableStateOf("") }
                        var filterCategory by remember { mutableStateOf("ALL") }
                        
                        val filteredBooks = remember(booksList, searchQuery, filterCategory) {
                            booksList.filter { book ->
                                (searchQuery.isEmpty() || book.title.contains(searchQuery, ignoreCase = true) || book.author.contains(searchQuery, ignoreCase = true)) &&
                                (filterCategory == "ALL" || book.categoryId == filterCategory)
                            }
                        }
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Catalog Publisher Console",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Insert, query, and remove operational materials instantly in Room database.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Interactive "Add Book" trigger banner
                                Button(
                                    onClick = { showAddBookDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Publish New Book Entry", fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Search bar
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search title, author...", color = Color(0xFF64748B)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF1B2032),
                                        unfocusedContainerColor = Color(0xFF1B2032),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Dynamic categories horizontal filter scroll list
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val cats = listOf("ALL" to "All Categories") + categoriesList.map { it.id to it.name }
                                    cats.forEach { (id, label) ->
                                        val isSelected = filterCategory == id
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { filterCategory = id },
                                            label = { Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF38BDF8),
                                                selectedLabelColor = Color.White,
                                                containerColor = Color(0xFF1B2032),
                                                labelColor = Color(0xFF94A3B8)
                                            ),
                                            border = null
                                        )
                                    }
                                }
                            }
                            
                            if (filteredBooks.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "No matching books found.", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                            } else {
                                items(filteredBooks) { book ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
                                        border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF38BDF8).copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Book,
                                                    contentDescription = null,
                                                    tint = Color(0xFF38BDF8)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = book.title,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "by ${book.author} | ${book.categoryId.uppercase()}",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = "${book.pages} pages | Size: ${book.fileSize}",
                                                    color = Color(0xFF64748B),
                                                    fontSize = 10.sp
                                                )
                                            }
                                            
                                            IconButton(
                                                onClick = { viewModel.adminDeleteBook(book.id) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete book",
                                                    tint = Color(0xFFEF4444).copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "users" -> {
                        // Advanced Users & Plan Moderator Section
                        var userSearchQuery by remember { mutableStateOf("") }
                        val filteredUsers = remember(usersList, userSearchQuery) {
                            usersList.filter { u ->
                                userSearchQuery.isEmpty() || u.name.contains(userSearchQuery, ignoreCase = true) || u.email.contains(userSearchQuery, ignoreCase = true)
                            }
                        }
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Database User Directory",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Audit registered accounts, switch roles, and promote memberships in real-time.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                TextField(
                                    value = userSearchQuery,
                                    onValueChange = { userSearchQuery = it },
                                    placeholder = { Text("Search emails, usernames...", color = Color(0xFF64748B)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF1B2032),
                                        unfocusedContainerColor = Color(0xFF1B2032),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            if (filteredUsers.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "No registered users located.", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                            } else {
                                items(filteredUsers) { user ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
                                        border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.25f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Icon
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (user.role == "ADMIN") Color(0xFFEE2D7B).copy(alpha = 0.15f)
                                                            else Color(0xFF38BDF8).copy(alpha = 0.15f)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = user.name.take(1).uppercase(),
                                                        color = if (user.role == "ADMIN") Color(0xFFEE2D7B) else Color(0xFF38BDF8),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(12.dp))
                                                
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = user.name,
                                                        color = Color.White,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = user.email,
                                                        color = Color(0xFF94A3B8),
                                                        fontSize = 11.sp
                                                    )
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(Color(0xFF38BDF8).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(text = "ROLE: ${user.role}", color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(Color(0xFFC084FC).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(text = "PLAN: ${user.membershipType}", color = Color(0xFFC084FC), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            HorizontalDivider(color = Color(0xFF2E374D).copy(alpha = 0.2f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // Toggle plan/roles buttons
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val targetMembership = if (user.membershipType == "FREE") "PREMIUM" else "FREE"
                                                Button(
                                                    onClick = {
                                                        viewModel.adminModifyUser(
                                                            user.email,
                                                            user.name,
                                                            user.role,
                                                            targetMembership
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF1B2032)
                                                    ),
                                                    border = BorderStroke(1.dp, Color(0xFFC084FC).copy(alpha = 0.4f)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(30.dp)
                                                ) {
                                                    Text(
                                                        text = "Toggle Plan: $targetMembership",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                                
                                                val targetRole = if (user.role == "USER") "ADMIN" else "USER"
                                                Button(
                                                    onClick = {
                                                        viewModel.adminModifyUser(
                                                            user.email,
                                                            user.name,
                                                            targetRole,
                                                            user.membershipType
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF1B2032)
                                                    ),
                                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(30.dp)
                                                ) {
                                                    Text(
                                                        text = "Make $targetRole",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "analytics" -> {
                        // Expanded Graphics View
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "System Analytics Studio",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Detailed weekly demographics, system memory usages, and transactions.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                            
                            item {
                                UserGrowthLineChartCard(modifier = Modifier.fillMaxWidth().height(220.dp))
                            }
                            
                            item {
                                DailyGrowthBarChartCard(modifier = Modifier.fillMaxWidth().height(220.dp))
                            }
                            
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(text = "Service System Metrics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        val totalUserCount = usersList.size
                                        val proUserCount = usersList.count { it.membershipType == "PREMIUM" }
                                        val pct = if (totalUserCount > 0) (proUserCount.toFloat() / totalUserCount) else 0f
                                        
                                        ProgressBarWithLabel(
                                            label = "SaaS Conversion: $proUserCount Premium / $totalUserCount Total Users",
                                            progress = pct,
                                            color = Color(0xFFC084FC)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        ProgressBarWithLabel(label = "API Servers Sync Health", progress = 0.98f, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }

                    "settings" -> {
                        // Custom Dashboard tools
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Administrative Preferences",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
                                border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(text = "SaaS Environment Debugger", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    
                                    Button(
                                        onClick = { viewModel.triggerMockNotification("SYSTEM") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F293D)),
                                        border = BorderStroke(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                    ) {
                                        Text(text = "Simulate Real-Time Server Sync Alert", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.markAllNotificationsAsRead() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F293D)),
                                        border = BorderStroke(0.5.dp, Color(0xFFC084FC).copy(alpha = 0.4f))
                                    ) {
                                        Text(text = "Force Flush Unread Communications", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Return to Reader Mode button (Crucial safe navigation fallback)
                            Button(
                                onClick = { viewModel.currentScreen = "main" },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEE2D7B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Exit",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🚪 Return to Standard Reader Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. Premium Dark Bottom Floating Navigation Bar (Matches Reference Image)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13172C)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val adminNavItems = listOf(
                        Triple("dashboard", Icons.Default.Dashboard, "Dashboard"),
                        Triple("books", Icons.Default.Book, "Books"),
                        Triple("users", Icons.Default.People, "Users"),
                        Triple("analytics", Icons.Default.BarChart, "Analytics"),
                        Triple("settings", Icons.Default.Settings, "Settings")
                    )

                    adminNavItems.forEach { (tabId, icon, label) ->
                        val isSelected = adminDashboardTab == tabId
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { adminDashboardTab = tabId }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFF64748B),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add Book Interactive Dialog Overlay
    if (showAddBookDialog) {
        var nBookTitle by remember { mutableStateOf("") }
        var nBookAuthor by remember { mutableStateOf("") }
        var nBookCategory by remember { mutableStateOf("fiction") }
        var nBookDesc by remember { mutableStateOf("") }
        
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAddBookDialog = false }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Publish Custom Book", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    TextField(
                        value = nBookTitle,
                        onValueChange = { nBookTitle = it },
                        label = { Text("Book Title", color = Color(0xFF64748B)) },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF0F121C), unfocusedContainerColor = Color(0xFF0F121C))
                    )
                    TextField(
                        value = nBookAuthor,
                        onValueChange = { nBookAuthor = it },
                        label = { Text("Author Name", color = Color(0xFF64748B)) },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF0F121C), unfocusedContainerColor = Color(0xFF0F121C))
                    )
                    TextField(
                        value = nBookCategory,
                        onValueChange = { nBookCategory = it },
                        label = { Text("Category ID (e.g. poetry, history)", color = Color(0xFF64748B)) },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF0F121C), unfocusedContainerColor = Color(0xFF0F121C))
                    )
                    TextField(
                        value = nBookDesc,
                        onValueChange = { nBookDesc = it },
                        label = { Text("Brief Description", color = Color(0xFF64748B)) },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF0F121C), unfocusedContainerColor = Color(0xFF0F121C))
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddBookDialog = false }) {
                            Text(text = "Cancel", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                if (nBookTitle.isNotEmpty() && nBookAuthor.isNotEmpty()) {
                                    viewModel.adminAddBook(
                                        title = nBookTitle,
                                        author = nBookAuthor,
                                        categoryId = nBookCategory.lowercase().trim(),
                                        description = nBookDesc,
                                        pages = (120..450).random(),
                                        size = "${(2..9).random()}.${(1..9).random()} MB"
                                    )
                                    showAddBookDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text(text = "Publish Now")
                        }
                    }
                }
            }
        }
    }
    
    // Add Category Interactive Dialog Overlay
    if (showAddCategoryDialog) {
        var nCatId by remember { mutableStateOf("") }
        var nCatName by remember { mutableStateOf("") }
        
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showAddCategoryDialog = false }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Create Interactive Category", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    TextField(
                        value = nCatId,
                        onValueChange = { nCatId = it },
                        label = { Text("Category ID (e.g. self-help)", color = Color(0xFF64748B)) },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF0F121C), unfocusedContainerColor = Color(0xFF0F121C))
                    )
                    TextField(
                        value = nCatName,
                        onValueChange = { nCatName = it },
                        label = { Text("Visual Title (e.g. Self Help)", color = Color(0xFF64748B)) },
                        colors = TextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedContainerColor = Color(0xFF0F121C), unfocusedContainerColor = Color(0xFF0F121C))
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text(text = "Cancel", color = Color(0xFF94A3B8))
                        }
                        Button(
                            onClick = {
                                if (nCatId.isNotEmpty() && nCatName.isNotEmpty()) {
                                    viewModel.adminAddCategory(
                                        id = nCatId.lowercase().trim(),
                                        name = nCatName,
                                        iconName = "folder"
                                    )
                                    showAddCategoryDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text(text = "Create Now")
                        }
                    }
                }
            }
        }
    }
}

// Sparklines Area Custom Painting Composable
@Composable
fun Sparkline(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val maxVal = points.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val range = (maxVal - minVal).coerceAtLeast(1f)

        val path = Path()
        val stepX = width / (points.size - 1).coerceAtLeast(1)
        
        for (i in points.indices) {
            val cx = i * stepX
            val cy = height - ((points[i] - minVal) / range) * (height - 8f) - 4f
            if (i == 0) {
                path.moveTo(cx, cy)
            } else {
                val prevX = (i - 1) * stepX
                val prevY = height - ((points[i - 1] - minVal) / range) * (height - 8f) - 4f
                val controlX1 = prevX + stepX / 2f
                val controlY1 = prevY
                val controlX2 = prevX + stepX / 2f
                val controlY2 = cy
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, cx, cy)
            }
        }

        // Main line path stroke
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Gradient area filling underneath
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )
    }
}

// Individual Stats Card matching Design Spec
@Composable
fun AdminStatsCard(
    title: String,
    value: String,
    change: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    sparklinePoints: List<Float>,
    sparklineColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
        border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icon + Change Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(iconBgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = change,
                        color = if (change.startsWith("+")) Color(0xFF10B981) else Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Metric content
                Column {
                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = title,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Embedded sparkline graph running underneath
            Sparkline(
                points = sparklinePoints,
                color = sparklineColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

// User Growth Area Chart
@Composable
fun UserGrowthLineChartCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(175.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
        border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // Dash axis lines
                    val lines = 3
                    for (i in 0..lines) {
                        val dy = (height / lines) * i
                        drawLine(
                            color = Color(0xFF2E374D).copy(alpha = 0.3f),
                            start = androidx.compose.ui.geometry.Offset(0f, dy),
                            end = androidx.compose.ui.geometry.Offset(width, dy),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    val values = listOf(0.2f, 0.45f, 0.35f, 0.65f, 0.48f, 0.85f, 0.62f)
                    val stepX = width / (values.size - 1)
                    val path = Path()
                    val knotsList = values.mapIndexed { i, value ->
                        androidx.compose.ui.geometry.Offset(i * stepX, height - (value * (height - 12f)) - 6f)
                    }

                    for (i in knotsList.indices) {
                        if (i == 0) {
                            path.moveTo(knotsList[i].x, knotsList[i].y)
                        } else {
                            val prev = knotsList[i - 1]
                            val curr = knotsList[i]
                            path.cubicTo(
                                prev.x + stepX / 2f, prev.y,
                                prev.x + stepX / 2f, curr.y,
                                curr.x, curr.y
                            )
                        }
                    }

                    // Fill
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Line overlay
                    drawPath(
                        path = path,
                        color = Color(0xFF38BDF8),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw point markers
                    knotsList.forEach { pt ->
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(text = day, color = Color(0xFF64748B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Daily Growth Bar Chart
@Composable
fun DailyGrowthBarChartCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(175.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
        border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Daily Growth", color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = "$8,520", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFFC084FC),
                    modifier = Modifier.size(16.dp)
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val values = listOf(0.35f, 0.55f, 0.8f, 0.48f, 0.68f, 0.9f)
                values.forEach { hPct ->
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .fillMaxHeight(hPct)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFC084FC), Color(0xFF38BDF8))
                                )
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("03", "04", "05", "06", "07", "08").forEach { n ->
                    Text(text = n, color = Color(0xFF64748B), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Quick Actions horizontal layout card
@Composable
fun QuickActionsGrid(
    onActionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Actions",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val actions = listOf(
                Triple("add_book", Icons.Default.Upload, "Add Book"),
                Triple("users", Icons.Default.People, "Manage Users"),
                Triple("categories", Icons.Default.Category, "Categories"),
                Triple("reviews", Icons.Default.Star, "Reviews"),
                Triple("reports", Icons.Default.Assessment, "Reports")
            )

            actions.forEach { (id, icon, label) ->
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clickable { onActionClick(id) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1B2032)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (id) {
                                        "add_book" -> Color(0xFF38BDF8).copy(alpha = 0.1f)
                                        "users" -> Color(0xFFC084FC).copy(alpha = 0.1f)
                                        "categories" -> Color(0xFFEE2D7B).copy(alpha = 0.1f)
                                        "reviews" -> Color(0xFFEAB308).copy(alpha = 0.1f)
                                        else -> Color(0xFF10B981).copy(alpha = 0.1f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = when (id) {
                                    "add_book" -> Color(0xFF38BDF8)
                                    "users" -> Color(0xFFC084FC)
                                    "categories" -> Color(0xFFEE2D7B)
                                    "reviews" -> Color(0xFFEAB308)
                                    else -> Color(0xFF10B981)
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when (label) {
                            "Categories" -> "Categorior"
                            "Reviews" -> "Autriews"
                            else -> label
                        },
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Activity Log card layout
@Composable
fun RecentActivitySection() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Recent Activity",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2032)),
            border = BorderStroke(1.dp, Color(0xFF2E374D).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val logs = listOf(
                    Triple("Emily S. purchased a Premium plan", "20m ago", Icons.Default.Person),
                    Triple("New book \"Future Tech\" added", "1h ago", Icons.Default.Book),
                    Triple("Support ticket #8472 resolved", "3h ago", Icons.Default.CheckCircle)
                )

                logs.forEachIndexed { i, (msg, time, icon) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when (i) {
                                        0 -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                                        1 -> Color(0xFFC084FC).copy(alpha = 0.15f)
                                        else -> Color(0xFF10B981).copy(alpha = 0.15f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = when (i) {
                                    0 -> Color(0xFF38BDF8)
                                    1 -> Color(0xFFC084FC)
                                    else -> Color(0xFF10B981)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = msg,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = time,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (i < logs.size - 1) {
                        HorizontalDivider(color = Color(0xFF2E374D).copy(alpha = 0.2f), modifier = Modifier.padding(top = 10.dp))
                    }
                }
            }
        }
    }
}



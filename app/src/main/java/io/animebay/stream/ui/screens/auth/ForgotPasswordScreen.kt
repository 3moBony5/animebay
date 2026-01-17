package io.animebay.stream.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // ✅ استيراد جديد
import io.animebay.stream.ui.theme.AppGreen
import kotlinx.coroutines.delay // ✅ استيراد جديد
import kotlinx.coroutines.launch // ✅ استيراد جديد

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel() // ✅ 1. استبدال onSendLinkClick بالـ ViewModel
) {
    var email by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    // ✅ 2. إضافة LaunchedEffect لمراقبة الحالة وعرض الرسائل
    LaunchedEffect(key1 = uiState) {
        // في حالة وجود رسالة خطأ
        uiState.error?.let {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(it)
            }
            authViewModel.resetState() // إعادة تعيين الحالة لتجنب عرض الرسالة مرة أخرى
        }

        // في حالة وجود رسالة نجاح
        uiState.successMessage?.let {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar(it)
                delay(2500) // الانتظار لمدة 2.5 ثانية
                onNavigateBack() // العودة إلى الشاشة السابقة
            }
            // لا تقم بإعادة تعيين الحالة هنا للسماح بالانتقال
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1a2634),
                        Color(0xFF12191f),
                        Color(0xFF0a0f14)
                    )
                )
            )
    ) {
        Scaffold(
            scaffoldState = scaffoldState, // ✅ 3. ربط الـ scaffoldState
            backgroundColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, enabled = !uiState.isLoading) { // تعطيل أثناء التحميل
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    },
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    modifier = Modifier.statusBarsPadding()
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AppGreen,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "إعادة تعيين كلمة المرور",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "أدخل بريدك الإلكتروني المسجل، وسنرسل لك رابطاً لإعادة تعيين كلمة المرور.",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 40.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("البريد الإلكتروني") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = getTextFieldColors(),
                    enabled = !uiState.isLoading // ✅ 4. تعطيل الحقل أثناء التحميل
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { authViewModel.sendPasswordResetEmail(email) }, // ✅ 5. استدعاء الدالة من الـ ViewModel
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppGreen),
                    enabled = !uiState.isLoading // ✅ 6. تعطيل الزر أثناء التحميل
                ) {
                    // ✅ 7. إظهار مؤشر التحميل أو النص
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("إرسال الرابط", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

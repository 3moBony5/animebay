package io.animebay.stream.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.animebay.stream.R
import io.animebay.stream.ui.theme.AppGreen

@Composable
fun GoogleSignInButton(onClick: () -> Unit, isLoading: Boolean) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
        enabled = !isLoading,
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.Transparent)
    ) {
        if (isLoading) {
             CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )
            Text(
                "المتابعة باستخدام جوجل",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
fun getTextFieldColors(): TextFieldColors {
    return TextFieldDefaults.outlinedTextFieldColors(
        textColor = Color.White,
        cursorColor = AppGreen,
        focusedBorderColor = AppGreen,
        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
        focusedLabelColor = AppGreen,
        unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
        leadingIconColor = Color.White.copy(alpha = 0.5f),
        trailingIconColor = Color.White.copy(alpha = 0.5f)
    )
}

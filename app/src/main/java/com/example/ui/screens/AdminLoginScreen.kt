package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.CycleViewModel

@Composable
fun AdminLoginScreen(
    viewModel: CycleViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phone by viewModel.adminPhone.collectAsState()
    val otp by viewModel.adminOtp.collectAsState()
    val isOtpSent by viewModel.isAdminOtpSent.collectAsState()
    val isLoading by viewModel.isAdminLoading.collectAsState()
    val errorMessage by viewModel.adminAuthError.collectAsState()
    val resendCooldown by viewModel.resendCooldown.collectAsState()
    val lastReceivedOtp by viewModel.lastAdminGeneratedOtp.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Secure Header
        Surface(
            color = NavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_login_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Portal Login",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AmberAccent
                        ) {
                            Text(
                                text = "RESTRICTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Authorized Personnel Only • Database Verification",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Security Notice Box
        Surface(
            color = Color(0xFFFEF3C7),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Administrator Authorization Check",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Only phone numbers registered in the store's authorized admin database can access product creation and inventory tools. Default manager: +91 9876543210 or 9999988888.",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("admin_login_card"),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (!isOtpSent) "Enter Registered Admin Mobile" else "Verify Admin OTP",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = if (!isOtpSent)
                        "Enter the registered administrator mobile number to receive a verification OTP."
                    else
                        "Enter the 6-digit verification code sent to +91 $phone",
                    fontSize = 12.sp,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (!isOtpSent) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) viewModel.adminPhone.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_phone_input"),
                        label = { Text("Admin Mobile Number") },
                        prefix = {
                            Text(
                                text = "+91 ",
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEE2E2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFF991B1B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.sendAdminOtp() },
                        enabled = !isLoading && phone.length >= 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_send_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = AmberAccent,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Send Admin Verification OTP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // OTP Box
                    if (lastReceivedOtp.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Admin Security Code:",
                                        fontSize = 11.sp,
                                        color = NavyDark
                                    )
                                    Text(
                                        text = lastReceivedOtp,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AmberAccent,
                                        letterSpacing = 4.sp
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) viewModel.adminOtp.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_otp_input"),
                        label = { Text("Enter 6-Digit Admin OTP") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NavyDark)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.quickFillAdminOtp() },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("admin_autofill_otp_button")
                        ) {
                            Text("Auto-Fill OTP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (resendCooldown > 0) {
                            Text(
                                text = "Resend in ${resendCooldown}s",
                                fontSize = 12.sp,
                                color = SlateTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Resend OTP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier
                                    .clickable { viewModel.sendAdminOtp() }
                                    .padding(4.dp)
                                    .testTag("admin_resend_otp_button")
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEE2E2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFF991B1B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.verifyAdminOtp() },
                        enabled = !isLoading && otp.length >= 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_verify_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Authorize & Enter Admin Panel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Change Mobile Number",
                        fontSize = 12.sp,
                        color = NavyPrimary,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                viewModel.isAdminOtpSent.value = false
                                viewModel.adminAuthError.value = null
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

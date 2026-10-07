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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.theme.EmeraldDiscount
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.CycleViewModel

@Composable
fun CustomerLoginScreen(
    viewModel: CycleViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phone by viewModel.customerPhone.collectAsState()
    val name by viewModel.customerName.collectAsState()
    val otp by viewModel.customerOtp.collectAsState()
    val isOtpSent by viewModel.isCustomerOtpSent.collectAsState()
    val isLoading by viewModel.isCustomerLoading.collectAsState()
    val errorMessage by viewModel.customerAuthError.collectAsState()
    val resendCooldown by viewModel.resendCooldown.collectAsState()
    val lastReceivedOtp by viewModel.lastCustomerGeneratedOtp.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(
            color = NavyPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("customer_login_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Customer Sign In",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Popular Cycle Company Store",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("customer_login_card"),
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
                    text = if (!isOtpSent) "Mobile Number Verification" else "Enter Verification Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = if (!isOtpSent)
                        "Enter your mobile number to receive a secure one-time password."
                    else
                        "We sent a 6-digit verification code to +91 $phone",
                    fontSize = 12.sp,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (!isOtpSent) {
                    // Mobile Number Input with Country Code
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) viewModel.customerPhone.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_phone_input"),
                        label = { Text("Mobile Number") },
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

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.customerName.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input"),
                        label = { Text("Full Name (Optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.sendCustomerOtp() },
                        enabled = !isLoading && phone.length >= 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("customer_send_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Send Verification OTP",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    // OTP Verification UI
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
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Active SMS Code (Testing Helper):",
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
                        onValueChange = { if (it.length <= 6) viewModel.customerOtp.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_otp_input"),
                        label = { Text("Enter 6-Digit OTP") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
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
                            onClick = { viewModel.quickFillCustomerOtp() },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("customer_autofill_otp_button")
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
                                    .clickable { viewModel.sendCustomerOtp() }
                                    .padding(4.dp)
                                    .testTag("customer_resend_otp_button")
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.verifyCustomerOtp() },
                        enabled = !isLoading && otp.length >= 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("customer_verify_otp_button"),
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
                                text = "Verify & Start Shopping",
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
                                viewModel.isCustomerOtpSent.value = false
                                viewModel.customerAuthError.value = null
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

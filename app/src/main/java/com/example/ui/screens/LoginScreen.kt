package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AdminPanelSettings
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldDiscount
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.CycleViewModel

@Composable
fun LoginScreen(
    viewModel: CycleViewModel,
    modifier: Modifier = Modifier
) {
    val phone by viewModel.loginPhone.collectAsState()
    val name by viewModel.loginName.collectAsState()
    val otp by viewModel.loginOtp.collectAsState()
    val generatedOtp by viewModel.generatedOtp.collectAsState()
    val isOtpSent by viewModel.isOtpSent.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val role by viewModel.otpRole.collectAsState()
    val loginBanners by viewModel.loginBanners.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
    ) {
        // Top Brand Header with Login Screen Promo Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            val bannerImage = loginBanners.firstOrNull()?.imageResId
                ?: R.drawable.login_promo_banner_1791185121376

            AsyncImage(
                model = bannerImage,
                contentDescription = "Popular Cycle Promo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                NavyDark.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AmberAccent
                ) {
                    Text(
                        text = "POPULAR CYCLE FEST 2026",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Welcome to Popular Cycle Co.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Flat 45% OFF on MTBs, E-Cycles & Genuine Tyres",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Login / Verification Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("login_card"),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (!isOtpSent) "Login or Sign Up with Mobile OTP" else "Verify Mobile OTP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyDark
                )
                Text(
                    text = if (!isOtpSent) "Enter your 10-digit number to receive a one-time verification code." else "We sent a 6-digit OTP to +91 $phone",
                    fontSize = 12.sp,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Role Selector (Customer Panel vs Admin Panel)
                Text(
                    text = "Select Application Panel:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NavyDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (role == UserRole.CUSTOMER) 2.dp else 1.dp,
                                color = if (role == UserRole.CUSTOMER) NavyPrimary else SlateBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.otpRole.value = UserRole.CUSTOMER }
                            .testTag("select_customer_role"),
                        color = if (role == UserRole.CUSTOMER) NavyPrimary.copy(alpha = 0.08f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (role == UserRole.CUSTOMER) NavyPrimary else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Customer App",
                                fontSize = 12.sp,
                                fontWeight = if (role == UserRole.CUSTOMER) FontWeight.Bold else FontWeight.Normal,
                                color = if (role == UserRole.CUSTOMER) NavyPrimary else Color.Gray
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (role == UserRole.ADMIN) 2.dp else 1.dp,
                                color = if (role == UserRole.ADMIN) AmberAccent else SlateBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.otpRole.value = UserRole.ADMIN }
                            .testTag("select_admin_role"),
                        color = if (role == UserRole.ADMIN) AmberAccent.copy(alpha = 0.12f) else Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (role == UserRole.ADMIN) AmberAccent else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Panel",
                                fontSize = 12.sp,
                                fontWeight = if (role == UserRole.ADMIN) FontWeight.Bold else FontWeight.Normal,
                                color = if (role == UserRole.ADMIN) Color.Black else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isOtpSent) {
                    // Mobile Number Input
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) viewModel.loginPhone.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input"),
                        label = { Text("Mobile Number") },
                        prefix = { Text("+91 ") },
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
                        onValueChange = { viewModel.loginName.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        label = { Text("Your Full Name (Optional)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.sendOtp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("get_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Get OTP Verification Code",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // OTP Sent Simulation Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
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
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SMS Sent! Your Verification Code:",
                                    fontSize = 11.sp,
                                    color = NavyDark
                                )
                                Text(
                                    text = generatedOtp,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AmberAccent,
                                    letterSpacing = 4.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 6) viewModel.loginOtp.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input"),
                        label = { Text("Enter 6-Digit OTP") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.quickFillTestOtp() },
                            modifier = Modifier.testTag("quick_fill_otp_button"),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Auto-Fill OTP", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Text(
                            text = "Resend OTP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier
                                .clickable { viewModel.sendOtp() }
                                .padding(4.dp)
                        )
                    }

                    if (authError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = authError ?: "",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.verifyOtp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_otp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (role == UserRole.ADMIN) "Enter Admin Control Panel" else "Enter Popular Cycle Store",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Trust features highlights
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FeaturePill(icon = Icons.Default.DirectionsBike, text = "100% Assembled")
                FeaturePill(icon = Icons.Default.CheckCircle, text = "1-Yr Warranty")
                FeaturePill(icon = Icons.Default.Security, text = "Safe Payments")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmberAccent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NavyDark)
    }
}

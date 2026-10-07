package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun LoginScreen(
    viewModel: CycleViewModel,
    modifier: Modifier = Modifier
) {
    WelcomeScreen(
        onSelectCustomerLogin = { viewModel.navigateTo(Screen.CustomerLogin) },
        onSelectAdminLogin = { viewModel.navigateTo(Screen.AdminLogin) },
        modifier = modifier
    )
}

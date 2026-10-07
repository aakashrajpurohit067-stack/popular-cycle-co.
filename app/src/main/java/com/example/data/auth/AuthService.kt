package com.example.data.auth

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

sealed class OtpResult {
    data class Success(val code: String, val message: String) : OtpResult()
    data class Error(val message: String) : OtpResult()
}

class AuthService(
    private val context: Context,
    private val simulatedDelayMs: Long = 0L
) {

    private val random = SecureRandom()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var cooldownJob: Job? = null

    private val _resendCooldown = MutableStateFlow(0)
    val resendCooldown: StateFlow<Int> = _resendCooldown.asStateFlow()

    @Volatile
    private var activePhone: String = ""
    @Volatile
    private var activeOtp: String = ""
    @Volatile
    private var otpExpiryTime: Long = 0L

    suspend fun sendOtpSuspend(rawPhone: String): OtpResult = withContext(Dispatchers.IO) {
        val cleanPhone = rawPhone.replace("+91", "").replace(" ", "").replace("-", "").trim()
        if (cleanPhone.length < 10) {
            return@withContext OtpResult.Error("Please enter a valid 10-digit mobile number")
        }

        if (_resendCooldown.value > 0) {
            return@withContext OtpResult.Error("Please wait ${_resendCooldown.value}s before requesting a new OTP")
        }

        if (simulatedDelayMs > 0) {
            delay(simulatedDelayMs)
        }

        // Generate 6-digit random code
        val codeNum = 100000 + random.nextInt(900000)
        val code = codeNum.toString()

        activePhone = cleanPhone
        activeOtp = code
        otpExpiryTime = System.currentTimeMillis() + (5 * 60 * 1000L) // 5 minutes validity

        // Show system toast with OTP code for seamless device testing
        try {
            Handler(Looper.getMainLooper()).post {
                try {
                    Toast.makeText(
                        context,
                        "Popular Cycle Verification Code: $code (Valid for 5 mins)",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        // Start 30-second cooldown
        startCooldown(30)

        OtpResult.Success(code, "Verification code sent to +91 $cleanPhone")
    }

    fun sendOtp(
        rawPhone: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                when (val result = sendOtpSuspend(rawPhone)) {
                    is OtpResult.Success -> onSuccess(result.code)
                    is OtpResult.Error -> onError(result.message)
                }
            } catch (e: Exception) {
                onError("Failed to send OTP: ${e.localizedMessage ?: "Network error"}")
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = scope.launch {
            _resendCooldown.value = seconds
            for (i in seconds downTo 1) {
                _resendCooldown.value = i
                delay(1000)
            }
            _resendCooldown.value = 0
        }
    }

    suspend fun verifyOtp(phone: String, inputOtp: String): Boolean = withContext(Dispatchers.IO) {
        val cleanPhone = phone.replace("+91", "").replace(" ", "").replace("-", "").trim()
        if (simulatedDelayMs > 0) {
            delay(simulatedDelayMs)
        }

        if (System.currentTimeMillis() > otpExpiryTime && otpExpiryTime > 0) {
            return@withContext false
        }

        if (cleanPhone != activePhone && activePhone.isNotBlank()) {
            return@withContext false
        }

        // Allow active generated OTP or standard tester code 123456
        val isValid = (inputOtp.trim() == activeOtp && activeOtp.isNotBlank()) || inputOtp.trim() == "123456"
        isValid
    }

    fun getActiveOtpForTesting(): String = activeOtp
}

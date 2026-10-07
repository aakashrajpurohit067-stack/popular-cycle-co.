package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AdminEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.model.ProductCategory
import com.example.data.model.UserRole
import com.example.data.repository.CycleRepository
import com.example.ui.viewmodel.CycleViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CycleAppRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: CycleRepository
    private lateinit var authService: AuthService
    private lateinit var sessionManager: com.example.data.auth.SessionManager
    private lateinit var viewModel: CycleViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = CycleRepository(
            productDao = database.productDao(),
            bannerDao = database.bannerDao(),
            orderDao = database.orderDao(),
            cartDao = database.cartDao(),
            adminDao = database.adminDao()
        )
        authService = AuthService(context)
        sessionManager = com.example.data.auth.SessionManager(context)
        sessionManager.clearSession()
        viewModel = CycleViewModel(repository, authService, sessionManager)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInitialStartScreenIsSplash() {
        assertEquals(Screen.Splash, viewModel.currentScreen.value)
    }

    @Test
    fun testCustomerOtpLoginFlow() = runBlocking {
        viewModel.customerPhone.value = "9876543210"
        val generated = viewModel.sendCustomerOtpDirect()

        assertTrue(generated.isNotBlank())
        assertEquals(6, generated.length)

        viewModel.customerOtp.value = generated
        val verified = viewModel.verifyCustomerOtpDirect()
        assertTrue(verified)

        // Verified customer state
        assertTrue(viewModel.userSession.value.isLoggedIn)
        assertEquals(UserRole.CUSTOMER, viewModel.userSession.value.role)
        assertEquals(Screen.Home, viewModel.currentScreen.value)
    }

    @Test
    fun testAdminAuthorizationRejectsUnauthorizedNumber() = runBlocking {
        // Only 9876543210 is authorized in this test
        repository.registerAdmin(
            AdminEntity(
                phone = "9876543210",
                name = "Store Manager",
                roleTitle = "Manager"
            )
        )

        // Attempt login with unauthorized customer number
        viewModel.adminPhone.value = "9123456789"
        val generated = viewModel.sendAdminOtpDirect()
        assertTrue(generated.isNotBlank())

        viewModel.adminOtp.value = generated
        val verified = viewModel.verifyAdminOtpDirect()
        assertFalse(verified)

        // Should be rejected
        assertFalse(viewModel.userSession.value.isLoggedIn)
        assertNotNull(viewModel.adminAuthError.value)
        assertTrue(viewModel.adminAuthError.value!!.contains("Access Denied"))
    }

    @Test
    fun testAdminAuthorizationAcceptsAuthorizedNumber() = runBlocking {
        repository.registerAdmin(
            AdminEntity(
                phone = "9876543210",
                name = "Popular Cycle Admin",
                roleTitle = "Head Store Manager"
            )
        )

        viewModel.adminPhone.value = "9876543210"
        val generated = viewModel.sendAdminOtpDirect()
        assertTrue(generated.isNotBlank())

        viewModel.adminOtp.value = generated
        val verified = viewModel.verifyAdminOtpDirect()
        assertTrue(verified)

        // Should succeed and grant Admin role
        assertTrue(viewModel.userSession.value.isLoggedIn)
        assertEquals(UserRole.ADMIN, viewModel.userSession.value.role)
        assertEquals(Screen.AdminDashboard, viewModel.currentScreen.value)
    }

    @Test
    fun testProductAddEditDeletePersistence() = runBlocking {
        val newCycle = ProductEntity(
            name = "Test Popular Trail Pro",
            category = ProductCategory.MENS.displayName,
            brand = "Popular Cycle",
            model = "TEST-2026",
            originalPrice = 20000.0,
            discountedPrice = 14999.0,
            stock = 5,
            imageUrisJson = "/path/to/photo1.jpg||/path/to/photo2.jpg",
            description = "Test description"
        )

        val insertedId = repository.saveProduct(newCycle)
        assertTrue(insertedId > 0)

        val fetched = repository.getProductByIdDirect(insertedId)
        assertNotNull(fetched)
        assertEquals("Test Popular Trail Pro", fetched?.name)
        assertEquals("TEST-2026", fetched?.model)
        assertEquals(2, fetched?.getPhotoList()?.size)

        // Test Edit
        val updated = fetched!!.copy(discountedPrice = 12999.0, stock = 12)
        repository.saveProduct(updated)

        val afterUpdate = repository.getProductByIdDirect(insertedId)
        assertEquals(12999.0, afterUpdate?.discountedPrice ?: 0.0, 0.01)
        assertEquals(12, afterUpdate?.stock)

        // Test Delete
        repository.deleteProduct(insertedId)
        val afterDelete = repository.getProductByIdDirect(insertedId)
        assertNull(afterDelete)
    }

    @Test
    fun testSessionPersistenceAfterRestart() = runBlocking {
        // Customer login
        viewModel.customerPhone.value = "9876543210"
        viewModel.customerName.value = "Aakash Sharma"
        val code = viewModel.sendCustomerOtpDirect()
        viewModel.customerOtp.value = code
        val verified = viewModel.verifyCustomerOtpDirect()
        assertTrue(verified)

        assertTrue(viewModel.userSession.value.isLoggedIn)

        // Simulate app kill & recreation with persistent session
        val newViewModel = CycleViewModel(repository, authService, sessionManager)
        assertTrue(newViewModel.userSession.value.isLoggedIn)
        assertEquals("9876543210", newViewModel.userSession.value.phone)
        assertEquals(UserRole.CUSTOMER, newViewModel.userSession.value.role)

        // Test Logout clears persistence
        newViewModel.logout()
        assertFalse(newViewModel.userSession.value.isLoggedIn)

        val freshViewModelAfterLogout = CycleViewModel(repository, authService, sessionManager)
        assertFalse(freshViewModelAfterLogout.userSession.value.isLoggedIn)
    }
}

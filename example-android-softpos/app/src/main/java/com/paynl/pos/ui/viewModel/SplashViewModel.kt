package com.paynl.pos.ui.viewModel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.paynl.pos.Home
import com.paynl.pos.Onboarding
import com.paynl.pos.sdk.shared.exceptions.SVErrorBaseException
import com.paynl.pos.sdk.shared.models.paynl.PayNlInitResult
import com.paynl.pos.services.PaymentService
import java.util.concurrent.Executors

class SplashViewModel(private val context: Context, private val navHostController: NavHostController): ViewModel() {
    private val executeService = Executors.newSingleThreadExecutor()

    fun start() {
        Log.e("SplashScreen", "onStart")

        executeService.submit {
            try {
                PaymentService.instance.setContext(context)

                val initResult = PaymentService.instance.initSdk()
                if (initResult == null) {
                    Log.e("SplashScreen", "Got error during init: EMPTY")
                    return@submit;
                }

                Log.e("SplashScreen", "Result: ${initResult}")
                when (initResult) {
                    PayNlInitResult.needsLogin -> resetNavigationTo(Onboarding)
                    PayNlInitResult.readyForPayments -> resetNavigationTo(Home)
                }
            } catch(exception: SVErrorBaseException) {
                Log.e("SplashScreen", "Got error during init: ${exception.code} - ${exception.description}")
            }
        }
    }

    private fun resetNavigationTo(screen: Any) {
        Handler(Looper.getMainLooper()).post {
            navHostController.navigate(screen) {
                popUpTo(navHostController.graph.findStartDestination().id) {
                    saveState = false
                    inclusive = true
                }
            }
        }
    }
}
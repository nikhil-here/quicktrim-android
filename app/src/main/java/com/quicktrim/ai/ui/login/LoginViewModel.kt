package com.quicktrim.ai.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicktrim.ai.domain.repository.AuthState
import com.quicktrim.ai.domain.repository.UserRepository
import com.quicktrim.ai.ui.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



sealed class LoginScreenSideEffects {
    data class Navigate(val path: String) : LoginScreenSideEffects()
}

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    val authState = userRepository.authState

    private val _sideEffect = Channel<LoginScreenSideEffects>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        initObserver()
    }

    private fun initObserver() {
        viewModelScope.launch {
            userRepository.authState.collectLatest {
                if (it is AuthState.LoggedIn) {
                    _sideEffect.trySend(LoginScreenSideEffects.Navigate(Routes.Upload.path))
                }
            }
        }
    }

    fun onGoogleSignInClick(activity: Activity) {
        userRepository.launchSignInFlow(activity)
    }

    fun onLoginScreenOpen(activity: Activity) {
        userRepository.launchSignInBottomSheet(activity)
    }
}
package com.quicktrim

import android.app.Activity
import android.util.Log
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

private const val TAG = "MainViewModel"

sealed class MainScreenSideEffect {
    data class Navigate(val path: String) : MainScreenSideEffect()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val authState = userRepository.authState

    private val _sideEffect = Channel<MainScreenSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        Log.i(TAG, "init: ")
        initListeners()
    }

    fun checkAndLaunchLoginFlow(activity: Activity) {
        userRepository.launchSignInBottomSheet(activity)
    }

    private fun initListeners() {
        viewModelScope.launch {
            launch {
                userRepository.authState.collectLatest {
                    if (it is AuthState.LogOut) {
                        _sideEffect.trySend(MainScreenSideEffect.Navigate(Routes.Login.path))
                    }
                }
            }
        }
    }

    fun logout(activity: Activity) {
        userRepository.onLogout(activity)
    }
}
package com.quicktrim.ai.ui.login

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.quicktrim.ai.R
import com.quicktrim.ai.domain.repository.AuthState
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val authState by loginViewModel.authState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    LaunchedEffect(true) {
        loginViewModel.onLoginScreenOpen(activity!!)
    }
    LaunchedEffect(true) {
        loginViewModel.sideEffect.collectLatest { effect ->
            when(effect) {
                is LoginScreenSideEffects.Navigate -> {
                    navController.navigate(effect.path)
                }
            }
        }
    }
    LoginScreenUi(
        modifier = Modifier.fillMaxSize(),
        authState = authState,
        onLoginClick = {
            activity?.let { loginViewModel.onGoogleSignInClick(activity) }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreenUi(
    modifier: Modifier = Modifier,
    authState: AuthState,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (authState is AuthState.Loading) {
            LoadingIndicator(polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(
            onClick = onLoginClick
        ) {
            Text("SIGN IN WITH GOOGLE")
        }
    }
}


@Preview
@Composable
private fun PreviewLoginScreenUi() {
    QuicktrimandroidTheme {
        LoginScreenUi(
            modifier = Modifier.fillMaxSize(),
            authState = AuthState.LogOut,
            onLoginClick = {

            }
        )
    }
}
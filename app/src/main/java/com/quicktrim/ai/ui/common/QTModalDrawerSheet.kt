package com.quicktrim.ai.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicktrim.MainViewModel
import com.quicktrim.ai.domain.repository.AuthState

@Composable
fun QTModalDrawerSheet(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    onLogoutClick: () -> Unit
) {
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()
    ModalDrawerSheet {
        Column() {
            ProfileCard(
                authState = authState,
                onLogoutClick = onLogoutClick
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    authState: AuthState,
    onLogoutClick: () -> Unit
) {
    val photoUrl = remember(authState) {
        (authState as? AuthState.LoggedIn)?.user?.photoUrl.toString()
    }

    val displayName = remember(authState) {
        (authState as? AuthState.LoggedIn)?.user?.displayName.toString()
    }

    val email = remember(authState) {
        (authState as? AuthState.LoggedIn)?.user?.email.toString()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onLogoutClick
            ) {
                Text("LOGOUT")
            }
        }
    }
}

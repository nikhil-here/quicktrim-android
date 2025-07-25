package com.quicktrim.ai.domain.repository

import android.app.Activity
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.quicktrim.ai.R
import com.quicktrim.ai.domain.model.User
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


sealed interface AuthState {
    data object Loading : AuthState
    data class LoggedIn(val user: User) : AuthState
    data object LogOut : AuthState
}

private const val TAG = "UserRepository"

@Singleton
class UserRepository @Inject constructor() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "UserRepository Exception", throwable)
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

    init {
        initListener()
    }

    private fun initListener() {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            _authState.update { auth.toAuthState() }
        }
        Firebase.auth.addAuthStateListener(listener)
    }

    fun launchSignInBottomSheet(activity: Activity) {
        scope.launch {
            Log.i(TAG, "onGoogleSignInClick: ")
            if (Firebase.auth.currentUser != null) return@launch
            _authState.update { AuthState.Loading }
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(activity.getString(R.string.web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = CredentialManager.create(activity).getCredential(
                    context = activity,
                    request = request
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                _authState.update { AuthState.LogOut }
                Log.i(TAG, "GetCredentialException: ", e)
            }
        }
    }

    fun launchSignInFlow(activity: Activity) {
        scope.launch {
            try {
                if (Firebase.auth.currentUser != null) return@launch
                Log.i(TAG, "handleNoCredentialException: ")
                _authState.update { AuthState.Loading }
                val signInWithGoogleOption = GetSignInWithGoogleOption
                    .Builder(serverClientId = activity.getString(R.string.web_client_id))
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()
                val result = CredentialManager.create(activity).getCredential(
                    context = activity,
                    request = request
                )
                handleSignIn(result)
            } catch (e: Exception) {
                _authState.update { AuthState.LogOut }
                Log.e(TAG, "handleNoCredentialException: ", e)
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        Log.i(TAG, "handleSignIn: ")
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            _authState.update { AuthState.LogOut }
        }
    }

    private fun firebaseAuthWithGoogle(token: String) {
        Log.i(TAG, "firebaseAuthWithGoogle: ")
        val credential = GoogleAuthProvider.getCredential(token, null)
        val auth = Firebase.auth
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.update { Firebase.toAuthState() }
                } else {
                    _authState.update { AuthState.LogOut }
                    Log.e(TAG, "firebaseAuthWithGoogle: exception", task.exception)
                }
            }
    }

    fun onLogout(activity: Activity) {
        scope.launch {
            _authState.update { AuthState.Loading }
            Firebase.auth.signOut()
            scope.launch {
                try {
                    val clearRequest = ClearCredentialStateRequest()
                    val credentialManager = CredentialManager.create(activity)
                    credentialManager.clearCredentialState(clearRequest)
                } catch (e: NoCredentialException) {

                } catch (e: ClearCredentialException) {
                    Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
                }
            }
        }
    }
}

fun Firebase.toAuthState(): AuthState {
    return this.auth.currentUser?.let { currentUser ->
        AuthState.LoggedIn(
            user = User(
                userId = currentUser.uid,
                email = currentUser.email,
                photoUrl = currentUser.photoUrl,
                displayName = currentUser.displayName,
            )
        )
    } ?: AuthState.LogOut
}

fun FirebaseAuth.toAuthState(): AuthState {
    return this.currentUser?.let { currentUser ->
        AuthState.LoggedIn(
            user = User(
                userId = currentUser.uid,
                email = currentUser.email,
                photoUrl = currentUser.photoUrl,
                displayName = currentUser.displayName,
            )
        )
    } ?: AuthState.LogOut
}
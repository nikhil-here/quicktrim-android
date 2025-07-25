package com.quicktrim.ai.domain.model

import android.net.Uri

data class User(
    val userId: String,
    val email: String?,
    val photoUrl: Uri?,
    val displayName: String?
)

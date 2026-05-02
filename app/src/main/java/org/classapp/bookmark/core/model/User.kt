package org.classapp.bookmark.core.model

data class User (
    val id: String? = "",
    private var password: String? = "",
    val username: String? = "",
    val email: String? = ""
)

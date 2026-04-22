package org.classapp.bookmark.core.src.main.java.org.classapp.lib.model

data class User (
    private var password: String? = "",
    val id: String? = "",
    val username: String? = "",
    val email: String? = ""

    public fun hashAndSetPassword(plainPass: String) {
        // TODO: Hash password
        this.password = plainPass
    }
)

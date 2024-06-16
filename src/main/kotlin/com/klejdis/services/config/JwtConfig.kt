package com.klejdis.services.config

import io.github.cdimascio.dotenv.Dotenv

data class JwtConfig(val vault: Dotenv) {
    val secret = vault["JWT_SECRET"]
    val issuer = vault["JWT_ISSUER"]
    val audience = vault["JWT_AUDIENCE"]
    val realm = vault["JWT_REALM"]
}

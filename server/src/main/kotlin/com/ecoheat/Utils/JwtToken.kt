package com.ecoheat.Utils
import io.github.cdimascio.dotenv.dotenv
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.context.MessageSource
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.security.Key
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Component
class JwtToken(private val messageSource: MessageSource) {

    private final val dotenv = dotenv()
    val secretKey = dotenv["JWT_SECRET"]!!

    fun generateToken(
        id: Long?,
        name: String?,
        role: String
    ): String? {
        val now = Date()
        val expiryDate = Date(now.time + 7200000000000)

        val key = Keys.hmacShaKeyFor(secretKey.toByteArray())

        return Jwts.builder()
            .setSubject(id.toString())
            .claim("id", id)
            .claim("name", name)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key)
            .compact()
    }
}

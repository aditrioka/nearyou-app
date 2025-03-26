package id.nearyou.app.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${security.jwt.secret-key}")
    private val secretKey: String,
    
    @Value("\${security.jwt.expiration}")
    private val jwtExpiration: Long,
    
    @Value("\${security.jwt.refresh-expiration}")
    private val refreshExpiration: Long
) {
    private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))

    fun generateToken(userDetails: UserDetails): String {
        return buildToken(
            mapOf("username" to userDetails.username),
            userDetails,
            jwtExpiration
        )
    }

    fun generateRefreshToken(userDetails: UserDetails): String {
        return buildToken(
            mapOf("username" to userDetails.username),
            userDetails,
            refreshExpiration
        )
    }

    private fun buildToken(
        extraClaims: Map<String, Any>,
        userDetails: UserDetails,
        expiration: Long
    ): String {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.username)
            .issuedAt(Date(System.currentTimeMillis()))
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String? {
        return extractClaim(token) { it.subject }
    }

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        return extractExpiration(token).before(Date())
    }

    private fun extractExpiration(token: String): Date {
        return extractClaim(token) { it.expiration }
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}

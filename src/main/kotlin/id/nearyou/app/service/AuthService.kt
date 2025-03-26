package id.nearyou.app.service

import id.nearyou.app.domain.User
import id.nearyou.app.repository.UserRepository
import id.nearyou.app.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun authenticate(username: String, password: String): String {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(username, password)
        )
        
        SecurityContextHolder.getContext().authentication = authentication
        
        return jwtTokenProvider.generateToken(authentication.principal as UserDetails)
    }
    
    fun refreshToken(refreshToken: String): String? {
        val username = jwtTokenProvider.extractUsername(refreshToken) ?: return null
        
        val userDetails = userRepository.findByUsername(username)
            .map { user ->
                org.springframework.security.core.userdetails.User(
                    user.username,
                    user.passwordHash,
                    listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("USER"))
                )
            }
            .orElse(null) ?: return null
        
        if (!jwtTokenProvider.isTokenValid(refreshToken, userDetails)) {
            return null
        }
        
        return jwtTokenProvider.generateToken(userDetails)
    }
    
    @Transactional
    fun register(username: String, email: String, password: String, bio: String? = null): User {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already exists: $username")
        }
        
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already exists: $email")
        }
        
        val user = User(
            username = username,
            email = email,
            passwordHash = passwordEncoder.encode(password),
            bio = bio
        )
        
        return userRepository.save(user)
    }
    
    fun getCurrentUser(): User? {
        val authentication = SecurityContextHolder.getContext().authentication
        
        if (authentication != null && authentication.isAuthenticated) {
            val username = authentication.name
            return userRepository.findByUsername(username).orElse(null)
        }
        
        return null
    }
    
    @Transactional
    fun changePassword(userId: String, currentPassword: String, newPassword: String): Boolean {
        val user = userRepository.findById(java.util.UUID.fromString(userId))
            .orElseThrow { NoSuchElementException("User not found") }
        
        // Authenticate with current password
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(user.username, currentPassword)
            )
        } catch (e: Exception) {
            return false
        }
        
        // Update password
        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword)
        )
        
        userRepository.save(updatedUser)
        return true
    }
}

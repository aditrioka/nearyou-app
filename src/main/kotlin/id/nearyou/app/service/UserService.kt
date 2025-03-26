package id.nearyou.app.service

import id.nearyou.app.domain.Location
import id.nearyou.app.domain.User
import id.nearyou.app.repository.LocationRepository
import id.nearyou.app.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun findById(id: UUID): Optional<User> {
        return userRepository.findById(id)
    }
    
    fun findByUsername(username: String): Optional<User> {
        return userRepository.findByUsername(username)
    }
    
    fun findByEmail(email: String): Optional<User> {
        return userRepository.findByEmail(email)
    }
    
    fun exists(username: String, email: String): Boolean {
        return userRepository.existsByUsername(username) || userRepository.existsByEmail(email)
    }
    
    @Transactional
    fun createUser(username: String, email: String, password: String, bio: String? = null): User {
        if (exists(username, email)) {
            throw IllegalArgumentException("Username or email already exists")
        }
        
        val user = User(
            username = username,
            email = email,
            passwordHash = passwordEncoder.encode(password),
            bio = bio
        )
        
        return userRepository.save(user)
    }
    
    @Transactional
    fun updateProfile(userId: UUID, bio: String?, profilePictureUrl: String?): User {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val updatedUser = user.copy(
            bio = bio ?: user.bio,
            profilePictureUrl = profilePictureUrl ?: user.profilePictureUrl,
            updatedAt = LocalDateTime.now()
        )
        
        return userRepository.save(updatedUser)
    }
    
    @Transactional
    fun updateLocation(userId: UUID, latitude: Double, longitude: Double, address: String? = null, 
                      city: String? = null, country: String? = null): User {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val location = Location(
            latitude = latitude,
            longitude = longitude,
            address = address,
            city = city,
            country = country
        )
        
        val savedLocation = locationRepository.save(location)
        
        val updatedUser = user.copy(
            lastLocation = savedLocation,
            updatedAt = LocalDateTime.now()
        )
        
        return userRepository.save(updatedUser)
    }
    
    @Transactional
    fun deactivateAccount(userId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val updatedUser = user.copy(
            active = false,
            updatedAt = LocalDateTime.now()
        )
        
        userRepository.save(updatedUser)
    }
    
    fun findNearbyUsers(latitude: Double, longitude: Double, radius: Double = 5000.0): List<User> {
        return userRepository.findNearbyUsers(latitude, longitude, radius)
    }
}

package id.nearyou.app.repository

import id.nearyou.app.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    
    fun findByUsername(username: String): Optional<User>
    
    fun findByEmail(email: String): Optional<User>
    
    fun existsByUsername(username: String): Boolean
    
    fun existsByEmail(email: String): Boolean
    
    @Query("SELECT u FROM User u WHERE u.lastLocation IS NOT NULL AND u.active = true")
    fun findAllWithLocation(): List<User>
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.lastLocation IS NOT NULL 
        AND u.active = true 
        AND function('ST_DistanceSphere', 
            function('ST_MakePoint', u.lastLocation.longitude, u.lastLocation.latitude),
            function('ST_MakePoint', :longitude, :latitude)
        ) <= :radius
    """)
    fun findNearbyUsers(latitude: Double, longitude: Double, radius: Double): List<User>
}

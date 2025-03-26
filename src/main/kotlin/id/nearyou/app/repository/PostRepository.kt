package id.nearyou.app.repository

import id.nearyou.app.domain.Post
import id.nearyou.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PostRepository : JpaRepository<Post, UUID> {
    
    fun findByUserAndIsDeletedFalse(user: User, pageable: Pageable): Page<Post>
    
    @Query("""
        SELECT p FROM Post p 
        WHERE p.isDeleted = false 
        AND function('ST_DistanceSphere', 
            function('ST_MakePoint', p.location.longitude, p.location.latitude),
            function('ST_MakePoint', :longitude, :latitude)
        ) <= :radius
        ORDER BY p.createdAt DESC
    """)
    fun findNearbyPosts(latitude: Double, longitude: Double, radius: Double, pageable: Pageable): Page<Post>
    
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false ORDER BY p.createdAt DESC")
    fun findLatestPosts(pageable: Pageable): Page<Post>
}

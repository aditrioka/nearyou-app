package id.nearyou.app.service

import id.nearyou.app.domain.Location
import id.nearyou.app.domain.Post
import id.nearyou.app.domain.User
import id.nearyou.app.repository.LocationRepository
import id.nearyou.app.repository.PostRepository
import id.nearyou.app.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository
) {

    fun findById(id: UUID): Post? {
        val post = postRepository.findById(id).orElse(null)
        return if (post?.isDeleted == false) post else null
    }
    
    fun findByUser(userId: UUID, pageable: Pageable): Page<Post> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        return postRepository.findByUserAndIsDeletedFalse(user, pageable)
    }
    
    fun findNearbyPosts(latitude: Double, longitude: Double, radius: Double = 5000.0, pageable: Pageable): Page<Post> {
        return postRepository.findNearbyPosts(latitude, longitude, radius, pageable)
    }
    
    fun findLatestPosts(pageable: Pageable): Page<Post> {
        return postRepository.findLatestPosts(pageable)
    }
    
    @Transactional
    fun createPost(userId: UUID, content: String, latitude: Double, longitude: Double, 
                  address: String? = null, city: String? = null, country: String? = null,
                  mediaUrls: List<String> = emptyList()): Post {
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
        
        val post = Post(
            user = user,
            content = content,
            location = savedLocation,
            mediaUrls = mediaUrls.toMutableList()
        )
        
        return postRepository.save(post)
    }
    
    @Transactional
    fun updatePost(postId: UUID, content: String, mediaUrls: List<String>? = null): Post {
        val post = postRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found with ID: $postId") }
        
        if (post.isDeleted) {
            throw IllegalStateException("Cannot update a deleted post")
        }
        
        val updatedPost = post.copy(
            content = content,
            updatedAt = LocalDateTime.now()
        )
        
        if (mediaUrls != null) {
            updatedPost.mediaUrls.clear()
            updatedPost.mediaUrls.addAll(mediaUrls)
        }
        
        return postRepository.save(updatedPost)
    }
    
    @Transactional
    fun deletePost(postId: UUID, userId: UUID) {
        val post = postRepository.findById(postId)
            .orElseThrow { NoSuchElementException("Post not found with ID: $postId") }
        
        if (post.user.id != userId) {
            throw IllegalStateException("Not authorized to delete this post")
        }
        
        val updatedPost = post.copy(
            isDeleted = true,
            updatedAt = LocalDateTime.now()
        )
        
        postRepository.save(updatedPost)
    }
}

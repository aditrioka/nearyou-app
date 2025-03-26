package id.nearyou.app.service

import id.nearyou.app.domain.Notification
import id.nearyou.app.domain.NotificationType
import id.nearyou.app.domain.User
import id.nearyou.app.repository.NotificationRepository
import id.nearyou.app.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {

    fun findById(id: UUID): Notification? {
        val notification = notificationRepository.findById(id).orElse(null)
        return if (notification?.isDeleted == false) notification else null
    }
    
    fun getUserNotifications(userId: UUID, pageable: Pageable): Page<Notification> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
            
        return notificationRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user, pageable)
    }
    
    fun getUnreadNotifications(userId: UUID, pageable: Pageable): Page<Notification> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
            
        return notificationRepository.findUnreadNotificationsForUser(user, pageable)
    }
    
    fun countUnreadNotifications(userId: UUID): Long {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
            
        return notificationRepository.countByUserAndReadAtIsNullAndIsDeletedFalse(user)
    }
    
    @Transactional
    fun createNotification(userId: UUID, type: NotificationType, content: String, relatedEntityId: UUID? = null): Notification {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val notification = Notification(
            user = user,
            type = type,
            content = content,
            relatedEntityId = relatedEntityId
        )
        
        return notificationRepository.save(notification)
    }
    
    @Transactional
    fun markAsRead(notificationId: UUID, userId: UUID) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found with ID: $notificationId") }
        
        if (notification.user.id != userId) {
            throw IllegalStateException("Not authorized to mark this notification as read")
        }
        
        if (notification.readAt == null) {
            val updatedNotification = notification.copy(
                readAt = LocalDateTime.now()
            )
            
            notificationRepository.save(updatedNotification)
        }
    }
    
    @Transactional
    fun markAllAsRead(userId: UUID) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
            
        val unreadNotifications = notificationRepository.findUnreadNotificationsForUser(user, Pageable.unpaged())
        
        val now = LocalDateTime.now()
        unreadNotifications.forEach { notification ->
            notificationRepository.save(notification.copy(readAt = now))
        }
    }
    
    @Transactional
    fun deleteNotification(notificationId: UUID, userId: UUID) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found with ID: $notificationId") }
        
        if (notification.user.id != userId) {
            throw IllegalStateException("Not authorized to delete this notification")
        }
        
        val updatedNotification = notification.copy(
            isDeleted = true
        )
        
        notificationRepository.save(updatedNotification)
    }
    
    @Transactional
    fun createNearbyUserNotification(userId: UUID, nearbyUserId: UUID, distance: Double) {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
        
        val nearbyUser = userRepository.findById(nearbyUserId)
            .orElseThrow { NoSuchElementException("Nearby user not found with ID: $nearbyUserId") }
        
        // Check if a similar notification already exists and is recent (within 24 hours)
        val recentNotifications = notificationRepository.findByUserAndTypeAndRelatedEntityIdAndIsDeletedFalse(
            user, NotificationType.NEARBY, nearbyUserId
        )
        
        val twentyFourHoursAgo = LocalDateTime.now().minusHours(24)
        val hasRecentNotification = recentNotifications.any { it.createdAt.isAfter(twentyFourHoursAgo) }
        
        if (!hasRecentNotification) {
            val formattedDistance = if (distance < 1000) {
                "${distance.toInt()} meters"
            } else {
                String.format("%.1f kilometers", distance / 1000)
            }
            
            val content = "${nearbyUser.username} is nearby (${formattedDistance} away)"
            
            createNotification(userId, NotificationType.NEARBY, content, nearbyUserId)
        }
    }
}

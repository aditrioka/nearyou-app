package id.nearyou.app.repository

import id.nearyou.app.domain.Notification
import id.nearyou.app.domain.NotificationType
import id.nearyou.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    
    fun findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user: User, pageable: Pageable): Page<Notification>
    
    @Query("""
        SELECT n FROM Notification n 
        WHERE n.user = :user 
        AND n.readAt IS NULL
        AND n.isDeleted = false
        ORDER BY n.createdAt DESC
    """)
    fun findUnreadNotificationsForUser(user: User, pageable: Pageable): Page<Notification>
    
    fun countByUserAndReadAtIsNullAndIsDeletedFalse(user: User): Long
    
    fun findByUserAndTypeAndRelatedEntityIdAndIsDeletedFalse(user: User, type: NotificationType, relatedEntityId: UUID): List<Notification>
}

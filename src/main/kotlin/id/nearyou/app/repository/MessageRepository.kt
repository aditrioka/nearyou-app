package id.nearyou.app.repository

import id.nearyou.app.domain.Message
import id.nearyou.app.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface MessageRepository : JpaRepository<Message, UUID> {
    
    @Query("""
        SELECT m FROM Message m 
        WHERE (m.sender = :user1 AND m.receiver = :user2) 
        OR (m.sender = :user2 AND m.receiver = :user1) 
        AND m.isDeleted = false
        ORDER BY m.sentAt DESC
    """)
    fun findConversation(user1: User, user2: User, pageable: Pageable): Page<Message>
    
    @Query("""
        SELECT m FROM Message m 
        WHERE m.receiver = :user 
        AND m.readAt IS NULL 
        AND m.isDeleted = false
    """)
    fun findUnreadMessagesForUser(user: User): List<Message>
    
    @Query("""
        SELECT DISTINCT 
            CASE 
                WHEN m.sender = :user THEN m.receiver 
                ELSE m.sender 
            END
        FROM Message m 
        WHERE (m.sender = :user OR m.receiver = :user) 
        AND m.isDeleted = false
        ORDER BY MAX(m.sentAt) DESC
    """)
    fun findRecentConversationPartners(user: User, pageable: Pageable): Page<User>
}

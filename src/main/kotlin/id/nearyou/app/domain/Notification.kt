package id.nearyou.app.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

enum class NotificationType {
    LIKE, COMMENT, FOLLOW, MESSAGE, NEARBY
}

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false, length = 500)
    val content: String,

    @Column(nullable = true)
    val relatedEntityId: UUID? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var readAt: LocalDateTime? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false
)
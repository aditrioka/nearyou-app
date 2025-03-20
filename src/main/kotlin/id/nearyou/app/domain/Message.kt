package id.nearyou.app.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "messages")
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @Column(nullable = false, length = 1000)
    val content: String,

    @Column(nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var readAt: LocalDateTime? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false
)
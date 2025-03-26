package id.nearyou.app.service

import id.nearyou.app.domain.Message
import id.nearyou.app.domain.User
import id.nearyou.app.repository.MessageRepository
import id.nearyou.app.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {

    fun findById(id: UUID): Message? {
        val message = messageRepository.findById(id).orElse(null)
        return if (message?.isDeleted == false) message else null
    }
    
    fun getConversation(user1Id: UUID, user2Id: UUID, pageable: Pageable): Page<Message> {
        val user1 = userRepository.findById(user1Id)
            .orElseThrow { NoSuchElementException("User not found with ID: $user1Id") }
        
        val user2 = userRepository.findById(user2Id)
            .orElseThrow { NoSuchElementException("User not found with ID: $user2Id") }
            
        return messageRepository.findConversation(user1, user2, pageable)
    }
    
    fun getUnreadMessages(userId: UUID): List<Message> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
            
        return messageRepository.findUnreadMessagesForUser(user)
    }
    
    fun getRecentConversations(userId: UUID, pageable: Pageable): Page<User> {
        val user = userRepository.findById(userId)
            .orElseThrow { NoSuchElementException("User not found with ID: $userId") }
            
        return messageRepository.findRecentConversationPartners(user, pageable)
    }
    
    @Transactional
    fun sendMessage(senderId: UUID, receiverId: UUID, content: String): Message {
        val sender = userRepository.findById(senderId)
            .orElseThrow { NoSuchElementException("Sender not found with ID: $senderId") }
        
        val receiver = userRepository.findById(receiverId)
            .orElseThrow { NoSuchElementException("Receiver not found with ID: $receiverId") }
        
        val message = Message(
            sender = sender,
            receiver = receiver,
            content = content
        )
        
        return messageRepository.save(message)
    }
    
    @Transactional
    fun markAsRead(messageId: UUID, userId: UUID) {
        val message = messageRepository.findById(messageId)
            .orElseThrow { NoSuchElementException("Message not found with ID: $messageId") }
        
        if (message.receiver.id != userId) {
            throw IllegalStateException("Not authorized to mark this message as read")
        }
        
        if (message.readAt == null) {
            val updatedMessage = message.copy(
                readAt = LocalDateTime.now()
            )
            
            messageRepository.save(updatedMessage)
        }
    }
    
    @Transactional
    fun deleteMessage(messageId: UUID, userId: UUID) {
        val message = messageRepository.findById(messageId)
            .orElseThrow { NoSuchElementException("Message not found with ID: $messageId") }
        
        if (message.sender.id != userId && message.receiver.id != userId) {
            throw IllegalStateException("Not authorized to delete this message")
        }
        
        val updatedMessage = message.copy(
            isDeleted = true
        )
        
        messageRepository.save(updatedMessage)
    }
}

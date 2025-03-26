package id.nearyou.app.security

import id.nearyou.app.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }
        
        if (!user.active) {
            throw UsernameNotFoundException("User account is deactivated")
        }
        
        return User.builder()
            .username(user.username)
            .password(user.passwordHash)
            .authorities(SimpleGrantedAuthority("USER"))
            .build()
    }
}

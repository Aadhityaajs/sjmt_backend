package com.sjmt.SJMT.Security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sjmt.SJMT.Entity.UserEntity;
import com.sjmt.SJMT.Entity.UserStatusEnum;
import com.sjmt.SJMT.Repository.UserRepository;

/**
 * Custom UserDetailsService for Spring Security
 * @author SJMT Team
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail)
            );
        
        // Check if user is active
        if (user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new UsernameNotFoundException("User account is blocked");
        }
        
        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new UsernameNotFoundException("Email not verified. Please verify your email first.");
        }
        
        return new User(
            user.getUsername(),
            user.getPassword(),
            user.getStatus() == UserStatusEnum.ACTIVE,
            true,
            true,
            true,
            getAuthorities(user)
        );
    }
    
    /**
     * Get authorities based on user role and privileges
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UserEntity user) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add role
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        // Add privileges
        authorities.add(new SimpleGrantedAuthority("PRIVILEGE_" + user.getPrivileges().name()));
        
        return authorities;
    }
}
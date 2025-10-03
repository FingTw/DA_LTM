package com.allies.app.model;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "Taikhoan") // Ánh xạ tới Taikhoan
public class Account implements UserDetails {
    
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MA_TK")
    private Integer id; 

    @Column(name = "TEN_DN", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "MK", nullable = false)
    private String password; 
    
    @Column(name = "AVARTA")
    private String avarta;

    // --- Implement UserDetails Methods ---

   

    // --- Các phương thức của UserDetails ---
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
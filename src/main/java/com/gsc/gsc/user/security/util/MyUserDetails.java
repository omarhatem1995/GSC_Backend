package com.gsc.gsc.user.security.util;

import com.gsc.gsc.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class MyUserDetails implements UserDetails {

    private String userName;
    private String password;
    private int userId;
    private int customerType;
    private boolean active;
    private Collection<? extends GrantedAuthority> authorities;

    public MyUserDetails(User user) {
        this.userName = user.getName();
        this.password = user.getPassword();
        this.userId = user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
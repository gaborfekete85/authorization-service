package com.crimelist.crime.security;

import com.crimelist.crime.model.ERole;
import com.crimelist.crime.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserPrincipal implements OAuth2User, UserDetails {
    private final UUID id;
    private final String email;
    private final String password;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;
    private final List<ERole> roles;
    private Map<String, Object> attributes;

    public UserPrincipal(UUID id, String email, boolean emailVerified, String password, Collection<? extends GrantedAuthority> authorities, List<ERole> roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.roles = roles;
        this.emailVerified = emailVerified;
    }

    public static UserPrincipal create(User user) {
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER, ROLE_MODERATOR, ROLE_ADMIN"));
        List<GrantedAuthority> authorities = user.getRoles().stream().map( x -> new SimpleGrantedAuthority(x.getName().name())).collect(Collectors.toList());
        //List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_MODERATOR"), new SimpleGrantedAuthority("ROLE_ADMIN"));
        List<ERole> roles = user.getRoles().stream().map( x -> x.getName()).collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getEmailVerified(),
                user.getPassword(),
                authorities,
                roles
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
        return emailVerified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    public List<ERole> getRoles() {
        return roles;
    }
}

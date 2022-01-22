package com.crimelist.crime.controller;

import com.crimelist.crime.controller.twogether.UsersRequest;
import com.crimelist.crime.controller.util.SecurityUtil;
import com.crimelist.crime.model.ERole;
import com.crimelist.crime.model.Role;
import com.crimelist.crime.payload.UpdateImageRequest;
import com.crimelist.crime.payload.UpdateRoleRequest;
import com.crimelist.crime.repository.RoleRepository;
import com.crimelist.crime.repository.UserRepository;
import com.crimelist.crime.security.CurrentUser;
import com.crimelist.crime.exception.ResourceNotFoundException;
import com.crimelist.crime.model.User;
import com.crimelist.crime.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/test")
    public String test() {
        return "Hello World";
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        return user;
    }

    @GetMapping("/check_token")
    @PreAuthorize("hasRole('USER')")
    public User checkToken(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public User findById(@PathVariable String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @PostMapping("/user/find")
    @PreAuthorize("hasRole('USER')")
    public List<User> findByIds(@RequestBody List<UUID> userIds) {
        return userRepository.findByIds(userIds);
    }

    @PostMapping("/user/update-image")
    @PreAuthorize("hasRole('USER')")
    public User updateImage(Principal principal, @RequestBody UpdateImageRequest updateImageRequest) {
        UUID userId = SecurityUtil.getUserId(principal);
        User user = userRepository.findById(userId).get();
        user.setImageUrl(updateImageRequest.getImageUrl());
        return userRepository.save(user);
    }

    @PostMapping("/user/update-role")
    @PreAuthorize("hasRole('MODERATOR')")
    public User updateUserRols(Principal principal, @RequestBody UpdateRoleRequest updateRoleRequest) {
        UUID userId = updateRoleRequest.getUserId();
        User user = userRepository.findById(userId).get();
        List<ERole> eroles = collectRoles(updateRoleRequest.getRole());
        user.setRoles(new HashSet<>());
        user.setRoles(eroles.stream().map(x -> roleRepository.findByName(x)).collect(Collectors.toSet()));
        return userRepository.save(user);
    }

    private List<ERole> collectRoles(ERole role) {
        if(ERole.ROLE_USER.equals(role)) {
            return List.of(ERole.ROLE_USER);
        }
        if(ERole.ROLE_SERVICE.equals(role)) {
            return List.of(ERole.ROLE_USER, ERole.ROLE_SERVICE);
        }
        if(ERole.ROLE_MODERATOR.equals(role)) {
            return List.of(ERole.ROLE_USER, ERole.ROLE_SERVICE, ERole.ROLE_MODERATOR);
        }
        if(ERole.ROLE_ADMIN.equals(role)) {
            return List.of(ERole.ROLE_USER, ERole.ROLE_SERVICE, ERole.ROLE_MODERATOR, ERole.ROLE_ADMIN);
        }
        return List.of();
    }
}

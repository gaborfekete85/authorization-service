package com.crimelist.crime.controller.util;

import com.crimelist.crime.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.security.Principal;
import java.util.UUID;

public class SecurityUtil {

    public static UUID getUserId(final Principal principal) {
        return ((UserPrincipal) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getId();
    }
}
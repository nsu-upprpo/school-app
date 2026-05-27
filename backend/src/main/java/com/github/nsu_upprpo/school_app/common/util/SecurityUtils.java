package com.github.nsu_upprpo.school_app.common.util;

import com.github.nsu_upprpo.school_app.security.UserDetailsImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserDetailsImpl getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return userDetails;
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
}

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
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new AccessDeniedException("User does not authenticated");
        }
        return (UserDetailsImpl) auth.getPrincipal();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
}

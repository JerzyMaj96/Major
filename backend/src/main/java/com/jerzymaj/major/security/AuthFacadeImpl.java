package com.jerzymaj.major.security;

import com.jerzymaj.major.models.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthFacadeImpl implements AuthFacade {
    @Override
    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}

package com.jerzymaj.major.security;

import com.jerzymaj.major.models.User;

public interface AuthFacade {
    User getCurrentUser();
}

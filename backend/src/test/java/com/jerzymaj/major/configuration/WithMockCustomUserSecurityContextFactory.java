package com.jerzymaj.major.configuration;

import com.jerzymaj.major.models.User;
import com.jerzymaj.major.models.enums.UserRole;
import com.jerzymaj.major.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        User user = userRepository.findByName(annotation.username())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(annotation.username());
                    newUser.setEmail(annotation.username() + "@mail.com");
                    newUser.setPassword("password");
                    newUser.setRole(UserRole.USER);
                    return userRepository.save(newUser);
                });

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}

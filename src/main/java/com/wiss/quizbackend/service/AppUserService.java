package com.wiss.quizbackend.service;

import com.wiss.quizbackend.entity.AppUser;
import com.wiss.quizbackend.entity.Role;
import com.wiss.quizbackend.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class AppUserService {
    private final static Pattern emailPattern = Pattern.compile("[a-z]+@[a-z]+\\.[a-z]+", Pattern.CASE_INSENSITIVE);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser registerUser(String username, String email, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException();
        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException();

        String hashedPassword = passwordEncoder.encode(rawPassword);

        AppUser user = new AppUser(username, email, hashedPassword, role);

        userRepository.save(user);
        return user;
    }

    public Optional<AppUser> findByUsername(String username) { return userRepository.findByUsername(username); }

    public Optional<AppUser> findByEmail(String email) { return userRepository.findByEmail(email); }

    public Optional<AppUser> authenticateUser(String username, String rawPassword) {
        Optional<AppUser> user = findByUsername(username);

        if (user.isPresent()) {
            if (passwordEncoder.matches(rawPassword, user.get().getPassword())) {
                return user;
            }
        }

        return Optional.empty();
    }

    public boolean isValidEmail(String email) {
        Matcher m = emailPattern.matcher(email);
        return m.find();
    }

    public boolean isUsernameAvailable(String username) { return !userRepository.existsByUsername(username); }
    public boolean isEmailAvailable(String email) { return !userRepository.existsByEmail(email); }
}

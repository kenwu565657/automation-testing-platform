package com.platform.testing.domain.user.repository;

import com.platform.testing.domain.user.User;
import com.platform.testing.domain.user.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    void deleteById(UserId id);
}

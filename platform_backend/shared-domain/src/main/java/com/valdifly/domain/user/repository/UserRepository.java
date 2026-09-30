package com.valdifly.domain.user.repository;

import com.valdifly.domain.user.User;
import com.valdifly.domain.user.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    void deleteById(UserId id);
}

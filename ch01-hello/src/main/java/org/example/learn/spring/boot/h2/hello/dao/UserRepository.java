package org.example.learn.spring.boot.h2.hello.dao;

import org.example.learn.spring.boot.h2.hello.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

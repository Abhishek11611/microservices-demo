package com.example.demo.repositories.users;

import com.example.demo.entities.users.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUsersRepository extends JpaRepository<AuthUser,Long> {

    boolean existsByUserCode(String userCode);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String s);
}

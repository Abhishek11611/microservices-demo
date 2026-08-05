package com.example.demo.repositories;

import com.example.demo.entities.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users,Long> {

    boolean existsByUserCode(String userCode);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String s);
}

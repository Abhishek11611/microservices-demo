package com.example.userservice.repositories;

import com.example.userservice.entities.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users,Long> {

    boolean existsByUserCode(String userCode);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String s);
}

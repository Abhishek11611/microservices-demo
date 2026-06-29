package com.example.demo.repositories;

import com.example.demo.entities.users.AuthUsers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUsersRepository extends JpaRepository<AuthUsers,Long> {
}

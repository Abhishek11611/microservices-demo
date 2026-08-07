package com.example.demo.repositories;

import com.example.demo.entities.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUsersRepository extends JpaRepository<Users,Long> {
}

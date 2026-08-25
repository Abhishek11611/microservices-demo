package com.example.demo.repositories.users;

import com.example.demo.entities.users.Role;
import com.example.demo.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Role,Long> {

    Optional<Role> findByNameAndStatus(String role, Status status);
    
}

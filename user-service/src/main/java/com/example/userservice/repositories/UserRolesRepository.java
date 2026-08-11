package com.example.userservice.repositories;

import com.example.userservice.entities.users.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRolesRepository extends JpaRepository<UserRoles,Long> {

}

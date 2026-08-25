package com.example.demo.service.onboard;

import com.example.commoncore.exception.NotFoundException;
import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.entities.users.Role;
import com.example.demo.entities.users.UserRoles;
import com.example.demo.enums.Status;
import com.example.demo.repositories.users.AuthUsersRepository;
import com.example.demo.repositories.users.RolesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthUserService {

    private final AuthUsersRepository authUsersRepository;
    private final RolesRepository rolesRepository;

    public AuthUserService(AuthUsersRepository authUsersRepository, RolesRepository rolesRepository) {
        this.authUsersRepository = authUsersRepository;
        this.rolesRepository = rolesRepository;
    }

    public AuthUser createUser(RegistrationDTO dto) {

        AuthUser authUser = new AuthUser();

        authUser.setUserCode(dto.getUserCode());
        authUser.setFirstName(dto.getFirstName());
        authUser.setLastName(dto.getLastName());
        authUser.setDateOfBirth(dto.getDateOfBirth());
        authUser.setEmail(dto.getEmail());
        authUser.setMobileNumber(dto.getMobileNumber());
        authUser.setPasswordHash(dto.getPasswordHash());

        Role role = rolesRepository
                .findByNameAndStatus("USER", Status.ACTIVE)
                .orElseThrow(() ->
                        new NotFoundException("User Role Not Found")
                );

        UserRoles userRole = new UserRoles();
        userRole.setRole(role);
        userRole.setAuthUser(authUser);

        authUser.setUserRoles(
                List.of(userRole)
        );

        return authUsersRepository.save(authUser);
    }
}

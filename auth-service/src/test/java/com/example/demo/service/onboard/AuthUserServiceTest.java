package com.example.demo.service.onboard;

import com.example.demo.exceptions.NotFoundException;
import com.example.demo.dtos.registration.RegistrationDTO;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.entities.users.Role;
import com.example.demo.entities.users.UserRoles;
import com.example.demo.enums.Status;
import com.example.demo.repositories.users.AuthUsersRepository;
import com.example.demo.repositories.users.RolesRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceTest {

    @Mock
    private AuthUsersRepository authUsersRepository;

    @Mock
    private RolesRepository rolesRepository;

    @InjectMocks
    private AuthUserService authUserService;

    @Test
    void createUser_mapsRegistrationDataAndAssignsActiveUserRole() {
        RegistrationDTO dto = registrationDto();
        Role role = new Role(1L, "USER");
        when(rolesRepository.findByNameAndStatus("USER", Status.ACTIVE)).thenReturn(Optional.of(role));
        when(authUsersRepository.save(any(AuthUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthUser result = authUserService.createUser(dto);

        ArgumentCaptor<AuthUser> userCaptor = ArgumentCaptor.forClass(AuthUser.class);
        verify(authUsersRepository).save(userCaptor.capture());
        AuthUser savedUser = userCaptor.getValue();
        assertThat(savedUser).isSameAs(result);
        assertThat(savedUser.getUserCode()).isEqualTo("UABC123");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getMobileNumber()).isEqualTo("9999999999");
        assertThat(savedUser.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.getUserRoles()).hasSize(1);
        UserRoles userRole = savedUser.getUserRoles().getFirst();
        assertThat(userRole.getRole()).isSameAs(role);
        assertThat(userRole.getAuthUser()).isSameAs(savedUser);
    }

    @Test
    void createUser_whenActiveUserRoleDoesNotExist_throwsNotFoundException() {
        RegistrationDTO dto = registrationDto();
        when(rolesRepository.findByNameAndStatus("USER", Status.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUserService.createUser(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User Role Not Found");

        verify(authUsersRepository, org.mockito.Mockito.never()).save(any(AuthUser.class));
    }

    @Test
    void createUser_whenSaveFails_propagatesException() {
        RegistrationDTO dto = registrationDto();
        Role role = new Role(1L, "USER");
        when(rolesRepository.findByNameAndStatus("USER", Status.ACTIVE)).thenReturn(Optional.of(role));
        when(authUsersRepository.save(any(AuthUser.class))).thenThrow(new RuntimeException("database down"));

        assertThatThrownBy(() -> authUserService.createUser(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("database down");
    }

    @Test
    void createUser_withNullDto_throwsNullPointerException() {
        assertThatThrownBy(() -> authUserService.createUser(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(authUsersRepository, rolesRepository);
    }

    private RegistrationDTO registrationDto() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUserCode("UABC123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setMobileNumber("9999999999");
        dto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        dto.setPasswordHash("encoded-password");
        return dto;
    }
}

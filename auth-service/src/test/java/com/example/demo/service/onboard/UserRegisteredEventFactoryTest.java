package com.example.demo.service.onboard;

import com.example.demo.dtos.events.UserRegisteredEvent;
import com.example.demo.entities.users.AuthUser;
import com.example.demo.entities.users.Role;
import com.example.demo.entities.users.UserRoles;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRegisteredEventFactoryTest {

    private final UserRegisteredEventFactory eventFactory = new UserRegisteredEventFactory();

    @Test
    void create_mapsUserFieldsAndRoleNames() {
        AuthUser user = userWithRoles("USER", "MANAGER");

        UserRegisteredEvent event = eventFactory.create(user);

        assertThat(event.userCode()).isEqualTo("UABC123");
        assertThat(event.firstName()).isEqualTo("John");
        assertThat(event.lastName()).isEqualTo("Doe");
        assertThat(event.email()).isEqualTo("john@example.com");
        assertThat(event.mobileNumber()).isEqualTo("9999999999");
        assertThat(event.dateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(event.role()).containsExactly("USER", "MANAGER");
    }

    @Test
    void create_withNoRoles_returnsEmptyRoleList() {
        AuthUser user = userWithRoles();

        UserRegisteredEvent event = eventFactory.create(user);

        assertThat(event.role()).isEmpty();
    }

    @Test
    void create_withNullUser_throwsNullPointerException() {
        assertThatThrownBy(() -> eventFactory.create(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void create_whenUserRolesAreNull_throwsNullPointerException() {
        AuthUser user = userWithRoles();
        user.setUserRoles(null);

        assertThatThrownBy(() -> eventFactory.create(user))
                .isInstanceOf(NullPointerException.class);
    }

    private AuthUser userWithRoles(String... roleNames) {
        AuthUser user = new AuthUser();
        user.setUserCode("UABC123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setMobileNumber("9999999999");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        List<UserRoles> userRoles = new ArrayList<>();
        for (String roleName : roleNames) {
            UserRoles userRole = new UserRoles();
            userRole.setRole(new Role(1L, roleName));
            userRole.setAuthUser(user);
            userRoles.add(userRole);
        }
        user.setUserRoles(userRoles);
        return user;
    }
}

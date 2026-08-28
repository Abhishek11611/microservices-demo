package com.example.userservice.service.registration;

import com.example.commoncore.exception.NotFoundException;
import com.example.userservice.dtos.RegistrationDTO;
import com.example.userservice.entities.users.Role;
import com.example.userservice.entities.users.UserRoles;
import com.example.userservice.entities.users.Users;
import com.example.userservice.enums.RegistrationStatus;
import com.example.userservice.repositories.RolesRepository;
import com.example.userservice.repositories.UserRolesRepository;
import com.example.userservice.repositories.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class UsersEventServiceImpl implements UsersEventService{

    private final ObjectMapper objectMapper;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;

    public UsersEventServiceImpl(ObjectMapper objectMapper, UsersRepository usersRepository, RolesRepository rolesRepository, UserRolesRepository userRolesRepository) {
        this.objectMapper = objectMapper;
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.userRolesRepository = userRolesRepository;
    }

    @KafkaListener(
            topics = "user-registration", groupId = "user-group1"
    )
    @Override
    public void saveUsers(String json) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(json);

        JsonNode payload = jsonNode.get("payload");

        String userCode = payload.get("userCode").asText();
        String firstName = payload.get("firstName").asText();
        String lastName = payload.get("lastName").asText();
        String email = payload.get("email").asText();
        String mobileNumber = payload.get("mobileNumber").asText();
        String dateOfBirth = payload.get("dateOfBirth").asText();

        LocalDate dob = LocalDate.parse(dateOfBirth);

        Users users = new Users();
        users.setUserCode(userCode);
        users.setFirstName(firstName);
        users.setLastName(lastName);
        users.setEmail(email);
        users.setMobileNumber(mobileNumber);
        users.setDateOfBirth(dob);

        String roleName = "USER";
        Role role = rolesRepository.findByName(roleName).orElseThrow(() -> new NotFoundException(roleName + " role not found"));

         UserRoles userRoles = new UserRoles();
        userRoles.setRole(role);
        userRoles.setUsers(users);

        users.getUserRoles().add(userRoles);

        usersRepository.save(users);
    }
}

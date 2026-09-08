package com.example.userservice.service.registration;

import com.example.commoncore.exception.NotFoundException;
import com.example.userservice.dtos.RegistrationDTO;
import com.example.userservice.entities.events.UserEvents;
import com.example.userservice.entities.users.Role;
import com.example.userservice.entities.users.UserRoles;
import com.example.userservice.entities.users.Users;
import com.example.userservice.enums.RegistrationStatus;
import com.example.userservice.repositories.RolesRepository;
import com.example.userservice.repositories.UserRolesRepository;
import com.example.userservice.repositories.UsersRepository;
import com.example.userservice.repositories.events.UserEventsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UsersEventServiceImpl implements UsersEventService{

    private final ObjectMapper objectMapper;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;
    private final UserEventsRepository userEventsRepository;

    public UsersEventServiceImpl(ObjectMapper objectMapper, UsersRepository usersRepository, RolesRepository rolesRepository, UserRolesRepository userRolesRepository, UserEventsRepository userEventsRepository) {
        this.objectMapper = objectMapper;
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.userRolesRepository = userRolesRepository;
        this.userEventsRepository = userEventsRepository;
    }

    @KafkaListener(
            topics = "user-registration", groupId = "user-group1",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    @Override
    public void saveUsers(String json, Acknowledgment acknowledgment) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(json);

        String eventType = jsonNode.get("eventType").asText();
        if (!"USER_REGISTERED".equals(eventType)){
            return;
        }

        String eventId = jsonNode.get("eventId").asText();

        // Ensure idempotency: skip already processed events
        if(userEventsRepository.existsByEventId(eventId)){
          String eventKey =   jsonNode.get("eventKey").asText();
            System.err.println("!!!Already Process = "+eventKey);
            return;
        }

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

        JsonNode rolesNode = payload.get("role");

        List<UserRoles> userRolesList = new ArrayList<>();
        if (rolesNode!=null && rolesNode.isArray()){

            for (JsonNode node: rolesNode){

                UserRoles userRoles = new UserRoles();

                String roleName = node.asText();
                 Role role =  rolesRepository.findByName(roleName)
                         .orElseThrow(() -> new NotFoundException(roleName + " role not found"));

                userRoles.setRole(role);
                userRoles.setUsers(users);

                userRolesList.add(userRoles);
            }
        }

        users.setUserRoles(userRolesList);
        usersRepository.save(users);

        UserEvents userEvents = new UserEvents();

        userEvents.setEventId(eventId);
        userEvents.setEventKey(jsonNode.get("eventKey").asText());
        userEvents.setEventType(eventType);
        userEvents.setRequestPayload(jsonNode);
        userEventsRepository.save(userEvents);

        acknowledgment.acknowledge();

    }
}

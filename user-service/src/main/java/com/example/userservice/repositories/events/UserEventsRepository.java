package com.example.userservice.repositories.events;

import com.example.userservice.entities.events.UserEvents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventsRepository extends JpaRepository<UserEvents,Long> {

    boolean existsByEventId(String eventId);
}

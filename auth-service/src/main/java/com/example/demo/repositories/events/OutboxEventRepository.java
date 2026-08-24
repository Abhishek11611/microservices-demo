package com.example.demo.repositories.events;

import com.example.demo.entities.events.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent,Long> {

}

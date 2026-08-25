package com.example.demo.repositories.events;

import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent,Long> {

    List<OutboxEvent> findByStatusInAndPublishedAtIsNullAndRetryCountLessThanOrderByIdAsc(
            List<OutboxEventStatus> outboxEventStatuses, Integer retryCount);




}

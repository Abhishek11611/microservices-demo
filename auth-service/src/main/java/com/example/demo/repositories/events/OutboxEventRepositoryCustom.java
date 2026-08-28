package com.example.demo.repositories.events;

import com.example.demo.entities.events.OutboxEvent;

import java.util.List;

public interface OutboxEventRepositoryCustom {

    List<OutboxEvent> claimPendingEvents(int batchSize,int maxRetryCount);

}

package com.example.demo.repositories.events;

import com.example.demo.entities.events.OutboxEvent;
import com.example.demo.enums.OutboxEventStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepositoryCustom{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<OutboxEvent> claimPendingEvents(
            int batchSize,
            int maxRetryCount
    ) {

        // FOR UPDATE SKIP LOCKED Line this if for multiple Instance dupication event handle
        List<OutboxEvent> events = entityManager.createNativeQuery("""
                SELECT *
                FROM outbox_events
                WHERE status IN ('PENDING', 'FAILED')
                  AND published_at IS NULL
                  AND retry_count < :maxRetryCount
                ORDER BY id
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """, OutboxEvent.class)
                .setParameter("maxRetryCount", maxRetryCount)
                .setParameter("batchSize", batchSize)
                .getResultList();


        for (OutboxEvent event : events) {

            event.setStatus(OutboxEventStatus.PROCESSING);
        }

        entityManager.flush();

        return events;
    }
}

ALTER TABLE outbox_events
DROP CONSTRAINT chk_outbox_events_status;

ALTER TABLE outbox_events
    ADD CONSTRAINT chk_outbox_events_status
        CHECK (
            status IN (
                       'PENDING',
                       'PROCESSING',
                       'PUBLISHED',
                       'FAILED'
                )
            );

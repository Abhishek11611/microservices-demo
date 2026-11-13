CREATE TABLE admin_setting (

   setting_key VARCHAR(100) PRIMARY KEY,

   value JSONB NOT NULL,

   value_type VARCHAR(20) NOT NULL,

   module VARCHAR(40) NOT NULL,

   description VARCHAR(500),

   version INT NOT NULL DEFAULT 1,

   updated_by VARCHAR(100) NOT NULL,

   updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

   CONSTRAINT chk_admin_setting_type
       CHECK (value_type IN (
                             'NUMBER',
                             'STRING',
                             'BOOLEAN',
                             'LIST',
                             'OBJECT'
           ))
);


CREATE TABLE admin_setting_audit (

     id BIGSERIAL PRIMARY KEY,

     setting_key VARCHAR(100) NOT NULL,

     old_value JSONB,

     new_value JSONB NOT NULL,

     changed_by VARCHAR(100) NOT NULL,

     changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

     CONSTRAINT fk_admin_setting_audit
         FOREIGN KEY (setting_key)
             REFERENCES admin_setting(setting_key)
);


INSERT INTO admin_setting
(setting_key, value, value_type, module, description, updated_by)
VALUES

    (
        'claims.loss_ratio_threshold',
        '70',
        'NUMBER',
        'claims',
        'Loss ratio threshold',
        'SYSTEM'
    ),

    (
        'claims.t13_persistency_gate',
        '85',
        'NUMBER',
        'claims',
        'T13 persistency gate',
        'SYSTEM'
    ),

    (
        'pmd.max_tasks_per_day',
        '10',
        'NUMBER',
        'pmd',
        'Maximum tasks per day',
        'SYSTEM'
    ),

    (
        'recruitment.stall_nudge_hours',
        '48',
        'NUMBER',
        'recruitment',
        'Stall reminder hours',
        'SYSTEM'
    );
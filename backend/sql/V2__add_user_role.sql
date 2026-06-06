-- Epic 1.2: add user role column (admin seeded by DataInitializer on startup)
USE market;

ALTER TABLE sys_user
    ADD COLUMN role TINYINT NOT NULL DEFAULT 1 COMMENT '1=buyer 2=merchant 9=admin' AFTER status;

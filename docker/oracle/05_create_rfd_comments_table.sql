--
-- Run as rfgradteam_owner
-- then run 07_add_rfd_comments_privilege_rfgradteam_users.sql as System to create
-- the required privileges for application user accounts
--
CREATE SEQUENCE rfd_comments_seq START WITH 1;

CREATE TABLE rfd_comments (
  COMMENT_ID                 NUMBER(20)      PRIMARY KEY,
  COMMENT_TIME               DATE            NOT NULL,
  USERNAME                   VARCHAR2(32)    NOT NULL,
  TOPIC                      VARCHAR2(128)   NOT NULL,
  COMMENT_STRING             VARCHAR2(2048)  NOT NULL
);

-- Then run 07_add_rfd_comments_privilege_rfgradteam_users.sql as System
-- to add permissions for read/rw users
-- This should be run as sysdba in order to update multiple users

-- Create a CACHE for the cavity data read from the CED web service.  Each call to that
-- takes a fraction of second, but we often end up making many calls over a date range.
-- Note: MOD_ANODE_HARVESTER_GSET_DATA isn't needed since it is already saved in the
--       database and is not from CED web service.
CREATE SEQUENCE rfgradteam_owner.cavity_cache_seq START WITH 1;
CREATE TABLE rfgradteam_owner.cavity_cache (
                                               CACHE_ID                   NUMBER(20)      PRIMARY KEY,
                                               QUERY_DATE                 DATE            NOT NULL,
                                               CAVITY_NAME                VARCHAR2(32)    NOT NULL,
                                               EPICS_NAME                 VARCHAR2(2048)  NOT NULL,
                                               MOD_ANODE_VOLTAGE          NUMBER(10, 6)   NOT NULL,
                                               CRYOMODULE_TYPE            VARCHAR2(2048)  NOT NULL,
                                               GSET                       NUMBER(10, 6),
                                               ODVH                       NUMBER(10, 6),
                                               Q0                         VARCHAR2(2048),
                                               QEXTERNAL                  VARCHAR2(2048),
                                               MAX_GSET                   NUMBER(10, 6) NOT NULL,
                                               OPS_GSET_MAX               NUMBER(10, 6),
                                               TRIP_OFFSET                NUMBER(12, 6),
                                               TRIP_SLOPE                 NUMBER(12, 6),
                                               LENGTH                     NUMBER(10, 6),
                                               BYPASSED                   NUMBER(1) NOT NULL,
                                               TUNER_BAD                  NUMBER(1) NOT NULL
);
CREATE INDEX cavity_date ON rfgradteam_owner.cavity_cache(QUERY_DATE, CAVITY_NAME);
ALTER TABLE rfgradteam_owner.cavity_cache ADD CONSTRAINT u_cavity_cache UNIQUE (QUERY_DATE, CAVITY_NAME);

-- Update read only
CREATE OR REPLACE SYNONYM rfgradteam_read.cavity_cache FOR rfgradteam_owner.cavity_cache;
GRANT SELECT ON rfgradteam_owner.cavity_cache TO rfgradteam_read;

-- Update read/write user
CREATE OR REPLACE SYNONYM rfgradteam_rw.cavity_cache FOR rfgradteam_owner.cavity_cache;
GRANT SELECT ON rfgradteam_owner.cavity_cache TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.cavity_cache TO rfgradteam_rw;
GRANT DELETE ON rfgradteam_owner.cavity_cache TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.cavity_cache_seq TO rfgradteam_rw;
CREATE OR REPLACE SYNONYM rfgradteam_rw.cavity_cache_seq FOR rfgradteam_owner.cavity_cache_seq;

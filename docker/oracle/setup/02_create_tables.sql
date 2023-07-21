--
-- Run as rfgradteam_owner
--
-- DROP SEQUENCE lem_scan_seq;
-- DROP TABLE lem_scan;
--
-- DROP TABLE MOD_ANODE_HARVESTER_GSET;
-- DROP TABLE MOD_ANODE_HARVESTER_LINAC_SCAN;
-- DROP TABLE MOD_ANODE_HARVESTER_SCAN;
-- DROP SEQUENCE ma_harvester_linac_scan_seq;
-- DROP SEQUENCE ma_harvester_gset_seq;
-- DROP SEQUENCE ma_harvester_scan_seq;


ALTER SESSION SET CONTAINER=XEPDB1;

CREATE SEQUENCE rfgradteam_owner.lem_scan_seq START WITH 1;

CREATE TABLE rfgradteam_owner.lem_scan (
                          SCAN_ID                  NUMBER(20)    PRIMARY KEY,
                          START_LABEL              VARCHAR2(20),
                          START_TIME               DATE          NOT NULL,
                          END_TIME                 DATE          NOT NULL,
                          LINAC                    VARCHAR2(20)  NOT NULL,
                          TRIPS_PER_HOUR_950_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_955_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_960_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_965_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_970_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_975_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_980_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_985_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_990_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_995_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1000_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1005_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1010_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1015_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1020_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1025_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1030_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1035_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1040_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1045_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1050_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1055_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1060_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1065_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1070_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1075_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1080_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1085_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1090_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1095_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1100_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1105_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1110_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1115_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1120_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1125_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1130_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1135_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1140_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1145_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1150_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1155_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1160_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1165_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1170_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1175_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1180_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1185_MEV  NUMBER(28,8),
                          TRIPS_PER_HOUR_1190_MEV  NUMBER(28,8),
                          CRYO_WATTS_950_MEV  NUMBER(20,8),
                          CRYO_WATTS_955_MEV  NUMBER(20,8),
                          CRYO_WATTS_960_MEV  NUMBER(20,8),
                          CRYO_WATTS_965_MEV  NUMBER(20,8),
                          CRYO_WATTS_970_MEV  NUMBER(20,8),
                          CRYO_WATTS_975_MEV  NUMBER(20,8),
                          CRYO_WATTS_980_MEV  NUMBER(20,8),
                          CRYO_WATTS_985_MEV  NUMBER(20,8),
                          CRYO_WATTS_990_MEV  NUMBER(20,8),
                          CRYO_WATTS_995_MEV  NUMBER(20,8),
                          CRYO_WATTS_1000_MEV  NUMBER(20,8),
                          CRYO_WATTS_1005_MEV  NUMBER(20,8),
                          CRYO_WATTS_1010_MEV  NUMBER(20,8),
                          CRYO_WATTS_1015_MEV  NUMBER(20,8),
                          CRYO_WATTS_1020_MEV  NUMBER(20,8),
                          CRYO_WATTS_1025_MEV  NUMBER(20,8),
                          CRYO_WATTS_1030_MEV  NUMBER(20,8),
                          CRYO_WATTS_1035_MEV  NUMBER(20,8),
                          CRYO_WATTS_1040_MEV  NUMBER(20,8),
                          CRYO_WATTS_1045_MEV  NUMBER(20,8),
                          CRYO_WATTS_1050_MEV  NUMBER(20,8),
                          CRYO_WATTS_1055_MEV  NUMBER(20,8),
                          CRYO_WATTS_1060_MEV  NUMBER(20,8),
                          CRYO_WATTS_1065_MEV  NUMBER(20,8),
                          CRYO_WATTS_1070_MEV  NUMBER(20,8),
                          CRYO_WATTS_1075_MEV  NUMBER(20,8),
                          CRYO_WATTS_1080_MEV  NUMBER(20,8),
                          CRYO_WATTS_1085_MEV  NUMBER(20,8),
                          CRYO_WATTS_1090_MEV  NUMBER(20,8),
                          CRYO_WATTS_1095_MEV  NUMBER(20,8),
                          CRYO_WATTS_1100_MEV  NUMBER(20,8),
                          CRYO_WATTS_1105_MEV  NUMBER(20,8),
                          CRYO_WATTS_1110_MEV  NUMBER(20,8),
                          CRYO_WATTS_1115_MEV  NUMBER(20,8),
                          CRYO_WATTS_1120_MEV  NUMBER(20,8),
                          CRYO_WATTS_1125_MEV  NUMBER(20,8),
                          CRYO_WATTS_1130_MEV  NUMBER(20,8),
                          CRYO_WATTS_1135_MEV  NUMBER(20,8),
                          CRYO_WATTS_1140_MEV  NUMBER(20,8),
                          CRYO_WATTS_1145_MEV  NUMBER(20,8),
                          CRYO_WATTS_1150_MEV  NUMBER(20,8),
                          CRYO_WATTS_1155_MEV  NUMBER(20,8),
                          CRYO_WATTS_1160_MEV  NUMBER(20,8),
                          CRYO_WATTS_1165_MEV  NUMBER(20,8),
                          CRYO_WATTS_1170_MEV  NUMBER(20,8),
                          CRYO_WATTS_1175_MEV  NUMBER(20,8),
                          CRYO_WATTS_1180_MEV  NUMBER(20,8),
                          CRYO_WATTS_1185_MEV  NUMBER(20,8),
                          CRYO_WATTS_1190_MEV  NUMBER(20,8),
                          COMMENTS             NVARCHAR2(1024),
                          CONSTRAINT linac_name CHECK (linac IN ('Injector', 'North', 'South')),
                          CONSTRAINT start_labels CHECK (start_label IN ('12AM', '6AM', '12PM', '6PM'))
);


CREATE SEQUENCE rfgradteam_owner.ma_harvester_scan_seq START WITH 1;
CREATE SEQUENCE rfgradteam_owner.ma_harvester_linac_scan_seq START WITH 1;
CREATE SEQUENCE rfgradteam_owner.ma_harvester_gset_seq START WITH 1;

CREATE TABLE rfgradteam_owner.MOD_ANODE_HARVESTER_SCAN (
                                          SCAN_ID                      NUMBER(20)    PRIMARY KEY,
                                          START_LABEL                  VARCHAR2(20)  CHECK (START_LABEL in ('12AM', '6AM', '12PM', '6PM')),
                                          EPICS_DATE                   DATE          NOT NULL,  -- Date of the EPICS PVs used in LEMSim
                                          START_TIME                   DATE          NOT NULL,  -- Start time of the scan
                                          END_TIME                     DATE          NOT NULL,  -- End time of the scan
                                          COMMENTS                     VARCHAR2(1024)
);

CREATE TABLE rfgradteam_owner.MOD_ANODE_HARVESTER_LINAC_SCAN (
                                                LINAC_SCAN_ID               NUMBER(20)     PRIMARY KEY,
                                                SCAN_ID                     NUMBER(20)     REFERENCES rfgradteam_owner.MOD_ANODE_HARVESTER_SCAN(SCAN_ID) ON DELETE CASCADE,
                                                LINAC                       VARCHAR2(10)   NOT NULL CHECK (LINAC IN ('North', 'South')),
                                                ENERGY_MEV                  NUMBER(5)      NOT NULL,
                                                TRIPS_PER_HOUR              NUMBER(15,8),
                                                TRIPS_PER_HOUR_NO_MAV       NUMBER(15,8)
);

CREATE TABLE rfgradteam_owner.MOD_ANODE_HARVESTER_GSET (
                                          GSET_ID               NUMBER(20)     PRIMARY KEY,
                                          SCAN_ID               NUMBER(20)     REFERENCES rfgradteam_owner.MOD_ANODE_HARVESTER_SCAN(SCAN_ID) ON DELETE CASCADE,
                                          CAVITY_EPICS          VARCHAR(4)     NOT NULL,
                                          ENERGY_MEV            NUMBER(5)      NOT NULL,
                                          GSET_MVPM             NUMBER(10,6),
                                          GSET_NO_MAV_MVPM      NUMBER(10,6),
                                          MOD_ANODE_KV          NUMBER(5,3)
);


CREATE SEQUENCE rfgradteam_owner.rfd_comments_seq START WITH 1;
CREATE TABLE rfgradteam_owner.rfd_comments (
                              COMMENT_ID                 NUMBER(20)      PRIMARY KEY,
                              COMMENT_TIME               DATE            NOT NULL,
                              USERNAME                   VARCHAR2(32)    NOT NULL,
                              TOPIC                      VARCHAR2(128)   NOT NULL,
                              COMMENT_STRING             VARCHAR2(2048)  NOT NULL
);

-- Create a CACHE for the cavity data read from the CED web service.  Each call to that
-- takes a fraction of second, but we often end up making many calls over a date range.
-- Note: MOD_ANODE_HARVESTER_GSET_DATA isn't needed since it is already saved in the
--       database and is not from CED web service.
-- Note: MOD_ANODE_VOLTAGE should be capped at 2 kV.  Number(10,6) gives a max 99.999999 kV for some wiggle room.
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

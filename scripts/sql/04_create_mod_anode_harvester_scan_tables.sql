DROP TABLE MOD_ANODE_HARVESTER_GSET;
DROP TABLE MOD_ANODE_HARVESTER_LINAC_SCAN;
DROP TABLE MOD_ANODE_HARVESTER_SCAN;
DROP SEQUENCE ma_harvester_linac_scan_seq;
DROP SEQUENCE ma_harvester_gset_seq;
DROP SEQUENCE ma_harvester_scan_seq;

CREATE SEQUENCE ma_harvester_scan_seq START WITH 1;
CREATE SEQUENCE ma_harvester_linac_scan_seq START WITH 1;
CREATE SEQUENCE ma_harvester_gset_seq START WITH 1;

CREATE TABLE MOD_ANODE_HARVESTER_SCAN (
  SCAN_ID                      NUMBER(20)    PRIMARY KEY,
  START_LABEL                  VARCHAR2(20)  CHECK (START_LABEL in ('12AM', '6AM', '12PM', '6PM')),
  EPICS_DATE                   DATE          NOT NULL,  -- Date of the EPICS PVs used in LEMSim
  START_TIME                   DATE          NOT NULL,  -- Start time of the scan
  END_TIME                     DATE          NOT NULL,  -- End time of the scan
  COMMENTS                     VARCHAR2(1024)
);

CREATE TABLE MOD_ANODE_HARVESTER_LINAC_SCAN (
  LINAC_SCAN_ID               NUMBER(20)     PRIMARY KEY,
  SCAN_ID                     NUMBER(20)     REFERENCES MOD_ANODE_HARVESTER_SCAN(SCAN_ID) ON DELETE CASCADE,
  LINAC                       VARCHAR2(10)   NOT NULL CHECK (LINAC IN ('North', 'South')),
  ENERGY_MEV                  NUMBER(5)      NOT NULL,
  TRIPS_PER_HOUR              BINARY_DOUBLE,
  TRIPS_PER_HOUR_NO_MAV       BINARY_DOUBLE
);

CREATE TABLE MOD_ANODE_HARVESTER_GSET (
  GSET_ID               NUMBER(20)     PRIMARY KEY,
  SCAN_ID               NUMBER(20)     REFERENCES MOD_ANODE_HARVESTER_SCAN(SCAN_ID) ON DELETE CASCADE,
  CAVITY_EPICS          VARCHAR(4)     NOT NULL,
  ENERGY_MEV            NUMBER(5)      NOT NULL,
  GSET_MVPM             NUMBER(10,6),
  GSET_NO_MAV_MVPM      NUMBER(10,6),
  MOD_ANODE_KV          NUMBER(5,3)
);
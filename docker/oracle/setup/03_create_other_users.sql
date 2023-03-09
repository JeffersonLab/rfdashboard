ALTER SESSION SET CONTAINER=XEPDB1;

-- Create users with read only and read/write permissions on the rfgradteam_owner schema

-- rfgradteam_read
-- UPDATE THIS PASSWORD
CREATE USER rfgradteam_read IDENTIFIED BY rfgradteam_read;
GRANT CREATE SESSION TO rfgradteam_read;
GRANT SELECT ON rfgradteam_owner.lem_scan TO rfgradteam_read;
GRANT CREATE SYNONYM TO rfgradteam_read;

CREATE OR REPLACE SYNONYM rfgradteam_read.lem_scan
  FOR rfgradteam_owner.lem_scan;
CREATE OR REPLACE SYNONYM rfgradteam_read.rfd_comments
  FOR rfgradteam_owner.rfd_comments;

-- Add additional reader permissions to for the new mod_anode_harvester_* tables
GRANT SELECT ON rfgradteam_owner.mod_anode_harvester_scan TO rfgradteam_read;
GRANT SELECT ON rfgradteam_owner.mod_anode_harvester_linac_scan TO rfgradteam_read;
GRANT SELECT ON rfgradteam_owner.mod_anode_harvester_gset TO rfgradteam_read;
GRANT SELECT ON rfgradteam_owner.rfd_comments TO rfgradteam_read;

-- Add the synonyms so we can access these tables using the shortname as this user
CREATE OR REPLACE SYNONYM rfgradteam_read.mod_anode_harvester_scan FOR rfgradteam_owner.mod_anode_harvester_scan;
CREATE OR REPLACE SYNONYM rfgradteam_read.mod_anode_harvester_linac_scan FOR rfgradteam_owner.mod_anode_harvester_linac_scan;
CREATE OR REPLACE SYNONYM rfgradteam_read.mod_anode_harvester_gset FOR rfgradteam_owner.mod_anode_harvester_gset;


-- rfgradteam_rw
-- UPDATE THIS PASSWORD
CREATE USER rfgradteam_rw IDENTIFIED BY rfgradteam_rw;
GRANT CREATE SESSION TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.lem_scan TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.lem_scan TO rfgradteam_rw;
GRANT CREATE SYNONYM TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.lem_scan_seq TO rfgradteam_rw;

CREATE OR REPLACE SYNONYM rfgradteam_rw.lem_scan
  FOR rfgradteam_owner.lem_scan;
CREATE OR REPLACE SYNONYM rfgradteam_rw.lem_scan_seq
  FOR rfgradteam_owner.lem_scan_seq;
CREATE OR REPLACE SYNONYM rfgradteam_rw.rfd_comments
  FOR rfgradteam_owner.rfd_comments;

    -- Add additional read/write permissions to for the new mod_anode_harvester_* tables
GRANT SELECT ON rfgradteam_owner.mod_anode_harvester_scan TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.mod_anode_harvester_scan TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.mod_anode_harvester_linac_scan TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.mod_anode_harvester_linac_scan TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.mod_anode_harvester_gset TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.mod_anode_harvester_gset TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.rfd_comments TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.rfd_comments TO rfgradteam_rw;

-- Add the synonyms so we can access these tables using the shortname as this user
CREATE OR REPLACE SYNONYM rfgradteam_rw.mod_anode_harvester_scan FOR rfgradteam_owner.mod_anode_harvester_scan;
CREATE OR REPLACE SYNONYM rfgradteam_rw.mod_anode_harvester_linac_scan FOR rfgradteam_owner.mod_anode_harvester_linac_scan;
CREATE OR REPLACE SYNONYM rfgradteam_rw.mod_anode_harvester_gset FOR rfgradteam_owner.mod_anode_harvester_gset;

-- Add additional SELECT permissions to for the new mod_anode_harvester_*_seq so we can do "good" updates
GRANT SELECT ON rfgradteam_owner.ma_harvester_scan_seq TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.ma_harvester_linac_scan_seq TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.ma_harvester_gset_seq TO rfgradteam_rw;

-- Add the synonyms so we can access these sequences using the shortname as this user
CREATE OR REPLACE SYNONYM rfgradteam_rw.ma_harvester_scan_seq FOR rfgradteam_owner.ma_harvester_scan_seq;
CREATE OR REPLACE SYNONYM rfgradteam_rw.ma_harvester_linac_scan_seq FOR rfgradteam_owner.ma_harvester_linac_scan_seq;
CREATE OR REPLACE SYNONYM rfgradteam_rw.ma_harvester_gset_seq FOR rfgradteam_owner.ma_harvester_gset_seq;

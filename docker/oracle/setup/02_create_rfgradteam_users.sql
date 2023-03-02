--
-- Run as System after lem_scan has been created
--

-- Create a user to display any data from the rfgradteam_owner schema
CREATE USER rfgradteam_read IDENTIFIED BY ;  -- UPDATE THIS PASSWORD
GRANT CREATE SESSION TO rfgradteam_read;
GRANT SELECT ON rfgradteam_owner.lem_scan TO rfgradteam_read;
GRANT CREATE SYNONYM TO rfgradteam_read;

CREATE OR REPLACE SYNONYM rfgradteam_read.lem_scan
  FOR rfgradteam_owner.lem_scan;

-- Create a user to display any data from the rfgradteam_owner schema
CREATE USER rfgradteam_rw IDENTIFIED BY ;   -- UPDATE THIS PASSWORD
GRANT CREATE SESSION TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.lem_scan TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.lem_scan TO rfgradteam_rw;
GRANT CREATE SYNONYM TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.lem_scan_seq TO rfgradteam_rw;

CREATE OR REPLACE SYNONYM rfgradteam_rw.lem_scan
  FOR rfgradteam_owner.lem_scan;
CREATE OR REPLACE SYNONYM rfgradteam_rw.lem_scan_seq
  FOR rfgradteam_owner.lem_scan_seq;

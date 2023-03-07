--
-- Run as System
--

ALTER SESSION SET CONTAINER=XEPDB1;

-- Create the table space for the data - the filename isn't necessary on dbo/dbd
--CREATE TABLESPACE rfgradteam DATAFILE SIZE 50M AUTOEXTEND ON NEXT 50M;
CREATE TABLESPACE rfgradteam DATAFILE 'rfgradteam.dbf' SIZE 50M AUTOEXTEND ON NEXT 50M;

-- Create the user - BUT UPDATE THE PASSWORD
CREATE USER rfgradteam_owner
  IDENTIFIED BY rfgradteam_owner
  DEFAULT TABLESPACE rfgradteam
  QUOTA UNLIMITED ON rfgradteam;
GRANT CREATE SESSION TO rfgradteam_owner;

-- Basic DDL  
GRANT CONNECT TO rfgradteam_owner;
GRANT CREATE SEQUENCE TO rfgradteam_owner;
GRANT CREATE TABLE TO rfgradteam_owner;
GRANT CREATE TRIGGER TO rfgradteam_owner;
GRANT CREATE SYNONYM TO rfgradteam_owner;

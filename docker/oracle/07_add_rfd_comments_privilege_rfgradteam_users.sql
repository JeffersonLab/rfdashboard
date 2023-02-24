--
-- Run as System after rfd_comments tables and rfd_comments_seq have been created
--

--
-- rfgradteam_read

-- Add additional reader permissions to for the rfd_comments table
GRANT SELECT ON rfgradteam_owner.rfd_comments TO rfgradteam_read;

-- Add the synonyms so we can access these tables using the shortname as this user
CREATE OR REPLACE SYNONYM rfgradteam_read.rfd_comments FOR rfgradteam_owner.rfd_comments;


--
-- rfgradteam_rw

-- Add additional read/write permissions to the read/write user
GRANT SELECT ON rfgradteam_owner.rfd_comments TO rfgradteam_rw;

-- Add the synonyms so we can access these tables using the shortname as this user
CREATE OR REPLACE SYNONYM rfgradteam_rw.rfd_comments FOR rfgradteam_owner.rfd_comments;

-- Add additional SELECT permissions to for the rfd_comments_seq so we can do "good" updates
GRANT SELECT ON rfgradteam_owner.rfd_comments TO rfgradteam_rw;
GRANT INSERT ON rfgradteam_owner.rfd_comments TO rfgradteam_rw;
GRANT SELECT ON rfgradteam_owner.rfd_comments_seq To rfgradteam_rw;

-- Add the synonyms so we can access these sequences using the shortname as this user
CREATE OR REPLACE SYNONYM rfgradteam_rw.rfd_comments FOR rfgradteam_owner.rfd_comments;
CREATE OR REPLACE SYNONYM rfgradteam_rw.rfd_comments_seq FOR rfgradteam_owner.rfd_comments_seq;

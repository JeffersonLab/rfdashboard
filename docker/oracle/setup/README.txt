These scripts create the application database schema for the rfdashboard app.  There are four steps.

####################### CHANGE PASSWORDS FOR PRODUCTION ######################
Make sure to change passwords for the users prior to production deployment.
##############################################################################

0. For production, comment out the 'ALTER SESSION SET CONTAINER=XEPDB1;' line.  It is used by the oracle docker image.
   Update the passwords set in the SQL files to match your users in the production database.

1. Create the main user/schema, rfgradteam_owner.  This contains all of the application tables and this user has
   unlimited permissions to these tables

2. Create the tables under the rfgradteam_owner schema

3. Create limited permission users, rfgradteam_read and rfgradteam_rw.  These allow read only and read/write
   permissions, respectively.

4. Optionally for test cases, the 100_data_import directory contains an import script and SQL*Loader data and control
   files that can load in a small snippet of data into an already created database.
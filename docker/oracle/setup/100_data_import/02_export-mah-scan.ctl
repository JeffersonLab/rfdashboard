load data
  infile './02_export-mah-scan.csv'
  into table rfgradteam_owner.MOD_ANODE_HARVESTER_SCAN
  fields terminated by "," optionally enclosed by '"'
  TRAILING NULLCOLS
  (SCAN_ID,
  START_LABEL,
  EPICS_DATE DATE 'DD-Mon-YYYY HH24:MI:SS',
  START_TIME DATE 'DD-Mon-YYYY HH24:MI:SS',
  END_TIME DATE 'DD-Mon-YYYY HH24:MI:SS',
  COMMENTS)

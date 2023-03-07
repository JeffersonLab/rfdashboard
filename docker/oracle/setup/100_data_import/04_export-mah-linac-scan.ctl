load data
  infile './04_export-mah-linac-scan.csv'
  into table rfgradteam_owner.MOD_ANODE_HARVESTER_LINAC_SCAN
  fields terminated by "," optionally enclosed by '"'
  TRAILING NULLCOLS
  (LINAC_SCAN_ID,SCAN_ID,LINAC,ENERGY_MEV,TRIPS_PER_HOUR,TRIPS_PER_HOUR_NO_MAV)


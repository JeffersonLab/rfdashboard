load data
  infile './03_export-mah-gset.csv'
  into table rfgradteam_owner.MOD_ANODE_HARVESTER_GSET
  fields terminated by "," optionally enclosed by '"'
  TRAILING NULLCOLS
  (GSET_ID, SCAN_ID, CAVITY_EPICS, ENERGY_MEV, GSET_MVPM, GSET_NO_MAV_MVPM, MOD_ANODE_KV)


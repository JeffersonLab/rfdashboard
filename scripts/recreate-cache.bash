#!/bin/bash

# This clears and recreates the cavity cache day-by-day over the last ~9.5 years.
# It takes a couple of seconds to create the cache for a given date, so it's 
# important that you don't clear the entire cache at once.

url_root='https://wildfly7.acc.jlab.org/RFDashboard'

# Cache start at 2016-01-01, which is 3450 days from today (2025-06-12)
for i in `seq 0 3450`
do
  d=$(date -I -d "$(date -I) -$i day")
  echo $d

  # Clear the cache
  echo wget "$url_root/ajax/cavity-cache?date=${d}&action=clear&secret=ayqs" -o /dev/null
  wget "$url_root/ajax/cavity-cache?date=${d}&action=clear&secret=ayqs" -o /dev/null

  # Query the cavity report which triggers the cache to be filled.
  echo wget "$url_root/ajax/cavity?date=${d}" -o /dev/null
  wget "$url_root/ajax/cavity?date=${d}" -o /dev/null
done

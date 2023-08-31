#!/bin/bash

# The RFDashboard caches cavity queries in a database as they take 10-20 seconds to complete.  Run this every morning to prime
#  the cache for that day.  Querying one week gives a small margin for error in case the wget fails or is not run.
/usr/bin/wget --quiet "https://ace.jlab.org/RFDashboard/ajax/cavity?end=`date +'%F'`&start=`date --date='1 week ago' +'%F'`&timeUnit=day" -O /dev/null
/usr/bin/wget --quiet "https://acctest.acc.jlab.org/RFDashboard/ajax/cavity?end=`date +'%F'`&start=`date --date='1 week ago' +'%F'`&timeUnit=day" -O /dev/null

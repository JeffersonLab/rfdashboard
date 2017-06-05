#!/bin/bash

# The RFDashboard caches cavity queries - need to update this every so often for acceptable performance
wget "https://accweb.acc.jlab.org/RFDashboard/ajax/cavity?end=`date +'%F'`&start=`date --date='1 year ago' +'%F'`&timeUnit=day" -O /dev/null
wget "https://accwebtest.acc.jlab.org/RFDashboard/ajax/cavity?end=`date +'%F'`&start=`date --date='1 year ago' +'%F'`&timeUnit=day" -O /dev/null
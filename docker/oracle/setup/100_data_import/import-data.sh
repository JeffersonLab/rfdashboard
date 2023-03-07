#!/bin/bash

OWD=`pwd`
DIR="$( cd "$( dirname "$(readlink -f "${BASH_SOURCE[0]}")" )" >/dev/null 2>&1 && pwd )"
cd $DIR

for file in $(basename -s .csv *.csv)
do
  ctl=./${file}.ctl
  log=./${file}.log
  if [ ! -f "$ctl" ] ; then
    echo "### Missing control file $ctl ###"
    continue
  fi

  sqlldr rfgradteam_owner/rfgradteam_owner@localhost:1521/XEPDB1  control=./${file}.ctl silent='(feedback)'

  if [ -f "$log" ] ; then
    rm "$log"
  fi

done

cd $OWD


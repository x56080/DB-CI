#!/bin/sh

echo "Start sequoiadb..."

if [ $# -ne 1 ]; then
    echo "usage:startDB.sh db_path"
    exit 0
fi

DB_PATH=$1

#create data directory
mkdir -p $DB_PATH/database/cata
mkdir -p $DB_PATH/database/data
mkdir -p $DB_PATH/database/log/cata
mkdir -p $DB_PATH/database/log/data

#copy configure file
$DB_PATH/conf/local/sdb.cata.conf
$DB_PATH/conf/local/sdb.data.conf
$DB_PATH/conf/local/sdb.coord.conf

cp -f $DB_PATH/conf/samples/sdb.cat $DB_PATH/conf/local/sdb.cata.conf/sdb.cat
cp -f $DB_PATH/conf/samples/sdb.conf.catalog $DB_PATH/conf/local/sdb.cata.conf/sdb.conf
cp -f $DB_PATH/conf/samples/sdb.conf.data $DB_PATH/conf/local/sdb.data.conf/sdb.conf
cp -f $DB_PATH/conf/samples/sdb.conf.coord $DB_PATH/conf/local/sdb.coord.conf/sdb.conf

#start sequoiadb proc
$DB_PATH/sequoiadb -c $DB_PATH/conf/local/sdb.cata.conf &
$DB_PATH/sequoiadb -c $DB_PATH/conf/local/sdb.data.conf &
$DB_PATH/sequoiadb -c $DB_PATH/conf/local/sdb.coord.conf &

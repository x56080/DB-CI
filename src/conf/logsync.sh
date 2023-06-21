#!/bin/bash

# This is a script used to sync sdblog.
# This script will check if inotify-tools are installed and Linux kernel version higher than 2.6.13.
# Be sure the logServer Address configuration have set.

REMOTE_SERVER=""

usage() {
	cat << EOF
This script is used to sync sdbdiag.
Before running this script, please ensure that:
* have installed inotify-tools in local.
* the linux kernel is support inotify.
* make sure sequoiadb is installed.
USAGE:
	synclog.sh  [OPTIONS]
OPTIONS:
	-h, --help           Prints help information
	-v, --version        Version of this script
	-p, --projectName    Name of CI-Project
	-j, --jobId          jobId of Project
EOF
}

main() {
  export RSYNC_PASSWORD=Admin@1024
  local projName=""
  local jobId=-1
	local curVersion="version 0.0.1"
	
	while getopts p:j:hv opt
	do 
		case "$opt" in
			p) projName=$OPTARG
			   isValidProjName $projName
			   ;;
			j) jobId=$OPTARG
			   isValidJobId $jobId
			   ;;
			h) usage
			   exit 0 ;;
			v) echo $curVersion 
			   exit 0 ;;
		    *) echo "unknow argument"
			   usage
			   exit 1 ;;
		esac
	done
	
	needCmd "hostname"
	hostName=`hostname`
	syncDir=$projName\_$jobId
	syncPath=$syncDir/$hostName\_diaglog
	
	checkEnv
	init $syncDir
	syncLog $syncPath
	cleanUp &
}

function checkEnv() {

        needCmd "uname"
        needCmd "awk"
        needCmd "inotifywait"
        
        kernelVersion=`uname -r | awk -F'-' '{print $1}'`
        local sub0Version=`echo $kernelVersion | awk -F. '{print $1}'`
        local sub1Version=`echo $kernelVersion | awk -F. '{print $2}'`
        local sub2Version=`echo $kernelVersion | awk -F. '{print $3}'`
	    if [[ $sub0Version -lt 2 ]]
        then
	        printf "Linux kernel version %s is too low\n" "${kernelVersion}"
		    exit 1
        elif [[ $sub0Version -lt 2 ]] && [[ $sub1Version -lt 6 ]]; then
			printf "Linux kernel version %s is too low\n" "${kernelVersion}"
			exit 1
        elif [[ $sub0Version -lt 2 ]] && [[ $sub1Version -lt 6 ]] && [[ $sub2Version -lt 13 ]]; then
		echo $sub2Version
			printf "Linux kernel version %s is too low\n" "${kernelVersion}"
			exit 1
	    fi
	
}

function needCmd(){
     if ! checkCmd "$1"; then
         err "need '$1' (command not found)"
     fi
}

function checkCmd(){
    command -v "$1" > /dev/null 2>&1
}

function init() {
	syncDir=$1
    selectRemoteServ  
	initInotifyDir
	initBackupDir $syncDir  
}

function initBackupDir() {
	syncDir=$1
	needCmd "rsync"
	ensure rsync -a /tmp/diaglog.txt $REMOTE_SERVER::backup/$syncDir/
}

function syncLog() {
	local syncDir=$1
	fullSyncLog $syncPath &
	incrSyncLog $syncPath &
}

function isValidProjName() {
        local projName=$1
        if [[ -z $projName ]]; then
                printf "project name %s is invalid\n" "${projName}"
                exit 1
        fi
}

function isValidJobId() {
        local jobId=$1
        if [[ $jobId -le 0 ]]; then
                printf "jobId %d is invalid\n" "${jobId}"
                exit 1
        fi
}

function selectRemoteServ() {
    local logServers=()
	while read line
	do
	    logServers[${#logServers[*]}]=$line
	done < /opt/logAddrConfig
	local logServNum=${#logServers[*]}
	
	needCmd "hostname"
	needCmd "sed"
	needCmd "md5sum"
	local hashCode=`hostname -i | md5sum | awk -F' ' '{print $1}'`
	hashCode=`echo $((16#$hashCode)) | sed s/'-'//g`
	
	needCmd "expr"
	local index=`expr $hashCode % $logServNum`
	REMOTE_SERVER=${logServers[index]}
}

function initInotifyDir() {
    local isInstalled="/etc/default/sequoiadb"
    if [ -n $isInstalled ]; then
 	    local installDir=`cat /etc/default/sequoiadb | grep INSTALL_DIR | awk -F= '{print $2}'`
	    if [ -n $installDir ]; then
		    ensure $installDir/bin/sdblist -l | awk -F' ' '{print $10}' | grep -v DBPath | grep -v "^$" > /tmp/diaglog.txt
            ensure sed -i "s/$/&diaglog/g" /tmp/diaglog.txt
	    else
	        printf "sequoiadb installDir not found"
	    fi
    fi
}

function fullSyncLog() {
    local syncPath=$1
    local dirs="/tmp/diaglog.txt"
    for dir in $(cat $dirs)
    do
        local prefixDirName=`dirname $(dirname $(dirname $dir))`
	    dir=`echo $dir | sed "s#$prefixDirName#$prefixDirName/.#g"`
        ensure rsync -avR  --exclude={'*.trap','*.core','core.*'} $dir 192.168.28.27::backup/$syncPath
    done
}

function incrSyncLog() {
    local isopen=0
    local syncPath=$1
    local inotifyCmd="inotifywait -mrq -e create,move --fromfile="/tmp/diaglog.txt" "
    $inotifyCmd | while read DIRECTORY EVENT FILE
    do
      
      if [[ $EVENT =~ "CREATE" ]] ;then
         exec 3< $DIRECTORY$FILE 
         isopen=1
      fi
    
	  if [[ $EVENT =~ "MOVE" ]] || [[ $EVENT =~ "CLOSE" ]];  then
	            local prefixDirName=`dirname $(dirname $(dirname $DIRECTORY))`
	            DIRECTORY=`echo $DIRECTORY | sed "s#$prefixDirName#$prefixDirName/.#g"`
				ensure rsync -avR --exclude={'*.core','*.trap','core.*'} $DIRECTORY$FILE 192.168.28.27::backup/$syncPath/
				if [ $isopen -eq 1 ];then
					exec 3>&-
					isopen=0
				fi 
	  fi
    done
}

function err() {
	printf 'execute sync sdbdiag: %s\n' "$1" >&2
	exit 1
}

function ensure() {
	if ! "$@"; then err "command failed: $*"; fi
}

function cleanUp() {
    local watchDir="inotifywait -mq -e delete /etc/default/sequoiadb"
    $watchDir | while read DIRECTORY EVENT FILE
    do	
		if [[ $EVENT =~ "DELETE" ]]; then
			ensure killall -r inotify
			ensure killall -r logsync
		fi
		break
    done
    printf "sync %s-%d log is completed\n" "${projName}" "${jobId}"
}


main "$@" || exit 1

#!/bin/bash
set -x
#获取当前作业的信息
job_name="$JOB_NAME"
build_number="$BUILD_NUMBER"
function check_jenkins_build_status() {
  local jenkins_url="http://192.168.29.80:8080/"
  local job_name="$1"
  local build_number="$2"

  local building=$(curl -s "$jenkins_url/job/$job_name/$build_number/api/json" | jq -r '.building')
  echo $building
}


function jenkins_build_lock() {

  # 要检查的文件
  local file_to_check="/tmp/$FLAG"

  # 最大等待时间（秒）：36000秒
  local max_wait_time=36000

  # 循环检查间隔时间（秒）：60秒
  local check_interval=60

  # 强制创建标志
  local force_create=false

  # 创建文件锁文件路径
  local lockfile="/tmp/$FLAG.lock"


  # 使用文件锁
  exec 200>"$lockfile"
  sleeptimelen=0
  while true; do
    if [ ! -e "$file_to_check" ]; then
      break
    fi

    jobname=$(cat $file_to_check | awk -F ',' '{print $1}')
    buildnumber=$(cat $file_to_check | awk -F ',' '{print $2}')

    building=$(check_jenkins_build_status $jobname $buildnumber)
    if [[ "$building" == "false" ]]; then
      break
    fi
    sleep "$check_interval"
    sleeptimelen=$(($sleeptimelen + $check_interval))
    if [ $sleeptimelen -ge $max_wait_time ]; then
      break
    fi
  done

  # 尝试获取文件锁
  if flock -n 200; then
    # 进程1：创建文件
    touch "$file_to_check"
    echo "$job_name,$build_number" >"$file_to_check"
    echo "文件已创建"
    # 释放文件锁
    exec 200>&-
    exit 0
  fi
}

function jenkins_build_unlock() {
  if [ -f "/tmp/$FLAG" ]; then
    rm -rf "/tmp/$FLAG"
  fi
  if [ -f "/tmp/$FLAG.lock" ]; then
    rm -rf "/tmp/$FLAG.lock"
  fi
}



while getopts :m:f:b:j: opt; do
  case "$opt" in
  m) METHOD=$OPTARG ;;
  f) FLAG=$OPTARG ;;
  b) JOB_NAME=$OPTARG ;;
  j) BUILD_NUMBER=$OPTARG ;;
  esac
done

case $METHOD in
unlock)
  jenkins_build_unlock
  exitcode=$?
  ;;
lock)
  jenkins_build_lock
  exitcode=$?
  ;;
*)
  # 处理无效选项
  echo "无效的选项"
  ;;
esac

exit $exitcode

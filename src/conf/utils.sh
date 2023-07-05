#!/bin/bash

function synctime() {
  command -v ntpdate && ntpdate ${val} || (command -v chronyc && chronyc -a makestep || echo 'not command ntpdate or chronyc')
}

while getopts :m:v: opt; do
  case "$opt" in
  m) method=$OPTARG ;;
  v) val=$OPTARG ;;
  esac
done

case $method in
synctime)
  synctime
  exitcode=$?
  ;;
\?)
  # 处理无效选项
  echo "无效的选项"
  ;;
esac

exit $exitcode

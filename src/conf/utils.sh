#!/bin/bash

# 同步时间
# args:
#   val 地址
# comment:
#   chronyc 服务预先配置好同步源所以无需地址
function synctime() {
  local ipaddr='192.168.29.80'
  if [[ -x "$(command -v ntpdate)" ]]; then
    if [[ -z ${val} ]]; then
      echo 'use default ntp ip. cause arg val is empty'
      ntpdate ${ipaddr}
    else
      ntpdate ${val}
    fi
  elif [[ -x "$(command -v chronyc)" ]]; then
    chronyc -a makestep
  else
    echo 'not command ntpdate or chronyc'
  fi
}

# 复制测试工具
# args:
#   val 工具类型
#   dir 位置
# description:
#   val匹配类型cmds,若为存在则复制
#   反之判断当前目录是否存在当前若不存在则复制,但若为同步服务未配置该类型则将会复制失败
# comment:
#   1. 环境变量设置在脚本中执行source并不会生效，所以需要调用脚本后执行source
#   2. 调用该方法中设置环境变量的类型时需要root权限, 因为echo xx > /etc/profile
function sync_test_tools() {
  export RSYNC_PASSWORD='Admin@1024'
  local RESOURCE_MACHINE='192.168.29.10'
  local RESOURCE_PATH='/hdd/test_tools'

  local envfile="/etc/profile"
  local userenvfile="/home/sdbadmin/.bashrc"
  local hostname=$(hostname)

  if ! [[ -e $userenvfile ]]; then
    echo "$hostname: $userenvfile is no exist"
    exit 1
  elif [[ -z $dir ]] || [[ -z $val ]]; then
    echo "$hostname: func sync_test_tools args(val=$val or dir=$dir) is empty"
    exit 1
  fi

  local cmds=("java" "ant" "mvn" "virtualenv" "php")
  local found=0

  # 使用 for 循环遍历列表元素
  for cmd in "${cmds[@]}"; do
    # 检查变量是否等于列表中的某个元素
    if [[ "$cmd" = "$val" ]]; then
      found=1
      break
    fi
  done

  if [[ "$found" = 1 ]]; then
    source "/etc/profile"
    if ! [[ -x "$(command -v $val)" ]]; then
      local sync_flag=$val
      [[ 'ant' == $sync_flag ]] && sync_flag='java'
      [[ 'mvn' == $sync_flag ]] && sync_flag='java'

      mkdir -p $dir
      rsync -avzp sdbadmin@${RESOURCE_MACHINE}::test_tools_$sync_flag $dir
      sync_ret=$(echo $?)
    else
      echo "$hostname: $val command is exist ==> $(command -v $val)"
      exit 0
    fi
  elif ! [[ -d $dir ]]; then
    rsync -avzp sdbadmin@${RESOURCE_MACHINE}::test_tools_$val $dir
    sync_ret=$(echo $?)
  else
    echo "$hostname: $val dir is exist"
    exit 0
  fi

  if ! [[ $sync_ret == 0 ]]; then
    echo "$hostname: rsync failure"
    exit 1
  fi

  [[ "$(uname -a)" == *'x86_64'* ]] && arch='x86' || arch='arm'
  local bak_flag=$(date "+%Y%m%d_%H%M%S")

  case "$val" in
  "java")
    tarname="openJDK-8u292_$arch.tar.gz"
    tar -zxf "$dir/$tarname" -C $dir
    test $? -eq 0 || (
      echo "$hostname: tar failure"
      exit 1
    )

    echo "# from db-ci project utils.sh script. add JAVA_HOME at $bak_flag " >>$envfile
    echo "export JAVA_HOME=$dir/openJDK-8u292" >>$envfile
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >>$envfile

    echo "# from db-ci project utils.sh script. add JAVA_HOME at $bak_flag " >>$userenvfile
    echo "export JAVA_HOME=$dir/openJDK-8u292" >>$userenvfile
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >>$userenvfile

    # runtest_java.xml配置为JAVA_HOME=/opt/jdk1.8.0_11执行mvn
    [[ -e "/opt/jdk1.8.0_11" ]] || ln -s "$dir/openJDK-8u292" "/opt/jdk1.8.0_11"

    ;;
  "ant")
    tarname="apache-tools.tar.gz"
    tar -zxf "$dir/$tarname" -C $dir
    test $? -eq 0 || (
      echo "$hostname: tar failure"
      exit 1
    )

    echo "# from db-ci project utils.sh script. add ANT_HOME at $bak_flag " >>$envfile
    echo "export ANT_HOME=$dir/apache-ant-1.9.16" >>$envfile
    echo "export PATH=\$ANT_HOME/bin:\$PATH" >>$envfile

    echo "# from db-ci project utils.sh script. add ANT_HOME at $bak_flag " >>$userenvfile
    echo "export ANT_HOME=$dir/apache-ant-1.9.16" >>$userenvfile
    echo "export PATH=\$ANT_HOME/bin:\$PATH" >>$userenvfile

    ;;
  "mvn")
    tarname="apache-tools.tar.gz"
    tar -zxf "$dir/$tarname" -C $dir
    test $? -eq 0 || (
      echo "$hostname: tar failure"
      exit 1
    )

    echo "# from db-ci project utils.sh script. add MAVEN_HOME at $bak_flag " >>$envfile
    echo "export MAVEN_HOME=$dir/apache-maven-3.3.9" >>$envfile
    echo "export PATH=\$MAVEN_HOME/bin:\$PATH" >>$envfile

    echo "# from db-ci project utils.sh script. add MAVEN_HOME at $bak_flag " >>$userenvfile
    echo "export MAVEN_HOME=$dir/apache-maven-3.3.9" >>$userenvfile
    echo "export PATH=\$MAVEN_HOME/bin:\$PATH" >>$userenvfile

    ;;
  "php")
    tarname="php_$arch.tar.gz"
    tar -zxf "$dir/$tarname" -C $dir
    test $? -eq 0 || (
      echo "$hostname: tar failure"
      exit 1
    )
    ;;
  "virtualenv")
    tarname="py_model.tar.gz"
    tar -zxf "$dir/$tarname" -C $dir
    test $? -eq 0 || (
      echo "$hostname: tar failure"
      exit 1
    )
    ;;
  esac

}

while getopts :m:v:d: opt; do
  case "$opt" in
  m) method=$OPTARG ;;
  v) val=$OPTARG ;;
  d) dir=$OPTARG ;;
  esac
done

case $method in
synctime)
  synctime
  exitcode=$?
  ;;
sync_test_tools)
  sync_test_tools
  exitcode=$?
  ;;
\?)
  echo "-m option:"
  echo "synctime"
  echo "sync_test_tools"
  ;;
*)
  # 处理无效选项
  echo "无效的选项"
  ;;
esac

exit $exitcode

#!/bin/bash
function compileTestcase(){
  WORKSPACE=$1
  TEST_CASE=$WORKSPACE/sequoiadb_compile/testcase_new
  DRIVER_PATH=$WORKSPACE/release/driver

  set -x
  echo "编译C驱动测试用例"
  cd $TEST_CASE/driver/c
  scons --dd .

  echo "编译CPP驱动测试用例"
  cd $TEST_CASE/driver/cpp
  scons --dd .

  echo "编译Java驱动测试用例"
  cd $TEST_CASE/driver/java
  sdbVersion=`ls $DRIVER_PATH/Java`
  sdbVersion=`echo $sdbVersion|awk -F .jar '{print $1}' | awk -F sequoiadb-driver- '{print $2}'`

  export JAVA_HOME='/opt/openjdk-8u292-b10/'
  mvn clean install -DsdbdriverDir=$DRIVER_PATH/Java/  -Dsdbdriver=$sdbVersion -DthreadexecutorDir=$TEST_CASE/tools/ -DskipTests
  cd $TEST_CASE/story/java
  mvn clean install -DsdbdriverDir=$DRIVER_PATH/Java/  -Dsdbdriver=$sdbVersion -DthreadexecutorDir=$TEST_CASE/tools/ -DskipTests
  cd $TEST_CASE/sdv/java
  mvn clean install -DsdbdriverDir=$DRIVER_PATH/Java/  -Dsdbdriver=$sdbVersion -DthreadexecutorDir=$TEST_CASE/tools/ -DskipTests
  cd $TEST_CASE/reliability/java
  mvn clean install -DsdbdriverDir=$DRIVER_PATH/Java/  -Dsdbdriver=$sdbVersion -DthreadexecutorDir=$TEST_CASE/tools/ -DskipTests
}


compileTestcase $1
#!/bin/sh
echo "Begin to make release package"

echo $#
if [ $# -ne 2 ]; then
    echo "usage:mkrelease.sh code_dir release_dir"
    exit 0
fi

CODE_DIR=$1
RELEASE_DIR=$2

mkdir -p $RELEASE_DIR/sequioadb/bin
mkdir -p $RELEASE_DIR/sequioadb/lib
mkdir -p $RELEASE_DIR/sequioadb/include
mkdir -p $RELEASE_DIR/sequioadb/samples
mkdir -p $RELEASE_DIR/sequioadb/java
mkdir -p $RELEASE_DIR/sequioadb/doc
mkdir -p $RELEASE_DIR/sequioadb/conf/samples
mkdir -p $RELEASE_DIR/sequioadb/conf/local


cp $CODE_DIR/sequoiadb $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdb $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdbbp $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdbcm $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdbstart $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdbstop $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdblist $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/sdbinspt $RELEASE_DIR/sequioadb/bin
cp $CODE_DIR/client/C/lib/*.so $RELEASE_DIR/sequioadb/lib
cp $CODE_DIR/client/CPP/lib/*.so $RELEASE_DIR/sequioadb/lib
mkdir -p $RELEASE_DIR/sequioadb/include/CPP/include
mkdir -p $RELEASE_DIR/sequioadb/include/C/include
cp $CODE_DIR/client/C/include/*.h $RELEASE_DIR/sequioadb/include/C/include
cp $CODE_DIR/client/CPP/include/*.h $RELEASE_DIR/sequioadb/include/CPP/include
cp -r $CODE_DIR/client/samples/*   $RELEASE_DIR/sequioadb/samples
ARCH=`arch`
if [ "$ARCH"="x86_64" ]; then
    cp -r $CODE_DIR/java/jdk_linux64   $RELEASE_DIR/sequioadb/java
#else arch
fi
cp $CODE_DIR/conf/samples/* $RELEASE_DIR/sequioadb/conf/samples/
 
echo "find $RELEASE_DIR/sequioadb -name .svn -exec rm -rf {} \;"
find $RELEASE_DIR/sequioadb -name .svn -exec rm -rf {} \;

echo "tar -czf  $RELEASE_DIR/sequoiadb.tar.gz sequioadb"
cd $RELEASE_DIR/
tar -czf  sequoiadb.tar.gz * sequioadb
echo "mv sequoiadb.tar.gz $RELEASE_DIR/sequoiadb.tar.gz"
mv sequoiadb.tar.gz $RELEASE_DIR/sequoiadb.tar.gz

echo "completed make release package"


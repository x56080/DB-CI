#!/bin/sh
echo "Begin to make release package"

echo $#
if [ $# -ne 2 ]; then
    echo "usage:mkrelase.sh code_dir release_dir"
    exit 0
fi

CODE_DIR=$1
RELEASE_DIR=$2

mkdir -p $RELEASE_DIR/release/bin
mkdir -p $RELEASE_DIR/release/lib
mkdir -p $RELEASE_DIR/release/include
mkdir -p $RELEASE_DIR/release/samples
mkdir -p $RELEASE_DIR/release/java
mkdir -p $RELEASE_DIR/release/doc

cp $CODE_DIR/sequoiadb $RELEASE_DIR/release/bin
cp $CODE_DIR/sdb $RELEASE_DIR/release/bin
cp $CODE_DIR/sdbbp $RELEASE_DIR/release/bin
cp $CODE_DIR/sdbcm $RELEASE_DIR/release/bin
cp $CODE_DIR/sdbstart $RELEASE_DIR/release/bin
cp $CODE_DIR/sdbstop $RELEASE_DIR/release/bin
cp $CODE_DIR/sdblist $RELEASE_DIR/release/bin
cp $CODE_DIR/sdbinspt $RELEASE_DIR/release/bin
cp $CODE_DIR/client/C/lib/*.so $RELEASE_DIR/release/lib
cp $CODE_DIR/client/CPP/lib/*.so $RELEASE_DIR/release/lib
mkdir -p $RELEASE_DIR/release/include/CPP/include
mkdir -p $RELEASE_DIR/release/include/C/include
cp $CODE_DIR/client/C/include/*.h $RELEASE_DIR/release/include/C/include
cp $CODE_DIR/client/CPP/include/*.h $RELEASE_DIR/release/include/CPP/include
cp -r $CODE_DIR/client/samples/*   $RELEASE_DIR/release/samples
ARCH=`arch`
if [ "$ARCH"="x86_64" ]; then
    cp -r $CODE_DIR/java/jdk_linux64   $RELEASE_DIR/release/java
#else arch
fi
 
find . -name .svn -exec rm -rf {} \;
tar -czf  $RELEASE_DIR/sequoiadb.tar.gz $RELEASE_DIR/release/*

echo "completed make release package"


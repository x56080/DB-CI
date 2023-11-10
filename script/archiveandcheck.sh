#!/bin/bash

function copyandcheck(){
   for file in $(ls $1)
   do
      if [ -f $1/${file} ];then
         origin=$(md5sum $1/${file} | awk -F' '  '{print $1}')
         mkdir -p $(dirname $2/${file})
         mv $1/${file} $2/${file}
         echo "${file} ==> $2/${file}"
         path=$2/${file}
         new=$(md5sum ${path})
         new=`echo $new | awk '{print $1}'`
         test $new = $origin
         if [ $? -ne 0 ];then
            echo "trans $file error"
            exit $?
         fi
      fi

      if [ -d $1/$file ];then
         src=$1/${file}
         dst=$2/${file}
         copyandcheck ${src} ${dst}
      fi
   done
}
copyandcheck $1 $2





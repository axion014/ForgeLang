#!/bin/bash
o="out.o"
v=""
ra=false
c=false
i=false
while [ -n "$1" ]; do
  if [ "$1" = "-ra" ]; then
    ra=true
  elif [ "$1" = "-c" ]; then
    c=true
  elif [ "$1" = "-v" ]; then
    v="-v"
  elif [ "$1" = "-i" ]; then
    i=true
    o=""
  else 
    o=$1
  fi
  shift
done

if $i; then
  ./clean.sh -s
  echo -e \n > tmp.omn
  echo $o >> ./tmp.omn
  java -jar ../../Jar/omni.jar ./tmp.omn | :
  rm ./tmp.omn
  o="out.o"
elif $ra; then
  rm -f $o
  lib/windows/as/as -o $o tmp.asm
fi

gcc $v -o test.exe driver.c $o || exit
echo "You got \"`./test.exe`\""

if $c; then
  ./clean.sh -s
fi

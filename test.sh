#!/bin/bash

cd $(dirname $0)

o="out.o"
v=""
ra=false
rat=win64
gccopts=""
c=false
i=false
while [ -n "$1" ]; do
  if [ "$1" = "-ra" ]; then
    ra=true
    shift
    if [ "$1" = "win" ] || [ "$1" = "win32" ] || [ "$1" = "win64" ] || [ "$1" = "macho" ] ||
      [ "$1" = "macho32" ] || [ "$1" = "macho64" ] || [ "$1" = "elf" ] || [ "$1" = "elf32" ] ||
      [ "$1" = "elf64" ] || [ "$1" = "elfx32" ] ; then
      rat=$1
  	fi
  elif [ "$1" = "-c" ]; then
    c=true
  elif [ "$1" = "-v" ]; then
    v="-v "
  elif [ "$1" = "-i" ]; then
    i=true
    o=""
  elif [ "$1" = "-gccopts" ]; then
    shift
    gccopts="$1 "
  else
    o="$1"
  fi
  shift
done

if $i; then
  ./clean.sh -s
  echo -e \n > tmp.fl
  echo $o >> ./tmp.fl
  java -jar ../../Jar/fl.jar ./tmp.fl | :
  rm ./tmp.fl
  o="out.o"
elif $ra; then
  rm -f $o
  lib/nasm/nasm -f $rat -o $o tmp.asm
fi

echo "gcc $v-o test.exe driver.c $o $gccopts-Wl,-no_pie"
gcc $v-o test.exe driver.c $o $gccopts-Wl,-no_pie || exit
echo "You got \"`./test.exe`\""

if $c; then
  ./clean.sh -s
fi

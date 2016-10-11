#!/bin/bash

cd $(dirname $0)

if [ "$(uname)" = 'Darwin' ]; then
  OS='Mac'
  rat='macho64'
elif [ "$(expr substr $(uname -s) 1 5)" = 'Linux' ]; then
  OS='Linux'
  rat='elf'
elif [ "$(expr substr $(uname -s) 1 10)" = 'MINGW32_NT' ]; then                                                                                           
  OS='Windows'
  rat='win'
elif [ "$(expr substr $(uname -s) 1 10)" = 'MINGW64_NT' ]; then                                                                                           
  OS='Windows'
  rat='win64'
else
  echo "Your platform ($(uname -a)) is not supported."
  exit 1
fi

o="out.o"
v=""
ra=false
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
    gccopts="$1"
  else
    o="$1"
  fi
  shift
done

if [ ! $OS = 'Windows' ]; then
  gccopts="$gccopts -Wl,-no_pie"
fi

if $i; then
  ./clean.sh -s
  echo -e \n > tmp.fl
  echo $o >> ./tmp.fl
  java -jar ../../Jar/fl.jar ./tmp.fl | :
  rm ./tmp.fl
  o="out.o"
elif $ra; then
  rm -f $o
  if [ $OS = 'Windows' ]; then
    lib/nasm/nasm.exe -f $rat -o $o tmp.asm
  else
  	lib/nasm/nasm -f $rat -o $o tmp.asm
  fi
fi

echo "gcc $v-o test.exe driver.c $o $gccopts"
gcc $v-o test.exe driver.c $o $gccopts || exit
echo "You got \"`./test.exe`\""

if $c; then
  ./clean.sh -s
fi

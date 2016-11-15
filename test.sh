#!/bin/bash

cd $(dirname $0)

if [ "$(uname)" = 'Darwin' ]; then
  OS='Mac'
	e="exe"
  rat='macho64'
elif [ "$(expr substr $(uname -s) 1 5)" = 'Linux' ]; then
  OS='Linux'
	e="exe"
  rat='elf'
elif [ "$(expr substr $(uname -s) 1 10)" = 'MINGW32_NT' ]; then
  OS='Windows'
	e="test.exe"
  rat='win'
elif [ "$(expr substr $(uname -s) 1 10)" = 'MINGW64_NT' ]; then
  OS='Windows'
	e="test.exe"
  rat='win64'
else
  echo "Your platform ($(uname -a)) is not supported."
  exit 1
fi

a="tmp.asm"
o="out.o"
cof=""
v=""
ra=false
sa=false
sc=false
gccopts=""
c=false
co=false
i=false
while [ -n "$1" ]; do
	if [ "$1" = "-h" ]; then
		echo "Forgelang test v1.0"
		echo "Usage: ./test.sh <-h | [-ra [bintype] [-a <assembly>] [-sa] | -co <file> | -i] [-sc] [-c] [-v] [object] [-gccopts <options>]>"
		echo "-h: show this help"
		echo -e "-ra: reassemble the file. default file name is tmp.asm. \n     option argument is binary file type."
		echo "	-a: assembly file name."
		echo "	-sa: show assembly file."
		echo "-co: compile the .fl file. required argument is file name."
		echo "-i: <object> argument change to Forgelang source, compile it."
		echo "-sc: show internal commands."
		echo "-c: clean project."
		echo "-v: show gcc verbose."
		echo "-e: executable file name."
		echo "-gccopts: transfer arguments to gcc."
		exit
	elif [ "$1" = "-ra" ]; then
    ra=true
    shift
    if [ "$1" = "win" ] || [ "$1" = "win32" ] || [ "$1" = "win64" ] || [ "$1" = "macho" ] ||
      [ "$1" = "macho32" ] || [ "$1" = "macho64" ] || [ "$1" = "elf" ] || [ "$1" = "elf32" ] ||
      [ "$1" = "elf64" ] || [ "$1" = "elfx32" ] ; then
      rat=$1
  	fi
	else
		if [ "$1" = "-c" ]; then
	    c=true
		elif [ "$1" = "-co" ]; then
	    co=true
			shift
			cof="$1"
	  elif [ "$1" = "-v" ]; then
	    v="-v "
	  elif [ "$1" = "-i" ]; then
	    i=true
			co=true
	    o=""
	  elif [ "$1" = "-gccopts" ]; then
	    shift
	    gccopts=" $1"
		elif $ra && [ "$1" = "-a" ]; then
			shift
			a="$1"
		elif [ "$1" = "-e" ]; then
			shift
			e="$1"
		elif [ "$1" = "-sa" ]; then
			sa=true
		elif [ "$1" = "-sc" ]; then
			sc=true
	  else
	    o="$1"
	  fi
	  shift
  fi
done

if [ ! $OS = 'Windows' ]; then
  gccopts="$gccopts -Wl,-no_pie"
fi

if $co; then
  ./clean.sh -s
  if $i; then
		echo -e "\n" > tmp.fl
  	echo $o >> ./tmp.fl
		cof=./tmp.fl
	fi
  java -jar ./fl.jar $cof | :
  $i && rm ./tmp.fl
  o="out.o"
elif $ra; then
  rm -f $o
	$sc && echo "nasm -f $rat -o $o $a"
  if [ $OS = 'Windows' ]; then
    lib/nasm/nasm.exe -f $rat -o $o $a
  else
  	lib/nasm/nasm -f $rat -o $o $a
  fi
fi

$sc && echo "gcc $v-o $e driver.c $o$gccopts"
gcc $v-o $e driver.c $o$gccopts || exit
result=`./$e`
if [ $? = 139 ]; then
	echo "You got SIGSEGV"
else
	echo "You got \"$result\""
fi
if $sa; then
	echo "Assembly is..."
	cat $a
fi

if $c; then
  ./clean.sh -s
fi


cd $(dirname $0)

if [ "$(uname)" = 'Darwin' ]; then
  OS='Mac'
elif [ "$(expr substr $(uname -s) 1 5)" = 'Linux' ]; then
  OS='Linux'
elif [ "$(expr substr $(uname -s) 1 10)" = 'MINGW32_NT' ] || [ "$(expr substr $(uname -s) 1 10)" = 'MINGW64_NT' ]; then
  OS='Windows'
else
  echo "Your platform ($(uname -a)) is not supported."
  exit 1
fi

function test {
  ./clean.sh -s
  java -jar ./fl.jar $1 | :
  if [ $OS = 'Windows' ]; then
    gcc -o test.exe driver.c out.o || exit
  else
  	gcc -o test.exe driver.c out.o -Wl,-no_pie || exit
  fi
  expected=`echo -e "$2"`
  if type dos2unix &>/dev/null; then
    result=`./test.exe | dos2unix`
  else
    result=`./test.exe`
  fi

	ret=$?

	if [ "$result" = "$expected" ]; then
    echo "Test done: You got \"$result\" from $1"
  else
		if [ $ret = 139 ]; then
	  	echo "Test($1) failed: SIGSEGV occured"
		else
			echo "Test($1) failed: \"$expected\" expected but got \"$result\""
		fi
    echo "Assembly is..."
    cat tmp.asm
  fi
}

trap "./clean.sh -s" 0

test test/old/test10.fl hoge
test test/old/test11.fl 1
test test/old/test12.fl 1
test test/test13.fl 13
test test/test14.fl "foo\n5\n30"
test test/test15.fl "9\n9\n5"
test test/test16.fl 0

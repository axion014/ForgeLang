
cd $(dirname $0)

function test {
  ./clean.sh -s
  java -jar ../../Jar/fl.jar $1 | :
  gcc -o test.exe driver.c out.o -Wl,-no_pie || exit
  expected=`echo -e "$2"`
  if type dos2unix &>/dev/null; then
    result=`./test.exe | dos2unix`
  else
    result=`./test.exe`
  fi
  
  if [ "$result" = "$expected" ]; then
    echo "Test done: You got \"$result\" from $1"
  else
    echo "Test($1) failed: \"$expected\" expected but got \"$result\""
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
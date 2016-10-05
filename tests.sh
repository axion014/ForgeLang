
function test {
  ./clean.sh -s
  java -jar ../../Jar/omni.jar $1 | :
  gcc -o test.exe driver.c out.o || exit
  expected=`echo -e "$2"`
  result=`./test.exe | dos2unix`
  if [ "$result" = "$expected" ]; then
    echo "Test done: You got \"$result\" from $1"
  else
    echo "Test($1) failed: \"$expected\" expected but got \"$result\""
  fi
}

trap "./clean.sh -s" 0

test test/old/test10.omn hoge
test test/old/test11.omn 1
test test/old/test12.omn 1
test test/test13.omn 13
test test/test14.omn "foo\n5\n30"
test test/test15.omn "9\n9\n5"
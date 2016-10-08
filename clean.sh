
cd $(dirname $0)

s=false
while [ -n "$1" ]; do
  if [ $1 = "-s" ]; then
    s=true
  fi
  shift
done

for file in tmp*.asm; do
  if [ ! $file = "tmp*.asm" ]; then
    if ! $s; then
      echo - $file
    fi
    rm $file
  fi 
done
for file in out*.o; do
  if [ ! $file = "out*.o" ]; then
    if ! $s; then
      echo - $file
    fi
    rm $file
  fi
done
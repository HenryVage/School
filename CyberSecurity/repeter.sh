#/bin/bash
# Simple script to repeat a string n times
number=$1
str=$2
for((i = 0; i<number; i++)) {
echo "$str"
}

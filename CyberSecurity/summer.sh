#!/bin/bash
declare -i sum=0
declare -i number
while read number
do
	((sum+=number))
done
echo "Summen er $sum."

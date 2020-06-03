#!/bin/bash
# a simple scrip to summarize number
declare -i sum=0
declare -i number
while read number
do
	((sum+=number))
done
echo "Summen er $sum."

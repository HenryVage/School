#/bash/bin
#simple script to summarize time of incidents(hendelser)
read -p "Hva er hendelsen?" hendelse
str=$(grep [$hendelse] < cat hendelse.logg)
printf -v str '%s' $str
var=$(( ${str//[!0-9]/} ))
declare -i sum=0
for (( i=0; i<${#var}; i++ )); do
	sum+=${var:$i:1}
done
echo $sum

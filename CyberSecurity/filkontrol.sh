#/bash/bin
fil=$1
declare -i sek=$2
var=true
exist=false
if [ -f $fil ];	then 
	exist=true
	lastedit=$(stat -c%Y $fil)
fi
while $var; do
	if [ -f $fil ];	then 
		if [ ! $exist ]; then
			echo "Filen $fil ble oprettet."
			exist=true
			var=false
		fi
		edit=$( stat -c%Y $fil )
		if [[ $edit  != $lastedit ]]; then
			lastedit=$(stat -c%Y $fil)
			echo "Filen $fil ble endret"
			var=false
		fi
	fi
	if $exist; then
		if [ ! -f $fil ]; then
			echo "Filen $fil ble slettet."
			exist=false
			var=false
		fi
	fi

if $var; then
	sleep $sek
fi

done

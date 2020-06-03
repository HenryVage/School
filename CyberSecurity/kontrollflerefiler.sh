#/bash/bin
while [ $# -ne 0 ]
do
	echo "test"
	./filkontrol.sh $1 60 &
	shift
done


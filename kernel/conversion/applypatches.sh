#!/bin/sh
mkdir tmp
pushd tmp
rm *
perl ../conversion/splitpatch.pl ../$1/*
popd
for i in `ls tmp`
do
	echo Applying tmp/$i
 	patch --batch $2 -p0  < tmp/$i
done

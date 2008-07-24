#!/bin/sh
mkdir tmp
pushd tmp
rm *
perl ../splitpatch.pl ../$1/*
popd
for i in `ls tmp`
do
	echo Applying tmp/$i
 	patch --batch -p0  < tmp/$i
done

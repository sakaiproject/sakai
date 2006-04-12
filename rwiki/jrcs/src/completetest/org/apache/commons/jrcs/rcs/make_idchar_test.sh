#!/bin/bash

fn=idchar.testfile
rm -f $fn $fn,v
echo "first version" > $fn
echo "description" | ci -m"blah" $fn

names="SIMPLENAME lowername MIXEDname UNDERSCORE_NAME"\
" NAME1 1NAME NAME1NAME"\
" BANG!NAME DQUOTE\"NAME HASH#NAME"\
" PERCENT%NAME AMP&NAME SQUOTE'NAME"\
" RRBR(NAME LRBR)NAME STAR*NAME PLUS+NAME"\
" MINUS-NAME SLASH/NAME"\
" RABR<NAME EQUALS=NAME LABR>NAME QUESTION?NAME"\
" RSBR[NAME SLOSH\\NAME LSBR]NAME CARAT^NAME TICK\`NAME"\
" RCBR{NAME BAR|NAME LCB}NAME TILDE~NAME"


for i in $names ; do
  rcs -n$i:1.1 $fn
done

mv $fn,v $fn

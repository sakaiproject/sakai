#! /bin/sh

## Transform $1 into $3 using XSL $2

java -cp xalan-2.6.0.jar org.apache.xalan.xslt.Process -in $1 -xsl $2 -out $3


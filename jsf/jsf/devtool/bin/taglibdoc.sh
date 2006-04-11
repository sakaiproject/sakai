#/usr/bin/sh
# Generate New Sakai tag library (TLD) HTML documentation

# Uncomment this block if you'd like to generate TLD documentation for the standard
# TLDs and the old (Sakai 1.5) TLD at the same time.
#
#  Edit for the path on your system
#
#  DEPLOY_HOME=~/dev
#  JSF_HOME=$DEPLOY_HOME/jsf/jsf-1_1
#  MYFACES_HOME=$DEPLOY_HOME/myfaces-1.0.9
#  STD_TLDS=$JSF_HOME/lib/jsf_core.tld $JSF_HOME$/lib/html_basic.tld

TLD_DIR=./../../widgets/src/META-INF
NEW_TLDS=$TLD_DIR/sakai-jsf.tld
PREVDIR=.

DEST=../target/taglibdoc

rm -r $DEST
mkdir -p $DEST

java -jar tlddoc.jar -d $DEST $NEW_TLDS $STD_TLDS $OLD_TLDS

pushd  $DEST
zip -r ../taglibdoc.zip *

echo Done.




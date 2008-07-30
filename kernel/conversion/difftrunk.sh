#!/bin/sh
from=49664
targets="alias  cluster  content  email event user authz component db  entity   jcr  memory site tool util "
mkdir patches`date +%Y%m%d`
pushd patches`date +%Y%m%d`
for i in $targets
do
svn diff  -r$from:HEAD https://source.sakaiproject.org/svn/$i/trunk  > $i.patch
done
popd

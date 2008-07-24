#!/bin/sh
targets="alias  cluster  content  email event user authz component db  entity   jcr  memory site tool util "
mkdir patches`date +%Y%m%d`
pushd patches`date +%Y%m%d`
for i in $targets
do
svn diff  -r40900:HEAD https://source.sakaiproject.org/svn/$i/trunk  > $i.patch
done
popd

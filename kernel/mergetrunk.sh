#!/bin/sh
targets="alias  cluster  content  email event user authz component db  entity   jcr  memory site tool util "
for i in $targets
do
pushd $i
svn merge -r40900:HEAD https://source.sakaiproject.org/svn/$i/trunk .
popd
done

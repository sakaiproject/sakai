#!/bin/sh

targets="alias authz cluster component content db email entity event jcr memory site tool user util"
for i in $targets
do 
    svn cp -m "Redoing Branch for pre-K1" https://source.sakaiproject.org/svn/$i/trunk https://source.sakaiproject.org/svn/$i/branches/pre-K1
done

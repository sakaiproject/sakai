#!/bin/sh
CORE=/Users/ieb/Caret/sakai22/sakai

pushd $CORE
find . -name pom.xml > allpoms
popd
mv $CORE/allpoms allpoms
egrep "^./alias/|^./cluster/|^./content/|^./email/|^./event/|^./user/|^./authz/|^./component/|^./db/|^./entity/|^./jcr/|^./memory/|^./site/|^./tool/|^./util/" allpoms > kernelpoms
for i in `cat kernelpoms` ; do grep artifactId $CORE/$i | head -2 | tail -1 | cut -d">" -f2 | cut -d"<" -f1; done > corekernelartifacts


find . -name pom.xml > allkernelpoms
(for i in `cat allkernelpoms`
do 
   
   artifact=`grep '<artifactId>' $i | head -2 | tail -1 | cut -d">" -f2 | cut -d"<" -f1`;
   group=`grep '<groupId>' $i | head -2 | tail -1 | cut -d">" -f2 | cut -d"<" -f1`;
   echo "$i:$group:$artifact";
done ) > kernelartifacts


for i in `cat corekernelartifacts`; do    kernelartifact=`grep $i kernelartifacts`;    echo "$i -> $kernelartifact"; done > artifacttransform


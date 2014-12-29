#Run this to get a list of changed directories
svn status sakai-trunk | grep "M " | awk '{print $2}' | awk -F/ '{print $1}' | sort | uniq -z | tr '\n' ' '

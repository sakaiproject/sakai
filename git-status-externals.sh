#! /bin/sh
cat .gitexternals  | grep path  | awk '{print "cd " $3 "; git status; cd .."}' | sh


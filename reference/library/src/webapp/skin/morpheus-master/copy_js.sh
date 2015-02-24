#!/bin/sh

# This is a temporary bit to copy all of the derived js files to the 
# other skin folders

# The process is this:
# (1) while grunt is running, you edit the files in js/src and js/plugins
# (2) grunt notices this and puts them together into 
#     morpheus-master/morpheus.plugins.js morpheus-master/morpheus.scripts.js which is
#     minified to morpheus-master/morpheus.plugins.min.js and
#     morpheus-master/morpheus.scripts.min.js
# (3) Run this script from morpheus-master to send the 
#     minified js files into all the other skin sets
#
# This is temporary and should be merged into Sakai-Compass-Compile.rb or something else
# eventually - I am just leaving this as a breadcrumb / workaround until a better 
# solution is found -- Chuck Wed Dec 17 18:04:50 EST 2014

for folder in morpheus-examp-u morpheus-default morpheus-rtl; do
    for fn in js/morpheus.plugins.min.js js/morpheus.scripts.min.js; do
       cp $fn ../$folder/$fn
       ls -l $fn ../$folder/$fn
    done
done
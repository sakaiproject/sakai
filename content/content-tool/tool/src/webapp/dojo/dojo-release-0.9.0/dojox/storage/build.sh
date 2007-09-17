#!/bin/sh
mtasc -version 6 -cp ../flash/flash6 -swf Storage_version6.swf -main -header 215:138:10 Storage.as
mtasc -version 8 -cp ../flash/flash8 -swf Storage_version8.swf -main -header 215:138:10 Storage.as

# port buildFileStorageProvider task from old ant script to compile Java code for Opera/Safari?

@echo HSQLDB build file for jdk 1.4
@echo *** we recommend the use of the ANT build.xml instead of this method
@echo for all jdk's include the path to jdk1.x.x\bin in your system path statement
cd ..\
md classes
del /s classes\*.class
cd src
javac -O -nowarn -d ../classes -classpath "%classpath%;../classes;../lib/servlet.jar;." ./*.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
cd ..\classes
copy ..\src\org\hsqldb\util\*.gif org\hsqldb\util
jar -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class org/hsqldb/util/*.gif
cd ..\build
pause

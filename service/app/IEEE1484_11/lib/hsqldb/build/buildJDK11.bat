@echo HSQLDB build file for jdk 1.1.x
@echo *** we recommend the use of the ANT build.xml instead of this method
@echo for all jdk's include the path to jdk1.x.x\bin in your system path statement
@echo for jdk1.1.x also set the system classpath to include the path to
@echo    jdk1.1.x\lib\classes.zip on your system
@echo example: set classpath=c:\jdk1.1.8\lib\classes.zip
cd ..\
md classes
del /s classes\*.class
cd src
mkdir ..\temp
copy org\hsqldb\jdbc\jdbcDataSource*.java ..\temp\
copy org\hsqldb\jdbc\jdbcSavepoint.java ..\temp\
copy org\hsqldb\jdbc\jdbcParameterMetaData.java ..\temp\
copy org\hsqldb\NIOLockFile.java ..\temp\
copy org\hsqldb\NIOScaledRAFile.java ..\temp\
del org\hsqldb\jdbc\jdbcDataSource*.java
del org\hsqldb\jdbc\jdbcSavepoint.java
del org\hsqldb\jdbc\jdbcParameterMetaData.java
del org\hsqldb\NIOLockFile.java
del org\hsqldb\NIOScaledRAFile.java
copy org\hsqldb\util\*Swing.java ..\temp\
del org\hsqldb\util\*Swing.java
javac -O -nowarn -d ../classes -classpath "%classpath%;../classes;../lib/servlet.jar;." ./*.java org/hsqldb/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
copy ..\temp\jdbcDataSource*.java org\hsqldb\jdbc
copy ..\temp\jdbcSavepoint.java org\hsqldb\jdbc
copy ..\temp\jdbcParameterMetaData.java org\hsqldb\jdbc
copy ..\temp\NIOLockFile.java org\hsqldb
copy ..\temp\NIOScaledRAFile.java org\hsqldb
del ..\temp\jdbcDataSource*.java
del ..\temp\jdbcSavepoint.java
del ..\temp\jdbcParameterMetaData.java
del ..\temp\NIOLockFile.java
del ..\temp\NIOScaledRAFile.java
copy ..\temp\*Swing.java org\hsqldb\util
del ..\temp\*Swing.java
rmdir ..\temp
cd ..\classes
jar -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class
cd ..\build
pause

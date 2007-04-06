@echo HSQLDB build file for jdk 1.2.x and 1.3.x
@echo *** we recommend the use of the ANT build.xml instead of this method
@echo for all jdk's include the path to jdk1.x.x\bin in your system path statement
cd ..\
md classes
del /s classes\*.class
cd src
mkdir ..\temp
copy org\hsqldb\jdbc\jdbcDataSource*.java ..\temp\
copy org\hsqldb\jdbc\jdbcSavepoint.java ..\temp\
copy org\hsqldb\jdbc\jdbcParameterMetaData.java ..\temp\
copy org\hsqldb\persist\NIOLockFile.java ..\temp\
copy org\hsqldb\persist\NIOScaledRAFile.java ..\temp\
del org\hsqldb\jdbc\jdbcDataSource*.java
del org\hsqldb\jdbc\jdbcSavepoint.java
del org\hsqldb\jdbc\jdbcParameterMetaData.java
del org\hsqldb\persist\NIOLockFile.java
del org\hsqldb\persist\NIOScaledRAFile.java
javac -O -nowarn -d ../classes -classpath "%classpath%;../classes;../lib/servlet.jar;." ./*.java org/hsqldb/*.java org/hsqldb/jdbc/*.java org/hsqldb/persist/*.java org/hsqldb/rowio/*.java org/hsqldb/scriptio/*.java org/hsqldb/store/*.java org/hsqldb/lib/*.java org/hsqldb/util/*.java
copy ..\temp\jdbcDataSource*.java org\hsqldb\jdbc
copy ..\temp\jdbcSavepoint.java org\hsqldb\jdbc
copy ..\temp\jdbcParameterMetaData.java org\hsqldb\jdbc
copy ..\temp\NIOLockFile.java org\hsqldb\persist
copy ..\temp\NIOScaledRAFile.java org\hsqldb\persist
del ..\temp\jdbcDataSource*.java
del ..\temp\jdbcSavepoint.java
del ..\temp\jdbcParameterMetaData.java
del ..\temp\NIOLockFile.java
del ..\temp\NIOScaledRAFile.java
rmdir ..\temp
cd ..\classes
copy ..\src\org\hsqldb\util\*.gif org\hsqldb\util
jar -cf ../lib/hsqldb.jar *.class org/hsqldb/*.class org/hsqldb/jdbc/*.class org/hsqldb/persist/*.class org/hsqldb/rowio/*.class org/hsqldb/scriptio/*.class org/hsqldb/store/*.class org/hsqldb/lib/*.class org/hsqldb/util/*.class  org/hsqldb/util/*.gif
cd ..\build
pause

cd ..\
md classes
del /s classes\*.class
cd build
cd ..\src\org\hsqldb\util
javac -d ..\..\..\..\classes CodeSwitcher.java
cd ..\..\..\..\build
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcStubs.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcCallableStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcConnection.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcDatabaseMetaData.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcPreparedStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcResultSet.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/jdbc/jdbcStatement.java +JAVA2 -JDBC3
java -classpath "%classpath%;../classes" org.hsqldb.util.CodeSwitcher ../src/org/hsqldb/HsqlDateTime.java +JAVA2 -JDBC3

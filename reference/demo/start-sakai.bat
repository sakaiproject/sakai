@echo off

set JAVA_OPTS=-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=256m -Dsakai.demo=true -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false
set CATALINA_OPTS=-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=256m -Dsakai.demo=true -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false

set CATALINA_HOME=
set CATALINA_BASE=

bin\startup.bat


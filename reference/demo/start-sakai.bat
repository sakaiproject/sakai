@echo off

set JAVA_OPTS=-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=512m -Dhttp.agent=Sakai -Dsakai.demo=true -Djava.awt.headless=true -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false -Dorg.apache.jasper.compiler.Parser.STRICT_WHITESPACE=false -Dsun.lang.ClassLoader.allowArraySyntax=true
set CATALINA_OPTS=-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=512m -Dhttp.agent=Sakai -Dsakai.demo=true -Djava.awt.headless=true -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false -Dorg.apache.jasper.compiler.Parser.STRICT_WHITESPACE=false -Dsun.lang.ClassLoader.allowArraySyntax=true

set CATALINA_HOME=
set CATALINA_BASE=

bin\startup.bat


#!/bin/sh

JAVA_OPTS="-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=512m -Dhttp.agent=Sakai -Dsakai.demo=true -Djava.awt.headless=true -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false -Dorg.apache.jasper.compiler.Parser.STRICT_WHITESPACE=false -Dsun.lang.ClassLoader.allowArraySyntax=true"
export JAVA_OPTS

CATALINA_OPTS="-server -Xmx1024m -XX:MaxNewSize=256m -XX:MaxPermSize=512m -Dhttp.agent=Sakai -Dsakai.demo=true -Djava.awt.headless=true -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false -Dorg.apache.jasper.compiler.Parser.STRICT_WHITESPACE=false -Dsun.lang.ClassLoader.allowArraySyntax=true"
export CATALINA_OPTS

# Demo failed to start correctly if CATALINA_HOME / CATALINA_BASE is set SAK-11553
unset CATALINA_HOME CATALINA_BASE 

bin/startup.sh; tail -f logs/catalina.out

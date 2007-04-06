How to build Hsqldb documentation from DocBook source
-----------------------------------------------------

$Id: readmedocbook.txt,v 1.7 2005/07/25 21:05:46 unsaved Exp $

1.  OBTAIN REQUIRED LIBRARIES AND STYLE SHEETS

If you are using a version of Java earlier than 1.4, then you will
have to get XML and XSLT libraries supported by DocBook.

(Java 1.4 users skip to the next paragraph).
I suggest the Xerces XML library and Xalan XSLT libraries, both
of which are available in the Xalan Java 2 distribution file
xalan-j-*-bin.* (not xalan-j--bin-2jars.*), available at 
http://www.apache.org/dyn/closer.cgi/xml/xalan-j
You will need to put the jars xalan.jar, xml-apis.jar, and xercesImpl.jar
into your classpath.

If you are using the Sun JDK 1.4, and get an exception when building
the docbooks, you may need newer Xerces / Xalan libraries. See also
http://xml.apache.org/xalan-j/faq.html

If you are building on one of the Sourceforge shell servers, then you can 
skip this step because I have all of the needed resources under
/home/users/us/unsaved.  You shouldn't build docs on Sourceforge unless
you are a developer who is building the documentation for publishing.

You need the following resources.
In all cases, use the latest stable version unless there is some
specific reason to use something else.

    ant-contrib*.jar from ant-contrib-*-bin.*, available from 
    http://sourceforge.net/project/showfiles.php?group_id=36177
    Place this jar into the lib subdirectory of your Ant installation.
    [Some distros spread the Ant files all over the place, and the
    lib directory isn't referenced at all.  In that case, just add
    the jar file to your classpath (exporting if needed by your shell)
    before running ant.

    The docbook-xsl package from 
  http://sourceforge.net/project/showfiles.php?group_id=21935&package_id=16608
    (or, for Linux RPM users, the docbook-xsl-stylesheets package).
    You need at least version 1.61.
    Extract anyplace.

That's all if you just want to build HTML docs.  If you want to build
PDF or postscript, then you also need the following.

    Binary distro of FOP, available from
    http://www.apache.org/dyn/closer.cgi/xml/fop
    Extract it anyplace.

    JimiProClasses.zip from Sun's JIMI SDK, available at 
    http://java.sun.com/products/jimi
    Just pull the JimiProClasses.zip file out and rename it to 
    jimi-1.0.jar in the lib subdirectory of your FOP installation.
    (If this seems weird to you, read about it in the FOP docs.
    Note that we use JIMI instead of JAI because JAI is non-portable).


2.  MAKE A build.properties TO TELL Ant WHERE TO FIND THE LIBRARIES AND
    STYLE SHEETS

Create a Java properties file named "build.properties" in the same directory 
as this readme file.  (If there's already one there, then just add to it).
Add values for 'docbook.xsl.home' and for 'fop.home'.
This example build.properties file (which works on cvs.sourceforge.net)
shows what is needed.

    docbook.xsl.home: /home/users/u/un/unsaved/docbook-xsl-1.65.1
    fop.home: /home/users/u/un/unsaved/fop-0.20.5

(RPM users be aware that docbook-xsl-stylesheets installs the style
sheets under /usr/share/sgml/docbook/docbook-xsl-stylesheets.  Run
"rpm -ql docbook-xsl-stylesheets").



THE REMAINDER OF THIS DOCUMENT IS ONLY FOR PEOPLE MESSING WITH THE
BUILD FILE ITSELF.  There's no need to understand this stuff if you
are only using external targets.

RELATIONSHIPS AMONG THE HSQLDB DOCBOOK-RELATED ANT TARGETS:

* denotes inheritall invocations.

docbook
    docbooks-html   ->                                     html
    docbooks-chunk  ->  *-ddIterate ->*-condlGenBook  ->   chunk
    docbooks-pdf    ->                                     pdf
   [docbooks-ps     ->]                                    ps

pdf/ps
    fo
    *-fop  ->  -initfop

chunk/html/fo
    -setXslFilesets
    -preDocbook
    *-htmlXslt/*-foXslt
    -postDocbook


-preDocbook  ->  *-sandwich

-postDocbook


(There's a good chance that this relationship "diagram" will get out-of-date.
Best to use this as a guide and verify any assumptions against the real
build.xml file).

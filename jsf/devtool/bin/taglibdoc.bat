REM Generate Sakai tag library (TLD) HTML documentation

REM Uncomment this block if you'd like to generate TLD documentation for the standard 
REM TLDs and the old (Sakai 1.5) TLD at the same time.
REM SET JSF_HOME=c:\dev\jsf\jsf-1_1
REM SET MYFACES_HOME=c:\dev\myfaces-1.0.9
REM SET STD_TLDS=%JSF_HOME%\lib\jsf_core.tld %JSF_HOME%\lib\html_basic.tld

SET TLD_DIR=%CD%\..\..\widgets\src\META-INF
SET NEW_TLDS=%TLD_DIR%\sakai-jsf.tld
SET PREVDIR=%CD%

SET DEST=%CD%\..\target\taglibdoc

rmdir /S /Q %DEST%
mkdir %DEST%

java -jar tlddoc.jar -d %DEST% %NEW_TLDS% %STD_TLDS% %OLD_TLDS%

cd /D %DEST%
zip -r ..\taglibdoc.zip *

cd /D %PREVDIR%
start %DEST%\index.html



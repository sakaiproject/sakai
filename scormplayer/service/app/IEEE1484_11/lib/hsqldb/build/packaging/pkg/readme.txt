README FOR THE SOLARIS HSQLDB PACKAGE

$Id: readme.txt,v 1.14 2005/07/28 15:47:19 unsaved Exp $


JAVA SUPPORT

The delivered hsqldb.conf uses /usr/bin/java (which on Solaris
is equivalent to /bin/java since /bin is just a sym-link to /usr/bin).
To use some other JRE, set JAVA_EXECUTABLE in /etc/hsqldb.conf.

The hsqldb.jar file was built with Sun Java 1.5.0.  It works with
Sun JRE's 4.x and 5.x...  You can certainly use this package with
earlier versions of Java, but you will have to build a hsqldb.jar
file for your Java version.  All you need to do so is a HSQLDB
distribution (this package will do, but you could also build it
on another computer, even with a different operating system).
a recent version of Ant, and a Java 1.3 JDK.  It's pretty easy.
Follow the instructions in the Building chapter of the HSQLDB User
Guide at the bottom of the Building With Ant subsection.
If you are going to run Java 1.x on Solaris (why!!!?), read the
next paragraph.  If you use this package with an IBM or open source
JRE, please let me know how it works.  If I get another job where I
need to run a non-Sun JRE on Solaris, I'll make whatever changes
are necessary.  You can run multiple HSQLDB versions under different
JRE's, or toggle back and forth, by renaming your hsqldb.jar's to
indicate the JRE target version (like "hsqldb-jre23.jar" and
"hsqldb-jre45.jar"), and changing the settings
JAVA_EXECUTABLE and HSQLDB_JAR_PATH in /etc/hsqldb.conf as needed.

Sun JRE 1.x USERS.  (Do read the previous paragraph about
rebuilding hsqldb.jar).
Sym-links don't work right for at least some Java 1.x builds.  
Make sure that every element of your JAVA_EXECUTABLE path is a 
real directory (as opposed to a sym link).
Known bug with Java 1.x:  If you run the daemons as root (which is
not the default), the default shutdown method fails and results in
a long wait before shutdown with TERM signal succeeds.  If this
bothers you, then upgrade your java, don't run as root, or set a 
short timeout in the hsqldb config file.

runUtil.sh and demo scripts:  Don't use them.  They are there for 
backwards compatibility only.  Run "man java" to learn how to 
execute the HSQLDB classes.



CONFIGURATION

Main config file is /etc/hsqldb.conf on Solaris.

You can have multiple versions of hsqldb installed, and you can
have them installed to the same or different install bases (like 
/usr and /usr/local).  Defaults to /opt.  To keep these 
different baselines straight, the hsqldb homes have a 
version-number in their name.  The last installation gets a 
sym-link called "hsqldb" (i.e., no version in it) right at the 
install base.  So, to use the default (last) installation at any 
install base, just access "hsqldb".  Example

    /opt/hsqldb-1.7.1   (default location on Solaris)
    /opt/hsqldb -> hsqldb-1.7.1   (sym-link to default hsqldb instance)

By default, /etc/init.d/hsqldb will start up one HSQLDB Server which
serves one database instance living at /opt/hsqldb/data/db1.  It will
run on the default Server port 9001 under user "hsqldb".  You can
customize this in lots of ways by editing /etc/hsqldb.conf and/or
/opt/hsqldb/data/*server.properties files.  You can, for example, 
serve standard hsql on multiple ports, plus http over multiple ports, 
some with SSL encrytion.  Each port can serve its own list of database 
instances of various types (memory-only, file, jar, etc.).  If 
multiple ports specify the same DB instance, then they will serve out 
a single, shared DB instance, just like you would want.  All of this
happening in a single JVM instance.  See the comments in 
/etc/hsqldb.conf and the HSQLDB User Guide for information on how to 
do all this stuff.

If you need JVM isolation for any reason, then you'll need to copy
/etc/init.d/hsqldb to something else (perhaps /etc/init.d/hsqldb-alt)
and edit this to use a different config file (perhaps 
/etc/hsqldb-alt.conf).  Edit your new conf file as necessary and
set up your hard links in /etc/rc?.d as required.  (Linux users be
aware that Solaris uses hard links for this, not sym links, and that
you use links only for runlevels with state "changes").

In general, we recommend against it, but if you want to run your servers 
as root, just change OWNER to root in /etc/hsqldb.conf and skip the rest 
of this file.

The rest of this file assumes that you are not running the daemons as
user 'root'.

By default, the daemons run as user 'hsqldb' (but you invoke the init
scripts as root).

You can not run a WebServer on the default port of 80 (since 80 is a 
privileged port).  See the Advanced chapter of the HSQLDB User Guide.



SOLARIS

    To install the Solaris package

	uncompress HSQLDBhsqldb-1_8_1-solaris.pkg.Z
	pkgadd -d HSQLDBhsqldb-1_8_1-solaris.pkg HSQLDBhsqldb

    (The version number will vary, of course).

    To install to an install base other than /opt, make an Admin
    file (like copy /var/sadm/install/admin/default) and set
    "basedir" whatever you want, then specify the Admin file to
    pkgadd with -a.

	pkgadd -a file.admin -d HSQLDBhsqldb-1_7_1-solaris.pkg HSQLDBhsqldb


    MULTIPLE INSTANCES

    You can install multiple instances of HSQLhsqldb, as long as
    the version is unique.  If you want more than one copy of the
    same version, then you will have to copy files manually because
    Solaris doesn't permit that.  If pkgadd refuses to let you 
    install an additional package even though the version is 
    unique, then you probably need to set the Admin file variable
    "instance" to "unique".

    If you don't understand what I say about Admin files,  run
    "man pkgadd" and "man -s 4 admin".


HSQLDB DEVELOPERS

Most of the files in .../pkg/cfg are named like HSQLhsqldb.something.
The intention was for the base name to be the entire package name, so
they should be HSQLDBhsqldb.something.  They will probably be renamed
properly in some future version.

To build a Solaris package, you need to do a cvs checkout of the
hsqldb-dev module (HEAD or a static tag, depending on what you
want).  For suggestions of the checkout command, click the CVS tab at
http://sourceforge.net/projects/hsqldb.
Make sure to build documentation and hsqldb.jar before building this
package!

.../build/packaging/pkg/pkgbuild is the main script to build a Solaris 
package.  Give the -p switch to rebuild the prototype file 
(definitely need to do that if there were any changes to anything
in the software to be delivered... as opposed to just a version or
package parameter change).

Contents of the Solaris package.  The package will contain exactly what
is listed in the .../build/packaging/pkg/cfg/*.proto file which is 
generated by "pkgbuild -p".  Several files in the checkedout out module
are specifically not included in the Solaris package.  To find out
exactly what files are currently excluded, see the command beginning
with "perl -ni.safe -we" in the pkgbuild script.  At the time I am
writing this, the perl command excludes the following:

    $HSQLDB_HOME/classes/...
    $HSQLDB_HOME/build/packaging
    $HSQLDB_HOME/.../CVS...   (Not necessary if you ran "cvs... export")
    $HSQLDB_HOME/lib/hsqldb.jar


Blaine
blaine.simpson@admc.com
unsaved at Sourceforge.net

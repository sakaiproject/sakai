This is a set of raw notes about how much I had to go through to make SOAP work on my Mac.

It is incomplete but better than nothing.

URLs:

http://www.entropy.ch/software/macosx/php/
http://www.phppatterns.com/index.php/article/articleview/39/1/2/
http://pear.php.net/package/SOAP

Upgrade to php5 - this may not be necessary but I did it

edit /usr/local/php5/lib/php/php.ini and turn on errors or your brain will explode.

; Print out errors (as a part of the output).  For production web sites,
; you're strongly encouraged to turn this feature off, and use error logging
; instead (see below).  Keeping display_errors enabled on a production web site
; may reveal security information to end users, such as file paths on your Web
; server, your database schema or other information.
display_errors = On

Convince pear to install extensions in the new php5 area

sh-2.05b# pear config-set php_dir /usr/local/php5/lib/php

sh-2.05b# pear config-show
Configuration:
==============
PEAR executables directory     bin_dir         /usr/bin
PEAR documentation directory   doc_dir         /usr/lib/php/docs
PHP extension directory        ext_dir         /usr/lib/php/extensions/no-debug-non-zts-20020429
PEAR directory                 php_dir         /usr/local/php5/lib/php
PEAR Installer cache directory cache_dir       /tmp/pear/cache
PEAR data directory            data_dir        /usr/lib/php/data
PHP CLI/CGI binary             php_bin         /usr/bin/php
PEAR test directory            test_dir        /usr/lib/php/tests
Cache TimeToLive               cache_ttl       3600
Preferred Package State        preferred_state stable
Unix file mask                 umask           22
Debug Log Level                verbose         1
HTTP Proxy Server Address      http_proxy      <not set>
PEAR server                    master_server   pear.php.net
PEAR password (for             password        <not set>
maintainers)
Signature Handling Program     sig_bin         /usr/local/bin/gpg
Signature Key Directory        sig_keydir      /private/etc/pearkeys
Signature Key Id               sig_keyid       <not set>
Package Signature Type         sig_type        gpg
PEAR username (for             username        <not set>
maintainers)

Now install SOAP using pear.  Notice that it does not do pre-req's automatically so 
I do the pre-reqs and then re-install SOAP.  I found the only way to find the version
was to manually download the darn thing.

sh-2.05b# pear install soap
No release with state equal to: 'stable' found for 'soap'
sh-2.05b# cd /tmp
sh-2.05b# pear download soap
File SOAP-0.8.1.tgz downloaded (69177 bytes)
sh-2.05b# pear install soap-0.8.1
downloading SOAP-0.8.1.tgz ...
Starting to download SOAP-0.8.1.tgz (69,177 bytes)
.................done: 69,177 bytes
requires package `Mail_Mime'
requires package `HTTP_Request'
requires package `Net_URL'
requires package `Net_DIME'
SOAP: Dependencies failed
sh-2.05b# pear install Net_DIME
No release with state equal to: 'stable' found for 'Net_DIME'
sh-2.05b# pear download Net_DIME
File Net_DIME-0.3.tgz downloaded (6740 bytes)
sh-2.05b# pear download Net_DIME-0.3
File Net_DIME-0.3.tgz downloaded (6740 bytes)
sh-2.05b# pear install Net_DIME-0.3
downloading Net_DIME-0.3.tgz ...
Starting to download Net_DIME-0.3.tgz (6,740 bytes)
.....done: 6,740 bytes
install ok: Net_DIME 0.3
sh-2.05b# pear install Net_URL     
downloading Net_URL-1.0.14.tgz ...
Starting to download Net_URL-1.0.14.tgz (5,173 bytes)
.....done: 5,173 bytes
install ok: Net_URL 1.0.14
sh-2.05b# pear install HTTP_Request
downloading HTTP_Request-1.2.4.tgz ...
Starting to download HTTP_Request-1.2.4.tgz (13,212 bytes)
.....done: 13,212 bytes
requires package `Net_Socket' >= 1.0.2
HTTP_Request: Dependencies failed
sh-2.05b# pear upgrade Net_Socket
downloading Net_Socket-1.0.6.tgz ...
Starting to download Net_Socket-1.0.6.tgz (4,623 bytes)
.....done: 4,623 bytes
upgrade ok: Net_Socket 1.0.6
sh-2.05b# pear install HTTP_Request
downloading HTTP_Request-1.2.4.tgz ...
Starting to download HTTP_Request-1.2.4.tgz (13,212 bytes)
.....done: 13,212 bytes
install ok: HTTP_Request 1.2.4
sh-2.05b# pear install Mail_MIME
downloading Mail_Mime-1.3.0.tgz ...
Starting to download Mail_Mime-1.3.0.tgz (16,417 bytes)
......done: 16,417 bytes
install ok: Mail_Mime 1.3.0
sh-2.05b# pear install soap-0.8.2
No release with version '0.8.2' found for 'soap'
sh-2.05b# pear install soap-0.8.1
downloading SOAP-0.8.1.tgz ...
Starting to download SOAP-0.8.1.tgz (69,177 bytes)
.................done: 69,177 bytes
install ok: SOAP 0.8.1
sh-2.05b# 

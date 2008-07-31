#!/usr/bin/perl
use strict;
use warnings;

sub ignorePatch() {
	my ($line) = @_;
        $line=~/content-impl-providers\/impl\/src\/java\/org\/sakaiproject\/content\/providers/ && return 1;
        $line=~/memory-tool\/tool\/src/ && return 1;
        $line=~/site-help\/src\/sakai_sites/ && return 1;
        $line=~/pom.xml/ && return 1;
        $line=~/.classpath/ && return 1;
        $line=~/content-impl-providers\/impl/ && return 1;
        $line=~/content-impl-providers\/integration-test/ && return 1;
        $line=~/content-impl-providers\/pack/ && return 1;
        $line=~/alias-tool\/tool\/src/ && return 1;
        $line=~/content-tool\/tool\/src/ && return 1;
        $line=~/authz-tool\/tool\/src/ && return 1;
        $line=~/content-help\/src/ && return 1;
        $line=~/user-tool-prefs\/tool/ && return 1;
        $line=~/user-tool\/tool/ && return 1;

        $line=~/site-tool\/tool\/src/ && return 1;
        $line=~/tool-tool\/su\/src/ && return 1;


	$line=~/user-impl\/integration-test/ && return 1;
        
        return 0;
}

sub notifyMissing() {
	my ($missing) = @_;
        $missing=~/kernel-impl\/src\/main\/sql/ && return 0;
        $missing=~/kernel-impl\/src\/main\/sql/ && return 0;
        $missing=~/kernel-impl\/src\/main\/java\/org\/sakaiproject\/content\/types/ && return 0;
        $missing=~/kernel-impl/ && return 0;
        $missing=~/api\/src\/main/ && return 0;
        $missing=~/component-manager\/src\/main/ && return 0;
        $missing=~/kernel-private\/src\/main/ && return 0;
        return 1;
}

sub rewriteLine() {
    my ($line) = @_;
    if ( $line=~/^Index:/ || $line=~/^\+\+\+/ || $line=~/^\-\-\-/ ) {
	$line=~s/alias-api\/api\/src\/java\/org\/sakaiproject\/alias/api\/src\/main\/java\/org\/sakaiproject\/alias/g;
	$line=~s/cluster-api\/api\/src\/java\/org\/sakaiproject\/cluster/api\/src\/main\/java\/org\/sakaiproject\/cluster/g;
	$line=~s/content-api\/api\/src\/java\/org\/sakaiproject\/content/api\/src\/main\/java\/org\/sakaiproject\/content/g;
	$line=~s/email-api\/api\/src\/java\/org\/sakaiproject\/email/api\/src\/main\/java\/org\/sakaiproject\/email/g;
	$line=~s/event-api\/api\/src\/java\/org\/sakaiproject\/event/api\/src\/main\/java\/org\/sakaiproject\/event/g;
	$line=~s/event-api\/api\/src\/java\/org\/sakaiproject\/event/api\/src\/main\/java\/org\/sakaiproject\/event/g;
        $line=~s/api\/src\/main\/java\/org\/sakaiproject\/event/api\/src\/main\/java\/org\/sakaiproject\/event/g;
	$line=~s/user-api\/api\/src\/java\/org\/sakaiproject\/user/api\/src\/main\/java\/org\/sakaiproject\/user/g;
	$line=~s/authz-api\/api\/src\/java\/org\/sakaiproject\/authz/api\/src\/main\/java\/org\/sakaiproject\/authz/g;
	$line=~s/component-api\/api\/src\/java\/org\/sakaiproject\/component/api\/src\/main\/java\/org\/sakaiproject\/component/g;
	$line=~s/db-api\/api\/src\/java\/org\/sakaiproject\/db/api\/src\/main\/java\/org\/sakaiproject\/db/g;
	$line=~s/entity-api\/api\/src\/java\/org\/sakaiproject\/entity/api\/src\/main\/java\/org\/sakaiproject\/entity/g;
	$line=~s/jcr-api\/api\/src\/java\/org\/sakaiproject\/jcr/api\/src\/main\/java\/org\/sakaiproject\/jcr/g;
	$line=~s/memory-api\/api\/src\/java\/org\/sakaiproject\/memory/api\/src\/main\/java\/org\/sakaiproject\/memory/g;
	$line=~s/site-api\/api\/src\/java\/org\/sakaiproject\/site/api\/src\/main\/java\/org\/sakaiproject\/site/g;
	$line=~s/tool-api\/api\/src\/java\/org\/sakaiproject\/tool/api\/src\/main\/java\/org\/sakaiproject\/tool/g;
	$line=~s/util-api\/api\/src\/java\/org\/sakaiproject/api\/src\/main\/java\/org\/sakaiproject/g;
	$line=~s/alias-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/cluster-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/content-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/content-jcr-impl\/impl\/src\/sql/kernel-impl\/content\/jcr\/sakai-content-jcr-impl\/src\/main\/sql/g;
	$line=~s/email-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/event-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/user-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/authz-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/component-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/db-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/entity-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/jcr-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/jcr-support\/impl\/src\/java\/org\/sakaiproject\/jcr/kernel-impl\/src\/main\/java\/org\/sakaiproject\/jcr/g;

	$line=~s/memory-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/site-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/tool-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/util-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/cluster-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/content-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/content-jcr-impl\/impl\/src\/java/kernel-impl\/content\/jcr\/sakai-content-jcr-impl\/src\/main\/java/g;
	$line=~s/email-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/event-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/user-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/authz-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/component-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/db-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/entity-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/jcr-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/memory-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/site-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/tool-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/util-impl\/impl\/src\/java/kernel-impl\/src\/main\/java/g;
	$line=~s/cluster-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/content-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/content-jcr-impl\/impl\/src\/test/kernel-impl\/content\/jcr\/sakai-content-jcr-impl\/src\/test\/java/g;
	$line=~s/email-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/event-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/user-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/authz-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/component-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/db-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/entity-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/jcr-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/memory-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/site-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/tool-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/util-impl\/impl\/src\/test/kernel-impl\/src\/test\/java/g;
	$line=~s/cluster-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/content-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/content-jcr-impl\/impl\/src\/bundle/kernel-impl\/content\/jcr\/sakai-content-jcr-impl\/src\/main\/resources/g;
	$line=~s/email-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/event-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/user-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/authz-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/component-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;


	$line=~s/db-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/entity-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/jcr-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/memory-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/site-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/tool-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/util-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;

	$line=~s/component-api\/component\/src\/java\/org\/sakaiproject/component-manager\/src\/main\/java\/org\/sakaiproject/g;
	$line=~s/component-api\/component\/src\/config\/org\/sakaiproject\/config/component-manager\/src\/main\/bundle\/org\/sakaiproject\/config/g;
	$line=~s/content-jcr-migration-api\/src\/java\/org\/sakaiproject\/content/kernel-impl-experimental\/content\/migration\/sakai-content-jcr-migration-api\/src\/main\/java\/org\/sakaiproject\/content/g;
	$line=~s/runconversion.sh/tools\/runconversion.sh/g;
	$line=~s/upgradeschema-mysql.config/tools\/content-conversion\/upgradeschema-mysql.config/g;
	$line=~s/upgradeschema-oracle.config/tools\/content-conversion\/upgradeschema-oracle.config/g;
	$line=~s/content-bundles/kernel-impl\/src\/main\/resources/g;
	$line=~s/content-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/content-impl\/impl\/src\/test\/org\/sakaiproject\/content\/impl\/serialize\/impl\/test/kernel-impl\/src\/test\/java\/org\/sakaiproject\/content\/impl\/serialize\/impl\/test/g;
	$line=~s/content-impl\/impl\/src\/java\/org\/sakaiproject\/content/kernel-impl\/src\/main\/java\/org\/sakaiproject\/content/g;
	$line=~s/content-impl\/impl\/src\/config/kernel-impl\/src\/main\/config/g;
	$line=~s/content-impl\/impl\/src\/bundle/kernel-impl\/src\/main\/resources/g;
	$line=~s/readme.txt/tools\/content-conversion\/readme.txt/g;
	$line=~s/content-jcr-migration-impl\/src\/java\/org\/sakaiproject\/content\/migration/kernel-impl-experimental\/content\/migration\/sakai-content-jcr-migration-impl\/src\/main\/java\/org\/sakaiproject\/content\/migration/g;
	$line=~s/db-util\/storage\/src\/java\/org\/sakaiproject\/util/kernel-util\/db-storage\/src\/main\/java\/org\/sakaiproject\/util/g;
	$line=~s/db-util\/conversion\/runconversion.sh/tools\/content-conversion\/runconversion.sh/g;
	$line=~s/db-util\/conversion\/src\/java\/org\/sakaiproject\/util\/conversion/kernel-util\/db-conversion\/src\/main\/java\/org\/sakaiproject\/util\/conversion/g;
	$line=~s/db-impl\/ext\/src\/java\/org\/apache\/commons\/dbcp/api\/src\/main\/java\/org\/apache\/commons\/dbcp/g;
	$line=~s/db-impl\/impl\/src\/sql/kernel-impl\/src\/main\/sql/g;
	$line=~s/util-util\/util\/src\/java\/org\/sakaiproject\/util/kernel-util\/util\/src\/main\/java\/org\/sakaiproject\/util/g;

        $line=~s/jackrabbit-impl\/impl\/src\/java\/org\/sakaiproject\/jcr/kernel-impl\/src\/main\/java\/org\/sakaiproject\/jcr/g;
        $line=~s/jackrabbit-impl\/impl\/src\/java\/org\/apache/kernel-impl\/src\/main\/java\/org\/apache/g;
        $line=~s/jackrabbit-impl\/impl\/src\/test\/org\/sakaiproject\/jcr/kernel-impl\/src\/test\/java\/org\/sakaiproject\/jcr/g;




        $line=~s/user-impl\/integration-test\/src\/test\/java\/org\/sakaiproject\/user/kernel-test\/user\/src\/test\/java\/java\/org\/sakaiproject\/user/g;

        $line=~s/util-util\/util\/src\/java\/org\/sakaiproject\/webapp\/impl/kernel-impl\/src\/main\/java\/org\/sakaiproject\/webapp\/impl/g;


        $line=~s/db-impl\/ext\/src\/java\/org\/sakaiproject\/springframework\/orm\/hibernate/kernel-private\/src\/main\/java\/org\/sakaiproject\/springframework\/orm\/hibernate/g;
 

	$line=~s/entity-util\/util\/src\/java\/org\/sakaiproject\/util\/serialize/kernel-util\/entity\/src\/main\/java\/org\/sakaiproject\/util\/serialize/g;
	$line=~s/event-util\/util\/src\/java\/org\/sakaiproject\/util/kernel-util\/event\/src\/main\/java\/org\/sakaiproject\/util/g;
 
        
        $line=~s/content-impl-jcr\/pack\/src\/webapp\/WEB-INF\/components-jcr.xml/kernel-component\/src\/main\/webapp\/WEB-INF\/content-jcr-components-jcr.xml/g;

        $line=~s/content-impl-jcr\/pack\/src\/webapp\/WEB-INF\/components.xml/kernel-component\/src\/main\/webapp\/WEB-INF\/content-jcr-components.xml/g;
              

        $line=~s/db-impl\/pack\/src\/webapp\/WEB-INF\/components.xml/kernel-component\/src\/main\/webapp\/WEB-INF\/db-components.xml/g;
        $line=~s/event-impl\/pack\/src\/webapp\/WEB-INF\/components.xml/kernel-component\/src\/main\/webapp\/WEB-INF\/event-components.xml/g;
        $line=~s/user-impl\/pack\/src\/webapp\/WEB-INF\/components.xml/kernel-component\/src\/main\/webapp\/WEB-INF\/user-components.xml/g;

        $line=~s/component-impl\/integration-test\/src\/test\/java/kernel-test\/component\/src\/test\/java/g;



    } 
    return $line;
}

my $fh;
my $n=0;
my $ignore=0;
while (<>) {
    if (/^Index:/) {
        my $ln = $_;
        if ( &ignorePatch($ln) ) {
           $ignore = 1;
  #         print "Ignoring patch ".$_;
        } else {
          $ignore = 0;
	  chomp;
          my $test =  &rewriteLine($_);
          my @target = split(/[:]/, $test );
          my @f = split( /[:]/, $_ );
        
          my $file = $f[1];
          my $rewriteFile = "..\/".$target[1];
          $rewriteFile=~s/ //;
          if ( ! -f $rewriteFile ) {
            if ( &notifyMissing($rewriteFile) ) {
	        print "Warning Target Does not appear to exist $rewriteFile\n";
            }
          }
          $file=~s/\//_/g;
          $file=~s/ /_/g;
          $file=sprintf("%04d_%s",$n,$file);
          open( $fh, ">>", $file ) or die "Can't open $file: $!\n";
          $n++;
           print $fh &rewriteLine($ln);
       }
    } else {
       if ( ! $ignore ) {
         print $fh &rewriteLine($_);
       }
    }
}


1;

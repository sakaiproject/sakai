#! /usr/bin/perl

# This script is for synchronizing help content in the Sakai svn repository with 
# help documents maintained in the Indiana University Knowledge Base (kb.iu.edu)
# It is not intended to be run by sites deploying Sakai.

use strict;

use XML::Simple;
use Data::Dumper;
use File::Copy;
use Getopt::Long;
use File::Basename;
my $cwd = dirname(__FILE__);

require $ENV{"HOME"}.'/kbauth.pl';
require 'helputil.pl';

(my $username, my $password) = getKbAuth();

my $KbBaseUrl = "http://remote.kb.iu.edu/REST/v0.2";

my $KbMediaUrl = "https://media.kb.iu.edu/";

my $xsl = "$cwd/kb-to-help.xsl";

#my $docrepo = $ARGV[0];
#my $svnrepo = $ARGV[1];
#my $preview = $ARGV[2];

my $docrepo;
my $svnrepo;
my $preview;
my $offline = 0;

my $result = GetOptions ("docs=s" => \$docrepo,
   "svn=s" => \$svnrepo,
   "preview=s" => \$preview, 
   "offline" => \$offline);  

my $docrepo = "$cwd/docs";
my $svnrepo = "$cwd/sakai-trunk";
my $preview = "sakai29";

my $update_from_kb = !$offline;

print "\nUsing documents in [$docrepo], svn repo [$svnrepo], preview [$preview], offline [$offline]\n";

die "Please specify document path." if !defined($docrepo) || $docrepo eq "";
die "Please specify svn path." if !defined($svnrepo) || $svnrepo eq "";

my $svn_comment = "NOJIRA Update help docs (synchronize with IU KB)";
(my $svn_user, my $svn_pass) = getSvnAuth();
my $svn = "/usr/bin/svn";

### ----- Get the index documents from the KB

if ($update_from_kb) {
 
# create XML Parser
my $xml = new XML::Simple (KeyAttr=>'id');

# Current documents and new documents

print "Fetching indexes\n";
getfile($username, $password, "$KbBaseUrl/sakaiht/documents", "$docrepo/docs_kb.xml") || die "Cannot get docs index\n";
getfile($username, $password, "$KbBaseUrl/sakainew/documents", "$docrepo/newdocs_kb.xml") || die "Cannot get newdocs index\n";

# read XML files
# If docs local doen't exist 
my $docs_local;
if (-e "$docrepo/docs.xml") {
	$docs_local = $xml->XMLin("$docrepo/docs.xml");
} 

my $newdocs_local;
if (-e "$docrepo/newdocs.xml") {
	$newdocs_local = $xml->XMLin("$docrepo/newdocs.xml");
}


my $newdocs_kb = $xml->XMLin("$docrepo/newdocs_kb.xml");
my $docs_kb = $xml->XMLin("$docrepo/docs_kb.xml");

# Fetch any update documents from docs or newdocs collections
# - timestamp is newer, or file doesn't exist locally

	#print $i . " " . $docs_local->{document}->{$i}->{timestamp}, "\n";

foreach my $docid (keys %{$docs_kb->{document}})
   {
      if ( (! -s "$docrepo/$docid.xml") ||
	   (! -s "$docrepo/$docid.html") ||
	   (!$docs_local || $docs_kb->{document}->{$docid}->{timestamp} ne $docs_local->{document}->{$docid}->{timestamp}) )
	{
		print "Fetching updated docid in docs collection: $docid\n";

		my $url = "$KbBaseUrl/sakaiht/document/sakaihelp/$docid.xml?domain=sakaiht\\&domain=sakainew";

		if (getfile($username, $password, $url, "$docrepo/$docid.xml"))
		{
		  transform("$docrepo/$docid.xml", "$docrepo/$docid.html", $xsl);
		}
	}
   }

foreach my $docid (keys %{$newdocs_kb->{document}})
   {
      if ( (! -s "$docrepo/$docid.xml") ||
	   (! -s "$docrepo/$docid.html") ||
	   (!$newdocs_local || $newdocs_kb->{document}->{$docid}->{timestamp} ne $newdocs_local->{document}->{$docid}->{timestamp}) )
	{
		print "Fetching updated docid in newdocs collection: $docid\n";

		my $url = "$KbBaseUrl/sakainew/document/sakaihelp/$docid.xml?domain=sakaiht\\&domain=sakainew";

		if (getfile($username, $password, $url, "$docrepo/$docid.xml"))
		{
		  transform("$docrepo/$docid.xml", "$docrepo/$docid.html", $xsl);
		}
	}
   }

# Check every doc in docs to see if there's a preview version available
# Slow but no more efficient way to do this

if ($preview ne "") {

 foreach my $docid (keys %{$docs_kb->{document}})
   {
	print "Checking preview for docid: $docid\n";

	my $url = "$KbBaseUrl/document/preview/sakaihelp/$docid/version/$preview?domain=sakaiht\\&domain=sakainew";

	if (getfile($username, $password, $url, "$docrepo/$docid-preview.xml"))
	{
	  my $kbe = `/bin/grep kberror $docrepo/$docid-preview.xml | /usr/bin/wc -l`;
	  chomp $kbe;
	  if ($kbe eq "0") {
	  	  print "Got preview docid in $preview collection: $docid\n";
		  rename("$docrepo/$docid-preview.xml","$docrepo/$docid.html");
		  transform("$docrepo/$docid.xml", "$docrepo/$docid.html", $xsl);
	  } else {
		  unlink("$docrepo/$docid-preview.xml");
	  }
	}
   }

}


# All updated, move the kb indexes to the local indexes
rename("$docrepo/docs_kb.xml","$docrepo/docs.xml");
rename("$docrepo/newdocs_kb.xml","$docrepo/newdocs.xml");

}

#### ----- Update the svn collection

print "Updating svn collection: $svnrepo from $docrepo\n";

update_svn_collection($svnrepo, $docrepo);

# Check for included images

checkimages($docrepo, $svnrepo . "/help/help-tool/src/webapp/TOCDisplay/image/", $KbMediaUrl);

#### ----- Commit changes to svn

if (!$offline) {
    #commit_svn_changes($svnrepo, $svn_user, $svn_pass, $svn_comment);
    echo "NOTICE: You need to commit your SVN changes!"
}

#### ----- Consistency checks on the document collections

print "Consistency check\n";

getdoclist($svnrepo);
checkmanifest($svnrepo);


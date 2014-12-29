use strict;
use XML::Parser::PerlSAX;
use LWP::Simple qw ($ua get);
use XML::Simple;
use Data::Dumper;
use File::Basename;
my $cwd = dirname(__FILE__);

my %imglist;
my %doclist;

sub getfile($$$$)
{
  my $username = shift;
  my $password = shift;
  my $url = shift;
  my $output = shift;

  my $wget = "/usr/bin/wget";

  # Get the file
  system("$wget --user=$username --password=$password --output-document=$output.tmp --quiet $url");

  # If the file exists and is non-zero, move it to it's final location
  if (-s "$output.tmp") {
	rename("$output.tmp", $output);
  	return 1;
  } else {
	unlink("$output.tmp");
 	return 0;
  }

}

sub transform($$$)
{
  my $in = shift;
  my $out = shift;
  my $xsl = shift;

  system("java -cp $cwd/xalan-2.6.0.jar org.apache.xalan.xslt.Process -in $in -out $out -xsl $xsl");
}

sub update_svn_collection ($$) 
{
 my $svnrepo = shift;
 my $docrepo = shift;

 # Match the pattern */src/*/*.html

 print "$svnrepo $docrepo\n";
 my @filelist = glob("$svnrepo/*/*-help/src/*/*.html");
 foreach my $helpfile (@filelist) {
    if ($helpfile =~ /\/([a-z]{4})\.html/) {
        my $fileid = $1;
        if (-s "$docrepo/$fileid.html") {
        	print "Helpfile for '$fileid' copied from '$docrepo'\n";
                copy("$docrepo/$fileid.html", $helpfile);
        }
    }
    else {
	print "No match for $helpfile\n";
    }
 }

 return;
}

sub commit_svn_changes($$$)
{
 my $svnrepo = shift;
 my $svn_user = shift;
 my $svn_pass = shift;
 my $svn_comment = shift;

 my $svn = "/usr/bin/svn";

 my @projectlist = glob("$svnrepo/*");

 print "Committing any svn changes:\n";

 foreach my $project (@projectlist) {
	system("$svn --username $svn_user --password $svn_pass -m \"$svn_comment\" ci $project");
 }
}

### Get a list of images referenced in the given 

sub checkimages($) 
{
 my $docrepo = shift;
 my $imagedir = shift;
 my $kbmedia = shift;

 %imglist = ();

 my $handler = SAXImgHandler->new();
 my $parser = XML::Parser::PerlSAX->new(Handler => $handler);

 my @files = glob("$docrepo/*.html");
 foreach my $file (@files) {
	my %parser_args = (Source => {SystemId => $file});
	$parser->parse(%parser_args);
 }

 print "\nImages in $docrepo:\n";
 foreach my $i (keys %imglist) {
	if ($i =~ /image\/(.*)/) {
		if (! -s "$imagedir/$1") {
		 	print "$i: $1 ADDING\n";
			addimage($i, $kbmedia, $imagedir, $1);
		} 
	}
 }

 return %imglist;
}

sub addimage ($$$)
{
  my $imgurl = shift;
  my $kbmedia = shift;
  my $imgdir = shift;
  my $imgbasename = shift;

  my $wget = "/usr/bin/wget";
  my $svn = "/usr/bin/svn";

  system("cd $imgdir ; $wget $kbmedia/$imgurl");
  system("$svn add $imgdir/$imgbasename");
}

### Check for referenced but not included files

sub getdoclist($)
{
 my $svnrepo = shift;

 %doclist = ();
 my %havedocs;

 my $handler = SAXDocHandler->new();
 my $parser = XML::Parser::PerlSAX->new(Handler => $handler);

 my @files = glob("$svnrepo/*/src/*/*.html");

 # Parse HTML files, but ignore i18n versions in other languages

 foreach my $file (@files) {
    if (! ($file =~ /_[a-z]{2}\.html$/) && !($file =~ /_[a-z]{2}_[A-Z]{2}\.html$/)) {
        my %parser_args = (Source => {SystemId => $file});
	# print "Parsing $file\n";
        $parser->parse(%parser_args);
	if ($file =~ /([A-Za-z]+).html/) {
		$havedocs{$1} = $1;
	}
    }
 }

 print "\nDocids in $svnrepo:\n";
 foreach my $i (keys %doclist) {
	if (!defined($havedocs{$i})) {
 		print "referenced but not included: $i\n";
	}
 }

 print "Have: " . keys(%havedocs) . " referenced: " . keys(%doclist) . "\n";

}

### Check that the files present match the help.xml index manifest

sub checkmanifest($)
{
 my $svnrepo = shift;

 # Check for English-language help index files
 my @files = glob("$svnrepo/*/src/*/help.xml");

 # Parse HTML files, but ignore i18n versions in other languages

 foreach my $file (@files) {

    if ($file =~ /([a-z0-9\/._-]+)\/src\/([a-z_]+)\/help.xml$/) {
       my $prefix= $1;
       my $tool = $2;

       # print "checking manifest: tool $tool\n";

       # Read in the help xml file
       
       my $xml = new XML::Simple(KeyAttr=>'id');;

       # read XML files
       my $helpindex = $xml->XMLin($file);

       my $reflist = $helpindex->{bean}->{'org.sakaiproject.api.app.help.TableOfContents'}->{property}[1]->{list}->{bean}->{property}[1]->{list}->{ref};

       my %manifest;

       if (ref($reflist) eq 'ARRAY') {
		foreach my $refs (@{$helpindex->{bean}->{'org.sakaiproject.api.app.help.TableOfContents'}->{property}[1]->{list}->{bean}->{property}[1]->{list}->{ref}}) {
                        my $bean = $refs->{bean};
			foreach my $property (@{$helpindex->{bean}->{$bean}->{property}}) {
				if ($property->{name} eq "location") {
	 				#print "bean: ". $refs->{bean} . " " . $property->{name} . " " . $property->{value} . "\n";
					$manifest{$bean} = $property->{value};
 				}
 			}
                }
       }  else { 
#		print "single bean: " . $reflist->{bean} . "\n";
                        my $bean = $reflist->{bean};
			foreach my $property (@{$helpindex->{bean}->{$bean}->{property}}) {
				if ($property->{name} eq "location") {
	 				#print "bean: ". $refs->{bean} . " " . $property->{name} . " " . $property->{value} . "\n";
					$manifest{$bean} = $property->{value};
 				}
 			}
       }

	# Forward check - each location referenced in the table of contents exists

       	foreach my $bean (keys %manifest) {
		# Check that the file exists
		my $beanpath = "$prefix/src" . $manifest{$bean};
                # print "checking for [$beanpath]\n";
		if (! -s "$beanpath") {
			print "Missing file for $tool : $bean : $beanpath\n";
		}
	}

	# Reverse check - each file is referenced in the help.xml

 	my @helpfiles = glob("$prefix/src/$tool/*.html");

 	foreach my $helpfile (@helpfiles) {

    		if (! ($helpfile =~ /_[a-z]{2}\.html$/) && !($helpfile =~ /_[a-z]{2}_[A-Z]{2}\.html$/)) {
			
			# print "checking that $helpfile is referenced\n";
			my $found = 0;
			foreach my $bean (keys %manifest) {
                		my $beanpath = "$prefix/src" . $manifest{$bean};
				if ($helpfile eq $beanpath) {
					$found = 1;
				}
			}
			if (!$found) {
				print "Help file $helpfile is not referenced in help.xml manifest\n";
			}

		}
    	}

    } 
 }

}

return 1;

# begin the in-line package (c/f examples at http://www.xml.com/pub/a/2001/02/14/perlsax.html)

package SAXImgHandler;

sub new {
    my $type = shift;
    return bless {}, $type;
}

sub start_element {
    my ($self, $element) = @_;

    if ($element->{Name} eq 'img') {
	$imglist{$element->{Attributes}->{'src'}} = $element->{Attributes}->{'src'};
    }
}

1;

# another SAX parser

package SAXDocHandler;

sub new {
    my $type = shift;
    return bless {}, $type;
}

sub start_element {
    my ($self, $element) = @_;

    if ($element->{Name} eq 'a') {
	## We want docids from <a href="content.hlp?docId=abcd">
	my $href = $element->{Attributes}->{'href'};
	if ($href =~ /^content.hlp\?docId=([A-Za-z]+)$/) {
        	$doclist{$1} = $1;
	}
    }
}


1;


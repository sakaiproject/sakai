use strict;
use XML::Parser::PerlSAX;
use LWP::Simple qw ($ua get);

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

  system("java -cp /usr/local/sakaihelp/jars/xalan-2.6.0.jar org.apache.xalan.xslt.Process -in $in -out $out -xsl $xsl");
}

sub update_svn_collection ($$) 
{
 my $svnrepo = shift;
 my $docrepo = shift;

 # Match the pattern */src/*/*.html

 my @filelist = glob("$svnrepo/*/src/*/*.html");
 foreach my $helpfile (@filelist) {
    if ($helpfile =~ /\/([a-z]{4})\.html/) {
        my $fileid = $1;
        if (-s "$docrepo/$fileid.html") {
                copy("$docrepo/$fileid.html", $helpfile);
        }
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

sub getdoclist($)
{
 my $svnrepo = shift;

 %doclist = ();
 my %havedocs;

 my $handler = SAXDocHandler->new();
 my $parser = XML::Parser::PerlSAX->new(Handler => $handler);

 my @files = glob("$svnrepo/*/src/*/*.html");

 foreach my $file (@files) {
        my %parser_args = (Source => {SystemId => $file});
        $parser->parse(%parser_args);
	if ($file =~ /([A-Za-z]+).html/) {
		$havedocs{$1} = $1;
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


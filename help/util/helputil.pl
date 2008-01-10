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
 my $svnrepo = $1;
 my $docrepo = $1;

 # Match the pattern */src/*/*.html

 my @filelist = glob("$svnrepo/*/src/*/*.html");

 foreach my $helpfile (@filelist) {
    if ($helpfile =~ /\/([a-z]{4})\.html/) {
        my $fileid = $1;
        if (-s "$DocRepo/$fileid.html") {
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
 my $svn_comment = shift;

 my $svn = "/usr/bin/svn";

 my @projectlist = glob("$svnrepo/*");

 foreach my $project (@projectlist) {
	system("$svn --username=$svn_user -m \"$svn_comment\" ci $project");
 }
}

return 1;

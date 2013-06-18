#wget http://mirrors.ibiblio.org/maven2/xalan/xalan/2.6.0/xalan-2.6.0.jar
#You have to have perl installed!
wget http://search.cpan.org/CPAN/authors/id/A/AP/APEIRON/local-lib-1.008004.tar.gz
tar zxvf local-lib-1.008004.tar.gz
cd local-lib-1.008004 && perl Makefile.PL --bootstrap --no-manpages
make test && make install
echo 'eval $(perl -I$HOME/perl5/lib/perl5 -Mlocal::lib)' >>~/.bashrc
source ~/.bashrc
cd ..
\rm -rf local-lib-1.008004.tar.gz

curl -k -L http://cpanmin.us | perl - --self-upgrade

PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install XML::Parser'
PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install LWP::Simple'
PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install Data::Dumper'
PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install File::Copy'
PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install Getopt::Long'
PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install XML::Simple'
PERL_MM_USE_DEFAULT=1 perl -MCPAN -Mlocal::lib -e 'install XML::Parser::PerlSAX'

#svn co https://source.sakaiproject.org/svn/sakai/trunk sakai-trunk
mkdir docs

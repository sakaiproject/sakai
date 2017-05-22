#! /usr/bin/perl -w

use strict;

my $file = 'context-menu.js';
my $outfile = $file.'-i18n';
my $langfile = 'en.js';

open FILE, "<$file";
#open OUTFILE, ">$outfile";
#open LANGFILE, ">$langfile";
my %texts = ();
while (<FILE>) {
    if (/"(.*?)"/) {
        my $inline = $_;
        chomp $inline;
        my $key = $1;
        my $val = $1;
        print "Key: [$key]: ";
        my $line = <STDIN>;
        if (defined $line) {
            chomp $line;
            if ($line =~ /(\S+)/) {
                $key = $1;
                print "-- using $key\n";
            }
            $texts{$val} = $key;
        } else {
            print " -- skipped...\n";
        }
    }
}
#close LANGFILE;
#close OUTFILE;
close FILE;

print "\n\n\n";
print '"', join("\"\n\"", sort keys %texts), '"', "\n";

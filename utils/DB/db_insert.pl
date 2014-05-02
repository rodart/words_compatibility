#!/usr/bin/env perl

use strict;
use warnings;

use DBI;
use File::Copy;
use File::Basename;
use utf8;

print "Start\n";

my $dbh = DBI->connect('DBI:mysql:words_compatibility', 'root', 'mysqlrootpw',
                      ) or die "Could not connect to database: $DBI::errstr";
$dbh->{'mysql_enable_utf8'} = 1;

my $project_path = '/home/boincadm/projects/words_compatibility/';
my @files = grep( basename($_) ne 'errors', glob("$project_path/sample_results/*") );

for my $file (@files) {
    open FILE, '<', $file or die "CANNOT OPEN $file for reading";

    while (defined (my $line = <FILE>)) {
        chomp $line;
        my ($word_from, $word_to, $relation, $num) = split(' ', $line);
        unless ($word_from or $word_to or $relation or $num) {
            print "ERROR: fail in parse line: $line\n";
            next;
        }
         
        $dbh->do('INSERT IGNORE INTO compatibility VALUES(?,?,?,?)', 
                  undef, $word_from, $word_to, $relation, $num);
    }

    close FILE;
    move($file, "$project_path/utils/DB/processed_files/" . basename($file));
    print "FINISH PROCESS $file\n";
}

$dbh->disconnect();

print "Finish all works\n";

1;


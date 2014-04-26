#!/usr/bin/env perl

use strict;
use warnings;

use File::Basename;
use utf8;

print "Start sentence generation\n";

my $project_path = '/home/boincadm/projects/words_compatibility/';
chdir("$project_path/utils/Searcher/") or die "cannot change: $!\n"; 

my $searcher = "searcher.jar";
my $index_dir = "$project_path/librusec/index/";

my @files = glob("$project_path/utils/WorkGeneration/words_lists/files/*");
for my $file (@files) {
    open FILE, '<', $file or die "CANNOT OPEN $file for reading" ;
    my $words_number = 1;
    while (defined (my $query_word = <FILE>)) {
        chomp $query_word;
        my $result_file = "$project_path/utils/WorkGeneration/sentences_lists/files/" . basename($file) . ".$words_number";
        $words_number++;
        my $cmd = "java -jar $searcher $index_dir $result_file $query_word 10000";
        system($cmd);
        print "$query_word processed, save result in $result_file\n";
    }
    close FILE;
}

print "Finish sentence generation\n";

1;


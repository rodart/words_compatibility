#!/usr/bin/env perl

use strict;
use warnings;

use File::Copy;
use File::Basename;

print("Start generation WUs\n");

my $project_path = '/home/boincadm/projects/words_compatibility/';
chdir($project_path) or die "cannot change: $!\n"; 

my @files = glob("$project_path/utils/WorkGeneration/sentences_lists/files/*");
for my $file (@files) {
        my $wu_name = basename($file);
        my $worker_conf = "bin/create_work -appname worker -wu_name $wu_name";
        my $template_conf = " -wu_template templates/worker_in -result_template tempales/worker_out";
        my $misc_conf = " --min_quorum 1 --max_success_results 3 --max_total_results 3 ";
        my $cmd = $worker_conf . $template_conf . $misc_conf . basename($file);
        system("$project_path/bin/dir_hier_path $wu_name | xargs cp $file ");
        system($cmd);
        move($file, "$project_path/utils/WorkGeneration/sentences_lists/processed_files/");
        print "work unit $wu_name has created\n";
}

print("Finish generation WUs\n");

1;


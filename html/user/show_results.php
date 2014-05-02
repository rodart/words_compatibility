<?php

require_once('../inc/util.inc');

page_head("Russian Words Compatibility");

start_table();
row1("Enter search russian word:");
rowify("
    <form action=\"db_show_compatibility.php\" method=\"GET\">
    <input type=\"text\" name=\"search_string\">
    <input type =\"submit\" value=\"  Search  \">
    </form>
");
end_table();

page_tail();
?>

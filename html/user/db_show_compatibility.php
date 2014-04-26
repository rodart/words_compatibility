<?php

require_once("../inc/util.inc");
require_once("../inc/boinc_db.inc");
require_once('../inc/util.inc');

check_get_args(array("search_string", "offset"));

$items_per_page = 20;
define ('ITEM_LIMIT', 10000);

function get_top_items($search_word, $offset) {
    global $items_per_page;
    $db = BoincDb::get(true);
    return BoincCompatibility::enum("word_from='$search_word'", "order by num desc limit $offset,$items_per_page");
}

function item_table_start() {
    start_table();
    echo "
        <tr>
        <th>".tra("Word from")."</th>
        <th>".tra("Word to")."</th>
        <th>".tra("Relation")."</th>
        <th>".tra("Number")."</th>
        </tr>
    ";
}

function show_item_row($item) {
    echo "
        <tr class=row1>
        <td>", $item->word_from, "</td>
        <td>", $item->word_to, "</td>
        <td>", $item->relation, "</td>
        <td>", $item->num,"</td>
        </tr>
    ";
}


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
echo "<br>";
$search_word = get_str("search_string", true);

$offset = get_int("offset", true);
if (!$offset) $offset=0;
if ($offset % $items_per_page) $offset = 0;

if ($offset < ITEM_LIMIT) {
    $data = get_top_items($search_word, $offset);
} else {
    error_page(tra("Limit exceeded - Sorry, first %1 items only", ITEM_LIMIT));
}

// Now display what we've got (gotten from DB)
//page_head("Search results");
item_table_start();

$items_num = 0;
foreach ($data as $item) {
    $items_num++;
    show_item_row($item);
}
echo "</table>\n<p>";

if ($offset > 0) {
    $new_offset = $offset - $items_per_page;
    echo "<a href=db_show_compatibility.php?search_string=$search_word&amp;offset=$new_offset>".tra("Previous %1", $items_per_page)."</a> | ";
}

if ($items_per_page == $items_num) { //If we aren't on the last page
    $new_offset = $offset + $items_per_page;
    echo "<a href=db_show_compatibility.php?search_string=$search_word&amp;offset=$new_offset>".tra("Next %1", $items_per_page)."</a>";
}

page_tail();

?>

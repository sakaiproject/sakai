<?php
if ( isset($_REQUEST['a']) && isset($_REQUEST['b']) ) {
    $a = trim($_REQUEST['a']);
    $b = trim($_REQUEST['b']);
    if ( strlen($a) == 0 || strlen($b) == 0 ) echo('<p>Please enter two base strings.</p>');
    else if ( $a == $b ) echo('<p style="color:green">Base strings match</p>');
    else {
        $len = strlen($a);
        if ( strlen($b) > $len) $len = strlen($b);
        $ao = "";
        $bo = "";
        $found = false;
        $dotdot = 100;
        for ($i=0; $i<$len; $i++) {
            $ac = $i < strlen($a) ? $a[$i] : false;
            $bc = $i < strlen($b) ? $b[$i] : false;
            if ( ! $found && $ac != $bc ) {
                $ao .= '<span style="color:red">';
                $bo .= '<span style="color:red">';
                $found = true;
            }
            if ( $ac !== false ) $ao .= $ac;
            if ( $bc !== false ) $bo .= $bc;
            if ( ($i % 60) == 59 ) $ao .= "<br/>\n";
            if ( ($i % 60) == 59 ) $bo .= "<br/>\n";
            if ( $found && $dotdot > 0 ) $dotdot--;
            if ( $dotdot == 0 ) {
                $ao .= " ...";
                $bo .= " ...";
                break;
            }
        }
        if ( $found ) {
            $ao .= '</span>';
            $bo .= '</span>';
        }
        $ao .= "<br/>\n";
        $bo .= "<br/>\n";
        echo('<div style="font-family: monospace"><br/>');
        echo($ao);
        echo("\n</div><hr/>\n");
        echo('<div style="font-family: monospace"><br/>');
        echo($bo);
        echo("\n</div><hr/>\n");
    }
}

?>
<html>
<head>
<title>Dr. Chuck's OAuth Base String Mismatch Tool</title>
</head>
<body>
<form action="basecheck.php" method="post">
<p>Base String 1</p>
<textarea name="a" rows="10" cols="80">
<?php if ( isset($_REQUEST['a']) ) {
 echo(htmlentities($_REQUEST['a']));
} ?>
</textarea>
<p>Base String 2</p>
<textarea name="b" rows="10" cols="80">
<?php if ( isset($_REQUEST['b']) ) {
 echo(htmlentities($_REQUEST['b']));
} ?>
</textarea><br>
<input type="submit" name="Compare" value="Compare">
</form>
<p>This tool also accepts "a=" or "b=" Request parameters if you want to directly link
to this tool from your own debug output.</p>
</body>
</html>

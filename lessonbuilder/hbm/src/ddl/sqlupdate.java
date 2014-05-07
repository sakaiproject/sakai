import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;

// at this instant this does just what hibernate does:
// add and delete columns and table, but not update columns
// and nothing with indices

public class sqlupdate {

    public static void main( String[] args) {
	
	// table col -> definition
	HashSet<String> olds = new HashSet<String>();
	HashSet<String> oldtables = new HashSet<String>();

	try {
	    BufferedReader oldschema = new BufferedReader(new FileReader(args[0]));
	    BufferedReader newschema = new BufferedReader(new FileReader(args[1]));

	    String table = null;
	    String line = null;

	    // build a set of the old cols
	    while ((line = oldschema.readLine()) != null) {
		line = line.trim();
		if (line.startsWith("create table")) {
		    int i = line.indexOf("(");
		    line = line.substring(12, i-1);
		    table = line.trim();
		    oldtables.add(table);
		} else if (line.startsWith(");")) {
		    table = null;
		} else if (line.length() > 0 && table != null) {
		    int i = line.indexOf(" ");
		    if (i > 0) {
			olds.add(table + " " + line.substring(0,i));
		    }
		}		    
	    }

	    PrintWriter out = new PrintWriter(args[2]);

	    // now go through new definition. Anything not in old is added.
	    // at the end, anythnig form the old not used should be deleted.

	    line = newschema.readLine();
	    mainloop:
	    while (line != null) {
		String oline = line;
		line = line.trim();
		if (line.startsWith("create table")) {
		    int i = line.indexOf("(");
		    line = line.substring(12, i-1);
		    table = line.trim();
		    if (!oldtables.remove(table)){
			out.println(oline);
			while ((line = newschema.readLine()) != null) {
			    if (line.trim().startsWith("create table")) {
				continue mainloop;
			    }
			    out.println(line);
			}
			if (line == null)
			    break;
		    }
		} else if (line.startsWith(");")) {
		    table = null;
		} else if (line.length() > 0 && table != null) {
		    int i = line.indexOf(" ");
		    if (i > 0) {
			if (! olds.remove(table + " " + line.substring(0,i))) {
			    if (line.endsWith(",") || line.endsWith(")"))
				line = line.substring(0, line.length() - 1);
			    out.println("alter table " + table + " add " + line + ";");
			}
		    }
		}
		line = newschema.readLine();		
	    }

	    // anything remaining in olds are not present in the new schema
	    for (String old: olds) {
		int i = old.indexOf(" ");
		table = old.substring(0,i);
		if (!oldtables.contains(table)) { 
		    // no need if we're dropping the whole table
		    String col = old.substring(i+1);
		    out.println("alter table " + table + " drop column " + col + ";");
		}
	    }

	    // anything remaining in oldtables is not present in new schema
	    for (String old: oldtables) {
		out.println("drop table " + old);
	    }

	    out.close();

	} catch (Exception e) {
	    System.err.println(e);
	}
    }
}

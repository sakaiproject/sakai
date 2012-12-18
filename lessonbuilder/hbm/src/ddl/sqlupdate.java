import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;

public class sqlupdate {

    public static void main( String[] args) {
	
	// table col -> definition
	HashSet<String> olds = new HashSet<String>();

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
		} else if (line.length() > 0) {
		    int i = line.indexOf(" ");
		    if (i > 0) {
			olds.add(table + " " + line.substring(0,i));
		    }
		}		    
	    }

	    PrintWriter out = new PrintWriter(args[2]);

	    // now go through new definition. Anything not in old is added.
	    // at the end, anythnig form the old not used should be deleted.

	    while ((line = newschema.readLine()) != null) {
		line = line.trim();
		if (line.startsWith("create table")) {
		    int i = line.indexOf("(");
		    line = line.substring(12, i-1);
		    table = line.trim();
		} else if (line.length() > 0) {
		    int i = line.indexOf(" ");
		    if (i > 0) {
			if (! olds.remove(table + " " + line.substring(0,i))) {
			    if (line.endsWith(",") || line.endsWith(")"))
				line = line.substring(0, line.length() - 1);
			    out.println("alter table " + table + " add " + line + ";");
			}
		    }
		}
	    }

	    out.close();

	} catch (Exception e) {
	    System.err.println(e);
	}
    }
}

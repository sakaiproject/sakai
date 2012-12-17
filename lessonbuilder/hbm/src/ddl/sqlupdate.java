import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;

public class sqlupdate {

    public static void main( String[] args) {
	
	// table col -> definition
	HashMap<String,String> adds = new HashMap<String, String>();

	try {
	    BufferedReader diffs = new BufferedReader(new FileReader(args[0]));
	    BufferedReader schema = new BufferedReader(new FileReader(args[1]));

	    PrintWriter out = new PrintWriter(args[2]);
	
	    String line;

	    while ((line = diffs.readLine()) != null) {
		if (line.startsWith("+")) {
		    line = line.substring(2);
		    adds.put(line,"");
		}
	    }
	    while ((line = schema.readLine()) != null) {
		int i = line.indexOf(" ");
		String table = line.substring(0,i);
		int j = line.indexOf(" ",i+1);
		String col = line.substring(i+1, j);
		String definition = line.substring(j+1);

		if (adds.get(table + " " + col) != null) {
		    if (definition.endsWith(",") || definition.endsWith(")"))
			definition = definition.substring(0, definition.length() - 1);
		    out.println("alter table " + table + " add " + col + " " + definition + ";");
		}
	    }

	    out.close();

	} catch (Exception e) {
	    System.err.println(e);
	}
    }
}

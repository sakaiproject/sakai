import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;

public class sql2cols {
    public static void main( String[] args) {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(args[0]));
	    PrintWriter out1 = new PrintWriter(args[1]);
	    PrintWriter out2 = new PrintWriter(args[2]);
	
	    String table = null;
	    String line = null;
	    while ((line = in.readLine()) != null) {
		line = line.trim();
		if (line.startsWith("create table")) {
		    int i = line.indexOf("(");
		    line = line.substring(12, i-1);
		    table = line.trim();
		} else if (line.length() > 0) {
		    int i = line.indexOf(" ");
		    if (i > 0) {
			out1.println(table + " " + line.substring(0,i));
			out2.println(table + " " + line);
		    }
		}		    
	    }
	    out1.close();
	    out2.close();

	} catch (Exception e) {
	    System.err.println(e);
	}
    }
}

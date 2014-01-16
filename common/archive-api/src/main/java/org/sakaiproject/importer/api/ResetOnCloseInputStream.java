package org.sakaiproject.importer.api;

import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

//Maybe java.io.RandomAccessFile would be okay too?

//From http://stackoverflow.com/questions/924990/how-to-cache-inputstream-for-multiple-use
public class ResetOnCloseInputStream extends InputStream {

    private InputStream decorated_is;
    private File decorated_file = null;
    public ResetOnCloseInputStream(File file) throws FileNotFoundException {
    	//For a file just store the file and reset the input stream each time
    	decorated_is = new FileInputStream(file);
    	decorated_file=file;
    }
    public ResetOnCloseInputStream(InputStream anInputStream) {
        if (!anInputStream.markSupported()) {
            throw new IllegalArgumentException("marking not supported");
        }

        anInputStream.mark( Integer.MAX_VALUE); 
        decorated_is = anInputStream;
    }

    @Override
    public void close() throws IOException {
    	if (decorated_file != null) {
    		decorated_is.close();
    		decorated_is=new FileInputStream(decorated_file);
    	}
    	else {
    		decorated_is.reset();
    	}
    }
    
    public void finalize() throws IOException{
    	if (decorated_is!=null)
    		decorated_is.close();
    }

    @Override
    public int read() throws IOException {
        return decorated_is.read();
    }
}

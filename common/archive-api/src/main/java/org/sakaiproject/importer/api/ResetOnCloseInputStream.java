/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

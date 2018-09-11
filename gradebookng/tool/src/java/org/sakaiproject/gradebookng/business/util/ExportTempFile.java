package org.sakaiproject.gradebookng.business.util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FilenameFilter;

public class ExportTempFile {
    private static File tempDir= new File(System.getProperty("java.io.tmpdir"));


    public static File createTempFile(String prefix, String suffix){
        return createTempFile(prefix, suffix, tempDir);
    }

    public static File createTempFile(final String prefix, String suffix, File directory){
        if(StringUtils.isEmpty(suffix)){
            suffix = ".csv"; //for export we are always wanting a csv file
        }

        if(directory == null){
            directory = tempDir;
        }

        String[] files = directory.list(new FilenameFilter(){
            public boolean accept(File dir, String name){
                return name.startsWith(prefix);
            }
        });
            return new File(directory, prefix + suffix);
    }
}

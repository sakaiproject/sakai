
/*************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
** redistribute this software in source and binary code form, provided that 
** i) this copyright notice and license appear on all copies of the software; 
** and ii) Licensee does not utilize the software in a manner which is 
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL 
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
** DAMAGES. 
**
*******************************************************************************
**
** Date Changed   Author of Change  Reason for Changes
** ------------   ----------------  -------------------------------------------
**
*******************************************************************************/
package org.adl.samplerte.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.adl.util.debug.DebugIndicator;

/*******************************************************************************
** 
** Filename:  PackageHandler.java
**
** File Description:  The PackageHandler class contains methods that search 
**                    for files and exact files from a zip format.
**                    
**
** Author:  ADLI Project
**
** Company Name: Concurrent Technologies Corporation
**
** Module/Package Name: 
** Module/Package Description: 
**
** Design Issues:
**
** Implementation Issues:
** Known Problems:
** Side Effects:
**
** References: 
**             ADL SCORM 2004 3rd Edition
**
*******************************************************************************/
public class LMSPackageHandler
{
   /**
    * The Zip file.
    */
   public static ZipFile zf;

   /**
    * Flag to indicate whether to print debug info or not
    */
   private static boolean _Debug = DebugIndicator.ON;


   /**
    * The default constructor.
    */
   public LMSPackageHandler()
   {
   }

   
   
   /**
    * This method is being used as a debugging tool.  It returns a string 
    * indicating the version number of the class file.
    * 
    * @return String representing the version of the class
    */
   public static String version()
   {  
      System.out.println("*************");
      System.out.println("in version()");
      System.out.println("*************\n");

      String versionId = new String("");
      versionId = "Version 1.03 For Live Site";

      return versionId;
   }

   /**
    * This method is being used as a debugging tool.  It writes the contents 
    * of a zip file to the dos console.
    * 
    * @param iZipFileName The name of the zip file to be used
    */
   public static void display( String iZipFileName)
   {  
      if ( _Debug )
      {
         System.out.println("*************");
         System.out.println("in display()");
         System.out.println("*************\n");
      }
      try 
      {
         System.out.println("** " + iZipFileName + " **\n");
         System.out.println("*****************************************");
         System.out.println("The Package Contains the following files:");
         System.out.println("*****************************************\n");

         zf = new ZipFile(iZipFileName);
         
         for (Enumeration entries = zf.entries(); entries.hasMoreElements();) 
         {
           System.out.println(((ZipEntry)entries.nextElement()).getName());
         }
      
         zf.close();
      } 
      catch (IOException e) 
      {
         System.out.println("IO Exception Caught: " + e);
      }
      if ( _Debug )
      {
         System.out.println("\n\n");
      }
   }

   /**
    * This method takes in the name of a zip file and a file to be extracted 
    * from the zip format.  The method locates the file and extracts into the 
    * '.' directory.
    * 
    * @param iZipFileName The name of the zip file to be used
    * 
    * @param iExtractedFile The name of the file to be extracted from the zip
    * 
    * @param iPathOfExtract The location of the extract
    * 
    * @return The name of the extracted file
    */
   public static String extract( String iZipFileName, String iExtractedFile, 
                                 String iPathOfExtract)
   {
      if ( _Debug )
      {
         System.out.println("***********************");
         System.out.println("in extract()           ");
         System.out.println("***********************");
         System.out.println("zip file: " + iZipFileName);
         System.out.println("file to extract: " + iExtractedFile);
      }

      String nameOfExtractedFile = "";

      try 
      {
         String pathAndName = "";
         //  Input stream for the zip file (package)
         ZipInputStream in =
            new ZipInputStream(new FileInputStream(iZipFileName));
         
         //  Cut the path off of the name of the file. (for writing the file)
         int indexOfFileBeginning = iExtractedFile.lastIndexOf("/") + 1;
         nameOfExtractedFile = iExtractedFile.substring(indexOfFileBeginning);
         pathAndName= iPathOfExtract + "\\" + nameOfExtractedFile;

         //  Ouput stream for the extracted file
         //*************************************
         //*************************************
         OutputStream out = new FileOutputStream(pathAndName);
         //OutputStream out = new FileOutputStream(nameOfExtractedFile);


         ZipEntry entry;
         byte[] buf = new byte[1024];
         int len;
         int flag = 0;

         while (flag != 1)  
         {
            entry = in.getNextEntry();

            if ((entry.getName()).equalsIgnoreCase(iExtractedFile)) 
            {
               if ( _Debug )
               {
                  System.out.println("Found file to extract...  extracting to "
                     + iPathOfExtract);
               }
               flag = 1;
            }
         }

                    
         while ((len = in.read(buf)) > 0) 
         {  
            
            out.write(buf, 0, len);
         }
    
         out.close();
         in.close();
      } 
      catch (IOException e) 
      {
         if ( _Debug )
         {
            System.out.println("IO Exception Caught: " + e);
         }
      }
      return nameOfExtractedFile;
   }

   /**
    * This method takes in the name of a zip file and tries to locate the 
    * imsmanifest.xml file
    * 
    * @param iZipFileName The name of the zip file to be used
    * 
    * @return boolean value signifies whether or not the manifest was found.
    */
   public static boolean findManifest( String iZipFileName )
   {
      if ( _Debug )
      {
         System.out.println("***********************");
         System.out.println("in findManifest()      ");
         System.out.println("***********************\n");
      }

      boolean rtn = false;

      try 
      {
         ZipInputStream in = 
            new ZipInputStream(new FileInputStream(iZipFileName));
               
         ZipEntry entry;
         int flag = 0;

         while ( (flag != 1) && (in.available() != 0) )  
         {
            entry = in.getNextEntry();
            
            if (in.available() != 0) 
            {
               if ((entry.getName()).equalsIgnoreCase("imsmanifest.xml")) 
               {
                  if ( _Debug )
                  {
                     System.out.println("Located manifest.... returning true");
                  }
                  flag = 1;
                  rtn = true;
               }
            }
         }

         in.close();
      } 
      catch (IOException e) 
      {
         if ( _Debug )
         {
            System.out.println("IO Exception Caught: " + e);
         }
      }
      return rtn;
   }

   /**
    * This method takes in the name of a zip file and locates all files with 
    * an .xml extension
    *  
    * @param iZipFileName The name of the zip file to be used
    * 
    * @return boolean value whether or not any xml files were found
    */
   public static boolean findMetadata( String iZipFileName )
   {
      if ( _Debug )
      {
         System.out.println("***********************");
         System.out.println("in findMetadata()      ");
         System.out.println("***********************\n");
      }

      boolean rtn = false;
      String suffix = ".xml";

      try 
      {
         //  The zip file being searched.
         ZipInputStream in = 
            new ZipInputStream(new FileInputStream(iZipFileName));
         //  An entry in the zip file
         ZipEntry entry;
         
         while ( in.available() != 0 )  
         {
            entry = in.getNextEntry();
            
            if (in.available() != 0) 
            {
               if ( (entry.getName()).endsWith(suffix) ) 
               {
                  rtn = true;
                  if ( _Debug )
                  {
                     System.out.println("Other Metadata located... " +
                        "returning true");
                  }
               }
            }
         }

         in.close();
      } 
      catch (IOException e) 
      {
         if ( _Debug )
         {
            System.out.println("IO Exception Caught: " + e);
         }
      }

      return rtn;
   }

   /**
    * This method takes in the name of a zip file and locates all files with 
    * an .xml extension an adds their names to a vector.
    * 
    * @param iZipFileName The name of the zip file to be used
    * 
    * @return A list of the names of xml files.
    */
   public static Vector locateMetadata( String iZipFileName )
   {
      if ( _Debug )
      {
         System.out.println("***********************");
         System.out.println("in locateMetadata()    ");
         System.out.println("***********************\n");
      }

      //  An array of names of xml files to be returned to ColdFusion
      Vector metaDataVector = new Vector();
      String suffix = ".xml";

      try 
      {
         //  The zip file being searched.
         ZipInputStream in = 
            new ZipInputStream(new FileInputStream(iZipFileName));
         //  An entry in the zip file
         ZipEntry entry;
         
         if ( _Debug )
         {
            System.out.println("Other metadata located:");
         }
         while ( in.available() != 0 )  
         {
            entry = in.getNextEntry();
            
            if (in.available() != 0) 
            {
               if ( (entry.getName()).endsWith(suffix) ) 
               {
                  if ( _Debug )
                  {
                     System.out.println(entry.getName());
                  }
                  metaDataVector.addElement(entry.getName());
               }
            }
         }
         in.close();

      } 
      catch (IOException e) 
      {
         if ( _Debug )
         {
            System.out.println("IO Exception Caught: " + e);
         }
      }
      return metaDataVector;
   }

   /**
    * This method takes in the name of a zip file and locates all files with 
    * an .xml extension an adds their names to a vector.  The vector is then 
    * changed to a comma delimited string and returned to the caller.
    * 
    * @param iZipFile The name of the zip file to be used
    * 
    * @return A comma delimited list of metadata files.
    */
   public static String getListOfMetadata( String iZipFile )
   {
      if ( _Debug )
      {
         System.out.println("***********************");
         System.out.println("in getListOfMetadata() ");
         System.out.println("***********************\n");
      }

      Vector mdVector = new Vector();
      mdVector = locateMetadata( iZipFile );

      String mdString = new String();
      mdString = mdVector.toString();


      if ( _Debug )
      {
         System.out.println("**********************************************");
         System.out.println("in getListOfMetadata(): String is " + mdString);
         System.out.println("**********************************************\n");
      }

      return mdString;
   }

} 
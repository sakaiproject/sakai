<%@page import = "java.sql.PreparedStatement,java.sql.ResultSet,java.sql.Connection,
   java.io.FileOutputStream, java.io.ObjectOutputStream, org.adl.sequencer.SeqActivityTree"%>
<%
   /***************************************************************************
   **
   ** Filename:  saveFileUtil.jsp
   **
   ** File Description:
   **
   **
   **
   ** Author: ADL Technical Team
   **
   ** Contract Number:
   ** Company Name: CTC
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
   ** References: ADL SCORM
   **
   /***************************************************************************
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
   ***************************************************************************/
%>

<%!
   private static String ERROR_PAGE = "../specialstate/error.htm";

   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>ERROR
   * <br><b>"_ERROR_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_ERROR = "../specialstate/error.htm";

   /**
   *  Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>Blocked
   * <br><b>"_Blocked_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_BLOCKED = "../specialstate/blocked.htm";

   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>View Table of Contents
   * <br><b>"_TOC_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_TOC = "../specialstate/viewTOC.htm";

   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>Course Complete
   * <br><b>"_COURSECOMPLETE_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_COURSECOMPLETE=
                        "../specialstate/coursecomplete.htm";
                        
   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>No Available Activities
   * <br><b>"_INVALIDNAVEVENT_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_INVALIDNAVEVENT   = 
                        "../specialstate/invalidevent.htm";
   
   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>No Available Activities
   * <br><b>"_ENDSESSION_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_ENDSESSION = "../specialstate/endsession.htm";
   
   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>Nothing
   * <br><b>"_NOTHING_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_NOTHING = "../specialstate/nothing.htm";
   
   /**
   * Enumeration of possible results of of the sequencing process that do not
   * provide the 'next' activity to launch.
   * <br>No Available Activities
   * <br><b>"_DEADLOCK_"</b>
   * <br>[SEQUENCING SUBSYSTEM CONSTANT]
   */
   public static String LAUNCH_DEADLOCK = "../specialstate/deadlock.htm";


   /****************************************************************************
   **
   ** Function:  persistActivityTree()
   ** Input:   iTree - SeqActivityTree
   ** Output:  boolean
   **
   ** Description:  This function returns the input source.
   **
   ***************************************************************************/
   private boolean persistActivityTree( SeqActivityTree iTree, String iUser, 
                                        String iCourse )
   {
    boolean result = true;
    String sampleRTERoot = File.separator + "SCORM3rdSampleRTE10Files";
    String userDir = sampleRTERoot + File.separator + iUser + File.separator + 
       iCourse;
    String theWebPath = getServletConfig().getServletContext().
                        getRealPath( "/" );
    String serializeFileName = userDir + File.separator + "serialize.obj";
   
    // Clear the tree session state
    iTree.clearSessionState();
    
    try
    {
       FileOutputStream fo = new FileOutputStream( serializeFileName );
       ObjectOutputStream out_file = new ObjectOutputStream( fo );
       out_file.writeObject( iTree );
       out_file.close();
       fo.close();
    }
    catch ( Exception e )
    {
       result = false;
    }
    
    return result;
   }

   /****************************************************************************
   **
   ** Function:  getLaunchLocation()
   ** Input:   
   ** Output:  
   **
   ** Description:  This function queries the RTE database for the resource
   **               launch location.  The activity id is used as the key.
   **
   ***************************************************************************/
   private String getLaunchLocation( PreparedStatement iStmtLaunchLocation,
                                  String iCourseID, String iActivityID )
   {
    String nextItem = new String();
    ResultSet launchInfo = null;
   
    try 
    {
       synchronized( iStmtLaunchLocation )
       {
          iStmtLaunchLocation.setString( 1, iCourseID );
          iStmtLaunchLocation.setString( 2, iActivityID );
   
          launchInfo = iStmtLaunchLocation.executeQuery();
       }
       if ( launchInfo.next() )
       {
          nextItem = launchInfo.getString("Launch");
       }
       else
       {
          nextItem = ERROR_PAGE;
       }
       launchInfo.close();
    }
    catch ( Exception e )
    {
       System.out.println("CAUGHT EXCEPTION IN UTIL");
       nextItem = ERROR_PAGE;
    }
    return nextItem;
   }

   /****************************************************************************
   **
   ** Function:  getSpecialState()
   ** Input:   
   ** Output:  
   **
   ** Description:  This method looks at the mActivityID member of the current
   **               ADLLaunch object.  If it is in a special sequencing state
   **               then the appropriate String is returned.  If its not in
   **               a special state, null is returned.
   **
   ***************************************************************************/
   private String getSpecialState( String iActivity )
   {
    String theState = new String();
   
    if ( iActivity != null )
    {
      if ( iActivity.equals("_ERROR_") )
      {
         theState = LAUNCH_ERROR;
      }
      else if ( iActivity.equals("_SEQBLOCKED_") )
      {
         theState = LAUNCH_BLOCKED;
      }
      else if ( iActivity.equals("_TOC_") )
      {
         theState = LAUNCH_TOC;
      }      
      else if ( iActivity.equals("_COURSECOMPLETE_") )
      {
         theState = LAUNCH_COURSECOMPLETE;
      }      
      else if ( iActivity.equals("_INVALIDNAVREQ_") )
      {
         theState = LAUNCH_INVALIDNAVEVENT;
      }     
      else if ( iActivity.equals("_ENDSESSION_") )
      {
         theState = LAUNCH_ENDSESSION;
      }
      else if ( iActivity.equals("_NOTHING_") )
      {
         theState = LAUNCH_NOTHING;
      }
      else if ( iActivity.equals("_DEADLOCK_") )
      {
         theState = LAUNCH_DEADLOCK;
      }
      else
      {
         theState = ERROR_PAGE;
      }
     }
     else
     {
       theState = ERROR_PAGE;
     }
    return theState;

   }

   /****************************************************************************
   ** 
   ** Function:  insertComp()
   ** Input:   
   ** Output:  
   **
   ** Description:  This method inserts a list of competencies into the
   **               database.  
   **
   ***************************************************************************/
   public static void insertComp(String iStudentID,
                                 Vector iCompList,
                                 Connection iConn, 
                                 String iCourseID)
   {
      try
      {
         PreparedStatement stmtCreateRecord = null;

         // Create the SQL string, convert it to a prepared statement
         String sqlCreateRecord = "INSERT INTO Competency " +
                                  "(CompID, UserID, CourseID, PassFail, " +
                                  "Score) VALUES (?, ?, ?, ?, ?)";

         stmtCreateRecord = iConn.prepareStatement(sqlCreateRecord);

         for ( int i = 0; i < iCompList.size(); i++ )
         {

            String compID = (String)iCompList.elementAt(i);

            // Insert values into the prepared statement and execute
            // query.
           
            synchronized(stmtCreateRecord)
            {
               stmtCreateRecord.setString(1, compID);
               stmtCreateRecord.setString(2, iStudentID);
               stmtCreateRecord.setString(3, iCourseID);
               stmtCreateRecord.setString(4, "unknown");
               stmtCreateRecord.setString(5, "unknown");

               stmtCreateRecord.executeUpdate();
            }
         }
         // Close the prepared statement 
         stmtCreateRecord.close();
        
      }
      catch ( Exception e )
      {
            System.out.println("insert Comp");
            e.printStackTrace();
      }
   }
%>
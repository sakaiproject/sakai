<%@ page contentType="text/html;charset=utf-8" %>

<%@page import = "java.sql.*,java.util.*,java.io.*,org.adl.samplerte.util.*"%>

<%
   /***************************************************************************
   **
   ** Filename:  LMSLogin.jsp
   **
   ** File Description:   This file implements the login function.  It checks 
   ** username and password that were entered by user against those in 
   ** database and redirects to login2.htm if values were not found, else 
   ** sends to menu page.  
   **
   **
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

<%
   Connection conn;
   PreparedStatement stmtSelectUser;
   LMSDatabaseHandler myDatabaseHandler = new LMSDatabaseHandler();
   String sqlSelectUser
             = "SELECT * FROM UserInfo Where UserID = ?";
   

   try
   {
      String UserName = new String("");
      String Password = new String("");
      String action = new String("");
      String fullName = new String("");
      String loginName = new String("");
      String firstName = new String("");
      String lastName = new String("");
      
      UserName = request.getParameter("uname");
      Password = request.getParameter("pwd");
      action = null;
      conn = myDatabaseHandler.getConnection();
      stmtSelectUser = conn.prepareStatement( sqlSelectUser );
    
      ResultSet userRS = null;
   
      synchronized( stmtSelectUser )
      {
         stmtSelectUser.setString( 1, UserName);
         userRS = stmtSelectUser.executeQuery();
   	  }
   
      // Verifies that the username was found by checking to see if the result
      // set 'userRS' is empty.  If the username was found, it checks to see if 
      // the entered password is correct.  If the username was not found, the 
      // variable 'action' is changed to indicate this.
      if( (userRS != null) && (userRS.next()) )
      {
         String userID = userRS.getString("UserID");
         
         if(userID.equals(UserName))
         {
            String passwd = userRS.getString("Password");
            boolean active = userRS.getBoolean("Active");
            firstName = userRS.getString("FirstName");
            lastName = userRS.getString("LastName");
            fullName = lastName + ", " + firstName;
            loginName = firstName + ' ' + lastName;

            if (! active )
            {
               action = "deactivated";
            }
         
            // Verifies that the password that was entered is not blank and that 
            // it matches the password found to belong to the username.  If either
            // of these conditions is incorrect, the variable 'action' is changed
            // to indicate this.
            if( (Password != null) && (!Password.equals(passwd)) )
            {
               action = "invalidpwd";      
            }
         }
         else
         {
            action = "invaliduname";
         }
         
      }
      else
      {
         action = "invaliduname";
      }
   	
      // Verifies that no errors were found with the login by checking to see 
      // if the action variable has been assigned anything.  If 'action' is
      //  null, no errors were found and the session variables 'USERID' and
      //  'RTEADMIN' are set.  
      if ( action == null )
      {
         session.putValue("USERID", UserName);
         session.putValue("USERNAME", fullName);
         session.putValue("LOGINNAME", loginName);

         String admin = userRS.getString("Admin"); 
         
         // Checks to see if the user has admin rights and sets the 'RTEADMIN'
         // variable accordingly.
         if ( (admin != null) && (admin.equals("1")) ) 
         {
           session.putValue("RTEADMIN", new String("true"));
         }
         else
         {
           session.putValue("RTEADMIN", new String("false"));
         }
      }
   
      // checks to see if the action variable is null.  If it is then no error
      // was found and the page is redirected to the menu page, otherwise it
      // is redirected to the second login page.
      if ( action != null )
      {
         response.sendRedirect( "LMSLogin2.htm" );
      }
      else
      {
         response.sendRedirect( "LMSMenu.jsp" );
      }
      userRS.close();
      stmtSelectUser.close();
      conn.close();


   }
   catch(SQLException e)
   {
      System.out.println("login sql exception ");e.printStackTrace();   
   }
   catch(Exception e)
   {  
%>
   Caught exception <%= e %>
<%
   } 
%>

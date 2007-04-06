
<%
   /***************************************************************************
   **
   ** Filename:  importUtil.jsp
   **
   ** File Description:   This file implements utility classes for   
   **                     importCourse.jsp.
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


<%!
   /***************************************************************************
   ** Method:  replace
   ** 
   ** Description:
   **    This method will perform a search and replace on the input string and
   **    return the resulting string.
   ** 
   ** @param ioString - The string to be formatted.
   ** @param iFrom - The string to search for.
   ** @param iTo - The newly formatted string.
   ** 
   ** @return - The newly formatted string.
   ***************************************************************************/
   private String replace( String ioString, String iFrom, String iTo )
   {
      int startPos = 0;
      int indexPos = -1;
      String tempString;

      while ( (indexPos = ioString.indexOf( iFrom, startPos)) != -1 )
      {
         tempString = ioString.substring(0, indexPos);
         tempString += iTo;
         tempString += ioString.substring(indexPos + iFrom.length());
         ioString = tempString;
         startPos = indexPos + iTo.length();
      }

      return ioString;
   }

   /***************************************************************************
   ** Method:  makeReadyForPrint
   ** 
   ** Description:
   **    This method will call the replace method on the input string to replace
   **    special characters that cannot appear in html.
   **    Note that not all special characters are being handled, only those
   **    that the validating XML parser is know to use in its error messages.
   ** 
   ** @param ioString - The String to be formatted.
   ** 
   ** @return String - The newly formatted string.
   ***************************************************************************/
   private String makeReadyForPrint( String ioString )
   {
      String tempString;

      // call replace to deal with special characters
      tempString = this.replace( ioString, "&", "&amp;" );
      tempString = this.replace( tempString, "\"", "&quot;" );
      tempString = this.replace( tempString, "<", "&lt;" );
      tempString = this.replace( tempString, ">", "&gt;" );
      tempString = this.replace( tempString, "[", "&#91;" );
      tempString = this.replace( tempString, "]", "&#93;" );
      tempString = this.replace( tempString, "\'", "&#39;" );
      tempString = this.replace( tempString, "\\", "\\\\" );

      return tempString;
   }
%>


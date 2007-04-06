
/******************************************************************************
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
******************************************************************************/

package org.adl.samplerte.server;

import java.io.Serializable;

/**
 * Enumeration of abstract servlet request types.<br><br>
 * 
 * <strong>Filename:</strong> ServletRequestTypes.java<br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class ServletRequestTypes implements Serializable
{

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>GET_COURSES
    * <br><b>1</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int GET_COURSES       =  1;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>GET_SCOS
    * <br><b>2</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int GET_SCOS          =  2;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>GET_COMMENTS
    * <br><b>3</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int GET_COMMENTS      =  3;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>UPDATE_SCO
    * <br><b>4</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int UPDATE_SCO        =  4;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>GET_USERS
    * <br><b>5</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int GET_USERS         =  5;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>GET_PREF
    * <br><b>6</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int GET_PREF          =  6;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>UPDATE_PREF
    * <br><b>7</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int UPDATE_PREF       =  7;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>ADD_USERS
    * <br><b>8</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int ADD_USERS         =  8;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>DELETE_USERS
    * <br><b>9</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int DELETE_USERS      =  9;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>DELETE_COURSE
    * <br><b>10</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int DELETE_COURSE     =  10;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>REG_COURSE
    * <br><b>11</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int REG_COURSE        =  11;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>PROC_REG_COURSE
    * <br><b>12</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int PROC_REG_COURSE   =  12;
   
   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>VIEW_REG_COURSE
    * <br><b>13</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int VIEW_REG_COURSE   =  13;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>SELECT_MY_COURSE
    * <br><b>14</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int SELECT_MY_COURSE  =  14;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>VIEW_MY_STATUS
    * <br><b>15</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int VIEW_MY_STATUS    =  15;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>CLEAR_DB
    * <br><b>16</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int CLEAR_DB          =  16;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>NEW_OBJ
    * <br><b>17</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int NEW_OBJ           =  17;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>USER_OBJ
    * <br><b>18</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int USER_OBJ          =  18;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>ADD_OBJ
    * <br><b>19</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int ADD_OBJ           =  19;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>COURSE_OBJ
    * <br><b>20</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int COURSE_OBJ        =  20;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>OBJ_ADMIN
    * <br><b>21</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int OBJ_ADMIN         =  21;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>EDIT_OBJ
    * <br><b>22</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int EDIT_OBJ          =  22;

   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>IMPORT_COURSE
    * <br><b>23</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int IMPORT_COURSE     =  23;

    /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>NEW_USER
    * <br><b>24</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int NEW_USER          =  24;
   
   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>LIST_BUCKETS
    * <br><b>25</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int LIST_BUCKETS          =  25;
   
   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>ADD_BUCKET_REQ
    * <br><b>26</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int ADD_BUCKET_REQ          =  26;
   
   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>ADD_BUCKET
    * <br><b>27</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int ADD_BUCKET          =  27;
   
   /**
    * Enumeration of possible Servlet Request Types.<br>
    * <br>DEL_BUCKET
    * <br><b>28</b>
    * <br><br>[SERVLET REQUEST TYPE CONSTANT]
    */
   public static final int DELETE_BUCKET          =  28;

} // end ServletRequestTypes
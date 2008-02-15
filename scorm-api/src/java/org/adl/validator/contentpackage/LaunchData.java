/*******************************************************************************
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
*******************************************************************************/
package org.adl.validator.contentpackage;

import java.io.Serializable;
import java.util.logging.Logger;


/**
 * <strong>Filename: </strong><br>LaunchData.java<br><br>
 *
 * <strong>Description: </strong><br> A <CODE>LaunchData</CODE> is a Data
 * Structure used to store information for the Launch Data of SCOs.
 *
 * @author ADL Technical Team
 */

public class LaunchData implements ILaunchData
{
	private static final long serialVersionUID = 1L;

	private Serializable id;
	
   /**
    * The variable representing the hideLMSUI value of "previous".
    */
   public boolean mPrevious;

   /**
    * The variable representing the hideLMSUI value of "continue".
    */
   public boolean mContinue;

   /**
    * The variable representing the hideLMSUI value of "exit".
    */
   public boolean mExit;
   
   /**
    * The variable representing the hideLMSUI value of "exitAll".
    */
   public boolean mExitAll;

   /**
    * The variable representing the hideLMSUI value of "abandon".
    */
   public boolean mAbandon;

   /**
    * The variable representing the hideLMSUI value of "suspendAll".
    */
   public boolean mSuspendAll;
   
   /**
    * Logger object used for debug logging.
    */
   private transient Logger mLogger = Logger.getLogger("org.adl.util.debug.validator");

   /**
    * The identifier attribute of the <code>&lt;organization&gt;</code> 
    * element.
    */
   private String mOrganizationIdentifier; 

   /**
    * The identifier attribute of the <code>&lt;item&gt;</code> element.
    */
   private String mItemIdentifier;

   /**
    * The identifier attribute of the <code>&lt;resource&gt;</code> element.
    */
   private String mResourceIdentifier;

   /**
    * The xml:base attribute of the <code>&lt;manifest&gt;</code> element.
    */
   private String mManifestXMLBase;

   /**
    * The xml:base attribute of the <code>&lt;resources&gt;</code> element.
    */
   private String mResourcesXMLBase;

   /**
    * The xml:base attribute of the <code>&lt;resource&gt;</code> element.
    */
   private String mResourceXMLBase;

   /**
    * The parameter value of the item.
    */
   private String mParameters;

   /**
    * The persistState attribute of an item.
    */
   private String mPersistState;

   /**
    * The location of the item.
    */
   private String mLocation;

   /**
    * The SCORM type of the item (sco, sca, asset).
    */
   private String mSCORMType;

   /**
    * The title value of the item.
    */
   private String mItemTitle;

   /**
    * The variable representing the datafromlms element of the item.
    */
   private String mDataFromLMS;

   /**
    * The variable representing the timelimitaction element of the item.
    */
   private String mTimeLimitAction;

   /**
    * The variable representing the minNormalizedMeasure element.
    */
   private String mMinNormalizedMeasure;

   /**
    * The variable representing the attemptAbsoluteDurationLimit element.
    */
   private String mAttemptAbsoluteDurationLimit;

   /**
    * The variable representing the completionThreshold element.
    */
   private String mCompletionThreshold;

   /**
    * The variable representing the objectives found in the sequencing.
    */
   private String mObjectivesList;

   /**
    * The default constructor.
    */
   public LaunchData()
   {
      //mLogger = Logger.getLogger("org.adl.util.debug.validator");

      mOrganizationIdentifier       = new String();
      mItemIdentifier               = new String();
      mResourceIdentifier           = new String();
      mManifestXMLBase              = new String();
      mResourcesXMLBase             = new String();
      mResourceXMLBase              = new String();
      mParameters                   = new String();
      mPersistState                 = new String();
      mLocation                     = new String();
      mSCORMType                    = new String();
      mItemTitle                    = new String();
      mDataFromLMS                  = new String();
      mTimeLimitAction              = new String();
      mMinNormalizedMeasure         = new String();
      mAttemptAbsoluteDurationLimit = new String();
      mCompletionThreshold          = new String();
      mObjectivesList               = new String();
      mPrevious                     = false;
      mContinue                     = false;
      mExit                         = false;
      mExitAll                      = false;
      mAbandon                      = false;
      mSuspendAll                   = false;
   }

   /** 
    * Assigns the given value to the mOrganizationIdentifier attribute.
    * 
    * @param iOrganizationIdentifier The organization identitifier value to 
    * be assigned.
    */
   public void setOrganizationIdentifier( String iOrganizationIdentifier )
   {
      mOrganizationIdentifier = iOrganizationIdentifier;
   }

   /**
    * Assigns the given value to the mItemIdentifier attribute.
    *
    * @param iItemIdentifier The item identifier value to be assigned.
    */
   public void setItemIdentifier( String iItemIdentifier )
   {
      mItemIdentifier = iItemIdentifier;
   }

   /**
    * Assigns the given value to the mResourceIdentifier attribute.
    *
    * @param iResourceIdentifier The resource identifier value to be assigned.
    */
   public void setResourceIdentifier( String iResourceIdentifier )
   {
      mResourceIdentifier = iResourceIdentifier;
   }

   /**
    * Assigns the given value to the mManifestXMLBase attribute.
    *
    * @param iManifestXMLBase The manifest xml:base value to be assigned.
    */
   public void setManifestXMLBase( String iManifestXMLBase )
   {
      mManifestXMLBase = iManifestXMLBase;
   }

   /**
    * Assigns the given value to the mResourcesXMLBase attribute.
    *
    * @param iResourcesXMLBase The resources xml:base value to be assigned.
    */
   public void setResourcesXMLBase( String iResourcesXMLBase )
   {
      mResourcesXMLBase = iResourcesXMLBase;
   }

   /**
    * Assigns the given value to the mResourceXMLBase attribute.
    *
    * @param iResourceXMLBase The resource xml:base value to be assigned.
    */
   public void setResourceXMLBase( String iResourceXMLBase )
   {
      mResourceXMLBase = iResourceXMLBase;
   }

   /**
    * Assigns the given value to the mParameters attribute.
    *
    * @param iParameters The parameters value to be assigned.
    */
   public void setParameters( String iParameters )
   {
      mParameters = iParameters;
   }

   /**
    * Assigns the given value to the mPersistState attribute of an item.
    * 
    * @param iPersistState The persistState value to be assigned.
    */
   public void setPersistState( String iPersistState )
   {
      mPersistState = iPersistState;
   }

   /**
    * Assigns the given value to the mLocation attribute.
    *
    * @param iLocation The location value to be assigned.
    */
   public void setLocation( String iLocation )
   {
      mLocation = iLocation;
   }

   /**
    * Assigns the given value to the mSCORMType attribute.
    *
    * @param iSCORMType The scormtype value to be assigned.
    */
   public void setSCORMType( String iSCORMType )
   {
      mSCORMType = iSCORMType;
   }

   /**
    * Assigns the given value to the mItemTitle attribute.
    *
    * @param iItemTitle The item value to be assigned.
    */
   public void setItemTitle( String iItemTitle )
   {
      mItemTitle = iItemTitle;
   }

   /**
    * Assigns the given value to the mDataFromLMS attribute.
    *
    * @param iDataFromLMS The datafromlms value to be assigned.
    */
   public void setDataFromLMS( String iDataFromLMS )
   {
      mDataFromLMS = iDataFromLMS;
   }

   /**
    * Assigns the given value to the mTimeLimitAction attribute.
    *
    * @param iTimeLimitAction The timelimitaction value to be assigned.
    */
   public void setTimeLimitAction( String iTimeLimitAction )
   {
      mTimeLimitAction = iTimeLimitAction;
   }

   /**
    * Assigns the given value to the minNormalizedMeasure attribute.
    *
    * @param iMinNormalizedMeasure The minnormalizedmeasure value to be
    * assigned.
    */
   public void setMinNormalizedMeasure( String iMinNormalizedMeasure )
   {
      mMinNormalizedMeasure = iMinNormalizedMeasure;
   }

   /**
    * Assigns the given value to the attemptAbsoluteDurationLimit attribute.
    *
    * @param iAttemptAbsoluteDurationLimit The attemptabsolutedurationlimit
    * value to be assigned.
    */
   public void setAttemptAbsoluteDurationLimit( String
                                                iAttemptAbsoluteDurationLimit )
   {
      mAttemptAbsoluteDurationLimit = iAttemptAbsoluteDurationLimit;
   }

   /**
    * Assigns the given value to the mCompletionThreshold attribute.
    * 
    * @param iCompletionThreshold The completionThreshold value to be 
    * assigned.
    */
   public void setCompletionThreshold( String iCompletionThreshold )
   {
      mCompletionThreshold = iCompletionThreshold;
   }

   /**
    * Assigns the given value to the mObjectivesList attribute.
    * 
    * @param iObjectivesList The objectives to be assigned.
    */
   public void setObjectivesList( String iObjectivesList )
   {
      mObjectivesList = iObjectivesList;
   }

   /**
    * Assigns the given value to the mPrevious attribute.
    *
    * @param iPrevious The previous value to be assigned.
    */
   public void setPrevious( boolean iPrevious )
   {
      mPrevious = iPrevious;
   }

   /**
    * Assigns the given value to the mContinue attribute.
    *
    * @param iContinue The continue value to be assigned.
    */
   public void setContinue( boolean iContinue )
   {
      mContinue = iContinue;
   }

   /**
    * Assigns the given value to the mExit attribute.
    *
    * @param iExit The exit value to be assigned.
    */
   public void setExit( boolean iExit )
   {
      mExit = iExit;
   }
   
   /**
    * Assigns the given value to the mExitAll attribute.
    * @param iExitAll the exitAll value to be assigned.
    */
   public void setExitAll( boolean iExitAll )
   {
      mExitAll = iExitAll;
   }

   /**
    * Assigns the given value to the mAbandon attribute.
    *
    * @param iAbandon The abandon value to be assigned.
    */
   public void setAbandon( boolean iAbandon )
   {
      mAbandon = iAbandon;
   }

   /**
    * Assigns the given value to the mSuspendAll attribute.
    *
    * @param iSuspendAll The suspendAll value to be assigned.
    */
   public void setSuspendAll( boolean iSuspendAll )
   {
      mSuspendAll = iSuspendAll;
   }


   /**
    * Gives access to the indentifier value of the 
    * <code>&lt;organization&gt;</code> element.
    * 
    * @return The identifier value of the <code>&lt;organization&gt;</code> 
    * element.
    */
   public String getOrganizationIdentifier()
   {
      return mOrganizationIdentifier;
   }

   /**
    * Gives access to the identifier value of the <code>&lt;item&gt;</code> 
    * element.
    *
    * @return The identifier value of the <code>&lt;item&gt;</code> element.
    */
   public String getItemIdentifier()
   {
      return mItemIdentifier;
   }

   /**
    * Gives access to the identifier value of the <code>&lt;resource&gt;</code>
    * element.
    *
    * @return The identifier value of the <code>&lt;resource&gt;</code> 
    * element.
    */
   public String getResourceIdentifier()
   {
      return mResourceIdentifier;
   }

   /**
    * Gives access to the xml:base value of the <code>&lt;manifest&gt;</code> 
    * element.
    *
    * @return The xml:base value of the <code>&lt;manifest&gt;</code> element.
    */
   public String getManifestXMLBase()
   {
      return mManifestXMLBase;
   }

   /**
    * Gives access to the xml:base value of the <code>&lt;resources&gt;</code> 
    * element.
    *
    * @return The xml:base value of the <code>&lt;resources&gt;</code> 
    * element.
    */
   public String getResourcesXMLBase()
   {
      return mResourcesXMLBase;
   }

   /**
    * Gives access to the xml:base value of the <code>&lt;resource&gt;</code>
    * element.
    *
    * @return The xml:base value of the <code>&lt;resource&gt;</code> element.
    */
   public String getResourceXMLBase()
   {
      return mResourceXMLBase;
   }

   /**
    * Gives access to the full <code>xml:base</code> value.
    *
    * @return The full <code>xml:base</code> value as determined in the 
    * manifest.
    */
   public String getXMLBase()
   {
	  if (mManifestXMLBase == null)
		  mManifestXMLBase = "";
	  if (mResourcesXMLBase == null)
		  mResourcesXMLBase = "";
	  if (mResourceXMLBase == null)
		  mResourceXMLBase = "";
	   
      StringBuffer result = new StringBuffer(mManifestXMLBase);

      // add a file separator only if there is a directory before and after it.
      if ( (! result.equals("")) &&
           (! mResourcesXMLBase.equals("")) &&
           (! result.toString().endsWith("/")) )
      {
         result.append("/");
      }

      result.append(mResourcesXMLBase);

      // add a file separator only if there is a directory before and after it.
      if ( (! result.equals("")) &&
           (! mResourceXMLBase.equals("")) &&
           (! result.toString().endsWith("/")) )
      {
         result.append("/");
      }

      result.append(mResourceXMLBase);

      return result.toString();
   }

   /**
    * Gives access to the parameters of the <code>&lt;item&gt;</code> element.
    *
    * @return - The parameter value of the <code>&lt;item&gt;</code> element.
    */
   public String getParameters()
   {
      return mParameters;
   }

   /**
    * TODO:  This attribute no longer exists, should we remove it
    * Gives access to the persistState attribute of the item.
    * 
    * @return The value of the persistState attribute of the item.
    */
   public String getPersistState()
   {
      return mPersistState;
   }

   /**
    * Gives access to the location of the item.
    *
    * @return The location value of the item.
    */
   public String getLocation()
   {
      return mLocation;
   }

   /**
    * Gives access to the SCORM type value of the item.
    *
    * @return The SCORM type value of the item.
    */
   public String getSCORMType()
   {
      return mSCORMType;
   }

   /**
    * Gives access to the title value of the item.
    *
    * @return The title value of the item.
    */
   public String getItemTitle()
   {
      return mItemTitle;
   }

   /**
    * Gives access to the datafromlms element value of the item.
    *
    * @return The value of the datafromlms element.
    */
   public String getDataFromLMS()
   {
      return mDataFromLMS;
   }

   /**
    * Gives access to the timelimitaction element value of the item.
    *
    * @return The value of the timelimitaction element.
    */
   public String getTimeLimitAction()
   {
      return mTimeLimitAction;
   }

   /**
    * Gives access to the minNormalizedMeasure element value.
    *
    * @return The value of the minNormalizedMeasure element.
    */
   public String getMinNormalizedMeasure()
   {
      return mMinNormalizedMeasure;
   }

   /**
    * Gives access to the attemptAbsoluteDurationLimit element value.
    *
    * @return The value of the attemptAbsoluteDurationLimit element.
    */
   public String getAttemptAbsoluteDurationLimit()
   {
      return mAttemptAbsoluteDurationLimit;
   }

   /**
    * Gives access to the completionThreshold element value.
    * 
    * @return The value of the completionThreshold element.
    */
   public String getCompletionThreshold()
   {
      return mCompletionThreshold;
   }

   /**
    * Gives access to the objectiveslist element value.
    * 
    * @return The value of the objectiveslist element.
    */
   public String getObjectivesList()
   {
      return mObjectivesList;
   }

   /**
    * Gives access to the value of mPrevious, which is a boolean
    * representing whether a hideLMSUI element for the item had the value of
    * "previous".
    *
    * @return The value of the mPrevious.
    */
   public boolean getPrevious()
   {
      return mPrevious;
   }

   /**
    * Gives access to the value of mContinue, which is a boolean
    * representing whether a hideLMSUI element for the item had the value of
    * "continue".
    *
    * @return The value of the mContinue.
    */
   public boolean getContinue()
   {
      return mContinue;
   }

   /**
    * Gives access to the value of mExit, which is a boolean
    * representing whether a hideLMSUI element for the item had the value of
    * "exit".
    *
    * @return The value of the mExit.
    */
   public boolean getExit()
   {
      return mExit;
   }
   
   /**
    * Gives access ot the value of mExitAll, which is a boolean representation
    * of whether a hideLMSUI element for the item had the value of "exitAll".
    * 
    * @return The value of mExitAll.
    */
   public boolean getExitAll()
   {
      return mExitAll;
   }

   /**
    * Gives access to the value of mAbandon, which is a boolean
    * representing whether a hideLMSUI element for the item had the value of
    * "abandon".
    *
    * @return The value of the mAbandon.
    */
   public boolean getAbandon()
   {
      return mAbandon;
   }

   /**
    * Gives access to the value of mSuspendAll, which is a boolean
    * representing whether a hideLMSUI element for the item had the value of
    * "suspendAll".
    *
    * @return The value of the mSuspendAll.
    */
   public boolean getSuspendAll()
   {
      return mSuspendAll;
   }


   /**
    * Gives access to the full launch line of the item including the full
    * xml:base of the item.
    *
    * @return The full launch location of the item.
    */
   public String getLaunchLine()
   {
      String launchLine = new String();
      String parameterString = new String();
      String xmlBase = getXMLBase();

      if ( (! xmlBase.equals("")) &&
           (! xmlBase.endsWith("/")) )
      {
          xmlBase += "/";
      }

      launchLine = mLocation;
      parameterString = mParameters;
      
      if (launchLine == null)
    	  launchLine = "";
      
      if (parameterString == null)
    	  parameterString = "";
      
      if (!parameterString.equals("") )
      {
         while ( parameterString.startsWith("?") || 
                 parameterString.startsWith("&") )
         {
            parameterString = parameterString.substring(1);   
         }
         if ( parameterString.startsWith("#") )
         {
            if ( launchLine.indexOf("#") != -1 )
            {
               return xmlBase + launchLine;
            }
            // if the above return is not executed return the following
            return xmlBase + launchLine + parameterString;

         }

         if (launchLine.indexOf("?") != -1)
         {
            launchLine = launchLine.concat("&");
         }
         else
         {
            launchLine = launchLine.concat("?");
         }
      }
      return xmlBase + launchLine + parameterString;
   }

   /**
    * Displays a string representation of the data structure for the SCO
    * Integration to the Java logger. 
    */
   public void print()
   {
      mLogger.fine( "##################################################");
      mLogger.fine( "####   resourceIdentifier = '" + mResourceIdentifier +
                       "'");
      mLogger.fine( "####   itemIdentifier = '" + mItemIdentifier + "'");
      mLogger.fine( "####   itemTitle = '" + mItemTitle + "'");
      mLogger.fine( "####   manifestXMLBase = '" + mManifestXMLBase + "'");
      mLogger.fine( "####   resourcesXMLBase = '" + mResourcesXMLBase + "'");
      mLogger.fine( "####   resourceXMLBase = '" + mResourceXMLBase + "'");
      mLogger.fine( "####   scormType = '" + mSCORMType + "'");
      mLogger.fine( "####   parameters = '" + mParameters + "'");
      mLogger.fine( "####   location = '" + mLocation + "'");
      mLogger.fine( "####   LaunchLine = '" + getLaunchLine() + "'");
      mLogger.fine( "##################################################");
   }

   /**
    * Displays a string representation of the data structure for Integration to
    * the Java Console.
    */
   public void printToConsole()
   {
      System.out.println( "###############################################");
      System.out.println( "###   resourceIdentifier = '" +
                          mResourceIdentifier + "'");
      System.out.println( "###  itemIdentifier = '" + mItemIdentifier + "'");
      System.out.println( "###  itemTitle = '" + mItemTitle + "'");
      System.out.println( "###  manifestXMLBase = '" + mManifestXMLBase +
                          "'");
      System.out.println( "###  resourcesXMLBase = '" + mResourcesXMLBase +
                          "'");
      System.out.println( "###  resourceXMLBase = '" + mResourceXMLBase +
                          "'");
      System.out.println( "###  scormType = '" + mSCORMType + "'");
      System.out.println( "###  parameters = '" + mParameters + "'");
      System.out.println( "###  location = '" + mLocation + "'");
      System.out.println( "###  LaunchLine = '" + getLaunchLine() + "'");
      System.out.println( "###############################################");
   }

public Serializable getId() {
	return id;
}

public void setId(Serializable id) {
	this.id = id;
}
}
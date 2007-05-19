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
*******************************************************************************/
package org.ims.ssp.samplerte.server.bucket;

/**
 * Describes the allocation requirements for a single bucket described by an ID.
 * <br><br>
 *
 * <strong>Filename:</strong> BucketAllocation.java<br><br>
 *
 * <strong>Description:</strong><br>
 * Describes the allocation requirements for a single bucket described by an ID.
 * 
 * <br><br>
 *
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM 2004 3rd Edition
 * Sample RTE. <br>
 * <br>
 *
 * <strong>Implementation Issues:</strong><br><br>
 *
 * <strong>Known Problems:</strong><br><br>
 *
 * <strong>Side Effects:</strong><br><br>
 *
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SSP Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 *
 * @author ADL Technical Team
 */
public class BucketAllocation
{
   /**
    *
    * The maximum size limit of a single bucket for this implementation.
    *
    * <b>Note:</b> This is an arbitrary value for this implementation.
    *
    */
   public static final int MAXIMUM_SIZE = 100000;

   /**
    *
    * The identifier of the bucket.
    *
    */
   protected String mBucketID = null;

   /**
    *
    * Reference to a type definition for the bucket's data.
    *
    */
   protected String mBucketType = "";

   /**
    *
    * Describes the minimum amount of space requested.
    *
    */
   protected String mMinimum = null;

   /**
    *
    * Describes how long the runtime system should persist the data in the
    * bucket.  This value may be one of the enumerated values described in the
    * <code>Persistence</code> class.  If not provided, the default value is
    * <code>Persistence.LEARNER</code>
    *
    */
   protected int mPersistence = Persistence.LEARNER;

   /**
    *
    * Indicates if the amount of space requested was allowed to be reduced.
    *
    */
   protected String mReducible = null;

   /**
    *
    * Describes the amount of space requested.
    *
    */
   protected String mRequested = null;

   /**
    *
    * Indicates the allocation status of the bucket.
    *
    */
   protected int mAllocationStatus = -1;

   /**
    *
    * Indicates the SCOID of the bucket.
    *
    */
   protected String mSCOID = null;

   /**
    *
    * Indicates bucket is failed because of an invalid reallocate.
    *
    */
   protected boolean mReallocateFailure = false;

   /**
    *
    * Indicates student ID of the bucket.
    *
    */
   protected String mStudentID = null;

   /**
    *
    * Indicates Course ID of the bucket.
    *
    */
   protected String mCourseID = null;

   /**
    *
    * Indicates Attempt ID of the bucket.
    *
    */
   protected String mAttemptID = null;

   /**
    *
    * Indicates offset for the requested operation.
    *
    */
   protected String mOffset = null;

   /**
    *
    * Indicates size for the requested operation.
    *
    */
   protected String mSize = null;

   /**
    *
    * Indicates value of the bucket.
    *
    */
   protected String mValue = null;

   /**
    *
    * Integer value of the minimum size.
    *
    */
   protected int mMinimumSizeInt = -1;

   /**
    *
    * Integer value of requested size.
    *
    */
   protected int mRequestedSizeInt = -1;

   /**
    *
    * Integer value of offset.
    *
    */
   protected int mOffsetInt = -1;

   /**
    *
    * Integer value of size.
    *
    */
   protected int mSizeInt = -1;

   /**
    *
    * Boolean value for reducible flag.
    *
    */
   protected boolean mReduciblebool = false;

   /**
    *
    * Indicates managed bucket index for bucket.
    *
    */
   protected int mManagedBucketIndex = -1;

   /**
    *
    * Indicates activity ID of the bucket.
    *
    */
   protected int mActivityID = -1;





   /**
    * Default constructor
    */
   public BucketAllocation ()
   {
      // default constructor
   }

   /**
    * Constructor
    * 
    * @param iBucketID The id of the bucket
    * @param iBucketType The type of bucket
    * @param iMinimum The minimum size of the bucket
    * @param iPersistence The persistence of the bucket
    * @param iReducible If the bucket size is reducible to the minimum size
    * @param iRequested The requested size of the bucket
    * @param iAllocationStatus Status of the allocation of the bucket
    * @param iSCOID The ID of the SCO for this bucket
    */
   public BucketAllocation ( String iBucketID, String iBucketType, int iMinimum,
                             int iPersistence, boolean iReducible, int iRequested,
                             int iAllocationStatus, String iSCOID )
   {
      mBucketID = iBucketID;
      mMinimumSizeInt = iMinimum;
      mPersistence = iPersistence;
      mReduciblebool = iReducible;
      mRequestedSizeInt = iRequested;
      mAllocationStatus = iAllocationStatus;
      mSCOID = iSCOID;

      if( iBucketType != null )
      {
         mBucketType = iBucketType;
      }
   }

   /**
    * Performs an equal function for buckets
    * 
    * @param iBucketAllocation Object describing the bucket
    * 
    * @return Whether the buckets equal
    */
   public boolean equals ( BucketAllocation iBucketAllocation )
   {
      boolean returnValue = false;

      try
      {
         if ( (this.mBucketID.equals(iBucketAllocation.mBucketID)) &&
              (this.mBucketType.equals(iBucketAllocation.mBucketType)) &&
              (this.mMinimumSizeInt == iBucketAllocation.mMinimumSizeInt) &&
              (this.mPersistence == iBucketAllocation.mPersistence) &&
              (this.mReduciblebool == iBucketAllocation.mReduciblebool) &&
              (this.mRequestedSizeInt == iBucketAllocation.mRequestedSizeInt) )
         {
              returnValue = true;
         }
      }
      catch( Exception e )
      {
         System.out.println( e.getMessage() );
         e.printStackTrace();
      }

      return returnValue;
   }

   /**
    * Accessor method to retrieve the ID of the bucket.
    *
    * @return - The identifier of the bucket.
    */
   public String getBucketID()
   {
      return mBucketID;
   }

   /**
    *
    * Accessor method to retrieve the Type of the bucket.
    *
    * @return - Reference to a type definition for the bucket's data.
    *
    */
   public String getBucketType()
   {
      return mBucketType;
   }


   /**
    *
    * Accessor method to retrieve the minimum size requirement of the bucket.
    *
    * @return - The minimum amount of space requested.
    *
    */
   public String getMinimum()
   {
      return mMinimum;
   }

   /**
    *
    * Accessor method to retrieve the persistence scope of the bucket.
    *
    * @return - How long the runtime system should persist the data in
    *           the bucket.  This value may be one of the enumerated values
    *           described in the <code>Persistence</code> class.
    *
    */
   public int getPersistence()
   {
      return mPersistence;
   }

   /**
    *
    * Accessor method to retrieve the value that indicates if the amount of
    * space requested was allowed to be reduced.
    *
    * @return - An Indicator if the amount of space requested was allowed to be
    *           reduced.
    *
    */
   public String getReducible()
   {
      return mReducible;
   }

   /**
    *
    * Accessor method to retrieve the requested size of the bucket.
    *
    * @return - The amount of space requested.
    *
    */
   public String getRequested()
   {
      return mRequested;
   }

   /**
    *
    * Accessor method to retrieve the allocation status of the bucket.
    *
    * @return - The allocation status of the bucket.
    *
    */
   public int getAllocationStatus()
   {
      return mAllocationStatus;
   }

   /**
    *
    * Accessor method to retrieve the SCOID of the bucket.
    *
    * @return - The SCOID of the bucket.
    *
    */
   public String getSCOID()
   {
      return mSCOID;
   }

   /**
    *
    * Accessor method to retrieve the ReallocateFailure flag
    * of the bucket.
    *
    * @return - The SCOID of the bucket.
    *
    */
   public boolean getReallocateFailure()
   {
      return mReallocateFailure;
   }

   /**
    *
    * Accessor method to retrieve the Learner ID
    * of the bucket.
    *
    * @return - The Learner ID of the bucket.
    *
    */
   public String getStudentID()
   {
      return mStudentID;
   }

   /**
    *
    * Accessor method to retrieve the Course ID
    * of the bucket.
    *
    * @return - The Course ID of the bucket.
    *
    */
   public String getCourseID()
   {
      return mCourseID;
   }

   /**
    *
    * Accessor method to retrieve the Attempt ID
    * of the bucket.
    *
    * @return - The Attempt ID of the bucket.
    *
    */
   public String getAttemptID()
   {
      return mAttemptID;
   }

   /**
    *
    * Accessor method to retrieve the Offset
    * of the bucket.
    *
    * @return - The Offset of the bucket.
    *
    */
   public String getOffset()
   {
      return mOffset;
   }

   /**
    *
    * Accessor method to retrieve the Size
    * of the bucket.
    *
    * @return - The Size of the bucket.
    *
    */
   public String getSize()
   {
      return mSize;
   }

   /**
    *
    * Accessor method to retrieve the value
    * of the bucket.
    *
    * @return - The value of the bucket.
    *
    */
   public String getValue()
   {
      return mValue;
   }

   /**
    *
    * Accessor method to retrieve the minimin size integer
    * of the bucket.
    *
    * @return - The minimum size integer of the bucket.
    *
    */
   public int getMinimumSizeInt()
   {
      return mMinimumSizeInt;
   }

   /**
    *
    * Accessor method to retrieve the requested size integer
    * of the bucket.
    *
    * @return - The requested size integer of the bucket.
    *
    */
   public int getRequestedSizeInt()
   {
      return mRequestedSizeInt;
   }

   /**
    *
    * Accessor method to retrieve the offset integer
    * of the bucket.
    *
    * @return - The offset integer of the bucket.
    *
    */
   public int getOffsetInt()
   {
      return mOffsetInt;
   }

   /**
    *
    * Accessor method to retrieve the size integer
    * of the bucket.
    *
    * @return - The size integer of the bucket.
    *
    */
   public int getSizeInt()
   {
      return mSizeInt;
   }

   /**
    *
    * Accessor method to retrieve the reducible boolean
    * of the bucket.
    *
    * @return - The reducible boolean of the bucket.
    *
    */
   public boolean getReducibleBoolean()
   {
      return mReduciblebool;
   }

   /**
    *
    * Accessor method to retrieve the managed bucket index
    * of the bucket.
    *
    * @return - The managed bucket index of the bucket.
    *
    */
   public int getManagedBucketIndex()
   {
      return mManagedBucketIndex;
   }

   /**
    *
    * Accessor method to retrieve the activity ID
    * of the bucket.
    *
    * @return - The activity ID of the bucket.
    *
    */
   public int getActivityID()
   {
      return mActivityID;
   }






   /**
    * Set method to set the ID of the bucket.
    *
    * @param iBucketID - The ID of the bucket.
    */
   public void setBucketID( String iBucketID )
   {
      mBucketID = iBucketID;
   }

   /**
    *
    * Set method to set the Type of the bucket.
    *
    *
    * @param iBucketType - The type for the bucket.
    *
    */
   public void setBucketType( String iBucketType )
   {

      if (iBucketType != null)
      {
         mBucketType = iBucketType;
      }
          
   }


   /**
    *
    * Set method to set the minimum size requirement of the bucket.
    *
    *
    * @param iMinimum - The minimum size for the bucket.
    *
    */
   public void setMinimum( String iMinimum )
   {
      mMinimum = iMinimum;
   }

   /**
    *
    * Set method to set the persistence scope of the bucket.
    *
    *
    * @param iPersistence - The persistence scope of the bucket.
    *
    */
   public void setPersistence( int iPersistence )
   {
      mPersistence = iPersistence;
   }

   /**
    *
    * Set method to set the value that indicates if the amount of
    * space requested was allowed to be reduced.
    *
    *
    * @param iReducible - The indicatior if the bucket space can be reduced.
    *
    */
   public void setReducible( String iReducible )
   {
      mReducible = iReducible;
   }

   /**
    *
    * Set method to set the requested size of the bucket.
    *
    *
    * @param iRequested - The amount of space requested for the bucket.
    *
    */
   public void setRequested( String iRequested )
   {
      mRequested = iRequested;
   }

   /**
    *
    * Set method to set the allocation status of the bucket.
    *
    *
    * @param iAllocationStatus - The allocation status for the bucket.
    *
    */
   public void setAllocationStatus( int iAllocationStatus )
   {
      mAllocationStatus = iAllocationStatus;
   }

   /**
    *
    * Set method to set the SCOID of the bucket.
    *
    *
    * @param iSCOID - The SCOID for the bucket.
    *
    */
   public void setSCOID( String iSCOID )
   {
      mSCOID = iSCOID;
   }

   /**
    *
    * Set method to set the ReallocateFailure flag
    * of the bucket.
    *
    *
    * @param iReallocateFailure - The ReallocateFailure falg.
    *
    */
   public void setReallocateFailure( boolean iReallocateFailure )
   {
      mReallocateFailure = iReallocateFailure;
   }

   /**
    *
    * Set method to set the student ID
    * of the bucket.
    *
    *
    * @param iStudentID - The student ID.
    *
    */
   public void setStudentID( String iStudentID )
   {
      mStudentID = iStudentID;
   }


   /**
    *
    * Set method to set the course ID
    * of the bucket.
    *
    *
    * @param iCourseID - The course ID.
    *
    */
   public void setCourseID( String iCourseID )
   {
      mCourseID = iCourseID;
   }

   /**
    *
    * Set method to set the attempt ID
    * of the bucket.
    *
    *
    * @param iAttemptID - The attempt ID.
    *
    */
   public void setAttemptID( String iAttemptID )
   {
      mAttemptID = iAttemptID;
   }

   /**
    *
    * Set method to set the offset
    * of the bucket.
    *
    *
    * @param iOffset - The offset.
    *
    */
   public void setOffset( String iOffset )
   {
      mOffset = iOffset;
   }

   /**
    *
    * Set method to set the size
    * of the bucket.
    *
    *
    * @param iSize - The size.
    *
    */
   public void setSize( String iSize )
   {
      mSize = iSize;
   }

   /**
    *
    * Set method to set the value
    * of the bucket.
    *
    *
    * @param iValue - The value.
    *
    */
   public void setValue( String iValue )
   {
      mValue = iValue;
   }

   /**
    *
    * Set method to set the minimum size integer
    * of the bucket.
    *
    *
    * @param iMinimumSizeInt - The minimum size integer.
    *
    */
   public void setMinimumSizeInt( int iMinimumSizeInt )
   {
      mMinimumSizeInt = iMinimumSizeInt;
   }

   /**
    *
    * Set method to set the requested size integer
    * of the bucket.
    *
    *
    * @param iRequestedSizeInt - The requested size integer.
    *
    */
   public void setRequestedSizeInt( int iRequestedSizeInt )
   {
      mRequestedSizeInt = iRequestedSizeInt;
   }

   /**
    *
    * Set method to set the offset integer
    * of the bucket.
    *
    *
    * @param iOffsetInt - The offset integer.
    *
    */
   public void setOffsetInt( int iOffsetInt )
   {
      mOffsetInt = iOffsetInt;
   }

   /**
    *
    * Set method to set the size integer
    * of the bucket.
    *
    *
    * @param iSizeInt - The size integer.
    *
    */
   public void setSizeInt( int iSizeInt )
   {
      mSizeInt = iSizeInt;
   }

   /**
    *
    * Set method to set the reducible boolean
    * of the bucket.
    *
    *
    * @param iReduciblebool - The reducible boolean.
    *
    */
   public void setReducibleBoolean( boolean iReduciblebool )
   {
      mReduciblebool = iReduciblebool;
   }

   /**
    *
    * Set method to set the managed bucket index
    * of the bucket.
    *
    *
    * @param iManagedBucketIndex - The managed bucket index.
    *
    */
   public void setManagedBucketIndex( int iManagedBucketIndex )
   {
      mManagedBucketIndex = iManagedBucketIndex;
   }

   /**
    *
    * Set method to set the activity ID
    * of the bucket.
    *
    *
    * @param iActivityID - The activity ID.
    *
    */
   public void setActivityID( int iActivityID )
   {
      mActivityID = iActivityID;
   }




}

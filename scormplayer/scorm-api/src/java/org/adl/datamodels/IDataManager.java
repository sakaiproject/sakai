package org.adl.datamodels;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.adl.datamodels.ieee.IValidatorFactory;

public interface IDataManager extends Serializable {

	/**
	 * Adds the identified data model to the set of run-time data models managed
	 * for this SCO.  First checks the current set of managed data models
	 * to ensure that the data model to be added is not aready present in the 
	 * Hashtable.
	 * 
	 * @param iModel  Describes the run-time data model to be added.
	 * @param validatorFactory TODO
	 */
	public DataModel addDM(int iModel, IValidatorFactory validatorFactory);

	/**
	 * Processes an equals() request against the SCO's run-time data.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * 
	 * @return A data model error code indicating the result of this
	 *         operation.
	 */
	public int equals(DMRequest iRequest);

	public long getAttemptNumber();

	public Date getBeginDate();

	public String getCourseId();

	/**
	 * Retrieves a specific Data Model managed by this 
	 * <code>SCODataManager</code>.
	 * 
	 * @param iDataModel  Describes the dot-notation binding string of the
	 *                    desired data model.
	 * 
	 * @return The <code>DataModel</code> object associated with the
	 *         requested data model.
	 */
	public DataModel getDataModel(String iDataModel);

	public Map<String, DataModel> getDataModels();

	public Long getId();

	public Date getLastModifiedDate();

	public String getScoId();

	public String getTitle();

	public String getUserId();

	/**
	 * Processes a GetValue() request against the SCO's run-time data.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * 
	 * @param oInfo    Provides the value returned by this request.
	 * 
	 * @return A data model error code indicating the result of this
	 *         operation.
	 */
	public int getValue(DMRequest iRequest, DMProcessingInfo oInfo);

	/** 
	 * Initializes all data models being managed for this SCO.
	 */
	public void initialize();

	public void setAttemptNumber(long attemptNumber);

	public void setBeginDate(Date beginDate);

	public void setCourseId(String courseId);

	public void setDataModels(Map<String, DataModel> dataModels);

	public void setId(Long id);

	public void setLastModifiedDate(Date lastModifiedDate);

	public void setScoId(String scoId);

	public void setUserId(String userId);

//	public void setValidatorFactory(IValidatorFactory validatorFactory);

	/**
	 * Processes a SetValue() request against the SCO's run-time data.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * @param validatorFactory TODO
	 * 
	 * @return A data model error code indicating the result of this
	 *         operation.
	 */
	public int setValue(DMRequest iRequest, IValidatorFactory validatorFactory);

	/** 
	 * Terminates all data models being managed for this SCO.
	 * @param validatorFactory TODO
	 */
	public void terminate(IValidatorFactory validatorFactory);

	/**
	 * Processes a validate() request against the SCO's run-time data.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * 
	 * @return A data model error code indicating the result of this
	 *         operation.
	 */
	public int validate(DMRequest iRequest);

}
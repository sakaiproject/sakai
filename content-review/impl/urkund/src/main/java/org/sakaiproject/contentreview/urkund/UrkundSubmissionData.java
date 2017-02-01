package org.sakaiproject.contentreview.urkund;

import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrkundSubmissionData {

	public Integer Id;
	public Date Timestamp;
	public String ExternalId;
	public String Filename;
	public String MimeType;
	public Map<String, Object> Status;
	public Map<String, Object> Document;
	public Map<String, Object> Report;
	public String Subject;
	public Boolean Anonymous;
	
	public String LocalisedMessage;
	public String Message;
	
	public Double getSignificance(){
		try {
			if(Report != null){
				return Double.parseDouble(Report.get("Significance").toString());
			}
		} catch(Exception e){}
		return 0.;
	}
}

package org.sakaiproject.contentreview.urkund;

import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;


public class UrkundSubmissionData {

	@Getter @Setter public Integer Id;
	@Getter @Setter public Date Timestamp;
	@Getter @Setter public String ExternalId;
	@Getter @Setter public String Filename;
	@Getter @Setter public String MimeType;
	@Getter @Setter public Map<String, Object> Status;
	@Getter @Setter public Map<String, Object> Document;
	@Getter @Setter public Map<String, Object> Report;
	@Getter @Setter public String Subject;
	@Getter @Setter public Boolean Anonymous;
	
	@Getter @Setter public String LocalisedMessage;
	@Getter @Setter public String Message;
	
	public Double getSignificance(){
		try {
			if(Report != null){
				return Double.parseDouble(Report.get("Significance").toString());
			}
		} catch(Exception e){}
		return 0.;
	}
}

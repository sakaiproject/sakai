package org.sakaiproject.contentreview.urkund;

import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrkundSubmissionData {

	/* Capitalized variables because the returned JSON response by Urkund and Jackson deserialization.
	 * 
	 * EXAMPLE JSON returned by Urkund :
	 * 
	 [
	  {
	    "Id": 00000000,
	    "ExternalId": "EXT-001",
	    "Filename": "test.pdf",
	    "MimeType": "application/pdf",
	    "Timestamp": "2017-01-01T00:00:00",
	    "Status": {
	      "Message": "The document has been analyzed.",
	      "State": "Analyzed"
	    },
	    "Document": {
	      "Date": "2017-01-01T00:00:00",
	      "DownloadUrl": "https://secure.urkund.com/archive/download/?c1=0000",
	      "OptOutInfo": {
	        "Message": "BIG MESSAGE",
	        "Url": "https://secure.urkund.com/account/document/exemptionstatus/0000"
	      },
	      "Id": 00000000
	    },
	    "Report": {
	      "MatchCount": 128,
	      "ReportUrl": "https://secure.urkund.com/view/0000",
	      "Significance": 99.08,
	      "SourceCount": 74,
	      "Warnings": [],
	      "Id": 00000000
	    },
	    "Subject": null,
	    "Message": null,
	    "Anonymous": false
	  }
	]
	 * */
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

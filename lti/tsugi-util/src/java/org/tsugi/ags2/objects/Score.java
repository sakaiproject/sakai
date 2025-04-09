package org.tsugi.ags2.objects;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

/* application/vnd.ims.lis.v1.score+json
	{
	  "timestamp": "2017-04-16T18:54:36.736+00:00",
	  "scoreGiven" : 83,
	  "scoreMaximum" : 100,
	  "comment" : "This is exceptional work.",
	  "activityProgress" : "Completed",
	  "gradingProgress": "FullyGraded",
	  "userId" : "5323497"
	}
 */
public class Score extends org.tsugi.jackson.objects.JacksonBase {

	public static final String MIME_TYPE = "application/vnd.ims.lis.v1.score+json";

	/**
	 * the user has not started the activity, or the activity has been reset for that student.
	 */
	public static final String ACTIVITY_INITIALIZED = "Initialized";
	
	/**
	 * the activity associated with the line item has been started by the user to which the result relates.
	 */
	public static final String ACTIVITY_STARTED = "Started";
	
	/**
	 * the activity is being drafted and is available for comment.
	 */
	public static final String ACTIVITY_INPROGRESS = "InProgress";	
		
	/**
	 * the activity has been submitted at least once by the user but the user is still able make further submissions.
	 */
	public static final String ACTIVITY_SUBMITTED = "Submitted";

	/**
	 * the user has completed the activity associated with the line item.
	 */
	public static final String ACTIVITY_COMPLETED = "Completed";	

	/**
	 * The grading process is completed; the score value, if any, represents the current Final Grade
	 */
	public static final String GRADING_FULLYGRADED = "FullyGraded";	
	
	/**
	 * Final Grade is pending, but does not require manual intervention; if a Score value is present, it indicates the current value is partial and may be updated.
	 */
	public static final String GRADING_PENDING = "Pending";	

	/**
	 * Final Grade is pending, and it does require human intervention; if a Score value is present, it indicates the current value is partial and may be updated during the manual grading.
	 */
	public static final String GRADING_PENDINGMANUAL = "PendingManual";	
	
	/**
	 * The grading could not complete.
	 */
	public static final String GRADING_FAILED = "Failed";	

	/**
	 * The grading could not complete.
	 */
	public static final String GRADING_NOTREADY = "NotReady";	
	
	@JsonProperty("timestamp")
	public String timestamp;
	/*
	 * All scoreGiven values MUST be positive number (including 0). scoreMaximum represents the denominator and MUST be present
	 * when scoreGiven is present. When scoreGiven is not present or null, this indicates there is presently no score for
	 * that user, and the platform should clear any previous score value it may have previously received from the tool and
	 *stored for that user and line item.
	 */
	@JsonProperty("scoreGiven")
	public Double scoreGiven;
	// The maximum score for this line item. Maximum score MUST be a numeric non-null value, strictly greater than 0.
	@JsonProperty("scoreMaximum")
	public Double scoreMaximum;
	@JsonProperty("userId")
	public String userId;      // TODO: LTI13 quirk - should be subject
	@JsonProperty("comment")
	public String comment;
	@JsonProperty("activityProgress")
	public String activityProgress;
	@JsonProperty("gradingProgress")
	public String gradingProgress;

	public Double getScoreGiven() {
		if (scoreGiven == null ) return null;
		if ( scoreGiven < 0.0 ) return 0.0;
		return scoreGiven;
	}

	public Double getScoreMaximum() {
		if ( scoreMaximum != null && scoreMaximum >= 0.0 ) return scoreMaximum;
		Double sg = getScoreGiven();
		if ( sg != null && sg < 2.0 ) return 1.0;
		return 100.0;
	}

	public String toString() {
		return "Score: " + scoreGiven + " / " + scoreMaximum + " " + userId;
	}
}

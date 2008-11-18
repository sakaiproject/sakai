package org.sakaiproject.sitestats.tool.wicket.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;


public class ReportParamsModel extends Model implements ReportParams {
	private static final long	serialVersionUID		= 1L;
	private String				siteId;
	private String				what					= ReportManager.WHAT_VISITS;
	private String				whatEventSelType		= ReportManager.WHAT_EVENTS_BYTOOL;
	private List<String>		whatToolIds				= new ArrayList<String>();
	private List<String>		whatEventIds			= new ArrayList<String>();
	private boolean				whatLimitedAction		= false;
	private boolean				whatLimitedResourceIds	= false;
	private List<String>		whatResourceIds			= new ArrayList<String>();
	private String				whatResourceAction		= ReportManager.WHAT_RESOURCES_ACTION_NEW;
	private String				when					= ReportManager.WHEN_LAST7DAYS;
	private Date				whenFrom;
	private Date				whenTo;
	private String				who						= ReportManager.WHO_ALL;
	private String				whoRoleId;
	private String				whoGroupId;
	private List<String>		whoUserIds				= new ArrayList<String>();
	private List<String>		howTotalsBy				= new ArrayList<String>();

	
	public ReportParamsModel(String siteId){
		this.siteId = siteId;
		whenFrom = new Date();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		whenFrom = c.getTime();
		whenTo = new Date();
	}
	
	public ReportParamsModel(String siteId, String what, List<String> whatToolIds, List<String> whatEventIds, String whatResourceAction, List<String> whatResourceIds, String when, Date whenFrom, Date whenTo, String who, String whoRoleId, String whoGroupId, List<String> whoUserIds) {
		super();
		this.siteId = siteId;
		this.what = what;
		this.whatToolIds = whatToolIds;
		this.whatEventIds = whatEventIds;
		this.whatResourceAction = whatResourceAction;
		this.whatLimitedAction = whatResourceAction != null;
		this.whatResourceIds = whatResourceIds;
		this.whatLimitedResourceIds = whatResourceIds != null;
		this.when = when;
		this.whenFrom = whenFrom;
		this.whenTo = whenTo;
		this.who = who;
		this.whoRoleId = whoRoleId;
		this.whoGroupId = whoGroupId;
		this.whoUserIds = whoUserIds;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportParams#getSiteId()
	 */
	public String getSiteId() {
		return siteId;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportParams#setWherSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhat()
	 */
	public String getWhat() {
		return what;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhat(java.lang.String)
	 */
	public void setWhat(String what) {
		this.what = what;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#getWhatEventSelType()
	 */
	public String getWhatEventSelType() {
		return whatEventSelType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#setWhatEventSelType(java.lang.String)
	 */
	public void setWhatEventSelType(String whatEventSelType) {
		this.whatEventSelType = whatEventSelType;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhatEventIds()
	 */
	public List<String> getWhatEventIds() {
		return whatEventIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhatEventIds(java.util.List)
	 */
	public void setWhatEventIds(List<String> whatEventIds) {
		this.whatEventIds = whatEventIds;
	}
	public void setWhatLimitedAction(boolean whatLimitedAction) {
		this.whatLimitedAction = whatLimitedAction;
	}

	public boolean isWhatLimitedAction() {
		return whatLimitedAction;
	}

	public void setWhatLimitedResourceIds(boolean whatLimitedResourceIds) {
		this.whatLimitedResourceIds = whatLimitedResourceIds;
	}

	public boolean isWhatLimitedResourceIds() {
		return whatLimitedResourceIds;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhatResourceIds()
	 */
	public List<String> getWhatResourceIds() {
		if(isWhatLimitedResourceIds()) {
			return whatResourceIds;
		}else{
			return new ArrayList<String>();
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhatResourceIds(java.util.List)
	 */
	public void setWhatResourceIds(List<String> whatResourceIds) {
		this.whatResourceIds = whatResourceIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#getWhatResourceAction()
	 */
	public String getWhatResourceAction(){
		if(isWhatLimitedAction()) {
			return whatResourceAction;
		}else{
			return null;
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ReportParams#setWhatResourceAction(java.lang.String)
	 */
	public void setWhatResourceAction(String whatResourceAction){
		this.whatResourceAction = whatResourceAction;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhatToolIds()
	 */
	public List<String> getWhatToolIds() {
		return whatToolIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhatToolIds(java.util.List)
	 */
	public void setWhatToolIds(List<String> whatToolIds) {
		this.whatToolIds = whatToolIds;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhen()
	 */
	public String getWhen() {
		return when;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhen(java.lang.String)
	 */
	public void setWhen(String when) {
		this.when = when;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhenEnd()
	 */
	public Date getWhenTo() {
		return whenTo;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhenEnd(java.util.Date)
	 */
	public void setWhenTo(Date whenTo) {
		this.whenTo = whenTo;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhenStart()
	 */
	public Date getWhenFrom() {
		return whenFrom;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhenStart(java.util.Date)
	 */
	public void setWhenFrom(Date whenFrom) {
		this.whenFrom = whenFrom;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWho()
	 */
	public String getWho() {
		return who;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWho(java.lang.String)
	 */
	public void setWho(String who) {
		this.who = who;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhoGroupId()
	 */
	public String getWhoGroupId() {
		return whoGroupId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhoGroupId(java.lang.String)
	 */
	public void setWhoGroupId(String whoGroupId) {
		this.whoGroupId = whoGroupId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhoRoleId()
	 */
	public String getWhoRoleId() {
		return whoRoleId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhoRoleId(java.lang.String)
	 */
	public void setWhoRoleId(String whoRoleId) {
		this.whoRoleId = whoRoleId;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#getWhoUserIds()
	 */
	public List<String> getWhoUserIds() {
		if(ReportManager.WHO_CUSTOM.equals(getWho())) {
			return whoUserIds;
		}else{
			return new ArrayList<String>();
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.impl.ReportParams#setWhoUserIds(java.util.List)
	 */
	public void setWhoUserIds(List<String> whoUserIds) {
		this.whoUserIds = whoUserIds;
	}
		
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportParams#setHowTotalsBy(java.util.List)
	 */
	public void setHowTotalsBy(List<String> totalsBy) {
		//this.howTotalsBy = totalsBy;
		this.howTotalsBy = howTotalsBy;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.report.ReportParams#getHowTotalsBy()
	 */
	public List<String> getHowTotalsBy() {
		//return howTotalsBy;
		return fixedHowTotalsBy(howTotalsBy);
	}
	
	private List<String> fixedHowTotalsBy(List<String> list) {
		List<String> fixedList = new ArrayList<String>();
		if(list == null || list.isEmpty()) {
			// set defaults if none specified
			if(getWhat().equals(ReportManager.WHAT_RESOURCES)){
				fixedList = StatsManager.TOTALSBY_RESOURCE_DEFAULT;
			}else{
				fixedList = StatsManager.TOTALSBY_EVENT_DEFAULT;
			}
		}else{
			// remove columns that shouldn't be selected
			for(String t : list) {
				if(t.equals(StatsManager.T_EVENT)) {
					if(!getWhat().equals(ReportManager.WHAT_RESOURCES)){
						fixedList.add(t);
					}
				}else if(t.equals(StatsManager.T_RESOURCE) || t.equals(StatsManager.T_RESOURCE_ACTION)) {
					if(getWhat().equals(ReportManager.WHAT_RESOURCES)){
						fixedList.add(t);
					}
				}else{
					fixedList.add(t);
				}
			}
		}
		return fixedList;
	}
	
	
	// ------------------------------------------------------------
	// Output functions (String, XML)
	// ------------------------------------------------------------	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append('{');		
		str.append(memberToString("siteId", siteId, true));
		str.append(memberToString("what", what, true));
		if(ReportManager.WHAT_EVENTS.equals(what)) {
			str.append(memberToString("whatEventSelType", whatEventSelType, true));
			if(ReportManager.WHAT_EVENTS_BYTOOL.equals(whatEventSelType)) {
				str.append(memberToString("whatToolIds", whatToolIds, true));
			}else{
				str.append(memberToString("whatEventIds", whatEventIds, true));
			}
		}else if(ReportManager.WHAT_RESOURCES.equals(what)) {
			if(whatResourceAction != null) {
				str.append(memberToString("whatResourceAction", whatResourceAction, true));
			}
			if(whatResourceIds != null) {
				str.append(memberToString("whatResourceIds", whatResourceIds, true));
			}
		}
		str.append(memberToString("when", when, true));
		if(ReportManager.WHEN_CUSTOM.equals(when)) {
			str.append(memberToString("whenFrom", whenFrom.toString(), true));
			str.append(memberToString("whenTo", whenTo.toString(), true));
		}
		if(ReportManager.WHO_ALL.equals(who)) {
			str.append(memberToString("who", who, true));
		}else{
			str.append(memberToString("who", who, true));
		}
		if(ReportManager.WHO_GROUPS.equals(who)) {
			str.append(memberToString("whoGroupId", whoGroupId, true));
		}
		if(ReportManager.WHO_ROLE.equals(who)) {
			str.append(memberToString("whoRoleId", whoRoleId, true));
		}
		if(ReportManager.WHO_CUSTOM.equals(who)) {
			str.append(memberToString("whoUserIds", whoUserIds, true));
		}	
		str.append(memberToString("howTotalsBy", getHowTotalsBy(), false));
		str.append('}');
		return str.toString();
	}
	
	private String memberToString(String member, String value, boolean hasMore) {
		StringBuilder str = new StringBuilder();
		str.append(member);
		str.append(": ");
		str.append(value);
		if(hasMore) {
			str.append(", ");
		}
		return str.toString();
	}
	
	private String memberToString(String member, List<String> values, boolean hasMore) {
		StringBuilder str = new StringBuilder();
		str.append(member);
		str.append(": ");
		str.append('[');
		boolean first = true;
		if(values != null) {
			for(String value : values) {
				if(!first) {
					str.append(", ");				
				}
				str.append(value);
				first = false;
			}
		}
		str.append(']');
		if(hasMore) {
			str.append(", ");
		}
		return str.toString();
	}
}

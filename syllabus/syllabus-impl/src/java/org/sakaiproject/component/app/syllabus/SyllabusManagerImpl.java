/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.syllabus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.query.Query;
import org.hibernate.criterion.Expression;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import org.sakaiproject.api.app.syllabus.SyllabusAttachment;
import org.sakaiproject.api.app.syllabus.SyllabusData;
import org.sakaiproject.api.app.syllabus.SyllabusItem;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;


/**
 * SyllabusManagerImpl provides convenience functions to query the database
 * 
 * @author <a href="mailto:jlannan@iupui.edu">Jarrod Lannan </a>
 * @version $Id:
 */
@Slf4j
public class SyllabusManagerImpl extends HibernateDaoSupport implements SyllabusManager
{
  private ContentHostingService contentHostingService;
  private CalendarService calendarService;
  private PreferencesService preferencesService;
  private TimeService timeService;
  private EntityManager entityManager;
  @Setter private UserDirectoryService userDirectoryService;
  private static final String SURROGATE_KEY = "surrogateKey";
  private static final String VIEW = "view";
  private static final String SYLLABI = "syllabi";  
  private static final String SYLLABUS_DATA_ID = "syllabusId";
  private static final String ATTACHMENTS = "attachments";
  
  public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
  
  /**
   * createSyllabusItem creates a new SyllabusItem
   * @param userId
   * @param contextId
   * @param redirectURL
   *        
   */
  public SyllabusItem createSyllabusItem(String userId, String contextId,
      String redirectURL)
  {
    if (userId == null || contextId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {
      // construct a new SyllabusItem
      SyllabusItem item = new SyllabusItem(userId, contextId, redirectURL);
      saveSyllabusItem(item);
      return item;
    }
  }
  
  /**
   * getSyllabiForSyllabusItem returns the collection of syllabi
   * @param syllabusItem
   */
  public Set getSyllabiForSyllabusItem(final SyllabusItem syllabusItem)
  {
    if (syllabusItem == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {                 
      HibernateCallback<Set> hcb = session -> {
        // get syllabi in an eager fetch mode
        Criteria crit = session.createCriteria(SyllabusItem.class)
                    .add(Expression.eq(SURROGATE_KEY, syllabusItem.getSurrogateKey()))
                    .setFetchMode(SYLLABI, FetchMode.EAGER);


        SyllabusItem syllabusItem1 = (SyllabusItem) crit.uniqueResult();

        if (syllabusItem1 != null){
          return syllabusItem1.getSyllabi();
        }
        return new TreeSet();
      };
      return getHibernateTemplate().execute(hcb);
    }
  }  
  
  public SyllabusData createSyllabusDataObject(String title, Integer position,
	        String asset, String view, String status, String emailNotification,
	        Date startDate, Date endDate, boolean linkCalendar, String calendarEventIdStartDate,
	        String calendarEventIdEndDate, SyllabusItem syllabusItem)      
	  {
	    if (position == null)
	    {
	      throw new IllegalArgumentException("Null Argument");
	    }
	    else
	    {
	      // construct a new SyllabusData persistent object
	      SyllabusData data = new SyllabusData();
	      data.setTitle(title);
	      data.setPosition(position);
	      data.setAsset(asset);
	      data.setView(view);
	      data.setStatus(status);
	      data.setEmailNotification(emailNotification);
	      data.setStartDate(startDate);
	      data.setEndDate(endDate);
	      data.setLinkCalendar(linkCalendar);
	      data.setSyllabusItem(syllabusItem);
	      data.setCalendarEventIdStartDate(calendarEventIdStartDate);
	      data.setCalendarEventIdEndDate(calendarEventIdEndDate);
	      return saveSyllabus(data);
	    }
	  }
    
  /**
   * removes a syllabus data object (on form cancel action) 
   * @see org.sakaiproject.api.app.syllabus.SyllabusManager#removeSyllabusDataObject(org.sakaiproject.api.app.syllabus.SyllabusData)
   */
  public void removeSyllabusDataObject(SyllabusData o)
  {
    getHibernateTemplate().execute(session -> {
      session.delete(session.merge(o));
      return null;
    });
  }
  
  /**
   * swapSyllabusDataPositions swaps positions for two SyllabusData objects
   * @param syllabusItem
   * @param d1
   * @param d2
   */
  public void swapSyllabusDataPositions(final SyllabusItem syllabusItem, final SyllabusData d1, final SyllabusData d2)      
  {
    if (syllabusItem == null || d1 == null || d2 == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {
      getHibernateTemplate().execute(session -> {
        // load objects from hibernate
        SyllabusData data1 = session.get(SyllabusData.class, d1.getSyllabusId());
        SyllabusData data2 = session.get(SyllabusData.class, d2.getSyllabusId());

        Integer temp = data1.getPosition();
        data1.setPosition(data2.getPosition());
        data2.setPosition(temp);

        return null;
      });
    }
  }    

  public void updateSyllabudDataPosition(final SyllabusData d, final Integer position){
	  if(d == null || position == null){
		  throw new IllegalArgumentException("Null Argument");
	  }else{
		  d.setPosition(position);
	      getHibernateTemplate().merge(d);
	  }
  }

  /**
   * findLargestSyllabusPosition finds the largest syllabus data position for an item
   * @param syllabusItem
   */
  public Integer findLargestSyllabusPosition(final SyllabusItem syllabusItem)      
  {
    if (syllabusItem == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {
      List<Number> list = getHibernateTemplate().execute(session -> {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Number> query = builder.createQuery(Number.class);
        Root<SyllabusData> root = query.from(SyllabusData.class);
        query.select(builder.max(root.get("position")))
                .where(builder.equal(root.get("syllabusItem"), syllabusItem.getSurrogateKey()));
        return session.createQuery(query).getResultList();
      });
      return list.size() == 1 && list.get(0) != null ? list.get(0).intValue() : 0;
    }
  }    
  
    
  /**
   * getSyllabusItemByContextId finds a SyllabusItem
   * @param contextId
   * @return SyllabusItem
   *        
   */
  public SyllabusItem getSyllabusItemByContextId(final String contextId)
  {
    if (contextId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    List<SyllabusItem> list = getHibernateTemplate().execute(session -> {
      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<SyllabusItem> query = builder.createQuery(SyllabusItem.class);
      Root<SyllabusItem> root = query.from(SyllabusItem.class);
      query.select(root).where(builder.equal(root.get("contextId"), contextId));
      return session.createQuery(query).getResultList();
    });
    return list.size() == 1 ? list.get(0) : null;
  }
  
  @SuppressWarnings("unchecked")
  public Set<SyllabusData> findPublicSyllabusData() {
      HibernateCallback<List<SyllabusData>> hcb = session -> {
        Criteria crit = session.createCriteria(SyllabusData.class)
                    .add(Expression.eq(VIEW, "yes"))
                    .setFetchMode(ATTACHMENTS, FetchMode.EAGER);

        return crit.list();
      };
      return new HashSet<>(getHibernateTemplate().execute(hcb));
  }
  
  @SuppressWarnings("unchecked")
  private Set<SyllabusData> findPublicSyllabusDataWithCalendarEvent(final long syllabusId) {
      HibernateCallback<List<SyllabusData>> hcb = session -> {
        Criteria crit = session.createCriteria(SyllabusData.class)
                        .add(Expression.eq("syllabusItem.surrogateKey", syllabusId))
                    .add(Expression.eq("status", "posted"))
                    .add(Expression.eq("linkCalendar", true));

        return crit.list();
      };
      return new HashSet<>(getHibernateTemplate().execute(hcb));
  }
  
  /**
   * getSyllabusItemByUserAndContextIds finds a SyllabusItem
   * @param userId
   * @param contextId
   * @return SyllabusItem
   *        
   */
  public SyllabusItem getSyllabusItemByUserAndContextIds(final String userId, final String contextId)
  {
    if (userId == null || contextId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    List<SyllabusItem> list = getHibernateTemplate().execute(session -> {
      CriteriaBuilder builder = session.getCriteriaBuilder();
      CriteriaQuery<SyllabusItem> query = builder.createQuery(SyllabusItem.class);
      Root<SyllabusItem> root = query.from(SyllabusItem.class);
      query.select(root).where(builder.and(builder.equal(root.get("userId"), userId), builder.equal(root.get("contextId"), contextId)));
      return session.createQuery(query).getResultList();
    });
    return list.size() == 1 ? list.get(0) : null;
  }
  
  /**
   * addSyllabusToSyllabusItem adds a SyllabusData object to syllabi collection
   * @param syllabusItem
   * @param syllabusData
   * @return Set
   */
  public void addSyllabusToSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData)
  {
	  addSyllabusToSyllabusItem(syllabusItem, syllabusData, true);
  }
  
  public void addSyllabusToSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData, boolean updateCalendar){
             
    if (syllabusItem == null || syllabusData == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }      
           
    HibernateCallback hcb = session -> {
      SyllabusItem returnedItem = (SyllabusItem) session.get(SyllabusItem.class, syllabusItem.getSurrogateKey());
      if (returnedItem != null){
        returnedItem.getSyllabi().add(syllabusData);
        returnedItem = (SyllabusItem) session.merge(returnedItem);
      }
      return null;
    };
    getHibernateTemplate().execute(hcb);
    updateSyllabusAttachmentsViewState(syllabusData);
    syllabusData.setSyllabusItem(syllabusItem);
    if(updateCalendar){
    	boolean modified = updateCalendarSettings(syllabusData);
    	if(modified){
    		getHibernateTemplate().merge(syllabusData);
    	}
    }
  }  
  
  
  /**
   * removeSyllabusToSyllabusItem loads many side of the relationship
   * @param syllabusItem
   * @param syllabusData
   * @return Set
   */
  public void removeSyllabusFromSyllabusItem(final SyllabusItem syllabusItem, final SyllabusData syllabusData)
  {
            
    if (syllabusItem == null || syllabusData == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    getHibernateTemplate().execute(session -> {
      SyllabusItem returnedItem = session.get(SyllabusItem.class, syllabusItem.getSurrogateKey());
      if (returnedItem != null){
        returnedItem.getSyllabi().remove(syllabusData);
        session.merge(returnedItem);
      }
      return null;
    });
  }

  /**
   * Make sure all attachments associated with a syllabus
   * are marked by the Content Hosting Service with appropriate
   * public (true/false) permissions.
   * 
   * @param syllabusData the SyllabusData object to check for publicness
   */
  public void updateSyllabusAttachmentsViewState(final SyllabusData syllabusData)
  {
	boolean publicView = "yes".equalsIgnoreCase(syllabusData.getView());
    Set<?> attachments = syllabusData.getAttachments();
    for (Object a: attachments) {
    	SyllabusAttachment attach = (SyllabusAttachment)a;
    	contentHostingService.setPubView(attach.getAttachmentId(), publicView);
    }
  }

  /**
   * Make sure this attachmentis marked by the Content Hosting Service
   * with the appropriate public (true/false) permissions.
   * 
   * @param syllabusData the SyllabusData object to check for publicness
   * @param attach the SyllabusAttachment object to update
   */
  private void updateSyllabusAttachmentViewState(final SyllabusData syllabusData, final SyllabusAttachment attach)
  {
	boolean publicView = "yes".equalsIgnoreCase(syllabusData.getView());
    contentHostingService.setPubView(attach.getAttachmentId(), publicView);
  }


  /**
   * saveSyllabusItem persists a SyllabusItem
   * @param item
   */
  public void saveSyllabusItem(SyllabusItem item)
  {
    getHibernateTemplate().saveOrUpdate(item);
  }
  
  public SyllabusData saveSyllabus(SyllabusData data)
  {
	  return saveSyllabus(data, true);
  }
  
  public SyllabusData saveSyllabus(SyllabusData data, boolean updateCalendar){
	  if(updateCalendar){
		  //calendar check
		  updateCalendarSettings(data);
	  }
	  SyllabusData savedData = getHibernateTemplate().merge(data);
	  if(updateCalendar){
		  updateSyllabusAttachmentsViewState(savedData);
		  //update calendar attachments
		  if(savedData.getAttachments() != null && savedData.getAttachments().size() > 0){
			  if(savedData.getCalendarEventIdStartDate() != null
					  && !"".equals(savedData.getCalendarEventIdStartDate())){
				  addCalendarAttachments(savedData.getSyllabusItem().getContextId(), savedData.getCalendarEventIdStartDate(), new ArrayList(savedData.getAttachments()));
			  }
			  if(savedData.getCalendarEventIdEndDate() != null
					  && !"".equals(savedData.getCalendarEventIdEndDate())){
				  addCalendarAttachments(savedData.getSyllabusItem().getContextId(), savedData.getCalendarEventIdEndDate(), new ArrayList(savedData.getAttachments()));
			  }
		  }
	  }
	  return savedData;
  }  

  public SyllabusData getSyllabusData(final String dataId)
  {
    if (dataId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {                 
      HibernateCallback<SyllabusData> hcb = session -> {
        Long longObj = new Long(dataId);
        return (SyllabusData) session.get(SyllabusData.class, longObj);
      };
      return getHibernateTemplate().execute(hcb);
    }

  }  

  public SyllabusItem getSyllabusItem(final Long itemId)
  {
    if (itemId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {                 
      HibernateCallback<SyllabusItem> hcb = session -> (SyllabusItem) session.get(SyllabusItem.class, itemId);
      return getHibernateTemplate().execute(hcb);
    }

  }
  
  public SyllabusAttachment createSyllabusAttachmentObject(String attachId, String name)      
  {
    try
    {
      SyllabusAttachment attach = new SyllabusAttachment();
      
      attach.setAttachmentId(attachId);
      
      attach.setName(name);

      ContentResource cr = contentHostingService.getResource(attachId);
      attach.setSize((Long.valueOf(cr.getContentLength())).toString());
      User creator = userDirectoryService.getUser(cr.getProperties().getProperty(cr.getProperties().getNamePropCreator()));
      attach.setCreatedBy(creator.getDisplayName());
      User modifier = userDirectoryService.getUser(cr.getProperties().getProperty(cr.getProperties().getNamePropModifiedBy()));
      attach.setLastModifiedBy(modifier.getDisplayName());
      attach.setType(cr.getContentType());
      String tempString = cr.getUrl();
      String surl = ServerConfigurationService.getServerUrl();
      tempString = tempString.indexOf(surl) > 0 ? tempString : tempString.substring(surl.length());
      String newString = new String();
      char[] oneChar = new char[1];
      for(int i=0; i<tempString.length(); i++)
      {
        if(tempString.charAt(i) != ' ')
        {
          oneChar[0] = tempString.charAt(i);
          String concatString = new String(oneChar);
          newString = newString.concat(concatString);
        }
        else
        {
          newString = newString.concat("%20");
        }
      } 
      //tempString.replaceAll(" ", "%20");
      attach.setUrl(newString);

      return saveSyllabusAttachment(attach);
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  public SyllabusAttachment saveSyllabusAttachment(SyllabusAttachment attach)
  {
    return getHibernateTemplate().merge(attach);
  }
  
  public void addSyllabusAttachToSyllabusData(final SyllabusData syllabusData, final SyllabusAttachment syllabusAttach)
  {
             
    if (syllabusData == null || syllabusAttach == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    SyllabusData updatedData = (SyllabusData) getHibernateTemplate().execute(session -> {
      SyllabusData returnedData = session.get(SyllabusData.class, syllabusData.getSyllabusId());
      if (returnedData != null) {
        syllabusAttach.setSyllabusData(returnedData);
        returnedData.getAttachments().add(syllabusAttach);
        return session.merge(returnedData);
      }
      return null;
    });
    if (updatedData != null) {
      updateSyllabusAttachmentViewState(updatedData, syllabusAttach);
    }
  }  


  public void removeSyllabusAttachmentObject(SyllabusAttachment o)
  {
    getHibernateTemplate().execute(session -> {
      session.delete(session.merge(o));
      return null;
    });
  }
  
  public void removeSyllabusAttachSyllabusData(final SyllabusData syllabusData, final SyllabusAttachment syllabusAttach)
  {
            
    if (syllabusData == null || syllabusAttach == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }

    getHibernateTemplate().execute(session -> {
      SyllabusData returnedData = session.get(SyllabusData.class, syllabusData.getSyllabusId());
      if (returnedData != null) {
        returnedData.getAttachments().remove(syllabusAttach);
        session.merge(returnedData);
      }
      return null;
    });
  }

  public Set getSyllabusAttachmentsForSyllabusData(final SyllabusData syllabusData)
  {
    if (syllabusData == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {                 
      HibernateCallback<Set<SyllabusAttachment>> hcb = session -> {
        Criteria crit = session.createCriteria(SyllabusData.class)
                    .add(Expression.eq(SYLLABUS_DATA_ID, syllabusData.getSyllabusId()))
                    .setFetchMode(ATTACHMENTS, FetchMode.EAGER);


        SyllabusData syllabusData1 = (SyllabusData) crit.uniqueResult();

        if (syllabusData1 != null){
          return syllabusData1.getAttachments();
        }
        return new TreeSet();
      };
      return getHibernateTemplate().execute(hcb);
    }
  }  

  public SyllabusAttachment getSyllabusAttachment(final String syllabusAttachId)
  {
    if (syllabusAttachId == null)
    {
      throw new IllegalArgumentException("Null Argument");
    }
    else
    {                 
      HibernateCallback<SyllabusAttachment> hcb = session -> {
        Long longObj = new Long(syllabusAttachId);
        return (SyllabusAttachment) session.get(SyllabusAttachment.class, longObj);
      };
      return getHibernateTemplate().execute(hcb);
    }

  }
  
  public CalendarService getCalendarService() {
		return calendarService;
	}

	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}

	public void removeCalendarEvent(String siteId, String eventId){
		try{
			String calendarId = calendarReference(siteId, SiteService.MAIN_CONTAINER);
			Calendar calendar = getCalendar(calendarId);
			if(calendar != null && eventId != null && !"".equals(eventId)){
				try{
					CalendarEvent calendarEvent = calendar.getEvent(eventId);
					calendar.removeEvent(calendar.getEditEvent(calendarEvent.getId(), CalendarService.EVENT_REMOVE_CALENDAR));
				}catch (PermissionException e)
				{
					//log.warn(e);
				}
				catch (InUseException e)
				{
					//log.warn(e);
				}
				catch (IdUnusedException e)
				{
					//log.warn(e);
				}
			}

		}catch (IdUnusedException e)
		{
			//log.warn(e);
		}catch (PermissionException e)
		{
			//log.warn(e);
		}
	}
	
	private boolean isOnSameDay(Date startDate, Date endDate){
		if(startDate != null && endDate != null){
			//check that the two dates are on the same day
			java.util.Calendar cal1 = java.util.Calendar.getInstance();
			java.util.Calendar cal2 = java.util.Calendar.getInstance();
			cal1.setTime(startDate);
			cal2.setTime(endDate);
			boolean sameDay = cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
			cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
			if(sameDay){
				return true;
			}
		}
		return false;
	}

	public boolean addCalendarEvent(SyllabusData data){
		boolean changed = false;
		String calendarId = calendarReference(data.getSyllabusItem().getContextId(), SiteService.MAIN_CONTAINER);
		try {
			Calendar calendar = getCalendar(calendarId);
			if(calendar != null){
				TimeRange timeRangeStart = null;
				TimeRange timeRangeEnd = null;
				if(data.getStartDate() != null && data.getEndDate() != null && isOnSameDay(data.getStartDate(), data.getEndDate())){
					Time sTime = timeService.newTime(data.getStartDate().getTime());
					Time eTime = timeService.newTime(data.getEndDate().getTime());
					timeRangeStart = timeService.newTimeRange(sTime, eTime);
				}else{ 
					if(data.getStartDate() != null){
						Time sTime = timeService.newTime(data.getStartDate().getTime());
						timeRangeStart = timeService.newTimeRange(sTime);
					}
					if(data.getEndDate() != null){
						Time sTime = timeService.newTime(data.getEndDate().getTime());
						timeRangeEnd = timeService.newTimeRange(sTime);
					}
				}
				
				List<Reference> attachments = entityManager.newReferenceList();
				
				//Start Date Event (or both if on the same day)
				if(timeRangeStart != null){
					CalendarEvent.EventAccess eAccess = CalendarEvent.EventAccess.SITE;
					// add event to calendar
					CalendarEvent event = calendar.addEvent(timeRangeStart,
							data.getTitle(),
							data.getAsset(),
							"Activity",
							"",
							eAccess,
							null,
							attachments);
					// now add the linkage to the assignment on the calendar side
					if (event.getId() != null) {
						// add the assignmentId to the calendar object

						CalendarEventEdit edit = calendar.getEditEvent(event.getId(), CalendarService.EVENT_ADD_CALENDAR);

						edit.setDescriptionFormatted(data.getAsset());

						calendar.commitEvent(edit);
						
						data.setCalendarEventIdStartDate(event.getId());
						
						changed = true;
					}
				}
				//End Date Event
				if(timeRangeEnd != null){
					CalendarEvent.EventAccess eAccess = CalendarEvent.EventAccess.SITE;
					// add event to calendar
					CalendarEvent event = calendar.addEvent(timeRangeEnd,
							data.getTitle(),
							data.getAsset(),
							"Deadline",
							"",
							eAccess,
							null,
							attachments);

					// now add the linkage to the assignment on the calendar side
					if (event.getId() != null) {
						// add the assignmentId to the calendar object

						CalendarEventEdit edit = calendar.getEditEvent(event.getId(), CalendarService.EVENT_ADD_CALENDAR);

						edit.setDescriptionFormatted(data.getAsset());

						calendar.commitEvent(edit);
						data.setCalendarEventIdEndDate(event.getId());
						changed = true;
					}
				}
			}
		} catch (IdUnusedException e) {
			//log.warn(e);
		} catch  (InUseException e) {
			//log.warn(e);
		}catch (PermissionException e){
			//log.warn(e);
		}


		return changed;
	}
  
	public void addCalendarAttachments(String siteId, String calendarEventId, List<SyllabusAttachment> attachments){
		if(attachments != null){
			String calendarId = calendarReference(siteId, SiteService.MAIN_CONTAINER);
				Calendar calendar;
				try {
					calendar = getCalendar(calendarId);
					if(calendar != null){
						CalendarEventEdit event = calendar.getEditEvent(calendarEventId, CalendarService.EVENT_ADD_CALENDAR);
						if(event != null){
							for(SyllabusAttachment attachment : attachments){
								ContentResource cr;
								try {
									cr = contentHostingService.getResource(attachment.getAttachmentId());
									if(cr != null){
										Reference ref = entityManager.newReference(cr.getReference());
										event.addAttachment(ref);
									}
								} catch (TypeException e) {
								}
							}
							calendar.commitEvent(event);
						}
					}
				} catch (IdUnusedException e) {
				} catch (PermissionException e) {
				} catch (InUseException e) {
				}
		}
	}
	
	public void removeCalendarAttachments(String siteId, String calendarEventId, SyllabusAttachment attachment){
		String calendarId = calendarReference(siteId, SiteService.MAIN_CONTAINER);
		Calendar calendar;
		try {
			calendar = getCalendar(calendarId);
			if(calendar != null){
				CalendarEventEdit event = calendar.getEditEvent(calendarEventId, CalendarService.EVENT_ADD_CALENDAR);
				if(event != null){
					for(Reference ref : event.getAttachments()){
						if(ref.getId().equals(attachment.getAttachmentId())){
							event.removeAttachment(ref);
							break;
						}
					}
					calendar.commitEvent(event);
				}
			}
		} catch (IdUnusedException e) {
		} catch (PermissionException e) {
		} catch (InUseException e) {
		}
	}
	
	public String calendarReference(String siteId, String container){
		return calendarService.calendarReference(siteId, container);
	}

	public Calendar getCalendar(String ref) throws IdUnusedException, PermissionException {
		return calendarService.getCalendar(ref);
	}
	
	public void updateAllCalendarEvents(long syllabusId){
		for(SyllabusData data : findPublicSyllabusDataWithCalendarEvent(syllabusId)){
			boolean updated = updateCalendarSettings(data);
			if(updated){
				getHibernateTemplate().merge(data);
			}
			if(data.getAttachments() != null && data.getAttachments().size() > 0){
		    	if(data.getCalendarEventIdStartDate() != null
		    			&& !"".equals(data.getCalendarEventIdStartDate())){
		    		addCalendarAttachments(data.getSyllabusItem().getContextId(), data.getCalendarEventIdStartDate(), new ArrayList(data.getAttachments()));
		    	}
		    	if(data.getCalendarEventIdEndDate() != null
		    			&& !"".equals(data.getCalendarEventIdEndDate())){
		    		addCalendarAttachments(data.getSyllabusItem().getContextId(), data.getCalendarEventIdEndDate(),  new ArrayList(data.getAttachments()));
		    	}
		    }
		}
	}
	
	public boolean removeCalendarEvents(SyllabusData data){
		boolean updated = false;
		//Remove Start Date Calendar Event
		if (data.getCalendarEventIdStartDate() != null) {
			  // first remove any existing calendar event
			  try {
				  String calendarDueDateEventId = data.getCalendarEventIdStartDate();
				  if (calendarDueDateEventId != null) {
					  removeCalendarEvent(data.getSyllabusItem().getContextId(),
							  calendarDueDateEventId);
				  }
			  } catch (Exception e) {
				  // user could have manually removed the calendar event
			  }
			  data.setCalendarEventIdStartDate(null);
			  updated = true;
		}
		//Remove End Date Calendar Event
		if (data.getCalendarEventIdEndDate() != null) {
			  // first remove any existing calendar event
			  try {
				  String calendarDueDateEventId = data.getCalendarEventIdEndDate();
				  if (calendarDueDateEventId != null) {
					  removeCalendarEvent(data.getSyllabusItem().getContextId(),
							  calendarDueDateEventId);
				  }
			  } catch (Exception e) {
				  // user could have manually removed the calendar event
			  }
			  data.setCalendarEventIdEndDate(null);
			  updated = true;
		}
		return updated;
	}
	
	private boolean updateCalendarSettings(SyllabusData data){
		boolean updated = false;
		//calendar check
		updated = removeCalendarEvents(data);
		
		if (data.getLinkCalendar()
				  && !SyllabusData.ITEM_DRAFT.equals(data.getStatus())
				  && (data.getSyllabusItem().getRedirectURL() == null
						  || data.getSyllabusItem().getRedirectURL().isEmpty())
				  && (data.getStartDate() != null || data.getEndDate() != null)) {
			// ok, let's post this to the calendar
			boolean changed = addCalendarEvent(data);
			if(changed){
				updated = true;
			}
		}
		return updated;
	}

/*  public SyllabusAttachment creatSyllabusAttachmentResource(String attachId, String name)
  {
    SyllabusAttachment attach = new SyllabusAttachmentImpl();
    
    attach.setAttachmentId(attachId);
    
    attach.setName(name);
    
    return attach;
  }*/

	public PreferencesService getPreferencesService() {
		return preferencesService;
	}

	public void setPreferencesService(PreferencesService preferencesService) {
		this.preferencesService = preferencesService;
	}

	public TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
}

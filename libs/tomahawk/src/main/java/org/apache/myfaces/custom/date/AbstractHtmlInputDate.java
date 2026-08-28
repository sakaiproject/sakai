/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.date;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.component.AlignProperty;
import org.apache.myfaces.component.ForceIdAware;
import org.apache.myfaces.component.UserRoleAware;
import org.apache.myfaces.component.UserRoleUtils;
import org.apache.myfaces.component.html.util.HtmlComponentUtils;
import org.apache.myfaces.custom.calendar.DateBusinessConverter;
import org.apache.myfaces.custom.calendar.DefaultDateBusinessConverter;

/**
 * Custom input control for dates and times. 
 * 
 * Unless otherwise specified, all attributes accept static values or EL expressions.
 * 
 * @JSFComponent
 *   name = "t:inputDate"
 *   class = "org.apache.myfaces.custom.date.HtmlInputDate"
 *   tagClass = "org.apache.myfaces.custom.date.HtmlInputDateTag"
 *   tagSuperclass = "org.apache.myfaces.custom.date.AbstractHtmlInputDateTag"
 *   tagHandler = "org.apache.myfaces.custom.date.HtmlInputDateTagHandler"
 *   
 * @since 1.1.7
 * @author Sylvain Vieujot (latest modification by $Author: lu4242 $)
 * @version $Revision: 1327313 $ $Date: 2012-04-17 17:38:42 -0500 (Tue, 17 Apr 2012) $
 */
public abstract class AbstractHtmlInputDate extends HtmlInputText 
    implements UserRoleAware, ForceIdAware, AlignProperty {
    public static final String COMPONENT_TYPE = "org.apache.myfaces.HtmlInputDate";
    public static final String COMPONENT_FAMILY = "jakarta.faces.Input";
    private static final String DEFAULT_RENDERER_TYPE = "org.apache.myfaces.Date";
    
    /**
     * Overriden to support the force id, since the parent is not an extended component 
     */
    public String getClientId(FacesContext context)
    {
        String clientId = HtmlComponentUtils.getClientId(this, getRenderer(context), context);
        if (clientId == null)
        {
            clientId = super.getClientId(context);
        }

        return clientId;
    }
        
    public boolean isRendered(){
        if (!UserRoleUtils.isVisibleOnUserRole(this)) return false;
        return super.isRendered();
    }
    
    public UserData getUserData(Locale currentLocale){
        return new UserData((Date) getDateBusinessConverter(this).getDateValue(getFacesContext(), this, getValue()), currentLocale, getTimeZone(), isAmpm(), getType());
    }
    
    private DateBusinessConverter getDateBusinessConverter(AbstractHtmlInputDate component)
    {
        DateBusinessConverter dateBusinessConverter = component.getDateBusinessConverter(); 
        if (dateBusinessConverter == null)
        {
            dateBusinessConverter = new DefaultDateBusinessConverter();
        }
        return dateBusinessConverter;
    }

    public static class UserData implements Serializable {
        private static final long serialVersionUID = -6507279524833267707L;
        private String day;
        private String month;
        private String year;
        private String hours;
        private String minutes;
        private String seconds;
        private TimeZone timeZone = null;
        private String ampm;
        private boolean uses_ampm;
        private String type;

        public UserData(Date date, Locale currentLocale, String _timeZone, boolean uses_ampm, String type){
            this.uses_ampm = uses_ampm;
            this.type = type;

            Calendar calendar = Calendar.getInstance(currentLocale);
            if (_timeZone != null) {
                timeZone = TimeZone.getTimeZone(_timeZone);
                calendar.setTimeZone(timeZone);
            }
            
            if(date == null)
                return;
          
            calendar.setTime( date );
            day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
            month = Integer.toString(calendar.get(Calendar.MONTH)+1);
            year = Integer.toString(calendar.get(Calendar.YEAR));
            if (uses_ampm) {
                int int_hours = calendar.get(Calendar.HOUR);
                // ampm hours must be in range 0-11 to be handled right; we have to handle "12" specially
                if (int_hours == 0) {
                    int_hours = 12;
                }
                hours = Integer.toString(int_hours);
                ampm = Integer.toString(calendar.get(Calendar.AM_PM));
            } else {
                hours = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
            }
            minutes = Integer.toString(calendar.get(Calendar.MINUTE));
            seconds = Integer.toString(calendar.get(Calendar.SECOND));
        }

        public Date parse() throws ParseException{
            Date retDate = null;
            Calendar tempCalendar=Calendar.getInstance();
            tempCalendar.setLenient(Boolean.FALSE.booleanValue());
            if (timeZone != null)
                   tempCalendar.setTimeZone(timeZone);
            try{
                if(!isSubmitValid(uses_ampm, type)) {
                    return null;
                }
                //There are this types: date | time | short_time | both | full
                if(! (type.equals( "time" ) || type.equals( "short_time" )) ) {
                    //Set day, month and year for type date, both, full
                    tempCalendar.set(Calendar.DAY_OF_MONTH,Integer.parseInt(day));
                    tempCalendar.set(Calendar.MONTH,Integer.parseInt(month)-1);
                    tempCalendar.set(Calendar.YEAR,Integer.parseInt(year));
                    
                    if( type.equals("date") ) {
                        //Reset hour, minute, second and milisecond to type date
                        
                        // According to Calendar javadoc: "... The HOUR_OF_DAY, HOUR 
                        // and AM_PM fields are handled independently and the the 
                        // resolution rule for the time of day is applied. 
                        // Clearing one of the fields doesn't reset the hour of day 
                        // value of this Calendar. Use set(Calendar.HOUR_OF_DAY, 0)
                        // to reset the hour value.  
                        tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
                        tempCalendar.set(Calendar.MINUTE, 0);
                        tempCalendar.set(Calendar.SECOND, 0);
                        tempCalendar.set(Calendar.MILLISECOND, 0);
                        
                        // Call to clear sets the given calendar field value and the 
                        // time value (millisecond offset from the Epoch) of this 
                        // Calendar undefined.
                        tempCalendar.clear(Calendar.HOUR);
                        tempCalendar.clear(Calendar.HOUR_OF_DAY);
                        tempCalendar.clear(Calendar.AM_PM);
                        tempCalendar.clear(Calendar.MINUTE);
                        tempCalendar.clear(Calendar.SECOND);
                        tempCalendar.clear(Calendar.MILLISECOND);

                        return new java.sql.Date(tempCalendar.getTimeInMillis());
                    }
                }

                if(! type.equals( "date" )) {
                    //Set hour, ampm, minute, second to
                    //type time, short_time, both, full
                    if (uses_ampm) {
                        int int_hours = Integer.parseInt(hours);
                        // ampm hours must be in range 0-11 to be handled right; we have to handle "12" specially
                        if (int_hours == 12) {
                            int_hours = 0;
                        }
                        tempCalendar.set(Calendar.HOUR,int_hours);
                        tempCalendar.set(Calendar.AM_PM,Integer.parseInt(ampm));
                    } else {
                        tempCalendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hours));
                    }
                    tempCalendar.set(Calendar.MINUTE,Integer.parseInt(minutes));
                    
                    if (seconds != null & (type.equals("full") || type.equals("time") || type.equals("short_time"))) {
                        tempCalendar.set(Calendar.SECOND,Integer.parseInt(seconds));
                    }
                    else
                    {
                        //Reset seconds for both type
                        tempCalendar.set(Calendar.SECOND,0);
                    }
                }
                tempCalendar.set(Calendar.MILLISECOND, 0);
                retDate = tempCalendar.getTime();
            } catch (NumberFormatException e) {
                throw new ParseException(e.getMessage(),0);
            } catch (IllegalArgumentException e) {
                throw new ParseException(e.getMessage(),0);
            } 
            return retDate;
        }

        private String formatedInt(String toFormat){
            if( toFormat == null )
                return null;

            int i = -1;
            try{
                i = Integer.parseInt( toFormat );
            }catch(NumberFormatException nfe){
                return toFormat;
            }
            if( i >= 0 && i < 10 )
                return "0"+i;
            return Integer.toString(i);
        }
        
        private boolean isDateSubmitted(boolean usesAmpm, String type) {
            boolean isDateSubmitted = ! (StringUtils.isEmpty(getDay()) && ((getMonth() == null) || getMonth().equals("-1")) && StringUtils.isEmpty(getYear()));
            if(usesAmpm)
                isDateSubmitted = isDateSubmitted || isAmpmSubmitted();
            return isDateSubmitted;
        }
        
        private boolean isTimeSubmitted(boolean usesAmpm, String type) {
            boolean isTimeSubmitted = ! (StringUtils.isEmpty(getHours()) && StringUtils.isEmpty(getMinutes()));
            if(type.equals("time") || type.equals("full"))
                isTimeSubmitted = isTimeSubmitted || ! StringUtils.isEmpty(getSeconds());
            if(usesAmpm)
                isTimeSubmitted = isTimeSubmitted || isAmpmSubmitted();
            return isTimeSubmitted;
        }
        
        private boolean isSubmitValid(boolean usesAmpm, String type) {
            if(type.equals("date"))
                return isDateSubmitted(usesAmpm, type);
            else if(type.equals("time") || (type.equals("short_time")))
                return isTimeSubmitted(usesAmpm, type);
            else if(type.equals("full") || type.equals("both"))
                return isDateSubmitted(usesAmpm, type) || isTimeSubmitted(usesAmpm, type);
            else
                return false;
        }
        
        private boolean isAmpmSubmitted() {
            if(getAmpm() == null)
                return false;
            else
                return ! getAmpm().equals("-1");
        }

        public String getDay() {
            return formatedInt( day );
        }
        public void setDay(String day) {
            this.day = day;
        }

        public String getMonth() {
            return month;
        }
        public void setMonth(String month) {
            this.month = month;
        }

        public String getYear() {
            return year;
        }
        public void setYear(String year) {
            this.year = year;
        }

        public String getHours() {
            return formatedInt( hours );
        }
        public void setHours(String hours) {
            this.hours = hours;
        }
        public String getMinutes() {
            return formatedInt( minutes );
        }
        public void setMinutes(String minutes) {
            this.minutes = minutes;
        }

        public String getSeconds() {
            return formatedInt( seconds );
        }
        public void setSeconds(String seconds) {
            this.seconds = seconds;
        }
        
        public String getAmpm() {
            return ampm;
        }
        public void setAmpm(String ampm) {
            this.ampm = ampm;
        }
        
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }

    /**
     * Indicate an object used as a bridge between the java.util.Date instance
     * used by this component internally and the value object used on the bean,
     * referred as a "business" value.
     * 
     * <ul>
     * <li>If the value is literal, look for the mentioned class instance, 
     * create a new instance and assign to the component property.</li>
     * <li>If it the value a EL Expression, set the expression to the 
     * component property.</li>
     * </ul> 
     * 
     * @JSFProperty stateHolder="true" inheritedTag="true"
     */
    public abstract DateBusinessConverter getDateBusinessConverter();
    
    public abstract void setDateBusinessConverter(DateBusinessConverter dateBusinessConverter);

    /**
     * @JSFProperty
     */
    public abstract String getTimeZone();
    
    /**
     * Specifies the type of value to be accepted. 
     * Valid values are: date | time | short_time | both | full
     * 
     * @JSFProperty
     *   defaultValue = "date"
     */
    public abstract String getType();
    
    /**
     *  If true, use 12hr times with AM/PM selector; if false, use 24hr time. Default false.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */
    public abstract boolean isAmpm();
    
    /**
     * @JSFProperty
     *   defaultValue = "false"
     */ 
    public abstract boolean isPopupCalendar();
        
    /**
     * Label to be used when displaying an empty month selection
     * 
     * @JSFProperty
     *   defaultValue = "\"\""
     */ 
    public abstract String getEmptyMonthSelection();
        
    /**
     * Label to be used when displaying an empty ampm selection
     * 
     * @JSFProperty
     *   defaultValue = "\"\""
     */    
    public abstract String getEmptyAmpmSelection();

    /**
     * HTML: When true, indicates that this component cannot be modified by the user. 
     * The element may receive focus unless it has also been disabled.
     * 
     * @JSFProperty
     */
    public abstract boolean isReadonly();

    /**
     * HTML: When true, this element cannot receive focus.
     * 
     * @JSFProperty
     *   defaultValue = "false"
     */    
    public abstract boolean isDisabled();

    /**
     * Retrieve the converter used by this component. 
     * <p>
     * If no converter is selected, submitted values are converted to 
     * its inner class UserData on decode method.
     * </p>
     * <p>
     * If some converter is used, submitted values are decoded as
     * a String with the following format:
     * </p>
     * <p></p>
     * <p>year=yyyy</p>
     * <p>month=mm</p>
     * <p>day=dd</p>
     * <p>hours=hh</p>
     * <p>minutes=mm</p>
     * <p>seconds=ss</p>
     * <p>ampm=ampm</p>
     * <p></p>
     * <p>
     * Note that submitted values could be wrong and it is necessary to
     * restore values on render response phase. The converter receive 
     * a string with this format on getAsObject method and it is expected
     * the converter encode it on getAsString method, so the renderer can
     * restore the submitted values correctly.
     * </p>
     * 
     * @JSFProperty
     */
    public Converter getConverter()
    {
        return super.getConverter();
    }
}

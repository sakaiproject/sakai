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

package org.apache.myfaces.custom.schedule.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeSet;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;

import org.apache.myfaces.custom.schedule.model.Interval;


/**
 * <p>
 * Some utility methods
 * </p>
 *
 * @author Jurgen Lust (latest modification by $Author: jlust $)
 * @author Kito Mann (some methods taken from the source code of his book 'JavaServer Faces in Action', which is also released under the Apache License
 * @author Bruno Aranda (adaptation of Jurgen's code to myfaces)
 * @version $Revision: 387334 $
 */
public class ScheduleUtil
{
    //~ Class Variables --------------------------------------------------------
    private final static String DATE_ID_PATTERN = "yyyyMMdd";
    
    //~ Constructors -----------------------------------------------------------
    /**
     * This class should not be instantiated, it only contains static methods
     */
    private ScheduleUtil() {
        super();
    }
    
    //~ Methods ----------------------------------------------------------------

    /**
     * <p>
     * Check if the value of a UIComponent is a value or method binding
     * expression.
     * </p>
     *
     * @param value the value to check
     *
     * @return whether the value is a binding expression
     */
    public static boolean isBindingExpression(Object value)
    {
        boolean returnboolean =
            ((value != null) && value instanceof String &&
            ((String) value).startsWith("#{") &&
            ((String) value).endsWith("}"));

        return returnboolean;
    }

    /**
     * <p>
     * Get the boolean value of a UIComponent, even if it is a value
     * binding expression.
     * </p>
     *
     * @param component the component
     * @param property the property
     * @param key the key of the value binding
     * @param defaultValue the default value
     *
     * @return the boolean value
     */
    public static boolean getBooleanProperty(
        UIComponent component,
        Boolean property,
        String key,
        boolean defaultValue
    )
    {
        if (property != null) {
            return property.booleanValue();
        } else {
            ValueBinding binding =
                (ValueBinding) component.getValueBinding(key);

            if (binding != null) {
                Boolean value =
                    (Boolean) binding.getValue(
                        FacesContext.getCurrentInstance()
                    );

                if (value != null) {
                    return value.booleanValue();
                }
            }
        }

        return defaultValue;
    }

    /**
     * <p>
     * Get the boolean value of an attribute
     * </p>
     *
     * @param attributeValue the attribute value
     * @param valueIfNull the default value
     *
     * @return the boolean value
     */
    public static boolean getBooleanValue(
        Object attributeValue,
        boolean valueIfNull
    )
    {
        if (attributeValue == null) {
            return valueIfNull;
        }

        if (attributeValue instanceof String) {
            return ((String) attributeValue).equalsIgnoreCase("true");
        } else {
            return ((Boolean) attributeValue).booleanValue();
        }
    }

    /**
     * <p>
     * Get the float value of a UIComponent, even if it is a value
     * binding expression.
     * </p>
     *
     * @param component the component
     * @param property the property
     * @param key the key of the value binding
     * @param defaultValue the default value
     *
     * @return the float value
     */
    public static float getFloatProperty(
        UIComponent component,
        Float property,
        String key,
        float defaultValue
    )
    {
        if (property != null) {
            return property.floatValue();
        } else {
            ValueBinding binding =
                (ValueBinding) component.getValueBinding(key);

            if (binding != null) {
                Float value =
                    (Float) binding.getValue(FacesContext.getCurrentInstance());

                if (value != null) {
                    return value.floatValue();
                }
            }
        }

        return defaultValue;
    }

    /**
     * <p>
     * Get the hashcode for the truncated date
     * </p>
     *
     * @param date the date
     *
     * @return the hashCode of the truncated date
     */
    public static int getHashCodeForDay(Date date,TimeZone tz)
    {
      Calendar calendar = getCalendarInstance(date, tz);

      return new Integer(calendar.get(Calendar.ERA)).hashCode() ^
        new Integer(calendar.get(Calendar.YEAR)).hashCode() ^
        new Integer(calendar.get(Calendar.DAY_OF_YEAR)).hashCode();
    }

    /**
     * <p>
     * Get the int value of a UIComponent, even if it is a value
     * binding expression.
     * </p>
     *
     * @param component the component
     * @param property the property
     * @param key the key of the value binding
     * @param defaultValue the default value
     *
     * @return the int value
     */
    public static int getIntegerProperty(
        UIComponent component,
        Integer property,
        String key,
        int defaultValue
    )
    {
        if (property != null) {
            return property.intValue();
        } else {
            ValueBinding binding =
                (ValueBinding) component.getValueBinding(key);

            if (binding != null) {
                Integer value =
                    (Integer) binding.getValue(
                        FacesContext.getCurrentInstance()
                    );

                if (value != null) {
                    return value.intValue();
                }
            }
        }

        return defaultValue;
    }
    
    /**
     * <p>
     * Get the object value of a UIComponent, even if it is a value
     * binding expression.
     * </p>
     *
     * @param component the component
     * @param property the property
     * @param key the key of the value binding
     * @param defaultValue the default value
     *
     * @return the object value
     */
    public static Object getObjectProperty(
        UIComponent component,
        Object property,
        String key,
        Object defaultValue
    )
    {
        if (property != null) {
            return property;
        } else {
            ValueBinding binding =
                (ValueBinding) component.getValueBinding(key);

            if (binding != null) {
                return binding.getValue(FacesContext.getCurrentInstance());
            }
        }

        return defaultValue;
    }

    /**
     * <p>
     * Check if the 2 dates are in the same day.
     * </p>
     *
     * @param date1 the first date
     * @param date2 the second date
     *
     * @return whether the dates are in the same day
     */
    public static boolean isSameDay(
        Date date1,
        Date date2,
        TimeZone tz
    )
    {
        if ((date1 == null) || (date2 == null)) {
            return false;
        }

        Calendar calendar1 = getCalendarInstance(date1, tz);
        Calendar calendar2 = getCalendarInstance(date2, tz);

        return (calendar1.get(Calendar.ERA) == calendar2.get(Calendar.ERA) &&
            calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * <p>
     * Check if the 2 dates are at the same time of day.
     * </p>
     *
     * @param date1 the first date
     * @param date2 the second date
     *
     * @return whether the dates are at the same time of day
     */
    public static boolean isSameTime(Date date1, Date date2)
    {
        if ((date1 == null) || (date2 == null))
        {
            return false;
        }

        Calendar calendar1 = getCalendarInstance(date1, null);
        Calendar calendar2 = getCalendarInstance(date2, null);

        return (calendar1.get(Calendar.HOUR_OF_DAY) == calendar2.get(Calendar.HOUR_OF_DAY) &&
                calendar1.get(Calendar.MINUTE) == calendar2.get(Calendar.MINUTE) &&
                calendar1.get(Calendar.SECOND) == calendar2.get(Calendar.SECOND));
    }


    /**
     * <p>
     * Check if two sets of intervals are equivalent.
     * Intervals are equivalent if their label is the same and they begin and end
     * at the same time of day.
     * </p>
     *
     * @param intervals1 the first set of intervals
     * @param intervals2 the second set of intervals
     *
     * @return whether the dates are at the same time of day
     */
    public static boolean areEquivalentIntervals(TreeSet intervals1, TreeSet intervals2)
    {
        if (intervals1 == null || intervals2 == null)
        {
            return !(intervals1 != null || intervals2 != null);
        }
        
        if (intervals1.size() == intervals2.size())
        {
            Iterator it1 = intervals1.iterator();
            Iterator it2 = intervals2.iterator();
            
            while (it1.hasNext())
            {
                if (!((Interval) it1.next()).isEquivalent(((Interval) it2.next()))) {
                    return false;
                }
            }
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * <p>
     * Get the String value of a UIComponent, even if it is a value
     * binding expression.
     * </p>
     *
     * @param component the component
     * @param property the property
     * @param key the key of the value binding
     * @param defaultValue the default value
     *
     * @return the String value
     */
    public static String getStringProperty(
        UIComponent component,
        String property,
        String key,
        String defaultValue
    )
    {
        if (property != null) {
            return property;
        } else {
            ValueBinding binding =
                (ValueBinding) component.getValueBinding(key);

            if (binding != null) {
                return (String) binding.getValue(
                    FacesContext.getCurrentInstance()
                );
            }
        }

        return defaultValue;
    }

    /**
     * <p>
     * Check if the value of the given component can be modified
     * </p>
     *
     * @param component the component
     *
     * @return whether the value can be modified
     */
    public static boolean canModifyValue(UIComponent component)
    {
        boolean returnboolean =
            (component.isRendered() &&
            !getBooleanValue(component.getAttributes().get("readonly"), false) &&
            !getBooleanValue(component.getAttributes().get("disabled"), false));

        return returnboolean;
    }

    /**
     * <p>
     * Compare 2 dates after truncating them.
     * </p>
     *
     * @param date1 the first date
     * @param date2 the second date
     *
     * @return the comparison
     */
    public static int compareDays(
        Date date1,
        Date date2,
        TimeZone tz
    )
    {
        if (date1 == null) {
            return -1;
        }

        if (date2 == null) {
            return 1;
        }

        return truncate(date1,tz).compareTo(truncate(date2,tz));
    }

    /**
     * truncate the given Date to 00:00:00 that same day
     *
     * @param date the date that should be truncated
     * @return the truncated date
     */
    public static Date truncate(Date date, TimeZone tz)
    {
        if (date == null) return null;
        Calendar cal = getCalendarInstance(date, tz);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    /**
     * get a String identifying this date
     * 
     * @param date the date
     * @return the identifier for this date
     */
    public static String getDateId(Date date, TimeZone timeZone)
    {
        if (date == null) return "NULL";
        return getDateIdFormater(timeZone).format(date);
    }
    
    /**
     * get a date from the given date ID
     * 
     * @param id the date ID
     * @return the date
     */
    public static Date getDateFromId(String id, TimeZone timeZone)
    {
        if (id == null || id.length() == 0 || "null".equals(id)) return null;
        try
        {
            return getDateIdFormater(timeZone).parse(id);
        }
        catch (ParseException e)
        {
            return null;
        }
    }
    
    public static Calendar getCalendarInstance(Date date, TimeZone timeZone)
    {
        Calendar cal = GregorianCalendar.getInstance(
                timeZone != null ? timeZone : TimeZone.getDefault());
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setTime(date);

        return cal;
    }
    
    private static SimpleDateFormat getDateIdFormater(TimeZone timeZone)
    {
        SimpleDateFormat format = new SimpleDateFormat(DATE_ID_PATTERN);
        
        format.setTimeZone(timeZone);
        
        return format;
    }
}
//The End

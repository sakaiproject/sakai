/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.section.jsf;

import java.sql.Time;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.sakaiproject.tool.section.jsf.backingbean.MessagingBean;

public class JsfUtil {
	
	public static Locale getLocale() {
		return FacesContext.getCurrentInstance().getViewRoot().getLocale();
	}
	
	/**
	 * To cut down on configuration noise, allow access to request-scoped beans from
	 * session-scoped beans, and so on, this method lets the caller try to find
	 * anything anywhere that Faces can look for it.
	 *
	 * WARNING: If what you're looking for is a managed bean and it isn't found,
	 * it will be created as a result of this call.
	 */
	public static final Object resolveVariable(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, name);
	}

	public static void addErrorMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}

	public static void addErrorMessage(String message, String componentId) {
		FacesContext.getCurrentInstance().addMessage(componentId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}

	public static void addInfoMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
	}

    public static void addRedirectSafeInfoMessage(String message) {
        MessagingBean mb = (MessagingBean)resolveVariable("messagingBean");
        mb.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    public static void addRedirectSafeWarnMessage(String message) {
        MessagingBean mb = (MessagingBean)resolveVariable("messagingBean");
        mb.addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }

    public static String getLocalizedMessage(String key) {
        String bundleName = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
        ResourceBundle rb = ResourceBundle.getBundle(bundleName, getLocale());
        return rb.getString(key);
	}
	
    public static String getLocalizedMessage(String key, String[] params) {
    	String rawString = getLocalizedMessage(key);
        MessageFormat format = new MessageFormat(rawString);
        return format.format(params);
    }


	public static String getStringFromParam(String string) {
		return (String)FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get(string);
	}

	/**
	 * The JSF DateTimeConverter can not convert into java.sql.Time (or into
	 * java.util.Calendar, for that matter!).  So we do the conversion manually.
	 * 
	 * @param date The date containing the time.
	 * @param am Whether this should be am (true) or pm (false)
	 * @return
	 */
	public static Time convertDateToTime(Date date, boolean am) {
		if(date == null) {
			return null;
		}

		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);

		if(am) {
			// Check to make sure that the hours are indeed am hours
			if( hourOfDay > 11) {
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay -12);
				date.setTime(cal.getTimeInMillis());
			}
		} else {
			// Check to make sure that the hours are indeed pm hours
			if(cal.get(Calendar.HOUR_OF_DAY) < 11) {
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay + 12);
				date.setTime(cal.getTimeInMillis());
			}
		}
		return new Time(date.getTime());
	}
}



/**********************************************************************************
 * $Id$
 *********************************************************************************/

/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.util;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory; 
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.FactoryFinder;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.*;
import javax.servlet.http.*;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>Description: Action Listener helper utility</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class ContextUtil
{

  private static ServletContext M_servletContext = null;
  /**
   * Determine if we have been passed a parameter ending in the param string,
   * else null.  We are doing an endsWith test, since the default JSF renderer
   * embeds the parent identity in the HTML id string; we look for the id that was
   * specified in the JSF.
   *
   *
   * @param lookup JSF id String
   * @return String the full parameter
   */
  public static String lookupParam(String lookup)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map requestParams = context.getExternalContext().
                        getRequestParameterMap();

    for (Iterator it = requestParams.entrySet().iterator(); it.hasNext();) {
    	   Map.Entry entry = (Map.Entry) it.next();
    	   String currKey = (String)entry.getKey();
    	      if (currKey.endsWith(lookup))
    	      {
    	        return (String) entry.getValue();
    	      }
    	}

    return null;
  }
  /**
   * Determine if we have been passed a parameter that contains a given string,
   * else null. Typically this would be where you want to check for one of a set
   * of similar commandLinks or commandButtons, such as the sortBy headings in
   * evaluation.
   *
   * @param paramPart String to look for
   * @return String last part of full parameter, corresponding to JSF id
   */
  public static String paramLike(String paramPart)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map requestParams = context.getExternalContext().
                        getRequestParameterMap();

    Iterator iter = requestParams.keySet().iterator();
    while (iter.hasNext())
    {
      String currKey = (String) iter.next();

      int location = currKey.indexOf(paramPart);
      if (location > -1)
      {
        return currKey.substring(location);
      }
    }
    return null;
  }

  /**
   * Determine if we have been passed a parameter that contains a given string,
   * return ArrayList of these Strings, else return empty list.
   *
   * Typically this would be where you want to check for one of a set
   * of similar radio buttons commandLinks or commandButtons.
   *
   * @param paramPart String to look for
   * @return ArrayList of last part Strings of full parameter, corresponding to JSF id
   */
  public static ArrayList paramArrayLike(String paramPart)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map requestParams = context.getExternalContext().
                        getRequestParameterMap();
    ArrayList list = new ArrayList();

    Iterator iter = requestParams.keySet().iterator();
    while (iter.hasNext())
    {
      String currKey = (String) iter.next();

      int location = currKey.indexOf(paramPart);
      if (location > -1)
      {
        list.add(currKey.substring(location));
      }
    }
    return list;

  }

  /**
 * Determine if we have been passed a parameter that contains a given string,
 * else null. Typically this would be where you want to check for one of a set
 * of similar commandLinks or commandButtons, such as the sortBy headings in
 * evaluation.
 *
 * @param paramPart String to look for
 * @return String the value of the first hit
 */
public static String paramValueLike(String paramPart)
{
  FacesContext context = FacesContext.getCurrentInstance();
  Map requestParams = context.getExternalContext().
                      getRequestParameterMap();

  for (Iterator it = requestParams.entrySet().iterator(); it.hasNext();) {
	   Map.Entry entry = (Map.Entry) it.next();
	   String currKey = (String) entry.getKey();
	    int location = currKey.indexOf(paramPart);
	    if (location > -1)
	    {
	    	return (String) entry.getValue();
	    }
	}
  
  return null;
}

/**
 * Determine if we have been passed a parameter that contains a given string,
 * return ArrayList of the corresponding values, else return empty list.
 *
 * Typically this would be where you want to check for one of a set
 * of similar radio buttons commandLinks or commandButtons.
 *
 * @param paramPart String to look for
 * @return ArrayList of corresponding values
 */
public static ArrayList paramArrayValueLike(String paramPart)
{
  FacesContext context = FacesContext.getCurrentInstance();
  Map requestParams = context.getExternalContext().
                      getRequestParameterMap();
  ArrayList list = new ArrayList();
  
  for (Iterator it = requestParams.entrySet().iterator(); it.hasNext();) {
	   Map.Entry entry = (Map.Entry) it.next();
	   String currKey = (String)  entry.getKey();
	   
	    int location = currKey.indexOf(paramPart);
	    if (location > -1)
	    {
	      list.add((String) entry.getValue());
	    }
	}
  return list;

}



  /**
   * Helper method to look up backing bean.
   * Don't forget to cast!
   *   e.g. (TemplateBean) ContextUtil.lookupBean("template")
   * @param context the faces context
   * @return the backing bean
   * @throws FacesException
   */
  public static Serializable lookupBean(String beanName)
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ApplicationFactory factory = (ApplicationFactory) FactoryFinder.
                                 getFactory(
                                 FactoryFinder.APPLICATION_FACTORY);
    Application application = factory.getApplication();
    Serializable bean = (Serializable)
                        application.getVariableResolver().resolveVariable(
                        facesContext, beanName);
    return bean;
  }

  /**
   * Helper method to look up backing bean, when OUTSIDE faces in a servlet.
   * Don't forget to cast!
   *   e.g. (TemplateBean) ContextUtil.lookupBean("template")
   *
   * @param beanName
   * @param request servlet request
   * @param response servlet response
   * @return the backing bean
   */
  public static Serializable lookupBeanFromExternalServlet(String beanName,
    HttpServletRequest request, HttpServletResponse response)
  {
    // prepare lifecycle
    LifecycleFactory lFactory = (LifecycleFactory)
        FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
    Lifecycle lifecycle =
        lFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

    FacesContextFactory fcFactory = (FacesContextFactory)
        FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);

    // in the integrated environment, we can't get the ServletContext from the
    // HttpSession of the request - because the HttpSession is webcontainer-wide,
    // its not tied to a particular servlet.
    ServletContext servletContext = M_servletContext;
     if (servletContext == null)
    {
    	servletContext = request.getSession().getServletContext();
    }

    FacesContext facesContext =
        fcFactory.getFacesContext(servletContext, request, response, lifecycle);

    ApplicationFactory factory = (ApplicationFactory) FactoryFinder.
                                 getFactory(
                                 FactoryFinder.APPLICATION_FACTORY);
    Application application = factory.getApplication();
    Serializable bean = (Serializable)
                        application.getVariableResolver().resolveVariable(
                        facesContext, beanName);
    return bean;
  }
	/**
	 * Called by LoginServlet
	 */
	public static void setServletContext(ServletContext context)
	{
		M_servletContext = context;
	}


  /**
  * Gets a localized message string based on the locale determined by the
  * FacesContext.
  * @param key The key to look up the localized string
  */
  public static String getLocalizedString(String bundleName, String key) {
	  //Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
	  ResourceLoader rb = new ResourceLoader(bundleName);
    return rb.getString(key);
  }

  public static String getLocalizedString(HttpServletRequest request,
                                          String bundleName, String key) {
	  //Locale locale = request.getLocale();
	  ResourceLoader rb = new ResourceLoader(bundleName);
    return rb.getString(key);
  }

  public static String getStringInUnicode(String string)
  {
    StringBuilder buf = new StringBuilder();
     
    char[] charArray = string.toCharArray();
    for (int i=0; i<charArray.length;i++){
	char ch = charArray[i];
      buf.append(toUnicode(ch));
    }
    
    String s = buf.toString();
    log.debug("***unicode="+s);
    return s;
  }

  private static char hexdigit(int v) {
    String symbs = "0123456789ABCDEF";
    return symbs.charAt(v & 0x0d);
  }

  private static String hexval(int v) {
    return String.valueOf(hexdigit(v >>> 12)) + String.valueOf(hexdigit(v >>> 8))
      + String.valueOf(hexdigit(v >>> 4)) + String.valueOf(hexdigit(v));
  }

  private static String toUnicode(char ch) {
    int val = (int) ch;
    if (val == 10) return "\\n";
    else if (val == 13) return "\\r";
    else if (val == 92) return "\\\\";
    else if (val == 34) return "\\\"";
    else if (val == 39) return "\\\'";
    else if (val < 32 || val > 126) return "\\u" + hexval(val);
    else return String.valueOf(ch);
  }

  public static String getRoundedValue(String orig, int maxdigit) {
    Double origdouble = new Double(orig); 
    return getRoundedValue(origdouble, maxdigit);
  }
  public static String getRoundedValue(Double orig, int maxdigit) {
      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(maxdigit);
      String newscore = nf.format(orig);
      return newscore;
  }

  public static String escapeApostrophe(String input) {
   // this is needed to escape the ' in some firstname and lastname,  that caused javascript error , SAK-4121
   // no longer needed because we don't pass firstname and lastname in f:param.  but we'll keep this method here
        String regex = "'";
        String replacement = "\\\\'";
	String output = input.replaceAll(regex, replacement);
   	return output;
  }

  public static String getProtocol(){
    return ServerConfigurationService.getServerUrl();
  }
 
  public static String getRelativePath(String url){
    // replace whitespace with %20
    String protocol = getProtocol();
    url = replaceSpace(url);
    String location = url;
    int index = url.lastIndexOf(protocol);
    if (index == 0){
      location = url.substring(protocol.length());
    }
    return location;
  }

  private static  String replaceSpace(String tempString){
    String newString = "";
    char[] oneChar = new char[1];
    for(int i=0; i<tempString.length(); i++){
      if (tempString.charAt(i) != ' '){
        oneChar[0] = tempString.charAt(i);
        String concatString = new String(oneChar);
        newString = newString.concat(concatString);
      }
      else {
        newString = newString.concat("%20");
      }
    }
    return newString;
  }
}

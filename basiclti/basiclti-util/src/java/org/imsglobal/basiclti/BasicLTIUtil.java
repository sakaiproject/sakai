/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2008 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. 
 *
 **********************************************************************************/
package org.imsglobal.basiclti;

import java.util.Locale;
import java.util.UUID;
import java.util.Date;
import java.util.TimeZone;
import java.util.Properties;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.util.Map;
import java.util.List;

import org.imsglobal.basiclti.XMLMap;
import org.imsglobal.basiclti.Base64;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthAccessor;
import net.oauth.signature.OAuthSignatureMethod;

/* Leave out until we have JTidy 0.8 in the repository 
import org.w3c.tidy.Tidy;
import java.io.ByteArrayOutputStream;
*/

/**
 * Some Utility code for IMS Basic LTI
 * http://www.anyexample.com/programming/java/java_simple_class_to_compute_sha_1_hash.xml
 */
public class BasicLTIUtil {

    /** To turn on really verbose debugging */
    private static boolean verbosePrint = false;

    // Simple Debug Print Mechanism
    public static void dPrint(String str)
    {
        if ( verbosePrint ) System.out.println(str);
    }

    private static void setErrorMessage(Properties retProp, String message)
    {
        retProp.setProperty("message",message);
        retProp.setProperty("status","fail");
    }

    public static boolean validateDescriptor(String descriptor)
    {
        Map<String,Object> tm = XMLMap.getFullMap(descriptor.trim());

        if ( tm == null )
        {
                return false;
        }

        // We demand at least an endpoint
        String ltiLaunch = XMLMap.getString(tm,"/basicltiresource/launch_url");
        String ltiSecureLaunch = XMLMap.getString(tm,"/basicltiresource/secure_launch_url");
        if ( ( ltiLaunch == null || ltiLaunch.trim().length() < 1 ) &&
             ( ltiSecureLaunch == null || ltiSecureLaunch.trim().length() < 1 ) )
        {
                return false;
        }
        return true;
    }

    // Remove any properties which we wil not send
    public static Properties cleanupProperties(Properties newMap) {
        Properties newProp = new Properties();
        for(Object okey : newMap.keySet() )
        {
                if ( ! (okey instanceof String) ) continue;
                String key = (String) okey;
                if ( key == null ) continue;
                String value = newMap.getProperty(key);
                if ( value == null ) continue;
                if ( key.startsWith("internal_") ) continue;
                if ( key.startsWith("_") ) continue;
                if ( "action".equalsIgnoreCase(key) ) continue;
                if ( "launchurl".equalsIgnoreCase(key) ) continue;
                if ( value.equals("") ) continue;
                newProp.setProperty(key, value);
         }
         return newProp;
    }

    // Add the necessary fields and sign
    public static Properties signProperties(Properties postProp, String method, String url, 
        String oauth_callback, String oauth_consumer_key, String oauth_consumer_secret)
    {
        postProp = BasicLTIUtil.cleanupProperties(postProp);
        postProp.setProperty("lti_version","basiclti-1.0");
        postProp.setProperty("basiclti_submit","Continue");
        if ( postProp.getProperty("oauth_callback") == null ) postProp.setProperty("oauth_callback","about:blank");

        OAuthMessage oam = new OAuthMessage(method, url,postProp.entrySet());
        OAuthConsumer cons = new OAuthConsumer(oauth_callback, 
            oauth_consumer_key, oauth_consumer_secret, null);
        OAuthAccessor acc = new OAuthAccessor(cons);
        System.out.println("OAM="+oam+"\n");
        try {
            // System.out.println("BM="+OAuthSignatureMethod.getBaseString(oam)+"\n");
            oam.addRequiredParameters(acc);
            System.out.println("BM2="+OAuthSignatureMethod.getBaseString(oam)+"\n");
            // System.out.println("OAM="+oam+"\n");

            List<Map.Entry<String, String>> params = oam.getParameters();
    
            Properties nextProp = new Properties();
            // Convert to Properties
            for (Map.Entry<String,String> e : params) {
                // System.out.println("value= " + e);
                nextProp.setProperty(e.getKey(), e.getValue());
            }
	    return nextProp;
        } catch (net.oauth.OAuthException e) {
            System.out.println("BasicLTIUtil.signProperties OAuth Exception "+e.getMessage());
            return null;
        } catch (java.io.IOException e) {
            System.out.println("BasicLTIUtil.signProperties IO Exception "+e.getMessage());
            return null;
        } catch (java.net.URISyntaxException e) {
            System.out.println("BasicLTIUtil.signProperties URI Syntax Exception "+e.getMessage());
            return null;
        }
    
    }

    // Create the HTML to render a POST form and then automatically submit it
    // Make sure to call cleanupProperties before signing
    public static String postLaunchHTML(Properties newMap, String launchurl, boolean debug) {
        if ( launchurl == null ) return null;
        StringBuffer text = new StringBuffer();
        text.append("<div id=\"ltiLaunchFormSubmitArea\">\n");
        text.append("<form action=\""+launchurl+"\" name=\"ltiLaunchForm\" method=\"post\">\n" );
        for(Object okey : newMap.keySet() )
        {
                if ( ! (okey instanceof String) ) continue;
                String key = (String) okey;
                if ( key == null ) continue;
                String value = newMap.getProperty(key);
                if ( value == null ) continue;
		// This will escape the contents pretty much - at least 
		// we will be safe and not generate dangerous HTML
                key = encodeFormText(key);
                value = encodeFormText(value);
                if ( key.equals("basiclti_submit") ) {
                  text.append("<input type=\"submit\" size=\"40\" name=\"");
                } else { 
                  text.append("<input type=\"hidden\" size=\"40\" name=\"");
                }
                text.append(key);
                text.append("\" value=\"");
                text.append(value);
                text.append("\"/>\n");
        }
        text.append("</form>\n" + 
                "</div>\n");
        if ( ! debug ) 
        {
            text.append(
                    " <script language=\"javascript\"> \n" +
		    "    document.getElementById(\"ltiLaunchFormSubmitArea\").style.display = \"none\";\n" +
                    "    document.ltiLaunchForm.submit(); \n" +
                    " </script> \n");
	}

        String htmltext = text.toString();
	return htmltext;
    }

    public static boolean launchInfo(Properties info, Properties launch, String descriptor)
    {
        Map<String,Object> tm = null;
        try
        {
                tm = XMLMap.getFullMap(descriptor.trim());
        } 
        catch (Exception e) {
                System.out.println("BasicLTIUtil exception parsing BasicLTI descriptor"+e.getMessage());
		e.printStackTrace();
		return false;
        }
        if ( tm == null ) {
            System.out.println("Unable to parse XML in launchInfo");
            return false;
        }

        boolean retVal = false;

        String launch_url = toNull(XMLMap.getString(tm,"/basicltiresource/launch_url"));
        String secure_launch_url = toNull(XMLMap.getString(tm,"/basicltiresource/secure_launch_url"));
        if ( launch_url == null && secure_launch_url == null ) return false;

        setProperty(info, "launch_url", launch_url);
        setProperty(info, "secure_launch_url", secure_launch_url);

        List<Map<String,Object>> theList = XMLMap.getList(tm, "/basicltiresource/custom/parameter");
        for ( Map<String,Object> setting : theList) {
                dPrint("Setting="+setting);
                String key = XMLMap.getString(setting,"/!key"); // Get the key atribute
                String value = XMLMap.getString(setting,"/"); // Get the value
                if ( key == null || value == null ) continue;
                key = "custom_" + mapKeyName(key);
                dPrint("key="+key+" val="+value);
		launch.setProperty(key,value);
        }
        return true;
    }

    /*
        The parameter name is mapped to lower case and any character that 
         is neither a number or letter is replaced with an "underscore".  
         So if a custom entry was as follows:

         <parameter name="Vendor:Chapter">1.2.56</parameter>

         Would map to: 
           custom_vendor_chapter=1.2.56
    */

    public static String mapKeyName(String keyname)
    {
       StringBuffer sb = new StringBuffer();
       if ( keyname == null ) return null;
       keyname = keyname.trim();
       if ( keyname.length() < 1 ) return null;
       for(int i=0; i < keyname.length(); i++) {
           Character ch = Character.toLowerCase(keyname.charAt(i));
           if ( Character.isLetter(ch) || Character.isDigit(ch) ) {
               sb.append(ch);
           } else {
               sb.append('_');
	   }
       }
       return sb.toString();
    }

    public static String toNull(String str)
    {
       if ( str == null ) return null;
       if ( str.trim().length() < 1 ) return null;
       return str;
    }

    public static void setProperty(Properties props, String key, String value)
    {
        if ( value == null ) return;
        if ( value.trim().length() < 1 ) return;
        props.setProperty(key, value);
    }

    // Basic utility to encode form text - handle the "safe cases"
    public static String encodeFormText(String input)
    {
	String retval = input.replace("&", "&amp;");
	retval = retval.replace("\"", "&quot;");
	retval = retval.replace("<", "&lt;");
	retval = retval.replace(">", "&gt;");
	retval = retval.replace(">", "&gt;");
	retval = retval.replace("=", "&#61;");
	return retval;
    }

}

/* Sample Descriptor 

<?xml version="1.0" encoding="UTF-8"?>
<basicltiresource xmlns="http://www.imsglobal.org/services/cc/imsblti_v1p0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <title>generated by tp+user</title>
  <description>generated by tp+user</description>
  <custom>
    <parameter key=”keyname”>value</parameter>
  </custom>
  <extensions platform=”www.lms.com”>
    <parameter name=”keyname”>value</parameter>
  </extensions>
  <launch_url>url to the basiclti launch URL</launch_url>
  <secure_launch_url>url to the basiclti launch URL</secure_launch_url>
  <icon>url to an icon for this tool (optional)</icon>
  <secure_icon>url to an icon for this tool (optional)</secure_icon>
  <catrtidge_icon identifieref="BLTI001_Icon" />
  	  <vendor>
		  <code>vendor.com</code>
         <version>4.32</version>
         <name>Pearson Education</name>
         <description>
           This is a Gradebook that supports many column types.
         </description>
         <contact>
            <email>support@vendor.com</email>
         </contact>
         <url>http://www.vendor.com/product</url>
	  </vendor>
</basicltiresource>

*/

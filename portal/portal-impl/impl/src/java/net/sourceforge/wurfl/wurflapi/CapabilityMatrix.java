/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/CapabilityMatrix.java,v 1.3 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

import java.util.*;

/**
 * @author <b>Luca Passani</b>
 * <br><br>
 * Big matrix of (device,capability) pairs. The matrix is derived from WURFL.
 * Conceptually, the capability matrix can be seen as the value of all 
 * capabilities for all devices.<br>
 * The actual implementation does not build the whole matrix, though.<br>
 * A real huge matrix would be too expensive in terms of memory and time (hundreds of
 * thousands of elements), so the CapabilityMatrix is really just a cache of
 * the most commonly used (device/capability) pairs. This makes a lot of sense since:
 * <ol>
 *  <li>most probably people don't do MMS, WAP, ringtones, wap-push, J2me development
 *   all at the same time</li>
 *  <li>given a country, 80% or more of the device in the WURFL are not relevant
 *      for that country</li>
 * </ol>
 * The implication of this is that the average developer will need under 2% of
 * the capability/device info in the WURFL, so there is no reason 
 * to squander memory and time.
 *
 * The CapabilityMatrix also offers some utility methods that can be
 * handy at times: <code>isCapabilityIn()</code>, <code>isDescendentOf()</code>
 * and  <code>isCapabilityDefinedInDevice()</code>.
 * 
 */

public class CapabilityMatrix {

    private Map ht = Collections.synchronizedMap(new HashMap(2053, 0.75f));
    //private HashMap ht = new HashMap(2053, 0.75f);
    private Map descendentList = Collections.synchronizedMap(new HashMap());

    private Wurfl wu = null;
   
   CapabilityMatrix(Wurfl _wu) {
   
      wu = _wu;
      ArrayList al = wu.getListOfCapabilities();
      String[] capas = (String[]) al.toArray(new String[0]);  
       
      //let's initialize the matrix with the generic
          for(int j = 0; j < capas.length;j++) {             
      String capa = capas[j];
      ht.put("generic"+capa,wu.getCapabilityValueForDeviceAndCapability("generic",capa));
      }            
   }
   
   /** 
    * Given a capability name and a device, return the value of the
    * capability for that device.
    *
    */

   public String getCapabilityForDevice(String devID, String capa) {
      
      Object obj = ht.get(devID+capa);
      if (obj != null) { //value is in the 'cache'
         return (String)obj;
         
      } else {  //let's find it, return it and put it in cache
          String capaValue = wu.getCapabilityValueForDeviceAndCapability(devID,capa);
        if (capaValue.equals("")) { //bogus capability look-up
           return "";
        } else {
           ht.put(devID+capa,capaValue); //cache for next time
           return capaValue;
        }
      }
      
   }

   /** 
    * Given a capability name, check if the capability is
    * one of the capabilities available in the WURFL
    *
    */

   public boolean isCapabilityIn(String capa) {
       return wu.isCapabilityIn(capa);
   }



   /** 
    * Given two device IDs (descendent and ancestor), check if 
    * the former falls back into the latter or not.<br/>
    * This featture has been added because requested,
    * but it is usually better not to refer to device IDs explicitly
    * in the code. Use the patch file to identify devices with a common root.
    */

   public  boolean isDescendentOf(String descendent, String ancestor) {
      Object obj = descendentList.get(descendent+ancestor);
      if (obj != null) { //value is in the 'cache'
         return ((Boolean)obj).booleanValue();
         
      } else {  //let's find it, return it and put it in cache
          boolean queryResult = wu.isDescendentOf(descendent,ancestor) ;
	  descendentList.put(descendent+ancestor,new Boolean(queryResult));
	  return queryResult;
      }
      
   }



   /** 
    * Given a device ID and a capability name, returns
    *  
    *    false -&gt; if the value of the capability is derived by following<br>
    *                the fallback for the device<br>
    *    true  -&gt; if the value of the capability is defined in the device.
    *
    * This API is probably only useful if you are building an utility to browse
    * the WURFL.
    */
    public boolean isCapabilityDefinedInDevice(String devID,String capaName) {
        //this is an utility. No caching
	return wu.isCapabilityDefinedInDevice(devID,capaName);
    }
}

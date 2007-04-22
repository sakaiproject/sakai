/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/UAManager.java,v 1.2 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

import java.util.*;


/**
 * @author <b>Luca Passani</b>, passani at eunet dot no
 * <br><br>
 *
 * Similarly to CapabilityMatrix, this object screens the rest of the
 * app from WURFL parsing and XOM.
 * UAManager implements a caching mechanism to minimize 
 * expensive analisys of UA strings to identify the device ID
 * <br><br>
 * There are actually two separate chaches.
 * One for storing queries for strict matching and one for 
 * queries for loose matching (the latter are
 * heavy to compute, so one definitely wants to cache that)
 * 
 */
public class UAManager {
   
   private Map ht_strict = Collections.synchronizedMap(new HashMap(2053, 0.75f));
   private Map ht_loose = Collections.synchronizedMap(new HashMap(2053, 0.75f));
   private Wurfl wu = null;
   
   UAManager(Wurfl _wu) {
        wu = _wu;
   }   


/**
 * Given the UA of a device, find the device ID of the
 * associated device. Match must be perfect.
 */
   
   public String getDeviceIDFromUA(String ua) {
       
      Object obj = ht_strict.get(ua);
        if (obj != null) {         //UA already cached?
           return (String)obj;         
        } else {  //let's find it, return it and put it in cache
           String devID = wu.getDeviceIDFromUA(ua);
           ht_strict.put(ua,devID); //cache for next time
           return devID;
        }
    }   
    
/**
 * Given the UA of a device, find the device ID of the
 * associated device. Match may be loose, meaning that 
 * <code>MOT-T720/G_05.01.43R</code> will match
 * <code>MOT-T720/05.08.41R MIB/2.0 Profile/MIDP-1.0 Configuration/CLDC-1.0</code>.
 * (assuming no better match is found).
 * Observe that this is more powerful than a simple substring match.
 * Matching UAs loosely is an expensive operation, but UAManager
 * implements a cache that helps a lot.
 */   
   
    public String getDeviceIDFromUALoose(String ua) {
       
      Object obj = ht_loose.get(ua);
      if (obj != null) {         //UA already cached?
           return (String)obj;
         
      } else {  //let's find it, return it and put it in cache
           String devID = wu.getDeviceIDFromUALoose(ua);
           ht_loose.put(ua,devID); //cache for next time
           return devID;
      }
    }
} 
   

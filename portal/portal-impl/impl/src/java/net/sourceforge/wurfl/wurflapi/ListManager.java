/*
 ***************************************************************************
 * Copyright 2003-2005 Luca Passani, passani at eunet.no                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************
 *   $Author$
 *   $Header: /cvsroot/wurfl/tools/java/wurflapi-xom/antbuild/src/net/sourceforge/wurfl/wurflapi/ListManager.java,v 1.3 2005/02/13 15:11:39 passani Exp $
 */

package net.sourceforge.wurfl.wurflapi;

import java.util.*;
import nu.xom.*; //needed because of dependency on nu.xom.Element

/**
 * @author <b>Luca Passani</b>, passani at eunet dot no
 * <br><br>
 * This class is useful to avoid exposing the Wurfl class externally.
 * It just returns lists of devices, lists of capabilities and
 * lists of capability name/values.
 * 
 */

public class ListManager {

    private Wurfl wu = null;

    private HashSet deviceIdSet = null;
    private ArrayList deviceIdSetSorted = null;

    private ArrayList capabilitySet = null; 
    private HashMap listOfGroups = null;

    private HashMap deviceElementsListXOM = null; 
    private HashMap deviceElementsList = null;//for fast access to device elements

    //lists of actual devices
    private TreeMap actualDeviceElementsList = new TreeMap(); //for fast access to actual device elements
    private TreeMap actualDevicesByBrand = new TreeMap(); //Actual Devices by brand
    private ArrayList brandList = new ArrayList(40); //List of device brands

    private HashMap listOfUAWithDeviceID = null; //associate UA string with device ID

    //Map of HashMaps with the list of capabilities/values. Key is a device ID
    //useful to avoid regenerating a new big HashMap every time getCapabilitiesForDeviceID()
    //is invoked
    private Map listOfListsOfCapabilityValuePairs = (new HashMap(200, 0.75f));; 
                                                              

    ListManager(Wurfl _wu) {
   
      wu = _wu;
      capabilitySet = wu.getListOfCapabilities();
      listOfGroups = wu.getListOfGroups();

      deviceIdSet = wu.getDeviceIdSet(); 
      deviceIdSetSorted = new ArrayList(deviceIdSet);
      Collections.sort(deviceIdSetSorted);

      listOfUAWithDeviceID = wu.getListOfUAWithDeviceID();      
       
      deviceElementsListXOM = wu.getDeviceElementsList();

      //Turn the HashMap of devices from one of nu.xom.Element to WurflDevice
      Iterator keys = deviceElementsListXOM.keySet().iterator();
      String key;
      deviceElementsList = new HashMap(2053,0.75f);
      while( keys.hasNext() )  {
	  key = (String )keys.next();
          WurflDevice wd = new WurflDevice((Element)deviceElementsListXOM.get(key));
	  deviceElementsList.put(key,wd);
      }


   }
   
   /** 
    * Return HashSet of device IDs (all device IDs, optimized for fast look-up on existence)
    *
    */

   public HashSet getDeviceIdSet() {
      
       return deviceIdSet;
   }

  /** 
    * Return ArrayList of device IDs to WurflDevices
    *
    */

   public ArrayList getDeviceIdSetSorted() {
       return deviceIdSetSorted;
   }


   /** 
    * Return ArrayList of Capabilities (i.e. name of all capabilities)
    *
    */

   public ArrayList getCapabilitySet() {
       return capabilitySet;
   }



   /** 
    * Return HashMap of Arraylists of Capabilities (i.e. name of all capabilities)
    *
    */

   public HashMap getListOfGroups() {
       return listOfGroups;
   }

   /** 
    * Return HashMap of device IDs to WurflDevices
    *
    */

   public HashMap getDeviceElementsList() {
       return deviceElementsList;
   }


   /** 
    * Return HashMap (from UA Strings to Device Ids)
    *
    */

   public HashMap getListOfUAWithDeviceID() {
       return listOfUAWithDeviceID;
   }


   /** 
    * Given a device ID returns HashMap with association
    * of all capabilities and their value
    *
    */

   public HashMap getCapabilitiesForDeviceID(String device_id) {

       Object obj = listOfListsOfCapabilityValuePairs.get(device_id);
       if (obj != null) { //HashMap exists from the past
	   return (HashMap)obj;
       } else { //we need to generate it and store for the next time
	   HashMap hm = new HashMap(capabilitySet.size());
	   CapabilityMatrix cm = ObjectsManager.getCapabilityMatrixInstance();

	   for (int i=0; i<capabilitySet.size(); i++) {
	       hm.put(capabilitySet.get(i),cm.getCapabilityForDevice(device_id,(String)capabilitySet.get(i)));
	   }
	   listOfListsOfCapabilityValuePairs.put(device_id,hm);
	   return hm;
       }
   }


   /** 
    * Return TreeMap of device IDs to WurflDevices
    * representing actual devices (i.e. this device element
    * represents a real device and a bunch of subdevices with
    * similar software subversions.
    *
    */

    public synchronized TreeMap getActualDeviceElementsList() {
       
	if (actualDeviceElementsList.isEmpty()) { 	       
	    CapabilityMatrix cm = ObjectsManager.getCapabilityMatrixInstance();	       
	    TreeMap actualXOMDevices = wu.getActualDeviceElementsList();
	    Iterator keys = actualXOMDevices.keySet().iterator();	    
	    while( keys.hasNext() )  {
		
		String key = (String)keys.next();
		Element el = (Element) actualXOMDevices.get(key);
		WurflDevice wd = new WurflDevice(el);	  
		String bn = cm.getCapabilityForDevice(key,"brand_name");
		String mn = cm.getCapabilityForDevice(key,"model_name");
		//only devices with name and brand defined in the WURFL please
		if (!bn.equals("") && !mn.equals("")) {
		    wd.setBrandName(bn);
		    wd.setModelName(mn);
		    actualDeviceElementsList.put(key,wd);
		}
		//else {  //just for debugging purposes
		//    System.out.println("Discarding actual device: "+wd.getId());
		//}
	    }
	}
	return actualDeviceElementsList;
    }


   /*
    * Just for debugging purposes. Returns list of devices
    * also for devices with no brand or no model name
    */

    public TreeMap getSpecialActualDeviceElementsList() {
       
	TreeMap specialActualDeviceElementsList = new TreeMap();

	CapabilityMatrix cm = ObjectsManager.getCapabilityMatrixInstance();	       
	TreeMap actualXOMDevices = wu.getActualDeviceElementsList();
	Iterator keys = actualXOMDevices.keySet().iterator();	    
	while( keys.hasNext() )  {
	    
	    String key = (String)keys.next();
	    Element el = (Element) actualXOMDevices.get(key);
	    WurflDevice wd = new WurflDevice(el);	  
	    String bn = cm.getCapabilityForDevice(key,"brand_name");
	    String mn = cm.getCapabilityForDevice(key,"model_name");
	    wd.setBrandName(bn);
	    wd.setModelName(mn);
	    specialActualDeviceElementsList.put(key,wd);
	}

	return specialActualDeviceElementsList;
    }
    
    /** 
     * Return HashMap of HashMaps brand-&gt;modelname-&gt;WurflDevice
     *
     */ 
    public TreeMap getDeviceGroupedByBrand() {
	
	if (actualDevicesByBrand.isEmpty()) {
	    TreeMap act_devices = getActualDeviceElementsList();       
	    Iterator keys = act_devices.keySet().iterator();	    
	    while( keys.hasNext() )  {
		String key = (String)keys.next();
		WurflDevice wd = (WurflDevice) act_devices.get(key);
		String bn = wd.getBrandName();
		if (actualDevicesByBrand.get(bn) == null) {
		    //new brand
		    TreeMap hm = new TreeMap();
		    hm.put(wd.getModelName(),wd);
		    actualDevicesByBrand.put(bn,hm);
		} else {
		    //add to existing brand
		    TreeMap hm = (TreeMap) actualDevicesByBrand.get(bn);
		    hm.put(wd.getModelName(),wd);
		}
	    }
	}
	return actualDevicesByBrand;
    }
    
    
    /** 
     * Return Ordered ArrayList of Brand Name
     *
     */ 
    public ArrayList getDeviceBrandList() {
	
	if (brandList.isEmpty()) {
	    TreeMap lol = getDeviceGroupedByBrand();
	    brandList = new ArrayList(lol.keySet());
	    Collections.sort(brandList);
	}
	return brandList;
    }
    
}


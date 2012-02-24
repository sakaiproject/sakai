/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;

/*
 * Used to provide a  interface to the MergedList class.  This wrapper
 * is created with a fixed set of channel references.  It then
 * defers most function calls to a wrapped EntryProvider that can
 * be of any type.  This is used when we know ahead of time which
 * channel references that we'll want to use.  This is to avoid
 * trying to process all sites.
 */
public class MergedListEntryProviderFixedListWrapper implements MergedList.EntryProvider
{
    /**
     * Defines a callback interface to convert a reference into a channel.
     */
    public interface ReferenceToChannelConverter
    {

        
        Object getChannel(String channelReference);

    }
   private MergedList.EntryProvider entryProvider;
   private List channelReferenceList = new ArrayList();
   
   
   public MergedListEntryProviderFixedListWrapper(MergedList.EntryProvider entryProvider, String primaryChannelReference, String [] mergedChannelsReferences, ReferenceToChannelConverter refToChan)
   {
       this.entryProvider = entryProvider;
       
       channelReferenceList.add(refToChan.getChannel(primaryChannelReference));
       
       // Add the merged channels.
       for ( int i=0; i < mergedChannelsReferences.length; i++ )
       {
           // Don't add the primary channel if it is already in the list.
           if ( !primaryChannelReference.equals(mergedChannelsReferences[i]) )
           {
               channelReferenceList.add(refToChan.getChannel(mergedChannelsReferences[i]));
    		}
       }
   }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#getIterator()
   */
  public Iterator getIterator()
  {
       return channelReferenceList.iterator();
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#allowGet(java.lang.String)
   */
  public boolean allowGet(String ref)
  {
       return entryProvider.allowGet(ref);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#getContext(java.lang.Object)
   */
  public String getContext(Object obj)
  {
       return entryProvider.getContext(obj);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#getReference(java.lang.Object)
   */
  public String getReference(Object obj)
  {
      if ( obj == null )
      {
          return null;
      }
          
      return entryProvider.getReference(obj);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#getProperties(java.lang.Object)
   */
  public ResourceProperties getProperties(Object obj)
  {
      if ( obj == null )
      {
          return null;
      }

      return entryProvider.getProperties(obj);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#isUserChannel(java.lang.Object)
   */
  public boolean isUserChannel(Object channel)
  {
      if ( channel == null )
      {
          return false;
      }

      return entryProvider.isUserChannel(channel);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#isSpecialSite(java.lang.Object)
   */
  public boolean isSpecialSite(Object channel)
  {
      if ( channel == null )
      {
          return true;
      }
      return entryProvider.isSpecialSite(channel);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#getSiteUserId(java.lang.Object)
   */
  public String getSiteUserId(Object channel)
  {
      if ( channel == null )
      {
          return "";
      }
      return entryProvider.getSiteUserId(channel);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.util.MergedList.EntryProvider#getSite(java.lang.Object)
   */
  public Site getSite(Object channel)
  {
      if ( channel == null )
      {
          return null;
      }
      
     return entryProvider.getSite(channel);
  }

}

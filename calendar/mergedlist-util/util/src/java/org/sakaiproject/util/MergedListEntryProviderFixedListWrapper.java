/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

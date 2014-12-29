/**
 * $Id$
 * $URL$
 * EntityTaggingService.java - entity-broker - Aug 4, 2008 10:37:25 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.dao.EntityBrokerDao;
import org.sakaiproject.entitybroker.dao.EntityTagApplication;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.entityprovider.capabilities.TagProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Taggable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.TagSearchService;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * Handles calls through the system related to tagging,
 * can delegate to a central tagging service
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityTaggingService implements TagSearchService {

   private EntityBrokerDao dao;
   public void setDao(EntityBrokerDao dao) {
      this.dao = dao;
   }

   private EntityProviderManager entityProviderManager;
   public void setEntityProviderManager(EntityProviderManager entityProviderManager) {
      this.entityProviderManager = entityProviderManager;
   }

   private EntityBrokerManager entityBrokerManager;
   public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
      this.entityBrokerManager = entityBrokerManager;
   }


   public List<EntityData> findEntitesByTags(String[] tags, String[] prefixes,
         boolean matchAll, org.sakaiproject.entitybroker.entityprovider.search.Search search) {
      // FIXME the match all and handling of merging results from multiple providers is currently a mess -AZ
      // check for valid inputs
      if (tags == null || tags.length == 0) {
         throw new IllegalArgumentException(
               "At least one tag must be supplied to this search, tags cannot be null or empty");
      }

      List<EntityData> results = new ArrayList<EntityData>();

      // do a quick check first for efficiency, only search by prefix if there is a valid prefix to check
      boolean doSearch = true;
      HashSet<String> validPrefixes = new HashSet<String>();
      if (prefixes != null) {
         for (String prefix : prefixes) {
            boolean valid = true;
            if (entityProviderManager.getProviderByPrefix(prefix) == null) {
               valid = false;
            } else {
               if (entityProviderManager.getProviderByPrefixAndCapability(prefix, Taggable.class) == null) {
                  valid = false;
               }
            }
            if (valid) {
               validPrefixes.add(prefix);
            }
         }
         if (validPrefixes.size() == 0) {
            doSearch = false;
         }
      }

      if (doSearch) {
         // search the internal storage first
         Search dbSearch = new Search();
         if (!matchAll && search != null) {
            // cannot use limits with match all...
            dbSearch.setLimit(search.getLimit());
            dbSearch.setStart(search.getStart());
         }
         dbSearch.addRestriction( new Restriction("tag", tags) );
         if (validPrefixes.size() > 0) {
            dbSearch.addRestriction( new Restriction("entityPrefix", validPrefixes.toArray(new String[validPrefixes.size()])) );
         }
         dbSearch.addOrder( new Order("entityRef") );
         List<EntityTagApplication> tagApps = dao.findBySearch(EntityTagApplication.class, dbSearch);
         if (matchAll) {
            // handle match all
            HashMap<String, Integer> matchMap = new HashMap<String, Integer>();
            for (EntityTagApplication tagApp : tagApps) {
               if (matchMap.containsKey(tagApp.getEntityRef())) {
                  int current = matchMap.get(tagApp.getEntityRef()).intValue();
                  matchMap.put(tagApp.getEntityRef(), current + 1);
               } else {
                  matchMap.put(tagApp.getEntityRef(), 1);
               }
            }
            int allCount = tags.length;
            for (Entry<String, Integer> entry : matchMap.entrySet()) {
               if (entry.getValue() >= allCount) {
                  results.add( new EntityData(entry.getKey(), (String)null) );
               }
            }
            Collections.sort(results, new EntityData.ReferenceComparator());
         } else {
            // filter the list down to the references first
            HashMap<String, String> refToTags = new HashMap<String, String>();
            for (EntityTagApplication tagApp : tagApps) {
               if (refToTags.containsKey(tagApp.getEntityRef())) {
                  refToTags.put(tagApp.getEntityRef(), refToTags.get(tagApp.getEntityRef()) + EntityView.SEPARATOR + tagApp.getTag());
               } else {
                  refToTags.put(tagApp.getEntityRef(), tagApp.getTag());
                  // note: no display available here
                  results.add( new EntityData(tagApp.getEntityRef(), (String)null) );
               }
            }
            // add in the tags property
            for (EntityData ed : results) {
               String reference = ed.getEntityReference().toString();
               ed.getEntityProperties().put("tags", refToTags.get(reference));
            }
         }

         if (isUnderSearchLimit(results.size(), search)) {
            // TODO how to handle search.start for these?
            // get the results from any entity providers which supply tag search results
            List<TagProvideable> tagProviders = entityProviderManager.getProvidersByCapability(TagProvideable.class);
            for (TagProvideable provider : tagProviders) {
               // only call if prefixes match and we are not at the limit
               if (isUnderSearchLimit(results.size(), search)) {
                  if (validPrefixes.size() == 0 
                        || validPrefixes.contains(provider.getEntityPrefix())) {
                     List<EntityData> pList = provider.findEntitesByTags(tags, matchAll, search);
                     if (pList != null) {
                        for (EntityData esr : pList) {
                           if (isUnderSearchLimit(results.size(), search)) {
                              results.add( esr );
                           } else {
                              break;
                           }
                        }
                     }
                  }
               }
            }
            // TODO fix up the order?
         }

         // populate the entity search results URLs (display names?)
         entityBrokerManager.populateEntityData(results);
      }
      return results;
   }

   private boolean isUnderSearchLimit(int value, org.sakaiproject.entitybroker.entityprovider.search.Search search) {
      boolean under = false;
      if (search == null) {
         under = true;
      } else {
         if (search.getLimit() > 0) {
            if (value < search.getLimit()) {
               under = true;
            }
         } else {
            under = true;
         }
      }
      return under;
   }

   public List<String> getTagsForEntity(String reference) {
      ArrayList<String> tags = new ArrayList<String>();
      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Invalid reference (" + reference + "), no entity provider to handle this reference");
      } else {
         reference = ref.toString();
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference + "), entity does not exist");
         }

         if (entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Taggable.class) != null) {
            TagProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), TagProvideable.class);
            if (provider == null) {
               // put in call to central tag system here if desired

               List<EntityTagApplication> results = dao.findBySearch(EntityTagApplication.class, new Search("entityRef", reference));
               for (EntityTagApplication entityTagApplication : results) {
                  tags.add(entityTagApplication.getTag());
               }
            } else {
               List<String> tList = provider.getTagsForEntity(reference);
               if (tList != null) {
                  tags.addAll( tList );
               }
            }
         } else {
            throw new UnsupportedOperationException("Cannot get tags from this entity ("+reference+"), it has no support for tagging in its entity provider");
         }
      }
      Collections.sort(tags);
      return tags;
   }

   public void addTagsToEntity(String reference, String[] tags) {
      if (tags == null) {
         throw new IllegalArgumentException("Invalid params, tags cannot be null");
      }
      validateTags(tags); // ensures tags are valid

      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Invalid reference (" + reference + "), no entity provider to handle this reference");
      } else {
         reference = ref.toString();
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference + "), entity does not exist");
         }

         if (entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Taggable.class) != null) {
            TagProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), TagProvideable.class);
            if (provider == null) {
               // put in call to central tag system here if desired

               Set<String> addTags = new HashSet<String>();
               Set<String> removeTags = new HashSet<String>();
               diffEntityTags(reference, tags, addTags, removeTags);

               Set<EntityTagApplication> newTagApps = new HashSet<EntityTagApplication>();
               for (String tag : addTags) {
                  newTagApps.add( new EntityTagApplication(reference, ref.getPrefix(), tag) );
               }
               if (newTagApps.size() > 0) {
                  dao.saveSet(newTagApps);
               }
            } else {
               provider.addTagsToEntity(reference, tags);
            }
         } else {
            throw new UnsupportedOperationException("Cannot set tags for this entity ("+reference+"), it has no support for tagging in its entity provider");
         }
      }
   }

   public void removeTagsFromEntity(String reference, String[] tags) {
      if (tags == null) {
         throw new IllegalArgumentException("Invalid params, tags cannot be null");
      }
      validateTags(tags); // ensures tags are valid

      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Invalid reference (" + reference + "), no entity provider to handle this reference");
      } else {
         reference = ref.toString();
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference + "), entity does not exist");
         }

         if (entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Taggable.class) != null) {
            TagProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), TagProvideable.class);
            if (provider == null) {
               // put in call to central tag system here if desired
               dao.deleteTags(reference, tags);
            } else {
               provider.removeTagsFromEntity(reference, tags);
            }
         } else {
            throw new UnsupportedOperationException("Cannot set tags for this entity ("+reference+"), it has no support for tagging in its entity provider");
         }
      }
   }

   public void setTagsForEntity(String reference, String[] tags) {
      if (tags == null) {
         throw new IllegalArgumentException("Invalid params, tags cannot be null");
      }
      validateTags(tags); // ensures tags are valid

      EntityReference ref = entityBrokerManager.parseReference(reference);
      if (ref == null) {
         throw new IllegalArgumentException("Invalid reference (" + reference + "), no entity provider to handle this reference");
      } else {
         reference = ref.toString();
         if (! entityBrokerManager.entityExists(ref)) {
            throw new IllegalArgumentException("Invalid reference (" + reference + "), entity does not exist");
         }

         if (entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), Taggable.class) != null) {
            TagProvideable provider = entityProviderManager.getProviderByPrefixAndCapability(ref.getPrefix(), TagProvideable.class);
            if (provider == null) {
               // put in call to central tag system here if desired

               Set<String> addTags = new HashSet<String>();
               Set<String> removeTags = new HashSet<String>();
               diffEntityTags(reference, tags, addTags, removeTags);

               Set<EntityTagApplication> newTagApps = new HashSet<EntityTagApplication>();
               for (String tag : addTags) {
                  newTagApps.add( new EntityTagApplication(reference, ref.getPrefix(), tag) );
               }
               if (newTagApps.size() > 0) {
                  dao.saveSet(newTagApps);
               }
               if (removeTags.size() > 0) {
                  dao.deleteTags(reference, removeTags.toArray(new String[removeTags.size()]));
               }
            } else {
               provider.setTagsForEntity(reference, tags);
            }
         } else {
            throw new UnsupportedOperationException("Cannot set tags for this entity ("+reference+"), it has no support for tagging in its entity provider");
         }
      }
   }

   /**
    * Finds the current tags and then diffs them to get the tags which need to be added and the tags which need to be deleted
    * @param reference entity ref
    * @param tags desired set of tags
    * @param addTags tags which need to be added
    * @param removeTags tags which need to be removed
    */
   protected void diffEntityTags(String reference, String[] tags, Set<String> addTags, Set<String> removeTags) {
      // first get the current set of tags for this reference
      Set<String> curTags = new HashSet<String>();
      List<EntityTagApplication> results = dao.findBySearch(EntityTagApplication.class, new Search("entityRef", reference));
      for (EntityTagApplication entityTagApplication : results) {
         curTags.add(entityTagApplication.getTag());
      }

      Set<String> setTags = new HashSet<String>();
      for (String tag : tags) {
         setTags.add(tag);
         if (curTags.contains(tag)) {
            continue;
         } else {
            addTags.add(tag);
         }
      }
      curTags.removeAll(setTags);
      removeTags.clear();
      removeTags.addAll(curTags);
   }

   /**
    * Validate a set of tags
    * @param tags
    */
   protected void validateTags(String[] tags) {
      for (String tag : tags) {
         if (! tag.matches(TemplateParseUtil.VALID_VAR_CHARS+"+")) {
            throw new IllegalArgumentException("Invalid tag ("+tag+"), tags can only contain the following (not counting []): " + TemplateParseUtil.VALID_VAR_CHARS);
         }
      }
   }

}

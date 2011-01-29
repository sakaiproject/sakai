/*
 * Created on 21 Jul 2008
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

/** Common base interface to a provider which handles a particular set of entity prefixes */

public interface PrefixHandler {

  /**
   * Allows you to define all the entity prefixes that this inferrer should deal with. This
   * can be more that one prefix but it is better to have one inferrer per entity,<br/>
   * recommend you use the ENTITY_PREFIX static string from your provider if you are
   * following best practices.
   * 
   * @return an array of all handled prefixes
   */
  public String[] getHandledPrefixes();
  
}

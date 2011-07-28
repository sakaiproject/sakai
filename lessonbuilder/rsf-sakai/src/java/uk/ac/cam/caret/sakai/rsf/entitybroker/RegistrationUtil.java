/*
 * Created on 22 Jul 2008
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import java.util.List;
import java.util.Map;

import uk.org.ponder.stringutil.StringList;

public class RegistrationUtil {
  public static StringList collectPrefixes(List inferrers, Map inferrermap) {
    StringList allprefixes = new StringList();
    if (inferrers != null && inferrers.size() > 0) {
      for (int i = 0; i < inferrers.size(); ++ i) {
        PrefixHandler evpi = (PrefixHandler) inferrers.get(i);
        String[] prefixes = evpi.getHandledPrefixes();
        allprefixes.append(prefixes);
        for (int j = 0; j < prefixes.length; ++ j) {
          inferrermap.put(prefixes[j], evpi);
        }
      }
    }
    return allprefixes;
  }
  
}

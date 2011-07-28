/*
 * Created on 1 Sep 2008
 */
package uk.ac.cam.caret.sakai.rsf.errors;

import uk.org.ponder.arrayutil.ArrayUtil;

public class ErrorFilter {

  private String[] ignoredKeys;
  
  public void setIgnoredKeys(String[] ignoredKeys) {
    this.ignoredKeys = ignoredKeys;
  }

  public boolean matchIgnores(String[] messagecodes) {
    if (messagecodes == null) return false;
    for (int i = 0; i < ignoredKeys.length; ++ i) {
      if (ArrayUtil.indexOf(messagecodes, ignoredKeys[i]) != -1)
        return true;
    }
    return false;
  }
  
}

package org.sakaiproject.tool.assessment.osid.shared.extension;

import org.osid.shared.Type;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TypeExtension extends Type {
  private String typeId;

  public TypeExtension(String authority, String domain, String keyword)
  {
    super(authority, domain, keyword);
  }

  public TypeExtension(
    String authority, String domain, String keyword, String description)
  {
    // cannot log due to the way super() works
    super(authority, domain, keyword, description);
  }

}
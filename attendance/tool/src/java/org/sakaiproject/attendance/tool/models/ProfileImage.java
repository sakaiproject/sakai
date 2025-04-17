package org.sakaiproject.attendance.tool.models;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

public class ProfileImage extends WebComponent
{
  public ProfileImage(String id, IModel urlModel)
  {
    super( id, urlModel );
  }

  protected void onComponentTag(ComponentTag tag)
  {
    super.onComponentTag( tag );
    checkComponentTag( tag, "img" );
    tag.put( "src", getDefaultModelObjectAsString() );
  }
}

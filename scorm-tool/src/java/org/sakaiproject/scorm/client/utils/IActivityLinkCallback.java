package org.sakaiproject.scorm.client.utils;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface IActivityLinkCallback extends Serializable
{
	void onClick(AjaxRequestTarget target);
}

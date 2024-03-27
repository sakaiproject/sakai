/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.wicket.markup.html.feedback;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * Wrapper for the Wicket FeedbackPanel to apply Sakai CSS banner classes as appropriate.
 * 
 * @author bjones86
 */
public class SakaiFeedbackPanel extends FeedbackPanel
{
	private static final long serialVersionUID = 1L;

	public SakaiFeedbackPanel( String id, IFeedbackMessageFilter filter )
	{
		super( id, filter );
	}

	public SakaiFeedbackPanel( String id )
	{
		super( id );
	}

	@Override
	protected String getCSSClass( final FeedbackMessage message )
	{
		switch( message.getLevel() )
		{
			case FeedbackMessage.SUCCESS:
				return "sak-banner-success";
			case FeedbackMessage.INFO:
				return "sak-banner-info";
			case FeedbackMessage.WARNING:
				return "sak-banner-warn";
			default:
				return "sak-banner-error";
		}
	}
}

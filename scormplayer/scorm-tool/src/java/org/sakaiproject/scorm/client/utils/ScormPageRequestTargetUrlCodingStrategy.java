package org.sakaiproject.scorm.client.utils;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;

public class ScormPageRequestTargetUrlCodingStrategy extends
		AbstractRequestTargetUrlCodingStrategy {

	/** bookmarkable page class. */
	protected final Class bookmarkablePageClass;

	/** page map name. */
	final String pageMapName;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            the mount path
	 * @param bookmarkablePageClass
	 *            the class of the bookmarkable page
	 * @param pageMapName
	 *            the page map name if any
	 */
	public ScormPageRequestTargetUrlCodingStrategy(final String mountPath, Class bookmarkablePageClass, String pageMapName)
	{
		super(mountPath);

		if (bookmarkablePageClass == null)
		{
			throw new IllegalArgumentException("Argument bookmarkablePageClass must be not null");
		}

		this.bookmarkablePageClass = bookmarkablePageClass;
		this.pageMapName = pageMapName;
	}


	

	public IRequestTarget decode(RequestParameters requestParameters)
	{
		final String parametersFragment = requestParameters.getPath().substring(
				getMountPath().length());
		final PageParameters parameters = new PageParameters(decodeParameters(parametersFragment,
				requestParameters.getParameters()));
		String pageMapName = (String)parameters.remove(WebRequestCodingStrategy.PAGEMAP);
		if (requestParameters.getPageMapName() == null)
		{
			requestParameters.setPageMapName(pageMapName);
		}
		else
		{
			pageMapName = requestParameters.getPageMapName();
		}

		// do some extra work for checking whether this is a normal request to a
		// bookmarkable page, or a request to a stateless page (in which case a
		// wicket:interface parameter should be available
		final String interfaceParameter = (String)parameters
				.remove(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);

		if (interfaceParameter != null)
		{
			WebRequestCodingStrategy.addInterfaceParameters(interfaceParameter, requestParameters);
			return new BookmarkableListenerInterfaceRequestTarget(pageMapName,
					bookmarkablePageClass, parameters, requestParameters.getComponentPath(),
					requestParameters.getInterfaceName());
		}
		else
		{
			return new BookmarkablePageRequestTarget(pageMapName, bookmarkablePageClass, parameters);
		}
	}

	public final CharSequence encode(final IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with "
					+ "instances of " + IBookmarkablePageRequestTarget.class.getName());
		}
		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		final IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;

		PageParameters pageParameters = target.getPageParameters();
		String pagemap = pageMapName != null ? pageMapName : target.getPageMapName();
		if (pagemap != null)
		{
			if (pageParameters == null)
			{
				pageParameters = new PageParameters();
			}
			pageParameters.put(WebRequestCodingStrategy.PAGEMAP, pagemap);
		}
		appendParameters(url, pageParameters);
		return url;
	}

	public boolean matches(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
			if (bookmarkablePageClass.equals(target.getPageClass()))
			{
				if (this.pageMapName == null)
				{
					return true;
				}
				else
				{
					return this.pageMapName.equals(target.getPageMapName());
				}
			}
		}
		return false;
	}
	
	public String toString()
	{
		return "BookmarkablePageEncoder[page=" + bookmarkablePageClass + "]";
	}

}

package org.sakaiproject.content.api;

/**
 * This service is for both copying all the resources in a Site (re-writing the URLs), but also for 
 * copying content related to tools which might have URLs into content stored in content hosting.
 * 
 * TODO: Special case "archive" (multiple, space separated)
 * 
 * To use this service to rewrite a piece of HTML content and copy across the resources when
 * copying between sites use something like:
 * <pre>
 * {@code
 * ContentCopy contentCopy = (ContentCopy)ComponentManager.get(ContentCopy.class);
 * ContentCopyContext context = contextCopy.createCopy("oldSiteId", "newSiteId", true);
 * String updateHtml = contentCopy.convertContent(context, "<div>Announcement text....", "text/html", null);
 * contentCopy.copyReferences(context);
 * }
 * </pre>
 * This will give you the updated HTML to put in the new site and copy and referenced resources into the new site.
 * 
 * @author buckett
 * 
 */
public interface ContentCopy {
	
	/**
	 * Create a new ContentCopyContext.
	 * @param oldSiteId The ID of the site with the existing content.
	 * @param newSiteId The ID of the site into which the content is being moved.
	 * @param walkReferences If <code>true</code> then references in HTML will also be walked and added to the
	 * list of content to be copied.
	 * @return A ContentCopyContext ready to be used for copying.
	 */
	public ContentCopyContext createCopyContext(String oldSiteId, String newSiteId, boolean walkReferences);
	
	/**
	 * This copies all the references defined in the context from one site to
	 * another.
	 * 
	 * @param context
	 *            The context about the resources to be copied.
	 */
	public void copyReferences(ContentCopyContext context);
	
	/**
	 * This looks for all the references in the supplied content and returns the
	 * re-written string. It also updates the supplied content and returns it.
	 * This can be use to initially setup the references if copying a piece of
	 * HTML content with embedded references. The calling code should so a
	 * sanity check on the size of the content before calling this. This doesn't
	 * deal with the content hosting API directly as it can be used on abitary
	 * HTML inside tools, for example copying the resources related to an
	 * announcement. This doesn't so any copying itself.
	 * 
	 * @param context
	 * @param content
	 *            The content to filter.
	 * @param mimeType
	 *            The mime type of the supplied content. This is most likely to
	 *            be &quot;text/html&quot;.
	 * @param contextConext
	 *            The URL of the content where is the currently deployed, this
	 *            can be <code>null</code>, but if it is then we won't copy
	 *            relative links. Normally it's enough to just supply the path.
	 * @return The converted content.
	 */
	public String convertContent(ContentCopyContext context, String content, String mimeType, String contentUrl);

}

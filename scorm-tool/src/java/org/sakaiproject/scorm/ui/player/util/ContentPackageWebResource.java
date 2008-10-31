package org.sakaiproject.scorm.ui.player.util;

import org.apache.wicket.Application;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.sakaiproject.scorm.model.api.ContentPackageResource;

public class ContentPackageWebResource extends WebResource {
	
	private static final long serialVersionUID = 1L;

	private static final String[] candidateCompressionContentTypes = { /*"text/html"*/ };
	
	private ContentPackageResource resource;
	private ContentPackageResourceStream resourceStream;
	
	public ContentPackageWebResource(ContentPackageResource resource) {
		setCacheable(false);
		this.resource = resource;
		this.resourceStream = new ContentPackageResourceStream(resource);
	}
	
	@Override
	public IResourceStream getResourceStream() {
		
		if (canCompress())
			return new CompressingContentPackageResourceStream(resource);
		
		return resourceStream;
	}
	
	@Override
	protected void setHeaders(WebResponse response) {
		super.setHeaders(response);
		if (canCompress()) {
			response.setHeader("Content-Encoding", "gzip");
		}
	}
	
	private boolean canCompress() {
		return isCandidateForCompression() && supportsCompression();	
	}
	
	private boolean isCandidateForCompression() {
		String contentType = resourceStream.getContentType();
		
		if (contentType != null)
			for (int i=0;i<candidateCompressionContentTypes.length;i++) 
				if (contentType.equals(candidateCompressionContentTypes[i]))
					return true;
		
		return false;
	}
	
	private boolean supportsCompression() {
		if (Application.get().getResourceSettings().getDisableGZipCompression())
		{
			return false;
		}
		if (RequestCycle.get() == null)
			return false;
		
		WebRequest request = (WebRequest)RequestCycle.get().getRequest();
		String s = request.getHttpServletRequest().getHeader("Accept-Encoding");
		if (s == null)
		{
			return false;
		}
		else
		{
			return s.indexOf("gzip") >= 0;
		}
	}
}

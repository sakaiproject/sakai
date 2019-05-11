package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;

public class JavaScriptToBucketResponseDecorator implements IHeaderResponseDecorator {
    private String bucketName;

    public JavaScriptToBucketResponseDecorator(String bucketName) {
        this.bucketName = bucketName;
    }

    @Override
    public IHeaderResponse decorate(IHeaderResponse response) {
        return new JavaScriptFilteredIntoFooterHeaderResponse(response, bucketName);
    }
}

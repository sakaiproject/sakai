
package org.tsugi.contentitem.objects;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;
import org.tsugi.casa.CASAUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "@context",
    "@graph"
})

public class ContentItemResponse extends JacksonBase {

    @JsonProperty("@context")
    private String context;

    @JsonProperty("@graph")
    private List<Object> graph = new ArrayList<Object>();

    // Constructor
    public ContentItemResponse() {
	this.context = "http://purl.imsglobal.org/ctx/lti/v1/ContentItem";
    }

    @JsonProperty("@context")
    public String getContext() {
        return context;
    }

    @JsonProperty("@context")
    public void setContext(String context) {
        this.context = context;
    }

    @JsonProperty("@graph")
    public List<Object> getGraph() {
        return graph;
    }

    @JsonProperty("@graph")
    public void setGraph(List<Object> graph) {
        this.graph = graph;
    }

    // Convienence method
    public void addGraph(Object item) {
	this.graph.add(item);
    }

}


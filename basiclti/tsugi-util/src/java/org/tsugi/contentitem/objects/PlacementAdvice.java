
package org.tsugi.contentitem.objects;

import java.util.ArrayList;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.shared.objects.TsugiBase;
import org.tsugi.casa.CASAUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "presentationDocumentTarget",
    "width",
    "height"
})

public class PlacementAdvice extends TsugiBase {

    public static final String WINDOW = "window";
    public static final String IFRAME = "iframe";

    @JsonProperty("presentationDocumentTarget")
    private String presentationDocumentTarget;
    @JsonProperty("displayHeight")
    private Integer displayHeight;
    @JsonProperty("displayWidth")
    private Integer displayWidth;

    // Constructor
    public PlacementAdvice() {
	this.presentationDocumentTarget = "window";
	this.displayHeight = 1024;
	this.displayWidth = 800;
    }

    @JsonProperty("presentationDocumentTarget")
    public String getPresentationDocumentTarget() {
        return presentationDocumentTarget;
    }

    @JsonProperty("presentationDocumentTarget")
    public void setPresentationDocumentTarget(String presentationDocumentTarget) {
        this.presentationDocumentTarget = presentationDocumentTarget;
    }

    @JsonProperty("displayHeight")
    public Integer getDisplayHeight() {
        return displayHeight;
    }

    @JsonProperty("displayHeight")
    public void setDisplayHeight(Integer displayHeight) {
        this.displayHeight = displayHeight;
    }

    @JsonProperty("displayWidth")
    public Integer getDisplayWidth() {
        return this.displayWidth;
    }

    @JsonProperty("displayWidth")
    public void setDisplayWidth(Integer displayWidth) {
        this.displayWidth = displayWidth;
    }

}


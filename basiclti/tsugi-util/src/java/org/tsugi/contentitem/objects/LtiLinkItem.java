
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
    "@type",
    "@id",
    "title",
    "mediaType",
    "text",
    "url",
    "placementAdvice",
    "icon"
})

public class LtiLinkItem extends TsugiBase {

    @JsonProperty("placementAdvice")
    private PlacementAdvice placementAdvice;

    @JsonProperty("icon")
    private Icon icon;

    // Constructor
    public LtiLinkItem(String id, PlacementAdvice placementAdvice, Icon icon) {
	this._id = id;
	this._type = "LtiLinkItem";
	this.placementAdvice = placementAdvice;
	this.mediaType = "application/vnd.ims.lti.v1.ltilink";
	this.icon = icon;
    }

}


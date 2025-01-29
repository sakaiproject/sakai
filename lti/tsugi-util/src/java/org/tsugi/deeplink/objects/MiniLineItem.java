
package org.tsugi.deeplink.objects;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)


// Strangely the lineItem in a DeepLink response is *different*
// than the LineItem in AGS (org.tsugi.ags2.objects.LineItem)
// So we call this one the MiniLineItem
public class MiniLineItem extends JacksonBase {

	@JsonProperty("scoreMaximum")
    public Double scoreMaximum;

    @JsonProperty("label")
    public String label;

    @JsonProperty("resourceId")
    public String resourceId;

    @JsonProperty("tag")
    public String tag;
}


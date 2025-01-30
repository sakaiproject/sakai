
package org.tsugi.shared.objects;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonPropertyOrder({
    "url",
    "width",
    "height"
})

public class SizedUrl extends JacksonBase {

    @JsonProperty("url")
    public String url;

    @JsonProperty("width")
    public Integer width;

    @JsonProperty("height")
    public Integer height;

}



package org.tsugi.lti13.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

/*
    "https://purl.imsglobal.org/spec/lti/claim/resource_link": {
        "id": "5",
        "title": "poiuytrewq",
        "description": ""
    },
*/

public class ResourceLink {

    @JsonProperty("id")
    public String id;
    @JsonProperty("title")
    public String title;
    @JsonProperty("description")
    public String description;

}

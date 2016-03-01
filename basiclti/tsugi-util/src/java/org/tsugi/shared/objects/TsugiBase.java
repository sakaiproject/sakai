
package org.tsugi.shared.objects;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")

public class TsugiBase extends org.tsugi.jackson.objects.JacksonBase {

    @JsonProperty("@context")
    protected List<Object> _context = new ArrayList<Object>();

    @JsonProperty("@type")
    protected String _type;

    @JsonProperty("@id")
    protected String _id;

    @JsonProperty("guid")
    protected String guid;

    @JsonProperty("name")
    protected String name;

    @JsonProperty("email")
    protected String email;

    @JsonProperty("title")
    protected String title;

    @JsonProperty("text")
    protected String text;

    @JsonProperty("url")
    protected String url;

    @JsonProperty("uri")
    protected String uri;

    @JsonProperty("mediaType")
    protected String mediaType;

    public TsugiBase() {
    }

    @JsonProperty("@context")
    public List<Object> get_context() {
	if (_context == null || _context.size() < 1 ) return null;
        return _context;
    }

    @JsonProperty("@context")
    public void set_context(List<Object> _context) {
        this._context = _context;
    }

    @JsonProperty("@type")
    public String get_type() {
        return _type;
    }

    @JsonProperty("@type")
    public void set_type(String _type) {
        this._type = _type;
    }

    @JsonProperty("@id")
    public String get_id() {
        return _id;
    }

    @JsonProperty("@id")
    public void set_id(String _id) {
        this._id = _id;
    }

    @JsonProperty("guid")
    public String getGuid() {
        return guid;
    }

    @JsonProperty("guid")
    public void setGuid(String guid) {
        this.guid = guid;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    @JsonProperty("mediaType")
    public String getMediaType() {
        return mediaType;
    }

    @JsonProperty("mediaType")
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }



}

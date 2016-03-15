
package org.tsugi.casa.objects;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.tsugi.jackson.objects.JacksonBase;
import org.tsugi.casa.CASAUtil;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    CASAUtil.LAUNCH_SCHEMA,
    CASAUtil.TITLE_SCHEMA,
    CASAUtil.TEXT_SCHEMA,
    CASAUtil.CONTACT_SCHEMA,
    CASAUtil.ICON_SCHEMA
})

public class Use extends JacksonBase {

    @JsonProperty(CASAUtil.LAUNCH_SCHEMA)
    private Launch launch;
    @JsonProperty(CASAUtil.TITLE_SCHEMA)
    private String title;
    @JsonProperty(CASAUtil.TEXT_SCHEMA)
    private String text;
    @JsonProperty(CASAUtil.CONTACT_SCHEMA)
    private List<Contact> contact = new ArrayList<Contact>();
    @JsonProperty(CASAUtil.ICON_SCHEMA)
    private String icon_url;

    // Constructor
    public Use(Launch launch) {
	this.launch = launch;
    }

    @JsonProperty(CASAUtil.ICON_SCHEMA)
    public String getIcon_url() {
        return icon_url;
    }

    @JsonProperty(CASAUtil.ICON_SCHEMA)
    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    @JsonProperty(CASAUtil.TITLE_SCHEMA)
    public String getTitle() {
        return title;
    }

    @JsonProperty(CASAUtil.TITLE_SCHEMA)
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty(CASAUtil.TEXT_SCHEMA)
    public String getText() {
        return text;
    }

    @JsonProperty(CASAUtil.TEXT_SCHEMA)
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty(CASAUtil.CONTACT_SCHEMA)
    public void setContacts(List<Contact> contact) {
        this.contact = contact;
    }

    // Convienence method
    public void addContact(Contact contact) {
        this.contact.add(contact);
    }

    @JsonProperty(CASAUtil.CONTACT_SCHEMA)
    public List<Contact> getContact() {
	if ( contact.size() < 1 ) return null;
        return contact;
    }


/*
    "use":{ 
      "f6820326-5ea3-4a02-840d-7f91e75eb01b":{
        "registration_url":"http://lti-provider.paulgray.net/register",
        "launch_url":"http://www.google.com"
      },
      "1f2625c2-615f-11e3-bf13-d231feb1dc81":"Local Mock Lti2 Provider",
      "d59e3a1f-c034-4309-a282-60228089194e":[{"name":"Paul Gray","email":"pfbgray@gmail.com"}],
      "c80df319-d5da-4f59-8ca3-c89b234c5055":["dev","lti"],
      "c6e33506-b170-475b-83e9-4ecd6b6dd42a":["lti"],
      "d25b3012-1832-4843-9ecf-3002d3434155":"http://www.iconsdb.com/icons/preview/green/literature-xxl.png",

    },
*/
}


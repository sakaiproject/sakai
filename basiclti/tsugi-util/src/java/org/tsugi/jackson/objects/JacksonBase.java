
package org.tsugi.jackson.objects;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


import org.tsugi.jackson.JacksonUtil;

public class JacksonBase 
{

	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public String prettyPrint() 
		throws com.fasterxml.jackson.core.JsonProcessingException
	{
		return JacksonUtil.prettyPrint(this);
	}

	public String prettyPrintLog() 
	{
		return JacksonUtil.prettyPrintLog(this);
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperties(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}


package org.sakaiproject.taggable.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.taggable.api.URLBuilder;

public class URLBuilderImpl implements URLBuilder {

	private String baseURL;
	private String view;
	private Map<String, String> params;

	public URLBuilderImpl() {
		;
	}

	public URLBuilderImpl(String base, String view, Map<String, String> params) {
		this.baseURL = base;
		this.view = view;
		this.params = params;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getURL() {
		StringBuilder url = new StringBuilder();
		if (baseURL != null) {
			url.append(baseURL);
			if (view != null) {
				url.append(view);
			}
			//Start with "?" then switch to "&" for everything afterwards
			String sep = "?";
			for (Entry<String, String> param : params.entrySet()) {
				url.append(sep + param.getKey() + "=" + param.getValue());
				sep = "&";
			}
		}
		return url.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBaseURL(String base) {
		this.baseURL = base;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setView(String view) {
		this.view = view;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

}

package uk.ac.cam.caret.sakai.rwiki.component.filter;

import org.radeox.filter.CacheFilter;
import org.radeox.filter.regex.LocaleRegexReplaceFilter;

public class SuperscriptFilter extends LocaleRegexReplaceFilter implements
		CacheFilter
{

	protected String getLocaleKey()
	{
		return "filter.superscript";
	}

}

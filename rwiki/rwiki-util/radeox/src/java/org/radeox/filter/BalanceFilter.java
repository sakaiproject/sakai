/**
 * 
 */
package org.radeox.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.filter.balance.Balancer;
import org.radeox.filter.context.FilterContext;

/**
 * @author andrew
 *
 */
public class BalanceFilter implements Filter, CacheFilter
{

	private InitialRenderContext initialContext;

	public static final String matcherString = "(<([^ />]+)(?: [^>]*?[^/])?>)|(</([^ />]+)>)";
	
	/* (non-Javadoc)
	 * @see org.radeox.filter.Filter#before()
	 */
	public String[] before()
	{
		return FilterPipe.EMPTY_BEFORE;
	}

	/* (non-Javadoc)
	 * @see org.radeox.filter.Filter#filter(java.lang.String, org.radeox.filter.context.FilterContext)
	 */
	public String filter(String input, FilterContext context)
	{
		Pattern p = Pattern.compile(matcherString);
		Matcher m = p.matcher(input);
		
		if (m.find()) {
			return actuallyFilter(m);
		} else {
			return input;
		}
	}

	private String actuallyFilter(Matcher m)
	{
		Balancer b = new Balancer();
		b.setMatcher(m);
		
		return b.filter();
	}

	/* (non-Javadoc)
	 * @see org.radeox.filter.Filter#getDescription()
	 */
	public String getDescription()
	{
		return "Balancing XML Fil" +
				"ter";
	}

	/* (non-Javadoc)
	 * @see org.radeox.filter.Filter#replaces()
	 */
	public String[] replaces()
	{
		return FilterPipe.NO_REPLACES;
	}

	/* (non-Javadoc)
	 * @see org.radeox.filter.Filter#setInitialContext(org.radeox.api.engine.context.InitialRenderContext)
	 */
	public void setInitialContext(InitialRenderContext context)
	{
		initialContext = context;
	}

}

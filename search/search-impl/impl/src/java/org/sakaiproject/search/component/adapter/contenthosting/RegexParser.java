/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public class RegexParser implements Iterator<String>
{
	private static Log log = LogFactory.getLog(RegexParser.class);

	private static final char[][] IGNORE_TAGS = new char[][] { "script".toCharArray(), "head".toCharArray(), "style".toCharArray() };

	private static final String PAD = "                                                                                                                   ";

	private static final Properties entities = new Properties();

	static
	{
		try
		{
			InputStream is = RegexParser.class.getResourceAsStream("/org/sakaiproject/search/component/bundle/htmlentities.properties");
			entities.load(is);
			is.close();
		}
		catch (Exception ex)
		{
			log.error("Unable to load HTML Entities", ex);
		}
	};

	private int[] elementStack = new int[1024];

	private int ignore = elementStack.length;

	private boolean notxml = false;

	private char[] cbuf;

	private int current = 0;

	private int clen = 0;;

	private int endstack = 0;

	private int last = 0;

	public RegexParser(String content)
	{
		cbuf = content.toCharArray();
		current = 0;
		clen = cbuf.length;
	}

	public String getTagName(String tag, int start)
	{
		tag = tag.substring(start);
		char[] c = tag.toCharArray();
		String[] words = tag.split("\\s", 2);
		if (words != null && words.length != 0)
		{
			return words[0];
		}
		else
		{
			return tag;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		if (current >= clen)
		{
			return false;
		}
		for (int i = current; i < clen; i++)
		{
			if (cbuf[i] == '<')
			{
				current = i;
				return true;
			}
		}
		current = clen - 1;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public String next()
	{
		int tagend = clen - 1;
		int elend = -1;
		int tagstart = current + 1;
		boolean ignoreBefore = !(ignore > endstack);
		boolean ignoreAfter = ignoreBefore;

		for (int i = current; i < clen; i++)
		{
			if (elend == -1 && Character.isWhitespace(cbuf[i]))
			{
				elend = i;
			}
			if (cbuf[i] == '>')
			{
				tagend = i;
				if (elend == -1)
				{
					elend = tagend;
				}
				break;
			}
		}
		if (tagstart < clen)
		{
			if (cbuf[tagend - 1] == '/')
			{
			}
			else if (tagstart + 2 < clen && cbuf[tagstart] == '!' && cbuf[tagstart + 1] == '-' && cbuf[tagstart + 2] == '-')
			{
			}
			else if (cbuf[tagstart] == '/')
			{
				tagstart++;
				if (!notxml)
				{
					boolean match = true;
					if ((elend - tagstart) == (elementStack[endstack - 1] - elementStack[endstack - 2]))
					{
						int j = elementStack[endstack - 2];
						for (int i = 0; i < (elend - tagstart); i++)
						{
							if (Character.toLowerCase(cbuf[tagstart + i]) != Character.toLowerCase(cbuf[j + i]))
							{
								match = false;
								break;
							}
						}
					}

					if (match)
					{
						endstack -= 2;
						ignoreAfter = !(ignore > endstack);
					}
					else
					{
						notxml = true;
					}
				}
			}
			else
			{
				if (!notxml)
				{
					elementStack[endstack] = tagstart;
					elementStack[endstack + 1] = elend;
					endstack += 2;
					if (!ignoreAfter)
					{
						for (int i = 0; i < IGNORE_TAGS.length; i++)
						{
							if (IGNORE_TAGS[i].length == (elend - tagstart))
							{
								ignoreAfter = true;
								for (int j = 0; j < IGNORE_TAGS[i].length; j++)
								{
									if (IGNORE_TAGS[i][j] != Character.toLowerCase(cbuf[tagstart + j]))
									{
										ignoreAfter = false;
										break;
									}
								}
								if (ignoreAfter)
								{
									break;
								}
							}
						}
					}
				}
			}
		}

		String t = "";
		if (notxml || !ignoreBefore)
		{
			StringBuilder sb = new StringBuilder();
			for (int i = last; i < current; i++)
			{
				if (cbuf[i] == '&')
				{
					if (cbuf[i + 1] == '#')
					{
						for (int j = i; j < current; j++)
						{
							if (cbuf[j] == ';')
							{
								String entity = new String(cbuf, i + 2, j - (i + 2));
								sb.append((char) Integer.decode(entity).intValue());
								i = j;
								break;
							}
						}
					}
					else
					{
						for (int j = i; j < current; j++)
						{
							if (cbuf[j] == ';')
							{

								String entity = new String(cbuf, i, j - i + 1);
								String s = (String) entities.get(entity);
								if (s == null)
								{
									s = entity;
								}
								else if (s.length() > 1)
								{
									sb.append(s.charAt(1));
								}
								i = j;
								break;
							}
						}
					}
				}
				else
				{
					sb.append(cbuf[i]);
				}
			}
			t = sb.toString();
		}
		last = tagend + 1;
		current = last;

		if (ignoreAfter)
		{
			if (!ignoreBefore)
			{
				ignore = endstack;
			}
		}
		else
		{
			ignore = endstack + 2;
		}
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
	}

}

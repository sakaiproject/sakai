/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.search.index.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.index.AnalyzerFactory;

/**
 * Snowball stemming algorithm
 * 
 * @author ieb
 */
public class SnowballAnalyzerFactory implements AnalyzerFactory
{
	private static final Log log = LogFactory.getLog(SnowballAnalyzerFactory.class);

	private static String[] stopWords = null;
	static
	{
		try
		{
			ArrayList<String> al = new ArrayList<String>();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(
							SnowballAnalyzerFactory.class
									.getResourceAsStream("/org/sakaiproject/search/component/bundle/stopwords.txt")));
			for (String line = br.readLine(); line != null; line = br.readLine())
			{
				al.add(line.trim());
			}
			br.close();
			stopWords = al.toArray(new String[0]);
		}
		catch (Exception ex)
		{
			log.error("Failed to load Stop words into Analyzer", ex);
		}
	}

	public Analyzer newAnalyzer()
	{
		return new StemAnalyzer();
	}

	public class StemAnalyzer extends Analyzer
	{
		StandardAnalyzer keywordAnalyzer;

		public StemAnalyzer()
		{
			keywordAnalyzer = new StandardAnalyzer(stopWords);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String,
		 *      java.io.Reader)
		 */
		public TokenStream tokenStream(String fieldName, Reader reader)
		{
			if (SearchService.FIELD_CONTENTS.equals(fieldName))
			{
				return new PorterStemFilter(keywordAnalyzer
						.tokenStream(fieldName, reader));
			}
			else
			{
				return keywordAnalyzer.tokenStream(fieldName, reader);
			}
		}
	}

}

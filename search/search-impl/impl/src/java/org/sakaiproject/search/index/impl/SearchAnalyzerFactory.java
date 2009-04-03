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

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.index.AnalyzerFactory;

/**
 * A Factory to generate search analyzers based on a configurable type setting.
 * The type of analyzer produced is selected by setting the analyzerFactory name
 * that will select one of the same name ijected into the currentAnalyzers Map.
 * 
 * @author ieb
 */
public class SearchAnalyzerFactory implements AnalyzerFactory
{
	private AnalyzerFactory runningAnalyzerFactory = null;

	private Map currentAnalyzers = null;

	private AnalyzerFactory defaultAnalyzerFactory;

	private String analyzerFactoryName;

	public void init()
	{
		runningAnalyzerFactory = (AnalyzerFactory) currentAnalyzers
				.get(analyzerFactoryName);
		if (runningAnalyzerFactory == null)
		{
			runningAnalyzerFactory = defaultAnalyzerFactory;
		}
	}

	public Analyzer newAnalyzer()
	{
		return defaultAnalyzerFactory.newAnalyzer();
	}

	/**
	 * @return Returns the analyzerFactoryName.
	 */
	public String getAnalyzerFactoryName()
	{
		return analyzerFactoryName;
	}

	/**
	 * @param analyzerFactoryName
	 *        The analyzerFactoryName to set.
	 */
	public void setAnalyzerFactoryName(String analyzerFactoryName)
	{
		this.analyzerFactoryName = analyzerFactoryName;
	}

	/**
	 * @return Returns the currentAnalyzers.
	 */
	public Map getCurrentAnalyzers()
	{
		return currentAnalyzers;
	}

	/**
	 * @param currentAnalyzers
	 *        The currentAnalyzers to set.
	 */
	public void setCurrentAnalyzers(Map currentAnalyzers)
	{
		this.currentAnalyzers = currentAnalyzers;
	}

	/**
	 * @return Returns the defaultAnalyzerFactory.
	 */
	public AnalyzerFactory getDefaultAnalyzerFactory()
	{
		return defaultAnalyzerFactory;
	}

	/**
	 * @param defaultAnalyzerFactory
	 *        The defaultAnalyzerFactory to set.
	 */
	public void setDefaultAnalyzerFactory(AnalyzerFactory defaultAnalyzerFactory)
	{
		this.defaultAnalyzerFactory = defaultAnalyzerFactory;
	}

	/**
	 * @return Returns the runningAnalyzerFactory.
	 */
	public AnalyzerFactory getRunningAnalyzerFactory()
	{
		return runningAnalyzerFactory;
	}

	/**
	 * @param runningAnalyzerFactory
	 *        The runningAnalyzerFactory to set.
	 */
	public void setRunningAnalyzerFactory(AnalyzerFactory runningAnalyzerFactory)
	{
		this.runningAnalyzerFactory = runningAnalyzerFactory;
	}

}

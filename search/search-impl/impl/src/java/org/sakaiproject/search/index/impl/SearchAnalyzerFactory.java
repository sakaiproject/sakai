/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.index.AnalyzerFactory;

/**
 * @author ieb
 *
 */
public class SearchAnalyzerFactory implements AnalyzerFactory
{
	private AnalyzerFactory runningAnalyzerFactory = null;
	private Map currentAnalyzers = null;
	private AnalyzerFactory defaultAnalyzerFactory;
	private String analyzerFactoryName;

	public void init() {
		runningAnalyzerFactory = (AnalyzerFactory)currentAnalyzers.get(analyzerFactoryName);
		if ( runningAnalyzerFactory == null ) {
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
	 * @param analyzerFactoryName The analyzerFactoryName to set.
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
	 * @param currentAnalyzers The currentAnalyzers to set.
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
	 * @param defaultAnalyzerFactory The defaultAnalyzerFactory to set.
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
	 * @param runningAnalyzerFactory The runningAnalyzerFactory to set.
	 */
	public void setRunningAnalyzerFactory(AnalyzerFactory runningAnalyzerFactory)
	{
		this.runningAnalyzerFactory = runningAnalyzerFactory;
	}

}

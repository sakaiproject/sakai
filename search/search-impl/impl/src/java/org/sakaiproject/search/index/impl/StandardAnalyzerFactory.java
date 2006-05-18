/**
 * 
 */
package org.sakaiproject.search.index.impl;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.sakaiproject.search.index.AnalyzerFactory;

/**
 * @author ieb
 *
 */
public class StandardAnalyzerFactory implements AnalyzerFactory
{

	public Analyzer newAnalyzer()
	{
		return new StandardAnalyzer();
	}

}

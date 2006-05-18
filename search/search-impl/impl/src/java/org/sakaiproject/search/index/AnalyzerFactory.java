/**
 * 
 */
package org.sakaiproject.search.index;

import org.apache.lucene.analysis.Analyzer;

/**
 * @author ieb
 *
 */
public interface AnalyzerFactory
{
	Analyzer newAnalyzer();
}

/**
 * 
 */
package org.sakaiproject.search.index.impl;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.sakaiproject.search.index.AnalyzerFactory;

/**
 * @author ieb
 *
 */
public class SnowballAnalyzerFactory implements AnalyzerFactory
{

	public Analyzer newAnalyzer()
	{
		return new StemAnalyzer();
	}
	
	public class StemAnalyzer extends Analyzer {
        StandardAnalyzer keywordAnalyzer = new StandardAnalyzer();

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.lucene.analysis.Analyzer#tokenStream(java.lang.String,
         *      java.io.Reader)
         */
        public TokenStream tokenStream(String fieldName, Reader reader) {
                if ("contents".equals(fieldName)) {
                        return new PorterStemFilter(keywordAnalyzer.tokenStream(
                                        fieldName, reader));
                } else {
                        return keywordAnalyzer.tokenStream(fieldName, reader);
                }
        }
}
	

}

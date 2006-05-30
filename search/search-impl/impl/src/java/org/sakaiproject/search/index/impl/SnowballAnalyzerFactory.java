/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
package org.sakaiproject.search.index.impl;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.index.AnalyzerFactory;

/**
 * Snowball stemming algorithm
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
                if (SearchService.FIELD_CONTENTS.equals(fieldName)) {
                        return new PorterStemFilter(keywordAnalyzer.tokenStream(
                                        fieldName, reader));
                } else {
                        return keywordAnalyzer.tokenStream(fieldName, reader);
                }
        }
}
	

}

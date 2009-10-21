package org.sakaiproject.search.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;

public class DidYouMeanParser {
	
	
	private String defaultField;
	private Directory spellIndexDirectory;
	private IndexReader origionalIndex;
	
	public DidYouMeanParser(String defaultField, Directory spellIndexDirectory) {
		this.defaultField = defaultField;
		this.spellIndexDirectory = spellIndexDirectory;
	}
	
	public DidYouMeanParser(String defaultField, Directory spellIndexDirectory, IndexReader origionalIndex) {
		this.defaultField = defaultField;
		this.spellIndexDirectory = spellIndexDirectory;
		this.origionalIndex = origionalIndex;
	}

	public Query parse(String queryString) throws ParseException {
		QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());
		queryParser.setDefaultOperator(QueryParser.Operator.AND);		
		return queryParser.parse(queryString);
	}

	public Query suggest(String queryString) throws ParseException {
		QuerySuggester querySuggester = new QuerySuggester(defaultField, new StandardAnalyzer());
		querySuggester.setDefaultOperator(QueryParser.Operator.AND);
		Query query = querySuggester.parse(queryString);
		return querySuggester.hasSuggestedQuery() ? query : null;
	}
	
	
	private class QuerySuggester extends QueryParser {
		private boolean suggestedQuery = false;
		public QuerySuggester(String field, Analyzer analyzer) {
			super(field, analyzer);
		}
		protected Query getFieldQuery(String field, String queryText) throws ParseException {
			// Copied from org.apache.lucene.queryParser.QueryParser
			// replacing construction of TermQuery with call to getTermQuery()
			// which finds close matches.
		    TokenStream source = getAnalyzer().tokenStream(field, new StringReader(queryText));
			Vector<String> v = new Vector<String>();
			Token t;

			while (true) {
				try {
					t = source.next();
				} catch (IOException e) {
					t = null;
				}
				if (t == null)
					break;
				v.addElement(t.term());
			}
			try {
				source.close();
			} catch (IOException e) {
				// ignore
			}

			if (v.size() == 0)
				return null;
			else if (v.size() == 1)
				return new TermQuery(getTerm(field, (String) v.elementAt(0)));
			else {
				PhraseQuery q = new PhraseQuery();
				q.setSlop(getPhraseSlop());
				for (int i = 0; i < v.size(); i++) {
					q.add(getTerm(field, (String) v.elementAt(i)));
				}
				return q;
			}
		}
		private Term getTerm(String field, String queryText) throws ParseException {
			SpellChecker spellChecker = null;
			try {
				spellChecker = new SpellChecker(spellIndexDirectory);
				if (spellChecker.exist(queryText)) {
					return new Term(field, queryText);
				}
				String[] similarWords = null;
				if (origionalIndex == null) {
					similarWords = spellChecker.suggestSimilar(queryText, 1);
				} else {
					similarWords = spellChecker.suggestSimilar(queryText, 1, origionalIndex, defaultField, true);
				}
				if (similarWords.length == 0) {
					return new Term(field, queryText);
				}			
				suggestedQuery = true;
				return new Term(field, similarWords[0]);
			} catch (IOException e) {
				throw new ParseException(e.getMessage());
			}
		}		
		public boolean hasSuggestedQuery() {
			return suggestedQuery;
		}
		
	}

}

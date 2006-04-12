/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.filter;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.filter.context.FilterContext;

/*
 * The paragraph filter finds any text between two empty lines and inserts a
 * <p/>
 * 
 * @author stephan @team sonicteam
 * 
 * @version $Id: ParagraphFilter.java 4158 2005-11-25 23:25:19Z
 *          ian@caret.cam.ac.uk $
 */

public class ParagraphFilter implements Filter, CacheFilter {

	private InitialRenderContext initialContext;

	private String breaksRE;

	private String replaceFirst;

	private String replaceLast;

	private String patternFristRE;

	private String patternLastRE;

	private String replaceAll;

	public String filter(String input, FilterContext context) {
		Pattern patternFirst = Pattern.compile(patternFristRE);
		Pattern patternLast = Pattern.compile(patternLastRE);
		Pattern patternBreaks = Pattern.compile(breaksRE);

		String[] p = patternBreaks.split(input);
				
		

		Matcher m = patternFirst.matcher(p[0]);
		StringBuffer sb = new StringBuffer();
		int ins = p[0].lastIndexOf(">") + 1;
		sb.append(p[0].substring(0, ins));
		sb.append(replaceFirst);
		sb.append(p[0].substring(ins));
		for (int i = 1; i < p.length - 1; i++) {
			sb.append(replaceAll);
			sb.append(p[i]);
		}
		ins = p[p.length - 1].indexOf("<") - 1;
		if (ins > 0) {
			sb.append(replaceAll);
			sb.append(p[p.length - 1].substring(0, ins));
			sb.append(replaceLast);
			sb.append(p[p.length - 1].substring(ins));
		} else if (ins == 0) {
			sb.append(replaceLast);
			sb.append(p[p.length - 1]);
		} else {
			sb.append(p[p.length - 1]);
			sb.append(replaceLast);
		}
				
		
		
		
		return sb.toString();
	}

	public String[] replaces() {
		return FilterPipe.NO_REPLACES;
	}

	public String[] before() {
		return FilterPipe.EMPTY_BEFORE;
	}

	public void setInitialContext(InitialRenderContext context) {
		initialContext = context;
		ResourceBundle outputMessages = getOutputBundle();
		ResourceBundle inputMessages = getInputBundle();

		breaksRE = inputMessages.getString("filter.paragraph.breaks.match");
		replaceAll = outputMessages.getString("filter.paragraph.breaks.print");
		replaceFirst = outputMessages.getString("filter.paragraph.first.print");
		replaceLast = outputMessages.getString("filter.paragraph.last.print");
		patternFristRE = inputMessages
				.getString("filter.paragraph.first.match");
		patternLastRE = inputMessages.getString("filter.paragraph.last.match");
	}

	public String getDescription() {
		return "Hand Coded paragraph filter";
	}

	protected ResourceBundle getInputBundle() {
		Locale inputLocale = (Locale) initialContext
				.get(RenderContext.INPUT_LOCALE);
		String inputName = (String) initialContext
				.get(RenderContext.INPUT_BUNDLE_NAME);
		return ResourceBundle.getBundle(inputName, inputLocale);
	}

	protected ResourceBundle getOutputBundle() {
		String outputName = (String) initialContext
				.get(RenderContext.OUTPUT_BUNDLE_NAME);
		Locale outputLocale = (Locale) initialContext
				.get(RenderContext.OUTPUT_LOCALE);
		return ResourceBundle.getBundle(outputName, outputLocale);
	}
	

}
/*
 * private static Log log = LogFactory.getLog(ParagraphFilter.class); private
 * String printFirst; private String printNext; private Pattern matchPattern;
 * 
 * protected String getLocaleKey() { return "filter.paragraph"; }
 * 
 * protected boolean isSingleLine() { return false; } public String
 * filter(String input, FilterContext context) { String result = input;
 * 
 * 
 * System.err.println("Using "+matchPattern.getRegex()+" replacing with
 * "+printFirst+" then "+printNext); try { Matcher matcher =
 * Matcher.create(result, matchPattern); Substitution s = new Substitution() {
 * boolean firstMatch = true; public void handleMatch(StringBuffer buffer,
 * MatchResult result) { if ( firstMatch ) { buffer.append(printFirst);
 * firstMatch = false; } else { buffer.append(printNext); } } }; result =
 * matcher.substitute(s); // Util.substitute(matcher, p, new
 * Perl5Substitution(s, interps), result, limit); } catch (Exception e) {
 * //log.warn("<span class=\"error\">Exception</span>: " + this + ": " + e);
 * log.warn("Exception for: " + this+" "+e); } catch (Error err) { //log.warn("<span
 * class=\"error\">Error</span>: " + this + ": " + err); log.warn("Error for: " +
 * this); err.printStackTrace(); }
 * 
 * return result; }
 * 
 * public void setInitialContext(InitialRenderContext context) {
 * super.setInitialContext(context); clearRegex();
 * 
 * ResourceBundle outputMessages = getOutputBundle(); ResourceBundle
 * inputMessages = getInputBundle();
 * 
 * String match = inputMessages.getString(getLocaleKey()+".match"); printFirst =
 * outputMessages.getString(getLocaleKey()+".print.1"); printNext =
 * outputMessages.getString(getLocaleKey()+".print.2"); try {
 * org.radeox.regex.Compiler compiler = org.radeox.regex.Compiler.create();
 * compiler.setMultiline(isSingleLine() ? RegexReplaceFilter.SINGLELINE :
 * RegexReplaceFilter.MULTILINE); matchPattern = compiler.compile(match); }
 * catch (Exception e) { log.warn("bad pattern: " + match + " -> " +
 * printFirst+" "+e); } } }
 */
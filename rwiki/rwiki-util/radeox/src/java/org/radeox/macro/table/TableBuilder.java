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

package org.radeox.macro.table;

import java.util.StringTokenizer;

/**
 * Built a table from a string
 * 
 * @author stephan
 * @version $Id$
 */

public class TableBuilder
{
	private static final String BAR = "|";
	private static final String NL = "\n";
	private static final String OPEN_LINK = "[";
	private static final String CLOSE_LINK = "]";
	private static final String TOKENS_STRING = BAR + NL + OPEN_LINK + CLOSE_LINK;
	
	private Table table = new Table();
	private StringTokenizer tokenizer;
	private String[] token;

	private TableBuilder(String content) {
		tokenizer = new StringTokenizer(content, TOKENS_STRING, true);
		
		token = new String[5];
		for (int i = 0; i <token.length; i++) {
			token[i] = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
		}

	}
	
	public static Table build(String content)
	{
		TableBuilder builder = new TableBuilder(content);
		builder.build();
		return builder.getTable();
	}

	
	public void build() {
		String lastToken = null;
		while (token[0] != null)
		{
			if ("\n".equals(token[0]))
			{
				if (null != lastToken) {
					table.newRow();
				}
				
			}
			else if ("|".equals(token[0])) {
				table.newCell();
			}
			else if ("[".equals(token[0])) {
				if (isText(token[1]) && isBar(token[2]) && isText(token[3]) && isCloseLink(token[4])) {
					for (int i = 0; i < 4; i++) {
						table.addText(token[0]);
						step();
					}
					table.addText(token[0]);
				} else {
					table.addText(token[0]);
				}
			}
			else 
			{
				table.addText(token[0]);
				//table.addCell(token);
			}
			lastToken = token[0];
			step();
		}
		if (!"\n".equals(lastToken)) {
			table.newRow();
		}
		
	}

	private void step() {
		for (int i = 1; i < token.length; i++) {
			token[i - 1] = token[i]; 
		}
		token[token.length - 1] = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
	}
	
	private static boolean isBar(String token) {
		return BAR.equals(token);
	}
	
	private static boolean isOpenLink(String token) {
		return OPEN_LINK.equals(token);
	}

	private static boolean isCloseLink(String token) {
		return CLOSE_LINK.equals(token);
	}

	private static boolean isNewLine(String token) {
		return NL.equals(token);
	}
	
	private static boolean isText(String token) {
		for (int i = 0 ; i < TOKENS_STRING.length(); i++) {
			if (("" + TOKENS_STRING.charAt(i)).equals(token)) {
				return false;
			}
		}
		return true;
	}

	
	public Table getTable() {
		return table;
	}
	
}

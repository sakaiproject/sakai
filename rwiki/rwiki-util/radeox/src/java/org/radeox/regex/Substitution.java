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

package org.radeox.regex;

/*
 * Called with a MatchResult which is substituted @author stephan @team
 * sonicteam
 * 
 * @version $Id$
 */

public interface Substitution
{
	/**
	 * When substituting matches in a matcher, the handleMatch method of the
	 * supplied substitution is called with a MatchResult. This method then does
	 * something with the match and replaces the match with some output, like
	 * replace all 2*2 with (2*2 =) 4.
	 * 
	 * @param buffer
	 *        StringBuffer to append the output to
	 * @param result
	 *        MatchResult with the match
	 */
	public void handleMatch(StringBuffer buffer, MatchResult result);
}
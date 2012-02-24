/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.parser;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.api.parser.EventParserTipFactory;
import org.xml.sax.Attributes;

public class EventParserTipFactoryImpl implements EventParserTipFactory, ObjectCreationFactory {

	public EventParserTip createEventParserTip() {
		return new EventParserTip();
	}
	
	public Object createObject(Attributes attributes) throws Exception {
		String _for = attributes.getValue("for");
		String _separator = attributes.getValue("separator");
		String _index = attributes.getValue("index");

		if(_for == null){ throw new Exception("Mandatory 'for' attribute not present on eventParserTip tag."); }
		if(_separator == null){ throw new Exception("Mandatory 'separator' attribute not present on eventParserTip tag."); }
		if(_index == null){ throw new Exception("Mandatory 'index' attribute not present on eventParserTip tag."); }
		EventParserTip e = new EventParserTip(_for, _separator, _index);
		return e;
	}

	public Digester getDigester() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDigester(Digester arg0) {
		// TODO Auto-generated method stub

	}

}

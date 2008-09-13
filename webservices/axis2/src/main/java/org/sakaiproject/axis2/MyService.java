/**
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.axis2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;

public class MyService {
	
	public OMElement sayhi(OMElement el) throws XMLStreamException {
		el.build();
		el.detach();
			
		OMElement child = el.getFirstChildWithName(new QName("name"));
		String person = child.getText();
		
		OMFactory factory = OMAbstractFactory.getOMFactory();
		
		OMElement reply = factory.createOMElement(new QName("reply"));
		reply.setText("Hello, " + person);
		
		return reply;
	}

}

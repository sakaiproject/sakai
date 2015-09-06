/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on 31 Oct 2006
 */
package org.sakaiproject.rsf.producers;

import org.sakaiproject.rsf.copies.Web;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIVerbatim;

/**
 * Emits a Javascript block containing a function call which may be used
 * whenever any AJAX/DOM manipulation has altered the size of the rendered pane.
 * NB - should detect a non-frames portal and emit a noop.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */

public class FrameAdjustingProducer {
	public static final String deriveFrameTitle(String placementID) {
		return Web.escapeJavascript("Main" + placementID);
	}

	private ToolManager toolmanager;

	public void setToolManager(ToolManager toolmanager) {
		this.toolmanager = toolmanager;
	}

	public void fillComponents(UIContainer tofill, String ID, String functionname) {
		UIVerbatim.make(tofill, ID, "\n<!-- \n\tfunction " + functionname + "()" + " {\n\t\tsetMainFrameHeight('"
				+ deriveFrameTitle(toolmanager.getCurrentPlacement().getId()) + "');\n\t\t}\n//-->\n");
	}

}

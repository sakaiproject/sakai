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
package org.sakaiproject.rsf.evolverimpl;

import uk.org.ponder.beanutil.BeanGetter;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.util.RSFUtil;

import java.util.Date;

import org.sakaiproject.rsf.util.ISO8601FieldDateTransit;

/**
 * Unlike the original date formatter we no longer do the parsing on the client
 * side as we just expect a ISO8601 formatted date from all languages.
 */
public class SakaiFieldDateInputEvolver implements FormatAwareDateInputEvolver {

	// This is the RSF ID that will be looked up in the templates to decide
	// which one to use.
	public static final String COMPONENT_ID = "sakai-date-field-input:";

	private String style = DATE_INPUT;
	// The transit base is the bean looked up to convert data between the
	// request and model.
	// In this case we need to translate between a string and date.
	private String transitBase = "iso8601DateTransit";

	private BeanGetter rbg;

	public void setInvalidDateKey(String s) {
	}

	public void setRequestBeanGetter(BeanGetter rbg) {
		this.rbg = rbg;
	}

	public UIJointContainer evolveDateInput(UIInput toEvolve, Date value) {
		// Pull in the template
		UIJointContainer togo = new UIJointContainer(toEvolve.parent, toEvolve.ID, COMPONENT_ID);
		// Remove the existing component from the tree
		toEvolve.parent.remove(toEvolve);
		String transitBean = transitBase + "." + togo.getFullID();

		// Need ISO9601 support.
		ISO8601FieldDateTransit transit = (ISO8601FieldDateTransit) rbg.getBean(transitBean);
		if (value == null) {
			// The UIInput we're evolving must have a OTP bean for this to work.
			value = (Date) rbg.getBean(toEvolve.valuebinding.value);
		}
		if (value != null) {
			transit.setDate(value);
		}

		String ttb = transitBean + ".";

		UIOutput display = UIOutput.make(togo, "display");

		UIInput field = UIInput.make(togo, "iso8601", ttb + "ISO8601", transit.getISO8601());
		field.mustapply = true;

		// Bind the value back through to the transitBase.
		// This generates a custom hidden HTML
		UIForm form = RSFUtil.findBasicForm(togo);
		form.parameters.add(new UIELBinding(toEvolve.valuebinding.value, new ELReference(ttb + "date")));

		UIInitBlock.make(togo, "init-date", "rsfDatePicker",
				new Object[] { display.getFullID(), field.getFullID(),
						// If we just supply a boolean it is output as a string
						// which doesn't work.
						(style.equals(DATE_TIME_INPUT) || style.equals(TIME_INPUT)) ? "1" : "0" });

		return togo;
	}

	public UIJointContainer evolveDateInput(UIInput toEvolve) {
		return evolveDateInput(toEvolve, null);
	}

	public void setInvalidTimeKey(String s) {

	}

	public void setStyle(String s) {
		this.style = s;
	}

}

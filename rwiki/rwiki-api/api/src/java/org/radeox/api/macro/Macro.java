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

package org.radeox.api.macro;

import java.io.IOException;
import java.io.Writer;

import org.radeox.api.engine.context.InitialRenderContext;

/*
 * Class that implements base functionality to write macros @author stephan
 * 
 * @version $Id$
 */

public interface Macro extends Comparable
{
	/**
	 * Get the name of the macro. This is used to map a macro in the input to
	 * the macro which should be called. The method has to be implemented by
	 * subclassing classes.
	 * 
	 * @return name Name of the Macro
	 */
	public String getName();

	/**
	 * Get a description of the macro. This description explains in a short way
	 * what the macro does
	 * 
	 * @return description A string describing the macro
	 */
	public String getDescription();

	/**
	 * Get a description of the paramters of the macro. The method returns an
	 * array with an String entry for every parameter. The format is {"1:
	 * description", ...} where 1 is the position of the parameter.
	 * 
	 * @return description Array describing the parameters of the macro
	 */
	public String[] getParamDescription();

	public void setInitialContext(InitialRenderContext context);

	/**
	 * Execute the macro. This method is called by MacroFilter to handle macros.
	 * 
	 * @param writer
	 *        A write where the macro should write its output to
	 * @param params
	 *        Macro parameters with the parameters the macro is called with
	 */
	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException;
}

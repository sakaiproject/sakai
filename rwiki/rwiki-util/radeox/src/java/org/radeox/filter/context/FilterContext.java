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

package org.radeox.filter.context;

import org.radeox.api.engine.context.RenderContext;
import org.radeox.api.macro.MacroParameter;

/**
 * FilterContext stores basic data for the context filters are called in.
 * FilterContext is used to allow filters be called in different enviroments,
 * inside SnipSnap or outside. Special enviroments should implement
 * FilterContext
 * 
 * @author Stephan J. Schmidt
 * @version $Id: FilterContext.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public interface FilterContext
{
	public MacroParameter getMacroParameter();

	public void setRenderContext(RenderContext context);

	public RenderContext getRenderContext();
}

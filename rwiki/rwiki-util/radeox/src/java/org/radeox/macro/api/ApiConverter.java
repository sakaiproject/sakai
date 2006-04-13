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

package org.radeox.macro.api;

import java.io.IOException;
import java.io.Writer;

/**
 * Converts a class name to an API url
 * 
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public interface ApiConverter
{
	/**
	 * Converts a class name to an url and adds the url to an Writer. The url
	 * usually shows som API information about the class e.g. for Java classes
	 * this points to the API documentation on the Sun site.
	 * 
	 * @param writer
	 *        Writer to add the class url to
	 * @param className
	 *        Namee of the class to create pointer for
	 */
	public void appendUrl(Writer writer, String className) throws IOException;

	/**
	 * Set the base Url for the Converter. A converter creates an API pointer by
	 * combining an base url and the name of a class.
	 * 
	 * @param baseUrl
	 *        Url to use when creating an API pointer
	 */
	public void setBaseUrl(String baseUrl);

	/**
	 * Get the base Url for the Converter. A converter creates an API pointer by
	 * combining an base url and the name of a class.
	 * 
	 * @return baseUrl Url the converter uses when creating an API pointer
	 */
	public String getBaseUrl();

	/**
	 * Returns the name of the converter. This is used to configure the BaseUrls
	 * and associate them with a concrete converter.
	 * 
	 * @return name Name of the Converter, e.g. Java12
	 */
	public String getName();
}

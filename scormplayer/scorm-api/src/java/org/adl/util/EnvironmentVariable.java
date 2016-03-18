/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you
** ("Licensee") a non-exclusive, royalty free, license to use, modify and
** redistribute this software in source and binary code form, provided that
** i) this copyright notice and license appear on all copies of the software;
** and ii) Licensee does not utilize the software in a manner which is
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
*******************************************************************************/
package org.adl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;

// xerces imports

// adl imports

/**
 * <strong>Filename:</strong><br>
 * EnvironmentVariable.java<br><br>
 *
 * <strong>Description:</strong><br>
 * A <code>EnvironmentVariable</code> is used to access a specified
 * environment variable.
 
 * @author ADL Technical Team
 */
public final class EnvironmentVariable {
	/**
	 * Retrieves the value of the specified environment variable.
	 *
	 * @param iKey   Name of the environment variable.
	 *
	 * @return Value of the specified environment variable.
	 */
	public static String getValue(String iKey) {
		String value = "";

		try {
			Process p;
			String osName = System.getProperty("os.name");

			if ((osName.equalsIgnoreCase("Windows 95")) || (osName.equalsIgnoreCase("Windows 98")) || (osName.equalsIgnoreCase("Windows Me"))) {
				p = Runtime.getRuntime().exec("command.com /c echo %" + iKey + "%");
			} else {
				p = Runtime.getRuntime().exec("cmd.exe /c echo %" + iKey + "%");
			}

			p.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			value = br.readLine();

			if (StringUtils.startsWith(value, "%")) {
				value = "";
			}

			br.close();
			p.destroy();
		} catch (IOException ioe) {
			System.out.println("Could not read environment variable key " + iKey);
		} catch (InterruptedException ie) {
			System.out.println("The process has been interrupted");
		}

		return value;
	}
}
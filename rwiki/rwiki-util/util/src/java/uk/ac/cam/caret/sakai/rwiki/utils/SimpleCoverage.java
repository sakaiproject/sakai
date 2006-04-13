/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A really simple coverage utility, prints a record of the calling method
 * 
 * @author ieb
 */
public class SimpleCoverage
{
	private static Log logger = LogFactory.getLog(SimpleCoverage.class);

	private static long last = System.currentTimeMillis();

	public static void cover(String message)
	{
		long now = System.currentTimeMillis();
		String elapsed = String.valueOf(now - last) + " ms ";
		last = now;
		Exception e = new Exception();
		StackTraceElement[] ste = e.getStackTrace();
		String method = ste[1].getMethodName();
		String file = ste[1].getFileName();
		int line = ste[1].getLineNumber();
		String className = ste[1].getClassName();
		logger.info("###### " + elapsed + " " + message + " SimpleCoverage at "
				+ className + "." + method + " (" + file + ":" + line + ") ");
	}

	public static void cover()
	{
		long now = System.currentTimeMillis();
		String elapsed = String.valueOf(now - last) + " ms ";
		Exception e = new Exception();
		StackTraceElement[] ste = e.getStackTrace();
		String method = ste[1].getMethodName();
		String file = ste[1].getFileName();
		int line = ste[1].getLineNumber();
		String className = ste[1].getClassName();
		logger.info("###### " + elapsed + " SimpleCoverage at " + className
				+ "." + method + " (" + file + ":" + line + ") ");
	}

	public static void covered()
	{
	}

}

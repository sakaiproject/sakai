/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.tool.jsf;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author nfernandes
 *
 */
public class ChartServlet extends HttpServlet {
	private static Log			LOG						= LogFactory.getLog(ChartServlet.class);
			
	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.info("doGet()");
		doAction(request, response);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LOG.info("doPost()");
		doAction(request, response);
	}
	
	private void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Enumeration en = request.getParameterNames();
		while(en.hasMoreElements()){
			String attr = (String)en.nextElement();
			LOG.info("Parameter "+attr+": "+request.getParameter(attr));
		}
		
//		JFreeChart chart = null;		
//		chart = ChartFactory.createPieChart("", getTestPieDataSet(), true, true, false);
//
//		OutputStream out = response.getOutputStream();
//		try {
//			response.setContentType("image/png");
//			out.flush();
//			ChartUtilities.writeChartAsPNG(out, chart, 300,200);
//		} catch (Exception e) {
//			LOG.error("Error occured while writing image.",e);			
//		} finally {
//			out.close();
//			//emptySession(session, id);
//		}
	}
	
//	private DefaultPieDataset getTestPieDataSet() {
//		DefaultPieDataset pieDataSet = new DefaultPieDataset();
//		pieDataSet.setValue("A",52);
//		pieDataSet.setValue("B", 18);
//		pieDataSet.setValue("C", 30);
//		return pieDataSet;
//	}
}

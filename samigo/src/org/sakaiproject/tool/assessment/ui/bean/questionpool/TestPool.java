/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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
package org.sakaiproject.tool.assessment.ui.bean.questionpool;

public class TestPool
{

 	private String  name;
 	private String creator;
 	private String lastModified;
 	private int noQuestions;
 	private int nosubpools;
 	private String trid;
 	private String level;

	/**
	 * noarg constructor, required for managed beans.
	 */
	public TestPool(String name, String creator, String lastModified, int no1, int no2 , String trowid, String level)
	{
		this.name= name;
		this.creator= creator;
		this.lastModified= lastModified;
		this.noQuestions= no1;
		this.nosubpools = no2;
		this.trid= trowid;
		this.level= level;
	}
	public String getLevel()
	{
		return level;
	}

	public void setLevel(String param)
	{
		level= param;
	}
	public String getTrid()
	{
		return trid;
	}

	public void setTrid(String param)
	{
		trid = param;
	}


	//Property
	public String getName()
	{
		return name;
	}

	//Property
	public String getCreator()
	{
		return creator;
	}

	//Property
	public String getLastModified()
	{
		return lastModified;
	}
       //Property
        public int getNoQuestions()
        {
                return noQuestions;
        }
       //Property
        public int getNosubpools()
        {
                return nosubpools;
        }


	//Property
	public void setName(String string)
	{
		name = string;
	}

	//Property
	public void setCreator(String param)
	{
		creator = param;
	}

	//Property
	public void setLastModified(String param)
	{
		lastModified= param;
	}
        public void setNoQuestions(int param)
        {
                noQuestions= param;
        }
        public void setNosubpools(int param)
        {
                nosubpools = param;
        }


}


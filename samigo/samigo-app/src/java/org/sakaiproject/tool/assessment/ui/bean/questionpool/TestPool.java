/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool;

/**
 * This is a single item on a simple page.
 * 
 * @author jeney
 * 
 */
public interface SimplePageItem {

	public static final int RESOURCE = 1;
	public static final int PAGE = 2;
	public static final int ASSIGNMENT = 3;
	public static final int ASSESSMENT = 4;
	public static final int TEXT = 5;
	public static final int URL = 6;
	public static final int MULTIMEDIA = 7;
	public static final int FORUM = 8;

    // sakaiId used for an item copied from another site with no real content
        public static final String DUMMY = "/dummy";


    /* WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. 
       If you change this, make very sure you change the archive and restore code in LessonBuilderEntityProducer
       and the copy code here.  Also copyItem in SimplePageBean.
       WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. WARNING. */

    public long getId();

    public long getPageId();

    public int getSequence();

    public int getType();

    public String getSakaiId();

    public String getName();

    public String getHtml();

    public String getDescription();

    public void setId(long i);

    public void setPageId(long s);

    public void setSequence(int o);

    public void setType(int t);

    public void setSakaiId(String s);

    public void setName(String s);

    public void setHtml(String html);

    public void setDescription(String desc);

    public void setHeight(String height);

    public String getHeight();

    public void setWidth(String width);

    public String getWidth();

    public void setAlt(String alt);

    public String getAlt();

    public void setNextPage(Boolean n);

    public boolean getNextPage();

    public void setFormat(String f);

    public String getFormat();

    public void setRequired(boolean required);

    public boolean isRequired();

    public void setAlternate(boolean alternate);

    public boolean isAlternate();

    public void setPrerequisite(boolean prerequisite);

	/**
	 * 
	 * @return Whether or not this is unavailable until all previous requirements are fulfilled.
	 */
    public boolean isPrerequisite();

    public void setSubrequirement(boolean requirement);

    public boolean getSubrequirement();

    public void setRequirementText(String requirementText);

    public String getRequirementText();

    public String getURL();

}

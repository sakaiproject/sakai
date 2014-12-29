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

package org.sakaiproject.tool.assessment.data.dao.assessment;
import java.io.Serializable;

public class FavoriteColChoicesItem
        implements Serializable {
                /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
				private Long favoriteItemId;
                private FavoriteColChoices favoriteChoice;
                private String choiceText;
                private Integer sequence;

        public FavoriteColChoicesItem() {}
      
        public FavoriteColChoicesItem(String choiceText, Integer sequence){
        	this.choiceText = choiceText;
        	this.sequence = sequence;
        }
        public FavoriteColChoicesItem(FavoriteColChoices favoriteChoice, String choiceText, Integer sequence){
        	this.favoriteChoice = favoriteChoice;
        	this.choiceText = choiceText;
        	this.sequence = sequence;
        }
        public Long getFavoriteItemId(){
        	return favoriteItemId;
        }
        public void setFavoriteItemId(Long id){
        	this.favoriteItemId = id;
        }
        public FavoriteColChoices getFavoriteChoice(){
        	return this.favoriteChoice;
        }
        public void setFavoriteChoice(FavoriteColChoices parent){
        	this.favoriteChoice = parent;
        }
        public String getChoiceText(){
        	return choiceText;
        }
        public void setChoiceText(String text){
        	this.choiceText = text;
        }
        public Integer getSequence(){
        	return sequence;
        }
        public void setSequence(Integer sequence){
        	this.sequence = sequence;
        }

    
}       
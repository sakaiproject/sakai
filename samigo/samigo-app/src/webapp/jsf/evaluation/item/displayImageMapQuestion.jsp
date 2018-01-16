<%-- $Id: $
include file for displaying fill in the numeric questions
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->

<samigo:script path="/../library/webjars/jquery/1.12.4/jquery.min.js"/>
<samigo:script path="/js/selection.author.preview.js"/>
<samigo:script path="/js/selection.student.preview.js"/>
<samigo:stylesheet path="/css/imageQuestion.author.css"/>
<samigo:stylesheet path="/css/imageQuestion.student.css"/>

<f:verbatim>
<script type="text/JavaScript">	
	jQuery(window).load(function(){
			
		$('input:hidden[id^=hiddenSerializedCoords_]').each(function(){
			var myregexp = /hiddenSerializedCoords_(\d+)_(\d+)/
			var matches = myregexp.exec(this.id);
			var sequence = matches[1];
			var label = matches[2];
			
			var serializedImageCoords = this.id.replace('hiddenSequence', 'hiddenSerializedCoords').replace(/:/g, '\\:');
			
			var sel = new selectionAuthor({selectionClass: 'selectiondiv', textClass: 'textContainer'}, 'imageMapContainer');
			try {
				sel.setCoords(jQuery.parseJSON(this.value));
				sel.setText(label);
			}catch(err){}
			
		});	
	});
	
	var selectionList = [];
	function loadAnswer(answerString) {
		for(var i=0; i<selectionList.length; i++)
		{
			selectionList[i].remove();
			delete selectionList[i];
		}
		selectionList = [];
		
		var tokens = answerString.split('<br/>');
		for(var i=0; i<tokens.length; i++)
		{
			try
			{
				var label = tokens[i].substring(0, tokens[i].indexOf(':'));
				var coords = tokens[i].substring(tokens[i].indexOf(':')+1);
				var newSelection = new selectionStudent('pointerClass', 'imageMapContainer');
				newSelection.setCoords(jQuery.parseJSON(coords));
				newSelection.setText(label);
				selectionList.push(newSelection);
			}catch(err){}
		}
	}
</script>
</f:verbatim>

<h:outputText value="#{question.text}"  escape="false"/>

<h:dataTable value="#{question.itemTextArraySorted}" var="itemText">
 <h:column>
   <h:outputText value="#{itemText.sequence}#{evaluationMessages.dot} #{itemText.text}" escape="false" />
   <h:dataTable value="#{itemText.answerArraySorted}" var="answer">     
     <h:column> 
	   <h:outputText escape="false" value="<input type='hidden' id='hiddenSerializedCoords_#{question.sequence}_#{itemText.sequence}' value='#{answer.text}' />" /> 
     </h:column>
   </h:dataTable>
 </h:column>
</h:dataTable>

<f:verbatim>  
	<div id="imageMapContainer" class='authorImageContainer'>
		<img id='img' src='</f:verbatim><h:outputText value="#{question.imageMapSrc}" /><f:verbatim>' />
	</div>
</f:verbatim>
<%@ include file="/jsf/evaluation/item/displayTags.jsp" %>

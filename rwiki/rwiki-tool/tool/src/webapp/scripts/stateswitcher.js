/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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


/****************************************************************************/
// Start of FCK Editor integration functions
/****************************************************************************/

// Global variables containing temporary values of content so changes are detected
var tempWikiContent="";
var tempFCKContent="";

// Variable used to discard the replacement of FCK content in asynchronous calls to server
var calledFromFck=false;

// Variable used to activate the tool init behaviour
var pageInit=true;

//Variable for wysiwyg mode
var wysiwyg=false;

//set wysiwyg mode or not
function setWysiwyg(bool)
{
	wysiwyg=bool;
}

// Remove uncompatible and unnecessary markup from a server-generated HTML content
function cleanHTML(inputString)
{
	  // Remove heading attibutes from <h3 class="heading-h2">Text</h3>
	  var headingText = "heading-h";
	  var closingTagText = "</h";
	  var beginHeadingPos = inputString.indexOf(headingText);
	  var beginTagPos = beginHeadingPos-9; // Up to the <h
	  var endHeadingPos = beginHeadingPos+headingText.length; //right after the heading Text
	  var start=beginTagPos-2;
	  var startExtract = 0;
	  var endExtract = 0;
	  var newText="";
	  
	  // Iterate on all to generate HTML without the heading attributes
	  // This code will be cleaned soon to be more efficient and readable
	  while(beginHeadingPos != -1)
	  {
	      // Creates the replacement tag for the heading
	      var headerType = inputString.substring(endHeadingPos,endHeadingPos+1);
	      var replacedString = inputString.substring(start,beginTagPos)+headerType+">";
	      var closingTagPos = (inputString.substring(endHeadingPos, inputString.length)).indexOf(closingTagText)+endHeadingPos;
	      replacedString += inputString.substring(endHeadingPos+3,closingTagPos)+closingTagText+headerType+">";
	      
	      endExtract=start;
	      var firstPart = inputString.substring(startExtract,endExtract); //everything before the new heading text
	      startExtract=closingTagPos+5;
	      newText += firstPart + replacedString;
	      
	      var lastPart =  inputString.substring(closingTagPos+5,inputString.length); //everything after the new heading text
	      beginHeadingPos = lastPart.indexOf(headingText);
	      
	      if(beginHeadingPos!=-1) 
	      	beginHeadingPos=beginHeadingPos+inputString.length-lastPart.length;
	      else
	      	newText+=lastPart;
	      	
	      endHeadingPos = beginHeadingPos+headingText.length; //right after the heading Text
	      beginTagPos = beginHeadingPos-9;
	      start=beginTagPos-2; 
	  }
	  
	  //Finally cleans the HTML content from heading !
	  inputString = newText;
	  
	  // Removes class attributes
	  inputString = inputString.replace(/<(\w[^>]*) class=([^ |>]*)([^>]*)/gi, "<$1$3") ;
	  
	  // Removes ending paragraph
		var srcPos  = inputString.lastIndexOf("<br/>\r\n\r\n");
		if (srcPos != -1){
			inputString = inputString.substring(0, srcPos);
		}
		
		// Removes anchors
		inputString = inputString.replace(/<a name=([^ |>]*)([^>]*)><\/a>/gi, "") ;
		
		return inputString;
}

// replace HTML links with Wiki
function replaceLink(selection) 
{
	  var RegexLink = /<a [^<]*>*<\/a>/gi;  //Find all links
	  var RegexLinkUrl = /<a[^<]*>/gi;  //Find the link url
	  var RegexExtLink = /http:\/\/|ftp:\/\//gi; //test the internal, external link
	  var RegexWorkLink = /worksite:/gi; //remove worksite: from link
	  
	  var escRep = "";
	  
		var resultat = selection.match(RegexLink)!= null? selection.match(RegexLink):[];

		for(i=0;i<resultat.length; i++) 
	  {
	    var result = resultat[i];
		  
			var resultatLink = result.match(RegexLinkUrl)!= null? result.match(RegexLinkUrl):[];
	
			var linkPos  = resultatLink[0].indexOf("href=\"");
			var linklimPos = resultatLink[0].lastIndexOf("\"");
			var textPos  = result.indexOf(">");
			var textlimPos = result.lastIndexOf("<");
			var anchorPos = result.indexOf("class=\"anchorpoint\"");
			
			if( linkPos != -1 ) {
			  var linkText = result.substring(textPos + 1, textlimPos);
			  var linkUrl = resultatLink[0].substring(linkPos+6, linklimPos);
			  var linkUrl  = linkUrl.replace(RegexWorkLink,escRep);
			  var linkFileName = linkUrl.lastIndexOf("/");
			  var strFileName = linkUrl.substring(linkFileName,linkUrl.length);
			  if(linkUrl.indexOf("http://")==-1 && linkUrl.indexOf("ftp://")==-1 && linkUrl.indexOf("https://")==-1 && linkUrl.indexOf("news://")==-1) { //if is an internal link
			     var newlink = "{link:"+linkText+"|worksite:"+strFileName+"}";
			  } 
			  else {
		         var newlink = "{link:text="+linkText+"|url="+linkUrl+"}";
		    }
			  selection = selection.replace(result,newlink);
			} 
				else if(anchorPos != -1) {
			    selection = selection.replace(result,escRep);
			}
	  }
	  return selection;
}

// replace HTML images with Wiki
function replaceImg(selection) 
{
	  var RegexImgTag = /<img[^<]*>/gi;  //Find the image tag
	  var imgExp = "\/icklearrow.gif"; //Image for rows
	  var escRep = "";

	  var resultat = selection.match(RegexImgTag)!= null? selection.match(RegexImgTag):[];

	  for(i=0;i<resultat.length; i++) 
	  {
	  	var result = resultat[i];
	  	var srcPos  = result.indexOf("src=\"");
	  	if( srcPos != -1 ) var imgTxt = result.substring(srcPos + 5, result.length);
	  	
	  	srcPos  = imgTxt.indexOf("\"");
		
			if( srcPos != -1 ) 
			{
			  var imgSrc = imgTxt.substring(0, srcPos);
			  srcPos  = imgSrc.lastIndexOf("\/");
			  var rowpos = imgSrc.indexOf(imgExp);
			  if (rowpos != -1){
	       	  var newImg = escRep;
	      } 
	      else if(srcPos != -1) {
			  	  var imgName = imgSrc.substring(srcPos, imgSrc.length);
		          var newImg = "{image:worksite:"+imgName+"|"+imgName+"}";
		    } 
		    else {
		          var newImg = "{image:worksite:"+imgSrc+"|"+imgSrc+"}";
		    }
			  selection = selection.replace(result,newImg);
			}
    }
	  return selection;
}

// replace HTML lists with Wiki
function replaceList(selection) {
	  var RegexListOTerm = /<ol[^<]*>*<\/ol>/gi; //find lists
	  var RegexListUTerm = /<ul[^<]*>*<\/ul>/gi;
	  var RegexListB = /<li[^<]*>/gi;
	  var RegexListE = /<\/li>\s?/gi;
	  var RegexListTerms = /<ul[^<]*>|<ol[^<]*>|<\/ol>|<\/ul>/gi;

	  var wikiListRep = 'SSS';
	  var wikiListRegRep = /SSS/gi;

	  var wikiListp = '* ';
	  var wikiListe = '# ';
	  var wikiEndParag = '\n';
	  var wikiesc = '';
	  var selectionCo = selection.replace(RegexListB,wikiListRep);
	  selectionCo = selectionCo.replace(RegexListE,wikiEndParag);
	  
	  var resultO = selectionCo.match(RegexListOTerm)!= null? selectionCo.match(RegexListOTerm):[];
	  var resultU = selectionCo.match(RegexListUTerm)!= null? selectionCo.match(RegexListUTerm):[];
	  
	  for(i=0;i<resultO.length; i++) {
	    var result = resultO[i];
        var newresult = result.replace(wikiListRegRep,wikiListe);
        selectionCo = selectionCo.replace(result,newresult);
    }
    
    for(i=0;i<resultU.length; i++) {
	    var result = resultU[i];
        var newresult = result.replace(wikiListRegRep,wikiListp);
        selectionCo = selectionCo.replace(result,newresult);
    }
    
    selectionCo = selectionCo.replace(RegexListTerms,wikiEndParag);

	  return selectionCo;
}

// replace xhtml text with wiki text
function replaceMarkup(selection, cible) {

		// get the elements Ids
		var content = document.getElementById(cible);

    var RegexBold = /<strong[^<]*>|<\/strong>/gi;
    // var RegexBold2 = /<span style="font-weight: bold;">|<\/span>/gi;
	  var RegexBold3 = /<b [^<]*>|<\/b>/gi;
	  
	  var RegexItalic = /<em[^<]*>|<\/em>/gi;
	  //var RegexItalic2 = /<span style="font-style: italic;">|<\/span>/gi;
	  var RegexItalic3 = /<i[^<]*>|<\/i>/gi;
	  
	  var RegexStrike = /<strike[^<]*>|<\/strike>/gi;
	  //var RegexStrike2 = /<span style="text-decoration: line-through;">|<\/span>/gi;
	  
	  var RegexParagBeg = /<p[^<]*>/gi;
	  var RegexParagEnd = /<\/p>/gi;
	  var RegexCarriageReturn = /<\/br>\s?|<br \/>\s?|<br\/>\s?/gi;

	  var RegexTable = /<table[^<]*>|<\/table>/gi;
	  var RegexTableSep = /<\/td><td[^<]*>|<\/th><th[^<]*>/gi;
  	var RegexTableL = /<tr[^<]*>|<th[^<]*>/gi;
    var RegexTableTerm = /<td[^<]*>|<\/td>|<\/tr>/gi;

	  var RegexTitle1 = /<h1[^<]*>/gi;
	  var RegexTitle2 = /<h2[^<]*>/gi;
	  var RegexTitle3 = /<h3[^<]*>/gi;
	  var RegexTitle4 = /<h4[^<]*>/gi;
	  var RegexTitle5 = /<h5[^<]*>/gi;
	  var RegexTitle6 = /<h6[^<]*>/gi;
	  var RegexTitleTerm = /<\/h1>|<\/h2>|<\/h3>|<\/h4>|<\/h5>|<\/h6>/gi;

	  var RegexSup = /<sup[^<]*>|<\/sup>/gi;
	  var RegexSub = /<sub[^<]*>|<\/sub>/gi;

	  var RegexHtmlTerms = /<div[^<]*>|<\/div>|<u [^<]*>|<\/u>|<span[^<]*>|<\/span>|&nbsp;|<tbody>|<\/tbody>|<th>|<\/th>/gi;

	  var wikiBold = '__';
	  var wikiItalic = '~~';
	  var wikiStrike = '--';
    var wikiesc = '';
    var wikiTable = '{table}';
	  var wikiTableSep = '|';
	  var wikiTitle1 = 'h1 ';
	  var wikiTitle2 = 'h2 ';
	  var wikiTitle3 = 'h3 ';
	  var wikiTitle4 = 'h4 ';
	  var wikiTitle5 = 'h5 ';
	  var wikiTitle6 = 'h6 ';
	  var wikiSup = '^^';
	  var wikiSub = '%%';
	  var wikiEndParag = '\r\n\r\n';
	  var wikiEscTable = '\r\n';
	  var wikiCarriageReturn = '\\\\\r\n';

	  var processedString = selection; //wiki text value
	  
	  //replace all Regular expression
	  processedString = processedString.replace(RegexBold,wikiBold);
	  processedString = processedString.replace(RegexItalic,wikiItalic);
	  processedString = processedString.replace(RegexStrike,wikiStrike);
	  
	  //processedString = processedString.replace(RegexBold2,wikiBold);
    processedString = processedString.replace(RegexBold3,wikiBold);
    //processedString = processedString.replace(RegexItalic2,wikiItalic);
    //processedString = processedString.replace(RegexStrike2,wikiStrike);

		//paragraph tags
		processedString = processedString.replace(RegexParagBeg,wikiesc);
		processedString = processedString.replace(RegexParagEnd,wikiEndParag);
		
		processedString = processedString.replace(RegexSup,wikiSup);
		processedString = processedString.replace(RegexSub,wikiSub);

	  processedString = processedString.replace(RegexTable,wikiTable);
	  processedString = processedString.replace(RegexTableSep,wikiTableSep);
	  processedString = processedString.replace(RegexTableL,wikiEscTable);
	  processedString = processedString.replace(RegexTableTerm,wikiesc);
	  
	  processedString = processedString.replace(RegexCarriageReturn,wikiCarriageReturn);
	  processedString = processedString.replace(RegexTitle1,wikiTitle1);
    processedString = processedString.replace(RegexTitle2,wikiTitle2);
    processedString = processedString.replace(RegexTitle3,wikiTitle3);
    processedString = processedString.replace(RegexTitle4,wikiTitle4);
    processedString = processedString.replace(RegexTitle5,wikiTitle5);
    processedString = processedString.replace(RegexTitle6,wikiTitle6);
    processedString = processedString.replace(RegexTitleTerm,wikiEndParag);
    
    processedString = ReplaceHTMLEntities(processedString);
	  processedString = processedString.replace(RegexHtmlTerms,wikiesc);
	  	  
	  var srcPos  = processedString.lastIndexOf("\r\n\r\n");
		if (srcPos != -1){
			processedString = processedString.substring(0, srcPos);
		}
	  
	  processedString  = replaceLink(processedString);
	  processedString  = replaceImg(processedString);
	  processedString  = replaceList(processedString);
		
	  content.value = processedString; //new text
}

// Unused function. FCK editor was configured to keep special chars intact
function ReplaceHTMLEntities(processedString)
{
		processedString = processedString.replace(/&#160;/g,unescape('%A0'));
    processedString = processedString.replace(/&amp;/g,unescape('%26'));
    processedString = processedString.replace(/&gt;/g,unescape('%3E'));
    processedString = processedString.replace(/&lt;/g,unescape('%3C'));
    
    return processedString;
}

// replace html text with wiki text
function ReplaceContents(cible1,cible2)
{
   	  // Get the editor instance that we want to interact with.
   	  var oEditor = FCKeditorAPI.GetInstance(cible1);
   	  var fckValue = oEditor.GetHTML();//Get the editor contents in HTML.
   	  replaceMarkup(fckValue,cible2);
}

function saveTempContent(editorType)
{
	if(editorType=="wiki" || editorType=="all")
	{
		tempWikiContent=document.getElementById('content').value;
	}
	
	if(editorType=="fck" || editorType=="all")
	{
		var oEditor = FCKeditorAPI.GetInstance('contentFck');
		tempFCKContent=oEditor.GetHTML();
	}
}

function isContentSynchro(editorType)
{
	isSynchro=true;
	// synchro occur only if wysiwyg tab is displayed
	if (wysiwyg)
	{
		// has modifications been made to wikiText?
		if(editorType=="wiki")
		{
			var newContent = document.getElementById('content').value;
			if (tempWikiContent==newContent) 
				isSynchro=true;
			else 
				isSynchro=false;
		}
		// has modifications been made to FCK Text?
		else if(editorType=="fck")
		{
			var oEditor = FCKeditorAPI.GetInstance('contentFck');
			var newContent=oEditor.GetHTML();
			if (tempFCKContent==newContent) 
				isSynchro=true;
			else 
				isSynchro=false;
		}
	}	
	return isSynchro;
}

function FCKToWiki()
{
	  //Permit to discard calls from the preview pane
	calledFromFck=false;
	 
	 // page initialization special behaviour: no synchro
	 if (!pageInit)
	 {	 
	  if(!isContentSynchro("fck"))
		{
			//Convert HTML back to wiki via Javascript
			ReplaceContents('contentFck','content');
			
			//Save current state
			saveTempContent('all');
		}		
	 }
	 else
	 {
		 pageInit=false;
	 }
	
}

function wikiToFCK()
{
		if(!isContentSynchro("wiki"))
		{
			//We only want the previewContent callback to pushback values is called from FCK window
			calledFromFck=true; 
			
			//Asks for a HTML preview to the server
			previewContent('content','previewContent', 'pageVersion', 'realm','pageName','?' );
			
			//Save current state
			saveTempContent('all');
		}
}

// Breaks the macro markup so the server won't process it
function disableMacros(inputString)
{
	  var macroImage = "image";
	  var macroLink = "link";
	  var macroTable = "table";
	  
	  var candidateMacroName = "";
	  var endMacroPos = inputString.indexOf("}");
	  var beginMacroPos = inputString.indexOf("{");
	  while(beginMacroPos != -1 && endMacroPos != 1)
	  {
	  	 candidateMacroName=inputString.substring(beginMacroPos+1, endMacroPos);
	  	 
	  	 var candidateMacroImage = candidateMacroName.substring(0,macroImage.length);
	  	 var candidateMacroLink =  candidateMacroName.substring(0,macroLink.length);
	     var candidateMacroTable = candidateMacroName.substring(0,macroTable.length);
	     
	     // If the candidate macro is non supported, replace its delimiters
	  	 if(candidateMacroImage != macroImage && 
	  	    candidateMacroLink  != macroLink && 
	  	    candidateMacroTable != macroTable)
	  	 {
	  	    inputString = inputString.substring(0,beginMacroPos)+ '@#' + candidateMacroName + '#@' + inputString.substring(endMacroPos+1,inputString.length);	
	  	 }
	  	 
	  	 // Extract the unprocessed part of the initial string
	  	 var tempString = inputString.substring(endMacroPos+1,inputString.length);
	  	 endMacroPos = tempString.indexOf("}");
	 		 beginMacroPos = tempString.indexOf("{");
	 		 
	 		 // Add the length of the processed part
	  	 if(endMacroPos!=-1) endMacroPos=endMacroPos+inputString.length-tempString.length;
	 		 if(beginMacroPos!=-1) beginMacroPos=beginMacroPos+inputString.length-tempString.length;
	  }
	  
	  var RegexOpenWikiPage = /\[/gi;
    var RegexCloseWikiPage = /\]/gi;
	  
	  inputString = inputString.replace(RegexOpenWikiPage,'@,');
	  inputString = inputString.replace(RegexCloseWikiPage,',@');
	  
    return inputString;
}

// Reactivate the macro markup after processing from server
function enableMacros(inputString)
{
	  var mrkPreviewMacroOpen = /@#/gi;
	  var mrkPreviewMacroClose = /#@/gi;
	  
	  var mrkPreviewWikiPageOpen = /@,/gi;
	  var mrkPreviewWikiPageClose = /,@/gi;
	  
	  var mrkWikiMacroOpen = '{';
	  var mrkWikiMacroClose = '}';
	  
	  var mrkWikiPageOpen = '[';
	  var mrkWikiPageClose = ']';
	  
	  inputString = inputString.replace(mrkPreviewMacroOpen,mrkWikiMacroOpen);
    inputString = inputString.replace(mrkPreviewMacroClose,mrkWikiMacroClose);
    
    inputString = inputString.replace(mrkPreviewWikiPageOpen,mrkWikiPageOpen);
    inputString = inputString.replace(mrkPreviewWikiPageClose,mrkWikiPageClose);
    
    return inputString;
}

var previewDiv;
function previewContent(contentId,previewId,pageVersionId,realmId,pageNameId,url) {
    try {
	 	var content = document.getElementById(contentId);
	 	var contentValue ="";
	 	
	 	// Before the call to the server by FCK, don't process Macros
	 	if(calledFromFck){contentValue = disableMacros(content.value);}
		else{contentValue = content.value;}
		
	 	var pageVersion = document.getElementById(pageVersionId);
	 	var pageName = document.getElementById(pageNameId);
	 	var realm = document.getElementById(realmId);
	 	previewDiv = document.getElementById(previewId);
	 	var formContent = new Array();
	 	formContent[0] = "content"
	 	formContent[1] = contentValue;
	 	formContent[2] = "pageName";
	 	formContent[3] = pageName.value;
	 	formContent[4] = "command_render";
	 	formContent[5] = "render";
	 	formContent[6] = "action";
	 	formContent[7] = "fragmentpreview";
	 	formContent[8] = "panel";
	 	formContent[9] = "Main";
	 	formContent[10] = "version";
	 	formContent[11] = pageVersion.value;
	 	formContent[12] = "realm";
	 	formContent[13] = realm.value;
	 	var myLoader = new AsyncDIVLoader();
	 	myLoader.loaderName = "previewloader";
	 	previewDiv.innerHTML = "<img src=\"/sakai-rwiki-tool/images/ajaxload.gif\" />";
	 	myLoader.fullLoadXMLDoc(url,"divReplaceCallback","POST",formContent);
 	} catch  (e) {
	 	previewDiv.innerHTML = "<img src=\"/sakai-rwiki-tool/images/silk/icons/error.png\" />";
 		alert("Failed to Load preview "+e);
 	}
}

function divReplaceCallback(responsestring) {
	previewDiv.innerHTML = responsestring;
	sizeFrameAfterAjax(previewDiv);
	
	// Push the result in the FCK editor window
	if (calledFromFck) {
		
		responsestring = cleanHTML(responsestring);
		responsestring = enableMacros(responsestring);
		
		var oEditor = FCKeditorAPI.GetInstance('contentFck');
  	oEditor.SetHTML(responsestring);
  }
}

/****************************************************************************/
// End of FCK Editor integration functions
/****************************************************************************/


function changeClass(oldclass, newclass) {

    var spantags = document.getElementsByTagName("SPAN");
    var oldclasses = getElementsByClass(spantags,oldclass);
    for (i = 0; i < oldclasses.length; i++ ) { 
       oldclasses[i].className = newclass;
    }
}

function changeRoleState(cb,column,enable,disable) {
    if ( cb ) {
        changeClass(column+disable,column+enable);
    } else {
        changeClass(column+enable,column+disable);
    }
}

// FIXME: Internationalize
contractsymbol = '<span class="rwiki_collapse"><img title="hide" alt="hide" src="/sakai-rwiki-tool/images/minus.gif"/><span>Hide </span></span>';
expandsymbol = '<span class="rwiki_expand"><img alt="show" src="/sakai-rwiki-tool/images/plus.gif" title="show"/><span>Show </span></span>';

function getElementsByClass(ellist, classname) {
  var els = new Array();
  for (i=0; i<ellist.length; i++) {
    if (ellist[i].className == classname) {
      els.push(ellist[i]);
    }
  }
  return els;
}

function expandcontent(root, blockname) {
  var block = document.getElementById(blockname);
  var spantags = root.getElementsByTagName("SPAN");
  var showstatespans = getElementsByClass(spantags, "showstate");

  block.style.display = (block.style.display != "block") ? "block" : "none";
  showstatespans[0].innerHTML = block.style.display == "block" ? contractsymbol : expandsymbol;
  window.onload();
}

function hidecontent(rootname, blockname) {
  var root = document.getElementById(rootname);
  var block = document.getElementById(blockname);
  var spantags = root.getElementsByTagName("SPAN");
  var showstatespans = getElementsByClass(spantags, "showstate");

  block.style.display = "none";
  showstatespans[0].innerHTML = expandsymbol;
}

function onload() {
	
  var allels = document.all? document.all : document.getElementsByTagName("*");
  var expandableContent = getElementsByClass(allels, "expandablecontent");
  for (var i = 0; i < expandableContent.length; i++) {
    expandableContent[i].style.display = "none";
  }
  var allexpandable = getElementsByClass(allels, "expandable");
  var i = 0;
  for (var i = 0; i < allexpandable.length; i++) {
    var spantags = allexpandable[i].getElementsByTagName("SPAN");
    var showstatespans = getElementsByClass(spantags, "showstate");
    showstatespans[0].innerHTML = expandsymbol;
  }
}
function storeCaret(el) {
    if ( el.createTextRange ) 
        el.caretPos = document.selection.createRange().duplicate();        
}

function addAttachment(textareaid, formid, editcontrolid, type) {
  var textarea;
  var editcontrol;
  var form;
  var store;
  if ( document.all ) {
    textarea = document.all[textareaid];
    editcontrol = document.all[editcontrolid];
    form = document.all[formid];
  } else {
    textarea = document.getElementById(textareaid);
    editcontrol = document.getElementById(editcontrolid);
    form = document.getElementById(formid);
  }    

  if (typeof(textarea.caretPos) != "undefined" && typeof(textarea.createTextRange) != "undefined")
  {
    var duplicate = textarea.caretPos.duplicate();
    var textareaRange = textarea.createTextRange();

    textareaRange.select();
    textareaRange = document.selection.createRange().duplicate();
    duplicate.select();

    var duplicateText = duplicate.text;
    var length = duplicateText.replace(/\r\n/g,"\n").length;

    duplicate.setEndPoint("StartToStart", textareaRange); 	

    duplicateText = duplicate.text;
    var endPoint = duplicateText.replace(/\r\n/g,"\n").length;

    var startPoint = endPoint - length;
    store = startPoint + ":" + endPoint;
  } else if (typeof(textarea.selectionStart) != "undefined") {
    store = textarea.selectionStart + ":" + textarea.selectionEnd;
  } else {
    store = "0:0";
  }

  
  editcontrol.innerHTML += "<input type='hidden' name='command_attach"+type+"' value='attach" + type + "'/><input type='hidden' name='caretPosition' value='"+ store + "'/>";
  form.submit();
}


function addMarkup(textareaid, contentMU, startMU, endMU) {
    var textarea;
        if ( document.all ) {
            textarea = document.all[textareaid];
		} else {
		    textarea = document.getElementById(textareaid);
		}    

	if (typeof(textarea.caretPos) != "undefined" && textarea.createTextRange)
	{
        
		var caretPos = textarea.caretPos, repText = caretPos.text, temp_length = caretPos.text.length;
		var i=-1;
		while (++i < repText.length && /\s/.test("" + repText.charAt(i))) {
		}

		switch (i) {
		case 0: break;
		case repText.length: startMU = repText + startMU;
		repText = "";
		break;
		default: startMU = repText.substring(0, i) + startMU;
		         repText = repText.substr(i);
		}

		i = repText.length;
		while ( i > 0 && /\s/.test("" + repText.charAt(--i))) {
		}

		switch (i) {
		case repText.length - 1: break;
		case -1: endMU = endMU + repText; break;
		default: endMU = endMU + repText.substr(i + 1);
		         repText = repText.substring(0, i + 1);
		}

		
		if ( repText.length == 0 )
		    repText = contentMU;

		caretPos.text = startMU + repText + endMU;

		textarea.focus(caretPos);
	} 
	// Mozilla text range wrap.
	else if (typeof(textarea.selectionStart) != "undefined")
	{
		var begin = textarea.value.substr(0, textarea.selectionStart);
		var repText = textarea.value.substr(textarea.selectionStart, textarea.selectionEnd - textarea.selectionStart);
		var end = textarea.value.substr(textarea.selectionEnd);
		var newCursorPos = textarea.selectionStart;
		var scrollPos = textarea.scrollTop;
		
		var i=-1;
		while (++i < repText.length && /\s/.test("" + repText.charAt(i))) {
		}
		
		switch (i) {
		case 0: break;
		case repText.length: startMU = repText + startMU;
		repText = "";
		break;
		default: startMU = repText.substring(0, i) + startMU;
		         repText = repText.substr(i);
		}

		i = repText.length;
		while ( i > 0 && /\s/.test("" + repText.charAt(--i))) {
		}

		switch (i) {
		case repText.length - 1: break;
		case -1: endMU = endMU + repText; break;
		default: endMU = endMU + repText.substr(i + 1);
		         repText = repText.substring(0, i + 1);
		}

		
		if ( repText.length == 0 )
		    repText = contentMU;

		textarea.value = begin + startMU + repText + endMU + end;

		if (textarea.setSelectionRange)
		{
			textarea.setSelectionRange(newCursorPos + startMU.length, newCursorPos + startMU.length + repText.length);
			textarea.focus();
		}
		textarea.scrollTop = scrollPos;
	}
	// Just put them on the end, then.
	else
	{
		textarea.value += startMU + contentMU + endMU;
		textarea.focus(textarea.value.length - 1);
	}
}

/*
 * Content-seperated javascript tree widget
 * Copyright (C) 2005 SilverStripe Limited
 * Feel free to use this on your websites, but please leave this message in the fies
 * http://www.silverstripe.com/blog
*/

/*
 * Initialise all trees identified by <ul class="tree">
 */
function autoInit_trees() {
	var candidates = document.getElementsByTagName('ul');
	for(var i=0;i<candidates.length;i++) {
		if(candidates[i].className && candidates[i].className.indexOf('tree') != -1) {
			initTree(candidates[i]);
			candidates[i].className = candidates[i].className.replace(/ ?unformatted ?/, ' ');
		}
	}
}
 
/*
 * Initialise a tree node, converting all its LIs appropriately
 */
function initTree(el) {
	var i,j;
	var spanA, spanB, spanC;
	var startingPoint, stoppingPoint, childUL;
	
	// Find all LIs to process
	for(i=0;i<el.childNodes.length;i++) {
		if(el.childNodes[i].tagName && el.childNodes[i].tagName.toLowerCase() == 'li') {
			var li = el.childNodes[i];

			// Create our extra spans
			spanA = document.createElement('span');
			spanB = document.createElement('span');
			spanC = document.createElement('span');
			spanA.appendChild(spanB);
			spanB.appendChild(spanC);
			spanA.className = 'a ' + li.className.replace('closed','spanClosed');
			spanA.onMouseOver = function() {}
			spanB.className = 'b';
			spanB.onclick = treeToggle;
			spanC.className = 'c';
			
			
			// Find the UL within the LI, if it exists
			stoppingPoint = li.childNodes.length;
			startingPoint = 0;
			childUL = null;
			for(j=0;j<li.childNodes.length;j++) {
			    if ( li.childNodes[j].tagName != null ) {
				if( li.childNodes[j].tagName.toLowerCase() == 'div') {
					startingPoint = j + 1;
					continue;
				}

				if( li.childNodes[j].tagName.toLowerCase() == 'ul') {
					childUL = li.childNodes[j];
					stoppingPoint = j;
					break;					
				}
				}
			}
				
			// Move all the nodes up until that point into spanC
			for(j=startingPoint;j<stoppingPoint;j++) {
				spanC.appendChild(li.childNodes[startingPoint]);
			}
			
			// Insert the outermost extra span into the tree
			if(li.childNodes.length > startingPoint) li.insertBefore(spanA, li.childNodes[startingPoint]);
			else li.appendChild(spanA);
			
			// Process the children
			if(childUL != null) {
				if(initTree(childUL)) {
					addClass(li, 'children', 'closed');
					addClass(spanA, 'children', 'spanClosed');
				}
			}
		}
	}
	
	if(li) {
		// li and spanA will still be set to the last item

		addClass(li, 'last', 'closed');
		addClass(spanA, 'last', 'spanClosed');
		return true;
	} else {
		return false;
	}
		
}
 
/*
 * +/- toggle the tree, where el is the <span class="b"> node
 * force, will force it to "open" or "close"
 */
function treeToggle(el, force) {
	el = this;
	
	while(el != null && (!el.tagName || el.tagName.toLowerCase() != "li")) el = el.parentNode;
	
	// Get UL within the LI
	var childSet = findChildWithTag(el, 'ul');
	var topSpan = findChildWithTag(el, 'span');

	if( force != null ){
		
		if( force == "open"){
			treeOpen( topSpan, el )
		}
		else if( force == "close" ){
			treeClose( topSpan, el )
		}
		
	}
	
	else if( childSet != null) {
		// Is open, close it
		if(!el.className.match(/(^| )closed($| )/)) {		
			treeClose( topSpan, el )
		// Is closed, open it
		} else {			
			treeOpen( topSpan, el )
		}
	}
}

function treeOpen( a, b ){
	removeClass(a,'spanClosed');
	removeClass(b,'closed');
}
	
	
function treeClose( a, b ){
	addClass(a,'spanClosed');
	addClass(b,'closed');
}

/*
 * Find the a child of el of type tag
 */
function findChildWithTag(el, tag) {
	for(var i=0;i<el.childNodes.length;i++) {
		if(el.childNodes[i].tagName != null && el.childNodes[i].tagName.toLowerCase() == tag) return el.childNodes[i];
	}
	return null;
}

/*
 * Functions to add and remove class names
 * Mac IE hates unnecessary spaces
 */
function addClass(el, cls, forceBefore) {
	if(forceBefore != null && el.className.match(new RegExp('(^| )' + forceBefore))) {
		el.className = el.className.replace(new RegExp("( |^)" + forceBefore), '$1' + cls + ' ' + forceBefore);

	} else if(!el.className.match(new RegExp('(^| )' + cls + '($| )'))) {
		el.className += ' ' + cls;
		el.className = el.className.replace(/(^ +)|( +$)/g, '');
	}
}
function removeClass(el, cls) {
	var old = el.className;
	var newCls = ' ' + el.className + ' ';
	newCls = newCls.replace(new RegExp(' (' + cls + ' +)+','g'), ' ');
	el.className = newCls.replace(/(^ +)|( +$)/g, '');
} 

/*
 * Handlers for automated loading
 */ 
 _LOADERS = Array();

function callAllLoaders() {
	var i, loaderFunc;
	for(i=0;i<_LOADERS.length;i++) {
		loaderFunc = _LOADERS[i];
		if(loaderFunc != callAllLoaders) loaderFunc();
	}
}

function appendLoader(loaderFunc) {
	if(window.onload && window.onload != callAllLoaders)
		_LOADERS[_LOADERS.length] = window.onload;

	window.onload = callAllLoaders;

	_LOADERS[_LOADERS.length] = loaderFunc;
}

function setMainFrameHeightNoScroll(id, shouldScroll) {
	if (typeof(shouldScroll) == 'undefined') {
		shouldScroll = true;
	}
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	id = id.replace(/[^a-zA-Z0-9]/g,"x");
	id = "Main" + id;

	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{
		// reset the scroll
		if (shouldScroll) {
		  parent.window.scrollTo(0,0);
		}

		var objToResize = (frame.style) ? frame.style : frame;
//		alert("After objToResize");

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var docElOffsetH = document.documentElement.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

//		alert("After innerDocScrollH");
	
		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			if (docElOffsetH > offsetH) {
			  height = docElOffsetH;
			} else {
			  height = offsetH;
			}
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 10;
		
		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
		
		
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;
//		window.status = s;
//		alert(s);
		//window.location.hash = window.location.hash;
		if (shouldScroll) {
		  var anchor = document.location.hash;
		  if (anchor != null && anchor.length > 0 && anchor.charAt(0) == '#') {
		    anchor = anchor.substring(1);
		    var coords = getAnchorPosition(anchor);
		    var framey = findPosY(frame);
		    parent.window.scrollTo(coords.x, coords.y + framey);
		  }
		}

                if (parent.postIframeResize){ 
			parent.postIframeResize(id);
		}
	}

}

// This invaluable function taken from QuirksMode @ http://www.quirksmode.org/index.html?/js/findpos.html
// Portable to virtually every browser, with a few caveates.
function findPosY(obj) {
  var curtop = 0;
  if (obj.offsetParent) {
    while (obj.offsetParent) {
      curtop += obj.offsetTop
	obj = obj.offsetParent;
    }
  } else if (obj.y) {
    curtop += obj.y;
  }
  return curtop;
}

function getAnchorPosition( anchorName){ 
 if (document.layers) {
    var anchor = document.anchors[anchorName];
    return { x: anchor.x, y: anchor.y };
  }
  else if (document.getElementById) {
    var anchor = document.anchors[anchorName];
    var coords = {x: 0, y: 0 };
    while (anchor) {
      coords.x += anchor.offsetLeft;
      coords.y += anchor.offsetTop;
      anchor = anchor.offsetParent;
    }
    return coords;
  }
}

appendLoader(autoInit_trees);


function hideSidebar(id) {
  document.getElementById('rwiki_sidebar').style.display='none';
  document.getElementById('rwiki_content').className = 'nosidebar';
  document.getElementById('sidebar_switch_on').style.display='block';
  document.getElementById('sidebar_switch_off').style.display='none';
  sizeFrameAfterAjax();
}
function showSidebar(id) {
  document.getElementById('rwiki_sidebar').style.display='block';
  document.getElementById('rwiki_content').className = 'withsidebar';
  document.getElementById('sidebar_switch_on').style.display='none';
  document.getElementById('sidebar_switch_off').style.display='block';
  sizeFrameAfterAjax();
}

function sizeFrameAfterAjax(el) {
		    var frame = getFrame(placementid);
		    
		    if ( frame != null ) {
		    
                var height;
                var objToResize = (frame.style) ? frame.style : frame;

                var scrollH = document.body.scrollHeight;
                var offsetH = document.body.offsetHeight;
                var clientH = document.body.clientHeight;
                var innerDocScrollH = null;

                if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
                {
                        // very special way to get the height from IE on Windows!
                        // note that the above special way of testing for undefined variables is necessary for older browsers
                        // (IE 5.5 Mac) to not choke on the undefined variables.
                        var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
                        innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
                }

//              alert("After innerDocScrollH");

                if (document.all && innerDocScrollH != null)
                {
                        // IE on Windows only
                        height = innerDocScrollH;
                }
                else
                {
                        // every other browser!
                        height = offsetH;
                }
                
                
                // loop round all elements in this dom and find the max y extent
                var tl = 0;
                var sh = 0;
                if ( el != null ) {
                  tl = getAbsolutePos(el);
                
                  sh = el.scrollHeight;
                  var oh = el.offsetHeight;
                  var ch = el.clientHeight;
                } else {
                  tl = findMaxExtent(document,0);
                  tl = tl+50;
                  sh = 0;
                }
                var bottom = tl.y + sh;
 
                // here we fudge to get a little bigger
                var newHeight = mmax(mmax(mmax(mmax(height,scrollH),clientH),innerDocScrollH),bottom) + 50;

                // no need to be smaller than...
                //if (height < 200) height = 200;
                objToResize.height=newHeight + "px";
		    
                var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + 
                clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " bottom "+ bottom+
                " sh "+ sh +
                " oh "+oh+
                " ch "+ch+
                " Set height to: " + newHeight;
//              window.status = s;
//              alert(s);
//		     } else {
//		      alert(" No placement Fame for "+placementid);
		     }
}
function findMaxExtent(el,y) {
    var ab = getAbsolutePos(el);
    if ( ab.y > y ) y = ab.y;
	for ( i = 0; i < el.childNodes.length; i++ ) {
	    ab = getAbsolutePos(el.childNodes[i]);
	    if ( ab.y > y ) y = ab.y;
	}
	return y;
}

function selectTabs() {
	var work = selectTabs.arguments;
	for ( i = 0; i < work.length-2; i+=3) {
	   
		var el = document.getElementById(work[i]);
		if ( el ) {
		    if ( el.className == work[i+1] ) {
				el.className = work[i+2];
			}
		}
	}
}

var NUMBER_OF_PERMISSIONS =0;
var CREATE = NUMBER_OF_PERMISSIONS++;
var READ = NUMBER_OF_PERMISSIONS++;
var UPDATE = NUMBER_OF_PERMISSIONS++;
var ADMIN = NUMBER_OF_PERMISSIONS++;
var SUPERADMIN = NUMBER_OF_PERMISSIONS++;

function setPermissionDisplay(enabledClass,disabledClass,readSwitch,updateSwitch,adminSwitch) {
	var switches = new Array();

	// lets try something a bit more magical...
	switches[CREATE] = true;
	switches[READ] = readSwitch;
	switches[UPDATE] = updateSwitch;
	switches[ADMIN] = adminSwitch;
	switches[SUPERADMIN] = true;
	

	// for each role row
	for ( rowStart = 0; rowStart < permissionsMatrix.length;  rowStart += NUMBER_OF_PERMISSIONS ) {
		// determine if each permission should be set:
		for ( j = 0; j < NUMBER_OF_PERMISSIONS; j++) {
			permissionNumber = rowStart + j;

			permissionArray = permissionsMatrix[permissionNumber];
			var enabled = false;
			// By checking if the switch is set and the lock is set.
			for (i = 0; (!enabled) && (i < NUMBER_OF_PERMISSIONS); i++) {
				enabled = enabled || (( permissionArray[1].charAt(i) == 'x' ) && ( permissionsMatrix[rowStart + i][0]) && (switches[i]));			  
			}
		  						
			setEnabledElement(permissionsStem + permissionNumber, enabled);
		}
	}
}

function setEnabledElement(elId, enabled) {
	var el = null;
	if ( document.all ) {
		el = document.all[elId];
	} else {
		el = document.getElementById(elId);
	}
	if (el != null) {
		el.innerHTML = enabled ? yes_val : no_val;
	} 
}

function setClassName(elId,className) {
	var el = null;
	if ( document.all ) {
		el = document.all[elId];
	} else {
		el = document.getElementById(elId);
	}
	if ( el != null ) {
		el.className = className;
	}
}


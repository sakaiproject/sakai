<!--
/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************/
-->
<div class="editToolBarContainer" >
<a class="editToolBar" href="#" id="toolbarButtonSave" onclick="var form = document.getElementById('editForm'); var saveButton = document.getElementById('saveButton'); form.command_save.value = saveButton.value; form.submit();" ><img src="/library/image/transparent.gif" border="0"   title="${rlb.jsp_toolb_save}" alt="${rlb.jsp_toolb_save}" /></a>
<a class="editToolBar" href="#" id="toolbarButtonBold" onclick="addMarkup('content','bold','__','__'); return false;" ><img src="/library/image/transparent.gif" border="0"   title="${rlb.jsp_toolb_bold}" alt="${rlb.jsp_toolb_bold}" /></a>
<a class="editToolBar" href="#" id="toolbarButtonItalic" onclick="addMarkup('content','italic','~~','~~'); return false;" ><img src="/library/image/transparent.gif" border="0"  title="${rlb.jsp_toolb_italic}" alt="${rlb.jsp_toolb_italic}" /></a>
<a class="editToolBar" href="#" id="toolbarButtonSuper" onclick="addMarkup('content','super','^^','^^'); return false;"  ><img src="/library/image/transparent.gif" border="0" title="${rlb.jsp_toolb_superscript}" alt="${rlb.jsp_toolb_superscript}" /></a>
<a class="editToolBar" href="#" id="toolbarButtonSub" onclick="addMarkup('content','sub','%%','%%'); return false;"  ><img src="/library/image/transparent.gif" border="0" title="${rlb.jsp_toolb_subscript}" alt="${rlb.jsp_toolb_subscript}" /></a>
<select name="toolbarButtonHeading" id="toobarButtonHeading" onChange="if ( this.value != 'none' ) { addMarkup('content','Heading 1','\n'+this.value+' ','\n'); this.value = 'none'; } return false;" >
	<option value="none" ><c:out value="${rlb.jsp_toolb_headings}" /></option>
	<option value="h1"><c:out value="${rlb.jsp_toolb_heading}" /> 1</option>
	<option value="h2"><c:out value="${rlb.jsp_toolb_heading}" /> 2</option>
	<option value="h3"><c:out value="${rlb.jsp_toolb_heading}" /> 3</option>
	<option value="h4"><c:out value="${rlb.jsp_toolb_heading}" /> 4</option>
	<option value="h5"><c:out value="${rlb.jsp_toolb_heading}" /> 5</option>
	<option value="h6"><c:out value="${rlb.jsp_toolb_heading}" /> 6</option>
</select>
<a class="editToolBar" href="#" id="toolbarButtonTable" onclick="addMarkup('content','col|col|col\nnewrow|col|col','{table}\n','\n{table}'); return false;" ><img src="/library/image/transparent.gif" border="0" title="${rlb.jsp_toolb_table}" alt="${rlb.jsp_toolb_table}" /></a>
<a class="editToolBar" href="#" id="toolbarButtonLink" onclick="addAttachment('content','editForm','editControl', 'link'); return false;"   ><img src="/library/image/transparent.gif" border="0" title="${rlb.jsp_toolb_link}" alt="${rlb.jsp_toolb_link}" /></a>
<a class="editToolBar" href="#" id="toolbarButtonImage" onclick="addAttachment('content','editForm', 'editControl', 'embed'); return false;"   ><img src="/library/image/transparent.gif" border="0" title="${rlb.jsp_toolb_image}" alt="${rlb.jsp_toolb_image}" /></a> 
</div>

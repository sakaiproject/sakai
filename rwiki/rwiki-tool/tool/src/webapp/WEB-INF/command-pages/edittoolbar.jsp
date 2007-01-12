<!--
/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************/
-->
<div class="editToolBarContainer" >
<a class="editToolBar" href="#" id="toolbarButtonSave" onclick="var form = document.getElementById('editForm'); var saveButton = document.getElementById('saveButton'); form.save.value = saveButton.value; form.submit();" ><img src="/library/image/transparent.gif" border="0"   title="Save" alt="Save" /></a>
<a class="editToolBar" href="#" id="toolbarButtonBold" onclick="addMarkup('content','bold','__','__'); return false;" ><img src="/library/image/transparent.gif" border="0"   title="bold" alt="bold" /></a>
<a class="editToolBar" href="#" id="toolbarButtonItalic" onclick="addMarkup('content','italic','~~','~~'); return false;" ><img src="/library/image/transparent.gif" border="0"  title="italic" alt="italic" /></a>
<a class="editToolBar" href="#" id="toolbarButtonSuper" onclick="addMarkup('content','super','^^','^^'); return false;"  ><img src="/library/image/transparent.gif" border="0" title="Superscript" alt="Superscript" /></a>
<a class="editToolBar" href="#" id="toolbarButtonSub" onclick="addMarkup('content','sub','%%','%%'); return false;"  ><img src="/library/image/transparent.gif" border="0" title="Subscript" alt="Subscript" /></a>
<select name="toolbarButtonHeading" id="toobarButtonHeading" onChange="if ( this.value != 'none' ) { addMarkup('content','Heading 1','\n'+this.value+' ','\n'); this.value = 'none'; } return false;" >
	<option value="none" >Headings...</option>
	<option value="h1">Heading 1</option>
	<option value="h2">Heading 2</option>
	<option value="h3">Heading 3</option>
	<option value="h4">Heading 4</option>
	<option value="h5">Heading 5</option>
	<option value="h6">Heading 6</option>
</select>
<a class="editToolBar" href="#" id="toolbarButtonTable" onclick="addMarkup('content','col|col|col\nnewrow|col|col','{table}\n','\n{table}'); return false;" ><img src="/library/image/transparent.gif" border="0" title="table" alt="table" /></a>
<a class="editToolBar" href="#" id="toolbarButtonLink" onclick="addAttachment('content','editForm','editControl', 'link'); return false;"   ><img src="/library/image/transparent.gif" border="0" title="link" alt="link" /></a>
<a class="editToolBar" href="#" id="toolbarButtonImage" onclick="addAttachment('content','editForm', 'editControl', 'embed'); return false;"   ><img src="/library/image/transparent.gif" border="0" title="image" alt="image" /></a> 
</div>

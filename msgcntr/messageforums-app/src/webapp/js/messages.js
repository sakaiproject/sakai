// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
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

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}

// MSGCNTR-53
$(document).ready(function(){
	  if ($('#displayWait').length<=0) {
		  $("body").append('<img id="displayWait" src="/messageforums-tool/images/wait_sakai.gif" width="50" height="50" style="display:none" />');
	  }
	  var loadUI = function() {
		  $.getScript("/library/webjars/jquery-blockui/2.65/jquery.blockUI.js",
			function(){
			  $('input[type=submit]').click(function() {
		        $.blockUI({ 
		            message: $('#displayWait'), 
		            css: { 
		                top:  ($(window).height() - 50) /2 + 'px', 
		                left: ($(window).width() - 50) /2 + 'px', 
		                width: '50px' 
		            } 
		        }); 
		        setTimeout($.unblockUI, 10000);
			  });
		    });
	  }
	  // I need jquery-UI js
	  // But we'll assume its already on the page so we don't break other scripts by loading an outdated version
	  loadUI();  
});

function changeSelect(obj) {
	$(obj).trigger("change");
}

function addTagSelector(obj) {
  	if (obj) {
	  	$(obj).select2({formatNoMatches:function(){return'';},placeholder: obj.getAttribute('title')});
	  	$(obj).on('change',function(){resize();});
  	}
}

$(document).ready(function(){

    $("#prefs_pvt_form\\:pvtmsgs\\:checkAll, #prefs_pvt_form\\:threaded_pvtmsgs\\:checkAll").click(function () {
        $('input:checkbox').not(this).prop('checked', this.checked);
        if(this.checked){
            toggleBulkOperations(true, 'prefs_pvt_form');
        }else{
            toggleBulkOperations(false, 'prefs_pvt_form');
        }
    });

    $('#prefs_pvt_form\\:search_by_date').change( function(){
      $('#prefs_pvt_form\\:pvt_beg_date, #prefs_pvt_form\\:pvt_end_date').toggleClass('showed');
    });

    $('#prefs_pvt_form\\:label').change( function(){
      $('#prefs_pvt_form\\:pvt_selected_label').toggleClass('showed');
    });

    $('#prefs_pvt_form\\:search_by_tags').change( function(){
      $('#prefs_pvt_form\\:pvt_selected_tags').toggleClass('showed');
    });

});

// Messages has one search box. Typing filters the listed messages, Enter searches the folder with the
// options behind the options button, and emptying the box drops a search the page was loaded with.
function setupMessagesSearch(field) {

  // JSF here cannot render a search type
  field.type = "search";
  const applied = field.value;
  const searchButton = document.getElementById("prefs_pvt_form:search_button");
  const clearButton = document.getElementById("prefs_pvt_form:clear_search_button");

  field.addEventListener("input", () => {
    if (applied && !field.value) {
      clearButton.click();
      return;
    }
    DataTable.tables({ api: true }).search(field.value).draw();
  });

  field.addEventListener("keydown", e => {
    if (e.key !== "Enter" || e.isComposing) {
      return;
    }
    e.preventDefault();
    if (field.value.trim()) {
      searchButton.click();
    } else if (applied) {
      clearButton.click();
    }
  });

  const toggle = document.getElementById("search-options-toggle");
  const options = document.getElementById("adv_input");
  toggle.addEventListener("click", () => {
    options.hidden = !options.hidden;
    toggle.setAttribute("aria-expanded", String(!options.hidden));
    // show the fields of the options already chosen
    const chosen = id => document.getElementById(`prefs_pvt_form:${id}`)?.checked;
    const show = (id, visible) => document.getElementById(`prefs_pvt_form:${id}`)?.classList.toggle("showed", visible);
    show("pvt_beg_date", chosen("search_by_date"));
    show("pvt_end_date", chosen("search_by_date"));
    show("pvt_selected_label", chosen("label"));
    show("pvt_selected_tags", chosen("search_by_tags"));
  });
}

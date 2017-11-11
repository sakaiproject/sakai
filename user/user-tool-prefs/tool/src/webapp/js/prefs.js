var setupMultipleSelect = function(){
    $(".selectSiteCheck").click(function(e) {
            var pos= $(this).position();
            var thisCol = $(this).closest('.flc-reorderer-column').attr('id');
            var thisEl = $(this).closest('.flc-reorderer-module');
            var panelMessage = $('.movePanelMessage').text();
            var panelMessageFav = $('.checkboxFromMessFav').text();
            var panelMessageAct = $('.checkboxFromMessAct').text();
            var panelMessageArc = $('.checkboxFromMessArc').text();

        if($(this).prop('checked')) {
            //only show actions that are germane to this specific column
            if(thisCol ==="reorderCol1") {
                // moving left disabled
                $('#movePanel').attr('class','col1Engaged');
                $('#movePanelLeftRight a').show();
                $('#movePanelRight').show().attr('title',panelMessage.replace('{0}',panelMessageAct)).find('.skip').text(panelMessage.replace('{0}',panelMessageAct));;
                $('#movePanel span').hide();
                $('#movePanel span.skip').show();
                $('#movePanelLeft').hide();
                $('#movePanelLeftDummy').show();
                $('#movePanelTop').show();
                $('#movePanelBottom').show();
                $('#movePanelTopDummy').hide();
                $('#movePanelBottomDummy').hide();
              }
            else {
                // only show move left, moving up/down is nonsensical
                // (will change the column header to reflect this)
                // moving right impossible
                pos.left = pos.left - 400;
                $('#movePanel').attr('class','col2Engaged');
                $('#movePanelLeftRight a').show();
                $('#movePanelLeft').show().attr('title',panelMessage.replace('{0}',panelMessageAct)).find('.skip').text(panelMessage.replace('{0}',panelMessageAct));
                $('#movePanelLeftRight span').hide();
                $('#movePanel span.skip').show();
                $('#movePanelRight').hide();
                $('#movePanelRightDummy').show();
                $('#movePanelTop').hide();
                $('#movePanelBottom').hide();
                $('#movePanelTopDummy').show();
                $('#movePanelBottomDummy').show();
            }
            // move the move panel down here 
            $('#movePanel').css({'top' : pos.top, 'left' : pos.left + 40,'display': 'block'});
            // uncheck the cbxs in the other col
            $('#layoutReorderer .selectSiteCheck').not('#' + thisCol + ' .selectSiteCheck').prop('checked',false);
            $('#layoutReorderer .last-login').not('#' + thisCol + ' .last-login').removeClass('siteSelected');
            $(thisEl).addClass('siteSelected');
        } 
        else {
            $(thisEl).removeClass('siteSelected');
            //hide #movePanel if all of the checkboxes are unchecked
            if( $(this).closest('.flc-reorderer-column').find(':checked').length ===0) {
                $('#movePanel').css('top','-1000px');
            }
        }
    });
        //going to have to refactor a bit of this
        $('#movePanelRight').click(function(e){
            e.preventDefault();
            var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             if($('#movePanel').attr('class') ==='col1Engaged'){
                $(selectedItems).prependTo('#reorderCol2 > span');
             }
             postMoveCleanUp(selectedItems);
        });
        $('#movePanelLeft').click(function(e){
            e.preventDefault();
             var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             if($('#movePanel').attr('class') ==='col2Engaged'){
                 $(selectedItems).prependTo($('#reorderCol1 > span'));
             };
             postMoveCleanUp(selectedItems);
        });
        $('#movePanelTop').click(function(e){
             e.preventDefault();
             var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             var selectedCol = $(selectedItems).first().closest('span');
             $(selectedItems).prependTo(selectedCol);
             postMoveCleanUp(selectedItems);
        });
         $('#movePanelBottom').click(function(e){
             e.preventDefault();
             var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             var selectedCol = $(selectedItems).first().closest('span');
             $(selectedItems).appendTo(selectedCol);
             postMoveCleanUp(selectedItems);
        });
var postMoveCleanUp = function(selectedItems) {
    var ids ='';
    var newTitle1=$('.checkboxSelectMessage').text().replace('{1}',$('.checkboxFromMessFav').text());
    var newTitle2=$('.checkboxSelectMessage').text().replace('{1}',$('.checkboxFromMessArc').text());

    $('.col1 .last-login').each(function(idx, item) {  
        var $thisCheckbox =$(this).find(':checkbox');
        var thisTitle = $(this).find('.siteLabel').text();
        $thisCheckbox.attr('title',newTitle1.replace('{0}', thisTitle));
        if (idx > 0) {
            ids =ids + ', ' + $(this).attr('id');
        }
        else {
            ids =$(this).attr('id');
        }
    });
    $('input[name$=prefTabString]').val(ids);
    var ids ='';
    $('.col2 .last-login').each(function(idx, item) {  
        var $thisCheckbox =$(this).find(':checkbox');
        var thisTitle = $(this).find('.siteLabel').text();
        $thisCheckbox.attr('title',newTitle2.replace('{0}', thisTitle));
        if (idx > 0) {
            ids =ids + ', ' + $(this).attr('id');
        }
        else {
            ids =$(this).attr('id');
        }
    });
    $('input[name$=prefHiddenString]').val(ids);
    $(selectedItems).hide();
    $(selectedItems).fadeIn(1500, function(){ 
    $(selectedItems).removeClass('siteSelected');
    });
     $('#layoutReorderer :checked').prop('checked',false);
     $('#movePanel').css('top','-999px');
     resizeFrame('grow');
};
};

setupPrefsGen = function(){
    if ($('.success').length) {
        $('.success').attr('tabindex','-1').fadeTo(5000,1).fadeOut(1000).css('outline','none').focus();
    }
};

setupPrefsTabs = function(from, to){
    $('.blockable').click(function(e){
        if ($(this).attr('onclick')) {
            $('.blockable').attr('onclick', '');
            $('.blockable').addClass('blocked', '');
        }
    });
    fromSelLen = $('.' + from).children('option').length;
    toSelLen = $('.' + to).children('option').length;
    
    if (fromSelLen === 0) {
        $('.br').attr('onclick', '');
        $('.br').addClass('blocked');
    }
    if (toSelLen === 0) {
        $('.bl').attr('onclick', '');
        $('.bl').addClass('blocked');
        $('.ud').attr('onclick', '');
        $('.ud').addClass('blocked');
    }
    if (toSelLen === 1) {
        $('.ud').attr('onclick', '');
        $('.ud').addClass('blocked');
    }
};

/* Converts implicit form control labeling to explicit by
 * adding an unique id to form controls if they don't already
 * have one and then setting the corresponding label element's
 * for attribute to form control's id value. This explicit 
 * linkage is better supported by adaptive technologies.
 * See SAK-18851.
 */
fixImplicitLabeling = function(){
    var idCounter = 0;
    $('label select,label input').each(function (idx, oInput) {
        if (!oInput.id) {
            idCounter++;
            $(oInput).attr('id', 'a11yAutoGenInputId' + idCounter.toString());
        }
        if (!$(oInput).parents('label').eq(0).attr('for')) {
            $(oInput).parents('label').eq(0).attr('for', $(oInput).attr('id'));
        }
    });
};

toggle_visibility = function(id) {
	   var e = document.getElementById(id);
	   var elabel = document.getElementById('toggle' + id);
	   if(e.style.display === 'block')
	   {
		  e.style.display = 'none';
		  elabel.src='/library/image/sakai/expand.gif';
		  elabel.title='</f:verbatim><h:outputText value="#{msgs.hideshowdesc_toggle_show}"/><f:verbatim>';
		  resizeFrame('shrink');
		}
	   else
	   {
		  e.style.display = 'block';
		  elabel.src='/library/image/sakai/collapse.gif';
		  elabel.title='</f:verbatim><h:outputText value="#{msgs.hideshowdesc_toggle_hide}"/><f:verbatim>';
		  resizeFrame();
		}  
	};
	
showHideDivBlock = function(hideDivisionNo, context, key)
	{
	  var tmpdiv = hideDivisionNo + "__hide_division_";
	  var tmpimg = hideDivisionNo + "__img_hide_division_";
	  var divisionNo = getTheElement(tmpdiv);
	  var imgNo = getTheElement(tmpimg);
	  if(divisionNo)
	 
	  {
	    if(divisionNo.style.display ==="block")
	    {
	      divisionNo.style.display="none";
	      if (imgNo)
	      {
	        imgNo.src = context + "/image/sakai/expand.gif";
	       }
	    }
	    else
	    {
	      divisionNo.style.display="block";
	      if(imgNo)
	      {
	        imgNo.src = context + "/image/sakai/collapse.gif";
	      }
	    }
	    resizeFrame('grow');
	    saveDivState(key, divisionNo.style.display);
	  }
	};
	
saveDivState = function (key, displayState) {
		var state = 0;
		
		if (displayState === "block")
			state = 1;
		
		jQuery.ajax({
	          type: "POST",
	          url: "/direct/userPrefs/1234/saveDivState",
	          data: {
	            key: key,
	            state: state
	          }
	        });
	};
	
getTheElement = function(thisid)
	{

	  var thiselm = null;

	  if (document.getElementById)
	  {
	    thiselm = document.getElementById(thisid);
	  }
	  else if (document.all)
	  {
	    thiselm = document.all[thisid];
	  }
	  else if (document.layers)
	  {
	    thiselm = document.layers[thisid];
	  }

	  if(thiselm)   
	  {
	    if(thiselm === null)
	    {
	      return;
	    }
	    else
	    {
	      return thiselm;
	    }
	  }
	};
	
	
resizeFrame = function (updown) {
	  var frame = parent.document.getElementById( window.name );
	  if( frame ) {
		if(updown==='shrink')
		{
		var clientH = document.body.clientHeight + 30;
	  }
	  else
	  {
	  var clientH = document.body.clientHeight + 30;
	  }
		$( frame ).height( clientH );
	  } else {
		window.console && console.log("no frame to resize");
	  }
};

var setupMultipleSelect = function(){
    $(".selectSiteCheck").click(function(e) {
            var pos= $(this).position();
            var thisCol = $(this).closest('.flc-reorderer-column').attr('id');
            var thisEl = $(this).closest('.flc-reorderer-module');

        if($(this).attr('checked')) {
            //only show actions that are germane to this specific column
            if(thisCol ==="reorderCol1") {
                // moving left disabled
                $('#movePanel').attr('class','col1Engaged');
                $('#movePanelLeftRight a').show();
                $('#movePanel span').hide();
                $('#movePanelLeft').hide();
                $('#movePanelLeftDummy').show();
                $('#movePanelTop').show();
                $('#movePanelBottom').show();
                $('#movePanelTopDummy').hide();
                $('#movePanelBottomDummy').hide();
              }
            else if(thisCol ==="reorderCol2") {
                // show all controls
                $('#movePanel').attr('class','col2Engaged');
                $('#movePanelLeftRight a').show();
                $('#movePanelLeftRight span').hide();
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
                $('#movePanel').attr('class','col3Engaged');
                $('#movePanelLeftRight a').show();
                $('#movePanelLeftRight span').hide();
                $('#movePanelRight').hide();
                $('#movePanelRightDummy').show();
                $('#movePanelTop').hide();
                $('#movePanelBottom').hide();
                $('#movePanelTopDummy').show();
                $('#movePanelBottomDummy').show();
            }
            // move the move panel down here 
            $('#movePanel').css({'top' : pos.top, 'left' : pos.left + 40,'display': 'block'});
            // uncheck the cbxs in the other 2 cols
            $('#layoutReorderer .selectSiteCheck').not('#' + thisCol + ' .selectSiteCheck').attr('checked',false);
            $('#layoutReorderer .last-login').not('#' + thisCol + ' .last-login').removeClass('siteSelected');
            $(thisEl).addClass('siteSelected');
        } 
        else {
            $(thisEl).removeClass('siteSelected');
            //hide #movePanel if all of the checkboxes are unchecked
            if( $(this).closest('.flc-reorderer-column').find(':checked').length ===0) {
                $('#movePanel').css('display','none');
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
             if($('#movePanel').attr('class') ==='col2Engaged'){
                 $(selectedItems).prependTo('#reorderCol3 > span');
             };
             postMoveCleanUp(selectedItems);
        })
        $('#movePanelLeft').click(function(e){
            e.preventDefault();
             var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             if($('#movePanel').attr('class') ==='col2Engaged'){
                $(selectedItems).prependTo('#reorderCol1 > span');
             }
             if($('#movePanel').attr('class') ==='col3Engaged'){
                 $(selectedItems).prependTo($('#reorderCol2 > span'));
             };
             postMoveCleanUp(selectedItems);
        })
        $('#movePanelTop').click(function(e){
             e.preventDefault();
             var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             var selectedCol = $(selectedItems).first().closest('span');
             $(selectedItems).prependTo(selectedCol);
             postMoveCleanUp(selectedItems);
        })
         $('#movePanelBottom').click(function(e){
             e.preventDefault();
             var selectedItems = $('#layoutReorderer :checked').closest('div.flc-reorderer-module');
             var selectedCol = $(selectedItems).first().closest('span');
             $(selectedItems).appendTo(selectedCol);
             postMoveCleanUp(selectedItems);
        })
var postMoveCleanUp = function(selectedItems) {
    var ids ='';
    $('.col1 .last-login').each(function(idx, item) {  
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
        if (idx > 0) {
            ids =ids + ', ' + $(this).attr('id');
        }
        else {
            ids =$(this).attr('id');
        }
    });
    $('input[name$=prefDrawerString]').val(ids);
    var ids ='';
    $('.col3 .last-login').each(function(idx, item) {  
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
    })    
     $('#layoutReorderer :checked').attr('checked',false);
     $('#movePanel').css('display','none');
     resizeFrame('grow');
}
}

setupPrefsGen = function(){
    if ($('.success').length) {
        $('.success').attr('tabindex','-1').fadeTo(5000,1).fadeOut(1000).css('outline','none').focus();
    }
    $('.formButton').click(function(e){
        $('.formButton').hide();
        $('.dummy').show();
    });
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
}

toggle_visibility = function(id) {
	   var e = document.getElementById(id);
	   var elabel = document.getElementById('toggle' + id);
	   if(e.style.display == 'block')
	   {
		  e.style.display = 'none';
		  elabel.src='/library/image/sakai/expand.gif'
		  elabel.title='</f:verbatim><h:outputText value="#{msgs.hideshowdesc_toggle_show}"/><f:verbatim>'
		  resizeFrame('shrink');
		}
	   else
	   {
		  e.style.display = 'block';
		  elabel.src='/library/image/sakai/collapse.gif'
		  elabel.title='</f:verbatim><h:outputText value="#{msgs.hideshowdesc_toggle_hide}"/><f:verbatim>'
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
	    if(divisionNo.style.display =="block")
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
		
		if (displayState == "block")
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
	    if(thiselm == null)
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
		if(updown=='shrink')
		{
		var clientH = document.body.clientHeight + 30;
	  }
	  else
	  {
	  var clientH = document.body.clientHeight + 30;
	  }
		$( frame ).height( clientH );
	  } else {
		throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
	  }
	};

    


//var demo = demo || {};
(function ($, fluid) {
    //fluid.setLogging(true);
    initlayoutReorderer = function () {
        fluid.reorderLayout("#layoutReorderer", {
            listeners: {
       	    	afterMove: function (args) {
					//resize iframe in case one of the lists made the doc higher
					resizeFrame('grow');
					// uncheck any selection that was checked and then moved via mouse or kbd 
					$(args).find('.selectSiteCheck').attr('checked',false)
    		    var ids = '';  
		    jQuery('.col1 .last-login').each(function(idx, item) {  
			// alert(item.id);
                        if ( ids.length > 1 ) ids += ', ' ;
        	        ids += item.id ; 
    		    });
            	    jQuery('input[name$=prefTabString]').val(ids);
    		    var ids = '';  
		    jQuery('.col2 .last-login').each(function(idx, item) {  
                        if ( ids.length > 1 ) ids += ', ' ;
        	        ids += item.id ; 
    		    });
            	    jQuery('input[name$=prefDrawerString]').val(ids);
    		    var ids = '';  
		    jQuery('.col3 .last-login').each(function(idx, item) {  
                        if ( ids.length > 1 ) ids += ', ' ;
        	        ids += item.id ; 
    		    });
            	    jQuery('input[name$=prefHiddenString]').val(ids);
       	        }
	    },
            selectors: {
                lockedModules: ".layoutReorderer-locked"
            },
            styles: {
                defaultStyle: "layoutReorderer-movable-default",
                selected: "layoutReorderer-movable-selected",
                dragging: "layoutReorderer-movable-dragging",
                mouseDrag: "layoutReorderer-movable-mousedrag",
                dropMarker: "layoutReorderer-dropMarker",
                avatar: "layoutReorderer-avatar"
            },
            disableWrap: true
        });
    };
})(jQuery, fluid);
    
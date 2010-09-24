setupPrefsGen = function(){
    if ($('.success').length) {
        $('.success').fadeOut(5000);
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

// page is /portal/site/nnn/tool/xxx
// however if you use that URL for ajax, you'll get a whole page
// you want /portal/tool/xxx
function fixLink(link) {
    var url = link.href;
    var i = url.indexOf("/site/");
    if (i >= 0) {
	var j = url.indexOf("/tool/");
	if (j >= 0) {
	    link.href = url.substring(0, i) + url.substring(j);
	}
    }
}

var MailSender = function()
{
	// attachment count keeps a net count of 
	var attachmentCount = 0;

	return {
		addAttachment : function(containerId)
		{
			// setup the screen to show differently if no attachments are showing
			if (attachmentCount == 0)
			{
				jQuery('#attachOuter').show();
				jQuery('#attachLink').hide();
				jQuery('#attachMoreLink').show();
			}
			Attachment.addAttachment(containerId);
			attachmentCount++;
		},

		removeAttachment : function(containerId, newDivId)
		{
			Attachment.removeAttachment(containerId, newDivId);
			attachmentCount--;
			if (attachmentCount == 0)
			{
				jQuery('#attachOuter').hide();
				jQuery('#attachLink').show();
				jQuery('#attachMoreLink').hide();
			}
		}
	}; // end return
}(); // end namespace

var Attachment = function()
{
	// attachment index will never decrease on a single request no matter how many
	// add/removes happen.  It is easier and safe to keep counting up than to rename and
	// replace removed numbers
	var attachmentIndex = 0;

	return {

		addAttachment : function(containerId)
		{
			// get a handle to the container
			var area = document.getElementById(containerId);

			// create the name for the div and the attachment field
			var newAttachmentId = 'attachment' + (attachmentIndex);
			var newDivId = newAttachmentId + 'Div';
			attachmentIndex++;

			// create the outer div element
			var newDiv = document.createElement('div');
			newDiv.id = newDivId;

			// create the file upload field
			var input = document.createElement('input');
			input.type = 'file';
			input.name = newAttachmentId;
			newDiv.appendChild(input);

			// create the remove link
			var link = document.createElement('a');
			link.className = 'removeAttachment';
			link.href = '#';
			link.onclick = function()
			{
				MailSender.removeAttachment(containerId, newDivId);
				return false;
			}
			link.innerHTML = 'Remove';
			newDiv.appendChild(link);

			// append the new div to the container
			area.appendChild(newDiv);
		},

		/**
		 * Remove a attachment div from a container
		 */
		removeAttachment : function(containerId, divId)
		{
			var area = document.getElementById(containerId);
			var div = document.getElementById(divId);
			area.removeChild(div);
		}
	}; // end return
}(); // end namespace

var Dirty = function()
{
	/**
	 * Collect all fossils on the page along with the user input fields
	 */
	function collectFossils()
	{
		var fossilex =  /^(.*)-fossil$/;
		var fossils = {};
		if (elements)
		{
			jQuery(':input').each(function()
			{
				var element = elements[i];
				// see if name exists and matches regex
				if (this.name)
				{
					var matches = this.name.match(fossilex);
					if (matches != null)
					{
						// use the name sans '-fossil' to store the element
						// this saves having to parse the field name again
						// later in processing.
						fossils[matches[1]] = this;
					}
				}
			});
		}
		return fossils;
	}

	return {
		isDirty : function()
		{
			var dirty = false;
			var fossilElements = collectFossils();
			var inputs = [];
			for (propName in fossilElements)
			{
				var fossilElement = fossilElements[propName];
				var fossil = RSF.parseFossil(fossilElement.value);
				jQuery(':' + propName).each(function()
				{
					if (((this.type == 'checkbox') && (this.checked != (fossil.oldvalue == "true")))
							|| ((this.type == 'select') && (this.options[this.selectedIndex].value != fossil.oldvalue))
							|| ((this.type == 'radio') && (this.checked) && (this.value != fossil.oldvalue))
							|| ((this.type == 'text' || this.type == 'textarea' || this.type == 'password') && (this.value != fossil.oldvalue)))
					{
						dirty = true;
						// return false to make jQuery stop processing loop
						return false;
					}
				});
				if (dirty) break;
			}
			return dirty;
		},

		check: function(msg)
		{
			var retval = true;
			if(Dirty.isDirty())
			{
				retval = confirm(msg);
			}
			return retval;
		}
	}; // end return
}(); // end namespace

var MailSenderUtil = function()
{
	return {
		/**
		 * Determine if a field is visible or hidden
		 *
		 * elementId: a element id
		 */
		isVisible : function(elementId)
		{
			var el = document.getElementById(elementId);
			return !(el.style.display == 'none' || el.style.visibility == 'hidden');
		},

		/**
		 * Hide a field.
		 *
		 * elementId: a element id or array of element ids
		 */
		hideElement : function(elementId)
		{
			if (elementId instanceof Array)
			{
				for (i in elementId)
					MailSenderUtil.hideElement(elementId[i]);
			}
			else
			{
				var el = document.getElementById(elementId);
				el.style.display = 'none';
				el.style.visibility = 'hidden';
			}
		},

		/**
		 * Show a field.
		 *
		 * elementId: a element id or array of element ids
		 * showInline: whether to show the element as inline or block
		 */
		showElement : function(elementId, showInline)
		{
			if (elementId instanceof Array)
			{
				for (i in elementId)
					MailSenderUtil.showElement(elementId[i]);
			}
			else
			{
				var el = document.getElementById(elementId);
				if (showInline)
					el.style.display = 'inline';
				else
					el.style.display = 'block';
				el.style.visibility = 'visible';
			}
		},

		/**
		 * Toggle the visibility of a field.
		 *
		 * elementId: a element id or array of element ids
		 */
		toggleElement : function(elementId)
		{
			if (elementId instanceof Array)
			{
				for (i in elementId)
					MailSenderUtil.toggleElement(elementId[i]);
			}
			else
			{
				if (MailSenderUtil.isVisible(elementId))
					MailSenderUtil.showElement(elementId);
				else
					MailSenderUtil.hideElement(elementId);
			}
		}
	}; // end return
}(); // end namespace

/* global jQuery */
var RcptSelect = function()
{
	// holds LinkResult objects
	var lastLink = null;
	var lastLinkReplacement = null;

	/**
	 * LinkResults object to hold data
	 *
	 * link: the link to follow (AJAX)
	 * content: the content returned after following link
	 *   showing/hiding results.  Expected values are >= 1.
	 */
	function LinkResult(link, content, resultSelector)
	{
		this.link = link;
		this.content = content;
		this.resultSelector = resultSelector;
	}

	/**
	 * Create an ID that is safe to use by jQuery.  This creates an ID string of the following
	 * syntax:<br/>
	 * <type>[id=<id>]
	 *
	 * @param string type The type of the element
	 * @param string id   The ID of the element.
	 */
	function _safeId(type, id)
	{
	    return type + '[id="' + id + '"]';
	}

	return {
		/**
		 * Show the results obtained from an ajax call.  If the call was made previously and the
		 * results still in the output area, that area is displayed.  Otherwise, the results are
		 * retrieved from the server.
		 *
		 * @param string  link     the link to follow for data retrieval.  should be an anchor object
		 * @param string  resultId the id of the field where results should be placed after retrieval
		 * @param boolean isTLM    flags this link as a top level menu
		 */
		showResults : function(link, resultId, isTLM)
		{
			// throw out the wait sign
			jQuery('body').css('cursor', 'wait');

			var resultArea = null;
			resultArea = jQuery(_safeId('', resultId));

			if (isTLM)
			{
				jQuery('.rolesArea').hide();
			}

			// if no cached content found, request content from server
			if (resultArea.html())
			{
				resultArea.show();
			}
			else
			{
				// setup the function that initiates the AJAX request
			    
			    fixLink(link);

			    var updater = RSF.getAJAXLinkUpdater(link, function(results)
					{
						// put the results on the page
						resultArea.html(results).show();
						// set the checkboxes based on the group level checkbox
						if (!isTLM) {
							RcptSelect.toggleSelectAll(jQuery(link).siblings('input[type=checkbox]:first').attr('id'));
						}
						// We do this in the callback (once the HTML has been added to the DOM).
						resetFrame();
					});

				// update the page
				updater();
			}

			// create a text version of the link
			var linkText = link.innerHTML;
			var linkTextNode = document.createTextNode(linkText);

			// set the last link to the current clicked link
			if (isTLM)
			{
				// make the last link a clickable link instead of text
				if (lastLink && lastLinkReplacement)
				{
					lastLinkReplacement.parentNode.replaceChild(lastLink, lastLinkReplacement);
				}
				lastLink = link;
				lastLinkReplacement = linkTextNode;

				// replace the link with just the text of the link
				link.parentNode.replaceChild(linkTextNode, link);
			}

			// take down the wait sign
			jQuery('body').css('cursor', 'default');
		},

		/**
		 * Show the 'other recipients' area.
		 *
		 * @return void
		 */
		showOther: function()
		{
			jQuery('#otherRecipientsDiv').show();
			jQuery('#otherRecipientsLink').hide();
			resetFrame();
		},

		/**
		 * Show individuals for a group as retrieved from the given link.
		 *
		 * @param string link           The link to follow for results to show.
		 * @param string usersAreaId    The area ID to place the results.
		 * @param string selectLinkId   The ID of the link to use for showing the group.
		 * @param string collapseLinkId The ID of the link to use for collapsing the group.
		 *
		 * @return void
		 */
		showIndividuals: function(link, usersAreaId, selectLinkId, collapseLinkId)
		{
			RcptSelect.showResults(link, usersAreaId, false);
			jQuery(_safeId('a', selectLinkId)).hide();
			jQuery(_safeId('a', collapseLinkId)).show();
			resetFrame();
		},

		/**
		 * Hide individuals for a group.
		 *
		 * @param string usersAreaId    The area ID to place the results.
		 * @param string selectLinkId   The ID of the link to use for showing the group.
		 * @param string collapseLinkId The ID of the link to use for collapsing the group.
		 *
		 * @return void
		 */
		hideIndividuals: function(usersAreaId, selectLinkId, collapseLinkId)
		{
			jQuery(_safeId('a', collapseLinkId) + ', ' + _safeId('div', usersAreaId)).hide();
			jQuery(_safeId('a', selectLinkId)).show();
			resetFrame();
		},

		/**
		 * Toggle the children checkboxes within a group based on the parent checkbox status.
		 *
		 * @param string checkboxId ID of the checkbox to use as a base for status.
		 *
		 * @return void
		 */
		toggleSelectAll: function(checkboxId)
		{
			if (!checkboxId)
			{
				var checked = jQuery('#mailsender-rcpt-all').attr('checked');
				jQuery('input[type=checkbox]:enabled', context).attr('checked', checked);
			}
			else
			{
				var checkbox = _safeId('input', checkboxId);
				var checked = jQuery(checkbox).is(':checked');
				var context = jQuery(checkbox + ' ~ div');
				jQuery('input[type=checkbox]:enabled', context).attr('checked', checked);
				if (!checked) {
					jQuery('#mailsender-rcpt-all').attr('checked',false);
				}
			}
		},

		/**
		 * Toggle the checkbox status for a child checkbox.  If all enabled children are selected,
		 * the parent checkbox is selected.
		 *
		 * @param string checboxId The ID of the checkbox to check.
		 *
		 * @return void
		 */
		toggleIndividual: function(checkboxId)
		{
			var checkbox = jQuery(_safeId('input', checkboxId));
			/*
			the individual checkboxes are nested down some divs.
			the structure looks like:
			<input id=<selectAll> />
			<div> <-- parent -->
			  <div> <-- parent -->
			    <div> <-- parent -->
			      <input id=<individual> />
			    </div>
			  </div>
			</div>
			*/
			var selectAll = checkbox.parent().parent().parent().siblings('input[type=checkbox]');
			if (!checkbox.is(':checked'))
			{
				selectAll.attr('checked', false);
				jQuery('#mailsender-rcpt-all').attr('checked',false);
			}
			else
			{
				/*
				have to go up 2 parents because each checkbox is held in a div of divs
				<div>
				  <div>
				    <input type=checkbox />
				    <label />
				  </div>
				  <div>
				    <input type=checkbox />
				    <label />
				  </div>
				</div>
				*/
				var allChecked = true;
				jQuery('input[type=checkbox]:enabled', checkbox.parent().parent()).each(function()
					{
						if (!this.checked)
						{
							allChecked = false;
							return false;
						}
					});
				if (allChecked)
				{
					selectAll.attr('checked', true);
				}
			}
		}
	}; // end return
}(); // end namespace

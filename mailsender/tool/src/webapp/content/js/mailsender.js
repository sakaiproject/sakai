var MailSender = function() {
	// attachment count keeps a net count of 
	var attachmentCount = 0;

	return {
		addAttachment : function(containerId) {
			// setup the screen to show differently if no attachments are showing
			if (attachmentCount == 0) {
				jQuery('#attachOuter').show();
				jQuery('#attachLink').hide();
				jQuery('#attachMoreLink').show();
			}
			Attachment.addAttachment(containerId);
			attachmentCount++;
		},

		removeAttachment : function(containerId, newDivId) {
			Attachment.removeAttachment(containerId, newDivId);
			attachmentCount--;
			if (attachmentCount === 0) {
				jQuery('#attachOuter').hide();
				jQuery('#attachLink').show();
				jQuery('#attachMoreLink').hide();
			}
		}
	}; // end return
}(); // end namespace

var Attachment = function() {
	// attachment index will never decrease on a single request no matter how many
	// add/removes happen.  It is easier and safe to keep counting up than to rename and
	// replace removed numbers
	var attachmentIndex = 0;

	let accumulatedFileSize = 0;
	let maxFileUploadSize;	
	document.addEventListener("DOMContentLoaded", () => {
		if (document.getElementById('max-attachment-size')) {
			maxFileUploadSize = document.getElementById('max-attachment-size').textContent;
		}
	});

	function msg(targetElId) {
		const targetEl = document.getElementById(targetElId);
		if (targetEl === null) {
			return targetElId;
		} else {
			return targetEl.innerHTML;
		}
	}
	
	function formatFileSize(bytes) {
		if (bytes === 0) { 
			return msg("mailsender.zero_bytes");
		}
		const K = 1000,
		sizes = msg("mailsender.unit_sizes").split(','),
		i = Math.floor(Math.log(bytes) / Math.log(K));
		return parseFloat((bytes / Math.pow(K, i)).toFixed(1)) + ' ' + sizes[i];
	}
	
	function accumulateAndCheckAttachmentSize() {
		let wasOverUploadSize = accumulatedFileSize > maxFileUploadSize;

		accumulatedFileSize = 
			[...document.querySelectorAll(".emailattachment")]
			.filter((el) => el.files.length > 0)
			.reduce((acc, el) => acc + el.files[0].size, 0);

		const isOverUploadSize = accumulatedFileSize > maxFileUploadSize ;
	
		if (wasOverUploadSize && !isOverUploadSize) {
			document.getElementById("attach-size-error").style.display = "none";
			document.querySelectorAll("input[type=submit]").forEach((el) => el.disabled = false);
			document.querySelector("#attachOuter img[alt='attachment_img']").classList.remove("disable-attach-more-icon");
			document.querySelector('#attachMoreLink button').classList.remove("disable-attach-more-link");
		} else if (!wasOverUploadSize && isOverUploadSize) {
			document.getElementById("attach-size-error").textContent = msg("mailsender.attachment_size_limit") + ' ' + (Math.round(((maxFileUploadSize/1000.0)/1000.0) * 10) / 10.0).toFixed(1) + ' ' + msg("mailsender.attachment_size_limit_end");
			document.querySelectorAll("input[type=submit]").forEach((el) => el.disabled = true);
			document.querySelector("#attachOuter img[alt='attachment_img']").classList.add("disable-attach-more-icon");
			document.querySelector('#attachMoreLink button').classList.add("disable-attach-more-link");
			document.getElementById("attach-size-error").style.display = "block";
		}
	}
	
	function emailattachmentChange() {
		
		accumulateAndCheckAttachmentSize();
	
		let elSaveSize = this.nextElementSibling;
		let elDisplaySize = this.nextElementSibling.nextElementSibling;
		
		if (this.files.length > 0) {
			let saveSize = this.files[0].size;
			elSaveSize.innerText = saveSize;
			elDisplaySize.innerText = formatFileSize(saveSize);
		} else {
			elSaveSize.innerText = "";
			elDisplaySize.innerText = "";
		}
	}

	return {

		addAttachment : function(containerId) {

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
			input.name = 'attachment';
			input.classList.add('emailattachment');
			input.style.display = 'inline-block';
			newDiv.appendChild(input);
			input.onchange = emailattachmentChange;

			var newSpan = document.createElement('span');
			newSpan.classList.add('emailattachment-size-save');
			newSpan.style.display = 'none';
			newDiv.appendChild(newSpan);
			
			newSpan = document.createElement('span');
			newSpan.classList.add('emailattachment-size');
			newSpan.classList.add('h6');
			newSpan.style.display = 'inline-block';
			newDiv.appendChild(newSpan);

			// create the remove link
			var link = document.createElement('a');
			link.className = 'removeAttachment';
			link.href = '#';
			link.onclick = function() {
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
		removeAttachment : function(containerId, divId) {
			var area = document.getElementById(containerId);
			var div = document.getElementById(divId);
			area.removeChild(div);
			accumulateAndCheckAttachmentSize();
		}
	}; // end return
}(); // end namespace

/* global jQuery */
var RcptSelect = function() {
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
	function LinkResult(link, content, resultSelector) {
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
	function _safeId(type, id) {
	    return type + '[id="' + id + '"]';
	}

	return {
		
		/**
		 * Show the 'other recipients' area.
		 *
		 * @return void
		 */
		showOther: function() {
			jQuery('#otherRecipientsDiv').show();
			jQuery('#otherRecipientsLink').hide();
		},

		/**
		 * Toggle the children checkboxes within a group based on the parent checkbox status.
		 *
		 * @param string checkboxId ID of the checkbox to use as a base for status.
		 *
		 * @return void
		 */
		toggleSelectAll: function(checkboxId) {
			let rcptAll = jQuery('#mailsender-rcpt-all')
			if (!checkboxId) {
				var checked = rcptAll.attr('checked');
				if (checked) {
					// Check all boxes in this section
					jQuery('input[type=checkbox]', rcptAll.parents('.section')).attr('checked', true);
				} else {
                    jQuery('input[type=checkbox]', rcptAll.parents('.section')).attr('checked', false);
                }
			} else {
				var checkbox = _safeId('input', checkboxId);
				var checked = jQuery(checkbox).is(':checked');
				var context = jQuery(checkbox + ' ~ ul > li');
				jQuery('input[type=checkbox]:enabled', context).attr('checked', checked);
				if (!checked) {
					rcptAll.attr('checked',false);
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
		toggleIndividual: function(checkboxId) {
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
			var selectAll = checkbox.parent().parent().siblings('input[type=checkbox]');
			if (!checkbox.is(':checked')) {
				selectAll.attr('checked', false);
				jQuery('#mailsender-rcpt-all').attr('checked',false);
			} else {
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
				jQuery('input[type=checkbox]:enabled', checkbox.parent().parent()).each(function() {
						if (!this.checked) {
							allChecked = false;
							return false;
						}
					});
				if (allChecked) {
					selectAll.attr('checked', true);
				}
			}
		}
	}; // end return
        
}(); // end namespace
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
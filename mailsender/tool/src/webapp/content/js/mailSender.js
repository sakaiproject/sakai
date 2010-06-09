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
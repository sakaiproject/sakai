var ASN_SVS = ASN_SVS || {};

/* For the cancel button - if the user made progress, we need them to confirm that they want to discard their progress */
ASN_SVS.confirmDiscardOrSubmit = function(attachmentsModified)
{
	var inlineProgress = false;
	var ckEditor = CKEDITOR.instances["$name_submission_text"];
	if (ckEditor)
	{
		inlineProgress = CKEDITOR.instances["$name_submission_text"].checkDirty();
	}
	var showDiscardDialog = inlineProgress || attachmentsModified;
	var submitPanel = document.getElementById("submitPanel");
	var confirmationDialogue = document.getElementById("confirmationDialogue");
	if (showDiscardDialog)
	{
		submitPanel.setAttribute('style', 'display:none;');
		confirmationDialogue.removeAttribute('style');
	}
	else
	{
		ASN.submitForm( 'addSubmissionForm', 'cancel', null, null );
	}
};

ASN_SVS.undoCancel = function()
{
	var submitPanel = document.getElementById("submitPanel");
	var confirmationDialogue = document.getElementById("confirmationDialogue");
	submitPanel.removeAttribute('style');
	confirmationDialogue.setAttribute('style', 'display:none;');
};

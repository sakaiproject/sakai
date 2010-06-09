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
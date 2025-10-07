$(function() {
	//get all divs with class forum-summary-div
	$(".forum-summary-div").each(function(){
		var rightColDiv = $(this).parent().parent();
		//Get parameters for each forum-summary-div
		var itemsToShow = rightColDiv.find('.numberOfConversations').text().replace(/'/g,"");
		var forumsUrl = rightColDiv.find('.forum-summary-site-url').text().replace(/'/g,"");
		var toolHref = rightColDiv.find('.forum-summary-view-url').text().replace(/'/g,"");
		showForums(forumsUrl, toolHref, itemsToShow, $(this));
	});
});

function showForums(forumsUrl, toolHref, itemsToShow, forumSummaryDiv  ){
	//make ajax request only when forum widget is added to the page
	if(forumsUrl.length){
		forumsUrl += '.json?n=' + itemsToShow;
		var messagesArray = [];
		var errorText;
		//Get forums
		$.ajax({
			url: forumsUrl,
			dataType: 'json',
			cache: false,
			success: function(data) {
				$(data["forums_collection"]).each(function(fc_index, fc_value){
					var author;
					if(fc_value['createdBy'].indexOf('(') > -1){
						author = fc_value['createdBy'].split('(')[0];
					}else{
						author = fc_value['createdBy'];
					}
					//escape markup
					var entityTitle = fc_value['entityTitle'].replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;');
					//added forumId , topicId and messageId into the messageArray to link the message into the forum tool
					var messageObject = {forumId: fc_value["forumId"],
										entityTitle: entityTitle,
										entityUrl: fc_value['entityURL'],
										lastModified: fc_value['lastModified'],
										author: author,
										topicId: fc_value['topicId'],
										messageId: fc_value['messageId']};
					messagesArray.push(messageObject);
				});
				outputForums(messagesArray, toolHref, forumSummaryDiv);
				setTimeout('showForums()', 300000);
			},
			error: function(xhr, textStatus, errorThrown){
				var err = textStatus + ", " + errorThrown;
				var error_for_forums = '<p>'+ msg("simplepage.forum-summary-error-message") + err +'</p>';
				forumSummaryDiv.html(error_for_forums);
			}
		});
	}
}
function outputForums(messagesArray, toolHref, forumSummaryDiv) {
	const title = msg("simplepage.forum-header-title");
	const fragments = [];
	fragments.push(`<div class="forumSummaryHeaderDiv"><h3 class="forumSummaryHeader"><span aria-hidden="true" class="fa-item-text icon-sakai--sakai-forums"></span><a href="${toolHref}" class="forumSummaryLink" title="${title}">${title}</a></h3></div>`);

	if (messagesArray.length === 0) {
		fragments.push(`<p>${msg("simplepage.forum-summary-no-message")}</p>`);
	} else {
		const sortedMessages = messagesArray.slice().sort((a, b) => Number(b.lastModified || 0) - Number(a.lastModified || 0));
		fragments.push('<ul class="forumSummaryList list-group list-group-flush">');
		for (const message of sortedMessages) {
			const dateTime = message.lastModifiedDisplay || '';
			const href = `${toolHref}&messageId=${message.messageId}&topicId=${message.topicId}&forumId=${message.forumId}`;
			fragments.push(`<li class="itemDiv forumSummaryItem list-group-item d-block mb-2"><div><a class="fw-semibold" href="${href}">${message.entityTitle}</a> <span class="text-muted">${msg("simplepage.created-by")} ${message.author}</span></div><div class="forumSummaryDate text-muted small">${dateTime}</div></li>`);
		}
		fragments.push('</ul>');
	}

	forumSummaryDiv.html(fragments.join('\n'));
}

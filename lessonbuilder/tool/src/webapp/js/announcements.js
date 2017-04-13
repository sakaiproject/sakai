$(function(){
	//get all divs with class announcementType
	$(".announcements-div").each(function(){
		var rightColDiv = $(this).parent().parent();
		//Get parameters for each announcements div
		var number = rightColDiv.find('.numberOfAnnouncements').text().replace(/'/g,"");
		var url = rightColDiv.find(".announcements-site-url").text().replace(/'/g,"");
		var tool_href = rightColDiv.find(".announcements-view-url").text().replace(/'/g,"");
		showAnnouncements(url, tool_href, number,  $(this));
	});
});

function showAnnouncements(url, tool_href, number, announcementsDiv){
	//only make ajax request if announcement widget is added
	if(url.length){
		var announcementsUrl = url + ".json?n=" + number;
		//get the announcement tool url
		var link_to_tool = tool_href.split("?", 1);
		var title = msg("simplepage.announcements-header-title");
		var text_for_announcements = '<div class="announcementsHeaderDiv"><h3 class="announcementSummaryHeader"><span aria-hidden="true" class="fa-item-text icon-sakai--sakai-announcements"></span><a href="'+link_to_tool+'" target="_top" class="announcementLink" title ="'+title+'">'+title+'</a></h3></div><ul class="announcementSummaryList">';
		//Get announcements
		$.ajax({
			url: announcementsUrl,
			dataType: 'json',
			cache: false,
			success: function(data) {
				if($(data["announcement_collection"]).size() === 0) {
					//ie no announcements
					text_for_announcements += '<p>'+msg("simplepage.announcements-no-message")+'</p>';
				}
				else {
					$(data["announcement_collection"]).each(function(){
						//create a new javascript Date object based on the timestamp
						date = new Date(this["createdOn"]);
						var hour = date.getHours() < 10 ? '0' + date.getHours() : date.getHours();
						var min = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
						//using javascript's toLocaleDateString() to include user's locale and local time zone
						date_time = hour +":"+min+ " " + date.toLocaleDateString();
						text_for_announcements += '<li class="itemDiv announcementSummaryItem">';
						var href = tool_href + this["announcementId"]+"&sakai_action=doShowmetadata";
						var entityTitle = this["entityTitle"].replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;');
						var createdByDisplayName = this["createdByDisplayName"].replace(/&/g, '&amp;').replace(/>/g, '&gt;').replace(/</g, '&lt;').replace(/"/g, '&quot;');
						text_for_announcements += '<div><a href="'+href+'" target="_top">'+ entityTitle +'</a> by '+ createdByDisplayName +'</div>';
						text_for_announcements += '<div class="itemDate">'+date_time+'</div>';
						text_for_announcements += '</li>';
					});
				}
		                text_for_announcements += '</ul>';
				announcementsDiv.html(text_for_announcements);
			},
			error: function(xhr, textStatus, errorThrown){
				var err = textStatus + ", " + errorThrown;
				text_for_announcements += '<p>'+ msg("simplepage.announcements-error-message") + err +'</p>';
		                text_for_announcements += '</div>';
				announcementsDiv.html(text_for_announcements);
			}
		});
	}
}
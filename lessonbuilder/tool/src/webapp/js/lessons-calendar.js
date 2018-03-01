$(function(){
    var url = $('.site-events-url').first().text().replace(/'/g,"");
    var moreInfoUrl = $('.event-tool-url').text().replace(/'/g,"");
    //get user locale from browser to customise text in calendar
    var language = window.navigator.userLanguage || window.navigator.language;
    $('.calendar-div').fullCalendar({
        displayEventTime: false,
        lang: language,
        header:
        {
            left: 'prev,next today',
            center: 'title',
            right: 'month,agendaWeek,agendaDay'
        },
        eventSources: [
            {
                events: function(start, end, timezone, callback) {
                    var start_date =  $('.calendar-div').fullCalendar('getView').start.format('YYYY-MM-DD');
                    var end_date  =   $('.calendar-div').fullCalendar('getView').end.format('YYYY-MM-DD');
                    $.ajax({
                        url: url+'.json',
                        dataType: 'json',
                        data:{ merged: "true", firstDate: start_date, lastDate:end_date},
                        cache: false,
                        success: function(data) {
                            var events = [];
                            $(data["calendar_collection"]).each(function() {
                                var startTime = this["firstTime"]["time"];
                                var startDate = new Date(startTime);
                                var endTime = this["firstTime"]["time"] + this["duration"] ;
                                var endDate = new Date();
                                endDate.setTime(endTime);
                                events.push({
                                    title: this["title"],
                                    description: this["description"],
                                    descriptionFormatted: this["descriptionFormatted"],
                                    start: startDate,
                                    site_name: this["siteName"],
                                    type: this["type"],
                                    icon: this["eventIcon"],
                                    event_id: this["eventId"],
                                    attachments: this["attachments"],
                                    eventReference: this["eventReference"],
                                    end: endDate
                                });
                            });
                            callback(events);
                        }
                    });
                },
                color: '#D4DFEE',
                textColor: '#0064cd'
            }
        ],
        eventClick: function(event, jsEvent, view){
            //to adjust startdate and enddate as per user's locale
            var startDate = new Date(event.start).toLocaleString();
            var endDate = new Date(event.end).toLocaleString();
            $("#startTime").text(moment(startDate, 'DD/MM/YYYY, HH:mm:ss').format('DD-MM-YYYY hh:mm A'));
            $("#endTime").text(moment(endDate, 'DD/MM/YYYY, hh:mm:').format('DD-MM-YYYY hh:mm A'));
            $('#event-type-icon').attr("class","icon " + event.icon);
            $("#event-type").text(event.type);
            $("#event-description").html(event.descriptionFormatted);
            $("#site-name").text(event.site_name);
            //if event has attachment show attachment info div , else hide it
            if(event.attachments.length >= 1){
                var attachments = "<br><ul class='eventAttachmentList'>";
                var altMessage = msg("simplepage.eventAttachments");
                for(i=0; i< event.attachments.length; i++){
                    var href = event.attachments[i].url;
                    var filename = href.split('/');
                    filename = filename[filename.length-1];
                    attachments += '<li class="eventAttachmentItem"><a href="'+href+'" target="_blank" alt="' + altMessage + '"><span class="icon icon-calendar-attachment"></span>'+
                                   '<span class="eventAttachmentFilename">'+filename+'</span></a></li>';
                }
                attachments += "</ul>"
                $('#event-attachments').html(attachments);
                $("#eventAttachmentsDiv").show();
            }
            else{
                $("#eventAttachmentsDiv").hide();
            }
            var more_info = moreInfoUrl + event.eventReference + "&panel=Main&sakai_action=doDescription&sakai.state.reset=true";
            var fullDetailsText = msg("simplepage.calendar-more-info");
            //when Full Details is clicked, event in the Calendar tool is shown.
            $("#fullDetails").html("<a href=" + more_info + " target=_top>" + fullDetailsText + "</a>");
            //On event click dialog is opened near the event
            $("#calendarEventDialog").dialog({ modal: true, title: event.title, width: 400 });
        }
    });
});

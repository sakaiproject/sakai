$(function(){
    var url = $('.site-events-url').first().text().replace(/'/g,'');
    var moreInfoUrl = $('.event-tool-url').text().replace(/'/g,'');

    var $elements = $('.calendar-div');
    if ($elements.length > 0) {
        $elements.each(function() {
            var calendar = new FullCalendar.Calendar(this, {
                plugins: ['interaction', 'dayGrid', 'timeGrid'],
                defaultView: 'dayGridMonth',
                displayEventTime: false,
                header: {
                    left: 'prev,next today',
                    center: 'title',
                    right: 'dayGridMonth,timeGridWeek,timeGridDay'
                },
                eventSources: [{
                    events: function(event, successCallback, failureCallback) {
                        var start_date = moment(event.start).format('YYYY-MM-DD');
                        var end_date = moment(event.end).format('YYYY-MM-DD');
                        $.ajax({
                            url: url+'.json',
                            dataType: 'json',
                            data:{ merged: 'true', firstDate: start_date, lastDate:end_date},
                            cache: false,
                            success: function(data) {
                                var events = [];
                                $(data['calendar_collection']).each(function() {
                                    var startTime = this['firstTime']['time'];
                                    var startDate = new Date(startTime);
                                    var endTime = this['firstTime']['time'] + this['duration'] ;
                                    var endDate = new Date();
                                    endDate.setTime(endTime);
                                    events.push({
                                        title: this['title'],
                                        description: this['description'],
                                        descriptionFormatted: this['descriptionFormatted'],
                                        start: startDate,
                                        site_name: this['siteName'],
                                        type: this['type'],
                                        icon: this['eventIcon'],
                                        event_id: this['eventId'],
                                        attachments: this['attachments'],
                                        eventReference: this['eventReference'],
                                        end: endDate
                                    });
                                });
                                successCallback(events);
                            }
                        });
                    },
                    color: '#D4DFEE',
                    textColor: '#0064cd'
                }],
                eventSourceSuccess: function(content, xhr) {
                    return content.eventArray;
                },
                eventClick: function(eventClick){
                    //to adjust startdate and enddate as per user's locale
                    var startDate = new Date().toLocaleString();
                    var endDate = new Date(eventClick.event.end).toLocaleString();
                    $('#startTime').text(moment(eventClick.event.start).format('LLLL'));
                    $('#endTime').text(moment(eventClick.event.end).format('LLLL'));
                    $('#event-type-icon').attr('class','icon ' + eventClick.event.extendedProps.icon);
                    $('#event-type').text(eventClick.event.extendedProps.type);
                    $('#event-description').html(eventClick.event.extendedProps.descriptionFormatted);
                    $('#site-name').text(eventClick.event.extendedProps.site_name);
                    //if event has attachment show attachment info div , else hide it
                    if (eventClick.event.extendedProps.attachments.length >= 1) {
                        var attachments = '<br><ul class="eventAttachmentList">';
                        var altMessage = msg('simplepage.eventAttachments');
                        for(i=0; i< eventClick.event.extendedProps.attachments.length; i++){
                            var href = eventClick.event.extendedProps.attachments[i].url;
                            var filename = href.split('/');
                            filename = filename[filename.length-1];
                            attachments += '<li class="eventAttachmentItem"><a href="'+href+'" target="_blank" alt="' + altMessage + '"><span class="icon icon-calendar-attachment"></span>'+
                                           '<span class="eventAttachmentFilename">'+filename+'</span></a></li>';
                        }
                        attachments += '</ul>';
                        $('#event-attachments').html(attachments);
                        $('#eventAttachmentsDiv').show();
                    } else {
                        $('#eventAttachmentsDiv').hide();
                    }
                    var more_info = moreInfoUrl + eventClick.event.extendedProps.eventReference + '&panel=Main&sakai_action=doDescription&sakai.state.reset=true';
                    var fullDetailsText = msg('simplepage.calendar-more-info');
                    //when Full Details is clicked, event in the Calendar tool is shown.
                    $('#fullDetails').html('<a href=' + more_info + ' target=_top>' + fullDetailsText + '</a>');
                    //On event click dialog is opened near the event
                    $('#calendarEventDialog').dialog({ modal: true, title: eventClick.event.title, width: 400 });
                }
            });
            calendar.render();
        });
    }
});

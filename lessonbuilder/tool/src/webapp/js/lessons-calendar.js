$(function(){
    let url = $('.site-events-url').first().text().replace(/'/g,'');
    let moreInfoUrl = $('.event-tool-url').text().replace(/'/g,'');

    const $elements = $('.calendar-div');
    if ($elements.length > 0) {
        $elements.each(function() {
            const calendar = new FullCalendar.Calendar(this, {
                initialView: 'dayGridMonth',
                displayEventTime: false,
                allDaySlot: false,
                themeSystem: 'bootstrap5',
                headerToolbar: {
                    left: 'prev,next today',
                    center: 'title',
                    right: 'dayGridMonth,timeGridWeek,timeGridDay'
                },
                buttonIcons: {
                    /*Use of bootstrap5 as themeSystem will expect bootstrap icons and prepend bi bi-*/
                    prev: 'chevron-left',
                    next: 'chevron-right',
                },

                eventSources: [{
                    events: function(event, successCallback, failureCallback) {
                        const start_date = moment(event.start).format('YYYY-MM-DD');
                        const end_date = moment(event.end).format('YYYY-MM-DD');
                        $.ajax({
                            url: url+'.json',
                            dataType: 'json',
                            data:{ merged: 'true', firstDate: start_date, lastDate:end_date},
                            cache: false,
                            success: function(data) {
                                let events = [];
                                $(data['calendar_collection']).each(function() {
                                    const startTime = this['firstTime']['time'];
                                    const startDate = new Date(startTime);
                                    const endTime = this['firstTime']['time'] + this['duration'] ;
                                    const endDate = new Date(endTime);
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
                // https://fullcalendar.io/docs/eventClick
                eventClick: function(eventClick){
                    //to adjust startdate and enddate as per user's locale
                    $('#startTime').text(moment(eventClick.event.start).format('LLLL'));
                    if (eventClick.event.end) {
                        $('#endTime').text(moment(eventClick.event.end).format('LLLL'));
                    } else {
                        $('#endTime').hide();
                        $('label[for="endTime"]').hide();
                    }
                    $('#event-type-icon').attr('class','icon ' + eventClick.event.extendedProps.icon);
                    $('#event-type').text(eventClick.event.extendedProps.type);
                    $('#event-description').html(eventClick.event.extendedProps.descriptionFormatted);
                    $('#site-name').text(eventClick.event.extendedProps.site_name);
                    //if event has attachment show attachment info div , else hide it
                    if (eventClick.event.extendedProps.attachments.length >= 1) {
                        let attachments = '<br><ul class="eventAttachmentList">';
                        let altMessage = msg('simplepage.eventAttachments');
                        for(i=0; i< eventClick.event.extendedProps.attachments.length; i++){
                            let href = eventClick.event.extendedProps.attachments[i].url;
                            let filename = href.split('/');
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
                    let more_info = moreInfoUrl + eventClick.event.extendedProps.eventReference + '&panel=Main&sakai_action=doDescription&sakai.state.reset=true';
                    let fullDetailsText = msg('simplepage.calendar-more-info');
                    //when Full Details is clicked, event in the Calendar tool is shown.
                    $('#fullDetails').html('<a href=' + more_info + ' target=_top>' + fullDetailsText + '</a>');
                    //On event click dialog is opened near the event
                    const modalEl = document.querySelector("#calendarEventDialog");
                    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                    const title = document.getElementById("calendarEventModalLabel");
                    title && (title.innerText = eventClick.event.title);
                    modal.show();
                }
            });
            calendar.render();
        });
    }
});

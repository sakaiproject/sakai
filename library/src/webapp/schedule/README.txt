
Generic Calendar import file explanation


The first line of your file must be a comma separated list of column headers.
You must use the headers specified in this file.  If you add your own header,
the import will attempt to match that header to a custom field defined on the
calendar.  If there is no match, the column will be ignored.


Column Explanation:

Title: This is a short title for your event.  It will always appear, whereas
the description will only appear where space allows.  If your title contains
commas, you must put the field in quotes.  This field is required.

Description: This is a more detailed description of your event. If your description
contains commas, this field must be in double-quotes.  This field is optional.

Date:  This is the date of the event in the form mm/dd/yy or mm/dd/yyyy.  For example,
6/7/04 or 6/7/2004 would both be fine for an event starting on June 7th of 2004.  This
field is required.

Start: This is the start time of the event in the form hh:mm AM/PM.  For example,
9:00 AM or 1:00 PM would be valid start times.  This field is required.

Duration: This is the duration of the event in hours and minutes.  This is in the form
hh:mm or just mm for events lasting less than an hour.  This field is required.

Type: This is the type of event.  Values for this field are the following:
event.activity = "Activity"
event.exam ="Exam"
event.meeting ="Meeting"
event.academic.calendar="Academic Calendar"
event.cancellation="Cancellation"
event.discussion="Class section - Discussion"
event.lecture="Class section - Lecture"
event.class="Class session"
event.computer="Computer Session"
event.deadline="Deadline"
event.formative="Formative Assessment"
event.conference="Multidisciplinary Conference"
event.quiz="Quiz"
event.special="Special event"
event.submission="Submission Date"
event.tutorial="Tutorial"
event.assignment="Web Assignment"
event.workshop="Workshop"

Location: This can be anything that you like.  You could put building/room numbers,
city, state, or anything that you wish to show up as the location of the event.  If
your location contains commas, you must put this field in double-quotes.  This
field is optional.

Repeating Events Columns - These are optional if you have no repeating events.

Frequency: this can be the words "day", "week", "month", or "year"

Interval:  This is best described by examples.  If your frequency is "daily" and
your interval is 1, then the event will recur every day.  If the interval is 2,
the event will recur every other day.  If the interval is 3, the event will occur
every third day.  The same holds true for other frequencies.  If your frequency
is "weekly" and your interval is 2, you will have an event that recurs every other
week.

Ends: This is the last date on which the recurring event will occur.  This date is in
the same form as the "Date" field, form mm/dd/yy or mm/dd/yyyy.  For example,
6/7/04 or 6/7/2004 would both be correct June 7th of 2004.  Do not use this field
if you are specifying a value for the "Repeat" field below.

Repeat:  This is the number of times that an event will repeat.  Do not use this field
if you are also using the "Ends" fields.  For example, it would make no sense to give
a daily event that repeats 3 times and ending date of 2/10/2005 if the start date
of the event occurs on 6/7/2004.

Please see the example_import_file.txt for an example of this format.



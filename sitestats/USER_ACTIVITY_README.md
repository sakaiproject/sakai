# User Activity

These are the implementation notes for the User Activity feature of SiteStats, also referred to internally as "Detailed Events".

## Overview

While the SiteStats tool provides totals of particular events that occur in the site for statistical purposes,
many users want more detail about the events in question. This is typically needed to investigate questions of
academic integrity.

User Activity provides a way to see exact timestamps for the events recorded (as opposed to just a daily total), and in
many cases see additional details about the event, such as the entity that the event relates to. For example,
instead of only knowing that Jane Doe read a lesson yesterday, you could see that Jane Doe read the "Week 3" lesson at 12:48pm.

## Setup

### Properties

There are two Sakai properties that govern this feature:

collectDetailedEvents@org.sakaiproject.sitestats.api.StatsUpdateManager.target=<true/false>
displayDetailedEvents@org.sakaiproject.sitestats.api.StatsManager.target=<true/false>

CollectDetailedEvents determines whether or not event details are stored in the database. You may want to enable this property ahead of time
to make sure the collection works properly and to populate some event data before enabling the User Activity feature for users.

DisplayDetailedEvents determines, in combination with user permissions, if the User Activity page is displayed in SiteStats. This can
be used as a master switch to disable the feature completely without having to change user permissions.

If your sakai_event table stores dates using a timezone that is different than your server time (ie. UTC), you will also want
to set the following property so that event times are displayed correctly in User Activity:
sakaiEventTimeZone@org.sakaiproject.sitestats.api.StatsAggregateJob=<time zone identifier>

Example if your sakai_event table stores dates in UTC: sakaiEventTimeZone@org.sakaiproject.sitestats.api.StatsAggregateJob=UTC

See SAK-40430 for more details about this property.

### Permissions

There are two permissions that are required for this feature:

sitestats.usertracking.track - determines if the user can access and use the User Activity page
sitestats.usertracking.be.tracked - determines if the user's events can be shown on the User Activity page

Generally, site maintainers should be granted only "track", while students should be granted only "be.tracked".

## Goals

The implementation of the User Activity feature has four main goals:

### Performance

There is often a vast amount of data to search through when looking for particular events. A single site can end up producing millions of events.
Therefore, it can be very easy to get into a situation where response time is extremely poor. This leads to a bad user experience and may
cause users to abandon future attempts to use User Activity.

Several methods are used to combat this situation:

* Data from the sakai session and event tables are copied into a dedicated "detailed events" table which contains only
the data needed by User Activity. This avoids a join when querying this data and allows for effective indexing.
* The UI restricts queries to a particular user and encourages limiting searches to a particular timeframe. This guides
the user to make a query that takes advantage of the underlying table indexing. The UI also prevents sorting on any
attribute other than timestamp, as this sort follows closely to the natural order of the data and is very quick.
To narrow down the search, filtering by tool is also provided.
* A custom Wicket component, SakaiInfinitePagingDataTable, was created to take advantage of true paging at the database
level. Standard Wicket DataTable components require knowledge of the full size of the dataset, which can be
extremely expensive with large amounts of data, even when well indexed. This table only queries for a number of
results matching the page size, allowing it to return quickly and always feel responsive to the user.
* Event detail lookups are performed on demand instead of as part of the search. In most cases the user does not know
what they need more detail about ahead of time, so a button is provided that they can click for more detail about a
particular event in the results, but otherwise time is not wasted finding details for events the user doesn't care about.

### Accuracy

It is very important to be able to correctly identify the entity the event refers to in the UI of the tool.
When possible, a link directly to the entity or the entity's parent container is provided. In other cases, enough
information to uniquely identify the entity is provided. For example, the data provided about a Lessons comment includes
the time posted and a snippet of the comment contents.

### Visibility

Users should not be able to see information that they could not see in the tool's UI. User Activity should respect tool
permissions and be aware of any special visibility restrictions in the tool. To that end:

* In cases where the tool's service layer does not include permission checks, additional permission checks are performed
by User Activity. User Activity errs on the side of caution here and requires maintainer-level permissions since it will
grant access to details for any event from the tool. This is not the ideal scenario but is required for some tools.
* User Activity is aware that several tools have a concept of anonymity, and tries to not reveal details that would
compromise anonymity. For example, the details for an assignment submission event will not identify the assignment if
it uses anonymous grading.
* User Activity is aware that some content can be hidden from the current user (for example, certain files in Resources)
and will not reveal information about it.
* User Activity will not show events that have been marked as "anonymous" in toolEventsDef.xml

### Precision

Sakai event references are ultimately just strings that can contain arbitrary data. If not properly validated, errors
can easily occur when trying to provide additional details about the event to the user. The User Activity implementation
strives to precisely model the details about each event, seeking to use appropriate data types whenever possible,
and validating early in the process. Once the final data about an event has been acquired, it is packaged into an
immutable object to further reduce the chance of error or misinterpretation. Error conditions are represented by
specific error objects rather than by incomplete or invalid instances of the intended object.

## Implementation Overview

At a high level

* New Sakai events are recorded automatically by the Collection layer
* Users invoke the Query layer to perform searches against these events by submitting the form on the User Activity page
* Users may click the "show more" button for an individual event in the query results, invoking the Resolver layer
which uses Sakai services to get additional details about that particular event
* The Presentation layer converts the specific details about the event into a form suitable for presentation
back to the user, which is then displayed inline in the query results table.

### Collection layer

As events are recorded by SiteStats, either in real time or in batches by the EventAggregator, they are now also stored
in the Detailed Events table, which includes the timestamp and the full event reference.

### Query layer

The User Activity page presents a form the user can fill out to search for events. A user and a date range are required,
and the query can optionally also be filtered by tool. Performing the search presents a table displaying the results
of the DetailedEventManager.getDetailedEvents(trackingParams, pagingParams, sortingParams) call.

### Resolver layer

Looking up details for a particular event in the query results is called "resolving" the event. Events must be marked
as "resolvable" in toolEventDef.xml in order to include a "show more" link in the results table. When this link is clicked,
the resolver layer is engaged to find more details about the event in question. DetailedEventsManager.isResolvable()
determines if the "show more" link appears, reading the resolvable flag via the EventRegistryService.

The resolver layer begins with the method DetailedEventManager.resolveEventReference(eventType, eventRef). This method
delegates resolution to the particular Resolver that handles the event type. Each tool has its own Resolver that
handles its events. Resolvers contain no internal state and are thus implemented as static methods.

Most event references end up referring to a particular Sakai entity (although some do provide additional data that might
only exist in the reference itself). In general, a Resolver will take the event reference and return the details about
the entity it refers to.

Resolvers always return an object that implements the ResolvedEventData interface. This interface is empty and exists only
to simulate a tagged union. The objects that implement ResolvedEventData are immutable lightweight representations of
just the relevant data about the entity, and thus are tailored to the entity they represent and don't necessarily share
anything in common. They attempt to be precise with respect to data types. For example, rather than Strings, these objects
will use, for example, Instants for timestamps and custom Enums for properites that can have only a particular set of
values.

Resolution is done in two stages:

#### Parsing

At this stage, the event reference string is validated and parsed out into an intermediate representation that will be used
by the resolution stage. Essentially the important details from the reference string are extracted and converted into a more
precise representation. For example, numeric ids are converted to numbers rather than left as Strings, and if they cannot be
converted as expected, the reference is considered invalid and not processed further.

Resolvers will delegate this stage to a Parser. Parsers typically use EventParserTips to aid in parsing the reference,
which are a flexible way to identify tokens in the reference by position and separator. If the format of an event reference
changes, it is easier to redefine the tips in the xml than it would be to refactor custom parsing code. EventParserTips
are defined in toolEventsDef.xml.

Most references can be broken down into a context, an optional subcontext, and an entity. For example, an announcement
reference contains the site id (context), the channel id (subcontext), and the message id (entity). There is a GenericRefParser
that is used to handle references that fit this pattern, but a few tools need custom parsing.

#### Resolution

The resolution stage takes the parsed reference data, and uses it to call Sakai services that return the entity.
Relevant data is read from the entity and stored in an immutable, lightweight object that implements ResolvedEventData.
If something goes wrong at the parsing stage or the service cannot return the desired results, one of several
ResolvedEventData objects representing error states can be returned instead of the expected object type.

### Presentation layer

When presenting the details of a particular event to the user, the ResolvedEventData must be first be transformed into
a presentable format.

Transformation, which includes localization, is done by a Transformer, of which there is one for each tool. For example,
AssignmentResolvedRefTransformer handles AssignmentEventData. Like Resolvers, Transformers contain no internal state and are
implemented as static methods. In the current implementation, all Transformers return a list of EventDetail objects.

EventDetail objects are simply localized key/value pairs. The value can be either plain text or a link. For example,
a submission to a normal assignment called "Assignment 1" would be transformed into a single key/value pair of
"Title"/"Assignment 1" (if user language was English). However, Transformers do more than simply localize each property
of the ResolvedEventData. They contain logic that determines how to present the data. To continue the "Assignment 1"
example, if the assignment has been deleted since the submission occurred, the Transformer would read both the "title" and
"deleted" properties of the AssignmentEventData and present the value as "Assignment 1 \[deleted\]" (if the user language
was English of course).

The current implementation uses a one-size-fits-all Wicket panel to present the list of EventDetail objects
in the same way no matter which event they are for.

#### Responsive Design

User Activity has a content-specific CSS breakpoint set at 640px that makes the following changes to the presentation:

- the search form switches to a column orientation
- the table switches to a "CardTable" layout that presents each table row as a separate "card", allowing the user
to see all the data in the row at once even on a narrow device


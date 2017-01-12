TAGS ADMINISTRATION 

1. SAKAI PROPERTIES

1.1 TAG SERVICE PROPERTIES

The tag service has a set of properties provided for configuration:

1.1.1 ENABLE THE SERVICE:
Enables the tag service. (default is true)
tagservice.enabled=true

(NOTE: At this time, disabling the tag service will only disable the admin tool.  Other features will still be available. In the future disabling the tag service will also disable the service.)

1.1.2 MAX NUMBER OF TAGS RETURNED BY THE REST SERVICES IN A PAGE
Sets the max size of the pages returned by the rest services in searches and lists to avoid large queries in big collections. (Default is 200)
tagservice.maxpagesize=200

1.1.3 CONFIGURATION OF THE IMPORT JOBS
The options will be defined later in this document.

1.1.3.1 UPDATE FULL COLLECTION
Defines the path inside the SAKAI folder for the full collection XML file. 
This process will delete all the tags not included in the collection and as a result, the update needs to contain a full set of tags.  The final step following the update and creation of the new tags will be to delete any tags of this collection in database not found in the update file. (By default tags/fullxmltags.xml)
tags.fullxmltagsfile=tags/fullxmltags.xml

1.1.3.2 UPDATE INDIVIDUAL TAGS
Defines the path inside the SAKAI folder for the "one by one" tag update option
Samples of these files can be found in the SAKAI source, at: 
/tags/tags-impl/impl/src/resources/xmlsamples (By default tags/tags.xml  and tags/tagcollections.xml)
tags.tagsfile=tags/tags.xml
tags.tagcollectionsfile=tags/tagcollections.xml

1.1.3.3 UPDATE MESH COLLECTION FILE
Defines the path inside the SAKAI folder for the MESH collection file:
The file can be downloaded from (The "Descriptors" file):
https://www.nlm.nih.gov/mesh/download_mesh.html
ftp://nlmpubs.nlm.nih.gov/online/mesh/MESH_FILES/xmlmesh/desc2017.xml
This process will delete all the tags not included in the collection and as a result, the update needs to contain a full set of tags.  The final step following the update and creation of the new tags will be to delete any tags of this collection in database not found in the update file.  For this process to work it needs to have previously created a collection with the external source defined as "MESH"  (By default tags/mesh.xml)
tags.meshcollectionfile=tags/mesh.xml

1.1.3.4 EMAIL CONFIRMATION
Defines the email address that will receive the confirmation emails from the import jobs (by default uses the defined email address in portal.error.email)
tags.import_job_email=yourmail@amailserver.net



TAGS ADMINISTRATION 

1. IMPORT JOBS

These jobs doesn't need to be scheduled for any periodic execution. They are just there to import and maintain tag collections, so they can be launched whenever they are needed.

The following three jobs have been developed for the tag import:

Generic Tags Update
This job allows updating of individual tags and collections. Functions include add, edit and delete.
Generic Tags Update job (Full Collection Import) 
This job allows editing for a complete collection. Functions include add, edit and delete of tags from the collections with an xml file containing the full collection.
Mesh Tags Update job
This job will import and update the MESH (Medical Subject Headings) collection.

All jobs described above require files to be placed in the sakai home folder (usually the sakai folder in tomcat) in the correct locations based on the properties explained below in the document.

1.1 Generic Tags Update job

This job allows add/edit of collections and add/edit/delete of individual tags. That means that we can modify only one tag from a 34000 tag collection.

Two files are required to perform these functions. Samples are below:

1.1.1 tagcollections.xml:
Here is a full sample in the source:
tags/tags-impl/impl/src/resources/xmlsamples/tagcollections.xml

Here is the structure of a tag collection:

  <TagCollection>
    <Name>Star Wars</Name>    
    <Description>This is the Starwars tag collection</Description>
    <ExternalSourceName>STARWARS</ExternalSourceName>
    <ExternalSourceDescription>Comes from a standard tags xml file</ExternalSourceDescription>
    <DateRevised>
	<Year>2016</Year>
	<Month>07</Month>
   	<Day>12</Day>
    </DateRevised>
  </TagCollection>


* Are mandatory fields

Name*: The name of the collection that displays to the users.
Description: A descriptive text explaining what this collection is about.
ExternalSourceName*: A unique ID that will be used to identify the collection when updating it in the future.
ExternalSourceDescription: Explains where this collection comes from.
DateRevised: The last revision date for this collection. That can be useful to know if it needs to be updated or not.
 

TagCollections will be updated with the new values while the externalSourceName remains the same

TagCollections can’t be deleted by a job. They can be deleted only through the UI when they are empty.


Another sample is:

<?xml version="1.0"?>
<TagCollections>
  <TagCollection>
    <Name>Countries</Name>    
    <Description>This is the list of countries in the world</Description>
    <ExternalSourceName>COUNTRIES</ExternalSourceName>
  </TagCollection>
</TagCollections>

Note this example has no ExternalSourceDescription or DateRevised. That situation is valid.



1.1.2 tags.xml
Here is a full sample in the source code:
tags/tags-impl/impl/src/resources/xmlsamples/tags.xml

This file provides the ability to add/edit/delete of tags one by one inside a collection(s).

The following example shows the structure of a tag.  Note a lot of fields are not mandatory but designed for future use based on what some of the most complex tag collections are using. 

<Tag Type = "DarkSide">
    <Action>delete</Action>
    <ExternalSourceName>STARWARS</ExternalSourceName>
    <ExternalId>SW00002</ExternalId>
    <TagLabel>Anakin Skywalker</TagLabel>
    <Description>Jedi Knight and Sith Lord</Description>
    <DateCreated>
	<Year>2012</Year>
	<Month>05</Month>
   	<Day>14</Day>
    </DateCreated>
    <DateRevised>
	<Year>2015</Year>
	<Month>06</Month>
   	<Day>30</Day>
    </DateRevised>
    <HierarchyCode></HierarchyCode>
    <AlternativeLabels>Darth Vader</AlternativeLabels>
    <Data>Anakin was a nice guy but went to the dark side</Data>
    <ParentId></ParentId>
  </Tag>

An analysis of each value is provided below:
Type: Provides the definition for the tag types. Type will be stored but it is not currently used.
Action: By default (if no value here defined) the tag will be created if it doesn’t exist or updated if it exists.  If the value is “delete”, the tag will be deleted.
ExternalSourceName*: Specifies the collection.
ExternalId*: Specifies an unique id in the collection. If tags are being imported from an external source, this will be the external ID. ExternalSourceName + ExternalId will be used to find the tag, so they are mandatory.
TagLabel*: Text value of the tag that will be displayed. 
Description: Provides additional information about the tag to display.
DateCreated: Date the tag was created IN THE EXTERNAL SOURCE. This can be used in the future to process differential updates.
DateRevised: Date the tag was updated IN THE EXTERNAL SOURCE. This is used to not update labels that are currently up-to-date.
HierarchyCode: Provides the location to store the EXTERNAL SOURCE hierarchy code.
AlternativeLabels: Some collections have alternative labels and they will be stored in this location. 
Data: Provides the ability to store any additional information that can’t be stored in any other field.
ParentId: Used to generate an internal hierarchy. Specify the ExternalId of the label that is the parent of this label.

As stated earlier, some of these fields are provided for future features and use, in case a tool needs them.

Simple tag examples are shown below:

<Tags>
    <Tag>
        <ExternalSourceName>COUNTRIES</ExternalSourceName>
        <ExternalId>AF</ExternalId>
        <TagLabel>Afghanistan</TagLabel>
    </Tag>
    <Tag>
        <ExternalSourceName>COUNTRIES</ExternalSourceName>
        <ExternalId>AL</ExternalId>
        <TagLabel>Albania</TagLabel>
    </Tag>
</Tags>


1.1.2 Generic Tags Update job (Full Collection Import)

Collections can be created using jobs and xml or collections can be created through the tag administration UI.  In either case, once collections have been created,  this job is useful to maintain the collections.  A full collection xml can be exported, a collection can be edited in an external editor and then imported again to update a collection or a process can be created to export from an external source in this format and update the full collection.

The main difference between this job and the Generic Tags Update job is that all tags that are not in this file will be deleted from the collection. In the Generic Tags Update job, we need to add the <Action>delete</Action> , but in this job (so be careful) all the tags not in the file will be erased. 

This provides an easy process to update a collection. Just load the full collection file and adds, edits and deletes will happen automatically.

1.1.2.1 Note: How to export an actual collection

The REST services are explained in the REST API document providing additional detail.


To export an actual collection you need to use the REST service with a call like the example shown below:

http://YOURSAKAI/direct/tagservice-admin/downloadCollection.xml?tagcollectionid=THETAGCOLLECTIONID&session=THETAGSSERVICESESSIONID

Where the tag collection Id is the Sakai internal id for that collection and the tag service SessionId is the sessionId created to use this service (like a security token). All details are provided in the REST API document

This file can be imported by the “Generic Tags Update (Full Collection Import)” job, and all the modifications, additions or deletions will be updated.

As stated previously, you can follow this format to do full updates from other external sources. If you want to create a collection in this way, remmeber that first, you will need to create the empty collection, so you can use the tagCollectionId needed in this file



1.1.3 Mesh Tags Update job

This job is one sample of a customized importer. This job imports tags from the MESH (Medical Subject Headings) collection https://www.nlm.nih.gov/mesh/download_mesh.html. If you want to add additional importers use this as a sample and update the code to import other collections import jobs. 

This one uses the mesh.xml file as it is, and maintains the mesh collection by adding, editing and deleting tags. 




TAGS ADMINISTRATION 

1. REST API

The tag service has a rest API that allows other tools to search it, and allows the admin user or any administrative tool to be integrated with the system to manage the existing tags and collections.

There are two different rest entry points:

tagservice
tagservice-admin

The first rest entry point is public.  The second rest entry point needs a specific token and only admin users are allowed to use it.


2. TAGSERVICE

This is the public tagservice that any tool can use for query. The service works with Spring injection, as an alternative way to work, but for javascript components to display tags, as the one used in Samigo, this provides a better way to query the tag service.

This service has the following four calls:

getTagsPaginatedByPrefixInLabel : list (GET) : [/tagservice/getTagsPaginatedByPrefixInLabel] (json)
getPaginatedTagCollections : list (GET) : [/tagservice/getPaginatedTagCollections] (json)
getTagsPaginatedInCollection : list (GET) : [/tagservice/getTagsPaginatedInCollection] (json)
getTag : list (GET) : [/tagservice/getTag] (json)

Every call that has the “paginated” word on it accepts these 2 parameters:

page: The page number used for pagination
pagelimit : The number of results per page (it will be limited by the tagservice.maxpagesize property in the sakai.properties file. If this is not set, the default max value is 200)

This is a sample call using these values:

http://localhost:8080/direct/tagservice/getTagsPaginatedInCollection.json?tagcollectionid=ed2d938a-1de4-4b16-ad27-a3c2ba334f8a&page=2&pagelimit=5 

If “page” or “pagelimit” is not specified, the first page with the default pagelimit value is returned.

2.1 getTagsPaginatedByPrefixInLabel

The following example returns the results when searching for tags that start with the prefix parameter.  

direct/tagservice/getTagsPaginatedByPrefixInLabel.json?prefix=Lo

Will search for any tag with a label starting with “Lo” (it is not case sensitive)

Parameters:
prefix : The string to search by.


2.2 getPaginatedTagCollections

This example returns the available tag collections in the system. 

direct/tagservice/getPaginatedTagCollections.json

Parameters:
No parameters



2.3 getTagsPaginatedInCollection

The following example shows how to return the tags from a collection.  

direct/tagservice/getTagsPaginatedInCollection.json?tagcollectionid=a267923b  -9eed-417f-9656-5f801f1164b6

Parameters:
tagcollectionid: To know the id, It can be retrieved from the getPaginatedTagCollections 


2.4 getTag

This example returns all the information from a single tag

tagservice/getTag.json?tagid=c5bc6575-ec2e-4278-aa2a-0dc899bd2512 

Parameters:
tagId: To know the id, It can be retrieved from the getTagsPaginatedInCollection or the getTagsPaginatedByPrefixInLabel

3. TAGSERVICE-ADMIN

The tagservice-admin endpoint will be used only by admin users to manage the tag collections.

To access this endpoint a sakai session needs to be opened and the correct token needs to be provided.

To create this token, you can use the startSession call for a user with this permission: tagservice.manage in the /site/!admin

The token created can be used later while the user session is active. The next part of this document provides an example for using the endpoint.

This API contains the following calls for the endpoint:

startSession : new (POST) : [/tagservice-admin/startSession] 
updateTag : edit (PUT) : [/tagservice-admin/:ID:/updateTag] 
downloadCollection : list (GET) : [/tagservice-admin/downloadCollection] (json) (xml)
deleteTag : new (POST) : [/tagservice-admin/deleteTag] 
createTag : new (POST) : [/tagservice-admin/createTag] 
deleteTagCollection : new (POST) : [/tagservice-admin/deleteTagCollection] 
createTagCollection : new (POST) : [/tagservice-admin/createTagCollection] 
updateTagCollection : edit (PUT) : [/tagservice-admin/:ID:/updateTagCollection] 

As stated earlier, except for startSession, every call will need to have:

sessionid : the session Id returned by the startSession call.


3.1 startSession (POST)

To get the token, first open a normal rest session. It is not enough to open it with a browser, a call needs to be made to this service:

https://YOURSERVERURL/direct/session?_username=XXXX&_password=XXXX

Provide the user information to access later.

Once completed (a sessionId will be returned but it can be ignored because the cookie that has been generated will be used) call the startSession with the POST:

direct/tagservice-admin/startSession/

a response will be received as shown below:

{"session":"53bc9e0a8da58d4bf6cfc096a5c92b874ef0448c1f979717ec145d782ff39bd5"}

This id will be used in the following calls as session

3.2 createTagCollection (POST)

Creates a new tag collection using:

direct/tagservice-admin/createTagCollection.json

Parameters:

name: The collection name to display to the users. Mandatory
description : The description for the tag collection.
externalsourcename : The external source name for the collection.  This is not a mandatory parameter but it is required to be used in the synchronization jobs. 
externalsourcedescription: A description about where this collection comes from.
externalcreation : 1 or 0, Indicates if this is supposed to be created by an external system or not. This will be used to allow the users in the UI to modify or not modify the collection.
externalupdate: 1 or 0, Indicates if this is supposed to be updated by an external system or not. This will be used to allow to the users in the UI to modify or not modify the collection.
lastsynchronizationdate: The last date (milliseconds format)  this collection was updated from the external source. (this can be useful in the synchronization jobs).
lastupdatedateinexternalsystem: The last date this collection was updated in the external system (this can be useful in the synchronization jobs).
session: The session identifier (sessionId).


3.3 createTag (POST)

Creates a new tag:

direct/tagservice-admin/createTag.json

Parameters:

taglabel:  The tag label to display to the users. Mandatory
tagcollectionid:  The collection id containing the tag. Mandatory
description : The tag description.
externalid : The identifier the external source uses to identify the tag. This is not a mandatory parameter but it is required to be used in the synchronization jobs.
alternativelabels: Other labels for the same tag. Some collections use this.
externalcreation : 1 or 0, Indicates if this is supposed to be created by an external system or not. This will be used to allow to the users in the UI to modify or not modify the collection
externalupdate: 1 or 0, Indicates if this is supposed to be updated by an external system or not. This will be used to allow to the users in the UI to modify or not modify the collection
lastupdatedateinexternalsystem: The last date where this collection was updated in the external system (this can be useful in the synchronization jobs)
parentid : The ID of the tag that is the parent of this tag, in case a hierarchy is to be created
externalhierarchycode: The external hierarchy information. Some collections have specific hierarchy codes. It is only informative.
externaltype: Some collections have different types. It is only informative.
data: Extra information that can be stored for a tag. It is only informative.
session: The session identifier (sessionId).


3.4. deleteTag (POST)

Deletes a tag:

/tagservice-admin/deleteTag

Parameters:
id: The tag id to delete.
session: The session identifier (sessionId).


3.5. updateTag (POST)

Updates the values in an existing tag:

/tagservice-admin/updateTag

Parameters:
id: The tag id to update.
Additional parameters are the same as createTag.


3.6. updateTagCollection (POST)

Updates the values in an existing tag collection:

/tagservice-admin/updateTagCollection

Parameters:
tagcollectionid: The tag collection id to update.
Additional parameters are the same as createTagCollection.


3.7. deleteTagCollection (POST)

Deletes a tag collection:

/tagservice-admin/deleteTagCollection

Parameters:
id: The tag collection id to delete.
session: The session identifier (sessionId).


3.8 downloadCollection (GET)

Downloads a full collection in an xml format. This file can be used later to update the collection (edit the file and use the sakai quartz job to read it).

/direct/tagservice-admin/downloadCollection.xml

tagcollectionid: The tag collection id to update.
session: The session identifier (sessionId).





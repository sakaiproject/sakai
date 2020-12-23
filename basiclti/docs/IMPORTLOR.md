
Using ContentItem / Deep Linking for a Learning Object Repository
=================================================================

While most LMS's (including Sakai) allow a user to upload a Common Cartridge
into a course shell and import it into the Course, Sakai supports the ability
to import a cartridge or structured content directly into Lessons from a
Learning Object Repository.

This is similar to the non-standard `content_migration` placement in Canvas
but is based 100% in standard protocols.  Currently Tsugi supports this
protocol and it leads to very convienent ability to import pieces of content
from a Tsugi LOR into Sakai.

Setting up the Import Placement in Sakai
----------------------------------------

Sakai contacts the Learning Object repository using the Content Item or
Deep Linking protocol with some special indications to communicate what
it is expecting.

If you have a URL that is ready to accept these "import requests", you
put it into Sakai as follows:

https://www.py4e.com/tsugi/lti/store/
12345
secret

Then tick the checkboxes that say 'The tool can receive a
Content-Item or Deep-Link launch" and 'Allow the tool to provide
a common cartridge'.

This will make a link appear under 'Lessons -> More Tools -> External
Tool Import'.  When you select the tool, it runs the import process
in a modal dialog and when the LOR sends the proper data back to Sakai
it is automatically imported.   

It is not all that amazing - all that is happening is that the LOR sends a
URL back to Sakai and Sakai downlaods that URL, expecting it to be an
IMS common cartridge.l

How it Works With Content Item
------------------------------

Content Item is part of the LTI 1.1 series of specifications:

https://www.imsglobal.org/specs/lticiv1p0/specification

When Sakai sends a Content Item link, it tells the tool that it is expecting 
a common catrridge by sending these values:

    accept_media_types=application/vnd.ims.imsccv1p2,application/vnd.ims.imsccv1p3
    accept_multiple=false

When a site receives this link, it can present the user a UI, perhaps to pick
which content the user wants for this particular import and when the user
is satisfied, the tool sends back a ContentItemResponse with a `FileItem`
entry in its `@graph` section:

    {
      "@context": "http://purl.imsglobal.org/ctx/lti/v1/ContentItem",
      "@graph": [
        {
          "@type": "FileItem",
          "url": "http://localhost:8888/py4e/tsugi/cc/export?tsugi_lms=sakai&anchors=install",
          "copyAdvice": "true",
          "expiresAt": "2020-12-18T14:29:19Z",
          "mediaType": "application/vnd.ims.imsccv1p3",
          "title": "Imported+from+Tsugi",
          "@id": ":item1"
        }
      ]
    }

The url provided must produce a valid Common Cartridge when Sakai does a GET to the URL.  Sakai
will not do any other authorization beyond a GET request.  The URL can contain a token that is
single use and/or expires pretty quickly.   The "copyAdvice" and "expiresAt" says "this url
won't last forever".

Sakai retrieves the URL within a few seconds of receiving the response and imports the cartridge.
Sakai does not hold on to the URL for later use.  If the user wants to do another import 
they wll start the process again and send a new ContentItem request will be sent.

How this works with Deep Linking
--------------------------------

The IMS LTI Advantage (LTI 1.3) variation of this approach uses Deep Linking:

https://www.imsglobal.org/spec/lti-dl/v2p0

Everything is quite similar except for the message formats.  Upon launch, the JSON claim is:

    "https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings": {
        "accept_types": ["file"],
        "accept_media_types": "application/vnd.ims.imsccv1p2,application/vnd.ims.imsccv1p3",
        "accept_multiple": false,
        ...
    }

The response is a `file` type in the JSON claim:

https://www.imsglobal.org/spec/lti-dl/v2p0#file


    "https://purl.imsglobal.org/spec/lti-dl/claim/content_items": [
        {
        "type": "file",
        "title": "Imported+from+Tsugi",
        "url": "http://localhost:8888/py4e/tsugi/cc/export?tsugi_lms=sakai&anchors=install",
        "mediaType": "application/pdf",
        "mediaType": "application/vnd.ims.imsccv1p3",
        "expiresAt": "2020-12-18T14:29:19Z"
        },
        ...
    ]

As you can see the protocols are quite similar in terms of how Sakai supports the import / migration
use case.



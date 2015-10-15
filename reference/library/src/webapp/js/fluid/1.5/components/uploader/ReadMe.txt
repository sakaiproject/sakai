Infusion Uploader Read Me

1) Progressive Enhancement
2) Upgrading
3) Known Issues

--------------------------------------

PROGRESSIVE ENHANCEMENT:

As of Infusion 1.3, the Uploader automatically delivers the best version of the component
possible, based on the features supported by a user's browser. There are two flavours of the
Uploader:

    1. Single file: delivered to browsers that don't support JavaScript or HTML 5
    2. HTML 5: the best and most widely-supported version of Uploader, suitable for modern browsers

If you don't want to offer the HTML5 version of the uploader to your users, you can simply omit the HTML5UploaderSupport.js file.

To do this, you will have to include all the required files individually, instead of using a single
concatenated file.

--------------------------------------

UPGRADING from previous versions:

As of Infusion 1.5, the Flash version of the Uploader has been removed due to a cross-site scripting vulnerability. 

https://nealpoole.com/blog/2012/05/xss-and-csrf-via-swf-applets-swfupload-plupload/

The following browsers will support the HTML 5 version of the Uploader:
  - Firefox 3.6 or higher
  - Safari 4 or higher
  - Google Chrome
  - Internet Explorer 10 or higher

All others will automatically receive the single file versions of the Uploader depending on browser capabilities.

--------------------------------------

KNOWN ISSUES:

Uploader and HTML 5:

* Uploading more than one file at a time without Flash requires a reasonably up-to-date browser with
  support for the following open web technologies, referred to under the umbrella of "HTML 5":
    - Multiple file form elements
    - XmlHTTPRequest Level 2
    - FormData
    - File API

* Firefox 3.6 has only partial support for HTML 5 uploads, and will load the entire file into browser
  memory. This causes a risk of crashing with very large files on computers with less memory. As a
  result, we've included an additional option called "legacyBrowserFileLimit," allowing file sizes to
  be specially capped in Firefox 3.6. The default value for this option is 100 MB.

* The Uploader's HTML 5 implementation doesn't currently support filtering based on file types.

WHAT IT IS

TwinPeaks is a capability to search external repositories while using the WSYWIG editor. TwinPeaks was originally developed by the Indiana University Library and was extended and merged into the Sakai 2.1 release by the MIT OKI project.

To add a repository to TwinPeaks, one must write or obtain an OKI DR OSID implementation for that particular repository and then install the OSID implementation in their Sakai instance. Documentation for installing and registering a new OSID implementation within Sakai is available in the Sakai Development Resources area on collab.sakiaproject.org.


HOW TO ENABLE IT

TwinPeaks can be enabled by changing the following sakai.properties setting to true.

# enable the twinpeaks feature in the WYSIWYG editor in legacy tools: true or false
wysiwyg.twinpeaks=false

The TwinPeaks implementation in the Sakai 2.1 release should not be turned on in production systems without additional work. The Sakai 2.1 out-of-the-box only has a very simple repository configured and TwinPeaks only works with some of the tools (Announcements and the other legacy tools).

TwinPeaks is suitable for use on development servers and can be used to enable sites to begin the development of their DR OSID implementations. As we move towards 2.2, the integration of TwinPeaks into the resource tool and WSYWIG editor will be improved.


WHO TO CONTACT

If you have any questions regarding TwinPeaks, please contact Jeff Kahn (jeffkahn@MIT.EDU).
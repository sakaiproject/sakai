
Importing Common Cartridges with LTI Links into Sakai
=====================================================

Sakai has been able to import Common Cartridges into Lessons for a long time.  Recently,
(hopefully Sakai 20.3 and Sakai 21.0) there have been some improvements that make things
more convienent.  We will document the "old way" and then document the latest improvements.
Please upgrade to the latest Sakai to get the best stuff (as usual).

Cartridges Structure and Lessons
--------------------------------

Cartridges are a hierarchical "table of contents".  Often they are something like:

    Basic Materials
        Slides
        Videos
        LTI Links
    Variables and Constants
        Slides
        Videos
        LTI Links
    Conditional Execution
        Slides
        Videos
        LTI Links
    ...

When Lessons sees these top level "folders" it imports them as Sub-Pages and promotes them 
to the left navigation.   If you want the major chunks of content rendered on the 
left navigation - this is great.

When Tsugi knows it is exporting to Sakai, it actually adds one extra folder level:

    Imported Content
        Basic Materials
            Slides
            Videos
            LTI Links
        Variables and Constants
            Slides
            Videos
            LTI Links
        Conditional Execution
            Slides
            Videos
            LTI Links
        ...

So you only get one left nav link which you can easily hide.  All the secondary folders
are still subpages so you can put them anywhere you like (i.e Lessons pages or left nav).

LTI Links While Importing a Cartridge (Old School)
--------------------------------------------------

When you have links in the carridge, they have launch urls but *not* keys and secrets.
So a big part of importing LTI links is associating them with an existing LTI Tool that either
has a key and secret *or* inserting a new tool in the site without a key and secret so that
you can edit the tool and add the key and secret later.

The link matching in the (pre 20.3) versions of Sakai is not as sophisticated as it should be.
It looks for a tool that matches the launch URL exactly with or without the query string
and if it finds a matching tool (globally or within the site) it connects to that existing tool.

This was a sufficient feature before the wide usage of Content Item and Deep Linking where the 
URLs all looked the same:

    https://mymathlab.co/calculus/lti/?section=1.0
    https://mymathlab.co/calculus/lti/?assignment=1.1
    https://mymathlab.co/calculus/lti/?section=2.0

All these would map to the same tool.  But when you got a cartridge from a site like
www.py4e.com - it had a lot of links that were quite different.

    https://www.py4e.com/tools/pythonauto/?assn=1.1
    https://www.py4e.com/mod/quiz/?quiz=1.1
    https://www.py4e.com/mod/peer-grade/?assn=welcome
    https://www.py4e.com/tools/pythonauto/?assn=2.1
    https://www.py4e.com/mod/quiz/?quiz=2.0
    https://www.py4e.com/mod/peer-grade/?assn=loops

This would be looking for three registered tools of the form:

    https://www.py4e.com/tools/pythonauto/
    https://www.py4e.com/mod/quiz/
    https://www.py4e.com/mod/peer-grade/

At least it ignores the query string and coalesces these into three tools instead of six
or 120 tools that each have to be configured with a key and secret.

If Lessons did not find a tool - it created a tool with an empty key and secret for each of the
query-less URL paths in the cartridge.  Lessons even had a nice feature that noticed when there 
was no secret and on the first launch it let you set the key and secret for one of the URLs
and all the rest of the links using that URL would work fine.  So in this example, you needed
to put the key and secret in three places and any number of LTI links would start working.

If you knew what you were doing in advance and installed those three tools with a key
and secret *before* the impoprt - the links would be properly associated and work instantly
after they were imported.  But sadly this did not happen often because it was not altogether
clear which URL should be registered and the fact that more than one registration might be required.

But it worked well enough to get by.

Importing LTI Links on the latest Sakai (> 20.3)
------------------------------------------------

The latest Sakai improves the user experience by addressing two use cases:

* Being able to register one global tool before import and have all links find that tool - 
"Improve LTI Tool Matching During Common Cartridge Import" - https://jira.sakaiproject.org/browse/SAK-44763

* Being able to register a tool after an import and transfer the imported links from the "stub tools"
created in the site to a tool that was added after the import and then deleting the "stub tools" and
having everything work (and no icky stub tools).  "Add ability to  "patch" LTI tool placements after a CC
import with broken links" - https://jira.sakaiproject.org/browse/SAK-44772

You can check to see if these changes are in your version of Sakai.   At worst you can play with them
on our nightly servers until you upgrade to the latest verison.

Improving Tool Matching (SAK-44763)
-----------------------------------

As Lessons looks at each LTI link being imported, it does the following matching:

1. Look for a locally registered tool with an exact match
2. Next snip off the query string and check for a local tool
3. Next look for a local tool that has a prefix match that at least includes protocol and host

Then we go through the globally registered tools following the same priority:

4. Look for a global tool with an exact match
5. Next snip off the query string and check for a global tool
6. Next look for a global tool that has a prefix match that at least includes protocol and host

After all that if we can't find a matching tool, we punt and make a new tool without a secret (old behavior).

It checks local before global to an override - but in general - the most common case is that there will be
no per-site (i.e. local) tools installed in the site so they will just find globally installed tools.
And if you disallow per-site tool creation by the instructor, then the fact that per-site tools are checked
first does not matter.

This means that if we were importing a cartridge with the following links:

    https://www.py4e.com/tools/pythonauto/?assn=1.1
    https://www.py4e.com/mod/quiz/?quiz=1.1
    https://www.py4e.com/mod/peer-grade/?assn=welcome
    https://www.py4e.com/tools/pythonauto/?assn=2.1
    https://www.py4e.com/mod/quiz/?quiz=2.0
    https://www.py4e.com/mod/peer-grade/?assn=loops

And we had a single globally registered tool

    https://www.py4e.com/tsugi/lti/store/

All of the links would automatically associate with with the registered tool (i.e. no stub tools would
be created) and all the cartridge links would instantly work.

The matching rule (above) in this case would be the last matching rule - "prefix match that at least
includes protocol and host".  But it would match and connect up.

Patching Tool Links Post-Import (SAK-44772)
-------------------------------------------

But if you imported a cartridge with the six links *before* you added the global tool Lessons would not
find any registered tool and so you would end up with three "stub tools" created locally in the site:

    https://www.py4e.com/tools/pythonauto/
    https://www.py4e.com/mod/quiz/
    https://www.py4e.com/mod/peer-grade/

You coud give these three tool key / secret values - and off you go like in the old days.  But you
could also install a new global tool at:

    https://www.py4e.com/tsugi/lti/store/

And then find the "stub tools" and transfer all the links from the stub tools to the global tool
and then delete the stub tools.  And all the links will work.

And since there is now a global tool that has a nice matchable url - future imports from www.py4e.com will
simply auto-associate and work as soon as they are imported.



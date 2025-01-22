
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

LTI Tool Matching
-----------------

As Lessons looks at each LTI link being imported, it does the following matching:

1. Look for a registered tool in the site with an exact match
2. Next snip off the query string and check for a tool in the site
3. Next look for a local tool that has a prefix match that at least includes protocol and host

Then we go through the globally registered tools following the same priority:

4. Look for a global tool with an exact match
5. Next snip off the query string and check for a global tool
6. Next look for a global tool that has a prefix match that at least includes protocol and host

After all that if we can't find a matching tool, we punt and make a new tool without a secret.

It checks "in site" before global to an override - but in general - the most common case is
that there will be no per-site (i.e. local) tools installed in the site so they will
just find globally installed tools.  And if you disallow per-site tool creation by
the instructor, then the fact that per-site tools are checked
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

Patching Tool Links Post-Import
-------------------------------

But if you imported a cartridge with the six links *before* you added the global tool Lessons would not
find any registered tool and so you would end up with three "stub tools" created locally in the site:

    https://www.py4e.com/tools/pythonauto/
    https://www.py4e.com/mod/quiz/
    https://www.py4e.com/mod/peer-grade/

You could give these three tool key / secret values or complete their LTI 1.3 configuration and
the tools would just start working. But you could also install a new global tool at:

    https://www.py4e.com/tsugi/lti/store/

And then find the "stub tools" (perhaps in the site that did the import) and transfer all the
links from the stub tools to the global tool and then delete the stub tools.  The transfer
feature is available to Sakai administrators in the `External Tools` application in the
`Admininstration Workspace`.

And since there is now a global tool that has a nice matchable url - future imports
from www.py4e.com will simply auto-associate and work as soon as they are imported.

Importing a CC+Sakai Cartridge
------------------------------

For some versions of Sakai there is an option to do a CC Export and include a Sakai Archive
in the cartridge in the Cartridge.  These proprietary Sakai files are ignored by other
LMSs doing cartridge imports because they are marked as "ignore this file".

However, if Sakai is importing this augmented cartridge, it notices these files and switches
to an un-archive instead of a CC import.  This leads to a much higher fidelity import.

When LTI links are exported into a CC+Sakai cartridge, there is more information included.
For example, if there is an Assignment with an LTI tool, the following is added to the
XML data:

            <sakai-lti-content>
                <id>83</id>
                <title>Breakout 1.1 window 3</title>
                <description>&lt;p&gt;ew&lt;/p&gt;</description>
                <newpage>1</newpage>
                <custom>submissionend=$ResourceLink.submission.endDateTime
                        availablestart=$ResourceLink.available.startDateTime
                        canvas_caliper_url=$Caliper.url
                        availableend=$ResourceLink.available.endDateTime
                        submissionstart=$ResourceLink.submission.startDateTime
                        resourcelink_id_history=$ResourceLink.id.history
                        context_id_history=$Context.id.history
                        coursegroup_id=$CourseGroup.id
                </custom>
                <launch>http://localhost:8888/py4e/mod/breakout/</launch>
                <sakai-lti-tool>
                    <id>1</id>
                    <title>Py4E 1.1 Store</title>
                    <launch>http://localhost:8888/py4e/tsugi/lti/store/</launch>
                    <newpage>2</newpage>
                    <pl_linkselection>1</pl_linkselection>
                    <pl_lessonsselection>1</pl_lessonsselection>
                    <pl_contenteditor>1</pl_contenteditor>
                    <pl_assessmentselection>1</pl_assessmentselection>
                    <sendname>1</sendname>
                    <sendemailaddr>1</sendemailaddr>
                    <allowoutcomes>1</allowoutcomes>
                    <allowlineitems>1</allowlineitems>
                    <lti13>0</lti13>
                    <sakai_tool_checksum>Q3bPh/gLibW0GYXxSoj8Lub351q4XNfLN6BZVQXFkn4=</sakai_tool_checksum>
                </sakai-lti-tool>
            </sakai-lti-content>

The export adds a `sakai_tool_checksum` which marks a particular tool installed in a particular
instance of Sakai.  If the CC+Sakai is imported into the *same* LMS and the tool is still in
the LMS and available to the site where the import is happening, and *exact* match looks up the
correct tool using this checksum.

If there is not tool that matches the checksum associated with the site, the CC+Sakai import
process falls back to the laucn url matching approach described above.

When a content item is being created the same kind of matching based on launch url is described above.


Importing a Large Number of Cartridges Between Systems
------------------------------------------------------

If you are moving a lot of courses between systems whether you are using CC or CC+Sakai, it is a
good idea to install as many LTI tools globally that were used on the source system.  This way
as Content Items are imported they will be successfully matched and will just work right after
import.

Once you have pre-installed as many LTI tools as you can, start by importing a single cartridge
and check which LTI links work and which are broken.  For the broken links, go into `Administration
Workspace` and fix those tools with a key and secret, auto provisioning, or
manual LTI 1.3 provisioning.  The stub tools are generally installed in a site, so promote them
to global whenever possible.

Then continue to import another course, fix its LTI tools, and so forth.  The thing to avoid is
importing 100 sites, and ending up with 300 broken stub tools that need to be fixed by hand or
transferred to another tool instance.





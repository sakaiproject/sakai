Full documentation of RWiki is available at: http://saffron.caret.cam.ac.uk/projects/sakai-rwiki/


ENABLING RWIKI

By default, RWiki is included in the release but hidden from the list of available tools in the Worksite Setup. RWiki will be available to administrators using the "Sites" administration interface, where it will appear under the label Wiki (or the  tool id sakai.rwiki). You have one of two choices in terms of enabling RWiki.

If you want to let a few selected users use RWiki to test it out, you can have the administrator selectively add it by hand to the sites for those users who you want to use RWiki.

If you want to make it so that any user can add Rwiki to their site using WorkSite Setup, edit webapps/sakai-rwiki/tools/sakai.rwiki.xml in a deployed instance, or rwiki/rwiki/src/webapp/tools/sakai.rwiki.xml in a source tree. Uncomment the <category ...> entries in the <tool> section.


KNOWN ISSUES

1. To install math support you should set the init-params headerScriptSrc and footerScript appropriately. We recommend that you keep these as the default and install an appropriate library in /library/jsMath. We suggest that you use jsMath (http://www.math.union.edu/~dpvc/jsMath/welcome.html) but cannot distribute it as it is GPL.

2. Searching is restricted to the default permission realm, and doesn't check that the pages returned can actually be viewed by the searcher.

3. We are currently missing functionality to provide for permission realm changes to pages, and renaming of pages.

4. There are very few unit-tests for this code, some more should be written. A similar statement applies for javadoc comments. There are about 40-50 unit tests for the core wiki engine, we dont normally turn them on since it slows down the maven build significantly.
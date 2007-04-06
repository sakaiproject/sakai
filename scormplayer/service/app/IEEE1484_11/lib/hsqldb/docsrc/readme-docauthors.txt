HOW TO CREATE AND WORK WITH DOCBOOK DOCUMENTS FOR HSQLDB

At some point, this document itself should be converted to DocBook format.


Our DocBook strategy is still subject to change.  For now...


TO CREATE A NEW DOCBOOK BOOK

Think up a base filename for your document.
It should be nice and short, without funny characters (but hyphens, other
than leading hypends, are ok).  Example:  sqltool.
Hereafter, I'll refer to this as your "book name".

Create a subdirectory of "docsrc" with name of your book name.
I'll refer to this directory (docsrc + book name) as "your book directory".

Inside your book directory, create your main DocBook source file with name
of your book name + ".xml", e.g. "sqltool.xml".

Your DocBook document may reference or include shared files in the
main 'docsrc' directory as well as any files which you put into your
book directory.
You may want to include sample .java files, screen shots, or component
DocBook source files.
Usually you will just copy these files right into your book directory.

For examples of just about everything, see .../docsrc/sqltool/sqltool.xml.
Notice that sqltool.xml pulls in a document section from the main docsrc
directory.

Add your book name to the .cvsignore file in the doc directory.

Until all of the CVS artifacts are moved out of the "doc" directory,
you will need to edit the clean-doc target in the build.xml file in
the build directory so that it will remove the derived files for
your book from under the doc directory.


HOW TO REFERENCE OR INCLUDE OTHER FILES IN YOUR DOCBOOK SOURCE FILE(s).

To link to outside documents (which you supply or not), you'll usually
use the DocBook <ulink> element.

To "import" other documents, just use the general external parsed entity 
mechanism.
This is a basic DTD-style XML feature where you use macros like
&entityname;.  Either find an XML reference or look around our existing
DocBook source files for an example to follow.

One tricky point is how to include external files verbatim.
If you just read in external files as parsed entities, they will be parsed
as DocBook source (and therefore they must consist of, at least, legal
XML)*.
But often you will want to import a real, working file (like a 
configuration file, sql file, Java source file), and you won't want to
hack it up just so you can import it.
(For one thing, you shouldn't have to; for another, you may want to 
provide a link to the file for download, so you wouldn't want people
to download a hacked-up version).
It would be nice if you could CDATA, then include the entity, but that
won't work since the &...; inclusion directive would thereby be escaped.
If you don't know what the hell CDATA is, just follow the instructions
in the next paragraph.

To import a document verbatim, define an external parsed entity for 
the file ../../docwork/BOOKNAME/cdata/file.name, where BOOKNAME is 
your book name and file.name is the name of the file to be imported
(which resides in the current directory).
If you want to know, what will happen is, the Ant build will copy the 
file-to-be-imported to the directory .../docwork/BOOKNAME/cdata and will 
sandwich it in a CDATA directive.
If you want to provide a link to the document, you just ulink to 
the document in the current directory, not to the one in the cdata

POSTNOTE:  The long-term way to do this is with XInclude, or some
other generic XML inclusion mechanism.  Unfortunately, none of the
good methods work with the Java ports of DocBook!


=======================================================================

CONVENTIONS

Please use <remark> elements to mark up notes for yourself or for
other developers.
All <remark>s should be removed before the doc goes public!

Please capitalize HSQLDB like "Hsqldb" in titles, and capitalize like
"HSQLDB" elsewhere.  (In filepaths and package names you code as
required for the filepath or package name, of course).

=======================================================================

TIPS

When closing DocBook <screen> and <programlisting> elements, make
sure that there is no line break after the text to display and before
the closing </screen> or </programlisting> tag.
Otherwise the resultant display will not look right.

Don't capitalize words or phrases to emphasize them (including in
section titles or headings).
If you want to emphasize something a certain way, then use a DocBook
emphasis role, and leave the presentation decisions to the style sheets.
It is very easy to set a CSS style to capitalize headings if you want
them to appear that way.



* Theoretically it would be better and SHOULD be simpler to use
unparsed entities for this purpose, but unparsed entities are a 
messy legacy feature of DTD which is more convoluted than the 
strategy described here.

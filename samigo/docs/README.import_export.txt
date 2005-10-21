Notes on QTI Import and Export for version 2.1
===============================================

Usage notes and Known issues:
1. QTI version 1.2 is the only version supported at this time.  Exports may be
made in questtestinterop format for either assessment/section/item or item.
Only assessment/section/item format may be imported.
Object bank format is supported for web service inquiries via QTIService.

2. Export Assessment XML Format.
   a. In earlier versions of Samigo, JSF prepended extra lines,and you may have
seen "XML Parsing Error: xml processing instruction is not at
start of external entity." in Firefox. For backward compatibility, these
prepended lines are ignored.

   b.  Exported Assessment files now default to the name "exportAssessment.xml".
Firefox users should accept the default "Save as Type:" "Web Page, HTML only".
DO NOT save as "Text File", this will save only the text nodes of the XML document,
and will cause errors on re-import.

   c.  Tests and Quizzes will accept files with any extension as long as they
are in correct format for importation.  (SAK-1203).


3. Question format issues on export.
  a. Blank space lost in the question text for the file upload question.
  b. A non-breaking space will be used to supply absent text between blanks in
fill-in-the-blank questions.  (SAK-1872).
  c. Selection-level feedback is not retained for matching questions. (SAM-520)

4. Sakai specific metadata.  Sakai specific metadata is supported by embedding
assessment information in QTI format, except for High Security IP address
settings. (SAK-1873)

5. Import and Export of Character Entities and Non-XHTML Data.
As of 2.01 is should be possible to include characters such as '&' in titles and
correctly export and import them.  Previously only rich text fields escaped them
out properly.  The the well-known symbols, "&", "<" and ">" are always supported.

Through cut-and-paste operations into rich text fields using unusual characters
such as mathematical symbols it is possible to create an incorrectly formatted
QTI document that is not importable into Tests and Quizzes.

There are two workarounds for this, using a text editor:

    a. For  well-known unicode entities you can replace them with their numbered
XML entity equivalents (see http://www.w3.org/TR/REC-html40/sgml/entities.html).

  For example, (FORALL) x (SUCH THAT) x (IS GREATER THAN) (SQUAREROOT OF) x

  could be rendered as

  <mattext charset="ascii-us" texttype="text/plain" xml:space="default">
    &#8704; x : x &gt; &#8730; x
  </mattext>

  Note that legibility for some entities may be dependent on the browser used.

    b. A more general solution is to enclose the non-XHTML text in CDATA.
For example:
  <mattext texttype="text/html">
    <![CDATA[True-False: Ist diese Frage richtig oder falsch? (Richtig) ]]>
  </mattext>

  Note that the XML parser will make no attempt to validate CDATA enclosed
character data, and will then accept non-XHTML and even bad HTML.

6. Respondus.
Fixed in 2.01.  Respondus questions with missing score can be edited.
Note: there are some non-Sakai QTI formatting issues in Respondus. We will be
building alternate processing to handle these, realistically, to be taken care
of in the next release. (SAM-500)



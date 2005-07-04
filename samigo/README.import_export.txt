Notes on QTI Import and Export for version 2.01
===============================================

Usage notes and Known issues:
1. QTI version 1.2 is the only version supported at this time.  Exports may be made in questtestinterop format
for either assessment/section/item or item.  Only assessment/section/item format may be imported.
Object bank format is supported for web service inquiries via QTIService.

2. Export in Firefox.
Users requiring an XML (.xml) extension in Firefox browsers should "Save as Type:" "Web Page, HTML only"
and specify the ".xml" extension, explicitly typing the desired file name and extension.  DO NOT save as
"Text File", this will save only the text nodes of the XML document, and will cause errors on re-import.

Note: Tests and Quizzes will accept files with any extension as long as they
are in correct format for importation.  (SAK-1203).

3. Question format issues on export.
  a. In some cases, blank space was lost in the question text for the file upload question.
  b. In some cases, one choice lost from Matching question (but not its pair).
  c. Fill In the Blank. In special case where the first option appears before any question text,
     question is exported from Sakai, and then reimported, format errors reported. Workaround (7 below).
  d. Issue reported with non-scored (survey) questions exported in incorrect format. (SAM-271)

4. Exporting model answers.
  Model Short Answer is not supported for exporting Short Answer/Essay question. (SAM-271).

5. Sakai specific metadata.  Sakai specific metadata is supported by embedding assessment information
in QTI format.  The following metadata elements are not yet supported:
    a. High Security IP address settings,
    b. Rich text in submission message. (SAM-271)

6. Import and Export of Character Entities and Non-XHTML Data.

Through cut-and-paste operations or use of unusual ASCII characters such as mathematical symbols it is possible
to create an incorrectly formatted QTI document that is not importable into Tests and Quizzes.

There are two workarounds for this, using a text editor:

    a. For the well-known symbols, "&", "<" and ">" you can work around the problem by replacing them with their
  XML named entity equivalents, "&amp;", "&lt;", and "&gt;" respectively.

  For all other well-known unicode entities you can replace them with their numbered XML entity equivalents
  (see http://www.w3.org/TR/REC-html40/sgml/entities.html).

  For example, (FORALL) x (SUCH THAT) x (IS GREATER THAN) (SQUAREROOT OF) x

  could be rendered as

  <mattext charset="ascii-us" texttype="text/plain" xml:space="default">&#8704; x : x &gt; &#8730; x</mattext>

  Note that legibility for some entities may be dependent on the browser used.

    b. A more general solution is to enclose the non-XHTML text in CDATA.  For example:
  <mattext texttype="text/html"><![CDATA[True-False: Ist diese Frage richtig oder falsch? (Richtig) ]]></mattext>

  Note that the XML parser will make no attempt to validate the enclosed character data, and will then
  accept non-XHTML and even bad HTML.

7. Workaround for fill-in-the-blank.(SAM-271)
The workaround is to have some non-blank text in the phrasing of the question, such as
   THE {ends} do not {justify} the means.
instead of
   {ends} do not {justify} the means.


--------------------------------------------------
$Id: README.import_export.txt,v 1.3 2005/06/15 01:06:59 esmiley.stanford.edu Exp $
--------------------------------------------------

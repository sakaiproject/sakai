Notes on QTI Import and Export for version 2.1
===============================================
1. QTI Format. QTI version 1.2 is the only version supported at this time.
  a. Exports may be made in questtestinterop format for either asi
(assessment/section/item) or item.
  b. Only assessment/section/item format may be imported.
  c. Object bank format supported only for web service inquiries via QTIService.

2. Export Assessment XML Format.
   a. In some earlier versions of Samigo, JSF prepended extra lines to QTI.
   b. For backward compatibility, these prepended lines are ignored.
   c. Exported Assessment files now default to the name "exportAssessment.xml".
   d. Firefox users.  You should accept the default
         "Save as Type:" "Web Page, HTML only".
   e. Firefox users.  DO NOT save as "Text File", this will save only the text
nodes of the XML document,and will cause errors on re-import.
   f. Firefox users.  You can also highlight the file name and substitute one
of your own with an xml extension (e.g. "mymultiplechoice.xml").
   g.  Tests and Quizzes will accept files with any extension as long as they
are in correct format for importation.  (SAK-1203).
   h. If you inadvertantly import a non-QTI, or corrupted QTI file, you will be
returned to the author index page and a warning will be displayed. (SAK-1201)


3. Question format issues on import/export.
  FIXED IN THIS VERSION
  a. correct grading of imported assessments. SAK-1955.
  b. correct import and export of timed assessment settings.  SAK-1898.
  c. correct import and export of MCSC as MCSC, not as MCMC.  SAK-1779.
  d. including rationale in import/export. SAK-1824.
  e. unlimited reimporting as unlimited submissions, not 9999. SAK-2176.
  f. match-level feedback now retained for matching questions. SAK-1874.
  KNOWN ISSUES IN THIS VERSION
  a. non-breaking space will be used to supply absent text between blanks in
fill-in-the-blank questions.  (SAK-1872).
  b. high security IP address settings are not retained. (SAK-1873)
  c. blank space is lost in question text for the file upload question.
  d. more than two successive question marks in imported/exported questions
will be lost (e.g. "Does this question have too many question marks?????") will
reimport as "Does this question have too many question marks???").

4. Import and Export of Character Entities and Non-XHTML Data.
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
Respondus questions have some non-Sakai-style QTI formatting issues.



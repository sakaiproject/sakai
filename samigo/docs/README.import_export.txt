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
   h. If you inadvertently import a non-QTI, or corrupted QTI file, you will be
returned to the author index page and a warning will be displayed. (SAK-1201)

3. Format and settings issues on import/export.
  FIXED IN THIS VERSION:
  a. Correct grading of imported assessments. SAK-1955.
  b. Correct import and export of timed assessment settings.  SAK-1898.
  c. Correct import and export of MCSC as MCSC, not as MCMC.  SAK-1779.
  d. Including rationale in import/export. SAK-1824.
  e. "Unlimited" now reimporting as unlimited submissions, not 9999. SAK-2176.
  f. Match-level feedback now retained for matching questions. SAK-1874.
  g. High security IP address settings are now retained. SAK-1873.
  h. Correct answers in multiple choice questions with more than four answers.
Answers after the fourth now preserve their correctness on export.  SAK-2766.
  KNOWN ISSUES IN THIS VERSION:
  a. Non-breaking space will be used to supply absent text between blanks in
fill-in-the-blank questions.  SAK-1872.
  b. Multiple choice single correct and multiple correct questions lose
answer-specific feedback. (Correct and incorrect feedback retained.) SAK-2777.
  c. Blank space is lost in question text for the file upload question.
(E.g. " Blank space is lost in question text. " becomes "Blank space is lost in
question text.")
  d. More than two successive question marks in imported/exported questions
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

7. Images.
  a. Make sure that you observe all copyright and intellectual property
restrictions when importing and exporting images.
  b. In general, when authoring questions, you should not include images that
may not be available (e.g transient, password protected, or https) when a test
is modified later on, or when it is taken.
  c. Specifically, you should not expect images to be visible on an imported
assessment just because it was visible in the originally exported assessment
unless you have access to the url of the original image from a new client on the
new server.



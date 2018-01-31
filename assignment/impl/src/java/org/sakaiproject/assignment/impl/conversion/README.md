# Assignments Conversion

This document describes the Assignments conversion for Sakai 12

## Assignment Conversion Job

The assignments conversion job for Sakai 12 is a quartz job. Which means after one has completed the normal conversion
from Sakai 11 to Sakai 12 there will be new assignment tables specifically:
```text
ASN_ASSIGNMENT
ASN_ASSIGNMENT_ATTACHMENTS
ASN_ASSIGNMENT_GROUPS
ASN_ASSIGNMENT_PROPERTIES

ASN_SUBMISSION
ASN_SUBMISSION_ATTACHMENTS
ASN_SUBMISSION_FEEDBACK_ATTACH
ASN_SUBMISSION_PROPERTIES
ASN_SUBMISSION_SUBMITTER
```

These new tables will be empty until the Assignments Conversion Job is run (_typically using the Job Scheduler_).

The following old assignment tables are used to perform the conversion:
```text
ASSIGNMENT_ASSIGNMENT
ASSIGNMENT_CONTENT
ASSIGNMENT_SUBMISSION
``` 
These tables are never altered by the migration they are simply used as input. Once the conversion is complete these
tables can be discarded, however I would suggest waiting until you are satisified that all data was converted successfully.
**IMPORTANT, Premature removal of these tables before doing some verification is considered a risk.**

#### Running the Conversion
The Assignment Conversion Job can be run as many times as needed it will not reprocess assignments that have already been
converted. Every time it is run it queries the old tables for a list of assignments to convert and then checks that with
new assignment table and removes any assignments that are already converted.

#### If you see a message about a failed assignment:

`AssignmentConversionServiceImpl.convert assignment content a8252d19-69f5-425e-804b-f32e81ff559e xml is invalid skipping assignment 8a252d19-69f5-425e-804b-f32e81ff559e`

The assignment and all its submissions will be completely skipped, it will be attemnpted the next time the job is run.
One should look at the assignments that could not be converted and address the issue in the old tables and rerun the
conversion job.

#### If you see a message about a failed submission:

`AssignmentConversionServiceImpl.convert deserialization of a submission failed in assignment 6cdf38ec-1542-4050-9b14-28a441f548de skipping submission`

The assignment was still converted as well as any other submissions that didn't fail it is up to you locate the failed
submission and correct it manually. One possible way of correcting it would be to fix the submissions xml in the original
table and remove the assignment and all its submissions from the new table and then rerun the assignment conversion.
The assigment and its submissions will be selected for conversion again.    

## Configuration
The following are the configuration options which can be adjusted as one needs. The values shown are the defaults.

- content.cleaner.filter.utf8 = true
  - whether surrogate utf8 chars should be stripped, if your database uses 4 byte utf8 characters this should be set to
  false, most mysql databases use utf8mb3 which is why true is the default.
- content.cleaner.filter.utf8.replacement = ""
  - the replacement character to use if a surrogate is found, the default is to remove it.

The following options are for adjusting the woodstox xml parser, generally nobody will ever need to adjust these but if
you see the following exceptions in the log then then you will need to adjust the appropriate setting in the job.
After increasing the appropriate setting just rerun the job and it will reprocess those assignments that were skipped.
If the error occurred on a submission you will need to delete the Assignment and all its submissions and then rerun the job. 

- `javax.xml.stream.XMLStreamException: Attribute limit (1000) exceeded`
  - Number of Attributes should be increased appropriately
- `javax.xml.stream.XMLStreamException: Maximum attribute size limit (524288) exceeded`
  - Attribute size should be increased appropriately 

See the following [link](https://github.com/FasterXML/woodstox/blob/master/src/main/java/com/ctc/wstx/api/WstxInputProperties.java)

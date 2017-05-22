# Urkund Content Review Implmentation

This is the Urkund content review implementation.

## Sakai properties

- contentreview.enabledProviders=Urkund
- urkund.address=URKUND_RECEIVER_ID (EMAIL)
- urkund.username=URKUND_ACCESS_USERNAME
- urkund.password=URKUND_ACCESS_PASSWORD

#### Optional  (or with default value)
  - urkund.apiURL=https://secure.urkund.com/api/
  - urkund.maxFileSize=20971520
  - urkund.maxRetry=20
  - urkund.spoofemailcontext=CONTEXT (string from 2 to 10 characters)
  - urkund.accept.all.files=false
  - urkund.acceptable.mime.types=application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.sun.xml.writer,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/pdf,text/plain,application/rtf,text/html,text/html,application/vnd.ms-works,application/vnd.oasis.opendocument.text
  - urkund.acceptable.file.extensions=.doc,.docx,.sxw,.ppt,.pptx,.pdf,.txt,.rtf,.html,.htm,.wps,.odt
  - urkund.acceptable.file.types=ACCEPTABLE_FILE_TYPES
  - urkund.networkTimeout=180000
  
#### Enable plagiarism detection in assignments

- assignment.useContentReview=true

#### Debugging

for debugging you can add the following logging config
- log.config.count=3
- log.config.1=DEBUG.org.sakaiproject.contentreview.urkund.UrkundReviewServiceImpl
- log.config.2=DEBUG.org.sakaiproject.contentreview.urkund.UrkundAccountConnection
- log.config.3=DEBUG.org.sakaiproject.contentreview.urkund.UrkundContentValidator
# Compilatio Content Review Implmentation

This is the Compilatio content review implementation.

## Configuration

- contentreview.enabledProviders=Compilatio
- compilatio.secretKey=CLIENT_KEY

#### Optional (or with default value)

  - compilatio.proxyHost=PROXY_HOST
  - compilatio.proxyPort=PROXY_PORT
  - compilatio.apiURL=http://service.compilatio.net/webservices/CompilatioUserClient.php?
  - compilatio.accept.all.files=false
  - compilatio.acceptable.mime.types=application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/excel,application/vnd.ms-excel,application/x-excel,application/x-msexcel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/mspowerpoint,application/powerpoint,application/vnd.ms-powerpoint,application/x-mspowerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/mspowerpoint,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.slideshow,application/pdf,application/postscript,application/postscript,text/plain,text/html,text/html,application/wordperfect,application/x-wpwin,application/vnd.oasis.opendocument.text,text/rtf,application/rtf,application/x-rtf,text/richtext
  - compilatio.acceptable.file.extensions=.doc,.docx,.xls,.xls,.xls,.xls,.xlsx,.ppt,.ppt,.ppt,.ppt,.pptx,.pps,.pps,.ppsx,.pdf,.ps,.eps,.txt,.html,.htm,.wpd,.wpd,.odt,.rtf,.rtf,.rtf,.rtf
  - compilatio.acceptable.file.types=ACCEPTABLE_FILE_TYPES
  - compilatio.filename.max.length=-1
  - compilatio.networkTimeout=180000

#### Enable plagiarism detection in assignments

- assignment.useContentReview=true

#### Debugging

for debugging you can add the following logging config
- log.config.count=3
- log.config.1=DEBUG.org.sakaiproject.contentreview.compilatio.CompilatioReviewServiceImpl
- log.config.2=DEBUG.org.sakaiproject.contentreview.compilatio.CompilatioAccountConnection
- log.config.3=DEBUG.org.sakaiproject.contentreview.compilatio.CompilatioContentValidator

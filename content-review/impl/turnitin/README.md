## Turnitin Content Review Implmentation

This is the turitin content review implementation which is for use with those that use the turnitin service.

### Configuration

- turnitin.apiURL=https://api.turnitin.com/api.asp?
- turnitin.secretKey=[yourAccountIdPassword]
- turnitin.said=[yourSubAccountID]
- turnitin.aid=[repeatYourSubAccountID]
- turnitin.useSourceParameter=true
- turnitin.option.institution_check=false

If not using the "Institution Paper Repository" then it is recommended to disable
sets the available repositories. 0=none; 1=standard; 2=institutional
- turnitin.repository.setting=0,1
- turnitin.repository.setting.value=0

for debugging you can add the following logging config
- log.config.count=2
- log.config.1 = DEBUG.org.sakaiproject.contentreview.impl.turnitin
- log.config.2 = DEBUG.org.sakaiproject.turnitin.util.TurnitinAPIUtil.apicalltrace

### Enable plagiarism detection in assignments

- assignment.useContentReview=true

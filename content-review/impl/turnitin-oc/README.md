## Turnitin OC Content Review Implementation

This is the Turnitin Originality Check content review implementation which is for use with those that use the TurnitinOC service.

### Configuration
```
contentreview.enabledProviders=TurnitinOC
contentreview.defaultProvider=TurnitinOC
assignment.useContentReview=true

# turnitin.oc.serviceUrl (Required)
# Required URL for the Turnitin content review service API
# default: empty
# example: turnitin.oc.serviceUrl=https://example.com/api

# turnitin.oc.apiKey (Required)
# Required API KEY for the Turnitin content review service API
# default: empty
# example: turnitin.oc.apiKey=mysupersecret

# Maximum delay between retires after recoverable errors (Optional)
# default: 240
# turnitin.oc.max.retry.minutes=240

# Maximum number of retries for recoverable errors (Optional)
# default: 16
# turnitin.oc.max.retry=16

# Skips any delays intended to reduce traffic to the content review servers. (Optional)
# For local development only; do not set this in production!
# default: false
# turnitin.oc.skip.delays=false

# turnitin.oc.accept.all.files (Optional)
# If true, any file type will be accepted by Sakai. Invalid file types will still be rejected by Turnitin.
# default: false
# example: turnitin.oc.accept.all.files=false

# turnitin.oc.acceptable.file.extensions (Optional)
# Allows you to customize the set of file extensions that Sakai will allow you to submit to Turnitin. Invalid file types will still be rejected by Turnitin. This list must match the list set for turnitin.oc.acceptable.mime.types and turnitin.oc.acceptable.file.types
# default: A hard coded list of mime types
# example: turnitin.oc.acceptable.file.extensions.count=3
# turnitin.oc.acceptable.file.extensions.1=.pdf
# turnitin.oc.acceptable.file.extensions.2=.docx
# turnitin.oc.acceptable.file.extensions.3=.txt

# turnitin.oc.acceptable.mime.types (Optional)
# Allows you to customize the set of file extensions that Sakai will allow you to submit to Turnitin. Invalid file types will still be rejected by Turnitin. This list must match the list set for turnitin.oc.acceptable.file.extensions and turnitin.oc.acceptable.file.types
# default: A hard coded list of mime types
# example: turnitin.oc.acceptable.mime.types.count=3
# turnitin.oc.acceptable.mime.types.1=application/pdf
# turnitin.oc.acceptable.mime.types.2=application/msword
# turnitin.oc.acceptable.mime.types.3=text/plain

# turnitin.oc.acceptable.file.types (Optional)
# Allows you to customize the set of file extensions that Sakai will allow you to submit to Turnitin. Invalid file types will still be rejected by Turnitin. This list must match the list set for turnitin.oc.acceptable.file.extensions and turnitin.oc.acceptable.mime.types
# default: A hard coded list of file type descriptions
# example: turnitin.oc.acceptable.file.types.count=3
# turnitin.oc.acceptable.file.types.1=PDF
# turnitin.oc.acceptable.file.types.2=Word Doc
# turnitin.oc.acceptable.file.types.3=Rich Text

# turnitin.option.exclude_bibliographic (Optional)
# Allows you to show or hide the "exclude bibliography" option when creating an assignment.
# default: turnitin.option.exclude_bibliographic=true
# example: turnitin.option.exclude_bibliographic=false

# turnitin.option.exclude_bibliographic.default (Optional)
# Allows you to set the default for the "exclude bibliography" option when creating an assignment.
# default: turnitin.option.exclude_bibliographic.default=true
# example: turnitin.option.exclude_bibliographic.default=false

# contentreview.option.exclude_quoted (Optional)
# Allows you to show or hide the "exclude quotes" option when creating an assignment.
# default: contentreview.option.exclude_quoted=true
# example: contentreview.option.exclude_quoted=false

# contentreview.option.exclude_quoted.default (Optional)
# Allows you to set the default for the "exclude quotes" option when creating an assignment.
# default: contentreview.option.exclude_quoted.default=true
# example: contentreview.option.exclude_quoted.default=false

# contentreview.option.store_inst_index (Optional)
# Allows you to show or hide the "index all submissions" option when creating an assignment.
# default: contentreview.option.store_inst_index=true
# example: contentreview.option.store_inst_index=false

# contentreview.option.store_inst_index.default (Optional)
# Allows you to set the default for the "index all submissions" option when creating an assignment.
# default: contentreview.option.store_inst_index.default=true
# example: contentreview.option.store_inst_index.default=false

# turnitin.report_gen_speed.setting (Optional)
# Allows you to customize the list of report generation options when creating an assignment.
# 0=Immediately
# 1=Immediately and At Due Date
# 2=At Due Date
# default: turnitin.report_gen_speed.setting.count=3
# turnitin.report_gen_speed.setting.1=0
# turnitin.report_gen_speed.setting.2=1
# turnitin.report_gen_speed.setting.3=2

# turnitin.report_gen_speed.setting.value (Optional)
# Allows you to set the default selected option for report generation options when creating an assignment.
# 0=Immediately
# 1=Immediately and At Due Date
# 2=At Due Date
# default: turnitin.report_gen_speed.setting.value=0

# turnitin.oc.auto_exclude_self_matching_scope
# Allows you to set the default self matching exclude scope for all submissions
# ALL (Exclude all self matching submissions from Similarity Report)
# NONE (Exclude no self matching submissions from Similarity Report)
# GROUP (Exclude all self matching submissions in current assignment from Similarity Report)
# GROUP_CONTEXT (Exclude all self matching submissions in current course from Similarity Report)
# default: GROUP

# turnitin.oc.may_view_submission_full_source.student
# Allows you to customize the default student role permission for being able to view the full source for a matching student paper in the report.
# If left null, the default role permissions will apply.
# FERPA Warning: These permissions provide access to data that is governed by federal and state laws. By altering these settings, you are certifying (1)
# that all changes made to the permission settings comply fully with all of your organisation's policies regarding access to student records, and (2) that
# the institution takes full and exclusive responsibility for access which the resulting permission settings control.
# default: null
# example: turnitin.oc.may_view_submission_full_source.student=true

#turnitin.oc.may_view_match_submission_info.student
# Allows you to customize the default student role permission for being able to view the user information for a matching student paper in the report.
# If left null, the default role permissions will apply.
# FERPA Warning: These permissions provide access to data that is governed by federal and state laws. By altering these settings, you are certifying (1)
# that all changes made to the permission settings comply fully with all of your organisation's policies regarding access to student records, and (2) that
# the institution takes full and exclusive responsibility for access which the resulting permission settings control.
# default: null
# example: turnitin.oc.may_view_match_submission_info.student=true

#turnitin.oc.may_view_submission_full_source.instructor
# Allows you to customize the default instructor role permission for being able to view the full source for a matching student paper in the report.
# If left null, the default role permissions will apply.
# FERPA Warning: These permissions provide access to data that is governed by federal and state laws. By altering these settings, you are certifying (1)
# that all changes made to the permission settings comply fully with all of your organisation's policies regarding access to student records, and (2) that
# the institution takes full and exclusive responsibility for access which the resulting permission settings control.
# default: null
# example: turnitin.oc.may_view_submission_full_source.instructor=true

#turnitin.oc.may_view_match_submission_info.instructor
# Allows you to customize the default instructor role permission for being able to view the user information for a matching student paper in the report.
# If left null, the default role permissions will apply.
# FERPA Warning: These permissions provide access to data that is governed by federal and state laws. By altering these settings, you are certifying (1)
# that all changes made to the permission settings comply fully with all of your organisation's policies regarding access to student records, and (2) that
# the institution takes full and exclusive responsibility for access which the resulting permission settings control.
# default: null
# example: turnitin.oc.may_view_match_submission_info.instructor=true

#turnitin.oc.roles.[TII_ROLE].mapping=SAKAI_ROLE1,SAKAI_ROLE2...
# Allows you to customize the default Sakai->Turnitin role mapping for user's default permission set when submitting and viewing reports.
# Note: if a user is an admin, they will automatically be given the role Administrator. If you want to re-map administrators, clear out the mapping
# like: turnitin.oc.roles.administrator.mapping="" and add "ADMINISTRATOR" to your desired role like: turnitin.oc.roles.instructor.mapping=ADMINISTRATOR,Faculty,Instructor,Mentor,Staff,maintain,Teaching Assistant.
# Default:
# turnitin.oc.roles.instructor.mapping=Faculty,Instructor,Mentor,Staff,maintain,Teaching Assistant
# turnitin.oc.roles.learner.mapping=Learner,Student,access
# turnitin.oc.roles.editor.mapping=""
# turnitin.oc.roles.user.mapping=Alumni,guest,Member,Observer,Other
# turnitin.oc.roles.applicant.mapping=ProspectiveStudent
# turnitin.oc.roles.administrator.mapping=Administrator,Admin
# turnitin.oc.roles.undefined.mapping=""


# Please make sure the property 'version.sakai' is set correctly
```


### Migrating from another service

In order to allow instructors to continue to have access to old reports using another content review service, like VeriCite, you will need to run both providers and configure existing site properties.

#### Example of how to migrate from VeriCite:

##### Step 1: Piloting Turnitin while running VeriCite as default provider:

Enable both providers and keep VeriCite as the default in sakai.properties:
```
contentreview.enabledProviders=TurnitinOC,VeriCite
contentreview.defaultProvider=VeriCite
```
For each site you want to pilot Turnitin, add the site property: `contentreview.provider=TurnitinOC`

##### Step 2: Switching default providers

Run a query to maintain VeriCite as the provider for existing courses (you can limit the scope of sites for this query as much as you like).
```
insert into sakai_site_property(SITE_ID, NAME, VALUE)
select SITE_ID, 'contentreview.provider', 'VeriCite' from sakai_site
where SITE_ID not in (
	select SITE_ID from sakai_site_property where name = 'contentreview.provider'
)
and IS_USER = 0
and IS_SPECIAL = 0
and SITE_ID != '!admin'
and SITE_ID != 'mercury'
-- optional restrictions:
-- and CREATEDON < '2013-08-04'
-- and TYPE = 'course'
-- and SITE_ID in (
--	select SITE_ID from sakai_site_property where NAME = 'term' and VALUE = 'FALL 2013'
-- )
```
Next, switch default provider in sakai.properties and restart Sakai.
```
contentreview.defaultProvider=TurnitinOC
```

##### Step 3: Remove old providers

Once a few terms have passed and instructors no longer need direct access to VeriCite reports, you can remove the sakai.properties for that provider and remove VeriCite from `contentreview.enabledProviders`.
```
contentreview.enabledProviders=TurnitinOC
contentreview.defaultProvider=TurnitinOC
```

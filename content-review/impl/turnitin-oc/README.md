## Turnitin OC Content Review Implmentation

This is the Turnitin Originality Check content review implementation which is for use with those that use the TurnitinOC service.

### Configuration

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

# turnitin.accept.all.files (Optional)
# If true, any file type will be accepted by Sakai. Invalid file types will still be rejected by Turnitin.
# default: false
# example: turnitin.accept.all.files=false

# turnitin.acceptable.file.extensions (Optional)
# Allows you to customize the set of file extensions that Sakai will allow you to submit to Turnitin. Invalid file types will still be rejected by Turnitin. This list must match the list set for turnitin.acceptable.mime.types and turnitin.acceptable.file.types
# default: A hard coded list of mime types
# example: turnitin.acceptable.file.extensions.count=3
# turnitin.acceptable.file.extensions.1=.pdf
# turnitin.acceptable.file.extensions.2=.docx
# turnitin.acceptable.file.extensions.3=.txt

# turnitin.acceptable.mime.types (Optional)
# Allows you to customize the set of file extensions that Sakai will allow you to submit to Turnitin. Invalid file types will still be rejected by Turnitin. This list must match the list set for turnitin.acceptable.file.extensions and turnitin.acceptable.file.types
# default: A hard coded list of mime types
# example: turnitin.acceptable.mime.types.count=3
# turnitin.acceptable.mime.types.1=application/pdf
# turnitin.acceptable.mime.types.2=application/msword
# turnitin.acceptable.mime.types.3=text/plain

# turnitin.acceptable.file.types (Optional)
# Allows you to customize the set of file extensions that Sakai will allow you to submit to Turnitin. Invalid file types will still be rejected by Turnitin. This list must match the list set for turnitin.acceptable.file.extensions and turnitin.acceptable.mime.types
# default: A hard coded list of file type descriptions
# example: turnitin.acceptable.file.types.count=3
# turnitin.acceptable.file.types.1=PDF
# turnitin.acceptable.file.types.2=Word Doc
# turnitin.acceptable.file.types.3=Rich Text

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

# Please make sure the property 'version.sakai' is set correctly
## Openstack Swift Sakai Resources

The cloud implementation of FileSystemHandler. This implementation writes and
reads files to and from cloud storage including OpenStack-Swift and S3.

## Usage

Clone and build this module like any other Sakai modules. It can be cloned into
an existing working copy or somewhere parallel. You must use the `sakai:deploy`
Maven goal to deploy the component pack.

To use cloud rather than the default file/database storage, settings must be
configured in two places:

 * sakai-configuration.xml
 * local.properties (or another like sakai.properties)

The only setting in sakai-configuration.xml needed is the bean alias to activate
this handler rather than the default. This file must be a valid Spring bean
config file; either create it in its entirety or just add this alias. Only one
handler may be active.

~~~~
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">

<beans>
  <!-- Swift handler -->
  <alias name="org.sakaiproject.content.api.FileSystemHandler.swift" alias="org.sakaiproject.content.api.FileSystemHandler" />

  <!-- Generic BlobStore handler, currently only for AWS S3 -->
  <!-- <alias name="org.sakaiproject.content.api.FileSystemHandler.blobstore" alias="org.sakaiproject.content.api.FileSystemHandler" /> -->
</beans>
~~~~

The actual configuration values should be supplied using the bean property
syntax in, e.g., `local.properties`. The standard `bodyPath` and
`bodyPathDeleted` properties must be set or the files will be stored in the
database. However, these should not be prefixed with `${sakai.home} or local
directory names since all leading paths will be created as containers or
pseudo-folders as needed, adding unneeded depth in your object store. The
`useIdForPath` setting can be either true or false, but true is recommended
as the paths will mirror the resource IDs/URLs.

Settings are only required for the active handler, but may be supplied for an
inactive handler as well. Pay close attention to the suffix (swift/blobstore)
and the slight differences in the available keys. For example, the endpoint
is required for Swift, while the baseContainer is required for S3. The provider
is optional for BlobStore, since only aws-s3 is the only one currently available
and is the default.

~~~~
endpoint@org.sakaiproject.content.api.FileSystemHandler.swift     = http://swift.server:5000/v2.0/
identity@org.sakaiproject.content.api.FileSystemHandler.swift     = tenant:username
credential@org.sakaiproject.content.api.FileSystemHandler.swift   = password
region@org.sakaiproject.content.api.FileSystemHandler.swift       = RegionOne
useIdForPath@org.sakaiproject.content.api.FileSystemHandler.swift = true

provider@org.sakaiproject.content.api.FileSystemHandler.blobstore      = aws-s3
identity@org.sakaiproject.content.api.FileSystemHandler.blobstore      = <S3 Access Key ID>
credential@org.sakaiproject.content.api.FileSystemHandler.blobstore    = <S3 Secret Access Key>
baseContainer@org.sakaiproject.content.api.FileSystemHandler.blobstore = your-bucket-name
useIdForPath@org.sakaiproject.content.api.FileSystemHandler.blobstore  = true

bodyPath@org.sakaiproject.content.api.ContentHostingService=/content/live/
bodyPathDeleted@org.sakaiproject.content.api.ContentHostingService=/content/deleted/
~~~~

If you are not familiar with `sakai-configuration.xml`, it is placed in the same location
as the properties files. See Confluence for an overview:
[https://confluence.sakaiproject.org/display/REL/More+Flexible+Sakai+Configuration](More Flexible Sakai Configuration)

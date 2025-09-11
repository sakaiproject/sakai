## MinIO Sakai Resources

The cloud implementation of `FileSystemHandler` backed by a MinIO/S3
compatible object store. This replaces the previous jclouds based handlers for
Swift and generic S3 providers.

## Authors

This module is based on
[contributions](https://github.com/OpenCollabZA/sakai-openstack-swift)
from OpenCollab under the
[Educational Community License v2.0](http://opensource.org/licenses/ECL-2.0).
It is also contains contributions from Longsight, Inc.

## Usage

Build this module like any other Sakai modules. You must use the `sakai:deploy`
Maven goal to deploy the component pack as usual. It is included in the default
profile of the top-level build as well.

To use cloud rather than the default file/database storage, settings must be
configured in two places:

 * `sakai-configuration.xml`
 * `local.properties` (or another like `sakai.properties`)

The only setting in `sakai-configuration.xml` needed is the bean alias to
activate this handler rather than the default. This file must be a valid Spring
bean config file; either create it in its entirety or just add this alias. Only
one handler may be active. To avoid configuration changes, this handler uses the
same alias as the former jclouds BlobStore implementation.

~~~~
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">

<beans>
  <!-- MinIO-backed BlobStore handler -->
  <alias name="org.sakaiproject.content.api.FileSystemHandler.blobstore" alias="org.sakaiproject.content.api.FileSystemHandler" />
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
inactive handler as well. All properties are suffixed with
`@org.sakaiproject.content.api.FileSystemHandler.blobstore`.

~~~~
endpoint@org.sakaiproject.content.api.FileSystemHandler.blobstore     = https://minio.example.edu
identity@org.sakaiproject.content.api.FileSystemHandler.blobstore     = <ACCESS_KEY>
credential@org.sakaiproject.content.api.FileSystemHandler.blobstore   = <SECRET_KEY>
baseContainer@org.sakaiproject.content.api.FileSystemHandler.blobstore= sakai-content
useIdForPath@org.sakaiproject.content.api.FileSystemHandler.blobstore = true
cloud.content.signedurl.expiry                                   = 600
cloud.content.multipart.partsize.mb                              = 10
cloud.content.maxblobstream.size                                 = 104857600
cloud.content.temporary.directory                                = /var/tmp/sakai-blobs

bodyPath@org.sakaiproject.content.api.ContentHostingService=/content/live/
bodyPathDeleted@org.sakaiproject.content.api.ContentHostingService=/content/deleted/
~~~~

If you are not familiar with `sakai-configuration.xml`, it is placed in the same location
as the properties files. See Confluence for an overview:
[https://confluence.sakaiproject.org/display/REL/More+Flexible+Sakai+Configuration](More Flexible Sakai Configuration)


## Testing

Integration tests require access to a MinIO server. Launch a local MinIO
container and configure the properties above before running
`mvn -pl cloud-content/impl test`.

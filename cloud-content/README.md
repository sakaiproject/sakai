# MinIO/S3 Cloud Content Handler

## Overview

The cloud implementation of `FileSystemHandler` is backed by an S3-compatible
object store using the MinIO Java SDK. This handler is the supported approach
for Sakai cloud content. The legacy jclouds-based S3 and Swift handlers have
been removed; S3 compatibility has become the standard, and MinIO remains a
maintained and reliable choice.

This module incorporates
[OpenCollab](https://github.com/OpenCollabZA/sakai-openstack-swift) and
Longsight contributions under the
[Educational Community License v2.0](http://opensource.org/licenses/ECL-2.0).

## Key Benefits

- Single-pass streaming uploads with unknown content length via multipart
  transfer.
- Lower memory usage; no need to buffer entire uploads for length detection.
- Clearer S3 error reporting and retry support from the MinIO SDK.
- Simplified dependency footprint by replacing jclouds and Swift-specific code.

## Architecture Notes

- The handler continues to implement `org.sakaiproject.content.api.FileSystemHandler`.
- Spring wiring preserves the bean id `org.sakaiproject.content.api.FileSystemHandler.blobstore`
  to avoid configuration churn.
- Uploads stream directly to S3/MinIO while the kernel computes SHA-256 and byte
  counts.
- Direct download links use presigned URLs with configurable expiry.
- Downloads stream directly from the object store with optional temp-file spill
  above `cloud.content.maxblobstream.size`.

### Streaming Upload Pattern

```java
MinioClient client = MinioClient.builder()
    .endpoint(endpoint)
    .credentials(identity, credential)
    .build();

int partSize = Math.max(5 * 1024 * 1024, configuredPartSize);
try (CountingInputStream cis = new CountingInputStream(stream)) {
  client.putObject(
      PutObjectArgs.builder()
          .bucket(bucket)
          .object(objectKey)
          .stream(cis, -1, partSize) // -1 allows unknown total size
          .contentType(contentType)
          .build()
  );
  long bytesStored = cis.getCount();
}
```

## Configuration

Build this module like any other Sakai module. Use the `sakai:deploy` Maven goal
to deploy the component pack; it is included in the default top-level profile.

### Spring Alias

In `sakai-configuration.xml`, ensure the alias activates the MinIO-backed
handler. The file must remain valid Spring XML.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">

<beans>
  <!-- MinIO-backed FileSystemHandler -->
  <alias name="org.sakaiproject.content.api.FileSystemHandler.blobstore"
         alias="org.sakaiproject.content.api.FileSystemHandler" />
</beans>
```

If you are new to `sakai-configuration.xml`, it lives alongside `local.properties`
and related property files. See the Sakai Confluence documentation on flexible
configuration for additional background.

### Property Settings

Provide handler settings via bean properties in `local.properties`,
`sakai.properties`, or another configuration file. Only the active handler's
properties are required. All properties are suffixed with
`@org.sakaiproject.content.api.FileSystemHandler.blobstore`.

```properties
endpoint@org.sakaiproject.content.api.FileSystemHandler.blobstore      = https://minio.example.edu
identity@org.sakaiproject.content.api.FileSystemHandler.blobstore      = <ACCESS_KEY>
credential@org.sakaiproject.content.api.FileSystemHandler.blobstore    = <SECRET_KEY>
baseContainer@org.sakaiproject.content.api.FileSystemHandler.blobstore = sakai-content
useIdForPath@org.sakaiproject.content.api.FileSystemHandler.blobstore  = true
cloud.content.signedurl.expiry                                         = 600
cloud.content.multipart.partsize.mb                                    = 10
cloud.content.maxblobstream.size                                       = 104857600
cloud.content.temporary.directory                                      = /var/tmp/sakai-blobs

bodyPath@org.sakaiproject.content.api.ContentHostingService  = /content/live/
bodyPathDeleted@org.sakaiproject.content.api.ContentHostingService = /content/deleted/
```

The handler also accepts the legacy `accessKey` and `secretKey` property names
as synonyms for `identity` and `credential` to ease property migrations. Avoid
prefixing `bodyPath` values with `${sakai.home}` or local filesystem roots;
leading path elements become object store containers or pseudo-folders.

## Operational Considerations

- Multipart uploads require part sizes ≥ 5 MiB and ≤ 10,000 parts (≈5 TiB
  maximum object size).
- Multipart ETags differ from MD5; do not rely on ETag for integrity checks.
- Explicitly set `contentType`; MinIO does not infer types automatically.
- Tune presigned URL expiry via `cloud.content.signedurl.expiry` and ensure NTP
  synchronization to avoid clock-skew issues.
- For installations requiring server-side encryption, configure the appropriate
  SSE headers when building `PutObjectArgs`.

## Legacy Status

The jclouds-based S3 and Swift handlers have been retired. Swift users should
transition to S3-compatible endpoints (MinIO, AWS S3, Ceph, etc.). MinIO is the
maintained and recommended implementation going forward.

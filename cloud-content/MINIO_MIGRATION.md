# MinIO Migration Guide (cloud-content)

This document outlines how to migrate Sakai's cloud-content storage from the deprecated jclouds handlers (S3 BlobStore and Swift) to the MinIO Java SDK. The primary benefit is true streaming uploads without pre-counting Content-Length, leading to lower memory use and faster uploads.

## Goals

- Replace jclouds-based S3/Swift handlers with a MinIO-based handler.
- Remove content-length pre-counting and double reads on upload.
- Maintain existing `FileSystemHandler` API and behavior (direct links, downloads, deletes).
- Simplify dependencies and configuration.

## Key Gains

- Remove Content-Length counting: stream unknown-length uploads using multipart (`stream(in, -1, partSize)`).
- Lower memory: no `mark/reset` over entire stream; memory bounded by part size and HTTP buffers.
- Faster single-pass uploads: compute SHA-256 and byte count while streaming.
- Simpler deps: drop jclouds + OSGi metadata registries for one small SDK.
- Clearer errors/retries: MinIO surfaces S3 errors and supports multipart retries.

## Proposed Architecture

- Keep `org.sakaiproject.content.api.FileSystemHandler` as the integration seam.
- Add `MinioFileSystemHandler` under `cloud-content/impl` and wire via Spring alias.
- Uploads: wrap the incoming stream to compute SHA-256 and total bytes inline, while MinIO streams to the bucket using multipart with unknown total length.
- Direct links: use presigned URLs from MinIO SDK.
- Downloads: stream directly from MinIO; optionally spill to temp file above a size threshold (parity with current behavior).
- Metadata: continue to set `id`/`path` metadata if needed for parity.

## Migration Plan

1. Add dependency
   - Add to `cloud-content/impl/pom.xml`:
     - `io.minio:minio` (8.x+)

2. Implement `MinioFileSystemHandler`
   - Implement `FileSystemHandler` methods using MinIO client:
     - saveInputStream: stream unknown-length uploads; compute hash/length inline.
     - getInputStream: `getObject` stream; keep temp-file spill logic/config.
     - getAssetDirectLink: presigned GET with configurable expiry.
     - delete: `removeObject`.

3. Upload streaming pattern (no Content-Length pre-counting)

```java
MinioClient client = MinioClient.builder()
    .endpoint(endpoint)
    .credentials(accessKey, secretKey)
    .build();

int partSize = Math.max(5 * 1024 * 1024, configuredPartSize); // >= 5 MiB
MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
try (DigestInputStream dis = new DigestInputStream(stream, sha256);
     CountingInputStream cis = new CountingInputStream(dis)) {

  client.putObject(
      PutObjectArgs.builder()
          .bucket(bucket)
          .`object`(objectKey)
          .stream(cis, -1, partSize)         // -1 = unknown total size
          .contentType(contentType)
          .build()
  );

  long bytes = cis.getCount();
  String hex = bytesToHex(sha256.digest()); // record length + sha256
}
```

- Notes:
  - If you must de-duplicate by SHA-256 before upload, spool to a temp file, hash it, then upload via `stream(new FileInputStream(file), file.length(), partSize)`.

4. Direct links (presigned URLs)

```java
String url = client.getPresignedObjectUrl(
    GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .bucket(bucket)
        .`object`(objectKey)
        .expiry(expiryMinutes, TimeUnit.MINUTES)
        .build()
);
```

5. Downloads
- Use `client.getObject(GetObjectArgs.builder().bucket(bucket).`object`(key).build())`.
- For large objects, optionally copy to a temp file first (retain `cloud.content.maxblobstream.size`).

6. Deletes
- `client.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(key).build())`.

7. Wire Spring configuration
- Add bean: `org.sakaiproject.content.api.FileSystemHandler.minio`.
- In `sakai-configuration.xml` set alias:

```xml
<alias name="org.sakaiproject.content.api.FileSystemHandler.minio"
       alias="org.sakaiproject.content.api.FileSystemHandler" />
```

8. Configuration keys (proposed)

- MinIO handler properties (example in `local.properties`):

```
endpoint@org.sakaiproject.content.api.FileSystemHandler.minio     = https://minio.example.edu
accessKey@org.sakaiproject.content.api.FileSystemHandler.minio    = <ACCESS_KEY>
secretKey@org.sakaiproject.content.api.FileSystemHandler.minio    = <SECRET_KEY>
baseContainer@org.sakaiproject.content.api.FileSystemHandler.minio= sakai-content
useIdForPath@org.sakaiproject.content.api.FileSystemHandler.minio = true
cloud.content.signedurl.expiry                                     = 600   # seconds
cloud.content.multipart.partsize.mb                                = 10    # >= 5
cloud.content.maxblobstream.size                                   = 104857600
cloud.content.temporary.directory                                  = /var/tmp/sakai-blobs
```

- Backward-compat: accept old S3/Swift keys and log a deprecation warning when MinIO is active.

## Deprecation Plan (jclouds S3 + Swift)

- Mark both jclouds handlers as deprecated immediately.
- Keep them for one release cycle with clear warnings in logs and docs.
- Remove both in the next major release unless active Swift users are identified.
- Rationale: S3-compatible APIs have effectively won; Swift deployments commonly expose S3 gateways; jclouds adds significant footprint and complexity.

## What to Watch Out For

- Multipart constraints: part size ≥ 5 MiB; max 10,000 parts (~5 TiB objects max).
- ETag semantics: multipart ETag is not MD5; do not use it for integrity checks.
- Pre-upload de-duplication: requires SHA-256 before upload; keep temp-file hash flow if needed.
- Content-Type: set explicitly on upload; do not rely on auto-detection.
- Endpoint style: MinIO typically path-style; align DNS/SSL configuration accordingly.
- Server-side encryption: if required, configure SSE-S3/SSE-KMS headers in `PutObjectArgs`.
- Timeouts/retries: tune HTTP timeouts and multipart retry policies for WANs.
- Clock skew: presigned URL validity is time-sensitive; ensure NTP sync.
- Permissions: ensure access keys have PUT/GET/DELETE for the target bucket/prefix.
- Licensing: MinIO Java SDK is Apache-2.0, compatible with Sakai.

## Testing

- Local MinIO container for integration tests.
- Cases: unknown-length upload streaming, large multipart uploads, presigned URL fetch, large-download temp-file spill, delete.
- Kernel interop: verify SHA-256 and length captured; existing single-instance dedupe logic continues to work (if retained).
- Performance: compare memory and throughput vs jclouds; expect significantly lower memory use and faster uploads.

## Rollout

1. Ship MinIO handler alongside jclouds (default remains jclouds) and gather feedback.
2. Switch default alias to MinIO for new installs; emit deprecation warnings when old handlers are used.
3. Remove jclouds S3 + Swift after one release cycle; provide a migration guide and property mapping.

## Appendix: Property Mapping

- Old (BlobStore/S3 via jclouds)
  - `provider@...blobstore`, `identity@...blobstore`, `credential@...blobstore`, `baseContainer@...blobstore`.
- Old (Swift via jclouds)
  - `endpoint@...swift`, `identity@...swift`, `credential@...swift`, `region@...swift`.
- New (MinIO)
  - `endpoint@...minio`, `accessKey@...minio`, `secretKey@...minio`, `baseContainer@...minio`.

## FAQ

- Why remove Content-Length counting?
  - With jclouds BlobStore we pre-read streams to get a length for `contentLength(size)`. MinIO supports streaming unknown-length data using multipart, so we can upload in a single pass and compute SHA-256 and size on the fly.

- Should we remove the old Swift BlobStore?
  - Recommendation: yes, deprecate now and remove next major unless active Swift users are identified. S3 compatibility has effectively won, and Swift often exposes S3 endpoints.


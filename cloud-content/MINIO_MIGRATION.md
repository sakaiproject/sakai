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
- Faster single-pass uploads: stream uploads while kernel computes SHA-256 and byte count.
- Simpler deps: drop jclouds + OSGi metadata registries for one small SDK.
- Clearer errors/retries: MinIO surfaces S3 errors and supports multipart retries.

## Proposed Architecture

- Keep `org.sakaiproject.content.api.FileSystemHandler` as the integration seam.
- Replace the jclouds `BlobStoreFileSystemHandler` with a MinIO-backed implementation but retain the same Spring bean id and configuration keys for drop-in compatibility.
- Uploads: stream the incoming data while kernel's DigestInputStream computes SHA-256 and the handler counts bytes, allowing MinIO to upload multipart data without a known total length.
- Direct links: use presigned URLs from MinIO SDK.
- Downloads: stream directly from MinIO; optionally spill to temp file above a size threshold (parity with current behavior).
- Metadata: continue to set `id`/`path` metadata if needed for parity.

## Migration Plan

1. Add dependency
   - Add to `cloud-content/impl/pom.xml`:
     - `io.minio:minio` (8.x+)

2. Implement `BlobStoreFileSystemHandler` (MinIO-backed)
   - Implement `FileSystemHandler` methods using MinIO client:
     - saveInputStream: stream unknown-length uploads and count bytes (kernel supplies SHA-256).
     - getInputStream: `getObject` stream; keep temp-file spill logic/config.
     - getAssetDirectLink: presigned GET with configurable expiry.
     - delete: `removeObject`.

3. Upload streaming pattern (no Content-Length pre-counting)

```java
MinioClient client = MinioClient.builder()
    .endpoint(endpoint)
    .credentials(identity, credential)
    .build();

int partSize = Math.max(5 * 1024 * 1024, configuredPartSize); // >= 5 MiB
try (CountingInputStream cis = new CountingInputStream(stream)) {

  client.putObject(
      PutObjectArgs.builder()
          .bucket(bucket)
          .`object`(objectKey)
          .stream(cis, -1, partSize)         // -1 = unknown total size
          .contentType(contentType)
          .build()
  );

  long bytes = cis.getCount(); // kernel's DigestInputStream computes SHA-256
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
- Bean id remains `org.sakaiproject.content.api.FileSystemHandler.blobstore`.
- In `sakai-configuration.xml` ensure the alias still points to `org.sakaiproject.content.api.FileSystemHandler.blobstore`.

8. Configuration keys

- MinIO handler properties (example in `local.properties`):

```
endpoint@org.sakaiproject.content.api.FileSystemHandler.blobstore     = https://minio.example.edu
identity@org.sakaiproject.content.api.FileSystemHandler.blobstore     = <ACCESS_KEY>
credential@org.sakaiproject.content.api.FileSystemHandler.blobstore   = <SECRET_KEY>
baseContainer@org.sakaiproject.content.api.FileSystemHandler.blobstore= sakai-content
useIdForPath@org.sakaiproject.content.api.FileSystemHandler.blobstore = true
cloud.content.signedurl.expiry                                     = 600   # seconds
cloud.content.multipart.partsize.mb                                = 10    # >= 5
cloud.content.maxblobstream.size                                   = 104857600
cloud.content.temporary.directory                                  = /var/tmp/sakai-blobs
```

- Backward-compat: `accessKey`/`secretKey` are still accepted as synonyms for `identity`/`credential`.

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

1. Ship MinIO-backed handler alongside jclouds (default remains jclouds) and gather feedback.
2. Switch default alias to the MinIO-backed handler for new installs; emit deprecation warnings when old handlers are used.
3. Remove jclouds S3 + Swift after one release cycle; provide a migration guide and property mapping.

## Appendix: Property Mapping

- Old (BlobStore/S3 via jclouds)
  - `provider@...blobstore`, `identity@...blobstore`, `credential@...blobstore`, `baseContainer@...blobstore`.
- Old (Swift via jclouds)
  - `endpoint@...swift`, `identity@...swift`, `credential@...swift`, `region@...swift`.
- New (MinIO)
  - `endpoint@...blobstore`, `identity@...blobstore`, `credential@...blobstore`, `baseContainer@...blobstore`.

## FAQ

- Why remove Content-Length counting?
  - With jclouds BlobStore we pre-read streams to get a length for `contentLength(size)`. MinIO supports streaming unknown-length data using multipart, so uploads happen in a single pass while the kernel computes SHA-256 and size concurrently.

- Should we remove the old Swift BlobStore?
  - Recommendation: yes, deprecate now and remove next major unless active Swift users are identified. S3 compatibility has effectively won, and Swift often exposes S3 endpoints.


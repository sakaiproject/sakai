# Sakai Single-Instance Store Documentation

## Table of Contents
1. [Overview](#overview)
2. [Configuration](#configuration)
3. [Technical Architecture](#technical-architecture)
4. [Operational Flow](#operational-flow)
5. [Security Considerations](#security-considerations)
6. [Troubleshooting & Monitoring](#troubleshooting--monitoring)

## Overview

### What is Single-Instance Store?

Single-Instance Store is a content deduplication feature in Sakai's Content Hosting system that eliminates duplicate file storage by sharing identical files across different sites, users, and contexts. When multiple users upload an identical file (identical content), only one copy is physically stored on disk, significantly reducing storage requirements.

### Purpose and Benefits

- **Storage Efficiency**: Dramatically reduces disk space usage by eliminating duplicate files
- **Performance**: Faster uploads when files already exist (no need to write to disk)
- **Cost Savings**: Reduced storage costs, especially important for large Sakai installations
- **Environmental Impact**: Lower storage requirements reduce datacenter resource consumption

### Key Concept

Files with identical **content** share the same physical file on disk, regardless of:
- Which site they were uploaded to
- Which user uploaded them
- What the filename is
- Where they are located in the folder structure

The deduplication is based purely on the SHA-256 cryptographic hash of the file content.

## Configuration

### Property Settings

Single-Instance Store is controlled by a single configuration property:

```properties
# Enable/disable Single-Instance Store (default: true)
content.singleInstanceStore=true
```

**Location**: `sakai.properties` or equivalent configuration file

**Default Value**: `true` (enabled by default as of SAK-48238/SAK-51701)

### Requirements

Single-Instance Store only works when:
1. Content is stored in the **filesystem** (not database BLOB storage)
2. The `bodyPath` configuration is set for file system storage
3. External file storage is properly configured

If content is stored in database BLOBs, Single-Instance Store is automatically disabled.

## Technical Architecture

### Database Schema

The Single-Instance Store functionality relies on these key database fields in the `CONTENT_RESOURCE` table:

```sql
CREATE TABLE CONTENT_RESOURCE (
    RESOURCE_ID VARCHAR(255) NOT NULL,        -- Unique resource identifier
    RESOURCE_UUID VARCHAR(36),                -- UUID for the resource
    RESOURCE_SHA256 VARCHAR(64),              -- SHA-256 hash (deduplication key)
    IN_COLLECTION VARCHAR(255),               -- Parent collection
    CONTEXT VARCHAR(99),                      -- Site context
    FILE_PATH VARCHAR(128),                   -- Physical file location
    FILE_SIZE BIGINT,                         -- File size in bytes
    RESOURCE_TYPE_ID VARCHAR(255),            -- Resource type
    XML LONGTEXT,                             -- Resource metadata
    BINARY_ENTITY BLOB                        -- Not used with filesystem storage
);

-- Index for efficient hash lookups
CREATE INDEX CONTENT_RESOURCE_SHA256 ON CONTENT_RESOURCE (RESOURCE_SHA256);
```

**Key Fields**:
- `RESOURCE_SHA256`: The 64-character hex SHA-256 hash used for deduplication
- `FILE_PATH`: Points to the actual file location on disk (shared when hashes match)
- `RESOURCE_ID`: Unique identifier for each database record (never shared)

### Hash Calculation Process

SHA-256 hashes are calculated during file upload using Java's built-in cryptographic functions:

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
DigestInputStream dstream = new DigestInputStream(inputStream, digest);
// ... file is processed through DigestInputStream ...
MessageDigest md2 = dstream.getMessageDigest();
String hex = StorageUtils.bytesToHex(md2.digest());
resource.setContentSha256(hex);
```

**Process**:
1. File content streams through `DigestInputStream` during upload
2. SHA-256 hash calculated incrementally as bytes are processed
3. Final hash converted to 64-character hexadecimal string
4. Hash stored in `RESOURCE_SHA256` database field

### Deduplication Logic

The core deduplication query searches for existing files with matching hashes:

```sql
SELECT file_path FROM CONTENT_RESOURCE 
WHERE resource_sha256 = ? 
ORDER BY file_path DESC 
LIMIT 1
```

## Operational Flow

### New File Upload Process

1. **User uploads file** via Sakai interface
2. **Temporary staging**: File is streamed to a temporary location while SHA-256 is computed
3. **Hash calculation**: SHA-256 computed during upload stream
4. **Deduplication check**: Query database for existing files with same hash
5. **Decision point**:
   - **Hash found**: Delete temporary file, reuse existing `FILE_PATH`
   - **Hash not found**: Keep new file, store new `FILE_PATH`
6. **Database record**: Create new `CONTENT_RESOURCE` record with hash and path
7. **User sees**: File appears normally in their site/collection

### File Access Process

1. **User requests file** via URL
2. **Database lookup**: Find `CONTENT_RESOURCE` record by `RESOURCE_ID`
3. **File retrieval**: Use `FILE_PATH` to locate physical file
4. **Content delivery**: Serve file content to user
5. **Metadata**: User sees their filename, permissions, properties (not shared)

### File Deletion Process

1. **User deletes file** from their site
2. **Database cleanup**: `CONTENT_RESOURCE` record moved to delete table
3. **Reference counting**: Check if other resources reference same `FILE_PATH`
4. **Physical deletion**: Only delete physical file when last reference removed
5. **Safety**: Files with existing references are preserved

## Security Considerations

### Content Isolation

**Critical Security Principle**: Each `CONTENT_RESOURCE` database record maintains complete isolation of metadata, permissions, and access control, even when physical files are shared.

**What's Shared**:
- Physical file content on disk
- `FILE_PATH` value in database

**What's NOT Shared** (Isolated per resource):
- `RESOURCE_ID` (always unique)
- File permissions and access controls
- Metadata and properties
- Site context and collection membership
- User-visible filename

### The Content Leakage Vulnerability (Fixed)

**Historical Issue**: Before recent fixes, the "Upload New Version" functionality could cause content leakage between sites.

**Root Cause**: "Upload New Version" reused existing database records instead of creating new ones, causing file path reuse without proper content verification.

**Fix Implementation**: "Upload New Version" now works exactly like uploading a new file:
1. Creates completely new `CONTENT_RESOURCE` record
2. Calculates new SHA-256 hash
3. Allows Single-Instance Store to work safely
4. Deletes old resource after successful creation

**Security Guarantee**: With the fix in place, users can never access content they didn't upload themselves, even when files are physically shared.

### Best Practices

1. **Regular Audits**: Periodically verify that file access permissions work correctly
2. **Monitoring**: Track storage savings to ensure Single-Instance Store is functioning
3. **Testing**: Test "Upload New Version" functionality in multi-site scenarios
4. **Backup Strategy**: Account for shared files in backup and recovery procedures

## Troubleshooting & Monitoring

### Verifying Single-Instance Store Operation

**Check Configuration**:
```sql
-- Verify feature is enabled
SELECT * FROM SAKAI_CLUSTER_CONFIG WHERE PROPERTY_NAME = 'content.singleInstanceStore';
```

**Monitor Deduplication**:
```sql
-- Count total resources vs unique file paths
SELECT 
    COUNT(*) as total_resources,
    COUNT(DISTINCT FILE_PATH) as unique_files,
    COUNT(*) - COUNT(DISTINCT FILE_PATH) as duplicates_eliminated
FROM CONTENT_RESOURCE 
WHERE FILE_PATH IS NOT NULL;
```

**Find Most Deduplicated Files**:
```sql
-- Files with most sharing
SELECT 
    FILE_PATH,
    RESOURCE_SHA256,
    COUNT(*) as reference_count
FROM CONTENT_RESOURCE 
WHERE FILE_PATH IS NOT NULL
GROUP BY FILE_PATH, RESOURCE_SHA256
HAVING COUNT(*) > 1
ORDER BY COUNT(*) DESC
LIMIT 10;
```

### Storage Savings Calculation

```sql
-- Calculate approximate storage savings
WITH blobs AS (
  SELECT FILE_PATH, MAX(FILE_SIZE) AS blob_size
  FROM CONTENT_RESOURCE
  WHERE FILE_PATH IS NOT NULL
  GROUP BY FILE_PATH
)
SELECT
  (SELECT SUM(FILE_SIZE) FROM CONTENT_RESOURCE WHERE FILE_PATH IS NOT NULL) AS total_logical_size,
  (SELECT SUM(blob_size) FROM blobs) AS actual_physical_size,
  (SELECT SUM(FILE_SIZE) FROM CONTENT_RESOURCE WHERE FILE_PATH IS NOT NULL) - (SELECT SUM(blob_size) FROM blobs) AS space_saved;
```

### Common Issues and Solutions

**Issue**: Single-Instance Store not working
- **Check**: Verify `content.singleInstanceStore=true` in configuration
- **Check**: Ensure filesystem storage is enabled (not database BLOB)
- **Check**: Verify `bodyPath` configuration is correct

**Issue**: Unexpected storage usage
- **Check**: Database vs filesystem storage configuration
- **Check**: Old files not being cleaned up properly
- **Monitor**: File deletion processes and reference counting

**Issue**: File access problems
- **Check**: File permissions on shared storage location
- **Check**: `FILE_PATH` values in database match actual files
- **Verify**: No broken symbolic links or missing files

### Log Analysis

Look for these log messages in Sakai logs:

```text
DEBUG: Duplicate body found path=/content/shared/abc123.bin
DEBUG: Content body is unique id=/group/site1/resource123
```

**Healthy Operation**: Mix of "duplicate found" and "unique" messages
**Potential Issue**: Only "unique" messages (no deduplication happening)

### Performance Monitoring

Single-Instance Store should improve performance for duplicate uploads:
- **Faster upload times** for files that already exist
- **Reduced I/O** on storage systems
- **Lower network bandwidth** for distributed storage

Monitor these metrics to verify expected performance improvements.

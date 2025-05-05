# Assignment Group Locking Documentation

## Overview

This document describes the group locking behavior in the Assignments tool in Sakai.

When an assignment is set up with group submission mode, the system locks the group membership to prevent changes after the assignment opens. This ensures fair and consistent group assignments and prevents membership changes that could disrupt submissions and grading.

## Group Locking Behavior

### Default Behavior (Prior to SAK-49085)

In Sakai prior to SAK-49085, group locking behavior was as follows:

1. When an assignment with group submissions is posted (i.e., saved and not in draft mode), the system immediately applies a lock to all associated groups.
2. Once locked, the group membership cannot be modified until the assignment is deleted or fully graded.
3. This immediate locking caused issues for instructors who wanted to post assignments in advance but still allow group membership changes until the assignment opened.

### Enhanced Behavior (SAK-49085)

The enhanced group locking behavior introduced in SAK-49085 operates as follows:

1. When an assignment with group submissions is posted, the system checks the assignment's open date:
   - If the open date is in the future, the groups remain unlocked
   - If the open date has already passed, the groups are locked immediately (same as original behavior)

2. A scheduled job runs periodically to check for assignments about to open (within 5 minutes) and locks their groups at the appropriate time.

3. For assignments with future open dates:
   - If the assignment has no submissions, instructors can still modify group membership until the open date
   - If the assignment already has submissions, groups remain locked regardless of open date changes

4. Special handling for Lessons tool integration:
   - Groups created by Lessons for access control purposes are handled differently
   - These "access groups" use a different locking mode (DELETE) rather than the full lock (ALL) used for regular groups

## Technical Implementation

The implementation uses the following components:

1. **AuthzGroup Locking Mechanism**: Uses Sakai's built-in group locking functionality with different lock modes:
   - `RealmLockMode.ALL`: Prevents all membership changes (used for regular groups)
   - `RealmLockMode.DELETE`: Prevents deletion but allows membership changes (used for Lessons access groups)
   - `RealmLockMode.NONE`: No locks applied

2. **Scheduled Jobs**:
   - `AssignmentGroupLockingJob`: Runs periodically to check for assignments about to open and locks their groups
   - `AssignmentGroupUnlockingJob`: Handles unlocking for assignments that have had their open date pushed to the future

3. **Lock Management**:
   - Locks are applied and removed through the `AuthzGroupService`
   - Lock state is maintained even if the server restarts

## Configuration

The group locking jobs are configured through Sakai's Quartz scheduler. Administrators can adjust:

1. Job Frequency: How often the locking job runs (recommended: every 1-5 minutes)
2. Window Size: How far in advance the job checks for assignments about to open (default: 5 minutes)

## Recommendations for Instructors

1. Create and configure your group assignments in advance
2. Set an appropriate open date
3. Post the assignment when ready - group memberships can still be modified until the open date
4. If you need to extend the open date, you can do so as long as no submissions have been made

## Notes

- The locking behavior applies only to assignments configured for group submission
- Assignments set to site or individual access do not affect group locks
- If a group is locked by multiple assignments, all locks must be removed before the group can be modified

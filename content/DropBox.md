# Drop Box

## Authorization

### Kernel / ContentHostingService / WebDAV -\> DropboxAuthzHandler Behaviour

#### Drop Box entities

Drop Box entity references have the following format:<br/>
/group-user/siteId/drop box owner's userId/...<br/>
where the drop box owner is typically a student. So when you read 'owner', think 'student'.

#### Maintain permissions

The "maintain" permissions act as prerequisites to unlock permissions on drop box content, preventing users from interacting with content in drop boxes they shouldn't have access to. Typically, an instructor can maintain the entire drop box, a student can maintain their own dropbox, a TA can maintain drop boxes of students in their groups. Specifically, a user can maintain a drop box entity if they have:

-   dropbox.maintain in its site, or
-   dropbox.maintain.own.groups in its site and they share a group with the entity's drop box owner, or
-   dropbox.own in its site, and they are the entity's drop box owner

Note that some entities don't exist inside a user's drop box, for example:<br/>
/group-user/siteId/someFile.txt<br/>
/group-user/siteId/some folder/someFile.txt<br/>
Only users with dropbox.maintain can maintain these.

#### Authorization granted from drop box maintain permissions

Authorization to maintain a drop box entity is a prerequisite before a user can unlock any permission.
If the user can maintain an entity, then they can unlock the following permissions under the following conditions: <br/>

| Permission | Entity type | Condition | Prohibited |
| --- | --- | --- | --- |
| content.read | Any | Always | |
| content.new | Any | Always | |
| content.write.any / dropbox.write.any | Any | The user has the realm permission | |
| content.write.own / dropbox.write.own | Any | The user has the realm permission, and is the file's creator | |
| content.delete.any / dropbox.delete.any | Resource | The user has the realm permission | |
| content.delete.any / dropbox.delete.any | Collection | The user has the realm permission, and can unlock a dropbox.write permission on the entity | Entity is a user's drop box itself. I.e.  /group-user/siteId/userId and the userId exists |
| content.delete.own / dropbox.delete.own | Resource | The user has the realm permission, and is the file's creator | |
| content.delete.own / dropbox.delete.own | Collection | The user has the realm permission, is the file's creator, and can unlock a dropbox.write permission on the entity | Entity is a user's drop box itself. I.e.  /group-user/siteId/userId and the userId exists |

Notice that SAK-46030 introduces four new permissions: dropbox.write.own / dropbox.write.any / dropbox.delete.own / dropbox.delete.any. Drop Box's authorization is now self-contained, and is unaffected by permissions intended for the Resources tool. Notice that when attempting to unlock a content.write.\* / content.delete.\* permission on a dropbox entity, the dropbox counterpart is unlocked instead. 

#### Site Drop Box authorization

The drop box container for the entire site has an entityId as follows: <br/>
/group-user/siteId

On the site drop box, the following permissions can be unlocked under the following conditions: <br/>

| Permission | Condition |
| --- | --- |
| content.read | User has dropbox.maintain, dropbox.maintain.own.groups, or dropbox.own |
| content.new | User has dropbox.maintain |
| content.write.any / dropbox.write.any | The user has the realm permission |
| content.write.own / dropbox.write.own | The user has the realm permission, and is the file's creator |
| content.delete.any / dropbox.delete.any | Never |
| content.delete.own / dropbox.delete.own | Never |

#### Further notes on realm permissions

Admins are authorized to unlock any permission on any drop box entity, including dropbox.delete.\* on a site drop box, or a user's drop box.

All of the above describes the behaviour of server-side checks and webdav. On the subject of webdav, because this is the cause of much confusion: <br/>
If you rename a file, it unlocks content.new, creates a new file, then tries to unlock content.delete.any / content.delete.own, and deletes the original.

So, if the user can unlock content.new, and not dropbox.delete.\* (E.g. when testing deletion on the site drop box, or a user's drop box), typically the client will show the new file and not the original, which looks like the operation succeeded. But if you refresh (e.g. by entering a folder, then backing out), you'll see that the new folder was created, the contents were copied over, but the original was not deleted, and still contains the contents, so the deletion actually failed, which is good.

* * * * *

### Resources / Drop Box Tool -\> DropboxAuthz Behaviour

#### Content Permissions

In the resources / drop box (both tools are in the same project: content), every entity has an "Actions" dropdown.<br/>
For each entity, the Actions dropdown is populated with actions that the user is authorized to perform based on whether they have the appropriate ContentPermissions - an enum with values: CREATE, DELETE, READ, REVISE, SITE\_UPDATE.

These aren't necessarily 1-to-1 with realm permissions - one ContentPermission can reveal many actions, one action can unlock many realm permissions.

It would be good to create a table documenting which ContentPermissions authorize with actions, and which locks are unlocked by each action, but this is no small feat. To find the ContentPermissions that authorize each action, see ResourcesAction.java line ~876 (a large static block before the constructor). To determine the locks unlocked for each action may require testing each action while debugging the security service.

When modifying the content project for SAK-46030, we defined 3 levels:

-   Level 1: the site drop box: /group-user/siteId
-   Level 2: the user drop box, or a sibling: /group-user/siteId/token
-   Level 3: Any content lower than level 2

#### Level 1

The following table lists the conditions required to authorize each ContentPermissions value on level 1<br/>

| ContentPermissions | Condition |
| --- | --- |
| CREATE | User has dropbox.maintain |
| READ | User has dropbox.maintain |
| REVISE | User has dropbox.maintain |
| DELETE | Never |
| SITE\_UPDATE | User has site.upd |

#### Level 2

The following table lists the conditions required to authorize each ContentPermissions value on level 2<br/>

| ContentPermissions | Condition |
| --- | --- |
| CREATE | User is authorized to maintain the entity |
| READ | User is authorized to maintain the entity |
| REVISE | <ul><li>User has dropbox.maintain and</li><ul><li>has dropbox.write.any, or</li><li>has dropbox.write.own and is the file creator</li></ul></ul> |
| DELETE | <ul> <li>User has dropbox.maintain and</li> <ul> <li>it is not a user's drop box and</li> <ul> <li>user has dropbox.delete.any or</li> <li>user has dropbox.delete.own and is the file creator</li> </ul> <li>if it's a collection, the user must also meet the criteria for REVISE</li> </ul> </ul> |
| SITE\_UPDATE | User has site.upd |

#### Level 3

The following table lists the conditions required to authorize each ContentPermissions value on level 3<br/>

| ContentPermissions | Condition |
| ----------------- | -------- |
| CREATE | User is authorized to maintain the entity |
| READ | User is authorized to maintain the entity |
| REVISE | <ul> <li>User is authorized to maintain the entity, and</li> <ul> <li>has dropbox.write.any, or</li> <li>has dropbox.write.own and is the file creator</li> </ul> </ul> |
|  DELETE | <ul> <li>User is authorized to maintain the entity, and </li> <ul> <li>has dropbox.delete.any or </li> <li>has dropbox.delete.own and is the file creator </li> </ul> <li>if it's a collection, the user must also meet the criteria for REVISE</li> </ul>|
| SITE\_UPDATE | User has site.upd |


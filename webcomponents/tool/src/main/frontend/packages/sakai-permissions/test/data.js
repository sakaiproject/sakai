export const siteId = "xyz";

export const i18nUrl = /.*i18n.*resourcebundle=permissions-wc.*/;

export const i18n = `
per.alrgra=already granted
per.lis.head=Permission
per.lis.head.title=Toggle all permissions for all roles
per.lis.role.title=Toggle all permissions for this role
per.lis.perm.title=Toggle all roles for this permission
per.lis.title=Permissions
per.lis=List of roles and permissions that can be applied to this folder. Layout: each row lists the permissions of a role. Layout: column 1 lists the roles, the other columns list the permissions, checkboxes permit enabling a permission for a role.
per.rol=Role
per.alert=Alert: no roles are defined.
per.lis.clearall=Clear all
per.lis.restoredef=Undo changes
per.lis.selectgrp=Set permissions for
gen.can=Cancel
gen.sav=Save
`;

export const toolI18nUrl = /.*i18n.*resourcebundle=tool.*/;
export const toolI18n = `
perm-tool.create=Create
perm-tool.delete=Delete
perm-tool.read=Read
perm-tool.update=Update
`;
export const permsUrl = /.*permissions.*tool\.json.*/;
export const perms = {
  available: [ "tool.read", "tool.create", "tool.delete" ],
  on: {
    "maintain": [ "tool.read", "tool.create", "tool.delete" ],
    "access": [ "tool.read" ],
  },
  roleNameMappings: { maintain: "Maintain", access: "Access" }
};

Sakai's Support for IMS Standards
---------------------------------

This folder holds the Sakai code for IMS Learning Tools Interoperability and other standards.
This area has many different contributors.  

There are several internal documents that you might find useful ranging from test plans to developer/configuration
documentation.  These documents are stored here in github:

* [Sakai LTI Documentation](https://github.com/sakaiproject/sakai/tree/master/basiclti/basiclti-docs/resources/docs)

Sakai has a unit test that we keep up-to-date with the latest LTI specifications.  The 
test harness functions as both Consumer and Provider and exercises both standard services 
and Sakai's particular extensions.  I make this tool available online at

* https://online.dr-chuck.com/sakai-api-test

Aditional Documentation

* [Configuring LTI 2.0](docs/LTI2.md)
* [Using Sakai with CASA](docs/CASA.md)
* [Using LTI Provider ContentItem](docs/CONTENTITEM.md)

# ######################################################
# SAK-30601 - Upgraded BasicLTI dashboard
# ######################################################
New Sakai properties added:
--------------------------------------------------------------------------------
* basiclti.tool.site.attribution.key and basiclti.tool.site.attribution.name : Used by the new dynamic column in the "site tools" table.
  - basiclti.tool.site.attribution.key : Defines which site property we will show
  - basiclti.tool.site.attribution.name : Title for the column header. This property can be a text or a translation key.
  - If basiclti.tool.site.attribution.name is not defined or is blank, the column will not be displayed.
  - Example :
    > basiclti.tool.site.attribution.key=Department
    > basiclti.tool.site.attribution.name=content.attribution


New actions supported :
--------------------------------------------------------------------------------
BASE_URL/portal/site/SITE_ID/tool/TOOL_ID?panel=ToolSite&sakai_action=doSort&criteria=COLUMN_ID
BASE_URL/portal/site/SITE_ID/tool/TOOL_ID?panel=ToolSite&sakai_action=doChangePageSize&pagesize=[10,50,100,200]
BASE_URL/portal/site/SITE_ID/tool/TOOL_ID?panel=ToolSite&sakai_action=doChangePage&page_event=[next,prev,last,first]
#doChangePage also supports the 'pagesize' parameter
BASE_URL/portal/site/SITE_ID/tool/TOOL_ID?panel=ToolSite&sakai_action=doChangePage&page_event=[next,prev,last,first]&pagesize=[10,50,100,200]
BASE_URL/portal/site/SITE_ID/tool/TOOL_ID?panel=ToolSite&sakai_action=doSearch&field=COLUMN_ID&search=SEARCH_VALUE

New URLs used in export service :
--------------------------------------------------------------------------------
- CSV : BASE_URL/access/basiclti/site/SITE_ID/export:CSV
- Excel : BASE_URL/access/basiclti/site/SITE_ID/export:EXCEL

You can also specify a filter with a TOOL_ID
- CSV : BASE_URL/access/basiclti/site/SITE_ID/export:CSV:TOOL_ID
- Excel : BASE_URL/access/basiclti/site/SITE_ID/export:EXCEL:TOOL_ID

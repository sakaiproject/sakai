Sakai's Support for IMS Standards
---------------------------------

This folder holds the Sakai code for IMS Learning Tools Interoperability and other standards.
This area has many different contributors.

There are several internal documents that you might find useful ranging from
 test plans to developer/configuration
documentation.  These documents are stored here in github:

Aditional Documentation

* [Using Tsugi in Sakai with LTI Advantage](https://www.tsugi.org/md/ADVANTAGE.md)
* [Using LTI ContentItem](docs/CONTENTITEM.md)
* [Using the IMS Reference Implementation with Sakai](docs/IMS_RI.md)
* [Setting up Tsugi in Sakai for Testing](docs/TSUGI.md)
* [Windows PostMessage Support in Sakai](docs/POSTMESSAGE.md)
* [Sakai API Documentation Including API Extensions](docs/sakai_basiclti_api.md)
Sakai has API extensions for Membership/Roster, Learning Object Repository Integration, and a Settings service.
* [Configuring the Sakai External Tools Portlet](docs/sakai_basiclti_portlet.md)
It is possible to make multiple pre-configured placements of the LTI Tool in a way that they are placeable as tools.
* [Configuring the Sakai LTI Provider](docs/sakai_basiclti_provider)
It is possible to use Sakai tools as LTI tools that can be used in an LTI Consumer like a portal or other LMS.
* [Documentation for Vendors of Sakai Tools](docs/sakai_basiclti_vendor.md)
This is CC0 licensed documenation that can be used to quickly develop vendor documnation showing how to configure a vendor's LTI
tool in Sakai.

Sakai QA Tests are available at:

* [Sakai LTI Documentation](https://github.com/sakaiproject/sakai/tree/master/basiclti/basiclti-docs/resources/docs)

Sakai has an LTI 1.1 unit test that we keep up-to-date with the latest LTI specifications.  The
test harness functions as both Consumer and Provider and exercises both standard services
and Sakai's particular extensions.  I make this tool available online at

* https://www.tsugi.org/lti-test

Internal Documentation
----------------------

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

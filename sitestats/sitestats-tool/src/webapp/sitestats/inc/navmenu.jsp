<%--

    $URL:$
    $Id:$

    Copyright (c) 2006-2009 The Sakai Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

                http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<sakai:tool_bar>
                <sakai:tool_bar_item
                        action="#{MenuBean.processSiteList}"
                        value="#{msgs.menu_sitelist}"
                        disabled="#{viewName eq 'SiteListBean'}"
                        rendered="#{ServiceBean.adminView}" />
                <sakai:tool_bar_item
                        value=" | "
                        disabled="true"
                        rendered="#{ServiceBean.adminView && ServiceBean.serverWideStatsEnabled}" />
                <sakai:tool_bar_item
                        action="#{MenuBean.processServerWide}"
                        value="#{msgs.menu_serverwide}"
                        disabled="#{viewName eq 'ServerWideReportBean'}"
                        rendered="#{ServiceBean.adminView && ServiceBean.serverWideStatsEnabled}" />
                <sakai:tool_bar_item
                        value=" | "
                        disabled="true"
                        rendered="#{viewName ne 'SiteListBean' && ServiceBean.adminView && viewName ne 'ServerWideReportBean'}" />
                <sakai:tool_bar_item
                        action="#{MenuBean.processOverview}"
                        disabled="#{viewName eq 'OverviewBean'}"
                        value="#{msgs.menu_overview}"
                        rendered="#{viewName ne 'SiteListBean' && viewName ne 'ServerWideReportBean' && (ServiceBean.enableSiteVisits || ServiceBean.enableSiteActivity)}" />
                <sakai:tool_bar_item
                        value=" | "
                        disabled="true"
                        rendered="#{viewName ne 'SiteListBean' && viewName ne 'ServerWideReportBean' && (ServiceBean.enableSiteVisits || ServiceBean.enableSiteActivity)}" />
                <sakai:tool_bar_item
                        action="#{MenuBean.processReports}"
                        disabled="#{viewName eq 'ReportsBean'}"
                        value="#{msgs.menu_reports}"
                        rendered="#{viewName ne 'SiteListBean' && viewName ne 'ServerWideReportBean'}" />
                <sakai:tool_bar_item
                        value=" | "
                        disabled="true"
                        rendered="#{viewName ne 'SiteListBean' && viewName ne 'ServerWideReportBean'}" />
                <sakai:tool_bar_item
                        action="#{MenuBean.processPrefs}"
                        disabled="#{viewName eq 'PrefsBean'}"
                        value="#{msgs.menu_prefs}"
                        rendered="#{viewName ne 'SiteListBean' && viewName ne 'ServerWideReportBean'}" />
</sakai:tool_bar>

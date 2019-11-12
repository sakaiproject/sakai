<%--
***********************************************************************************
*
* Copyright (c) 2019 Apereo Foundation

* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*             http://opensource.org/licenses/ecl2

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
--%>
<%-- Application Extensions --%>
<h:panelGroup rendered="#{attach.mimeType == 'application/mac-binhex40'}" styleClass="fa fa-file-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/mathematica'}" styleClass="fa fa-file-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/postscript'}" styleClass="fa fa-file-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/x-stuffit'}" styleClass="fa fa-file-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/octet-stream'}" styleClass="fa fa-file-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/pdf'}" styleClass="fa fa-file-pdf-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/msword'}" styleClass="fa fa-file-word-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/vnd.ms-excel'}" styleClass="fa fa-file-excel-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/vnd.ms-powerpoint'}" styleClass="fa fa-file-powerpoint-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/xhtml+xml'}" styleClass="fa fa-file-code-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/xml'}" styleClass="fa fa-file-code-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/xml-dtd'}" styleClass="fa fa-file-code-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'application/zip'}" styleClass="fa fa-file-archive-o"></h:panelGroup>
<%-- Audio Extensions --%>
<h:panelGroup rendered="#{attach.mimeType == 'audio/basic'}" styleClass="fa fa-file-audio-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'audio/mpeg'}" styleClass="fa fa-file-audio-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'audio/x-wav'}" styleClass="fa fa-file-audio-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'audio/x-mpegurl'}" styleClass="fa fa-file-audio-o"></h:panelGroup>
<%-- Image Extensions --%>
<h:panelGroup rendered="#{attach.mimeType == 'image/vnd.djvu'}" styleClass="fa fa-file-image-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'image/bmp'}" styleClass="fa fa-file-image-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'image/gif'}" styleClass="fa fa-file-image-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'image/jpeg'}" styleClass="fa fa-file-image-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'image/tiff'}" styleClass="fa fa-file-image-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'image/png'}" styleClass="fa fa-file-image-o"></h:panelGroup>
<%-- Text Extensions --%>
<h:panelGroup rendered="#{attach.mimeType == 'text/html'}" styleClass="fa fa-file-code-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'text/url'}" styleClass="fa fa-file-code-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'text/plain'}" styleClass="fa fa-file-text-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'text/xml'}" styleClass="fa fa-file-text-o"></h:panelGroup>
<%-- Video Extensions --%>
<h:panelGroup rendered="#{attach.mimeType == 'video/mpeg'}" styleClass="fa fa-file-video-o"></h:panelGroup>
<h:panelGroup rendered="#{attach.mimeType == 'video/quicktime'}" styleClass="fa fa-file-video-o"></h:panelGroup>


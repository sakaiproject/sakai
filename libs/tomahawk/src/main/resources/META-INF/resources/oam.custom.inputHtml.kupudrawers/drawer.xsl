<?xml version="1.0" encoding="utf-8" ?>
<!--
##############################################################################
#
# Copyright (c) 2003-2005 Kupu Contributors. All rights reserved.
#
# This software is distributed under the terms of the Kupu
# License. See LICENSE.txt for license text. For a list of Kupu
# Contributors see CREDITS.txt.
#
##############################################################################

XSL transformation from Kupu Library XML to HTML for the library drawers.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:tal="http://xml.zope.org/namespaces/tal" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:i18n="http://xml.zope.org/namespaces/i18n" i18n:domain="kupu">
    <tal:block define="x python:request.RESPONSE.setHeader('Content-Type', 'text/xml;;charset=UTF-8')" />
    <xsl:param name="drawertype">
        <xsl:variable name="v" select="/libraries/param[@name='drawertype']"></xsl:variable>
        <xsl:choose>
            <xsl:when test="count($v)"><xsl:value-of select="$v"/></xsl:when>
            <xsl:otherwise>image</xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="drawertitle">
        <xsl:variable name="v" select="/libraries/param[@name='drawertitle']"></xsl:variable>
        <xsl:choose>
          <xsl:when test="count($v)"><xsl:value-of select="$v"/></xsl:when>
          <xsl:otherwise i18n:translate="default_drawer_title">Drawer</xsl:otherwise>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="showupload">
      <!-- does this default value actually make any sense? -->
      <xsl:variable name="v" select="/libraries/param[@name='showupload']"></xsl:variable>
      <xsl:choose>
        <xsl:when test="count($v)"><xsl:value-of select="$v"/></xsl:when>
      </xsl:choose>
    </xsl:param>
    <xsl:param name="usecaptions">
        <xsl:variable name="v" select="/libraries/param[@name='usecaptions']"></xsl:variable>
        <xsl:choose>
            <xsl:when test="count($v)"><xsl:value-of select="$v"/></xsl:when>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="multiple"><xsl:variable name="v" select="/libraries/param[@name='multiple']"></xsl:variable>
        <xsl:choose>
            <xsl:when test="count($v)"><xsl:value-of select="$v"/></xsl:when>
        </xsl:choose>
    </xsl:param>
    <xsl:param name="showanchors">yes</xsl:param>
    <xsl:param name="image-align">inline</xsl:param>
    <xsl:param name="image-caption">true</xsl:param>
    <xsl:param name="image-class"></xsl:param>
    <xsl:param name="link_target"></xsl:param>
    <xsl:param name="link_name"></xsl:param>
    <xsl:param name="ie"></xsl:param>
    <xsl:variable name="titlelength" select="255"/>
    <xsl:variable name="i18n_drawertitle">
        <xsl:choose>
            <xsl:when i18n:translate="imagedrawer_title" test="$drawertype='image'">Insert Image</xsl:when>
            <xsl:when i18n:translate="linkdrawer_title"
                test="$drawertype='link'">Insert Link</xsl:when>
            <xsl:otherwise><xsl:value-of select="$drawertitle"/></xsl:otherwise>
        </xsl:choose>
    </xsl:variable>
    <xsl:template name="drawer-title" i18n:translate="default_drawer_title">Drawer</xsl:template>
    <xsl:output indent="yes" method="xml" />
    <xsl:preserve-space elements="form div strong br input textarea"/>
    <xsl:template match="/"><!-- root is always 'libraries'? -->
        <html lang="en" xml:lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:i18n="http://xml.zope.org/namespaces/i18n" i18n:domain="kupu" >
            <head>
                <title>
                    <xsl:value-of select="$i18n_drawertitle"/>
                </title>
                <xsl:call-template name="mystyle" />
            </head>
            <body>
                <div class="kupu-drawer">
                    <div id="kupu-librarydrawer">
                        <div id="kupu-searchbox">
                            <!--<form onsubmit="return false;">-->
                                <xsl:variable name="search_value" i18n:translate="kupudrawer_search"
                                    >Search</xsl:variable>
                                <input id="kupu-searchbox-input"
                                    class="kupu-searchbox-input nofocus">
                                  <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                                    <xsl:attribute name="onkeyup">if(event.keyCode == 13){drawertool.current_drawer.search();return false;}</xsl:attribute>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="$search_value"/>
                                    </xsl:attribute>
                                    <xsl:attribute name="onclick">
                                        if (this.value == '<xsl:value-of
                                            select="$search_value"/>') this.value = ''; this.style.fontStyle='normal';</xsl:attribute>
                                </input>
                            <!--</form>-->
                        </div>
                        <h1 class="kupu-drawer-title">
                            <xsl:value-of select="$i18n_drawertitle"/>
                        </h1>
                        <div id="kupu-breadcrumbs">
                          <xsl:if test="//*[@selected]/breadcrumbs">
                              <xsl:apply-templates select="//*[@selected]/breadcrumbs"/>
                          </xsl:if>
                        </div>
                        <div class="kupu-panels">
                          <table>
                                <tr class="kupu-panelsrow">
                                    <td id="kupu-librariespanel" class="panel">
                                        <div id="kupu-librariesitems" class="overflow">
                                            <xsl:apply-templates select="/libraries/library"/>
                                        </div>
                                    </td>
                                    <td id="kupu-resourcespanel" class="panel">
                                       <xsl:if test="/libraries/*[@selected]//uploadbutton">
                                          <xsl:attribute name="class">panel upload-allowed</xsl:attribute>
                                       </xsl:if>
                                        <div id="kupu-resourceitems" class="overflow">
                                            <xsl:apply-templates
                                                select="/libraries/*[@selected]/items"/>
                                        </div>
                                        <xsl:apply-templates select="/libraries/*[@selected]//uploadbutton"
                                                                mode="currentpanel"/>
                                    </td>
                                    <td id="kupu-propertiespanel" class="panel">
                                        <div id="kupu-properties" class="overflow">
                                          <xsl:apply-templates select="." mode="panel" />
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div class="kupu-dialogbuttons">
                            <button type="button"
                                    class="kupu-dialog-button"
                                    i18n:translate="button_ok"
                                onclick="drawertool.current_drawer.save();">Ok</button>
                            <button type="button"
                                    class="kupu-dialog-button"
                                    i18n:translate="button_cancel"
                                onclick="drawertool.closeDrawer();">Cancel</button>
                            <button type="button"
                                    class="kupu-dialog-button"
                                    i18n:translate="button_reload"
                                onclick="drawertool.current_drawer.reloadCurrent();">Reload</button>
                        </div>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- The panel template can be overridden, if something else must happen on the right panel of the drawer. -->
    <xsl:template match="*" mode="panel">
      <xsl:choose>
        <xsl:when test="$drawertype='image'">
          <xsl:if test="//resource[@selected]">
            <xsl:apply-templates
                select="/libraries/*[@selected]//resource[@selected]"
                mode="image-properties"/>
          </xsl:if>
          <!-- use image upload template -->
          <xsl:if test="$showupload='yes'">
            <xsl:apply-templates
                select="/libraries/*[@selected]//uploadbutton"
                mode="image-upload"/>
          </xsl:if>
        </xsl:when>
        <xsl:when test="$drawertype='link'">
          <xsl:apply-templates
              select="/libraries/*[@selected]//resource[@selected]"
              mode="link-properties"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates
              select="/libraries/*[@selected]//resource[@selected]"
              mode="properties"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template name="mystyle" />

    <xsl:template match="library">
        <div onclick="drawertool.current_drawer.selectLibrary('{@id}');" class="kupu-libsource"
            title="{title}" style="">
            <xsl:attribute name="id">
                <xsl:value-of select="@id"/>
            </xsl:attribute>
            <xsl:if test="@selected=1">
               <xsl:attribute name="class">kupu-libsource-selected</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="icon"/>
            <span class="drawer-item-title">
               <xsl:call-template name="lf2br">
                  <xsl:with-param name="StringToTransform" select="title/text()"/>
               </xsl:call-template>
            </span>
        </div>
    </xsl:template>
    <xsl:template match="items">
        <xsl:apply-templates select="collection|resource" mode="currentpanel"/>
    </xsl:template>
    <xsl:template match="resource|collection" mode="currentpanel">
        <div id="{@id}" title="{description}">
           <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
              kupu-<xsl:value-of select="local-name()"/>
              <xsl:if test="@selected">
                 selected-item
              </xsl:if>
           </xsl:attribute>
            <xsl:attribute name="onclick">
                <xsl:choose>
                    <xsl:when test="local-name()='collection'">drawertool.current_drawer.selectCollection(this);</xsl:when>
                    <xsl:otherwise>drawertool.current_drawer.selectItem(this, event)</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <span>
               <input name="drawer-item-radio">
                  <xsl:attribute name="type">
                     <xsl:choose>
                        <xsl:when test="$multiple='yes'">checkbox</xsl:when>
                        <xsl:otherwise>radio</xsl:otherwise>
                     </xsl:choose>
                  </xsl:attribute>
                  <xsl:if test="@checked">
                     <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="not(local-name()='resource')">
                     <xsl:attribute name="style">visibility:hidden;</xsl:attribute>
                  </xsl:if>
               </input>
               <xsl:apply-templates select="icon"/>
               <xsl:apply-templates select="(label|title)[1]"/>
            </span>
        </div>
    </xsl:template>

    <xsl:template match="uploadbutton" mode="currentpanel">
        <button class="kupu-dialog-button kupu-upload">
            <xsl:attribute name="onclick"> drawertool.current_drawer.selectUpload(); </xsl:attribute>
            <span tal:omit-tag="" i18n:translate="imagedrawer_upload_link">Upload&#xa0;image&#xa0;here...</span>
        </button>
    </xsl:template>

    <xsl:template match="icon">
        <img src="{.}" alt="{../title}">
            <xsl:attribute name="class">library-icon-<xsl:value-of select="local-name(..)"/>
            </xsl:attribute>
        </img>
    </xsl:template>
    <xsl:template match="label|title">
        <span class="drawer-item-title">
            <xsl:choose>
                <xsl:when test="string-length() &gt; $titlelength">
                    <xsl:value-of select="substring(., 0, $titlelength)"/>... </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
            </span>
    </xsl:template>
    <!-- resource template with no mode can be used to generate a quick preview -->
    <xsl:template match="resource">
      <div>
        <h1 class="kupu-title-row">
            <xsl:value-of select="title"/>
        </h1>
        <div class="kupu-preview-row">
          <xsl:apply-templates select="." mode="image-view" />
        </div>
        <xsl:if test="description != ''">
            <div class="kupu-description-row documentDescription">
                <p><xsl:copy-of
                select="description/*|description/text()"/></p>
            </div>
        </xsl:if>
        <div style="clear:both;line-height:1px;">&#xA0;</div>
      </div>
    </xsl:template>
    <xsl:template match="resource|collection" mode="base-properties">
        <h1 class="kupu-title-row">
            <xsl:value-of select="title"/>
        </h1>
        <div class="kupu-preview-row">
            <xsl:apply-templates select="status"/>
            <xsl:apply-templates select="." mode="image-view" />
            <div class="preview-size">
                <xsl:if test="width" i18n:translate="imagedrawer_size"> (<span i18n:name="width">
                <xsl:value-of select="width"/>
                    </span> by <span i18n:name="height">
                        <xsl:value-of select="height"/>
                    </span>)</xsl:if>
            </div>
        </div>
        <xsl:if test="description != ''">
            <div class="kupu-description-row documentDescription">
               <p><xsl:copy-of
               select="description/*|description/text()"/></p>
            </div>
        </xsl:if>
        <div style="clear:both;"/>
    </xsl:template>

    <!--
     This template shows one image.
     If a 'preview' tag is available then that one is used, otherwise 'uri'.
    -->
    <xsl:template match="resource|collection" mode="image-view">
       <xsl:variable name="p" select="preview"></xsl:variable>
       <xsl:choose>
          <xsl:when test="media='flash'">
             <object src="{$p}" data="{$p}" type="application/x-shockwave-flash"
                     width="100" height="100">
                <param name="movie" value="{$p}" />
             </object>
          </xsl:when>
          <xsl:when test="$p!=''">
             <img src="{$p}" title="{title}" width="{width}" height="{height}" id="kupu-preview-image">
                <xsl:choose>
                   <xsl:when test="height > width">
                      <xsl:if test="height > 128">
                         <xsl:attribute name="width"><xsl:value-of select="width*128 div height" /></xsl:attribute>
                         <xsl:attribute name="height">128</xsl:attribute>
                      </xsl:if>
                   </xsl:when>
                   <xsl:when test="width >= height">
                      <xsl:if test="width > 128">
                         <xsl:attribute name="height"><xsl:value-of select="height*128 div width" /></xsl:attribute>
                         <xsl:attribute name="width">128</xsl:attribute>
                      </xsl:if>
                   </xsl:when>
                   <xsl:otherwise>
                      <xsl:attribute name="height">1</xsl:attribute>
                      <xsl:attribute name="width">1</xsl:attribute>
                      <xsl:attribute name="onload">kupuFixImage(this);</xsl:attribute>
                   </xsl:otherwise>
                </xsl:choose>
             </img>
          </xsl:when>
          <xsl:otherwise/>
       </xsl:choose>
    </xsl:template>

    <!--
        Used in image-upload template.
    -->
    <xsl:template match="resource|collection|uploadbutton" mode="image-extra-properties">
       <div class="kupu-image-fields">
          <input id="kupu-media" type="hidden" value="{media}" />
          <input id="kupu-width" type="hidden" value="{width}" />
          <input id="kupu-height" type="hidden" value="{height}" />
          <label class="kupu-detail" i18n:translate="image_alignment">Alignment</label>
          <span class="kupu-detail">
             <input type="radio" name="image-align" id="image-align-left" value="image-left">
                <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                <xsl:if test="$image-align='left'">
                   <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
             </input>
             <label for="image-align-left" i18n:translate="imagedrawer_left">Left</label>
             <input type="radio" name="image-align" id="image-align-inline" value="image-inline">
                <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                <xsl:if test="$image-align='inline'">
                   <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
             </input>
             <label for="image-align-inline" i18n:translate="imagedrawer_inline">Inline</label>
             <input type="radio" name="image-align" id="image-align-right" value="image-right">
                <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                <xsl:if test="$image-align='right'">
                   <xsl:attribute name="checked">checked</xsl:attribute>
                </xsl:if>
             </input>
             <label for="image-align-right" i18n:translate="imagedrawer_right">Right</label>
          </span>
          <xsl:if test="$usecaptions='yes'">
             <xsl:choose>
                <xsl:when test="media='flash'" />
                <xsl:otherwise>
                   <label class="kupu-detail"
                          for="image-caption" i18n:translate="imagedrawer_caption_label">Caption</label>
                   <span class="kupu-detail">
                      <input type="checkbox" name="image-caption" id="image-caption">
                         <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                         <xsl:if test="$image-caption='true'">
                            <xsl:attribute name="checked">checked</xsl:attribute>
                         </xsl:if>
                         <xsl:attribute name="onclick">toggleAltFieldVisibility(this);</xsl:attribute>
                      </input>
                   </span>
                </xsl:otherwise>
             </xsl:choose>
          </xsl:if>
          <xsl:if test="sizes">
             <label class="kupu-detail"
                    for="image-size-selector"><span
                    i18n:translate="imagedrawer_size_2">Size</span>
                <span class="image-dimensions" i18n:translate="imagedrawer_size">
                   (<span i18n:name="width"><xsl:value-of select="width"/></span>
                   by
                   <span i18n:name="height"><xsl:value-of select="height"/></span>)
                </span>
             </label>
             <span class="kupu-detail">
                <select name="image-size-selector">
                   <xsl:apply-templates select="sizes/size" />
                </select>
             </span>
          </xsl:if>
          <xsl:if test="class">
             <label class="kupu-detail" i18n:translate="imagedrawer_style"
                    for="kupu-image-class-selector">Style</label>
             <span class="kupu-detail">
                <select name="kupu-image-class-selector" id="kupu-image-class">
                   <xsl:apply-templates select="class"/>
                </select>
             </span>
          </xsl:if>
          <xsl:apply-templates select="." mode="image-extra-properties-alt" />
       </div>
    </xsl:template>

    <!--
        Used in image-extra-properties. Produces a form entry for the 'alt' attribute.
    -->
    <xsl:template match="resource|collection|uploadbutton" mode="image-extra-properties-alt">
      <label class="kupu-detail" id="image-alt-label"
             for="image-alt">
        <xsl:if test="$usecaptions='yes' and $image-caption='true'">
          <xsl:attribute name="style">display:none;</xsl:attribute>
        </xsl:if>
        <span tal:omit-tag="" i18n:translate="imagedrawer_upload_alt_text">Text equivalent</span>
      </label>
      <textarea class="kupu-detail" type="text" id="image-alt" rows="4">
        <xsl:if test="$usecaptions='yes' and $image-caption='true'">
          <xsl:attribute name="style">display:none;</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="title"/>
      </textarea>
    </xsl:template>


    <xsl:template match="resource|collection" mode="image-properties">
       <xsl:apply-templates select="." mode="base-properties"/>
       <xsl:apply-templates select="." mode="image-extra-properties"/>
    </xsl:template>

    <xsl:template match="class">
       <option value="{@name}">
          <xsl:if test="$image-class=@name">
             <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="@title" /></option>
    </xsl:template>

    <xsl:template match="size">
       <option value="{uri}">
          <xsl:if test="selected">
             <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="label" />
       </option>
    </xsl:template>

    <xsl:template match="resource|collection" mode="link-properties">
        <xsl:apply-templates select="." mode="base-properties"/>

        <!-- <form onsubmit="return false;"> -->
        <div class="kupu-linkdrawer-name-row">
                <div><label i18n:translate="linkdrawer_name_label">Name</label></div>
                <input type="text" id="link_name" size="10">
                   <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                   <xsl:attribute name="value"><xsl:value-of select="$link_name"/></xsl:attribute>
                </input>
            </div>
            <div class="kupu-linkdrawer-target-row">
                <div><label i18n:translate="linkdrawer_target_label">Target</label></div>
                <input type="text" id="link_target" size="10">
                   <xsl:attribute name="onkeypress">if(event.keyCode==13)return false;</xsl:attribute>
                   <xsl:attribute name="value"><xsl:value-of select="$link_target"/></xsl:attribute>
                </input>
            </div>
            <xsl:if test="$showanchors='yes'">
                <xsl:apply-templates select="anchor"></xsl:apply-templates>
            </xsl:if>
        <!-- </form> -->
    </xsl:template>

    <xsl:template match="resource|collection" mode="properties">
        <xsl:apply-templates select="." mode="base-properties"/>
    </xsl:template>

    <xsl:template match="anchor">
       <div class="kupu-linkdrawer-anchors">
          <input type="hidden" value="{../uri}"/>
          <label for="kupu-anchor-select" i18n:translate="linkdrawer_anchor">Link to anchor</label>
          <xsl:if test="$ie='true'">
             <input type="checkbox">
                <xsl:attribute name="onclick">drawertool.current_drawer.initAnchors();</xsl:attribute>
             </input>
          </xsl:if>
          <select onmousedown="drawertool.current_drawer.initAnchors();">
             <xsl:if test="$ie='true'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
             </xsl:if>
             <option i18n:translate="top_of_page" value="">top of page (default)</option>
             <option i18n:translate="fetching_anchors" value=""><em>...fetching anchors...</em></option>
          </select>
       </div>
    </xsl:template>

    <xsl:template match="status">
        <div>
            <xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute>
            <xsl:value-of select="text()"/>
        </div>
    </xsl:template>

    <xsl:template match="uploadfield">
      <label>
        <span><xsl:value-of select="name" /></span>
        <xsl:copy-of select="input/*" />
      </label>
    </xsl:template>


    <!--
        Used in the 'image-upload' template.
        Produces all non-file input entries (i.e. in this default version 'title' and 'description').

        Can be overriden if you need other fields.
    -->
    <xsl:template match="uploadbutton" mode="upload-fields">
      <xsl:choose>
        <xsl:when test="../uploadfield">
          <xsl:apply-templates select="../uploadfield" />
        </xsl:when>
        <xsl:otherwise>
          <label>
            <span tal:omit-tag="" i18n:translate="imagedrawer_upload_title_label">Title</span>
            <input id="kupu-upload-title" type="text" name="node_prop_title" size="23" value=""/>
          </label>
          <label>
            <span tal:omit-tag=""
                  i18n:translate="imagedrawer_upload_desc_label">Description</span>
            <textarea rows="5" cols="40" name="node_prop_desc">&#160;</textarea>
          </label>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!--
        Used in the 'image-upload' template.
        Shows a message about where the image is uploaded to, and creates a file input entry.
    -->
    <xsl:template match="uploadbutton" mode="upload-to">
      <label>
        <tal:block i18n:translate="imagedrawer_upload_to_label">Upload to: <span tal:omit-tag="" i18n:name="folder"> <xsl:value-of select="/libraries/*[@selected]/title"/> </span> </tal:block>
        <input id="kupu-upload-file" type="file" name="node_prop_image" size="20"/>
      </label>
    </xsl:template>


    <!--
         Image upload form
    -->
    <xsl:template match="uploadbutton" mode="image-upload">
            <div id="kupu-upload-instructions" i18n:translate="imagedrawer_upload_instructions">
                Select an image from your computer and click ok to have it automatically uploaded to
                selected folder and inserted into the editor. </div>
            <form name="kupu_upload_form" id="kupu_upload_form" method="POST"
                scrolling="off"
                target="kupu_upload_form_target" enctype="multipart/form-data" style="margin: 0;
                border: 0;">
              <xsl:attribute name="action"><xsl:value-of select="uri"/></xsl:attribute>
              <xsl:apply-templates select="." mode="upload-to" />
              <xsl:apply-templates select="." mode="upload-fields" />
            </form>
            <iframe id="kupu_upload_form_target" name="kupu_upload_form_target" src="javascript:''"
                scrolling="off" frameborder="0" width="0px" height="0px" display="None">&#160;</iframe>
            <xsl:apply-templates select="." mode="image-extra-properties"/>
    </xsl:template>

    <xsl:template match="breadcrumbs">
        <span>
          <xsl:attribute name="class">
              <xsl:value-of select="@class"/>
          </xsl:attribute>
        <xsl:apply-templates select="crumb"></xsl:apply-templates>
        </span>
    </xsl:template>
    <xsl:template match="crumb[@href]">
        <a>
            <xsl:attribute name="href">
              <xsl:value-of select="@href"/>
            </xsl:attribute>
            <xsl:attribute name="onclick">drawertool.current_drawer.selectBreadcrumb(this);return false;</xsl:attribute>
            <xsl:value-of select="node()"/>
        </a>
        <xsl:if test="not(position()=last())">
            <span class="kupu-breadcrumbSeparator"> &#8594; </span>
        </xsl:if>
    </xsl:template>
    <xsl:template match="crumb">
        <xsl:value-of select="node()"/>
    </xsl:template>
    <xsl:template name="lf2br">
       <xsl:param name="StringToTransform"/>
       <xsl:choose>
          <xsl:when test="contains($StringToTransform,'&lt;br&gt;')">
             <xsl:value-of select="substring-before($StringToTransform,'&lt;br&gt;')"/>
             <br/>
             <xsl:call-template name="lf2br">
                <xsl:with-param name="StringToTransform">
                   <xsl:value-of select="substring-after($StringToTransform,'&lt;br&gt;')"/>
                </xsl:with-param>
             </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
             <xsl:value-of select="$StringToTransform"/>
          </xsl:otherwise>
       </xsl:choose>
    </xsl:template>
</xsl:stylesheet>

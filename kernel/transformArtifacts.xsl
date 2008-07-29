<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:p="http://maven.apache.org/POM/4.0.0" version="1.0" 
  exclude-result-prefixes="p" >
  <xsl:output  indent="yes" method="xml" omit-xml-declaration="no"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="/">
     <xsl:apply-templates/>
  </xsl:template>
  <!-- a copy template that ensures proper namespace -->
  <xsl:template match="*">
    <xsl:element name="{local-name(.)}"><xsl:apply-templates /></xsl:element>
  </xsl:template>
  <xsl:template match="comment()"><xsl:copy-of select="."/></xsl:template>
  
  <xsl:template match="//p:dependencies">
    <dependencies>
    <xsl:choose>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-alias-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-authz-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-cluster-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-email-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-memory-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-site-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-servlet']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-api']" >
        <dependency>
          <groupId>org.sakaiproject.kernel</groupId>
          <artifactId>sakai-kernel-api</artifactId>
        </dependency>
      </xsl:when>
    </xsl:choose>
    
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component']" >
      <dependency>
        <groupId>org.sakaiproject.kernel</groupId>
        <artifactId>sakai-component-manager</artifactId>
      </dependency>
    </xsl:if>
    
    
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-entity-util</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-event-util</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-content-util</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-conversion']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-db-conversion</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-storage']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-db-storage</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-jcr-util</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-tool-util</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-user-util</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-log']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-util-log</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.util</groupId>
        <artifactId>sakai-util</artifactId>
      </dependency>
    </xsl:if>
      
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-integration-test']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.test</groupId>
        <artifactId>sakai-component-integration-test</artifactId>
      </dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-integration-test']" >
      <dependency>
        <groupId>org.sakaiproject.kernel.user.component</groupId>
        <artifactId>sakai-user-integration-test</artifactId>
      </dependency>
    </xsl:if>
    
    <xsl:apply-templates/>
      
    </dependencies>
    
  </xsl:template>
  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-alias-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-authz-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-cluster-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-email-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-memory-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-site-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-servlet']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-api']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template> 
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-conversion']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-storage']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-log']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-integration-test']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-integration-test']" >
    <xsl:comment><xsl:copy-of select="."/></xsl:comment>
  </xsl:template>
  
  
</xsl:stylesheet>

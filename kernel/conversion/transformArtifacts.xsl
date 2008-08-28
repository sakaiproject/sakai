<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:p="http://maven.apache.org/POM/4.0.0" version="1.0" 
 exclude-result-prefixes="p"   >
  <xsl:output  indent="yes" method="xml" omit-xml-declaration="no"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="/">
     <xsl:apply-templates/>
  </xsl:template>
 
  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>
  
  <xsl:template match="comment()"><xsl:copy-of select="."/></xsl:template>
  
  <xsl:template match="//p:dependencies">
    <p:dependencies>
    <xsl:choose>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-alias-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-authz-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-cluster-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-email-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-memory-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-site-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-servlet']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-api</p:artifactId>
        </p:dependency>
      </xsl:when>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-api']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-component-manager</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-component-manager</p:artifactId>
        </p:dependency>
      </xsl:when>
    </xsl:choose>
      
    
    <xsl:choose>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-conversion']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-storage']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-log']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-content-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-db-conversion']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-db-storage']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-entity-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-event-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-jcr-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-tool-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-user-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
      <xsl:when test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-webapp-util']" >
        <p:dependency>
          <p:groupId>org.sakaiproject.kernel</p:groupId>
          <p:artifactId>sakai-kernel-util</p:artifactId>
        </p:dependency>
      </xsl:when>
    </xsl:choose>

    <xsl:if test="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-util-log']" >
      <p:dependency>
        <p:groupId>org.sakaiproject.kernel</p:groupId>
        <p:artifactId>sakai-kernel-common</p:artifactId>
      </p:dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-integration-test']" >
      <p:dependency>
        <p:groupId>org.sakaiproject.kernel.test</p:groupId>
        <p:artifactId>sakai-component-integration-test</p:artifactId>
      </p:dependency>
    </xsl:if>
    <xsl:if test="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-integration-test']" >
      <p:dependency>
        <p:groupId>org.sakaiproject.kernel.user.component</p:groupId>
        <p:artifactId>sakai-user-integration-test</p:artifactId>
      </p:dependency>
    </xsl:if>
    
    <xsl:apply-templates/>
      
    </p:dependencies>
    
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
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-impl']" >
    <xsl:comment>
      sakai-util-impl is a part of the kernel implementation, p:dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-impl']" >
    <xsl:comment>
      sakai-util-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-alias-impl']" >
    <xsl:comment>
      sakai-alias-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-alias-impl']" >
    <xsl:comment>
      sakai-authz-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-cluster-impl']" >
    <xsl:comment>
      sakai-cluster-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-component-impl']" >
    <xsl:comment>
      sakai-component-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-lock-hbm']" >
    <xsl:comment>
      sakai-content-lock-hbm is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-impl']" >
    <xsl:comment>
      sakai-content-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-ontent-jcr-impl']" >
    <xsl:comment>
      sakai-ontent-jcr-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-jcr-migration-api']" >
    <xsl:comment>
      sakai-content-jcr-migration-api is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-content-jcr-migration-impl']" >
    <xsl:comment>
      sakai-content-jcr-migration-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-contentmulti-impl']" >
    <xsl:comment>
      sakai-contentmulti-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-ext']" >
    <xsl:comment>
      sakai-db-ext is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-db-impl']" >
    <xsl:comment>
      sakai-db-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-email-impl']" >
    <xsl:comment>
      sakai-email-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-entity-impl']" >
    <xsl:comment>
      sakai-entity-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-event-impl']" >
    <xsl:comment>
      sakai-event-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jackrabbit-impl']" >
    <xsl:comment>
      sakai-jackrabbit-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-jcr-support-impl']" >
    <xsl:comment>
      sakai-jcr-support-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-memory-impl']" >
    <xsl:comment>
      sakai-memory-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-site-impl']" >
    <xsl:comment>
      sakai-site-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-tool-impl']" >
    <xsl:comment>
      sakai-tool-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-user-impl']" >
    <xsl:comment>
      sakai-user-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject' and p:artifactId='sakai-util-impl']" >
    <xsl:comment>
      sakai-util-impl is a part of the kernel implementation, dependency removed
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>  
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-content-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-db-conversion']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-db-storage']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-entity-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-event-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-jcr-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-tool-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-user-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-webapp-util']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-util
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
  <xsl:template match="p:dependency[p:groupId='org.sakaiproject.kernel.util' and p:artifactId='sakai-util-log']" >
    <xsl:comment>
      This has been relocated to sakai-kernel-common
      <xsl:copy-of select="."/>
    </xsl:comment>
  </xsl:template>
</xsl:stylesheet>

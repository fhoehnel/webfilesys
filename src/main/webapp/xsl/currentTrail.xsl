<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
  <xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8"/>

  <xsl:template name="currentTrail">

    <div class="currentTrail">
      <xsl:if test="@unixRoot">
        <xsl:value-of select="@separator"/>
      </xsl:if>
    
      <xsl:for-each select="pathElem">
        <a class="currentTrail">
          <xsl:attribute name="href">/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="@path"/>&amp;fastPath=true</xsl:attribute>
          <xsl:attribute name="target">DirectoryPath</xsl:attribute>
          <xsl:value-of select="@name"/> 
        </a>
        <xsl:if test="not(position()=last())"><xsl:value-of select="../@separator"/></xsl:if>
      </xsl:for-each>
    
      <xsl:value-of select="@separator"/>
      <a class="currentTrail" href="javascript:void(0)"><xsl:value-of select="@mask"/></a>
      
      <a class="icon-font icon-copy icon-copyToClip" titleResource="label.copyPath">
        <xsl:attribute name="href">javascript:copyPathToClipboard('<xsl:value-of select="/fileList/relativePath" />')</xsl:attribute>
      </a>
      
    </div>
  
  </xsl:template>

</xsl:stylesheet>

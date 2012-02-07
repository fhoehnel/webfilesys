<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:template name="insDoubleBackslash">
  <xsl:param name="string" />
    <xsl:if test="contains($string, '%5C')">
      <xsl:value-of select="substring-before($string, '%5C')" />%5C%5C<xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="substring-after($string, '%5C')" /></xsl:with-param></xsl:call-template>
  </xsl:if>
  <xsl:if test="not(contains($string, '%5C'))">
    <xsl:value-of select="$string" />
  </xsl:if>
</xsl:template>

<!-- root node-->
<xsl:template match="/">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="/file">

  <xsl:variable name="pathForScript"><xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="encodedPath" /></xsl:with-param></xsl:call-template></xsl:variable>
  
  <xsl:variable name="nameForScript"><xsl:call-template name="insDoubleBackslash"><xsl:with-param name="string"><xsl:value-of select="encodedName" /></xsl:with-param></xsl:call-template></xsl:variable>

  <a>
    <xsl:attribute name="href">javascript:showImage('<xsl:value-of select="$pathForScript" />',<xsl:value-of select="fullScreenWidth" />,<xsl:value-of select="fullScreenHeight" />)</xsl:attribute>
    <img class="thumb" border="0">
      <xsl:attribute name="src"><xsl:value-of select="imgPath" /></xsl:attribute>
      <xsl:attribute name="width"><xsl:value-of select="thumbnailWidth" /></xsl:attribute>
      <xsl:attribute name="height"><xsl:value-of select="thumbnailHeight" /></xsl:attribute>
      <xsl:if test="description">
        <xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
      </xsl:if>
    </img>
  </a>
  
  <br/>
  <input type="checkbox" class="cb2">
    <xsl:attribute name="name">list-<xsl:value-of select="@name" /></xsl:attribute>
  </input>
  
  <a class="fn">
    <xsl:attribute name="href">javascript:jsContextMenu('<xsl:value-of select="$nameForScript" />','<xsl:value-of select="imgType" />','<xsl:value-of select="@id" />')</xsl:attribute>
    <xsl:attribute name="title"><xsl:value-of select="@name" /></xsl:attribute>
    <xsl:value-of select="displayName" />
  </a>
              
  <br/>
  <xsl:value-of select="@lastModified" />
                
  <br/>
  <xsl:value-of select="@size" /> KB
  &#160;
  <xsl:value-of select="xpix" />
  x
  <xsl:value-of select="ypix" />
  pix
                
  <br/>
  <xsl:value-of select="comments" />
  <xsl:value-of select="' '" />
  <xsl:value-of select="resources/msg[@key='label.comments']/@value" />

  &#160;

  <xsl:if test="ownerRating or visitorRating">
    <a class="dirtree">
      <xsl:attribute name="title">
        <xsl:if test="ownerRating">Rating by Owner: <xsl:value-of select="ownerRating" /><xsl:if test="visitorRating"> / </xsl:if></xsl:if>
        <xsl:if test="visitorRating">Rating by Visitors: <xsl:value-of select="visitorRating" /></xsl:if> (5 = best)
      </xsl:attribute>
      <img src="images/star.gif" border="0" />
      <xsl:if test="ownerRating">
        <xsl:value-of select="ownerRating" />
        <xsl:if test="visitorRating">/</xsl:if>
      </xsl:if>
      <xsl:if test="visitorRating">
        <xsl:value-of select="visitorRating" />
      </xsl:if>
    </a>
  </xsl:if>
            
</xsl:template>

</xsl:stylesheet>

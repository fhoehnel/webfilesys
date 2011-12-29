<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="folderTree folder" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/folderTree/css" />.css</xsl:attribute>
  </link>

  <style type="text/css">
    img {vertical-align:middle}
  </style>
  
  <title><xsl:value-of select="/folderTree/resources/msg[@key='label.viewzip']/@value" /></title>

</head>

<body>

<xsl:apply-templates />

</body>
</html>


</xsl:template>
<!-- end root node-->

<xsl:template match="folderTree">

  <img src="images/space.gif" border="0" width="12" height="17" />
  <img src="images/zip.gif" border="0" width="19" height="14" />
  <img src="images/space.gif" border="0" width="4" height="1" />
  <a class="dirtree">
    <xsl:value-of select="/folderTree/shortZipFileName" />
  </a>
  
  <xsl:for-each select="zipEntry">
    <xsl:call-template name="zipEntry" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="zipEntry"> 

  <div class="last">
      
    <xsl:if test="position()=last()">
      <img src="images/branchLast.gif" border="0" width="15" height="17" />
    </xsl:if>
    <xsl:if test="position()!=last()">
      <img src="images/branch.gif" border="0" width="15" height="17" />
    </xsl:if>

    <xsl:if test="@folder">
      <img src="images/folder.gif" border="0" width="17" height="14" />
    </xsl:if>

    <xsl:if test="not(@folder)">
      <img border="0" width="16" height="16">
        <xsl:attribute name="src">icons/<xsl:value-of select="@icon" /></xsl:attribute>
      </img>
    </xsl:if>

    <img src="images/space.gif" border="0" width="4" height="1" />
    
    <a class="dirtree">
      <xsl:attribute name="href">/webfilesys/servlet?command=getZipContentFile&amp;zipFilePath=<xsl:value-of select="/folderTree/zipFileEncodedPath" />&amp;zipContentPath=<xsl:value-of select="@path" /></xsl:attribute>
      <xsl:attribute name="target">_blank</xsl:attribute>
      <xsl:attribute name="class">
        <xsl:value-of select="'dirtree'"/>
      </xsl:attribute>
    
      <xsl:value-of select="@name" /> 
    </a>
    
    <xsl:if test="not(@folder)">
      (<xsl:value-of select="@entrySize" /> / <xsl:value-of select="@compressedSize" /> bytes)
    </xsl:if>

    <xsl:if test="zipEntry">
      <xsl:if test="position()=last()">
        <div class="indent">
          <xsl:for-each select="zipEntry">
            <xsl:call-template name="zipEntry" />
          </xsl:for-each>
        </div>
      </xsl:if>

      <xsl:if test="position()!=last()">
        <div class="indent">
          <div class="more">
            <xsl:for-each select="zipEntry">
              <xsl:call-template name="zipEntry" />
            </xsl:for-each>
          </div>
        </div>
      </xsl:if>
    </xsl:if>

  </div>
  
</xsl:template>

</xsl:stylesheet>



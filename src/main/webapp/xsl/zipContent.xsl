<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="folderTree folder" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/fileIcons.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/folderTree/css" />.css</xsl:attribute>
  </link>

  <style type="text/css">
    img {vertical-align:middle}
  </style>
  
  <title><xsl:value-of select="/folderTree/resources/msg[@key='label.viewzip']/@value" /></title>

</head>

<body class="zipContent">

<xsl:apply-templates />

</body>
</html>


</xsl:template>
<!-- end root node-->

<xsl:template match="folderTree">

  <span class="icon-font icon-folderOpen"></span>
  
  <a class="dirtree">
    <xsl:value-of select="/folderTree/shortZipFileName" />
  </a>
  
  <xsl:for-each select="zipEntry">
    <xsl:call-template name="zipEntry" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="zipEntry"> 

  <div class="last zipEntry">
      
    <xsl:if test="position()=last()">
      <img src="images/branchLast.gif" border="0" width="15" height="17" />
    </xsl:if>
    <xsl:if test="position()!=last()">
      <img src="images/branch.gif" border="0" width="15" height="17" />
    </xsl:if>

    <xsl:if test="@folder">
      <span class="icon-font icon-folderOpenFilled"></span>
    </xsl:if>

    <xsl:if test="not(@folder)">
      <xsl:if test="@icon">
        <img border="0" width="16" height="16">
          <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon" /></xsl:attribute>
        </img>
      </xsl:if>
      <xsl:if test="@iconFont">
        <span>
          <xsl:attribute name="class">icon-font fileIcon icon-file-<xsl:value-of select="@iconFont" /></xsl:attribute>
        </span>
      </xsl:if>
      <xsl:if test="not(@icon) and not(@iconFont)">
        <span class="icon-font fileIcon icon-file"></span>
      </xsl:if>
    </xsl:if>

    <a class="dirtree">
      <xsl:attribute name="href">/webfilesys/servlet?command=getZipContentFile&amp;zipFilePath=<xsl:value-of select="/folderTree/zipFileEncodedPath" />&amp;zipContentPath=<xsl:value-of select="@path" /></xsl:attribute>
      <xsl:attribute name="target">_blank</xsl:attribute>
      <xsl:attribute name="class">
        <xsl:value-of select="'dirtree'"/>
      </xsl:attribute>
    
      <xsl:value-of select="@name" /> 
    </a>
    
    <xsl:if test="not(@folder)">
      <span class="zipSize">
        (<xsl:value-of select="@entrySize" /> / <xsl:value-of select="@compressedSize" /> bytes)
      </span>
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



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
    img {vertical-align:bottom;}
  </style>

</head>

<body>
<xsl:attribute name="class">dirTree</xsl:attribute>

<xsl:apply-templates />

</body>
</html>


</xsl:template>
<!-- end root node-->

<xsl:template match="folderTree">

  <img src="images/space.gif" border="0" width="12" height="17" />
  <img src="images/fastpath.gif" border="0" width="19" height="14" />
  <img src="images/space.gif" border="0" width="4" height="1" />
  <a class="dirtree">
    <xsl:value-of select="resources/msg[@key='label.fastpath']/@value" />
  </a>
  
  <xsl:for-each select="folder">
    <xsl:call-template name="folder" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="folder"> 

  <div class="last">
      
    <xsl:if test="position()=last()">
      <img src="images/branchLast.gif" border="0" width="15" height="17" />
    </xsl:if>
    <xsl:if test="position()!=last()">
      <img src="images/branch.gif" border="0" width="15" height="17" />
    </xsl:if>

    <xsl:if test="@bookmark and @visited">
      <img src="images/bookmark.gif" border="0" width="16" height="16" />
    </xsl:if>

    <xsl:if test="not(@bookmark)">
      <xsl:if test="@icon">
        <img class="icon">
          <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon"/></xsl:attribute>
        </img>
      </xsl:if>
    
      <xsl:if test="not(@icon)">
        <img src="images/folder.gif" border="0" width="17" height="14" />
      </xsl:if>
    </xsl:if>

    <img src="images/space.gif" border="0" width="4" height="1" />
    
    <a class="dirtree">
      <xsl:attribute name="href">/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="@path" />&amp;mask=*&amp;fastPath=true</xsl:attribute>

      <xsl:if test="not(@visited)">
        <xsl:attribute name="class">
          <xsl:value-of select="'tab'"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="@visited">
        <xsl:attribute name="class">
          <xsl:value-of select="'dirtree'"/>
        </xsl:attribute>
        
        <xsl:if test="@textColor">
          <xsl:attribute name="style">color:<xsl:value-of select="@textColor" /></xsl:attribute>
        </xsl:if>
      </xsl:if>
    
      <xsl:value-of select="@name" />
    </a>

    <xsl:if test="folder">
      <xsl:if test="position()=last()">
        <div class="indent">
          <xsl:for-each select="folder">
            <xsl:call-template name="folder" />
          </xsl:for-each>
        </div>
      </xsl:if>

      <xsl:if test="position()!=last()">
        <div class="indent">
          <div class="more">
            <xsl:for-each select="folder">
              <xsl:call-template name="folder" />
            </xsl:for-each>
          </div>
        </div>
      </xsl:if>
    </xsl:if>

  </div>
  
</xsl:template>

</xsl:stylesheet>



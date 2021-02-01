<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="folderTree folder" />

<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/folderTree/css" />.css</xsl:attribute>
  </link>

  <style type="text/css">
    img {vertical-align:bottom;}
  </style>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/folderTree/language" /></xsl:attribute>
  </script>

</head>

<body>
  <xsl:attribute name="class">dirTree</xsl:attribute>

  <xsl:apply-templates />

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>


</xsl:template>
<!-- end root node-->

<xsl:template match="folderTree">

  <div class="fastpathHeadline">
    <span class="icon-font icon-folderOpenFilled"></span>
    <span resource="label.fastpath"></span>
  </div>
  
  <xsl:for-each select="folder">
    <xsl:call-template name="folder" />
  </xsl:for-each>

</xsl:template>

<xsl:template name="folder"> 

  <div class="fastpath last">
      
    <xsl:if test="position()=last()">
      <img src="images/branchLast.gif" border="0" width="15" height="17" />
    </xsl:if>
    <xsl:if test="position()!=last()">
      <img src="images/branch.gif" border="0" width="15" height="17" />
    </xsl:if>

    <xsl:if test="@bookmark and @visited">
      <span class="icon-font icon-star"></span>
    </xsl:if>

    <xsl:if test="not(@bookmark)">
      <xsl:if test="@icon">
        <img class="icon">
          <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="@icon"/></xsl:attribute>
        </img>
      </xsl:if>
    
      <xsl:if test="not(@icon)">
        <xsl:if test="folder">
          <span class="icon-font icon-folderOpenFilled"></span>
        </xsl:if>
        <xsl:if test="not(folder)">
          <span class="icon-font icon-folder"></span>
        </xsl:if>
      </xsl:if>
    </xsl:if>

    <img src="images/space.gif" border="0" width="4" height="1" />
    
    <a class="dirtree">
      <xsl:attribute name="href">javascript:fastpath('<xsl:value-of select="@pathForScript" />')</xsl:attribute>

      <xsl:if test="not(@visited)">
        <xsl:attribute name="class">fastpath</xsl:attribute>
      </xsl:if>

      <xsl:if test="@visited">
        <xsl:attribute name="class">dirtree</xsl:attribute>
        
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



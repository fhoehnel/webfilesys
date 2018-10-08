<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="watchList folder" />

<!-- root node-->
<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/watchList/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
	<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/watchList/language" /></xsl:attribute>
    </script>
	
  </head>

  <body onload="setBundleResources()" class="folderWatch">

  <xsl:apply-templates />

  </body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="watchList">

  <div class="headline" resource="watchListHeadline"></div>

  <div class="watchList">

    <xsl:if test="folder">

      <ul class="folderWatchList">

        <xsl:for-each select="folder">
		
          <li>
            <xsl:if test="icon">
              <img border="0" style="vertical-align:middle;margin-right:10px;">
                <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="icon" /></xsl:attribute>
              </img>
            </xsl:if>
            <xsl:if test="not(icon)">
               <span class="icon-font icon-folderClosed"></span>
            </xsl:if>

	        <a class="dirtree" href="javascript:void(0)">
              <xsl:if test="textColor">
                <xsl:attribute name="style">color:<xsl:value-of select="textColor" /></xsl:attribute>
              </xsl:if>
              <xsl:attribute name="title"><xsl:value-of select="relativePath" /></xsl:attribute>
		      <xsl:value-of select="shortPath" />
            </a>
            
            &#160;
              
            <a class="icon-font icon-delete" titleResource="button.stopWatch">
              <xsl:attribute name="href">/webfilesys/servlet?command=watchList&amp;cmd=unwatch&amp;path=<xsl:value-of select="encodedPath" /></xsl:attribute>
            </a>
          </li>
            
        </xsl:for-each>
      
      </ul>
    
    </xsl:if>
    
    <xsl:if test="not(folder)">
      <span resource="watchListEmpty"></span>
    </xsl:if>

  </div>

  <div class="buttonCont">
    <input type="button" resource="button.return">
	  <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="/watchList/currentPathEncoded"/>'</xsl:attribute>
	</input>
	<input type="hidden"></input>
  </div>
  
</xsl:template>

</xsl:stylesheet>

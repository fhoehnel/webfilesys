<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="watchList folder" />

<!-- root node-->
<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />

    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/watchList/css" />.css</xsl:attribute>
    </link>

	<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/watchList/language" /></xsl:attribute>
    </script>
	
  </head>

  <body onload="setBundleResources()">

  <xsl:apply-templates />

  </body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="watchList">

  <div class="headline" resource="watchListHeadline"></div>

  <xsl:if test="folder">

    <ul class="folderWatchList">

      <xsl:for-each select="folder">
		
        <li>
          <img border="0" style="vertical-align:middle;margin-right:10px;">
            <xsl:if test="icon">
              <xsl:attribute name="src">/webfilesys/icons/<xsl:value-of select="icon" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="not(icon)">
              <xsl:attribute name="src">/webfilesys/images/folder.gif</xsl:attribute>
            </xsl:if>
          </img>

	      <a class="dirtree" href="javascript:void(0)">
            <xsl:if test="textColor">
              <xsl:attribute name="style">color:<xsl:value-of select="textColor" /></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="title"><xsl:value-of select="relativePath" /></xsl:attribute>
		    <xsl:value-of select="shortPath" />
          </a>
            
          &#160;
              
          <a titleResource="button.stopWatch">
            <xsl:attribute name="href">/webfilesys/servlet?command=watchList&amp;cmd=unwatch&amp;path=<xsl:value-of select="encodedPath" /></xsl:attribute>
            <img src="/webfilesys/images/trash.gif" border="0" style="vertical-align:middle"/>
          </a>
        </li>
            
      </xsl:for-each>
      
    </ul>
    
  </xsl:if>
    
  <xsl:if test="not(folder)">
    <br />
    <span resource="watchListEmpty"></span>
    <br/><br/>
  </xsl:if>

  <div class="buttonCont">
    <input type="button" resource="button.return">
	  <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="/watchList/currentPathEncoded"/>'</xsl:attribute>
	</input>
  </div>
  
</xsl:template>

</xsl:stylesheet>

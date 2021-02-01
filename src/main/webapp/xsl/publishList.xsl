<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="publishList" />

<!-- root node-->
<xsl:template match="/">

<html>
  <head>

    <meta http-equiv="expires" content="0" />
    
    <title resource="label.publishList" />

    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/icons.css" />

    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/publishList/css" />.css</xsl:attribute>
    </link>

    <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/publish.js" type="text/javascript"></script>
    <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
	<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>

    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/publishList/language" /></xsl:attribute>
    </script>
	
  </head>

  <body onload="setBundleResources()" class="publishList">

      <div class="headline" resource="label.publishList"></div>

      <xsl:if test="/publishList/publications/publication">

        <table class="dataList" style="width:100%;margin-top:20px;">

		  <tr>
			<th class="datahead" resource="label.path"></th>
			<th class="datahead" resource="label.expires"></th>
			<th class="datahead" resource="label.accesscode"></th>
			<th class="datahead">&#160;</th>
		  </tr>

          <xsl:for-each select="/publishList/publications/publication">

            <tr>
              <td class="data"><xsl:value-of select="relativePath" /></td>
              
              <td class="data"><xsl:value-of select="expires" /></td>
              
              <td class="data">
                <textarea class="publicUrlCont" readonly="readonly">
                  <xsl:attribute name="id">urlInput-<xsl:value-of select="position()" /></xsl:attribute>
                  <xsl:value-of select="secretUrl" />
                </textarea> 
              </td>
              
              <td class="data">
                <a class="icon-font icon-copy" titleResource="copyPublicUrl">
                  <xsl:attribute name="onclick">copyUrlToClip(this, 'urlInput-<xsl:value-of select="position()" />')</xsl:attribute>
                </a>
                <a class="icon-font icon-delete" titleResource="label.cancelpublish">
                  <xsl:attribute name="href"><xsl:value-of select="cancelUrl" /></xsl:attribute>
                </a>
              </td>
            </tr>
            
          </xsl:for-each>
      
        </table>
    
      </xsl:if>
    
      <xsl:if test="not(/publishList/publications/publication)">
        <br />
        <span class="plaintext" resource="nothingPublished"></span>
        <br/><br/>
      </xsl:if>

      <div class="centeredButtonCont">
        <input type="button" resource="button.closewin">
	      <xsl:attribute name="onclick">self.close()</xsl:attribute>
	    </input>
      </div>

  </body>
</html>

</xsl:template>
<!-- end root node-->


</xsl:stylesheet>

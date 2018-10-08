<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="publishFile" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/publishFile/css" />.css</xsl:attribute>
</link>

<script src="/webfilesys/javascript/publish.js" type="text/javascript"></script>

<title>
  <xsl:value-of select="/publishFile/resources/msg[@key='label.publishFile']/@value" />
</title>

</head>

<body onload="selectPublicLink()" class="publish">

  <div class="headline">
    <xsl:value-of select="/publishFile/resources/msg[@key='label.publishFile']/@value" />
  </div>

  <br/>
 
  <table class="dataForm" width="100%">
    <tr>
      <td colspan="2" class="formParm1">
        &#160;
      </td>
    </tr>
      
    <tr>
      <td colspan="2" class="formParm1">
        <xsl:value-of select="/publishFile/resources/msg[@key='label.fileToPublish']/@value" />:
      </td>
    </tr>

    <tr>
      <td colspan="2" class="formParm2" style="padding-left:20px">
        <xsl:value-of select="/publishFile/shortPath" />
      </td>
    </tr>
    
    <tr>
      <td colspan="2" class="formParm1">
        &#160;
      </td>
    </tr>
      
    <tr>
      <td colspan="2" class="formParm1">
        <xsl:value-of select="/publishFile/resources/msg[@key='label.filePublished']/@value" />:
      </td>
    </tr>

    <tr>
      <td colspan="2" class="formParm2">
        <textarea id="publicLinkCont" readonly="readonly" style="height:50px;width:100%"><xsl:value-of select="/publishFile/secretURL" /></textarea>
      </td>
    </tr>
    
    <tr>
      <td colspan="2" class="formParm1">
        &#160;
      </td>
    </tr>
      
    <tr>
      <td colspan="2" class="formParm2" style="text-align:center">
        <input type="button" onclick="self.close()">
          <xsl:attribute name="value"><xsl:value-of select="/publishFile/resources/msg[@key='button.closewin']/@value" /></xsl:attribute>        
        </input>
      </td>
    </tr>
    
  </table>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

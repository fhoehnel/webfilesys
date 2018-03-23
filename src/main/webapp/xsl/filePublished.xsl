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

<body onload="selectPublicLink()">

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="/publishFile/resources/msg[@key='label.publishFile']/@value" />
      </th>
    </tr>
  </table>

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
      <td colspan="2" class="formParm2">
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
      
        <table border="0" width="100%">
          <tr>
            <td width="40%">&#160;</td>
            <td nowrap="nowrap" style="padding:10px">
              <a class="button" href="#"> 
                <xsl:attribute name="onclick">this.blur();self.close();</xsl:attribute>
                <span><xsl:value-of select="/publishFile/resources/msg[@key='button.closewin']/@value" /></span>
              </a>
            </td>
            <td width="40%">&#160;</td>
          </tr>
        </table>
      
      </td>
    </tr>
    
  </table>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

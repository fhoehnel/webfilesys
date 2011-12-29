<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="download" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/download/css" />.css</xsl:attribute>
</link>

<title>
  WebFileSys Download 
</title>

</head>

<body style="text-align:center">

  <div class="info" style="width:100%;padding:10px;text-align:center">
    <a class="dirtree" style="text-decoration:underline">
      <xsl:attribute name="href">/webfilesys/servlet?command=getFile&amp;filePath=<xsl:value-of select="/download/path" />&amp;disposition=download</xsl:attribute>
      <xsl:value-of select="/download/resources/msg[@key='label.download.link']/@value" />
    </a>
    <br/><br/>
    <xsl:value-of select="/download/resources/msg[@key='label.download.text1']/@value" />
    <br/><br/>
    <table border="0" width="100%" cellpadding="0" cellspacing="0">
      <tr>
        <td class="value" align="center">
          <xsl:value-of select="/download/shortPath" />
        </td>
      </tr>
    </table>
    <br/><br/>
    <xsl:value-of select="/download/resources/msg[@key='label.download.text2']/@value" />
  </div>

  <table border="0" width="100%">
    <tr>
      <td width="40%">&#160;</td>
      <td nowrap="nowrap" style="padding:10px">
        <a class="button" href="#"> 
          <xsl:attribute name="onclick">this.blur();self.close();</xsl:attribute>
          <span><xsl:value-of select="/download/resources/msg[@key='button.closewin']/@value" /></span>
        </a>
      </td>
      <td width="40%">&#160;</td>
    </tr>
  </table>
</body>

</html>

</xsl:template>

</xsl:stylesheet>


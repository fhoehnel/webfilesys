<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="categoryList category" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/categoryList/css" />.css</xsl:attribute>
</link>

<title>WebFileSys: <xsl:value-of select="/categoryList/resources/msg[@key='label.newCategory']/@value" /></title>

<xsl:if test="/categoryList/resources/msg[@key='error.duplicateCategory']">
  <script langauge="javascript">
    alert('<xsl:value-of select="/categoryList/resources/msg[@key='error.duplicateCategory']/@value" />');
  </script>
</xsl:if>

</head>

<body>

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="categoryList">

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
    <input type="hidden" name="command" value="category" />
    <input type="hidden" name="cmd" value="new" />

    <table border="0" width="100%" cellpadding="2" cellspacing="0">
      <tr>
        <th class="headline">
          <xsl:value-of select="resources/msg[@key='label.newCategory']/@value" />
        </th>
      </tr>
    </table>

    <br/>

    <table class="dataForm" width="100%">

      <tr>
        <td colspan="2">
          &#160;
        </td>
      </tr>

      <tr>
        <td class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.categoryName']/@value" />:
        </td>
        <td class="formParm2">
          <input type="text" name="newCategory" style="width:200px">
            <xsl:if test="category">
              <xsl:attribute name="value"><xsl:value-of select="category/name" /></xsl:attribute>
            </xsl:if>
          </input>
        </td>
      </tr>

      <tr>
        <td colspan="2">
          &#160;
        </td>
      </tr>

      <tr>
        <td class="formButton">
          <a class="button" onclick="this.blur();"> 
            <xsl:attribute name="href">javascript:document.form1.submit()</xsl:attribute>
            <span><xsl:value-of select="resources/msg[@key='button.createCategory']/@value" /></span>
          </a>              
        </td>

        <td class="formButton" nowrap="nowrap" style="text-align:right">
          <a class="button" onclick="this.blur();" style="float:right"> 
            <xsl:attribute name="href">javascript:window.location.href='/webfilesys/servlet?command=category'</xsl:attribute>
            <span><xsl:value-of select="resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
      </tr>
      
    </table>

  </form>
  
</xsl:template>

</xsl:stylesheet>

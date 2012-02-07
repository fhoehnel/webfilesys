<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="error" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<script language="javascript">
  function backToDirTree()
  {
      window.location.href = '/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="/error/currentPath" />';
  }

  <xsl:if test="/error/errorMsg">
    alert('<xsl:value-of select="/error/errorMsg" />');
  </xsl:if>

  setTimeout('backToDirTree()', 1000);

</script>

</head>
</html>

</xsl:template>

</xsl:stylesheet>


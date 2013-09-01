<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/result/shortPath" />
</div>
    
<form name="grepForm" accept-charset="utf-8" style="display:inline;" method="post" action="/webfilesys/servlet" target="_blank">
  <input type="hidden" name="command" value="grep" />
  <input type="hidden" name="fileName">
    <xsl:attribute name="value"><xsl:value-of select="/result/fileName" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td colspan="2" class="formParm1">
        <xsl:value-of select="/result/resources/msg[@key='grepPrompt']/@value" />:
      </td>
    </tr>

    <tr>
      <td colspan="2" class="formParm2">
        <input type="text" name="filter" style="width:280px" />
      </td>
    </tr>
    
    <tr>
      <td>
        <input type="button">
          <xsl:attribute name="onclick">document.grepForm.submit();setTimeout('hidePrompt()', 1000)</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="/result/resources/msg[@key='button.startGrep']/@value" /></xsl:attribute>
        </input>
      </td>
      
      <td style="text-align:right">
        <input type="button" onclick="hidePrompt()">
          <xsl:attribute name="value"><xsl:value-of select="/result/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
        </input>
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>

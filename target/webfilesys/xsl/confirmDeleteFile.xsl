<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/result/filePath" />
</div>
    
<form name="delForm" accept-charset="utf-8" style="display:inline;" method="post" action="/webfilesys/servlet">
  <input type="hidden" name="command" value="fmdelete" />
  <input type="hidden" name="deleteRO" value="yes" />
  <input type="hidden" name="fileName">
    <xsl:attribute name="value"><xsl:value-of select="/result/fileName" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td colspan="2" class="formParm1">
        <xsl:value-of select="/result/deletePromptMsg" />
      </td>
    </tr>
    
    <xsl:if test="/result/error">
      <tr>
        <td colspan="2">
          <a class="button" onclick="this.blur();" style="float:right"> 
            <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
            <span><xsl:value-of select="/result/resources/msg[@key='button.return']/@value" /></span>
          </a>              
        </td>
      </tr>
    </xsl:if>
    
    <xsl:if test="not(/result/error)">
      <tr>
        <td>
          <a class="button" onclick="this.blur();"> 
            <xsl:attribute name="href">javascript:document.delForm.submit()</xsl:attribute>
            <span><xsl:value-of select="/result/resources/msg[@key='button.delete']/@value" /></span>
          </a>              

          <a class="button" onclick="this.blur();" style="float:right"> 
            <xsl:attribute name="href">javascript:hidePrompt()</xsl:attribute>
            <span><xsl:value-of select="/result/resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
      </tr>
    </xsl:if>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/cryptoPrompt/resources/msg[@key='label.decrypt']/@value" />
</div>
    
<form accept-charset="utf-8" name="cryptoForm" id="cryptoForm" method="post" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="decrypt" />
  <input type="hidden" name="fileName">
    <xsl:attribute name="value"><xsl:value-of select="/cryptoPrompt/fileName" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/cryptoPrompt/resources/msg[@key='label.fileToDecrypt']/@value" />:
      </td>
      <td class="formParm2">
        <xsl:value-of select="/cryptoPrompt/shortFileName" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/cryptoPrompt/resources/msg[@key='label.cryptoKey']/@value" />:
      </td>
      <td class="formParm2">
        <input type="password" name="cryptoKey" id="cryptoKey" maxlength="16" style="width:120px;" />
      </td>
    </tr>

    <tr id="buttonRow">
      <td>
        <input type="button">
          <xsl:attribute name="onclick">validateAndSubmitCrypto('<xsl:value-of select="/cryptoPrompt/resources/msg[@key='alert.illegalCryptoKey']/@value" />')</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="/cryptoPrompt/resources/msg[@key='button.decrypt']/@value" /></xsl:attribute>
        </input>
      </td>

      <td style="text-align:right">
        <input type="button">
          <xsl:attribute name="onclick">hidePrompt()</xsl:attribute>
          <xsl:attribute name="value"><xsl:value-of select="/cryptoPrompt/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
        </input>
      </td>
    </tr>

  </table>
  
  <div id="hourGlass" style="width:64px;height:64px;position:absolute;top:50%;left:50%;margin-left:-32px;margin-top:-32px;background-color:ivory;visibility:hidden;border:1px solid black;">
    <img src="/webfilesys/images/hourglass.gif" width="32" height="32" border="0" style="padding:8px;" /> 
  </div>
  
</form>

</xsl:template>

</xsl:stylesheet>

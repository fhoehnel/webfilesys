<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/emailFile/resources/msg[@key='label.sendfilehead']/@value" />
</div>
    
<form accept-charset="utf-8" name="emailForm" id="emailForm" method="get" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="emailFile" />
  <input type="hidden" name="fileName">
    <xsl:attribute name="value"><xsl:value-of select="/emailFile/fileName" /></xsl:attribute>
  </input>
  
  <xsl:if test="/emailFile/filePath">
    <input type="hidden" name="filePath">
      <xsl:attribute name="value"><xsl:value-of select="/emailFile/filePath" /></xsl:attribute>
    </input>
  </xsl:if>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/emailFile/resources/msg[@key='label.filetosend']/@value" />:
      </td>
      <td class="formParm2" width="70%">
        <xsl:value-of select="/emailFile/shortFileName" />
      </td>
    </tr>
    
    <tr>
      <td colspan="2" class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/emailFile/resources/msg[@key='label.receiver']/@value" />:
      </td>
    </tr>
    <tr>
      <td colspan="2" class="formParm2">
        <input type="email" name="receiver" maxlength="256" style="width:100%;">
          <xsl:attribute name="placeholder">nobody@nowhere.com</xsl:attribute>
        </input>
      </td>
    </tr>

    <tr>
      <td colspan="2" class="formParm1" nowrap="nowrap">
        <xsl:value-of select="/emailFile/resources/msg[@key='label.subject']/@value" />:
      </td>
    </tr>
    <tr>
      <td colspan="2" class="formParm2">
        <input type="text" name="subject" maxlength="256" style="width:100%;">
          <xsl:attribute name="value"><xsl:value-of select="/emailFile/fileName" /></xsl:attribute>
        </input>
      </td>
    </tr>

    <tr>
      <td colspan="2" class="formParm1">
        <input type="checkbox" name="sendSynchronous" class="cb2" checked="checked" />
        &#160;
        <xsl:value-of select="/emailFile/resources/msg[@key='label.sendEmailSync']/@value" />
      </td>
    </tr>

    <tr>
      <td colspan="2">
        <a id="sendButton" class="button" onclick="this.blur();"> 
          <xsl:attribute name="href">javascript:if (validateEmailList(document.emailForm.receiver.value)) {sendFileViaEmail(); hidePrompt(); showHourGlass();} else {alert('<xsl:value-of select="/emailFile/resources/msg[@key='alert.emailsyntax']/@value" />');}</xsl:attribute>
          <span><xsl:value-of select="/emailFile/resources/msg[@key='button.sendfile']/@value" /></span>
        </a>              

        <a class="button" onclick="this.blur();" style="float:right"> 
          <xsl:attribute name="href">javascript:hidePrompt();hideHourGlass();</xsl:attribute>
          <span><xsl:value-of select="/emailFile/resources/msg[@key='button.cancel']/@value" /></span>
        </a>              
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>

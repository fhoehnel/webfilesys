<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead">
  <xsl:value-of select="/addBookmark/resources/msg[@key='label.addBookmark']/@value" />
</div>
    
<form accept-charset="utf-8" name="bookmarkForm" id="bookmarkForm" method="get" action="/webfilesys/servlet" 
      style="display:inline;">
  <xsl:attribute name="onsubmit">validateBookmarkName('<xsl:value-of select="/addBookmark/resources/msg[@key='alert.bookmarkMissingName']/@value" />');return false;</xsl:attribute>
  <input type="hidden" name="command" value="createBookmark" />
  <input type="hidden" name="currentPath">
    <xsl:attribute name="value"><xsl:value-of select="/addBookmark/currentPath" /></xsl:attribute>
  </input>
  
  <table border="0" width="100%" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/addBookmark/resources/msg[@key='label.directory']/@value" />:
      </td>
    </tr>
    <tr>      
      <td class="formParm2">
        <xsl:value-of select="/addBookmark/currentPathShort" />
      </td>
    </tr>
    <tr>
      <td class="formParm1" nowrap="nowrap" style="padding-right:0px;">
        <xsl:value-of select="/addBookmark/resources/msg[@key='label.bookmarkName']/@value" />:
      </td>
    </tr>
    <tr>
      <td class="formParm2">
        <input type="text" name="bookmarkName" maxlength="256" value="" style="width:100%;" />
      </td>
    </tr>

    <tr>
      <td>
        <div class="buttonCont">
          <input type="button" style="float:left">
            <xsl:attribute name="onclick">javascript:validateBookmarkName('<xsl:value-of select="/addBookmark/resources/msg[@key='alert.bookmarkMissingName']/@value" />')</xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="/addBookmark/resources/msg[@key='button.create']/@value" /></xsl:attribute>
          </input>              

          <input type="button" style="float:right"> 
            <xsl:attribute name="onclick">javascript:hidePrompt()</xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="/addBookmark/resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
          </input>
        </div>      
      </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>

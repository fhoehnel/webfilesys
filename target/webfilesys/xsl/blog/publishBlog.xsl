<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<div class="promptHead" resource="blog.publishTitle"></div>
    
<form accept-charset="utf-8" name="publishForm" id="publishForm" method="post" action="/webfilesys/servlet" style="display:inline;">
  <input type="hidden" name="command" value="blog" />
  <input type="hidden" name="cmd" value="publish" />
  
  <table id="publishTable" class="blogPublishForm" cellpadding="10">
  
    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="blog.daysPerPage" />:
      </td>
      <td class="formParm2" width="70%">
        <input type="text" id="daysPerPage" name="daysPerPage" maxlength="3" style="width:80px;" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" colspan="2" nowrap="nowrap">
	    <input type="checkbox" id="allowComments" name="allowComments" />
	    <label for="allowComments" resource="label.allowcomments" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.expiration" />:
      </td>
      <td class="formParm2" width="70%">
        <input type="text" id="expirationDays" name="expirationDays" maxlength="5" style="width:80px;" />
      </td>
    </tr>

    <tr>
      <td class="formParm1" nowrap="nowrap">
	    <span resource="label.language" />:
      </td>
      <td class="formParm2" width="70%">
        <select id="language" name="language">
          <option value="" resource="label.selectLanguage"></option>
          <xsl:for-each select="/blog/languages/language">
            <option><xsl:value-of select="." /></option>
          </xsl:for-each>
        </select>
      </td>
    </tr>

    <tr>
      <td colspan="2">
	  
        <input type="button" resource="button.publish" style="float:left">
          <xsl:attribute name="onclick">javascript:validatePublishFormAndSubmit()</xsl:attribute>
        </input> 
	  
        <input type="button" resource="button.cancel" style="float:right">
          <xsl:attribute name="onclick">javascript:hidePublishForm()</xsl:attribute>
        </input> 

	  </td>
    </tr>

  </table>
  
</form>

</xsl:template>

</xsl:stylesheet>

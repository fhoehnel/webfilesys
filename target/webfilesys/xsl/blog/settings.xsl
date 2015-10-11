<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="settings" />

<!-- root node-->
<xsl:template match="/">

<script language="javascript">
</script>

<div class="blogSettings">

  <div class="blogSettingsHead">
    <span resource="blog.settingsHeadline"></span>
  </div>

  <form accept-charset="utf-8" id="blogSettingsForm" name="blogSettingsForm" method="post" action="/webfilesys/servlet" class="blogSettingsForm">
  
    <input type="hidden" name="command" value="blog" />
    <input type="hidden" name="cmd" value="saveSettings" />

    <table width="100%">
      <tr>
        <td class="formParm1">
          <label for="blogTitle" resource="blog.titleText" />:
        </td>
        <td class="formParm2">
          <input type="text" id="blogTitle" name="blogTitle" class="settings">
            <xsl:if test="/settings/blogTitleText">
              <xsl:attribute name="value"><xsl:value-of select="/settings/blogTitleText" /></xsl:attribute>
            </xsl:if>
           </input>
        </td>
      </tr>

      <tr>
        <td class="formParm1">
          <label for="daysPerPage" resource="blog.daysPerPage" />:
        </td>
        <td class="formParm2">
          <input type="text" id="daysPerPage" name="daysPerPage" class="settings">
            <xsl:if test="/settings/daysPerPage">
              <xsl:attribute name="value"><xsl:value-of select="/settings/daysPerPage" /></xsl:attribute>
            </xsl:if>
           </input>
        </td>
      </tr>

      <tr>
        <td class="formParm1">
          <label for="password" resource="blog.newPassword" />:
        </td>
        <td class="formParm2">
          <input type="password" id="newPassword" name="newPassword" class="settings">
           </input>
        </td>
      </tr>

      <tr>
        <td class="formParm1">
          <label for="password" resource="blog.newPasswdConfirm" />:
        </td>
        <td class="formParm2">
          <input type="password" id="newPasswdConfirm" name="newPasswdConfirm" class="settings">
           </input>
        </td>
      </tr>

      <tr>
        <td colspan="2">
          <div class="buttonCont">        
            <input type="button" resource="button.save" onclick="validateSettingsForm()" />
            <input type="button" resource="button.cancel" class="rightAlignedButton" onclick="hideSettings()" />
          </div>
        </td>
      </tr>

    </table>
    
  </form>

</div>

</xsl:template>

</xsl:stylesheet>

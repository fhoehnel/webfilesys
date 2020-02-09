<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="userSettings" />

<xsl:template match="/">

<html>
  <head>
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
    
    <link rel="stylesheet" type="text/css">
      <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/userSettings/css" />.css</xsl:attribute>
    </link>
    
    <script type="text/javascript" src="/webfilesys/javascript/ajaxslt/util.js"></script>
    <script src="/webfilesys/javascript/jquery/jquery.min.js"></script>
    <script type="text/javascript" src="/webfilesys/javascript/browserCheck.js"></script>
    <script type="text/javascript" src="/webfilesys/javascript/util.js">
    </script><script type="text/javascript" src="/webfilesys/javascript/fmweb.js"></script>
    <script type="text/javascript" src="/webfilesys/javascript/resourceBundle.js"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/userSettings/language" /></xsl:attribute>
    </script>
  </head>
  
  <body class="selfEditUser">
	<div class="headline" resource="label.editregistration"></div>

	<br />

	<form accept-charset="utf-8" method="post" action="/webfilesys/servlet" class="userData">
		<input type="hidden" name="command" value="selfChangeUser" />
		<table class="dataForm" width="100%">
			<tr>
				<td class="formParm1"><b><span resource="label.login"></span></b></td>
				<td class="formParm2"><xsl:value-of select="/userSettings/login" /></td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.newpassword"></td>
				<td class="formParm2">
				  <input type="text" class="secure" name="password" maxlength="30" value="" autocomplete="off"/>
				</td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.passwordconfirm"></td>
				<td class="formParm2">
				  <input type="text" class="secure" name="pwconfirm" maxlength="30" value="" autocomplete="off"/>
				</td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.ropassword"></td>
				<td class="formParm2">
				  <input type="text" class="secure" name="ropassword" maxlength="30" value="" autocomplete="off"/>
				</td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.ropwconfirm"></td>
				<td class="formParm2">
				  <input type="text" class="secure" name="ropwconfirm" maxlength="30" value="" autocomplete="off"/>
				</td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.firstname"></td>
				<td class="formParm2">
				  <input type="text" name="firstName" maxlength="64">
				    <xsl:attribute name="value"><xsl:value-of select="/userSettings/firstName" /></xsl:attribute>
				  </input>
				</td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.lastname"></td>
				<td class="formParm2">
				  <input type="text" name="lastName" maxlength="64">
				    <xsl:attribute name="value"><xsl:value-of select="/userSettings/lastName" /></xsl:attribute>
				  </input>
				</td>
			</tr>
			<tr>
				<td class="formParm1"><b><span resource="label.email"></span></b></td>
				<td class="formParm2">
				  <input type="email" name="email" maxlength="120" autocomplete="off" required="required">
				    <xsl:attribute name="value"><xsl:value-of select="/userSettings/email" /></xsl:attribute>
                  </input>				  
				</td>
			</tr>
			<tr>
				<td class="formParm1" resource="label.phone"></td>
				<td class="formParm2">
				  <input type="text" name="phone" maxlength="30">
				    <xsl:attribute name="value"><xsl:value-of select="/userSettings/phone" /></xsl:attribute>
				  </input>
				</td>
			</tr>
			<tr>
				<td class="formParm1"><b><span resource="label.language"></span></b></td>
				<td class="formParm2">
				  <select name="language" size="1">
				    <xsl:for-each select="/userSettings/availableLanguages/lang">
				      <option>
				        <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
				        <xsl:if test="@selected"> 
				          <xsl:attribute name="selected">selected</xsl:attribute>
				        </xsl:if>
				        <xsl:value-of select="."/>
				      </option>
				    </xsl:for-each>
				  </select>
				</td>
			</tr>
			<tr>
				<td class="formParm1"><b><span resource="label.css"></span></b></td>
				<td class="formParm2">
				  <select name="css" size="1">
				    <xsl:for-each select="/userSettings/availableSkins/skin">
				      <option>
				        <xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
				        <xsl:if test="@selected"> 
				          <xsl:attribute name="selected">selected</xsl:attribute>
				        </xsl:if>
                        <xsl:value-of select="."/>
				      </option>
				    </xsl:for-each>
				  </select>
			    </td>
			</tr>
			<tr>
				<td colspan="2" class="formParm1"></td>
			</tr>
			<tr>
				<td class="formButton">
				  <input type="submit" name="changebutton" resource="button.save" />
				</td>
				<td class="formButton" style="text-align: right">
				  <input type="button" resource="button.cancel" onclick="window.location.href='/webfilesys/servlet?command=listFiles';" />
				</td>
			</tr>
		</table>
	</form>
  </body>
  
  <script type="text/javascript">
    setBundleResources();
    
    <xsl:if test="/userSettings/errorMsg">
        customAlert('<xsl:value-of select="/userSettings/errorMsg" />');
    </xsl:if>
  </script>
  
</html>

</xsl:template>

</xsl:stylesheet>

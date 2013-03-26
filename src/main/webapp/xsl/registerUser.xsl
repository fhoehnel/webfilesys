<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="registration" />

<!-- root node-->
<xsl:template match="/registration">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/registration/css" />.css</xsl:attribute>
</link>

<style type="text/css">
  div.registrationBox {width:520px;height:460px;position:relative;top:-230px;left:-260px;background-color:#ffffe0;border-style:solid;border-width:1px;border-color:black;padding-top:20px;}

  td.formError {background-color:#ffffe0;color:#b00000;font-size:10pt;font-family:Arial,Helvetica;vertical-align:top;padding:8px;padding-top:4px;padding-bottom:4px;}

</style>

<link rel="shortcut icon" href="/webfilesys/images/favicon.ico" />

<title>
  WebFileSys: 
  <xsl:value-of select="/registration/resources/msg[@key='label.regtitle']/@value" />
</title>

<script language="javascript">

</script>

</head>

<body>

<div class="centerBox">
  <div class="registrationBox">

    <table border="0" cellpadding="5" cellspacing="0" width="100%">
      <tr>
        <td class="loginTitle" style="padding-left:10px;vertical-align:top;">
          <img src="/webfilesys/images/logo.gif" border="0" />
          <div style="width:100%;padding-top:10px;padding-left:0px;">
            WebFileSys
            <br/>
            <span style="font-style:italic">
              <xsl:value-of select="resources/msg[@key='label.regtitle']/@value" />
            </span>
            
            <!-- validation error messages -->
            <xsl:if test="validation/error">
              <br/>
              <br/>
              <table border="0" cellpadding="0" cellspacing="0">
                <xsl:for-each select="validation/error">
                  <tr> 
                    <td class="formError" style="padding-left:0px;padding-right:0px;">
                      <xsl:if test="@message">
                        <xsl:value-of select="@message" />
                      </xsl:if>
                    </td>
                  </tr>
                </xsl:for-each>
              </table>
            </xsl:if>

          </div>
        </td>
        
        <td width="65%">
          <form accept-charset="utf-8" name="form1" method="POST" action="/webfilesys/servlet" style="margin:0px">

            <input type="hidden" name="command" value="registerSelf" />

            <table border="0">
            
              <tr>
                <td>
                  <xsl:if test="validation/error[@field='username']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='username'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <b><xsl:value-of select="resources/msg[@key='label.login']/@value" /></b>
                </td>
                <td class="formParm2">
                  <input type="text" name="username" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='username']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='username']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td>
                  <xsl:if test="validation/error[@field='password']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='password'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <b><xsl:value-of select="resources/msg[@key='label.password']/@value" /></b>
                </td>
                <td class="formParm2">
                  <input type="password" name="password" size="20" maxlength="30" style="width:150px"> 
                    <xsl:if test="requestParms/requestParm[@key='password']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='password']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td>
                  <xsl:if test="validation/error[@field='pwconfirm']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='pwconfirm'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <b><xsl:value-of select="resources/msg[@key='label.passwordconfirm']/@value" /></b>
                </td>
                <td class="formParm2">
                  <input type="password" name="pwconfirm" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='pwconfirm']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='pwconfirm']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td>
                  <xsl:if test="validation/error[@field='ropassword']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='ropassword'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <xsl:value-of select="resources/msg[@key='label.ropassword']/@value" />
                </td>
                <td class="formParm2">
                  <input type="password" name="ropassword" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='ropassword']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='ropassword']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td>
                  <xsl:if test="validation/error[@field='ropwconfirm']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='ropwconfirm'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <xsl:value-of select="resources/msg[@key='label.ropwconfirm']/@value" />
                </td>
                <td class="formParm2">
                  <input type="password" name="ropwconfirm" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='ropwconfirm']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='ropwconfirm']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td class="formParm1">
                  <xsl:value-of select="resources/msg[@key='label.firstname']/@value" />
                </td>
                <td class="formParm2">
                  <input type="text" name="firstName" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='firstName']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='firstName']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td class="formParm1">
                  <xsl:value-of select="resources/msg[@key='label.lastname']/@value" />
                </td>
                <td class="formParm2">
                  <input type="text" name="lastName" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='lastName']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='lastName']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td>
                  <xsl:if test="validation/error[@field='email']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='email'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <b><xsl:value-of select="resources/msg[@key='label.email']/@value" /></b>
                </td>
                <td class="formParm2">
                  <input type="text" name="email" size="20" maxlength="120" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='email']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='email']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td class="formParm1">
                  <xsl:value-of select="resources/msg[@key='label.phone']/@value" />
                </td>
                <td class="formParm2">
                  <input type="text" name="phone" size="20" maxlength="30" style="width:150px">
                    <xsl:if test="requestParms/requestParm[@key='phone']">
                      <xsl:attribute name="value"><xsl:value-of select="requestParms/requestParm[@key='phone']" /></xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
              
              <tr>
                <td class="formParm1">
                  <xsl:if test="validation/error[@field='language']">
                    <xsl:attribute name="class">formError</xsl:attribute>
                  </xsl:if>  
                  <xsl:if test="not(validation/error[@field='language'])">
                    <xsl:attribute name="class">formParm1</xsl:attribute>
                  </xsl:if>  
                  <b><xsl:value-of select="resources/msg[@key='label.language']/@value" /></b>
                </td>
                <td class="formParm2">
                  <select name="language" size="1">
                  
                    <option value=""><xsl:value-of select="resources/msg[@key='label.selectLanguage']/@value" /></option>
                  
                    <xsl:for-each select="languages/language">
                      <option>
                        <xsl:if test="@selected">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="." />
                      </option>
                    </xsl:for-each>
                  
                  </select>
                </td>
              </tr>

              <tr>
                <td class="formParm1">
                  <xsl:value-of select="resources/msg[@key='label.css']/@value" />
                </td>
                <td class="formParm2">
                  <select name="css" size="1">
                  
                    <xsl:for-each select="layouts/layout">
                      <option>
                        <xsl:if test="@selected">
                          <xsl:attribute name="selected">true</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="." />
                      </option>
                    </xsl:for-each>
                    
                  </select>
                </td>
              </tr>
              
              <tr>
                <td align="left" class="formButton">
                  <input type="submit">
                    <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.register']/@value" /></xsl:attribute>
                  </input>
                </td>
                <td align="right" class="formButton">
                  <input type="button">
                    <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet'</xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
                  </input>
                </td>
              </tr>
            </table>
          
          </form>
        
        </td>
      </tr>
    </table>

  </div>
</div>


</body>
</html>

</xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="login" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="X-UA-Compatible" content="IE=Edge" />

<meta http-equiv="expires" content="0" />

<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/login/css" />.css</xsl:attribute>
</link>

<link rel="shortcut icon" href="/webfilesys/images/favicon.ico" />

<title>
  WebFileSys: 
  <xsl:value-of select="/login/localHost"/>
  (<xsl:value-of select="/login/operatingSystem"/>)
  -
  <xsl:value-of select="/login/version"/>
</title>

<script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
<script src="/webfilesys/javascript/util.js" type="text/javascript"></script>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/login/language" /></xsl:attribute>
</script>

<script language="javascript">

  function about() {
      infowindow = window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=230,resizable=no,left=250,top=150,screenX=250,screenY=150');
      infowindow.focus();
  }

  function setFocus() {
      document.passwordform.userid.focus();
  }

  function doLogin() {
      document.getElementById("screenWidth").value = screen.availWidth;
      document.passwordform.submit();
  }

  <xsl:if test="/login/activationSuccess">
    customAlert(resourceBundle["activationSuccessful"]);
  </xsl:if>

</script>

</head>

<body onload="setFocus()">

<div class="centerBox">
  <div class="loginBox">
    <table border="0" cellpadding="5" cellspacing="0" width="100%">
      <tr>
        <td class="loginTitle" style="padding-left:10px">
          <img src="/webfilesys/images/logo.gif" border="0" style="margin-top:20px" />
          <div style="width:100%;padding-top:10px;padding-left:0px;" resource="label.login.title">
          </div>
        </td>
        
        <td>
          <form accept-charset="utf-8" name="passwordform" method="post" action="/webfilesys/servlet">
            <input type="hidden" name="command" value="login" />
            <input type="hidden" id="screenWidth" name="screenWidth" value="" />
          
            <table border="0" cellpadding="5" cellspacing="0" width="100%">
              <tr>
                <td colspan="2">
                  &#160;
                </td>
              </tr>
              <tr>
                <td class="loginFormLabel">
                  <label for="userid" resource="label.userid" />:
                </td>
                <td class="value">
                  <input type="text" id="userid" name="userid" maxlength="64" style="width:100px;" required="required"/>
                </td>
              </tr>
              <tr>
                <td class="loginFormLabel">
                  <label for="password" resource="label.password" />:
                </td>
                <td class="value">
                  <input type="password" id="password" name="password" maxlength="64" style="width:100px;" required="required"/>
                </td>
              </tr>
              <tr>
                <td>
                  &#160;
                </td>
                <td>
                  <input type="button" onclick="doLogin()" resource="label.logon" class="loginButton" />
                </td>
              </tr>
              <tr>
                <td colspan="2" style="text-align:right;padding-right:10px;">
                  <xsl:if test="/login/openRegistration">
                    <a class="dir" href="/webfilesys/servlet?command=registerSelf" resource="label.registerself"></a>
                  </xsl:if>
                </td>
              </tr>
              <tr>
                <td colspan="2" style="text-align:right;padding-right:10px;">
                  <a class="dir" href="javascript:about()" resource="label.about"></a>
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

<script type="text/javascript">
  setBundleResources();
    
  <xsl:if test="/login/authFailed">
    customAlert(resourceBundle["alert.invalidlogin"]);
  </xsl:if>
    
</script>

</html>

</xsl:template>

</xsl:stylesheet>

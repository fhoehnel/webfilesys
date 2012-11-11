<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="login" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/login/css" />.css</xsl:attribute>
</link>

<link rel="shortcut icon" href="/webfilesys/images/favicon.ico" />

<title>
  WebFileSys: 
  <xsl:value-of select="/login/localHost"/>
  (<xsl:value-of select="/login/operatingSystem"/>)
  -
  <xsl:value-of select="/login/version"/>
</title>

<script language="javascript">

  function about()
  {
      infowindow = window.open('/webfilesys/servlet?command=versionInfo','infowindow','status=no,toolbar=no,location=no,menu=no,width=300,height=220,resizable=no,left=250,top=150,screenX=250,screenY=150');
      infowindow.focus();
  }

  function setFocus()
  {
      document.passwordform.userid.focus();
  }

  <xsl:if test="/login/resources/msg[@key='alert.invalidlogin']">
    alert('<xsl:value-of select="/login/resources/msg[@key='alert.invalidlogin']/@value" />');
  </xsl:if>

  now=(new Date()).getTime();
  exp=new Date(now +  60000);
  document.cookie='CookieTest=1;';
  idx=document.cookie.indexOf("CookieTest=") + 11;
  cookieValue='';
  if (idx >=0)
  {
    cookieValue=document.cookie.substring(idx,idx+1);
  }
    
  if ((document.cookie==null) || (cookieValue!='1'))
  {
      alert('Cookies must be enabled to login to this web site!');
  }

</script>

</head>

<body onload="setFocus()">

<div class="centerBox">
  <div class="loginBox">
    <table border="0" cellpadding="5" cellspacing="0" width="100%">
      <tr>
        <td class="loginTitle" style="padding-left:10px">
          <img src="/webfilesys/images/logo.gif" border="0" />
          <div style="width:100%;padding-top:10px;padding-left:0px;">
            <xsl:value-of select="/login/resources/msg[@key='label.login.title']/@value" />
          </div>
        </td>
        
        <td>
          <form accept-charset="utf-8" name="passwordform" method="post" action="/webfilesys/servlet">
            <input type="hidden" name="command" value="login" />
          
            <table border="0" cellpadding="5" cellspacing="0" width="100%">
              <tr>
                <td colspan="2">
                  &#160;
                </td>
              </tr>
              <tr>
                <td class="loginFormLabel">
                  <xsl:value-of select="/login/resources/msg[@key='label.userid']/@value" />:
                </td>
                <td class="value">
                  <input type="text" name="userid" size="15" maxlength="64" style="width:100px;" required="required"/>
                </td>
              </tr>
              <tr>
                <td class="loginFormLabel">
                  <xsl:value-of select="/login/resources/msg[@key='label.password']/@value" />:
                </td>
                <td class="value">
                  <input type="password" name="password" size="15" maxlength="64" style="width:100px;" required="required"/>
                </td>
              </tr>
              <tr>
                <td>
                  &#160;
                </td>
                <td>
                  <input type="submit" name="logonbutton">
                    <xsl:attribute name="value"><xsl:value-of select="/login/resources/msg[@key='label.logon']/@value" /></xsl:attribute>
                  </input>
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  &#160;
                </td>
              </tr>
              <tr>
                <td colspan="2" align="right">
                  <xsl:if test="/login/resources/msg[@key='label.registerself']">
                    <a class="dir" href="/webfilesys/servlet?command=registerSelf">
                      <xsl:value-of select="/login/resources/msg[@key='label.registerself']/@value" />
                    </a>
                  </xsl:if>
                  &#160;
                </td>
              </tr>
              <tr>
                <td colspan="2" align="right">
                  <a class="dir" href="javascript:about()">
                    About WebFileSys
                    &#160;
                  </a>
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

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="publishFile" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/publishFile/css" />.css</xsl:attribute>
</link>

<title>
  <xsl:value-of select="/publishFile/resources/msg[@key='label.publishFile']/@value" />
</title>

<script language="javascript">
  
  function switchMailFlag()
  {
     if (document.form1.sendMail.checked == true)
     {
         document.form1.recipient.disabled = false;
         document.form1.subject.disabled = false;
         document.form1.msgText.disabled = false;
     }
     else
     {
         document.form1.recipient.value = '';
         document.form1.recipient.disabled = true;
         document.form1.subject.value = '';
         document.form1.subject.disabled = true;
         document.form1.msgText.value = '';
         document.form1.msgText.disabled = true;
     }
  }
  
  <xsl:if test="/publishFile/errorMsg">
    alert('<xsl:value-of select="/publishFile/errorMsg" />');
  </xsl:if>
</script>

</head>

<body>

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="/publishFile/resources/msg[@key='label.publishFile']/@value" />
      </th>
    </tr>
  </table>

  <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet">

    <input type="hidden" name="command" value="publishFile" />
  
    <input type="hidden" name="publishPath">
      <xsl:attribute name="value"><xsl:value-of select="/publishFile/publishPath" /></xsl:attribute>
    </input> 
 
    <table class="dataForm" width="100%">
      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="/publishFile/resources/msg[@key='label.fileToPublish']/@value" />:
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="/publishFile/publishPath" />
        </td>
      </tr>
    
      <tr>
        <td colspan="2" class="formParm1">
          &#160;
        </td>
      </tr>
      
      <xsl:if test="/publishFile/mailEnabled">
        <tr>
          <td colspan="2" class="formParm2">
            <table class="dataForm" width="100%">
      
              <tr>
                <td colspan="2" class="formParm1">
                  <input type="checkbox" class="cb2" name="sendMail" onclick="javascript:switchMailFlag()">
                    <xsl:if test="/publishFile/requestParms/requestParm[@key='sendMail']">
                      <xsl:attribute name="checked">checked</xsl:attribute>
                    </xsl:if>
                  </input>
                  <xsl:value-of select="/publishFile/resources/msg[@key='label.sendInvitationMail']/@value" />
                </td>
              </tr>
            
              <tr>
                <td colspan="2" class="formParm1">
                  <xsl:value-of select="/publishFile/resources/msg[@key='label.receiver']/@value" />:
                </td>
              </tr>
            
              <tr>
                <td colspan="2" class="formParm2">
                  <input type="text" name="recipient" size="40" maxlength="256" style="width:100%">
                    <xsl:if test="/publishFile/requestParms/requestParm[@key='recipient']">
                      <xsl:attribute name="value"><xsl:value-of select="/publishFile/requestParms/requestParm[@key='recipient']" /></xsl:attribute>
                    </xsl:if>
                    <xsl:if test="not(/publishFile/requestParms/requestParm[@key='sendMail'])">
                      <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>

              <tr>
                <td colspan="2" class="formParm1">
                  &#160;
                </td>
              </tr>

              <tr>
                <td colspan="2" class="formParm1">
                  <xsl:value-of select="/publishFile/resources/msg[@key='label.subject']/@value" />:
                </td>
              </tr>

              <tr>
                <td colspan="2" class="formParm2">
                  <input type="text" name="subject" value="" size="40" maxlength="60" style="width:100%">           
                    <xsl:if test="/publishFile/requestParms/requestParm[@key='subject']">
                      <xsl:attribute name="value"><xsl:value-of select="/publishFile/requestParms/requestParm[@key='subject']" /></xsl:attribute>
                    </xsl:if>
                    <xsl:if test="not(/publishFile/requestParms/requestParm[@key='sendMail'])">
                      <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </tr>
      
              <tr>
                <td colspan="2" class="formParm1">
                  &#160;
                </td>
              </tr>

              <tr>
                <td colspan="2" class="formParm1">
                  <xsl:value-of select="/publishFile/resources/msg[@key='label.invitationtext']/@value" />:
                </td>
              </tr>

              <tr>
                <td colspan="2" class="formParm2">
                  <textarea name="msgText" rows="3" cols="50" style="width:100%">
                    <xsl:if test="not(/publishFile/requestParms/requestParm[@key='sendMail'])">
                      <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:value-of select="/publishFile/requestParms/requestParm[@key='msgText']" />
                  </textarea>
                </td>
              </tr>

            </table>
        
          </td>
        </tr>
      </xsl:if>

      <tr>
        <td colspan="2" class="formParm1">
          &#160;
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="/publishFile/resources/msg[@key='label.expiration']/@value" />:
          &#160;
          <input type="text" name="expiration" size="4" maxlength="4">
            <xsl:if test="/publishFile/requestParms/requestParm[@key='expiration']">
              <xsl:attribute name="value"><xsl:value-of select="/publishFile/requestParms/requestParm[@key='expiration']" /></xsl:attribute>
            </xsl:if>
          </input>
        </td>
      </tr>

      <tr>
        <td class="formButton">
          <a class="button" href="#"> 
            <xsl:attribute name="onclick">this.blur();document.form1.submit()</xsl:attribute>
            <span><xsl:value-of select="/publishFile/resources/msg[@key='button.publish']/@value" /></span>
          </a>              
        </td>
        <td class="formButton" style="text-align:right">
          <a class="button" href="#" style="float:right"> 
            <xsl:attribute name="onclick">this.blur();self.close();</xsl:attribute>
            <span><xsl:value-of select="/publishFile/resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
      </tr>

    </table>
  </form>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

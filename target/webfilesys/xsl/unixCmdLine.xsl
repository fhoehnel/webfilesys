<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="cmdLine" />

<xsl:template match="/cmdLine">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/cmdLine/css" />.css</xsl:attribute>
  </link>

  <title><xsl:value-of select="resources/msg[@key='label.cmdhead']/@value" /></title>

  <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/unixCmdLine.js" type="text/javascript"></script>

  <script type="text/javascript">
    function sendOnEnter()
    {
        if (event.keyCode == 13)
        {
            submitCmd();
            return true;
        }
    }  
    
    function cleanup()
    {
        document.form1.unixCmd.value='';
        var cmdOutDiv = document.getElementById('cmdOutput');
        cmdOutDiv.innerHTML = '';
    }
  </script>

</head>

<body>

  <div class="headline">
    <xsl:value-of select="resources/msg[@key='label.cmdprompt']/@value" />
  </div>

  <form accept-charset="utf-8" name="form1" method="post" action="javascript:submitCmd()">
  
    <input type="hidden" name="command" value="runUnixCmd" />

    <table class="dataForm" style="width:100%">
      
      <tr>
        <td colspan="2" class="formParm2">
          <table border="0" style="width:100%">
            <tr>
              <td style="width:90%">
                <input type="text" name="unixCmd" value="" style="width:100%" onkeyup="sendOnEnter()"/>
              </td>
              <td style="padding-left:10px;">
                <input type="button" onclick="cleanup()">
                  <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.clearCmd']/@value" /></xsl:attribute>
                </input>                
              </td>
            </tr>
          </table>
        </td>
      </tr>
      
      <tr>
        <td class="formButton">
          <input type="button" onclick="javascript:submitCmd();">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.run']/@value" /></xsl:attribute>
          </input>
        </td>
       
        <td class="formButton" style="text-align:right;">
          <input type="button" name="cancel" onclick="self.close();">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
          </input>
        </td>
      </tr>
     
    </table>
    
    <div id="cmdOutput" style="width:100%"></div>
  </form>
  
</body>

</html>

</xsl:template>

</xsl:stylesheet>

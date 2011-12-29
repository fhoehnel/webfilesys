<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/folder/css" />.css</xsl:attribute>
</link>

<script language="JavaScript" src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>

<script language="javascript">

  <xsl:if test="/folder/errorMsg">
    alert('<xsl:value-of select="/folder/errorMsg" />');
  </xsl:if>
  
  function validate()
  {
      if (checkFileNameSyntax(document.form1.newDirName.value))
      {
          document.form1.submit();
          
          return;
      }
      
      alert('<xsl:value-of select="/folder/resources/msg[@key='alert.illegalCharInFilename']/@value" />');
      
      document.form1.newDirName.focus();
      
      document.form1.newDirName.select();
  }
  
</script>

</head>

<body>

  <table border="0" width="100%" cellpadding="2" cellspacing="0">
    <tr>
      <th class="headline">
        <xsl:value-of select="/folder/resources/msg[@key='label.renamedir']/@value" />
      </th>
    </tr>
  </table>

  <form accept-charset="utf-8" name="form1" method="get" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="renameDir" />

    <input type="hidden" name="path">
      <xsl:attribute name="value"><xsl:value-of select="/folder/currentPath" /></xsl:attribute>
    </input> 
 
    <table class="dataForm" width="100%">
      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="/folder/resources/msg[@key='label.currentName']/@value" />:
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="/folder/currentName" />
        </td>
      </tr>
    
      <tr>
        <td colspan="2" class="formParm1">
          &#160;
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="/folder/resources/msg[@key='label.newname']/@value" />:
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <input type="text" name="newDirName" size="30" maxlength="256" style="width:100%">
            <xsl:if test="/folder/newDirName">
              <xsl:attribute name="value"><xsl:value-of select="/folder/newDirName" /></xsl:attribute>
            </xsl:if>
            <xsl:if test="not(/folder/newDirName)">
              <xsl:attribute name="value"><xsl:value-of select="/folder/currentName" /></xsl:attribute>
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
        <td class="formButton">
          <a class="button" href="#"> 
            <xsl:attribute name="onclick">this.blur();validate()</xsl:attribute>
            <span><xsl:value-of select="/folder/resources/msg[@key='button.rename']/@value" /></span>
          </a>              
        </td>

        <td class="formButton">
          <a class="button" href="#" style="float:right"> 
            <xsl:attribute name="onclick">this.blur();window.location.href='/webfilesys/servlet?command=exp&amp;expandPath=<xsl:value-of select="/folder/encodedPath" />'</xsl:attribute>
            <span><xsl:value-of select="/folder/resources/msg[@key='button.cancel']/@value" /></span>
          </a>              
        </td>
      </tr>
    </table>
  </form>

  <script language="javascript">
    document.form1.newDirName.focus();
    document.form1.newDirName.select();
  </script>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

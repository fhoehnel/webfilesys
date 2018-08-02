<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="categoryList category" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<title>WebFileSys: <xsl:value-of select="/categoryList/resources/msg[@key='label.manageCategories']/@value" /></title>

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/categoryList/css" />.css</xsl:attribute>
</link>

<script language="javascript">
  function anySelected()
  {
      for (i=document.form1.elements.length-1;i>=0;i--)
      {
           if ((document.form1.elements[i].type=="checkbox") &amp;&amp;
               (document.form1.elements[i].checked==true))
	   {
	        return(true);
	   }
      }

      return(false);
  }


  function deleteSelected()
  {
      if (!anySelected())
      {
          alert("<xsl:value-of select="/categoryList/resources/msg[@key='alert.noCategorySelected']/@value" />");
          return;
      }      
  
      if (confirm("<xsl:value-of select="/categoryList/resources/msg[@key='confirm.delCategory']/@value" />"))
      {
          document.form1.cmd.value = 'delete';
          document.form1.submit();
      }
  }

  <xsl:if test="/categoryList/resources/msg[@key='error.duplicateCategory']">
    alert('<xsl:value-of select="/categoryList/resources/msg[@key='error.duplicateCategory']/@value" />');
  </xsl:if>

  <xsl:if test="/categoryList/resources/msg[@key='error.missingCategoryName']">
    alert('<xsl:value-of select="/categoryList/resources/msg[@key='error.missingCategoryName']/@value" />');
  </xsl:if>

</script>

</head>

<body class="categories">

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="categoryList">

  <div class="headline">
    <xsl:value-of select="resources/msg[@key='label.manageCategories']/@value" />
  </div>

  <br/>

  <form accept-charset="utf-8" name="form0" method="post" action="/webfilesys/servlet">
    <input type="hidden" name="command" value="category" />
    <input type="hidden" name="cmd" value="new" />
    <input type="hidden" name="filePath">
      <xsl:attribute name="value"><xsl:value-of select="/categoryList/filePath" /></xsl:attribute>
    </input>

    <table class="dataForm" width="100%">

      <tr>
        <td class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.newCategory']/@value" />:
        </td>
        <td class="formParm2">
          <input type="text" name="newCategory" style="width:140px">
            <xsl:if test="category">
              <xsl:attribute name="value"><xsl:value-of select="/categoryList/newCategory" /></xsl:attribute>
            </xsl:if>
          </input>
        </td>
        <td nowrap="nowrap">
          <input type="button" onclick="document.form0.submit()">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.createCategory']/@value" /></xsl:attribute>
          </input>
        </td>

      </tr>
      
    </table>

  </form>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
    <input type="hidden" name="command" value="category" />
    <input type="hidden" name="cmd" value="new" />
    <input type="hidden" name="filePath">
      <xsl:attribute name="value"><xsl:value-of select="/categoryList/filePath" /></xsl:attribute>
    </input>

    <br />

    <table class="dataForm" border="0" width="100%" cellpadding="0" cellspacing="0">

      <xsl:for-each select="category">
        <tr>
          <td class="fileListData sepTop">
            <input type="checkbox" name="categoryId" class="cb2">
              <xsl:attribute name="value"><xsl:value-of select="@id" /></xsl:attribute>
            </input>
          </td>
          <td class="fileListData sepTop" width="95%">
            <xsl:value-of select="name" />
          </td>
        </tr>
      </xsl:for-each>

 
      <xsl:if test="not(category)">
        <tr>
          <td class="formParm2" style="padding:10px;">
            <xsl:value-of select="resources/msg[@key='label.noCategoryDefined']/@value" />
          </td>
        </tr>
      </xsl:if>
    
    </table>

    <br/>

    <div class="buttonCont">

      <xsl:if test="category">
        <input type="button" onclick="deleteSelected()">
          <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.delete']/@value" /></xsl:attribute>
        </input>
      </xsl:if>

      <input type="button" style="float:right">
        <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=assignCategory&amp;filePath=<xsl:value-of select="/categoryList/filePathForScript" />'</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.return']/@value" /></xsl:attribute>
      </input>
    
    </div>

  </form>
  
</xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="categoryList category" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<title>WebFileSys: <xsl:value-of select="/categoryList/resources/msg[@key='label.assignCategories']/@value" /></title>

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/categoryList/css" />.css</xsl:attribute>
</link>

<script language="javascript">

  function assign()
  {
      document.form1.cmd.value="assign";
      document.form1.submit();
  }

  function unassign()
  {
      document.form1.cmd.value="unassign";
      document.form1.submit();
  }
  
  function manageCategories()
  {
      document.form1.command.value = 'category';
      document.form1.cmd.value = 'list';
      document.form1.submit();
  }
  
</script>

</head>

<body>

<xsl:apply-templates />

</body>
</html>

</xsl:template>
<!-- end root node-->

<xsl:template match="categoryList">

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
    <input type="hidden" name="command" value="assignCategory" />
    <input type="hidden" name="cmd" value="list" />
    <input type="hidden" name="filePath">
      <xsl:attribute name="value"><xsl:value-of select="filePath" /></xsl:attribute>
    </input>

    <table border="0" width="100%" cellpadding="2" cellspacing="0">
      <tr>
        <th class="headline">
          <xsl:value-of select="resources/msg[@key='label.assignCategories']/@value" />
        </th>
      </tr>
    </table>

    <br />

    <table class="dataForm" width="100%">
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="shortFilePath" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">

          <xsl:if test="category">
            <table border="0" width="100%" cellpadding="3" cellspacing="0">
      
              <tr>
                <td class="formParm1" align="center">
                  <xsl:value-of select="resources/msg[@key='label.assignedCats']/@value" />:
                </td>
                <td>
                  &#160;
                </td>
                <td class="formParm1" align="center">
                  <xsl:value-of select="resources/msg[@key='label.unassignedCats']/@value" />:
                </td>
              </tr>

              <tr>
                <td align="center">
                  <select name="assigned" size="6" style="width:180px;height:200px;">
          
                    <xsl:for-each select="category">

                      <xsl:if test="assigned">
                        <option>
                          <xsl:attribute name="value"><xsl:value-of select="@id" /></xsl:attribute>
                          <xsl:value-of select="name" />
                        </option>
                      </xsl:if>      

                    </xsl:for-each>

                  </select>
                </td>
        
                <td valign="center" align="center">
                  <input type="button" value="&lt;&lt;" style="font-weight:bold;font-family:Courier;" onclick="assign()" />
                  <br/> 
                  <br/> 
                  <input type="button" value="&gt;&gt;" style="font-weight:bold;font-family:Courier;" onclick="unassign()" />
                </td>
        
                <td align="center">
                  <select name="unassigned" size="6" style="width:180px;height:200px;">
          
                    <xsl:for-each select="category">

                      <xsl:if test="not(assigned)">
                        <option>
                          <xsl:attribute name="value"><xsl:value-of select="@id" /></xsl:attribute>
                          <xsl:value-of select="name" />
                        </option>
                      </xsl:if>      

                    </xsl:for-each> 
                  </select>
                </td>
        
              </tr>
            </table>
          
    
          </xsl:if>

          <xsl:if test="not(category)">
            <div style="padding:10px;">
              <xsl:value-of select="resources/msg[@key='label.noCategoryDefined']/@value" />
            </div>
          </xsl:if>
    
        </td>
      </tr>

      <tr>
        <td class="formButton">
          <a class="button" onclick="this.blur();"> 
            <xsl:attribute name="href">javascript:self.close()</xsl:attribute>
            <span><xsl:value-of select="resources/msg[@key='button.ok']/@value" /></span>
          </a>              
        </td>
        <td class="formButton" nowrap="nowrap" style="text-align:right">
          <a class="button" onclick="this.blur();" style="float:right"> 
            <xsl:attribute name="href">javascript:manageCategories()</xsl:attribute>
            <span><xsl:value-of select="resources/msg[@key='button.manageCategories']/@value" /></span>
          </a>       
        </td>
      </tr>
      
    </table>
    
  </form>
  
</xsl:template>

</xsl:stylesheet>

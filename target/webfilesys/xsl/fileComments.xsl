<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="fileComments" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/fileComments/css" />.css</xsl:attribute>
</link>

<title>
  <xsl:value-of select="/fileComments/resources/msg[@key='label.commentList']/@value" />
</title>

<script language="javascript">
  function limitText()
  {  
      if (document.form1.newComment.value.length > 2048)
      {  
          document.form1.newComment.value=document.form1.newComment.value.substring(0,2048);
      }
  }
  
  function confirmDelete()
  {  
      if (confirm('<xsl:value-of select="/fileComments/resources/msg[@key='confirm.delcomments']/@value" />'))
      { 
          window.location.href='/webfilesys/servlet?command=delComments&amp;actPath=<xsl:value-of select="/fileComments/encodedPath" />';
      }
  }
</script>


</head>

<body>

  <div class="headline">
    <xsl:value-of select="/fileComments/shortPath" />
  </div>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="addComment" />
    
    <input type="hidden" name="actPath">
      <xsl:attribute name="value"><xsl:value-of select="/fileComments/path" /></xsl:attribute>
    </input>

    <table class="dataForm" width="100%">
   
      <xsl:if test="not(/fileComments/comments) or not(/fileComments/comments/comment)">
        <tr>
          <td class="formParm1">
            <xsl:value-of select="/fileComments/resources/msg[@key='label.nocomments']/@value" />
          </td>
        </tr>
      </xsl:if>

      <xsl:if test="/fileComments/comments and /fileComments/comments/comment">
            
        <xsl:for-each select="/fileComments/comments/comment">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="user" />
              (<xsl:value-of select="date" />)
            </td>
          </tr>
              
          <tr>
            <td class="formParm2">
              <xsl:value-of select="msg" />
            </td>
          </tr>
   
          <tr><td class="formParm1">&#160;</td></tr>
    
        </xsl:for-each>

      </xsl:if>
    
      <tr>
        <td class="formParm1">
          <xsl:value-of select="/fileComments/resources/msg[@key='label.addcomment']/@value" />:
        </td>
      </tr>

      <tr>
        <td class="formParm2">
          <textarea name="newComment" cols="100" rows="4" wrap="virtual" style="width:100%" onKeyup="limitText()" onChange="limitText()"></textarea>
        </td>
      </tr>
      
      <xsl:if test="/fileComments/virtualUser">
        <tr>
          <td class="formParm1">
            <xsl:value-of select="/fileComments/resources/msg[@key='label.commentAuthor']/@value" />:
            &#160;
            <input type="text" name="author" style="width:150px" />
          </td>
        </tr>
      </xsl:if>
	  
      <tr>
        <td class="formParm1" style="padding-top:10px;padding-bottom:10px">
        
          <xsl:if test="/fileComments/modifyPermission">
            <a class="button" onclick="this.blur();"> 
              <xsl:attribute name="href">javascript:document.form1.submit()</xsl:attribute>
              <span><xsl:value-of select="/fileComments/resources/msg[@key='button.addComment']/@value" /></span>
            </a>              

            <xsl:if test="not(/fileComments/readonly)">
              <xsl:if test="/fileComments/comments and /fileComments/comments/comment">              
                <a class="button" onclick="this.blur();"> 
                  <xsl:attribute name="href">javascript:confirmDelete();</xsl:attribute>
                  <span><xsl:value-of select="/fileComments/resources/msg[@key='button.delComments']/@value" /></span>
                </a>
              </xsl:if>  
            </xsl:if>  
              
          </xsl:if>              

          <a class="button" onclick="this.blur();"> 
            <xsl:if test="/fileComments/mobile">
              <xsl:attribute name="href">/webfilesys/servlet?command=mobile&amp;cmd=folderFileList</xsl:attribute>
              <span><xsl:value-of select="/fileComments/resources/msg[@key='button.return']/@value" /></span>
            </xsl:if>
            <xsl:if test="not(/fileComments/mobile)">
              <xsl:attribute name="href">javascript:window.close()</xsl:attribute>
              <span><xsl:value-of select="/fileComments/resources/msg[@key='button.closewin']/@value" /></span>
            </xsl:if>
          </a>              
        </td>
      </tr>
    </table>
    
  </form>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

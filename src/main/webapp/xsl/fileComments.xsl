<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="fileComments" />

<!-- root node-->
<xsl:template match="/">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/fileComments/css" />.css</xsl:attribute>
  </link>

  <title resource="label.commentList"></title>

  <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
  <script type="text/javascript">
    <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/fileComments/language" /></xsl:attribute>
  </script>

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
      if (confirm(resourceBundle["confirm.delcomments"]))
      { 
          window.location.href='/webfilesys/servlet?command=delComments&amp;actPath=<xsl:value-of select="/fileComments/encodedPath" />';
      }
  }
  
  <xsl:if test="/fileComments/modifyPermission">
    function refreshParentWindow() {
        window.opener.location.reload();
    }
    
    window.onbeforeunload = refreshParentWindow;  
  </xsl:if>
</script>

</head>

<body>

  <div class="headline">
    <xsl:if test="/fileComments/shortPath">
      <xsl:value-of select="/fileComments/shortPath" />
    </xsl:if>
    <xsl:if test="not(/fileComments/shortPath)">
      <span resource="label.commentList"></span>
    </xsl:if>
  </div>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="addComment" />
    
    <input type="hidden" name="actPath">
      <xsl:attribute name="value"><xsl:value-of select="/fileComments/path" /></xsl:attribute>
    </input>

    <table class="dataForm" width="100%">
   
      <xsl:if test="not(/fileComments/comments) or not(/fileComments/comments/comment)">
        <tr>
          <td class="formParm1" resource="label.nocomments"></td>
        </tr>
      </xsl:if>

      <xsl:if test="/fileComments/comments and /fileComments/comments/comment">
            
        <xsl:for-each select="/fileComments/comments/comment">
          <tr>
            <td class="formParm1">
              <xsl:value-of select="user" />,&#160;
              <xsl:value-of select="date" />
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
    
      <xsl:if test="/fileComments/modifyPermission">
        <tr>
          <td class="formParm1">
            <span resource="label.addcomment"></span>:
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
              <span resource="label.commentAuthor"></span>:
              &#160;
              <input type="text" name="author" style="width:150px" />
            </td>
          </tr>
        </xsl:if>
      </xsl:if>
	  
      <tr>
        <td>
          <div class="buttonCont">
        
            <xsl:if test="/fileComments/modifyPermission">
			
              <input type="button" resource="button.addComment">
                <xsl:attribute name="onclick">javascript:document.form1.submit()</xsl:attribute>
              </input>              

              <xsl:if test="not(/fileComments/readonly)">
                <xsl:if test="/fileComments/comments and /fileComments/comments/comment"> 
                  <input type="button" resource="button.delComments">
                    <xsl:attribute name="onclick">javascript:confirmDelete()</xsl:attribute>
                  </input>              
                </xsl:if>  
              </xsl:if>  
              
            </xsl:if>              

            <xsl:if test="/fileComments/mobile">
              <input type="button" resource="button.return">
                <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=mobile&amp;cmd=folderFileList';</xsl:attribute>
              </input>
            </xsl:if>
            <xsl:if test="not(/fileComments/mobile)">
              <input type="button" resource="button.closewin">
                <xsl:attribute name="onclick">window.close()</xsl:attribute>
              </input>
            </xsl:if>

		  </div>
        </td>
      </tr>
    </table>
    
  </form>

</body>

<script type="text/javascript">
  setBundleResources();
</script>

</html>

</xsl:template>

</xsl:stylesheet>

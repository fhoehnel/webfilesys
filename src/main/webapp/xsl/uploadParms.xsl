<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="upload" />

<!-- root node-->
<xsl:template match="/upload">

<html>
<head>

  <meta http-equiv="expires" content="0" />

  <link rel="stylesheet" type="text/css">
    <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="css" />.css</xsl:attribute>
  </link>

  <script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/fmweb.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/ajaxUpload.js" type="text/javascript"></script>
  <script language="JavaScript" src="/webfilesys/javascript/util.js" type="text/javascript"></script>

  <script language="javascript">
    var resourceLabelOf = '<xsl:value-of select="resources/msg[@key='label.of']/@value" />';
  
    var uploadStatus=0;
    
    function openStatusWindow()
    {  
        if (checkFileSelected())
        {  
            var targetFileName = document.form1.serverFileName.value;
            
            if (targetFileName == "") 
            {
                targetFileName = document.form1.uploadfile.value;
            }
            
            if (!checkFileNameSyntax(targetFileName))
            {
                alert('<xsl:value-of select="resources/msg[@key='alert.illegalCharInFilename']/@value" />');
                return;
            }
            
            if (existUploadTargetFile(targetFileName))
            {
                if (!confirm(targetFileName + ': <xsl:value-of select="resources/msg[@key='upload.file.exists']/@value" />')) 
                {
                    return;
                }
            }
            document.form1.destFileName.value = document.form1.serverFileName.value;
            document.form1.description.value = replaceNewline(document.form1.desc.value);
            document.form1.serverFileName.disabled = true;
            document.form1.desc.disabled = true;
            document.getElementById('uploadButton').style.visibility ='hidden';
            
            var statusBox = document.getElementById('uploadStatus');
            centerBox(statusBox);
            statusBox.style.visibility='visible';
            
            document.form1.submit();
            window.setTimeout("getUploadStatus()", 1000);
        }
    }

    function replaceNewline(text) 
    {
        var cleanText = "";
        
        var textLength = text.length;
        for (i = 0; i &lt; textLength; i++)
        {
             var c = text.charAt(i);
             if ((c == '\n') || (c == '\r'))
             {
                 cleanText = cleanText + ' ';
             }
             else
             {
                 cleanText = cleanText + c;
             }
        }
        return cleanText;
    }

    function closeStatusWindow()
    {   
        if (uploadStatus!=0) 
        {
            uploadStatus.close();
        }
    }
    
    function checkFileSelected()
    {  
        if (document.form1.uploadfile.value.length==0)
        {  
            alert('<xsl:value-of select="resources/msg[@key='alert.nofileselected']/@value" />');
            return(false);
        }
   
        return(true);
    }
    
    function showHideZipParms()
    {  
        if (document.form1.uploadfile.value.length &lt; 5)
        {
            return(true);
        }
        
        ext = document.form1.uploadfile.value.substr(document.form1.uploadfile.value.length - 4, 4).toLowerCase();
   
        if (ext=='.zip')
        {
            document.getElementById('unzipDiv').style.visibility='visible';
        }
        else
        {
            document.getElementById('unzipDiv').style.visibility='hidden';
        }
    }

    function setUnzipParm()
    {  
        if (document.form1.unzipFlag.checked)
        {
            document.form1.unzip.value='true';
            document.form1.serverFileName.value='';
            document.form1.desc.value='';
        }
        else
        {
            document.form1.unzip.value='false';
        }
    }
    
    function showMultiUploadLink() 
    {
        try
        {
            var featureTest = new FileReader();
            if (featureTest != null) 
            {
                document.getElementById('linkMultiUpload').style.visibility = 'visible';
            } 
        }
        catch (Exception)
        {
            if (browserChrome || browserSafari) 
            {
                document.getElementById('linkMultiUpload').style.visibility = 'visible';
            }
        }
    }
  </script>

</head>

<body onload="showMultiUploadLink()">

  <div class="headline">
    <xsl:value-of select="resources/msg[@key='label.uploadfile']/@value" />
  </div>

  <form accept-charset="utf-8" name="form1" method="post" action="/webfilesys/upload" enctype="multipart/form-data">
  
    <input type="hidden" name="actpath">
      <xsl:attribute name="value"><xsl:value-of select="currentPath" /></xsl:attribute>
    </input>
    <input type="hidden" name="unzip" value="false" />
    <input type="hidden" name="destFileName" value="" />
    <input type="hidden" name="description" value="" />

    <table class="dataForm" width="100%">

      <tr>
        <td colspan="2" class="formParm1">
          <font class="small">
            <xsl:value-of select="resources/msg[@key='hint.upload.multizip']/@value" />
          </font>
        </td>
      </tr>

      <tr><td colspan="2">&#160;</td></tr>

      <tr>
        <td class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.destdir']/@value" />:
        </td>
        <td style="text-align:right;padding-right:20px;">
          <input id="linkMultiUpload" type="button" style="visibility:hidden">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='link.multiUpload']/@value" /></xsl:attribute>
            <xsl:attribute name="onclick">window.location.href='/webfilesys/servlet?command=multiUpload';</xsl:attribute>
          </input>
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="shortPath" />
        </td>
      </tr>

      <tr><td colspan="2">&#160;</td></tr>

      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.upload.localFile']/@value" />:
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <input type="file" name="uploadfile" size="60" class="formParm1" style="width:100%" onchange="showHideZipParms()" />
        </td>
      </tr>

      <tr><td colspan="2">&#160;</td></tr>

      <tr>
        <td class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.upload.serverFileName']/@value" />:
        </td>
        <td class="formParm2" width="70%">
          <input type="text" name="serverFileName" maxlength="128" style="width:100%" />
        </td>
      </tr>

      <tr>
        <td class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.upload.description']/@value" />:
        </td>
        <td class="formParm2">
          <textarea name="desc" rows="3" cols="40" style="width:100%" wrap="virtual"></textarea>
        </td>
      </tr>
      
      <tr>
        <td colspan="2" class="formParm1">
          <div id="unzipDiv" style="visibility:hidden">
            <input type="checkbox" class="cb3" name="unzipFlag" onclick="setUnzipParm()" />
            <xsl:value-of select="resources/msg[@key='label.upload.unzip']/@value" />
          </div>
        </td>
      </tr>
      
      <tr>
        <td class="formButton" id="uploadButton">
          <input type="button">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.startUpload']/@value" /></xsl:attribute>
            <xsl:attribute name="onclick">javascript:openStatusWindow();return false;</xsl:attribute>
          </input>
        </td>
        
        <td class="formButton" align="right">
          <input type="button">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
            <xsl:attribute name="onclick">javascript:window.location.href='/webfilesys/servlet?command=listFiles';</xsl:attribute>
          </input>
        </td>
      </tr>
      
    </table>
  </form>
  
  <img src="/webfilesys/images/bluedot.gif" border="0" width="1" height="1" />
  
</body>

</html>

<div id="uploadStatus" class="uploadStatus" style="visibility:hidden">
  <div class="promptHead">
    <xsl:value-of select="resources/msg[@key='label.uploadStatus']/@value" />
  </div>
  
  <br/><br/>
  <center>

    <div style="width:302px;height:20px;border-style:solid;border-width:1px;border-color:blue;margin:0px;padding:0px;text-align:left;font-size:3px;"><img id="done" src="/webfilesys/images/bluedot.gif" width="1" height="20" border="0"/></div>

    <br/>

    <table border="0" cellspacing="0" cellpadding="0">
      <tr>
        <td class="fileListData">
          <div id="statusText">
            0 <xsl:value-of select="resources/msg[@key='label.of']/@value" /> 0 bytes (0 %)
          </div>
        </td>
      </tr>
    
    </table>
  </center>
  
</div>

</xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

<xsl:strip-space elements="upload" />

<!-- root node-->
<xsl:template match="/upload">

<html>
  <head>
  
    <link rel="stylesheet" type="text/css" href="/webfilesys/css/fmweb.css" />
  
    <style type="text/css">
      img.uploadPreview {height:100px;border:2px ridge #808080;margin:4px;}
      div.dropZone {width:95%;min-height:112px;background-color:#e0e0e0;border:2px ridge #808080;}
      p.dragDropHint {color:#808080;font-family:Arial,Helvetica;font-size:16pt;font-weight:bold;}
      div#dragDropHint {position:relative;left:0px;top:0px;}
      li.selectedFile {color:navy;font-family:Arial,Helvetica;font-size:10pt;}
    </style>
  
    <script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/ajaxUpload.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/util.js" type="text/javascript"></script>
  
    <script type="text/javascript">
    
      var confirmOverwriteText = '<xsl:value-of select="resources/msg[@key='upload.file.exists']/@value" />';
    
      var selectedForUpload = new Array();
      
      var xhr;
      
      var lastUploadedFile;
      
      var uploadStartedByButton = false;
      
      var MAX_PICTURE_SIZE_SUM = 40000000;
      
      var pictureFileSize = 0;
      
      var resourceOf = '<xsl:value-of select="resources/msg[@key='label.of']/@value" />';
      var resourceFileTooLarge = '<xsl:value-of select="resources/msg[@key='upload.file.too.large']/@value" />';
          
      function hideBrowserSpecifics()
      {
          document.getElementById('lastUploaded').style.visibility = 'hidden';
          document.getElementById('lastUploaded').style.display = 'none';
          document.getElementById('selectedForUpload').style.visibility = 'hidden';
          document.getElementById('selectedForUpload').style.display = 'none';
          
          if (browserSafari)
          {
              document.getElementById('dropTarget').style.visibility = 'hidden';
              document.getElementById('dropTarget').style.display = 'none';
          }
      }
          
    </script>

    <script language="JavaScript" src="/webfilesys/javascript/multiUpload.js" type="text/javascript"></script>
  </head>

  <body onload="prepareDropZone();hideBrowserSpecifics();positionStatusDiv();">
    
  <div class="headline">
    <xsl:value-of select="resources/msg[@key='headline.multiUpload']/@value" />
  </div>
    
  <form accept-charset="utf-8" name="form1">
  
    <table class="dataForm" width="100%">

      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="resources/msg[@key='label.destdir']/@value" />:
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="shortPath" />
        </td>
      </tr>

      <tr id="dropTarget">
        <td colspan="2" style="text-align:center;padding:10px;">
          <div id="dropZone" class="dropZone">
            <div id="dragDropHint"><p class="dragDropHint"><xsl:value-of select="resources/msg[@key='upload.dropZone']/@value" /></p></div>
          </div>
        </td>
      </tr>

      <tr id="lastUploaded">
        <td class="formParm1">
          <xsl:value-of select="resources/msg[@key='upload.lastSent']/@value" />:
        </td>
        <td colspan="2" class="formParm2">
          <span id="lastUploadedFile"/>
        </td>
      </tr>

      <tr><td colspan="2">&#160;</td></tr>

      <tr id="selectedForUpload">
        <td colspan="2" class="formParm1">
          <xsl:value-of select="resources/msg[@key='upload.selectedFiles']/@value" />:
          <ul id="uploadFiles"/>
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <xsl:value-of select="resources/msg[@key='upload.selectFilePrompt']/@value" />:
        </td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <input type="file" id="uploadFiles" multiple="true" class="formParm1" style="width:400px;" onchange="handleFiles(this.files)" />        
        </td>
      </tr>

      <tr><td colspan="2">&#160;</td></tr>
      
      <tr>
        <td class="formButton">
          <input id="uploadButton" type="button" onclick="positionStatusDiv();sendFiles()" style="visibility:hidden;display:none;">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.startUpload']/@value" /></xsl:attribute>
          </input>
          <input id="doneButton" type="button" style="visibility:hidden"
                 onclick="window.location.href='/webfilesys/servlet?command=listFiles&amp;keepListStatus=true'">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='upload.button.done']/@value" /></xsl:attribute>
          </input>
        </td>
        
        <td class="formButton" align="right">
          <input type="button" onclick="window.location.href='/webfilesys/servlet?command=listFiles&amp;keepListStatus=true'">
            <xsl:attribute name="value"><xsl:value-of select="resources/msg[@key='button.cancel']/@value" /></xsl:attribute>
          </input>
        </td>
      </tr>
      
    </table>
  </form>
    
  </body>
  
  <div id="uploadStatus" class="uploadStatus" style="visibility:hidden">
    <table border="0" width="100%" cellpadding="2" cellspacing="0">
      <tr>
        <th class="headline" style="border-width:0;border-bottom-width:1px;">
          <xsl:value-of select="resources/msg[@key='label.uploadStatus']/@value" />
        </th>
      </tr>
      
      <tr>
        <td style="text-align:center;padding-top:10px;"><span id="currentFile"></span></td> 
      </tr>
    </table>
  
    <br/><br/>
    <center>

      <div style="width:302px;height:20px;border-style:solid;border-width:1px;border-color:blue;margin:0px;padding:0px;text-align:left;">
        <img id="done" src="/webfilesys/images/bluedot.gif" width="1" height="20" border="0" />
        <img id="todo" src="/webfilesys/images/space.gif" width="299" height="20" border="0" />  
      </div>

      <br/>

      <table border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td class="fileListData">
            <div id="statusText">
              0 
              <xsl:value-of select="resources/msg[@key='label.of']/@value" />
              0 bytes (0 %)
            </div>
          </td>
        </tr>
    
      </table>
    </center>
  
  </div>
  
</html>

</xsl:template>

</xsl:stylesheet>
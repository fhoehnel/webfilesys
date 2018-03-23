<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="upload" />

<!-- root node-->
<xsl:template match="/upload">

<html>
  <head>
  
    <link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />
  
    <link rel="stylesheet" type="text/css">
       <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="css" />.css</xsl:attribute>
    </link>
       
    <style type="text/css">
      img.uploadPreview {height:100px;border:2px ridge #808080;margin:4px;}
      div.dropZone {width:95%;min-height:112px;background-color:#e0e0e0;border:2px ridge #808080;}
      div.dragDropHint {color:#808080;font-family:Arial,Helvetica;font-size:16pt;font-weight:bold;margin-top:45px;}
      div#dragDropHint {position:relative;left:0px;top:0px;}
      li.selectedFile {color:navy;font-family:Arial,Helvetica;font-size:10pt;}
    </style>
  
    <script language="JavaScript" src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/ajaxUpload.js" type="text/javascript"></script>
    <script language="JavaScript" src="/webfilesys/javascript/util.js" type="text/javascript"></script>
    
    <script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
    <script type="text/javascript">
      <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="language" /></xsl:attribute>
    </script>
  
    <script type="text/javascript">
    
      var confirmOverwriteText = resourceBundle["upload.file.exists"];
    
      var selectedForUpload = new Array();
      
      var xhr;
      
      var lastUploadedFile;
      
      var uploadStartedByButton = false;
      
      var MAX_PICTURE_SIZE_SUM = 40000000;
      
      var pictureFileSize = 0;
	  
	  var currentFileNum = 1;
	  
	  var totalSizeSum = 0;
	  
	  var totalLoaded = 0;
	  
	  var sizeOfCurrentFile = 0;
      
      var resourceOf = resourceBundle["label.of"];
      var resourceFileTooLarge = resourceBundle["upload.file.too.large"];
          
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
    
  <div class="headline" resource="headline.multiUpload"></div>
    
  <form accept-charset="utf-8" name="form1">
  
    <table class="dataForm" width="100%">

      <tr>
        <td colspan="2" class="formParm1" resource="label.destdir"></td>
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="shortPath" />
        </td>
      </tr>

      <tr id="dropTarget">
        <td colspan="2" style="text-align:center;padding:10px;">
          <div id="dropZone" class="dropZone">
            <div id="dragDropHint"><div class="dragDropHint" resource="upload.dropZone"></div></div>
          </div>
        </td>
      </tr>

      <tr id="lastUploaded">
        <td class="formParm1" resource="upload.lastSent"></td>
        <td colspan="2" class="formParm2">
          <span id="lastUploadedFile"/>
        </td>
      </tr>

      <tr><td colspan="2">&#160;</td></tr>

      <tr id="selectedForUpload">
        <td colspan="2" class="formParm1">
          <span resource="upload.selectedFiles"></span>:
          <ul id="uploadFiles"/>
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <span resource="upload.selectFilePrompt"></span>:
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
          <input id="uploadButton" type="button" onclick="positionStatusDiv();checkUploadFileConflicts()" resource="button.startUpload" style="visibility:hidden;display:none;" />
          <input id="doneButton" type="button" style="visibility:hidden" resource="upload.button.done"
                 onclick="window.location.href='/webfilesys/servlet?command=listFiles&amp;keepListStatus=true'" />
        </td>
        
        <td class="formButton" align="right">
          <input type="button" onclick="window.location.href='/webfilesys/servlet?command=listFiles&amp;keepListStatus=true'" resource="button.cancel" />
        </td>
      </tr>
      
    </table>
  </form>
    
  </body>
  
  <div id="uploadStatus" class="uploadStatus" style="visibility:hidden">
    <table border="0" width="100%" cellpadding="2" cellspacing="0">
      <tr>
        <th class="headline" style="border-width:0;border-bottom-width:1px;" resource="label.uploadStatus"></th>
      </tr>
    </table>
	
	<div id="currentFile" class="uploadStatusCurrentFile"></div>
  
    <center>

      <div class="uploadStatusBar">
        <img id="done" src="/webfilesys/images/bluedot.gif" width="1" height="20" border="0" />
        <img id="todo" src="/webfilesys/images/space.gif" width="299" height="20" border="0" />  
      </div>

      <table border="0" cellspacing="0" cellpadding="0" style="width:300px">
        <tr>
          <td class="fileListData">
            <div id="statusText" class="uploadStatusText">
              0 
              <span resource="label.of"></span>
              0 bytes (0 %)
            </div>
          </td>
        </tr>

      </table>
	  
	  <div class="uploadStatusCurrentFile">
	    <span resource="upload.total.status"></span>:
	  </div>
	  
      <div class="uploadStatusBar">
        <img id="totalDone" src="/webfilesys/images/bluedot.gif" width="1" height="20" border="0" />
        <img id="totalTodo" src="/webfilesys/images/space.gif" width="299" height="20" border="0" />  
      </div>

      <table border="0" cellspacing="0" cellpadding="0" style="width:300px">

        <tr>
          <td class="fileListData">
            <div id="statusText" class="uploadStatusText">
			  <span resource="label.file"></span>
			  <xsl:text> </xsl:text>
              <span id="currentFileNum">1</span> 
			  <xsl:text> </xsl:text>
              <span resource="label.of"></span>
			  <xsl:text> </xsl:text>
              <span id="filesToUploadNum"></span>
		    </div>

            <div id="totalStatusText" class="uploadStatusText">
              0 
			  <xsl:text> </xsl:text>
              <span resource="label.of"></span>
			  <xsl:text> </xsl:text>
              0 bytes (0 %)
            </div>

          </td>
        </tr>
    
      </table>
	  
    </center>
  
  </div>
  
  <script type="text/javascript">
    setBundleResources();
  </script>
  
</html>

</xsl:template>

</xsl:stylesheet>
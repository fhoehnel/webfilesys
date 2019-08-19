<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
<xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" 
    doctype-public="html" />

<xsl:strip-space elements="searchParms" />

<xsl:template match="/searchParms">

<html>
<head>

<meta http-equiv="expires" content="0" />

<link rel="stylesheet" type="text/css" href="/webfilesys/styles/common.css" />

<link rel="stylesheet" type="text/css">
  <xsl:attribute name="href">/webfilesys/styles/skins/<xsl:value-of select="/searchParms/css" />.css</xsl:attribute>
</link>

<link rel="stylesheet" href="javascript/jquery-ui-1.12.1.custom/jquery-ui.min.css" />
<link rel="stylesheet" href="javascript/jquery-ui-1.12.1.custom/jquery-ui.structure.min.css" />
<link rel="stylesheet" href="javascript/jquery-ui-1.12.1.custom/jquery-ui.theme.min.css" />

<title resource="label.searchTitle"></title>

<script type="text/javascript" src="javascript/jquery/jquery.min.js"></script>
<script type="text/javascript" src="javascript/jquery-ui-1.12.1.custom/jquery-ui.min.js"></script>

<script src="javascript/browserCheck.js" type="text/javascript"></script>
<script src="javascript/ajaxCommon.js" type="text/javascript"></script>
<script src="javascript/util.js" type="text/javascript"></script>
<script src="javascript/search.js" type="text/javascript"></script>
<script src="javascript/calendar/CalendarPopup.js" type="text/javascript"></script>
<script src="javascript/calendar/AnchorPosition.js" type="text/javascript"></script>
<script src="javascript/calendar/date.js" type="text/javascript"></script>
<script src="javascript/calendar/PopupWindow.js" type="text/javascript"></script>

<script src="/webfilesys/javascript/resourceBundle.js" type="text/javascript"></script>
<script type="text/javascript">
  <xsl:attribute name="src">/webfilesys/servlet?command=getResourceBundle&amp;lang=<xsl:value-of select="/searchParms/language" /></xsl:attribute>
</script>

<style id="calendarStyle">
</style>

</head>

<body onload="setBundleResources()" class="search">

  <div class="headline" resource="label.searchHead" />
  
  <form accept-charset="utf-8" name="findform" method="post" action="/webfilesys/servlet">
  
    <input type="hidden" name="command" value="fmfindfile" />
  
    <input type="hidden" name="actpath">
      <xsl:attribute name="value"><xsl:value-of select="currentPath" /></xsl:attribute>
    </input> 
 
    <table class="dataForm" width="100%">
      <tr>
        <td colspan="2" class="formParm1" resource="label.searchPath" />
      </tr>
      <tr>
        <td colspan="2" class="formParm2">
          <xsl:value-of select="relativePath" />
        </td>
      </tr>
    
      <tr>
        <td class="formParm1" resource="label.filemask" />
        <td class="formParm2">
          <input type="text" name="FindMask" maxlength="256" value="*.*" style="width:250px" />
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.searcharg" />
        <td class="formParm2" id="searchTextList">
          <div class="searchTextCont">
            <input class="searchArg" type="text" name="searchText" maxlength="256" 
              onchange="switchCheckboxes()" onkeyup="switchCheckboxes()" />
            <input type="button" onclick="addSearchTextField()" class="addSearchTextButton" value="+" />
          </div>
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm2" style="padding-left:30px">
          <span resource="label.argdesc1" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <input type="checkbox" name="includeSubdirs" checked="true" class="cb5" />
          &#160;
          <span resource="label.includeSubdirs" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <input id="includeDesc" type="checkbox" name="includeDesc" class="cb5" disabled="disabled" />
          &#160;
          <span resource="label.includemetainf" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <input id="descOnly" type="checkbox" name="descOnly" class="cb5" disabled="disabled"/>
          &#160;
          <span resource="label.metainfonly" />
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.dateRangeFrom" />
        <td class="formParm2">
          <input type="text" id ="dateRangeFrom" name="dateRangeFrom" style="width:80px" readonly="readonly" />
          &#160;
          <a href="javascript:openFromDateSelection()">
            <img src="images/calendar.gif" border="0" />
          </a>
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.dateRangeUntil" />
        <td class="formParm2">
          <input type="text" id ="dateRangeUntil" name="dateRangeUntil" style="width:80px" readonly="readonly" />
          &#160;
          <a href="javascript:openUntilDateSelection()">
            <img src="images/calendar.gif" border="0" />
          </a>
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.assignedToCategory" />
        <td class="formParm2">
          <select name="category">
            <option value="-1" resource="label.selectCategory" />

            <xsl:for-each select="categories/category">
              <option>
                <xsl:attribute name="value"><xsl:value-of select="@name" /></xsl:attribute>
                <xsl:value-of select="@name" />
              </option>
            </xsl:for-each>
          </select>
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm1">
          <input id="resultAsTree" type="checkbox" name="resultAsTree" class="cb5" />
          &#160;
          <span resource="label.searchResultAsTree" />
        </td>
      </tr>

      <tr style="margin-top:10px">
        <td class="formButton">
          <input id="searchButton" type="button" onclick="submitIfValid()" resource="button.startsearch" />
        </td>
        
        <td class="formButton" align="right">
          <input id="cancelButton" type="button" onclick="self.close()" resource="button.cancel" />
        </td>

      </tr>
    </table>
  </form>

  <div id="calDiv" style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></div>

  <script language="javascript">
    document.findform.FindMask.focus();
    document.findform.FindMask.select();
  </script>

</body>

</html>

</xsl:template>

</xsl:stylesheet>

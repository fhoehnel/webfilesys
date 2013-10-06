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

<title resource="label.searchTitle"></title>

<script src="javascript/browserCheck.js" type="text/javascript"></script>
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

<script language="javascript">

  function setCalendarStyles() 
  {
      if (browserFirefox) 
      {
          var calendarCssElem = document.getElementById("calendarStyle");
          calendarCssElem.innerHTML = getCalStyles();
      }
  }

  if (!browserFirefox) 
  {
      document.write(getCalendarStyles());
  }
  
  var cal1x = new CalendarPopup("calDiv");
   
  function selectFromDate()
  {
     cal1x.setReturnFunction("splitFromDate");
     cal1x.select(document.findform.fromYear,'anchorFrom','MM/dd/yyyy')
  }

  function splitFromDate(y,m,d) 
  { 
     document.findform.fromYear.value = y; 
     document.findform.fromMonth.value = LZ(m); 
     document.findform.fromDay.value = LZ(d); 
  }

  function selectToDate()
  {
     cal1x.setReturnFunction("splitToDate");
     cal1x.select(document.findform.toYear,'anchorTo','MM/dd/yyyy')
  }

  function splitToDate(y,m,d) 
  { 
     document.findform.toYear.value = y; 
     document.findform.toMonth.value = LZ(m); 
     document.findform.toDay.value = LZ(d); 
  }

  function checkDate()
  {
      dateInvalid = false;
  
      fromYear = document.findform.fromYear.value;
      toYear = document.findform.toYear.value;

      if (fromYear == '')
      {
          return(true);
      }
      
      if (fromYear &lt; toYear)
      {
          return(true);
      }

      if (fromYear &gt; toYear)
      {
          return(false);
      }
      
      fromMonth = document.findform.fromMonth.value;
      toMonth = document.findform.toMonth.value;
      
      if (fromMonth &lt; toMonth)
      {
          return(true);
      }

      if (fromMonth &gt; toMonth)
      {
          return(false);
      }

      fromDay = document.findform.fromDay.value;
      toDay = document.findform.toDay.value;

      if (fromDay &gt; toDay)
      {
          return(false);
      }
      
      return(true);
  }
  
  function submitIfValid()
  {
      if (!checkDate())
      {
          alert(resourceBundle["label.searchDateConflict"]);
          return;
      }
      
      document.getElementById("searchButton").disabled = true;
      document.getElementById("cancelButton").disabled = true;
      
      var resultAsTreeCheckbox = document.getElementById("resultAsTree");
      
      if (resultAsTreeCheckbox.checked) 
      {
          document.findform.command.value = "findFileTree";
      }
      
      document.findform.submit();
  }

  function switchCheckboxes() 
  {
      var searchArgField = document.getElementById("searchArg");
      
      var resultAsTreeCheckbox = document.getElementById("resultAsTree");
      var includeDescCheckbox = document.getElementById("includeDesc");
      var descOnlyCheckbox = document.getElementById("descOnly");
      
      if (searchArgField.value.length == 0)
      {
          resultAsTreeCheckbox.disabled = false;
          includeDescCheckbox.checked = false;
          includeDescCheckbox.disabled = true;
          descOnlyCheckbox.checked = false;
          descOnlyCheckbox.disabled = true;
      }
      else
      {
          resultAsTreeCheckbox.checked = false;
          resultAsTreeCheckbox.disabled = true;
          includeDescCheckbox.disabled = false;
          descOnlyCheckbox.disabled = false;
      }
  }

  <xsl:if test="/folder/errorMsg">
    alert('<xsl:value-of select="/folder/errorMsg" />');
  </xsl:if>
</script>

</head>

<body onload="setBundleResources();setCalendarStyles()">

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
        <td class="formParm2">
          <input id="searchArg" type="text" name="SearchArg" maxlength="256" 
              style="width:250px" onchange="switchCheckboxes()" onkeyup="switchCheckboxes()" />
        </td>
      </tr>

      <tr>
        <td colspan="2" class="formParm2" style="padding-left:30px">
          <span resource="label.argdesc1" />
          <br/>
          <span resource="label.argdesc2" />
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
          <input type="text" name="fromYear" size="4" maxlength="4" style="width:40px" id="fromYear" readonly="readonly"/>
          /
          <input type="text" name="fromMonth" size="2" maxlength="2" style="width:30px" id="fromMonth" readonly="readonly"/>
          /
          <input type="text" name="fromDay" size="2" maxlength="2" style="width:30px" id="fromDay" readonly="readonly"/>
          &#160;
          <a href="#" name="anchorFrom" id="anchorFrom" titleResource="label.searchCalendar">
            <xsl:attribute name="onClick">selectFromDate()</xsl:attribute>
            <img src="images/calendar.gif" border="0" />
          </a>
        </td>
      </tr>

      <tr>
        <td class="formParm1" resource="label.dateRangeUntil" />
        <td class="formParm2">
          <input type="text" name="toYear" size="4" maxlength="4" style="width:40px" readonly="readonly">
            <xsl:attribute name="value"><xsl:value-of select="currentDate/year" /></xsl:attribute>
          </input>
          /
          <input type="text" name="toMonth" size="2" maxlength="2" style="width:30px" readonly="readonly">
            <xsl:attribute name="value"><xsl:value-of select="currentDate/month" /></xsl:attribute>
          </input>
          /
          <input type="text" name="toDay" size="2" maxlength="2" style="width:30px" readonly="readonly">
            <xsl:attribute name="value"><xsl:value-of select="currentDate/day" /></xsl:attribute>
          </input>
          &#160;
          <a href="#" name="anchorTo" id="anchorTo" titleResource="label.searchCalendar">
            <xsl:attribute name="onClick">selectToDate()</xsl:attribute>
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

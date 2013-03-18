<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">	
  <xsl:output method="html" indent="yes" omit-xml-declaration="yes" encoding="UTF-8" />

  <!-- root node-->
  <xsl:template match="/">

    <html>
      <head>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href">/webfilesys/css/<xsl:value-of select="/calendar/css" />.css</xsl:attribute>
        </link>

        <link rel="stylesheet" type="text/css" href="/webfilesys/css/calendar.css" />
	  
        <script src="/webfilesys/javascript/browserCheck.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/sunburstChart.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/appointment/alarm.js" type="text/javascript"></script>
	  
	<script type="text/javascript">  
	
      document.title = "WebFileSys: <xsl:value-of select="/calendar/resources/msg[@key='calendar.titleYear']/@value" />" + " " + "<xsl:value-of select="/calendar/@year" />";	      
	
	  var resourceReminder = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.reminder']/@value" />';
	  var resourceReminderCloseButton = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.reminderCloseButton']/@value" />';

      var resourceDontRemindAgain = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.dontRemindAgain']/@value" />';
      var resourceRemindAgain = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.remindAgain']/@value" />';
      var resourceMinute = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.minute']/@value" />';
      var resourceHour = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.hour']/@value" />';
      var resourceDay = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.day']/@value" />';
      var resourceWeek = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.week']/@value" />';
	
	  function createColorMap(redStart, greenStart, blueStart, redIncr, greenIncr, blueIncr, numColors)
	  {
	      var colorMap = new Array();
	      
	      var redVal = redStart;
	      var greenVal = greenStart;
	      var blueVal = blueStart;
	      
	      for (var i = 0; i &lt; numColors; i++) 
	      {
	          var colorVal = (redVal * 0x10000) + (greenVal * 0x100) + blueVal;  
	          var colorHex = colorVal.toString(16);
	          colorMap.push("#" + colorHex);
	          
	          redVal += redIncr;
	          greenVal += greenIncr;
	          blueVal += blueIncr;
	      }
	      
	      return colorMap;
	  }
	  
	  function createWeekColorMap() {
	      var colorMap = new Array();

	      for (var i = 0; i &lt; 54; i++) 
		  {
		      var redVal = 0x90 + Math.floor(Math.random() * 100);
		      var greenVal = 0x90 + Math.floor(Math.random() * 100);
		      var blueVal = 0x90 + Math.floor(Math.random() * 100);

	          var colorVal = (redVal * 0x10000) + (greenVal * 0x100) + blueVal;  
	          var colorHex = colorVal.toString(16);
	          colorMap.push("#" + colorHex);
		  }

	      return colorMap;
	  }
	  
	  var dayColorMap = 
	  {    
	      "0": createColorMap(0x90, 0x90, 0xb0, 1, 1, 2, 32),
	      "1": createColorMap(0xb0, 0x90, 0x90, 2, 1, 1, 32),
	      "2": createColorMap(0x90, 0xb0, 0x90, 1, 2, 1, 32),
	      "3": createColorMap(0x90, 0xb0, 0xb0, 1, 2, 2, 32),
	      "4": createColorMap(0xb0, 0x90, 0xb0, 2, 1, 2, 32),
	      "5": createColorMap(0x90, 0x90, 0xb0, 1, 1, 2, 32),
	      "6": createColorMap(0x80, 0xa0, 0xc0, 1, 1, 2, 32),
	      "7": createColorMap(0xc0, 0x80, 0xa0, 1, 1, 2, 32),
	      "8": createColorMap(0xa0, 0xc0, 0x80, 1, 1, 2, 32),
	      "9": createColorMap(0xc0, 0xa0, 0x80, 1, 1, 2, 32),
	      "10": createColorMap(0x80, 0xc0, 0xa0, 1, 1, 2, 32),
	      "11": createColorMap(0xa0, 0x80, 0xc0, 1, 1, 2, 32)
	  }
	  
	  var weekColorMap = createWeekColorMap();

	  var monthColorMap = ["#a0c0f0", "#f0a0c0", "#c0f0a0", "#a0f0c0", "#c0a0f0", "#f0c0a0", "#c0c0ff", "#c0ffc0", "#ffc0c0", "#b0e0ff", "#ffb0e0", "#e0ffb0"];
	  
	  var COLOR_YEAR = "#9090f0";
	  
	  var COLOR_TODAY = "#ffff90";
	  
	  var COLOR_SUNDAY = "#ff8080";

	  var COLOR_HOLIDAY = "#ffa0a0";
	  
	  var COLOR_TIME_OF_DAY = "#ffffff";
	  
	  var weekDayMap = 
	  [
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.monday']/@value" />',
          '<xsl:value-of select="/calendar/resources/msg[@key='calendar.tuesday']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.wednesday']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.thursday']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.friday']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.saturday']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.sunday']/@value" />'
	  ]
	  
	  var monthMap =
	  [
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.january']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.february']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.march']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.april']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.may']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.june']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.july']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.august']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.september']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.october']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.november']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.december']/@value" />'
	  ]
		
	  var innerCircleRadius;
	  var shellWidth;

	  function createChart() 
	  {
	      if (browserChrome)
		  {
	          <!-- delay with setTimeout is required for chrome, which reports incorrect window dimensions else -->
              setTimeout('startCreateChart()', 100)	  
		  }
		  else
		  {
		      startCreateChart();
		  }
	  }
	  
	  function startCreateChart()
	  {
  	      windowWidth = getWinWidth();
          windowHeight = getWinHeight();	
          
          centerX = (windowHeight - 100) / 2 + 35;
          centerY = (windowHeight - 100) / 2 + 35;
	    
	      innerCircleRadius = Math.round(((windowHeight - 100) / 2) / 6);
	      shellWidth = Math.round(((windowHeight - 100) - (innerCircleRadius * 2))  / 5.8);
		
	      document.getElementById("svgChartCont").style.width = (windowWidth - 30) + "px";
	      document.getElementById("svgChartCont").style.height = (windowHeight - 30) + "px";
	    
	      document.getElementById("svgChart").style.width = (windowWidth - 30) + "px";
	      document.getElementById("svgChart").style.height = (windowHeight - 30) + "px";
	    
	      document.getElementById("titleBox").style.left = (windowWidth - 200) + "px";
    	     
	      yearTooltipText = "year " +  <xsl:value-of select="/calendar/@year" />;
			  
		  var yearColor = COLOR_YEAR;
		  <xsl:if test="/calendar/@current">
		    yearColor = COLOR_TODAY;
		  </xsl:if>
			  
	      var circleElem = document.createElementNS(svgNS, "circle");
	      circleElem.setAttribute("cx", centerX);
	      circleElem.setAttribute("cy", centerY);
	      circleElem.setAttribute("r", innerCircleRadius);
	      circleElem.setAttribute("fill", yearColor);
	      circleElem.setAttribute("stroke", "#000000");
	      circleElem.setAttribute("stroke-width", "1");
	      document.getElementById("svgChart").appendChild(circleElem);

    	  var yearText = document.createElementNS(svgNS, "text");
	      yearText.setAttribute("x", centerX - 30);
          yearText.setAttribute("y", centerY + 4);
	      yearText.setAttribute("style", "font-family:Arial,Helvetica;font-size:12px;font-weight:bold");
          var yearTextNode = document.createTextNode('<xsl:value-of select="/calendar/resources/msg[@key='calendar.year']/@value" /> <xsl:value-of select="/calendar/@year" />');
	      yearText.appendChild(yearTextNode);
	      document.getElementById("svgChart").appendChild(yearText);
			  
	      var shellSectorStartAngle = 0;
			  
	      <xsl:for-each select="/calendar/monthList/month">
		    <xsl:call-template name="month" />
	      </xsl:for-each>

	      shellSectorStartAngle = 0;

	      <xsl:for-each select="/calendar/weekList/week">
		    <xsl:call-template name="week" />
	      </xsl:for-each>

	      shellSectorStartAngle = 0;

	      <xsl:for-each select="/calendar/weekList/week/day">
		    <xsl:call-template name="day" />
	      </xsl:for-each>
	      
	      setTimeout("checkAlarm()", 1000);
	  }
	</script>
	  
      </head>
      <body onload="createChart()">
	  
        <div id="svgChartCont" style="width:970px;height:670px;background-color:ivory;border:1px solid black">
          <svg id="svgChart" xmlns="http://www.w3.org/2000/svg" version="1.1">
	      </svg>
	    </div>
		
	    <div id="titleBox" style="position:absolute;left:800px;top:60px;width:150px;height:30px;color:#000000;font-family:Arial,Helvetica;font-size:12px;border:1px solid #a0a0a0;padding:10px;visibility:hidden"></div>
	  
	    <a id="prevYearLink" class="calendarYearLink calendarPrevYearLink">
	      <xsl:attribute name="href">/webfilesys/servlet?command=calendar&amp;year=<xsl:value-of select="/calendar/prevYear" /></xsl:attribute>
	      <xsl:value-of select="/calendar/resources/msg[@key='calendar.prevYear']/@value" />
	    </a>  
	    <a id="nextYearLink" class="calendarYearLink calendarNextYearLink">
	      <xsl:attribute name="href">/webfilesys/servlet?command=calendar&amp;year=<xsl:value-of select="/calendar/nextYear" /></xsl:attribute>
	      <xsl:value-of select="/calendar/resources/msg[@key='calendar.nextYear']/@value" />
	    </a>  
      </body>
    </html>
	
  </xsl:template>
  
  <xsl:template name="month">
    var daysInMonth = <xsl:value-of select="@endDay" /> - <xsl:value-of select="@startDay" /> + 1;
  
    var shellSectorSize = daysInMonth * 360 / <xsl:value-of select="/calendar/@days" />;
  
    var monthColor = monthColorMap[<xsl:value-of select="@id" />];
    <xsl:if test="@current">
      monthColor = COLOR_TODAY;
    </xsl:if>
  
    var shellSectorEndAngle = shellSectorStartAngle + shellSectorSize;
	
	var monthName = monthMap[<xsl:value-of select="@id" />];
	
	var monthDetailLink = '/webfilesys/servlet?command=calendar&amp;cmd=month&amp;year=<xsl:value-of select="/calendar/@year" />&amp;month=<xsl:value-of select="@id" />';
	
    shellSector(shellSectorStartAngle, shellSectorEndAngle, 
	            monthColor, 
		        innerCircleRadius, innerCircleRadius + shellWidth,
       	        monthName + ' ' + '<xsl:value-of select="/calendar/@year" />',
       	        monthDetailLink);
				
	sectorLabel(shellSectorStartAngle, shellSectorEndAngle, 
	            innerCircleRadius, innerCircleRadius + shellWidth,
	            "#000000", monthName, "14px");
				
    shellSectorStartAngle = shellSectorEndAngle;
	
  </xsl:template>
  
  <xsl:template name="week">
    var daysInWeek = <xsl:value-of select="@endDay" /> - <xsl:value-of select="@startDay" /> + 1;
  
    var shellSectorSize = daysInWeek * 360 / <xsl:value-of select="/calendar/@days" />;
  
    var shellSectorEndAngle = shellSectorStartAngle + shellSectorSize;

    var colorIdx = <xsl:value-of select="@id" />;
    if ((colorIdx == 0) &amp;&amp; (shellSectorStartAngle > 0)) 
    {
        colorIdx = 52;
    }
    if ((colorIdx == 51) &amp;&amp; (shellSectorStartAngle == 0)) 
    {
        colorIdx = 53;
    }

    var weekColor = weekColorMap[colorIdx];
    <xsl:if test="@current">
      weekColor = COLOR_TODAY;
    </xsl:if>
	
	var tooltipText = '<xsl:value-of select="/calendar/resources/msg[@key='calendar.week']/@value" /> ' + (<xsl:value-of select="@id" /> + 1);
	
	var weekStartDay = <xsl:value-of select="day/@dayOfMonth"/> + 1;
	var weekEndDay = <xsl:value-of select="day[position() = last()]/@dayOfMonth"/> + 1;

	var weekStartMonth = monthMap[<xsl:value-of select="day/@month"/>];
	var weekEndMonth = monthMap[<xsl:value-of select="day[position() = last()]/@month"/>];
	
	tooltipText = tooltipText + ' (' + weekStartMonth + ' ' + weekStartDay + ' <xsl:value-of select="/calendar/resources/msg[@key='calendar.to']/@value" /> ' + weekEndMonth + ' ' + weekEndDay + ')';
	
    shellSector(shellSectorStartAngle, shellSectorEndAngle, 
	            weekColor, 
		        innerCircleRadius + shellWidth, innerCircleRadius + (2 * shellWidth),
       	        tooltipText);
				
	sectorLabel(shellSectorStartAngle, shellSectorEndAngle,
	            innerCircleRadius + shellWidth, innerCircleRadius + (2 * shellWidth),
	            "#000000",
                '<xsl:value-of select="/calendar/resources/msg[@key='calendar.week']/@value" /> ' + (<xsl:value-of select="@id" /> + 1));
				
    shellSectorStartAngle = shellSectorEndAngle;
  </xsl:template>

  <xsl:template name="day">
    var shellSectorSize = 360 / <xsl:value-of select="/calendar/@days" />;
  
    var shellSectorEndAngle = shellSectorStartAngle + shellSectorSize;
	
	var dayColor = dayColorMap['<xsl:value-of select="@month"/>'][<xsl:value-of select="@dayOfMonth"/>];
	
	var weekDay = weekDayMap[<xsl:value-of select="@dayOfWeek" />];
	
	var month = monthMap[<xsl:value-of select="@month" />];
	
	var tooltipText = '';
	
	<xsl:if test="@today">
	  dayColor = COLOR_TODAY;
	  tooltipText = '<xsl:value-of select="/calendar/resources/msg[@key='calendar.today']/@value" />: ';
	</xsl:if>
	
	tooltipText = tooltipText + weekDay + ', ' + (<xsl:value-of select="@dayOfMonth" /> + 1) + ' ' + month + ' <xsl:value-of select="/calendar/@year" />';
	
    shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                dayColor, 
		        innerCircleRadius + (2 * shellWidth), innerCircleRadius + (3 * shellWidth),
       	        tooltipText);
				
	<xsl:if test="@dayOfWeek = '6'">
      shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                  COLOR_SUNDAY, 
		          innerCircleRadius + (2 * shellWidth), innerCircleRadius + (2 * shellWidth) + 10,
       	          '<xsl:value-of select="/calendar/resources/msg[@key='calendar.sunday']/@value" />');
	</xsl:if>
				
	<xsl:if test="@holiday">
      shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                  COLOR_HOLIDAY, 
		          innerCircleRadius + (3 * shellWidth) - 10, innerCircleRadius + (3 * shellWidth),
       	          '<xsl:value-of select="@holiday" />');
	</xsl:if>

	<xsl:if test="@today">
	  var now = new Date();
	  var hours = now.getHours();
	  
	  var innerTimeSectorRadius = innerCircleRadius + (2 * shellWidth);
      var outerTimeSectorRadius = innerTimeSectorRadius + ((hours + 1) * shellWidth / 24);
	  
      shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                  COLOR_TIME_OF_DAY, 
		          innerTimeSectorRadius, outerTimeSectorRadius,
       	          tooltipText);
       	        
      var dayLabelStartAngle = shellSectorStartAngle - 1;
      if (dayLabelStartAngle &lt; 0)
      {
          dayLabelStartAngle = 0;
      }
      var dayLabelEndAngle = shellSectorEndAngle +1;
      if (dayLabelEndAngle > 360)
      {
          dayLabelEndAngle = 360;
      }
      
	  sectorLabel(dayLabelStartAngle, dayLabelEndAngle, 
	              innerCircleRadius + (3 * shellWidth), innerCircleRadius + (3 * shellWidth) + 30,
	              "#000000", <xsl:value-of select="@dayOfMonth" /> + 1);
       	          
    </xsl:if>	
	
    shellSectorStartAngle = shellSectorEndAngle;
  </xsl:template>

</xsl:stylesheet>

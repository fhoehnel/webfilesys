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
        <script src="/webfilesys/javascript/appointment/appointment.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/util.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/ajaxCommon.js" type="text/javascript"></script>
        <script src="/webfilesys/javascript/appointment/alarm.js" type="text/javascript"></script>
	  
	<script type="text/javascript">  
	
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

	      for (var i = 0; i &lt; 7; i++) 
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
	  
	  var selectedDay = new Object();
	  
	  var dayColorMap = createColorMap(0xa0, 0xff, 0xb0, 0, -2, 2, 32);
	  
	  var weekColorMap = ["#a0c0f0", "#f0a0c0", "#c0f0a0", "#a0f0c0", "#c0a0f0", "#f0c0a0"];
	  
	  var COLOR_MONTH = "#9090f0";
	  
	  var COLOR_EXTRA_DAYS = "#e0e0e0";
	  var COLOR_EXTRA_DAYS_BEFORE = "#c0c0d0";
	  var COLOR_EXTRA_DAYS_AFTER = "#d0c0c0";
	  
	  var COLOR_TODAY = "#ffff90";
	  
	  var COLOR_SUNDAY = "#ff8080";

	  var COLOR_SATURDAY = "#ffb0b0";
	  
	  var COLOR_WEEKDAY = "#f0f0f0";

	  var COLOR_HOLIDAY = "#ffa0a0";
	  
	  var COLOR_TIME_OF_DAY = "#ffffff";
	  
	  var COLOR_APPOINTMENT = "#ffb0b0";
	  var COLOR_APP_REPEAT_DAILY = "#f0e0e0";
	  var COLOR_APP_REPEAT_OTHER = "#ffc080";
      var COLOR_APP_FULLDAY = "#ffd0c0";
	  
	  var monthAppointments = {};
	  
	  var resourceStartTime = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.startTime']/@value" />';
	  var resourceEndTime = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.endTime']/@value" />';
	  var resourceSubject = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.subject']/@value" />';
	  var resourceDescription = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.description']/@value" />';
	  
	  var resourceFullDay = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.fullDay']/@value" />';
	  var resourceMultiDay = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.multiDay']/@value" />';
	  var resourceNumOfDays = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.numOfDays']/@value" />';
	  
	  var resourceRepeatPeriod = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatPeriod']/@value" />';
	  var resourceRepeatNone = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatNone']/@value" />';
	  var resourceRepeatDaily = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatDaily']/@value" />';
	  var resourceRepeatWeekday = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatWeekday']/@value" />';
	  var resourceRepeatWeekly = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatWeekly']/@value" />';
	  var resourceRepeatMonthly = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatMonthly']/@value" />';
	  var resourceRepeatYearly = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.repeatYearly']/@value" />';

	  var resourceAlarmType = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmType']/@value" />';
	  var resourceAlarmNone = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmNone']/@value" />';
	  var resourceAlarmVisual = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmVisual']/@value" />';
	  var resourceAlarmSound = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmSound']/@value" />';
	  var resourceAlarmMail = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmMail']/@value" />';
	  var resourceAlarmAll = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmAll']/@value" />';

	  var resourceAlarmAhead = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.alarmAhead']/@value" />';

	  var resourceDuration = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.duration']/@value" />';

	  var resourceButtonCreate = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.buttonCreate']/@value" />';
	  var resourceButtonSave = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.buttonSave']/@value" />';
	  var resourceButtonDelete = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.buttonDelete']/@value" />';
	  var resourceButtonCancel = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.buttonCancel']/@value" />';
	  var resourceButtonMove = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.buttonMove']/@value" />';
	  var resourceButtonPaste = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.buttonPaste']/@value" />';
	  var resourceHintPaste = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.hintPaste']/@value" />';

	  var resourceConfirmDelete = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.confirmDelete']/@value" />';

	  var resourceReminder = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.reminder']/@value" />';
	  var resourceReminderCloseButton = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.reminderCloseButton']/@value" />';

      var resourceDontRemindAgain = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.dontRemindAgain']/@value" />';
      var resourceRemindAgain = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.remindAgain']/@value" />';
      var resourceMinute = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.minute']/@value" />';
      var resourceHour = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.hour']/@value" />';
      var resourceDay = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.day']/@value" />';
      var resourceWeek = '<xsl:value-of select="/calendar/resources/msg[@key='appointment.week']/@value" />';

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

	  var weekDayShortMap = 
	  [
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.mon']/@value" />',
          '<xsl:value-of select="/calendar/resources/msg[@key='calendar.tue']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.wed']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.thu']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.fri']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.sat']/@value" />',
	      '<xsl:value-of select="/calendar/resources/msg[@key='calendar.sun']/@value" />'
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
		
      var appointmentToMove = "";
      <xsl:if test="/calendar/appointmentToMove">
        appointmentToMove = '<xsl:value-of select="/calendar/appointmentToMove" />';
      </xsl:if>
		
	  var windowWidth = getWinWidth();
      var windowHeight = getWinHeight();	
          
      centerX = (windowHeight - 100) / 2 + 35;
      centerY = (windowHeight - 100) / 2 + 35;
	    
	  var innerCircleRadius = Math.round(((windowHeight - 100) / 2) / 4);
	  var shellWidth = Math.round(((windowHeight - 100) - (innerCircleRadius * 2))  / 3.8);
	  
	  var weekdaySectorWidth = 35;
	  
	  var multiDayEvents = new Array();
		  
	  function createChart()
	  {
          document.getElementById("prevMonthLink").innerHTML = monthMap[<xsl:value-of select="/calendar/prevMonth/month"/>] + ' <xsl:value-of select="/calendar/prevMonth/year"/>';		  
          document.getElementById("nextMonthLink").innerHTML = monthMap[<xsl:value-of select="/calendar/nextMonth/month"/>] + ' <xsl:value-of select="/calendar/nextMonth/year"/>';		  
		  
	      document.getElementById("svgChartCont").style.width = (windowWidth - 30) + "px";
	      document.getElementById("svgChartCont").style.height = (windowHeight - 30) + "px";
	    
	      document.getElementById("svgChart").style.width = (windowWidth - 30) + "px";
	      document.getElementById("svgChart").style.height = (windowHeight - 30) + "px";
	    
	      document.getElementById("appointmentCont").style.height = (windowHeight - 160) + "px";
    	     
		  var monthColor = COLOR_MONTH;
		  <xsl:if test="/calendar/@current">
		    monthColor = COLOR_TODAY;
		  </xsl:if>

          var prevMonthDays = <xsl:value-of select="/calendar/@prevMonthDays" />;
		  var nextMonthDays = <xsl:value-of select="/calendar/@nextMonthDays" />;
		  
		  var visibleDays = <xsl:value-of select="/calendar/@days" /> + prevMonthDays + nextMonthDays;

		  var startAngle = 0;
		  
		  var endAngle = 0;

		  if (prevMonthDays > 0) 
		  {
		      var prevMonthTooltipText = monthMap[<xsl:value-of select="/calendar/prevMonth/month"/>] + ' ' + '<xsl:value-of select="/calendar/prevMonth/year"/>';
		      endAngle = prevMonthDays * 360 / visibleDays;
			  var clickTarget = '/webfilesys/servlet?command=calendar&amp;cmd=month&amp;year=<xsl:value-of select="/calendar/prevMonth/year"/>&amp;month=<xsl:value-of select="/calendar/prevMonth/month"/>';
              pieChartSector(startAngle, endAngle, COLOR_EXTRA_DAYS_BEFORE, innerCircleRadius, prevMonthTooltipText, "", clickTarget); 

		      var prevMonthSectorText = monthMap[<xsl:value-of select="/calendar/prevMonth/month"/>].substring(0,3) + ' ' + '<xsl:value-of select="/calendar/prevMonth/year"/>';
	          sectorLabel(startAngle, endAngle, 35, innerCircleRadius + 10, "#000000", prevMonthSectorText);
		  }
		  
	      var monthTooltipText = monthMap[<xsl:value-of select="/calendar/@month" />] + ' ' + '<xsl:value-of select="/calendar/@year" />';
			 
		  startAngle = endAngle;
	      endAngle = startAngle + (<xsl:value-of select="/calendar/@days" /> * 360 / visibleDays);
          pieChartSector(startAngle, endAngle, monthColor, innerCircleRadius, monthTooltipText); 

          if (nextMonthDays > 0)
		  {
		      var nextMonthTooltipText = monthMap[<xsl:value-of select="/calendar/nextMonth/month"/>] + ' <xsl:value-of select="/calendar/nextMonth/year"/>';
		      startAngle = endAngle;
		      endAngle = startAngle + (nextMonthDays * 360 / visibleDays);
			  var clickTarget = '/webfilesys/servlet?command=calendar&amp;cmd=month&amp;year=<xsl:value-of select="/calendar/nextMonth/year"/>&amp;month=<xsl:value-of select="/calendar/nextMonth/month"/>';
              pieChartSector(startAngle, endAngle, COLOR_EXTRA_DAYS_AFTER, innerCircleRadius, nextMonthTooltipText, "", clickTarget); 

		      var nextMonthSectorText = monthMap[<xsl:value-of select="/calendar/nextMonth/month"/>].substring(0,3) + ' ' + '<xsl:value-of select="/calendar/nextMonth/year"/>';
	          sectorLabel(startAngle, endAngle, 35, innerCircleRadius + 10, "#000000", nextMonthSectorText);
		  }

          var monthSectorText = monthMap[<xsl:value-of select="/calendar/@month" />].substring(0,3) + ' ' + '<xsl:value-of select="/calendar/@year" />';

    	  var monthText = document.createElementNS(svgNS, "text");
	      monthText.setAttribute("x", centerX - 75);
          monthText.setAttribute("y", centerY + 4);
	      monthText.setAttribute("style", "font-family:Arial,Helvetica;font-size:16px;font-weight:bold");
          var monthTextNode = document.createTextNode(monthSectorText);
	      monthText.appendChild(monthTextNode);
	      document.getElementById("svgChart").appendChild(monthText);
			  
	      var shellSectorStartAngle = 0;
			  
		  var colorCounter = 0;
			  
	      <xsl:for-each select="/calendar/weekList/week">
		    <xsl:call-template name="week" />
	      </xsl:for-each>

	      shellSectorStartAngle = 0;

	      <xsl:for-each select="/calendar/weekList/week/day">
		    <xsl:call-template name="day" />
	      </xsl:for-each>
	      
          showMultiDayEvents();
	      
          document.title = "WebFileSys: <xsl:value-of select="/calendar/resources/msg[@key='calendar.titleMonth']/@value" /> " + monthTooltipText;	      
	      
	      setTimeout("checkAlarm()", 1000);
	  }
	  
	  function showMultiDayEvents()
	  {
          // var multiDayInnerRadius = innerCircleRadius + shellWidth + (shellWidth / 2) - 10;
          var multiDayInnerRadius = innerCircleRadius + shellWidth + 10;

	      for (var i = 0; i &lt; multiDayEvents.length; i++)
	      {
	          var multiDayEvent = multiDayEvents[i];
	          
              shellTransparentSector(multiDayEvent.startAngle, multiDayEvent.endAngle, 
                          COLOR_APP_FULLDAY, "0.5", 
		                  multiDayInnerRadius, multiDayInnerRadius + 10,
       	                  multiDayEvent.appToolTip,
       	                  multiDayEvent.appointmentClickAction);
       	                  
       	      multiDayInnerRadius = multiDayInnerRadius + 10;
	      }
	  }
	</script>
	  
      </head>
      <body onload="createChart()">
	  
        <div id="svgChartCont" style="width:970px;height:670px;background-color:ivory;border:1px solid black">
          <svg id="svgChart" xmlns="http://www.w3.org/2000/svg" version="1.1" width="970" height="670">
	      </svg>
	    </div>
		
	    <div id="titleBox" style="position:absolute;right:30px;top:30px;width:150px;height:40px;color:#000000;font-family:Arial,Helvetica;font-size:12px;border:1px solid #a0a0a0;padding:10px;visibility:hidden"></div>
	  
	    <a id="prevMonthLink" class="calendarYearLink calendarPrevMonthLink">
	      <xsl:attribute name="href">/webfilesys/servlet?command=calendar&amp;cmd=month&amp;year=<xsl:value-of select="/calendar/prevMonth/year"/>&amp;month=<xsl:value-of select="/calendar/prevMonth/month"/></xsl:attribute>
	    </a>  
	    <a id="nextMonthLink" class="calendarYearLink calendarNextMonthLink">
	      <xsl:attribute name="href">/webfilesys/servlet?command=calendar&amp;cmd=month&amp;year=<xsl:value-of select="/calendar/nextMonth/year"/>&amp;month=<xsl:value-of select="/calendar/nextMonth/month"/></xsl:attribute>
	    </a>  
	    <a id="backToYearLink" class="calendarYearLink calendarNextYearLink">
	      <xsl:attribute name="href">/webfilesys/servlet?command=calendar&amp;year=<xsl:value-of select="/calendar/@year"/></xsl:attribute>
	      <xsl:value-of select="/calendar/resources/msg[@key='calendar.year']/@value" />
	      <xsl:value-of select="/calendar/@year" />
	    </a>  

	    <div id="appointmentCont" class="appointmentCont">
	      <span id="selectedDay" class="selectedDay">selected day</span>
	    </div>
      </body>
    </html>
	
  </xsl:template>
  
  <xsl:template name="week">
    var daysInWeek = 7;
  
    var shellSectorSize = daysInWeek * 360 / visibleDays;
  
    var shellSectorEndAngle = shellSectorStartAngle + shellSectorSize;

    var weekColor = weekColorMap[colorCounter];
    <xsl:if test="@current">
      weekColor = COLOR_TODAY;
    </xsl:if>
	
	var tooltipText = '<xsl:value-of select="/calendar/resources/msg[@key='calendar.week']/@value" /> ' + (<xsl:value-of select="@id" /> + 1);
	
	var weekStartDay = <xsl:value-of select="day/@dayOfMonth"/> + 1;
	var weekEndDay = <xsl:value-of select="day[position() = last()]/@dayOfMonth"/> + 1;

	var weekStartMonth = monthMap[<xsl:value-of select="day/@month"/>];
	var weekEndMonth = monthMap[<xsl:value-of select="day[position() = last()]/@month"/>];
	
	tooltipText = tooltipText + ' (' + weekStartMonth + ' ' + weekStartDay + ' <xsl:value-of select="/calendar/resources/msg[@key='calendar.to']/@value" /> ' + weekEndMonth + ' ' + weekEndDay + ')';
	
	var outerCircleRadius = innerCircleRadius + shellWidth - weekdaySectorWidth;
	
    shellSector(shellSectorStartAngle, shellSectorEndAngle, 
	            weekColor, 
		        innerCircleRadius, outerCircleRadius,
       	        tooltipText);
				
	sectorLabel(shellSectorStartAngle, shellSectorEndAngle,
	            innerCircleRadius, outerCircleRadius,
	            "#000000",
                '<xsl:value-of select="/calendar/resources/msg[@key='calendar.week']/@value" /> ' + (<xsl:value-of select="@id" /> + 1),
				"16px");
				
    shellSectorStartAngle = shellSectorEndAngle;
	
    colorCounter++;
  </xsl:template>

  <xsl:template name="day">
    var shellSectorSize = 360 / visibleDays;
  
    var shellSectorEndAngle = shellSectorStartAngle + shellSectorSize;
	
	var dayInMonth = <xsl:value-of select="@dayOfMonth"/>;
	
	var dayColor = dayColorMap[dayInMonth];
	
	if (<xsl:value-of select="@month"/> != <xsl:value-of select="/calendar/@month"/>) 
	{
	    dayColor = COLOR_EXTRA_DAYS;
	}
	
	var weekDay = weekDayMap[<xsl:value-of select="@dayOfWeek" />];
	
	var month = monthMap[<xsl:value-of select="@month" />];
	
	var tooltipText = '';
	
	<xsl:if test="@today">
	  dayColor = COLOR_TODAY;
	  tooltipText = '<xsl:value-of select="/calendar/resources/msg[@key='calendar.today']/@value" />: ';
	</xsl:if>
	
	tooltipText = tooltipText + weekDay + ', ' + (dayInMonth + 1) + ' ' + month + ' <xsl:value-of select="/calendar/@year" />';
	
	var clickAction = "javascript:hideAppointments()";
	
	<xsl:if test="not(/calendar/readonly)">
	  if (<xsl:value-of select="@month"/> == <xsl:value-of select="/calendar/@month"/>) 
	  {
	      var clickParams = "<xsl:value-of select="/calendar/@year"/>, <xsl:value-of select="@month"/>, <xsl:value-of select="@dayOfMonth"/>, '" + tooltipText + "'"; 
	      clickAction = "javascript:showAppointments(" + clickParams + ")";
  	  }
	</xsl:if>
	
	var weekDayInnerRadius = innerCircleRadius + shellWidth - weekdaySectorWidth;
	
	var weekdayColor = COLOR_WEEKDAY;
	
	<xsl:if test="@dayOfWeek = '5'">
	    weekdayColor = COLOR_SATURDAY;
    </xsl:if>
	<xsl:if test="@dayOfWeek = '6'">
	    weekdayColor = COLOR_SUNDAY;
    </xsl:if>
	
    shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                weekdayColor, 
		        weekDayInnerRadius, innerCircleRadius + shellWidth,
       	        tooltipText,
       	        clickAction);
       	        
    sectorLabel(shellSectorStartAngle, shellSectorEndAngle, 
	            weekDayInnerRadius - 6, innerCircleRadius + shellWidth + 6,
	            "#000000", 
	            weekDayShortMap[<xsl:value-of select="@dayOfWeek" />], "12px");

    shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                dayColor, 
		        innerCircleRadius + shellWidth, innerCircleRadius + (2 * shellWidth),
       	        tooltipText,
       	        clickAction);
				
	<xsl:if test="@holiday">
      shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                  COLOR_HOLIDAY, 
		          innerCircleRadius + (2 * shellWidth) - 10, innerCircleRadius + (2 * shellWidth),
       	          '<xsl:value-of select="@holiday" />');
	</xsl:if>

	<xsl:if test="@today">
	  var now = new Date();
	  var hours = now.getHours();
	  
	  var innerTimeSectorRadius = innerCircleRadius + shellWidth;
      var outerTimeSectorRadius = innerTimeSectorRadius + (hours * shellWidth / 24);
	  
      shellSector(shellSectorStartAngle, shellSectorEndAngle, 
                  COLOR_TIME_OF_DAY, 
		          innerTimeSectorRadius, outerTimeSectorRadius,
       	          tooltipText,
       	          clickAction);
    </xsl:if>	
	
    sectorLabel(shellSectorStartAngle, shellSectorEndAngle, 
	            innerCircleRadius + (2 * shellWidth) - 40, innerCircleRadius + (2 * shellWidth) - 10,
	            "#000000", <xsl:value-of select="@dayOfMonth" /> + 1, "16px");

    var appointmentStartAngle = shellSectorStartAngle;
    
    <xsl:if test="@month = /calendar/@month"> 
      <xsl:if test="appointmentList/appointment">	
        var dayAppointments = new Array();
        monthAppointments['<xsl:value-of select="@dayOfMonth" />'] = dayAppointments;
	    
	    var appointmentWidth = (shellSectorEndAngle - shellSectorStartAngle) / (2 * <xsl:value-of select="count(appointmentList/appointment)" />);
	
	    <xsl:for-each select="appointmentList/appointment">
	      <xsl:call-template name="appointment" />
	    </xsl:for-each>
	  </xsl:if>
	</xsl:if>
	
    shellSectorStartAngle = shellSectorEndAngle;
  </xsl:template>

  <xsl:template name="appointment">
  
    appointmentStartAngle = appointmentStartAngle + (appointmentWidth / 2);
    var appointmentEndAngle = appointmentStartAngle + appointmentWidth;
  
    var appointmentStartHour = <xsl:value-of select="startHour" />;
    var appointmentEndHour = <xsl:value-of select="endHour" />;

    var appointment = new Object();
    dayAppointments.push(appointment);
    appointment.eventId = <xsl:value-of select="id" />;
    appointment.eventTime = <xsl:value-of select="eventTime" />;
    appointment.duration = <xsl:value-of select="duration" />;
    appointment.startMinuteOfDay = <xsl:value-of select="startMinuteOfDay" />;
    appointment.endMinuteOfDay = <xsl:value-of select="endMinuteOfDay" />;
    appointment.startHour = appointmentStartHour;
    appointment.endHour = appointmentEndHour;
    appointment.subject = '<xsl:value-of select="subject" />';
    appointment.repeatPeriod = <xsl:value-of select="repeatPeriod" />;
    appointment.alarmType = <xsl:value-of select="alarmType" />;
    appointment.alarmAheadHours = <xsl:value-of select="alarmAheadHours" />;
    appointment.alarmAheadMinutes = <xsl:value-of select="alarmAheadMinutes" />;
    appointment.description = '<xsl:value-of select="description" />';
    appointment.formattedStartTime = '<xsl:value-of select="startTime" />';
    appointment.formattedEndTime = '<xsl:value-of select="endTime" />';

    appointment.fullDay = ('<xsl:value-of select="fullDay" />' == 'true');
    if (appointment.fullDay)
    {
        appointment.fullDayNum = 0;
        <xsl:if test="fullDayNum">
          appointment.fullDayNum = <xsl:value-of select="fullDayNum" />
          appointment.fullDayTotalNum = <xsl:value-of select="fullDayTotalNum" />
          appointment.fullDaysInCurrentMonth = <xsl:value-of select="fullDaysInCurrentMonth" />
        </xsl:if>
    }

    var appointmentInnerRadius;
    var appointmentOuterRadius;

    if (appointment.fullDay)
    {
        if (appointment.fullDayNum == 0)
        {
            appointmentInnerRadius = innerCircleRadius + shellWidth;
            appointmentOuterRadius = innerCircleRadius + shellWidth + shellWidth;
        }
    }
    else
    {
        appointmentInnerRadius = innerCircleRadius + shellWidth + (appointmentStartHour * shellWidth / 24);
        appointmentOuterRadius = innerCircleRadius + shellWidth + (appointmentEndHour * shellWidth / 24);
        if (appointmentEndHour == appointmentStartHour)
        {
            appointmentOuterRadius = appointmentOuterRadius + 5;
        }
    }  
    
    var appToolTip;
    if (appointment.fullDay) 
    {
        if (appointment.fullDayNum == 0)
        {
            appToolTip = tooltipText;
        }
        else
        {
            // TODO: end day
            appToolTip = tooltipText;
        }
    }
    else
    {
        appToolTip = '<xsl:value-of select="startTime" /> - <xsl:value-of select="endTime" />';
    }
    <xsl:if test="subject">
      var shortSubject = shortText('<xsl:value-of select="subject" />', 38);
      appToolTip = appToolTip + ' ' + shortSubject;
    </xsl:if>
    
	var appointmentColor = COLOR_APPOINTMENT;
	if (appointment.fullDay)
	{
        appointmentColor = COLOR_APP_FULLDAY;
	}
	else
	{
        if ((appointment.repeatPeriod == 1) || (appointment.repeatPeriod == 5))
        {
	        appointmentColor = COLOR_APP_REPEAT_DAILY;
        }	
	    else if (appointment.repeatPeriod != 0) 
	    {
	        appointmentColor = COLOR_APP_REPEAT_OTHER;
	    }
	}
	
	var appointmentClickAction = clickAction;
	<xsl:if test="not(/calendar/readonly)">
	  appointmentClickAction = appointmentClickAction + ";showAppointmentDetail(" + dayInMonth + "," + appointment.eventId + ")";
	</xsl:if>
	
    if ((appointment.fullDay) &amp;&amp; (appointment.fullDaysInCurrentMonth > 0)) 
    {
        var multiDayEvent = new Object();
        multiDayEvent.startAngle = shellSectorStartAngle;
        multiDayEvent.endAngle = shellSectorStartAngle + (shellSectorSize * appointment.fullDaysInCurrentMonth);
        multiDayEvent.appToolTip = appToolTip;
        multiDayEvent.appointmentClickAction = appointmentClickAction;

        multiDayEvents.push(multiDayEvent);
    }
	else
	{
        shellSector(appointmentStartAngle, appointmentEndAngle, 
                    appointmentColor, 
		            appointmentInnerRadius, appointmentOuterRadius,
       	            appToolTip,
       	            appointmentClickAction);
	}
    
    appointmentStartAngle = appointmentEndAngle;
  </xsl:template>

</xsl:stylesheet>

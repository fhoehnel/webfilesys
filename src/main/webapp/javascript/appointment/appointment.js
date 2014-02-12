var TIMETABLE_COLOR = "#80ffff";

function clearAppointmentCont()
{
    var appCont = document.getElementById("appointmentCont");

	var detailFormElem = document.getElementById("detailForm");
	if (detailFormElem)
	{
		appCont.removeChild(detailFormElem);
	}
	
	var timeTableElem = document.getElementById("timeTable");
    if (timeTableElem)
    {
        appCont.removeChild(timeTableElem);
    }    

	var buttonContElem = document.getElementById("buttonCont");
    if (buttonContElem)
    {
        appCont.removeChild(buttonContElem);
    }    
}

function showAppointments(year, month, dayOfMonth, formattedSelectedDay)
{
	if (year)
	{
		selectedDay.year = year;
		selectedDay.month = month;
		selectedDay.dayOfMonth = dayOfMonth;
		selectedDay.formattedSelectedDay = formattedSelectedDay;
	}
	else
	{
		year = selectedDay.year;
		month = selectedDay.month;
		dayOfMonth = selectedDay.dayOfMonth;
		formattedSelectedDay = selectedDay.formattedSelectedDay;
	}
	
    document.getElementById("selectedDay").innerHTML = formattedSelectedDay;

    var appCont = document.getElementById("appointmentCont");

    clearAppointmentCont();    
    
   	timeTableElem = document.createElement("div");
   	timeTableElem.id = "timeTable";
   	timeTableElem.setAttribute("class", "timeTable");
   	appCont.appendChild(timeTableElem);

    for (var i = 0; i < 24; i++)
    {
    	var hourElem = document.createElement("div");
    	hourElem.setAttribute("class", "timeTableHour");
    	hourElem.style.top = (i * 25) + "px";
    	timeTableElem.appendChild(hourElem);
        var hourTextNode = document.createTextNode(i);
	    hourElem.appendChild(hourTextNode);
    }
    
    var dayAppointments = monthAppointments[dayOfMonth];
    if (dayAppointments)
    {
    	var dayAppNum = dayAppointments.length;
    	
    	var appWidth = 125 / dayAppNum - 10;
    	
    	for (var k = 0; k < dayAppointments.length; k++) 
    	{
    		var appointment = dayAppointments[k];
    		
    		if (!appointment.fullDay || (appointment.fullDayNum == 0))
    		{
        		var titleText = appointment.formattedStartTime + " - " + appointment.formattedEndTime + " " + appointment.subject;
        		
            	var appElem = document.createElement("div");
            	appElem.setAttribute("class", "timeTableAppointment");
            	appElem.setAttribute("title", titleText);
            	appElem.style.left = (10 + (k * (appWidth + 10))) + "px";
            	appElem.style.width = appWidth + "px";
            	appElem.style.top = ((appointment.startMinuteOfDay) * 25 / 60) + "px";
            	
            	var durationMinutes = appointment.endMinuteOfDay - appointment.startMinuteOfDay;
            	if (durationMinutes < 20)
            	{
            		durationMinutes = 20;
            	}
            	appElem.style.height = ((durationMinutes) * 25 / 60) + "px";
            	timeTableElem.appendChild(appElem);
            	
            	var detailFuncCall = "showAppointmentDetail(" + dayOfMonth + "," + appointment.eventId + ")";
                appElem.setAttribute("onclick", detailFuncCall);
    		}
    	}
    }
    
	var buttonCont = document.createElement("div");
	buttonCont.id = "buttonCont";
	buttonCont.setAttribute("class", "appointmentForm timeTableButtonCont");
	
	var createButton = document.createElement("button");
	createButton.setAttribute("class", "appointmentForm appointmentButton");
	createButton.setAttribute("onclick", "showCreateAppointmentForm()");
	createButton.innerHTML = resourceButtonCreate;
    buttonCont.appendChild(createButton);
    
    if (appointmentToMove.length > 0)
    {
    	var pasteButton = document.createElement("button");
    	pasteButton.setAttribute("class", "appointmentForm appointmentButton");
    	pasteButton.setAttribute("onclick", "pasteAppointment('" + year + "','" + month + "','" + dayOfMonth + "')");
    	pasteButton.innerHTML = resourceButtonPaste;
        buttonCont.appendChild(pasteButton);
    }
    
    appCont.appendChild(buttonCont);
    
    appCont.style.visibility = "visible";
}

function showCreateAppointmentForm()
{
    clearAppointmentCont();    

    var appCont = document.getElementById("appointmentCont");

    var detailForm = document.createElement("form");
    detailForm.id = "detailForm";
    detailForm.setAttribute("onsubmit", "return false;");
    detailForm.setAttribute("class", "appointmentDetailForm");
    appCont.appendChild(detailForm);
    
    detailForm.appendChild(createSubjectField(""));

    detailForm.appendChild(createTimeSelection("startHour", "startMinute", 0, 0, resourceStartTime));

    detailForm.appendChild(createTimeSelection("endHour", "endMinute", 0, 0, resourceEndTime));

    detailForm.appendChild(createDuration());

    detailForm.appendChild(createFullDaySelection());
    
    detailForm.appendChild(createRepeatPeriodSelection(0));

    detailForm.appendChild(createAlarmSelection(0));

    detailForm.appendChild(createTimeSelection("alarmAheadHours", "alarmAheadMinutes", 0, 0, resourceAlarmAhead));
    
    detailForm.appendChild(createDescriptionField(""));

    detailForm.appendChild(createNewButtons());
    
    document.getElementById("startHour").setAttribute("onchange", "adjustEndTime()");
    document.getElementById("startMinute").setAttribute("onchange", "adjustEndTime()");
    document.getElementById("endHour").setAttribute("onchange", "adjustDuration()");
    document.getElementById("endMinute").setAttribute("onchange", "adjustDuration()");
}

function createNewButtons()
{
	var buttonCont = document.createElement("div");
	buttonCont.setAttribute("class", "appointmentForm appointmentButtonCont");
	
	var saveButton = document.createElement("button");
    saveButton.setAttribute("class", "appointmentForm appointmentButton");
    saveButton.setAttribute("onclick", "createAppointment()");
    saveButton.innerHTML = resourceButtonCreate;
    buttonCont.appendChild(saveButton);
	
	var cancelButton = document.createElement("button");
    cancelButton.setAttribute("class", "appointmentForm appointmentButton");
    cancelButton.setAttribute("onclick", "showAppointments()");
    cancelButton.innerHTML = resourceButtonCancel;
    buttonCont.appendChild(cancelButton);
    
    return buttonCont;
}

function showAppointmentDetail(dayOfMonth, eventId) 
{
	var selectedAppointment;
	
    var dayAppointments = monthAppointments[dayOfMonth];

    var stop = false;
	for (var i = 0; (!stop) && (i < dayAppointments.length); i++) 
	{
		if (dayAppointments[i].eventId == eventId)
		{
			selectedAppointment = dayAppointments[i];
			stop = true;
		}
	}
	
	var eventStartDate = new Date(selectedAppointment.eventTime);
	var eventEndDate = new Date(selectedAppointment.eventTime + selectedAppointment.duration);
	
	var appCont = document.getElementById("appointmentCont");

    clearAppointmentCont();    

    var detailForm = document.createElement("form");
    detailForm.id = "detailForm";
    detailForm.setAttribute("onsubmit", "return false;");
    detailForm.setAttribute("class", "appointmentDetailForm");
    appCont.appendChild(detailForm);

    var eventIdParam = document.createElement("input");
    eventIdParam.setAttribute("type", "hidden");
    eventIdParam.setAttribute("name", "appointmentId");
    eventIdParam.setAttribute("value", eventId);
    detailForm.appendChild(eventIdParam);
    
    detailForm.appendChild(createSubjectField(selectedAppointment.subject));

    detailForm.appendChild(createTimeSelection("startHour", "startMinute", 
    	eventStartDate.getHours(), eventStartDate.getMinutes(), resourceStartTime));

    detailForm.appendChild(createTimeSelection("endHour", "endMinute",
    	eventEndDate.getHours(), eventEndDate.getMinutes(), resourceEndTime));

    detailForm.appendChild(createDuration());
    
    detailForm.appendChild(createFullDaySelection(selectedAppointment.fullDay, selectedAppointment.fullDayTotalNum));
    
    setDurationValues(eventStartDate.getHours(), eventStartDate.getMinutes(), 
    		          eventEndDate.getHours(), eventEndDate.getMinutes());    
    
    detailForm.appendChild(createRepeatPeriodSelection(selectedAppointment.repeatPeriod));

    detailForm.appendChild(createAlarmSelection(selectedAppointment.alarmType));
    
    detailForm.appendChild(createTimeSelection("alarmAheadHours", "alarmAheadMinutes", 
    	selectedAppointment.alarmAheadHours, selectedAppointment.alarmAheadMinutes, resourceAlarmAhead));

    detailForm.appendChild(createDescriptionField(selectedAppointment.description));

    detailForm.appendChild(createEditButtons(eventId));
    
    document.getElementById("startHour").setAttribute("onchange", "adjustEndTime()");
    document.getElementById("startMinute").setAttribute("onchange", "adjustEndTime()");
    document.getElementById("endHour").setAttribute("onchange", "adjustDuration()");
    document.getElementById("endMinute").setAttribute("onchange", "adjustDuration()");
    
    if (selectedAppointment.fullDay) 
    {
    	switchShowMultiDay();
    	if (selectedAppointment.fullDayNum && (selectedAppointment.fullDayNum > 1)) 
    	{
    		switchShowNumOfDays();
    	}
    }
}

function createEditButtons(eventId)
{
	var buttonCont = document.createElement("div");
	buttonCont.setAttribute("class", "appointmentForm appointmentButtonCont");
	
	var saveButton = document.createElement("button");
    saveButton.setAttribute("class", "appointmentForm appointmentButton");
    saveButton.setAttribute("onclick", "changeAppointment()");
    saveButton.innerHTML = resourceButtonSave;
    buttonCont.appendChild(saveButton);

	var moveButton = document.createElement("button");
	moveButton.setAttribute("class", "appointmentForm appointmentButton");
	moveButton.setAttribute("onclick", "moveAppointment('" + eventId + "')");
	moveButton.innerHTML = resourceButtonMove;
    buttonCont.appendChild(moveButton);

    var deleteButton = document.createElement("button");
	deleteButton.setAttribute("class", "appointmentForm appointmentButton");
	deleteButton.setAttribute("onclick", "deleteAppointment('" + eventId + "')");
	deleteButton.innerHTML = resourceButtonDelete;
    buttonCont.appendChild(deleteButton);
    
	var cancelButton = document.createElement("button");
    cancelButton.setAttribute("class", "appointmentForm appointmentButton");
    cancelButton.setAttribute("onclick", "showAppointments()");
    cancelButton.innerHTML = resourceButtonCancel;
    buttonCont.appendChild(cancelButton);
    
    return buttonCont;
}

function createDuration()
{
	var durationCont = document.createElement("div");
	durationCont.setAttribute("class", "appointmentForm appointmentDurationCont");
	
    var labelText = document.createElement("span");
    labelText.setAttribute("class", "appointmentDuration");
    labelText.innerHTML = resourceDuration + " : &nbsp;";
    durationCont.appendChild(labelText);

    var durationHours = document.createElement("span");
    durationHours.id = "durationHours";
    durationHours.setAttribute("class", "appointmentDuration");
    durationCont.appendChild(durationHours);
    
    var durationMinutes = document.createElement("span");
    durationMinutes.id = "durationMinutes";
    durationMinutes.setAttribute("class", "appointmentDuration");
    durationCont.appendChild(durationMinutes);

    return durationCont;
}

function setDurationValues(startHour, startMinute, endHour, endMinute)
{
	var durationHours = document.getElementById("durationHours");
	var durationMinutes = document.getElementById("durationMinutes");

	if ((endHour < startHour) || ((endHour == startHour) && (endMinute < startMinute)))
	{
		durationHours.innerHTML = "";
		durationMinutes.innerHTML = "";
		return;
	}
	
	var durationHourVal;
    if (endMinute >= startMinute)
    {
        durationHourVal = (endHour - startHour).toString();
    }
    else
    {
        durationHourVal = (endHour - startHour -1).toString();
    }

    if (durationHourVal.length < 2)
    {
    	durationHourVal = "0" + durationHourVal;
    }
    durationHours.innerHTML = durationHourVal + ":";
	
    var durationMin;
    if (endMinute >= startMinute)
    {
    	durationMin = (endMinute - startMinute).toString();
    }
    else
    {
    	durationMin = (endMinute + (60 - startMinute)).toString();
    }
    
    if (durationMin.length < 2)
    {
    	durationMin = "0" + durationMin;
    }
    durationMinutes.innerHTML = durationMin;
}

function createFullDaySelection(fullDay, multiDayNum)
{
	var fullDaySelCont = document.createElement("div");
	fullDaySelCont.setAttribute("class", "appointmentForm appointmentFullDayCont");
	
    var fullDayCheckbox = document.createElement("input");
    fullDayCheckbox.id = "fullDayCheckbox";
    fullDayCheckbox.setAttribute("type", "checkbox");
    fullDayCheckbox.setAttribute("name", "fullDay");
    fullDayCheckbox.setAttribute("class", "appointmentFullDay");
    fullDayCheckbox.setAttribute("onchange", "switchShowMultiDay()");
    if (fullDay) 
    {
    	fullDayCheckbox.checked = true;
    }
    fullDaySelCont.appendChild(fullDayCheckbox);
	
    var labelText = document.createElement("label");
    labelText.setAttribute("class", "appointmentFullDay");
    labelText.setAttribute("for", "fullDayCheckbox");
    labelText.innerHTML = resourceFullDay;
    fullDaySelCont.appendChild(labelText);
    
    var multiDayCheckbox = document.createElement("input");
    multiDayCheckbox.id = "multiDayCheckbox";
    multiDayCheckbox.setAttribute("type", "checkbox");
    multiDayCheckbox.setAttribute("name", "multiDay");
    multiDayCheckbox.setAttribute("class", "appointmentFullDay");
    multiDayCheckbox.setAttribute("onchange", "switchShowNumOfDays()");
    if (!fullDay) 
    {
        multiDayCheckbox.style.display = "none";
    }
    if (multiDayNum && multiDayNum > 1)
    {
    	multiDayCheckbox.checked = true;
    }
    fullDaySelCont.appendChild(multiDayCheckbox);
    
    labelText = document.createElement("label");
    labelText.id = "multiDayLabel";
    labelText.setAttribute("class", "appointmentFullDay");
    labelText.setAttribute("for", "multiDayCheckbox");
    labelText.innerHTML = resourceMultiDay;
    labelText.style.display = "none";
    fullDaySelCont.appendChild(labelText);
    
    labelText = document.createElement("span");
    labelText.id = "numOfDayLabel";
    labelText.setAttribute("class", "appointmentForm appointmentNumOfDayLabel");
    labelText.innerHTML = resourceNumOfDays + ":";
    labelText.style.display = "none";
    fullDaySelCont.appendChild(labelText);

    var numOfDay = document.createElement("input");
    numOfDay.id = "numOfDay";
    numOfDay.setAttribute("type", "text");
    numOfDay.setAttribute("name", "numOfDays");
    numOfDay.setAttribute("class", "appointmentForm appointmentNumOfDays");
    if (multiDayNum && multiDayNum > 1)
    {
    	numOfDay.value = multiDayNum;
    }
    else
    {
        numOfDay.style.display = "none";
    }
    fullDaySelCont.appendChild(numOfDay);
    
    return fullDaySelCont;
}

function switchShowNumOfDays()
{
    var multiDayCheckbox = document.getElementById("multiDayCheckbox");
    if (multiDayCheckbox.checked) 
    {
    	document.getElementById("numOfDayLabel").style.display = "inline";
    	document.getElementById("numOfDay").style.display = "inline";
    	
    	document.getElementById("repeatPeriodSel").value = "0";
    	document.getElementById("repeatPeriodSel").disabled = true;
    }
    else
    {
    	document.getElementById("numOfDayLabel").style.display = "none";
    	document.getElementById("numOfDay").style.display = "none";
    	document.getElementById("numOfDay").value = "";

    	document.getElementById("repeatPeriodSel").disabled = false;
    }
}

function switchShowMultiDay()
{
    var fullDayCheckbox = document.getElementById("fullDayCheckbox");
    if (fullDayCheckbox.checked) 
    {
    	document.getElementById("multiDayCheckbox").style.display = "inline";
    	document.getElementById("multiDayLabel").style.display = "inline";
    	
    	document.getElementById("startHour").disabled = true;
    	document.getElementById("endHour").disabled = true;
    	document.getElementById("startMinute").disabled = true;
    	document.getElementById("endMinute").disabled = true;
    	
    	document.getElementById("startHour").selectedIndex = 0;
    	document.getElementById("endHour").selectedIndex = 0;
    	document.getElementById("startMinute").selectedIndex = 0;
    	document.getElementById("endMinute").selectedIndex = 0;
    }
    else 
    {
    	document.getElementById("multiDayCheckbox").style.display = "none";
    	document.getElementById("multiDayLabel").style.display = "none";
    	document.getElementById("multiDayCheckbox").checked = false;

    	document.getElementById("startHour").disabled = false;
    	document.getElementById("endHour").disabled = false;
    	document.getElementById("startMinute").disabled = false;
    	document.getElementById("endMinute").disabled = false;
    	
    	switchShowNumOfDays();    	
    }
}

function createSubjectField(subjectText)
{
	var subjectCont = document.createElement("div");
	subjectCont.setAttribute("class", "appointmentForm appointmentSubjectCont");
	
    var labelText = document.createElement("span");
    labelText.setAttribute("class", "appointmentForm appointmentLabel");
    labelText.innerHTML = resourceSubject + ":";
    subjectCont.appendChild(labelText);

    var subject = document.createElement("input");
    subject.setAttribute("type", "text");
    subject.setAttribute("name", "subject");
    if (subjectText)
    {
    	subject.setAttribute("value", subjectText);
    }
	subject.setAttribute("class", "appointmentForm appointmentSubject");
    subjectCont.appendChild(subject);
    
    return subjectCont;
}

function createDescriptionField(descText)
{
	var descCont = document.createElement("div");
	descCont.setAttribute("class", "appointmentForm appointmentDescCont");
	
    var labelText = document.createElement("span");
    labelText.setAttribute("class", "appointmentForm appointmentLabel");
    labelText.innerHTML = resourceDescription + ":";
    descCont.appendChild(labelText);

    var description = document.createElement("textarea");
    description.setAttribute("name", "description");
    description.setAttribute("class", "appointmentForm appointmentDescription");
    if (descText)
    {
    	description.innerHTML = descText;
    }
    descCont.appendChild(description);
    
    return descCont;
}

function createRepeatPeriodSelection(selectedValue)
{
	var repeatPeriodCont = document.createElement("div");
	repeatPeriodCont.setAttribute("class", "appointmentForm appointmentRepeatPeriodCont");

    var labelText = document.createElement("span");
    labelText.setAttribute("class", "appointmentForm appointmentLabel");
    labelText.innerHTML = resourceRepeatPeriod + ":";
    repeatPeriodCont.appendChild(labelText);
    
	var selectbox = document.createElement("select");
	selectbox.id = "repeatPeriodSel";
	selectbox.setAttribute("name", "repeatPeriod");
	selectbox.setAttribute("class", "appointmentForm appointmentRepeatPeriod");
	
	selectbox.options[0] = new Option(resourceRepeatNone, 0, (selectedValue == 0), (selectedValue == 0));
	selectbox.options[1] = new Option(resourceRepeatDaily, 1, (selectedValue == 1), (selectedValue == 1));
	selectbox.options[2] = new Option(resourceRepeatWeekly, 2, (selectedValue == 2), (selectedValue == 2));
	selectbox.options[3] = new Option(resourceRepeatMonthly, 3, (selectedValue == 3), (selectedValue == 3));
	selectbox.options[4] = new Option(resourceRepeatYearly, 4, (selectedValue == 4), (selectedValue == 4));
	selectbox.options[5] = new Option(resourceRepeatWeekday, 5, (selectedValue == 5), (selectedValue == 5));

    repeatPeriodCont.appendChild(selectbox);
	
	return repeatPeriodCont;
}

function createAlarmSelection(selectedValue)
{
	var alarmCont = document.createElement("div");
	alarmCont.setAttribute("class", "appointmentForm appointmentAlarmCont");

    var labelText = document.createElement("span");
    labelText.setAttribute("class", "appointmentForm appointmentLabel");
    labelText.innerHTML = resourceAlarmType + ":";
    alarmCont.appendChild(labelText);
    
	var selectbox = document.createElement("select");
	selectbox.setAttribute("name", "alarmType");
	selectbox.setAttribute("class", "appointmentForm appointmentAlarm");
	
	selectbox.options[0] = new Option(resourceAlarmNone, 0, (selectedValue == 0), (selectedValue == 0));
	selectbox.options[1] = new Option(resourceAlarmVisual, 1, (selectedValue == 1), (selectedValue == 1));
	selectbox.options[2] = new Option(resourceAlarmSound, 2, (selectedValue == 2), (selectedValue == 2));
	selectbox.options[3] = new Option(resourceAlarmMail, 3, (selectedValue == 3), (selectedValue == 3));
	selectbox.options[4] = new Option(resourceAlarmAll, 4, (selectedValue == 4), (selectedValue == 4));

    alarmCont.appendChild(selectbox);
	
	return alarmCont;
}

function createTimeSelection(hourParamName, minuteParamName, selectedHour, selectedMinute, label)
{
	var timeSelCont = document.createElement("div");
	timeSelCont.setAttribute("class", "appointmentForm appointmentTimeCont");

    var labelText = document.createElement("span");
    labelText.setAttribute("class", "appointmentForm appointmentLabel");
    labelText.innerHTML = label + ":";
    timeSelCont.appendChild(labelText);
	
	timeSelCont.appendChild(createHourSelection(hourParamName, selectedHour));
	
	var sep = document.createElement("span");
    sep.setAttribute("class", "appointmentTimeSep");
	sep.innerHTML = ":"
	timeSelCont.appendChild(sep);
	
	timeSelCont.appendChild(createMinuteSelection(minuteParamName, selectedMinute));
	return timeSelCont;
}

function createHourSelection(formElementName, selectedHour)
{
	var selectbox = document.createElement("select");
	selectbox.id = formElementName;
	selectbox.setAttribute("name", formElementName);
	selectbox.setAttribute("class", "appointmentForm appointmentHour");
	
	for (var i = 0; i < 24; i++)
	{
		var optionText = i.toString();
		if (i < 10)
		{
			optionText = "0" + optionText;
		}
		selectbox.options[selectbox.length] = new Option(optionText, i, (i == selectedHour), (i == selectedHour));
	}
	
	return selectbox;
}

function createMinuteSelection(formElementName, selectedMinute)
{
	var selectbox = document.createElement("select");
	selectbox.id = formElementName;
	selectbox.setAttribute("name", formElementName);
	selectbox.setAttribute("class", "appointmentForm appointmentMinute");
	
	for (var i = 0; i < 60; i+=5)
	{
		var optionText = i.toString();
		if (i < 10)
		{
			optionText = "0" + optionText;
		}
		selectbox.options[selectbox.length] = new Option(optionText, i, (i == selectedMinute), (i == selectedMinute));
	}
	
	return selectbox;
}

function hideAppointments()
{
    document.getElementById("appointmentCont").style.visibility = "hidden";
}

function deleteAppointment(eventId)
{
	if (!confirm(resourceConfirmDelete))
	{
		return;
	}
	var url = "/webfilesys/servlet?command=calendar&cmd=delAppointment&eventId=" + eventId;
	xmlRequest(url, showDeleteResult);
}

function showDeleteResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true')
            {
                var deletedEventId = resultElem.getElementsByTagName("deletedId")[0].firstChild.nodeValue;            
                
                var dayAppointments = monthAppointments[selectedDay.dayOfMonth];
                if (dayAppointments) 
                {
                	var found = false;
                	for (var i = 0; (!found) && (i < dayAppointments.length); i++)
                	{
                		if (dayAppointments[i].eventId == deletedEventId)
                		{
                			dayAppointments.splice(i, 1);
                			found = true;
                		}
                	}
                }
    	        // showAppointments();	
				reloadMonth();
            }
        }
    }
}

function moveAppointment(eventId)
{
	var url = "/webfilesys/servlet?command=calendar&cmd=moveAppointment&eventId=" + eventId;
	xmlRequest(url, showMoveResult);
}

function showMoveResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true')
            {
            	alert(resourceHintPaste);
				reloadMonth();
            }
        }
    }
}

function pasteAppointment(year, month, dayOfMonth)
{
	var url = "/webfilesys/servlet?command=calendar&cmd=pasteAppointment&year=" + year + "&month=" + month + "&dayOfMonth=" + dayOfMonth;
	xmlRequest(url, showPasteResult);
}

function showPasteResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true')
            {
 				reloadMonth();
            }
        }
    }
}

function changeAppointment()
{
	if (!validateFormData())
	{
		alert("invalid data entered");
		return;
	}
	
	var formData = getFormData(document.getElementById("detailForm"));
	
	formData = formData + "command=calendar&cmd=changeAppointment";
	
	xmlRequestPost("/webfilesys/servlet", formData, showChangeResult)	
}

function showChangeResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true')
            {
                var appointmentElem = resultElem.getElementsByTagName("appointment")[0];            

                var appointmentId = appointmentElem.getElementsByTagName("id")[0].firstChild.nodeValue;
                
                var changedAppointment = getAppointmentById(selectedDay.dayOfMonth, appointmentId);

                if (changedAppointment != null)
                {
                    changedAppointment.eventTime = Number(appointmentElem.getElementsByTagName("eventTime")[0].firstChild.nodeValue);
                    changedAppointment.duration = Number(appointmentElem.getElementsByTagName("duration")[0].firstChild.nodeValue);
                    changedAppointment.startMinuteOfDay = appointmentElem.getElementsByTagName("startMinuteOfDay")[0].firstChild.nodeValue;
                    changedAppointment.endMinuteOfDay = appointmentElem.getElementsByTagName("endMinuteOfDay")[0].firstChild.nodeValue;
                    changedAppointment.startHour = appointmentElem.getElementsByTagName("startHour")[0].firstChild.nodeValue;
                    changedAppointment.endHour = appointmentElem.getElementsByTagName("endHour")[0].firstChild.nodeValue;
                    changedAppointment.subject = appointmentElem.getElementsByTagName("subject")[0].firstChild.nodeValue;
                    changedAppointment.repeatPeriod = appointmentElem.getElementsByTagName("repeatPeriod")[0].firstChild.nodeValue;
                    changedAppointment.alarmType = appointmentElem.getElementsByTagName("alarmType")[0].firstChild.nodeValue;
                    changedAppointment.alarmAheadHours = appointmentElem.getElementsByTagName("alarmAheadHours")[0].firstChild.nodeValue;
                    changedAppointment.alarmAheadMinutes = appointmentElem.getElementsByTagName("alarmAheadMinutes")[0].firstChild.nodeValue;
                    
                    var descElemList = appointmentElem.getElementsByTagName("description");
                    if (descElemList && (descElemList.length > 0))
                    {
                        var description = descElemList[0].firstChild.nodeValue;
                        if (description)
                        {
                        	changedAppointment.description = description;
                        }	
                    }
                }
            	
    	        // showAppointments();
                reloadMonth();				
            }
        }
    }
}

function createAppointment()
{
	if (!validateFormData())
	{
		alert("invalid data entered");
		return;
	}
	
	var formData = getFormData(document.getElementById("detailForm"));
	
	formData = formData + "command=calendar&cmd=newAppointment";
	
	formData = formData + "&year=" + selectedDay.year + "&month=" + selectedDay.month + "&day=" + selectedDay.dayOfMonth;
	
	xmlRequestPost("/webfilesys/servlet", formData, showCreateResult)	
}

function showCreateResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true')
            {
                var appointmentElem = resultElem.getElementsByTagName("appointment")[0];            
            	
                var dayAppointments = monthAppointments[selectedDay.dayOfMonth];

                if (!dayAppointments)
                {
                	dayAppointments = new Array();
                	monthAppointments[selectedDay.dayOfMonth] = dayAppointments;
                }
                
                var appointment = new Object();
                dayAppointments.push(appointment);
                
                appointment.eventId = appointmentElem.getElementsByTagName("id")[0].firstChild.nodeValue;
                appointment.eventTime = Number(appointmentElem.getElementsByTagName("eventTime")[0].firstChild.nodeValue);
                appointment.duration = Number(appointmentElem.getElementsByTagName("duration")[0].firstChild.nodeValue);
                appointment.startMinuteOfDay = appointmentElem.getElementsByTagName("startMinuteOfDay")[0].firstChild.nodeValue;
                appointment.endMinuteOfDay = appointmentElem.getElementsByTagName("endMinuteOfDay")[0].firstChild.nodeValue;
                appointment.startHour = appointmentElem.getElementsByTagName("startHour")[0].firstChild.nodeValue;
                appointment.endHour = appointmentElem.getElementsByTagName("endHour")[0].firstChild.nodeValue;
                appointment.subject = appointmentElem.getElementsByTagName("subject")[0].firstChild.nodeValue;
                appointment.repeatPeriod = appointmentElem.getElementsByTagName("repeatPeriod")[0].firstChild.nodeValue;
                appointment.alarmType = appointmentElem.getElementsByTagName("alarmType")[0].firstChild.nodeValue;
                appointment.alarmAheadHours = appointmentElem.getElementsByTagName("alarmAheadHours")[0].firstChild.nodeValue;
                appointment.alarmAheadMinutes = appointmentElem.getElementsByTagName("alarmAheadMinutes")[0].firstChild.nodeValue;
                
                var descElemList = appointmentElem.getElementsByTagName("description");
                if (descElemList && (descElemList.length > 0))
                {
                    var description = descElemList[0].firstChild.nodeValue;
                    if (description)
                    {
                        appointment.description = description;
                    }	
                }
            	
    	        // showAppointments();	
				reloadMonth();
            }
        }
    }
}

function reloadMonth()
{
    window.location.href = "/webfilesys/servlet?command=calendar&cmd=month&year=" + selectedDay.year + "&month=" + selectedDay.month;
}

function getAppointmentById(dayOfMonth, appointmentId)
{
    var appointmentsOfMonth = monthAppointments[dayOfMonth];
    if (!appointmentsOfMonth)
    {
    	return null;
    }

    for (var i = 0; i < appointmentsOfMonth.length; i++)
    {
    	if (appointmentsOfMonth[i].eventId == appointmentId)
    	{
    		return appointmentsOfMonth[i];
    	}
    }
    
    return null;
}

function validateFormData()
{
	var startHourInput = document.getElementById("startHour");
	var startHour = Number(startHourInput.options[startHourInput.selectedIndex].value);

	var endHourInput = document.getElementById("endHour");
	var endHour = Number(endHourInput.options[endHourInput.selectedIndex].value);

	var startMinuteInput = document.getElementById("startMinute");
	var startMinute = Number(startMinuteInput.options[startMinuteInput.selectedIndex].value);

	var endMinuteInput = document.getElementById("endMinute");
	var endMinute = Number(endMinuteInput.options[endMinuteInput.selectedIndex].value);
	
	console.log("startHour=" + startHour + " endHour=" + endHour + " startMinute=" + startMinute + " endMinute=" + endMinute);
	
	if (endHour < startHour)
	{
		return false;
	}
	if (endHour == startHour)
	{
		if (endMinute < startMinute)
		{
			return false;
		}
	}
	
	
    var multiDayCheckbox = document.getElementById("multiDayCheckbox");
    if (multiDayCheckbox.checked) 
    {
    	var numOfDays = document.getElementById("numOfDay").value;
    	if ((numOfDays == "") || isNaN(parseInt(numOfDays)))
    	{
    		return false;
    	}
    }
	
	return true;
}

function adjustEndTime()
{
    var startHourSel = document.getElementById("startHour");
    var startMinSel = document.getElementById("startMinute");
    var endHourSel = document.getElementById("endHour");
    var endMinSel = document.getElementById("endMinute");
    
    var startHour = Number(startHourSel.options[startHourSel.selectedIndex].value);
    var startMinute = Number(startMinSel.options[startMinSel.selectedIndex].value);
    var endHour = Number(endHourSel.options[endHourSel.selectedIndex].value);
    var endMinute = Number(endMinSel.options[endMinSel.selectedIndex].value);
    
    if ((endHour < startHour) || ((endHour == startHour) && (endMinute < startMinute)))
    {
        endHourSel.value = startHour.toString();
    	endMinSel.value = startMinute.toString();
        endHour = startHour;
        endMinute = startMinute;
    }
    
    setDurationValues(startHour, startMinute, endHour, endMinute);  
}

function adjustDuration()
{
    var startHourSel = document.getElementById("startHour");
    var startMinSel = document.getElementById("startMinute");
    var endHourSel = document.getElementById("endHour");
    var endMinSel = document.getElementById("endMinute");
    
    var startHour = Number(startHourSel.options[startHourSel.selectedIndex].value);
    var startMinute = Number(startMinSel.options[startMinSel.selectedIndex].value);
    var endHour = Number(endHourSel.options[endHourSel.selectedIndex].value);
    var endMinute = Number(endMinSel.options[endMinSel.selectedIndex].value);
    
    setDurationValues(startHour, startMinute, endHour, endMinute);  
}

function checkAlarm() 
{
	var url = "/webfilesys/servlet?command=calendar&cmd=checkAlarm";
	xmlRequest(url, handleAlarmResult);
}

function handleAlarmResult() 
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var alarmList = resultElem.getElementsByTagName("alarm");

            var alertText = "";
            var soundAlarm = false;
            
            var eventId;
            
            for (var i = 0; i < alarmList.length; i++)
            {
            	var alarm = alarmList[i];
            	
            	var eventTime = "";
            	var alarmTime = "";
            	var subject = "";
            	var alarmType = "";
            	
            	var childNodeNum = alarm.childNodes.length;
            	
            	for (var k = 0; k < childNodeNum; k++) 
            	{
            		var childNode = alarm.childNodes[k];	
            		
            		if (childNode.tagName == "eventTime")
            		{
            			eventTime = childNode.firstChild.nodeValue;
            		}
            		else if (childNode.tagName == "alarmTime")
            		{
            			alarmTime = childNode.firstChild.nodeValue;
            		}
            		else if (childNode.tagName == "subject")
            		{
            			subject = childNode.firstChild.nodeValue;
            		}
            		else if (childNode.tagName == "alarmType")
            		{
            			alarmType = childNode.firstChild.nodeValue;
            		}
            		else if (childNode.tagName == "eventId")
            		{
            			eventId = childNode.firstChild.nodeValue;
            		}
            	}
            	
            	alertText = alertText + eventTime + "<br/><br/>" + subject;
            	
            	if (i < alarmList.length - 1)
            	{
            		alertText = alertText + "<br/><br/>";
            	}
            	
            	if ((alarmType == 2) || (alarmType == 4))
            	{
            	    soundAlarm = true;	
            	}
            }
            
            if (alertText != "")
            {
            	if (soundAlarm)
            	{
           	        try 
           	        {
           	        	beep(500, 3);
           	        }
           	        catch (err)
           	        {
           	        	if (console)
           	        	{
           	        		console.log(err);
           	        	}
           	        }
            	}

            	showReminder(alertText, eventId);
            }
            
   	        setTimeout("checkAlarm()", 60000);
        }
    }
}

function showReminder(appointmentText, eventId)
{
   	reminderBox = document.createElement("div");
   	reminderBox.id = "reminderBox";
   	reminderBox.setAttribute("class", "reminderMsgBox");
   	document.documentElement.appendChild(reminderBox);
   	
   	reminderBox.style.top = (getWinHeight() / 2 - 75) + "px";
   	reminderBox.style.left = (getWinWidth() / 2 - 125) + "px";

   	reminderHeadline = document.createElement("span");
   	reminderHeadline.id = "reminderHeadline";
   	reminderHeadline.setAttribute("class", "reminderHeadline");
   	reminderHeadline.innerHTML = resourceReminder;
   	reminderBox.appendChild(reminderHeadline);

   	reminderText = document.createElement("span");
   	reminderText.id = "reminderText";
   	reminderText.setAttribute("class", "reminderText");
   	reminderText.innerHTML = appointmentText;
   	reminderBox.appendChild(reminderText);
   	
	var selectbox = document.createElement("select");
	selectbox.id = "remindAgainSel";
	selectbox.setAttribute("name", "remindAgain");
	selectbox.setAttribute("class", "remindAgain");
	
	selectbox.options[0] = new Option(resourceDontRemindAgain, 0);
	selectbox.options[1] = new Option(resourceRemindAgain + " 5 " + resourceMinute, 5);
	selectbox.options[2] = new Option(resourceRemindAgain + " 10 " + resourceMinute, 10);
	selectbox.options[3] = new Option(resourceRemindAgain + " 15 " + resourceMinute, 15);
	selectbox.options[4] = new Option(resourceRemindAgain + " 30 " + resourceMinute, 30);
	selectbox.options[5] = new Option(resourceRemindAgain + " 1 " + resourceHour, 60);
	selectbox.options[6] = new Option(resourceRemindAgain + " 2 " + resourceHour, 120);
	selectbox.options[7] = new Option(resourceRemindAgain + " 6 " + resourceHour, 370);
	selectbox.options[8] = new Option(resourceRemindAgain + " 12 " + resourceHour, 720);
	selectbox.options[9] = new Option(resourceRemindAgain + " 1 " + resourceDay, 1440);
	selectbox.options[10] = new Option(resourceRemindAgain + " 2 " + resourceDay, 2880);
	selectbox.options[11] = new Option(resourceRemindAgain + " 1 " + resourceWeek, 2880);

	reminderBox.appendChild(selectbox);
   	
	var buttonCont = document.createElement("div");
	buttonCont.id = "reminderButtonCont";
	buttonCont.setAttribute("class", "reminderButtonCont");
   	reminderBox.appendChild(buttonCont);
	
   	var clickAction = "delayOrCloseReminder('" + eventId + "')";
   	
	var okButton = document.createElement("button");
	okButton.setAttribute("class", "reminderButton");
	okButton.setAttribute("onclick", clickAction);
	okButton.innerHTML = resourceReminderCloseButton;
    buttonCont.appendChild(okButton);
    
    window.focus();
}

function delayOrCloseReminder(eventId)
{
	var remindAgainSel = document.getElementById("remindAgainSel");
	
	if (remindAgainSel.value == 0)
	{
		hideReminder();
        return;
	}

	var url = "/webfilesys/servlet?command=calendar&cmd=delay&eventId=" + eventId + "&delayMinutes=" + remindAgainSel.value;
	xmlRequest(url, handleDelayResult);
}

function handleDelayResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
    		hideReminder();
        }
    }
}

function hideReminder()
{
	var reminderBox = document.getElementById("reminderBox");
	if (reminderBox)
	{
		document.documentElement.removeChild(reminderBox);
	}
}

function beep(duration, type) 
{
    if (!(window.audioContext || window.webkitAudioContext)) 
    {
        throw Error("Your browser does not support Audio Context.");
    }

    duration = +duration;

    // Only 0-4 are valid types.
    type = (type % 5) || 0;

    var ctx = new (window.audioContext || window.webkitAudioContext);
    var osc = ctx.createOscillator();
    osc.type = type;
    osc.connect(ctx.destination);
    osc.noteOn(0);

    setTimeout(function() {
                  osc.noteOff(0);
               }, 
               duration);
}
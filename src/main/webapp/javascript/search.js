var keepSearchResults = false;
var resultsDiscarded = false;

function showResults() {
	keepSearchResults = true;
            
    if (mobile) {
        window.opener.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + searchResultDir; 
    } else {
        window.opener.parent.DirectoryPath.location.href = "/webfilesys/servlet?command=exp&expandPath=" + searchResultDir + "&fastPath=true"; 
    }

	setTimeout("self.close()", 1000);
}
			
function discardSearchResults() {
    if (keepSearchResults || resultsDiscarded) {
        return;
    }

    var discardURL = "/webfilesys/servlet?command=discardSearchResults&resultDir=" + searchResultDir;
    
	xmlRequest(discardURL, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                resultsDiscarded = true;
                setTimeout("self.close()", 1000); 
            } else {
                alert('communication error');
            }
        }
    });
}

function discardAndClose() {
    discardSearchResults();
}
	
function gotoSearchResultFolder(folderPath) {
	if (window.opener) {
		window.opener.location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(folderPath) + "&fastPath=true;"
	}
}

function addSearchTextField() {
	$(".addSearchTextButton").val("-").attr("onclick", "javascript:removeSearchTextField(this)");
	$(".addSearchTextButton").removeClass("addSearchTextButton").addClass("removeSearchTextButton");
	$("#searchTextList").append("<div class=\"searchTextCont\">" + 
                                "<input class=\"searchArg\" type=\"text\" name=\"searchText\" maxlength=\"256\" onchange=\"switchCheckboxes()\" onkeyup=\"switchCheckboxes()\" />" +
                                "<input type=\"button\" onclick=\"addSearchTextField()\" class=\"addSearchTextButton\" value=\"+\"/>" +
                                "</div>");
}

function removeSearchTextField(clickTarget) {
	clickTarget.parentNode.parentNode.removeChild(clickTarget.parentNode);
}

function switchCheckboxes() {
    var resultAsTreeCheckbox = document.getElementById("resultAsTree");
    var includeDescCheckbox = document.getElementById("includeDesc");
    var descOnlyCheckbox = document.getElementById("descOnly");
    
    var searchArgFilled = false;
    
    $(".searchArg").each(function() {
    	if ($(this).val().length > 0) {
    		searchArgFilled = true;
    	}
    });    
    
    if (searchArgFilled) {
        resultAsTreeCheckbox.checked = false;
        resultAsTreeCheckbox.disabled = true;
        includeDescCheckbox.disabled = false;
        descOnlyCheckbox.disabled = false;
    } else {
        resultAsTreeCheckbox.disabled = false;
        includeDescCheckbox.checked = false;
        includeDescCheckbox.disabled = true;
        descOnlyCheckbox.checked = false;
        descOnlyCheckbox.disabled = true;
    }
}

var selectedFromDate = null;
var selectedUntilDate = null;

$(document).ready(function() {
	var dayNamesShort = [
        resourceBundle["calendar.mon"], 
        resourceBundle["calendar.tue"], 
        resourceBundle["calendar.wed"], 
        resourceBundle["calendar.thu"], 
        resourceBundle["calendar.fri"], 
        resourceBundle["calendar.sat"], 
        resourceBundle["calendar.sun"] 
    ]; 

    $("#dateRangeFrom").datepicker({
        showButtonPanel: true,
        showOtherMonths: true,
        selectOtherMonths: true,
        changeMonth: true,
        changeYear: true,
        dateFormat: resourceBundle["datePickerFormat"],
        currentText: resourceBundle["calendar.today"],
        closeText: resourceBundle["button.close"],
        dayNamesMin: dayNamesShort,
        onSelect: function(dateText, inst) { 
                      selectedFromDate = $(this).datepicker('getDate'); 
                  }
    });
    
    $("#dateRangeUntil").datepicker({
        showButtonPanel: true,
        showOtherMonths: true,
        selectOtherMonths: true,
        changeMonth: true,
        changeYear: true,
        dateFormat: resourceBundle["datePickerFormat"],
        currentText: resourceBundle["calendar.today"],
        closeText: resourceBundle["button.close"],
        dayNamesMin: dayNamesShort,
        onSelect: function(dateText, inst) { 
                      selectedUntilDate = $(this).datepicker('getDate'); 
                  }
    });
});

function openFromDateSelection() {
    $("#dateRangeFrom").trigger("focus");
}

function openUntilDateSelection() {
    $("#dateRangeUntil").trigger("focus");
}

function submitIfValid() {

    if ((selectedFromDate != null) && (selectedUntilDate != null)) {
        if (selectedFromDate.getTime() > selectedUntilDate.getTime()) {
            customAlert(resourceBundle["label.searchDateConflict"]);
            return;
        }
    }
    
    showHourGlass();
    
    document.getElementById("searchButton").disabled = true;
    document.getElementById("cancelButton").disabled = true;
    
    var resultAsTreeCheckbox = document.getElementById("resultAsTree");
    
    if (resultAsTreeCheckbox.checked) {
        document.findform.command.value = "findFileTree";
    }
    
    document.findform.submit();
}

window.onbeforeunload = discardSearchResults;
			
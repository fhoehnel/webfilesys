var firstErrorMsg = true;

function addErrorMsg(errorMsg) 
{
    var errorMsgList = document.getElementById("errorMsgs");
    if (!errorMsgList)
    {
        return;
    }

    var errorEntry = document.createElement("li");
    errorEntry.setAttribute("class", "errorMsg");
    errorEntry.innerHTML = errorMsg;
    errorMsgList.appendChild(errorEntry);
    
    if (firstErrorMsg) 
    {
        errorMsgList.style.display = "block";
        firstErrorMsg = false;
    }
}

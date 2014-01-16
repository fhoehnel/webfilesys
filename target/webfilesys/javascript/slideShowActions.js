var buttonDivShown = false;

function showActionButtons()
{
    if (buttonDivShown)
    {
        return;
    }

    actionButtonDiv = document.getElementById('buttonDiv');
    
    actionButtonDiv.style.visibility = 'visible';
    
    buttonDivShown = true;
}

function hideActionButtons()
{
    actionButtonDiv = document.getElementById('buttonDiv');    

    actionButtonDiv.style.visibility = 'hidden';
}
function checkExclusion(clickedCheckbox, otherCheckboxId)
{
    if (clickedCheckbox.checked)
    {
        otherCheckbox = document.getElementById(otherCheckboxId);
        
        otherCheckbox.checked = false;
    }
}

function selectAllSyncItems()
{
    allSelected = true;
	
    for (i=0;i<document.form1.elements.length;i++)
    {
	if ((document.form1.elements[i].type=="checkbox") &&
            (document.form1.elements[i].name.substring(0,3)=="id-"))
        {
	    if ((document.form1.elements[i].checked==false) &&
	        (document.form1.elements[i].disabled==false))
            {
		allSelected = false;
	    }
	} 
    }
	
    if (allSelected==false)
    {
	for (i=0;i<document.form1.elements.length;i++)
        {
            if ((document.form1.elements[i].name.substring(0,3)=="id-") &&
                (document.form1.elements[i].disabled==false))
	    {
		document.form1.elements[i].checked = true;
	    }		
	}
    }
    else
    {
 	for (i=0;i<document.form1.elements.length;i++)
        {
            if (document.form1.elements[i].name.substring(0,3)=="id-")
	    {
		document.form1.elements[i].checked = false;
	    }	
	}
    }
}

function anyItemSelected()
{
    for (i = document.form1.elements.length-1; i >= 0; i--)
    {
         if ((document.form1.elements[i].type=="checkbox") &&
             (document.form1.elements[i].checked) &&
             (document.form1.elements[i].name.substring(0,3)=="id-"))
	 {
	      return(true);
	 }
    }

    return(false);
}


function handleFileListKey(evt)
{
    var keyHandled = false;
    
    if (!evt) 
    {
        if (window.event) 
        {
            evt = window.event;
        }
    }
    if (evt) 
    {
        if (evt.keyCode == 27)
        {
            // ESC key
            handleESCKey();              
            
            keyHandled = true;
        }
    }
      
    if (keyHandled) 
    {
        evt.stopPropagation();
    }
}

function handleESCKey() 
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
    
    var promptBox = document.getElementById('prompt');
    if (promptBox)
    {
        promptBox.style.visibility = 'hidden';
    }
}

function handleRowClick(evt)
{
    var clickTarget;
    if (!evt) 
    {
        var evt = window.event;
    }
    if (evt.target) 
    {
        clickTarget = evt.target;
    }
    else if (evt.srcElement) 
    {
        clickTarget = evt.srcElement;
    }
    else 
    {
        return;
    }
    if (clickTarget.nodeType == 3) 
    {
         // defeat Safari bug
	clickTarget = clickTarget.parentNode;
    }

    if (clickTarget.tagName == "A") 
    {
        return;
    }

    var clickOnCheckbox = false;
    var checkbox;

    if (clickTarget.tagName == "INPUT") {
        clickOnCheckbox = true;
        checkbox = clickTarget;
    }

    var tagName = clickTarget.tagName;
    while (tagName != "TR")
    {
	clickTarget = clickTarget.parentNode;
        tagName = clickTarget.tagName;
    }

    var tableRow = clickTarget;
    
    var leftButtonClicked = false;
    if (evt.which) 
    {
        leftButtonClicked = (evt.which == 1);
    }
    else if (evt.button) 
    {
        leftButtonClicked = (evt.button == 1);
    }

    if (browserMSIE) 
    {
        evt.cancelBubble = true;
    }
    else 
    {
        evt.stopPropagation();
    }

    if (!leftButtonClicked) {
        return;
    }

    var checkboxTableCell = tableRow.firstChild;
    if (checkboxTableCell.tagName != "TD") 
    {
        // Chrome
        checkboxTableCell = checkboxTableCell.nextSibling;
    }
    var checkbox = checkboxTableCell.firstChild;
    if (checkbox.checked)
    {
        checkbox.checked = false;
        deselectTableRow(tableRow);
    }
    else
    {
        checkbox.checked = true;
        selectTableRow(tableRow);
        if (evt.shiftKey)
        {
            selectRowRange(tableRow);
        }
    }
    
    if (clickOnCheckbox) 
    {
        checkbox.checked = (!checkbox.checked);
    }
}

function selectRowRange(clickedRow)
{
    var table = clickedRow.parentNode;
    var tableRows = table.childNodes;
    
    var clickedRowIdx = (-1);
    for (var i = 0; (clickedRowIdx < 0) && (i < tableRows.length); i++)
    {
        if (tableRows[i] == clickedRow)
        {
            clickedRowIdx = i;
        }
    }
    
    if (clickedRowIdx < 0)
    {
        // should not happen
        return;
    }

    
    for (var k = clickedRowIdx - 1; k >= 0; k--)
    {
        if (tableRows[k].nodeType == 1)
        {
            var checkboxTableCell = tableRows[k].firstChild;
            if (checkboxTableCell.nodeType != 1) 
            {
                checkboxTableCell = checkboxTableCell.nextSibling;
            }
            var checkbox = checkboxTableCell.firstChild;
            if (checkbox.checked)
            {
                for (var s = clickedRowIdx - 1; s > k; s--) 
                {
                    setTableRowSelected(tableRows[s]);
                }
            
                return;
            }
        }
    }

    for (var k = clickedRowIdx + 1; k < tableRows.length; k++)
    {
        if (tableRows[k].nodeType == 1)
        {
            var checkboxTableCell = tableRows[k].firstChild;
            if (checkboxTableCell.nodeType != 1) 
            {
                checkboxTableCell = checkboxTableCell.nextSibling;
            }
            var checkbox = checkboxTableCell.firstChild;
            if (checkbox.checked)
            {
                for (var s = clickedRowIdx + 1; s < k; s++) 
                {
                    setTableRowSelected(tableRows[s]);
                }
            
                return;
            }
        }
    }
}

function setTableRowSelected(tableRow)
{
    if (tableRow.nodeType != 1) 
    {
        return;
    }

    var checkboxTableCell = tableRow.firstChild;
    if ((checkboxTableCell.nodeType != 1) || (checkboxTableCell.tagName != "TD")) 
    {
        // Chrome
        checkboxTableCell = checkboxTableCell.nextSibling;
    }
    var checkbox = checkboxTableCell.firstChild;
    if (checkbox.tagName == "INPUT") 
    {
        // skip description rows
        checkbox.checked = true;
        selectTableRow(tableRow);
    }
}

function deselectTableRow(tableRow) 
{
    var children = tableRow.childNodes;
    for (var i = 0; i < children.length; i++)
    {
         var child = children[i];
         if (child.tagName == "TD")
         {
             var oldCSS;
             if (browserMSIE)
             { 
                 oldCSS = child.getAttribute("className");
             }
             else
             {
                 oldCSS = child.getAttribute("class");
             }
             var selectedStyleIdx = oldCSS.indexOf("fileListSelectedRow");
             if (selectedStyleIdx > 0)
             {
                 var newCSS = oldCSS.substring(0, selectedStyleIdx);
                 if (browserMSIE) 
                 {
                     child.setAttribute("className", newCSS);  
                 }
                 else 
                 {
                     child.setAttribute("class", newCSS);
                 }
             }
         }
    }
}

function selectTableRow(tableRow) 
{
    var children = tableRow.childNodes;
    for (var i = 0; i < children.length; i++)
    {
         var child = children[i];
         if (child.tagName == "TD")
         {
             var oldCSS;
             if (browserMSIE)
             { 
                 oldCSS = child.getAttribute("className");
             }
             else
             {
                 oldCSS = child.getAttribute("class");
             }
             
             var newCSS = oldCSS + " fileListSelectedRow";
             
             if (browserMSIE) 
             {
                 child.setAttribute("className", newCSS);  
             }
             else 
             {
                 child.setAttribute("class", newCSS);
             }
         }
    }
}

function setAllFilesSelected()
{
    selectAll();

    var fileListTable = document.getElementById("tableFileList");
    if (!fileListTable)
    {
        return;
    }

    var tbody = fileListTable.firstChild;
    if (tbody.nodeType != 1) 
    {
        tbody = tbody.nextSibling;
    }

    var tableRows = tbody.childNodes;
    
    for (var i = 0; i < tableRows.length; i++)
    {
         if (tableRows[i].nodeType == "1") 
         {
             var tableRow = tableRows[i];
             
             var checkboxTableCell = tableRow.firstChild;
             
             if (checkboxTableCell) 
             {
                 if ((checkboxTableCell.nodeType != 1) || (checkboxTableCell.tagName != "TD")) 
                 {
                     checkboxTableCell = checkboxTableCell.nextSibling;
                 }

                 if (checkboxTableCell && (checkboxTableCell.tagName == "TD")) 
                 {
                     var checkbox = checkboxTableCell.firstChild;
                     if (checkbox.checked)
                     {
                         selectTableRow(tableRow);
                     }
                     else 
                     {
                         deselectTableRow(tableRow);
                     }
                 }
             }
        }
    }
}

function setAllFilesUnselected()
{
    var fileListTable = document.getElementById("tableFileList");
    if (!fileListTable)
    {
        return;
    }

    var tbody = fileListTable.firstChild;
    if (tbody.nodeType != 1) 
    {
        tbody = tbody.nextSibling;
    }

    var tableRows = tbody.childNodes;
    
    for (var i = 0; i < tableRows.length; i++)
    {
         if (tableRows[i].nodeType == "1") 
         {
             var tableRow = tableRows[i];
             
             var checkboxTableCell = tableRow.firstChild;
             
             if (checkboxTableCell) 
             {
                 if ((checkboxTableCell.nodeType != 1) || (checkboxTableCell.tagName != "TD")) 
                 {
                     checkboxTableCell = checkboxTableCell.nextSibling;
                 }

                 if (checkboxTableCell && (checkboxTableCell.tagName == "TD")) 
                 {
                     deselectTableRow(tableRow);
                 }
             }
        }
    }
}
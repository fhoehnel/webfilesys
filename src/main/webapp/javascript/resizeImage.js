function handleTargetSizeSelection(selectbox)
{
    var targetSizeRow = document.getElementById("targetSizeRow");

    var targetSize = selectbox.options[selectbox.selectedIndex].value;
    
    if (targetSize == "-1")
    {
        targetSizeRow.style.display = "";
        var targetSizeField = document.getElementById("targetSize");
        targetSizeField.focus();
        targetSizeField.select();
    }
    else
    {
        targetSizeRow.style.display = "none";
    }
}

function switchCopyRightFields()
{
    if (document.form1.stampText.checked == true)
    {
        document.form1.copyRightText.disabled = false;
        document.form1.copyRightPos.disabled = false;
        document.form1.copyRightColor.disabled = false;
        document.form1.copyRightFontSize.disabled = false;
    }
    else
    {
        document.form1.copyRightText.disabled = true;
        document.form1.copyRightPos.disabled = true;
        document.form1.copyRightColor.disabled = true;
        document.form1.copyRightFontSize.disabled = true;
    }
}


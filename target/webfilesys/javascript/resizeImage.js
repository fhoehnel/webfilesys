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

function prepareCropAreaParms()
{
    var picture = document.getElementById('editPicture');
    var picturePos = findPos(picture);

    var pictureXpos = picturePos[0];
    var pictureYpos = picturePos[1];

    var selectorLeft = parseInt(areaSelector.style.left);
    var selectorTop = parseInt(areaSelector.style.top);

    var pictureLeft = selectorLeft - pictureXpos;
    var pictureTop = selectorTop - pictureYpos + 1;
    var pictureWidth = parseInt(areaSelector.style.width) + 2;   
    var pictureHeight = parseInt(areaSelector.style.height) + 3;

    document.form1.cropAreaLeft.value = pictureLeft;
    document.form1.cropAreaTop.value = pictureTop;
    document.form1.cropAreaWidth.value = pictureWidth;
    document.form1.cropAreaHeight.value = pictureHeight;
}

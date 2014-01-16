// Firefox 2/3
// var browserFirefox = /a/[-1]=='a';
var browserFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;

// MSIE 6/7/8:
var browserMSIE = '\v'=='v';

// MSIE 9
var browserMSIE9 = false
if (document.all && document.addEventListener) {
    browserMSIE = true;
	browserMSIE9 = true;
}

// Google Chrome
// var browserChrome = /source/.test((/a/.toString+''));
var browserChrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;

var browserSafari = /a/.__proto__=='//';

var browserOpera = false;
if (window.opera)
{
    browserOpera = true;
}


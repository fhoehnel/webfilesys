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

// var browserSafari = /a/.__proto__=='//';
var browserSafari = navigator.vendor.indexOf("Apple")==0 && /\sSafari\//.test(navigator.userAgent);

var browserOpera = false;
if (window.opera)
{
    browserOpera = true;
}

// var browserEdge = (navigator.userAgent.toLowerCase().indexOf('Edge/') > -1);

var osAndroid = (navigator.userAgent.toLowerCase().indexOf('android') > -1);


/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.colors = jlab.colors || {};
// Inj, North, South, Total, Unknown
jlab.colors.linacs = ["#FF0000", "#006400", "#273BE7", "#333333", "#ECAF2F"];
// C25, C50, C100, Total, Unknown
jlab.colors.cmtypes = ["#FFCE00", "#0375B4", "#007849", "#333333", "#ECAF2F"];

// This assumes that you are passing an array of flot data series (a 3d array in all).  It returns the smallest difference
// between any two points in a single series.
jlab.getMinDataWidth = function (data) {
    var min = Number.MAX_SAFE_INTEGER;
    var maxLength = 0;

    for (var i = 0; i < data.length; i++) {
        if ( data[i].length > maxLength ) {
            maxLength = data[i].length;
        }
        for (var j = 1; j < data[i].length; j++) {
            width = data[i][j][0] - data[i][j - 1][0];
            if (min > width) {
                min = width;
            }
        }
    }
    
    // If only one datapoint is in the list, assume a width of 1 day
    if ( maxLength === 1 ) {
        min = 60 * 60 * 24 * 1000;
    }
    return min;
};

jlab.triCharMonthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

jlab.showTooltip = function (x, y, contents, z) {
    $('<div id="flot-tooltip">' + contents + '</div>').css({
        top: y,
        left: x,
        'border-color': z
    }).appendTo("body").show();
};

// date: a yyyy-mm-dd formated string
// numDays: number of days to add
jlab.addDays = function(date, numDays) {
    var dateParts = date.split('-');
    var y = parseInt(dateParts[0], 10);
    var m = parseInt(dateParts[1], 10);
    var d = parseInt(dateParts[2], 10);
    
    var nDate = new Date(y, m-1, d + numDays);
    var ny = nDate.getFullYear();
    var nm = nDate.getMonth() + 1;
    if (nm < 10) {
        nm = "0" + nm;
    }

    var nd = nDate.getDate();
    if (nd < 10) {
        nd = "0" + nd;
    }
    
    // javascript Date month is an enum (zero-indexed)
    return ny + "-" + nm + "-" + nd;
};

// Turn javascript time (in milliseconds) to our standard yyyy-MM-dd format truncating the hh:mm:ss portions
jlab.millisToDate = function(time) {
    var date = new Date(time);
    
    // check if the number has at least two digits.  Add a zero in front if not.
    var pad = function(num) {
        return (num/10 < 1 ? '0' : '') + "" + num;
    };
    
    // All of our server-side dates are handled as UTC and are truncated to the day.  Using methods like getDate()
    // return the date in local time which would be off by a day when we do the -4/5 hours for EDT/EST.
    var out = date.getUTCFullYear() + "-" + pad(date.getUTCMonth()+1) + "-" + pad(date.getUTCDate());
    console.log(out);
    console.log(date.toISOString());
    return out;
};
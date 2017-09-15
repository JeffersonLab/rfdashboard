/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.util = jlab.util || {};

jlab.contextPath = '/RFDashboard';
jlab.isRequest = function () {
    return jlab.ajaxInProgress;
};
jlab.requestStart = function () {
    jlab.ajaxInProgress = true;
};
jlab.requestEnd = function () {
    jlab.ajaxInProgress = false;
};

jlab.colors = jlab.colors || {};
// Inj, North, South, Total, Unknown
jlab.colors.linacs = ["#FF0000", "#006400", "#273BE7", "#333333", "#ECAF2F"];
// C25, C50, C100, Total, Unknown
jlab.colors.cmtypes = ["#FFCE00", "#0375B4", "#007849", "#333333", "#ECAF2F"];
// Total 1050, Total 1050 No M.A.V, Total 1090, Total 1090 No M.A.V
jlab.colors.modAnodeHarvester = ["#5e3c99", "#b2abd2", "#e66101", "#fdb863"];
jlab.colors.energyReach = ["#666666"];

// Default JQuery dialog properties
jlab.dialogProperties = {
    autoOpen: false,
    modal: true,
    midWidth: 300,
    maxWidth: 1400,
    width: 800,
    show: {
        effect: "blind",
        duration: 500
    },
    hide: {
        effect: "blind",
        duration: 500
    }
};

jlab.sizes = jlab.sizes || {};
jlab.sizes.pageDetailsMinWidth = 300;
jlab.sizes.pageDetailsMaxWidth = 1400;
jlab.sizes.pageDetailsWidth = 800;

// This assumes that you are passing an array of flot data series (a 3d array in all).  It returns the smallest difference
// between any two points in a single series.
jlab.getMinDataWidth = function (data) {
    var min = Number.MAX_SAFE_INTEGER;
    var maxLength = 0;

    for (var i = 0; i < data.length; i++) {
        if (data[i].length > maxLength) {
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
    if (maxLength === 1) {
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
jlab.addDays = function (date, numDays) {
    var dateParts = date.split('-');
    var y = parseInt(dateParts[0], 10);
    var m = parseInt(dateParts[1], 10);
    var d = parseInt(dateParts[2], 10);

    var nDate = new Date(y, m - 1, d + numDays);
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

// Turn javascript time (in milliseconds) to our standard yyyy-MM-dd format string truncating the hh:mm:ss portions
jlab.millisToDate = function (time) {
    var date = new Date(time);

    // check if the number has at least two digits.  Add a zero in front if not.
    var pad = function (num) {
        return (num / 10 < 1 ? '0' : '') + "" + num;
    };

    // All of our server-side dates are handled as UTC and are truncated to the day.  Using methods like getDate()
    // return the date in local time which would be off by a day when we do the -4/5 hours for EDT/EST.
    var out = date.getUTCFullYear() + "-" + pad(date.getUTCMonth() + 1) + "-" + pad(date.getUTCDate());
    console.log(out);
    console.log(date.toISOString());
    return out;
};

// This calculates the number days between two yyyy-MM-dd formated date strings.  Converts them both to UTC as it
// ignores DST changes and days are always 24*60*60*1000 milliseconds long.
jlab.daysBetweenDates = function (d1, d2) {
    var d1 = new Date(d1);  // d1 is ISO 8061 compliant.  Without time info this becomes UTC.
    var d2 = new Date(d2);

    var millisPerDay = 24 * 60 * 60 * 1000;
    return (d2 - d1) / millisPerDay;
};

// This does a decent job of parsing out argument names of a function.
// It gets tripped up by things like defualt values using '()' (e.g. a  = 1 / (5*7)),
// but I don't tend to do those things.
jlab.getArgNames = function (func) {

    // Big chain of functions...
    var args = func.toString()
            // Remove inline comments.
            .replace(/\/\*.*\*\//, '')
            // Match everything inside the function argument parens.
            .match(/function\s.*?\(([^)]*)\)/)[1]
            // Split to array on commas
            .split(',')
            // Remove unnecessary whitespace
            .map(function (arg) {
                return arg.trim();
            })
            // Ensure no undefined values are added
            .filter(function (arg) {
                return arg;
            });

    return args;
};

// Show a loading icon for the given chartId.
// To be used with the chart-widget tag
jlab.showChartLoading = function (chartId) {
    $('#' + chartId + '-loader').show();
};

jlab.util.showTableLoading = function (widgetId) {
    $(widgetId + ' .ajax-loader').show();
};
jlab.util.hideTableLoading = function (widgetId, msg) {
    if (typeof msg !== "undefined" && msg !== "") {
        $(widgetId + '.table-panel').prepend(msg);
    }
    $(widgetId + ' .ajax-loader').hide();
};

// Hide a loading icon for the given HTML element and optionally display message.
// To be used with the chart-widget tag
jlab.hideChartLoading = function (chartId, msg) {
    if (typeof msg !== "undefined" && msg !== "") {
        $("#" + chartId).prepend(msg);
    }
    $('#' + chartId + "-loader").hide();
};

// Prints a formated time string similar to ISO 8601, but in local time and with out the 'T'
jlab.formatTimestampPretty = function (date) {

    function pad(number) {
        if (number < 10) {
            return '0' + number;
        }
        return number;
    }

    return this.getFullYear() +
            '-' + pad(this.getMonth() + 1) +
            '-' + pad(this.getDate()) +
            ' ' + pad(this.getHours()) +
            ':' + pad(this.getMinutes()) +
            ':' + pad(this.getSeconds());
};

// Note: to be used with a tablesorter widget
//jlab.util.createTableSorterTable = function (tableId, contents) {
//    if (typeof contents.data === "undefined") {
//        console.log("No data supplied to createTableSorterTable");
//        return;
//    }
//    var data = contents.data;
//    var sortList = contents.sortList || [[0, 0]]; // Default to first column ascending
//
//    // Build the table HTML
//    var tableString = "<table class=\"tablesorter\">";
//    for (var i = 0; i < data.length; i++) {
//        if (i === 0) {
//
//            // Header row
//            tableString += "<thead><tr>";
//            for (var j = 0; j < data[i].length; j++) {
//                tableString += "<th>" + data[i][j] + "</th>";
//            }
//            tableString += "</tr></thead><tbody>";
//        } else {
//
//            // Body rows
//            tableString += "<tr>";
//            for (var j = 0; j < data[i].length; j++) {
//                tableString += "<td>" + data[i][j] + "</td>";
//            }
//            tableString += "</tr>";
//        }
//    }
//
//    // Append the table HTML
//    $("#" + tableId + "-table").append(tableString);
//    // Setup the sortable functionality
//    $(".tablesorter")
//            .tablesorter({"sortList": sortList}) // sort on the first column (asc)
//            .tablesorterPager({container: $("#" + tableId + "-pager")});
//};


// Note: to be used with a tablesorter widget
jlab.util.createTableSorterTable = function (widgetId, contents) {
    if (typeof contents.data === "undefined") {
        console.log("No data supplied to createTableSorterTable");
        return;
    }
    var data = contents.data;
    var sortList = contents.sortList || [[0, 0]]; // Default to first column ascending

    // Build the table HTML
    var tableString = "";
    for (var i = 0; i < data.length; i++) {
        if (i === 0) {

            // Header row
            tableString += "<thead><tr><th></th>";
            for (var j = 0; j < data[i].length; j++) {
                tableString += "<th>" + data[i][j] + "</th>";
            }
            tableString += "</tr></thead><tbody>";
        } else {

            // Body rows
            tableString += '<tr><td><input type="checkbox"></td>';
            for (var j = 0; j < data[i].length; j++) {
                tableString += "<td>" + data[i][j] + "</td>";
            }
            tableString += "</tr>";
        }
    }
    tableString += "</tbody>";

    $(widgetId + " table").append(tableString);
    jlab.tableSorter.initTable(widgetId);
};

/*
 * This checks that the maps have the same keysets.  True if they do, false if they don't.
 * @param {type} m1 First map
 * @param {type} m2 Second map
 * @returns boolean
 */
jlab.util.compareMapKeySets = function (m1, m2) {
    // Check that the two maps contain identical cavity set sizes
    if (m1.keys().length !== m2.keys().length) {
        return false;
    }
    // Check that every key from the first is found in the second.
    for (let k1 of m1.keys()) {
        var match = false;
        for (let k2 of m2.keys()) {
            if (k1 === k2) {
                match = true;
            }
        }
        if (!match) {
            return false;
        }
    }
    // If both conditions hold, then the sets are identical.
    return true;
};

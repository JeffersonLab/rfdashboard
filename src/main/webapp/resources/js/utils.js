/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.util = jlab.util || {};

// Setup commonly used URLs
jlab.contextPath = '/RFDashboard';
jlab.util.energyReachUrl = jlab.contextPath + "/ajax/lem-scan";
jlab.util.cavityAjaxUrl = jlab.contextPath + "/ajax/cavity";
jlab.util.cedUpdateHistoryAjaxUrl = jlab.contextPath + "/ajax/ced-update-history";
jlab.util.commentsAjaxUrl = jlab.contextPath + "/ajax/comments";
jlab.util.commentFilterAjaxUrl = jlab.contextPath + "/ajax/comment-filter";
jlab.util.newCommentUrl = jlab.contextPath + "/comments/new-comment";

/* globally enchance String object */
if (!String.prototype.encodeXml) {
    String.prototype.encodeXml = function () {
        return this.replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/'/g, '&apos;')
                .replace(/"/g, '&quot;');
    };
}
if (!String.prototype.decodeXml) {
    String.prototype.decodeXml = function () {
        return this.replace(/&quot;/g, '"')
                .replace(/&apos;/g, '\'')
                .replace(/&gt;/g, '>')
                .replace(/&lt;/g, '<')
                .replace(/&amp;/g, '&');
    };
}

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
jlab.colors = {
    "Reach": "#666666", "Total":"#333333", "Unknown": "#ECAF2F",
    "Injector": "#FF0000", "North": "#006400", "South": "#273BE7",
    "C100": "#FFCE00", "C25": "#0375B4", "C50": "#007849", "C75": "#239E23",
    "C50T": "#0ff0aa", "F100": "#e80000", "QTR": "#AF7171",
    "Total_1050": "#5e3c99", "Total_1050_NoMAV": "#b2abd2", "Total_1090": "#e66101", "Total_1090_NoMAV": "#fdb863"
};

// Default JQuery dialog properties
jlab.dialogProperties = {
    autoOpen: false,
    modal: true,
    maxHeight: 700,
    midWidth: 300,
    maxWidth: 1400,
    width: 850,
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
    return jlab.addTime(date, "day", numDays);
};


// This function takes a formatted date string, and adds the specified number of unit time units to it.
// Returns the updated date string.
// date: yyyy-mm-dd formated date string
// unit: 'day', 'month', or 'year'
// number: the number of time units to increment the date parameter
jlab.addTime = function(date, unit, number) {
    var dateParts = date.split('-');
    var y = parseInt(dateParts[0], 10);
    var m = parseInt(dateParts[1], 10);
    var d = parseInt(dateParts[2], 10);

    switch (unit) {
        case 'day':
            d = d + number;
            break;
        case 'month':
            m = m + number;
            break;
        case 'year':
            y = y + number;
            break;
        default:
            console.log("Error in jlab.addTime():  Unrecognized time unit");
            return null;
    }

    var nDate = new Date(y, m - 1, d);
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


// This returns a date string (YYYY-mm-dd that is the end date adjusted to the week boundary that occurs after,
// but nearest to end.
jlab.adjustEndToWeekBoundary = function(date1, date2) {
    var days = jlab.daysBetweenDates(date1, date2);
    var d2 = new Date(date2);

    // Set the date to whatever it was plus the number needed to hit the next week boundary (7 day grouping).
    d2.setDate(d2.getDate() + 7 - (days % 7));
    return jlab.formatDatePretty(d2);
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


jlab.formatDatePretty = function(date) {
    function pad(number) {
        if (number < 10) {
            return '0' + number;
        }
        return number;
    }

    return date.getUTCFullYear() +
        '-' + pad(date.getUTCMonth() + 1) +
        '-' + pad(date.getUTCDate());
}

/*
 * Generates a simple HTML table from a 2D arary.  Uses the first row as headers.
 * @param {type} array A 2D array
 * @returns String A string containing the HTML definition of the table
 */
jlab.util.createHTMLTable = function (array) {
    var ts = "<table>";
    for (let i = 0; i < array.length; i++) {
        if (i === 0) {
            ts += "<thead><tr>";
            for (let j = 0; j < array[i].length; j++) {
                ts += "<th>" + array[i][j] + "</th>";
            }
            ts += "</tr></thead><tbody>";
        } else {
            ts += "<tr>";
            for (let j = 0; j < array[i].length; j++) {
                ts += "<td>" + array[i][j] + "</td>";
            }
            ts += "</tr>";
        }
    }
    ts += "</tbody>";
    return ts;
};


// Note: to be used with a tablesorter widget
jlab.util.createTableSorterTable = function (widgetId, contents) {
    if (typeof contents.data === "undefined") {
        console.log("No data supplied to createTableSorterTable");
        return;
    }
    var data = contents.data;

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
 * Array.prototype.includes is not supported by IE, but indexOf is pretty universal.  If array doesn't contain value, then indexOf
 * returns -1.  Otherwise it returns the index.
 * @param {type} array
 * @param {type} value
 * @returns {Boolean}
 */
jlab.util.arrayIncludes = function (array, value) {
    var pos = array.indexOf(value);
    if (pos >= 0) {
        return true;
    }
    return false;
};

/*
 * This checks that the maps have the same keysets.  True if they do, false if they don't.
 * @param {type} m1 First map
 * @param {type} m2 Second map
 * @returns boolean
 */
jlab.util.compareMapKeySets = function (m1, m2) {
    var nkeys1 = 0;
    var nkeys2 = 0;

    // Check that every key from the first is found in the second.
//    for (let i = 0; i < m1k.length; i++) {
    m1.forEach(function (v1, k1) {
        nkeys1++;
        var match = false;
        m2.forEach(function (v2, k2) {
            // Don't want to multi count the keys in the inner loop
            if (nkeys1 === 1) {
                nkeys2++;
            }
            if (k1 === k2) {
                match = true;
            }
        });
        if (!match) {
            return false;
        }
    });
    // Check that the two maps contain identical cavity set sizes
    if (nkeys1 !== nkeys2) {
        return false;
    }

    // If both conditions hold, then the sets are identical.
    return true;
};

/*Custom time picker*/
jlab.util.dateTimePickerControl = {
    create: function (tp_inst, obj, unit, val, min, max, step) {
        $('<input class="ui-timepicker-input" value="' + val + '" style="width:50%">')
                .appendTo(obj)
                .spinner({
                    min: min,
                    max: max,
                    step: step,
                    change: function (e, ui) { // key events
                        // don't call if api was used and not key press
                        if (e.originalEvent !== undefined)
                            tp_inst._onTimeChange();
                        tp_inst._onSelectHandler();
                    },
                    spin: function (e, ui) { // spin events
                        tp_inst.control.value(tp_inst, obj, unit, ui.value);
                        tp_inst._onTimeChange();
                        tp_inst._onSelectHandler();
                    }
                });
        return obj;
    },
    options: function (tp_inst, obj, unit, opts, val) {
        if (typeof (opts) === 'string' && val !== undefined)
            return obj.find('.ui-timepicker-input').spinner(opts, val);
        return obj.find('.ui-timepicker-input').spinner(opts);
    },
    value: function (tp_inst, obj, unit, val) {
        if (val !== undefined)
            return obj.find('.ui-timepicker-input').spinner('value', val);
        return obj.find('.ui-timepicker-input').spinner('value');
    }
};

jlab.util.initDateTimePickers = function () {
    $('.datetime-picker').datetimepicker({
        controlType: jlab.util.dateTimePickerControl,
        dateFormat: 'yy-mm-dd',
        timeFormat: 'HH:mm:ss'
    });
};

/* calendar-start-end functions */
jlab.util.initCalendarStartEnd = function(widgetId) {
    var startCal = $(widgetId + " #start");
    var endCal = $(widgetId + " #end");
    var today = jlab.millisToDate(Date.now());

    // Year to date
    $(widgetId + " .year-button").on("click", function() {
        startCal.datepicker("setDate", jlab.addTime(today, "year", -1));
        endCal.datepicker("setDate", today);
    });

    // Month to date
    $(widgetId + " .month-button").on("click", function() {
        startCal.datepicker("setDate", jlab.addTime(today, "month", -1));
        endCal.datepicker("setDate", today);
    });

    // Week to date
    $(widgetId + " .week-button").on("click", function() {
        startCal.datepicker("setDate", jlab.addTime(today, "day", -7));
        endCal.datepicker("setDate", today);
    });
};

/*
 * Add a horizontal legend to a div
 * id - the id of the element to prepend legend tag (no leading '#')
 * colors - array with fill colors that match the chart
 * labels - array with color labels that match the chart
 * colorFirst - should the color appear before the label
 */
jlab.util.addLegend = function (id, colors, labels, colorFirst=false) {
    var legendString = "<div class=chart-legend id=" + id + '-legend">\n';
    legendString += "<table>";
    if (colors.length !== labels.length) {
        console.log("Error: unequal number of colors and labels");
    }
    legendString += '<tr>';
    for (var i = 0; i < colors.length; i++) {
        if (colorFirst) {
            legendString += '<td><div class=color-box style="background-color: ' + colors[i] + ';"></div></td>' + '<td>' + labels[i] + '</td>';
        } else {
            legendString += '<td>' + labels[i] + '</td>' + '<td><div class=color-box style="background-color: ' + colors[i] + ';"></div></td>';
        }
    }
    legendString += '</tr>';

    legendString += '</table></div>';
    $("#" + id).prepend(legendString);
};
var jlab = jlab || {};
jlab.flotCharts = jlab.flotCharts || {};

//+++++++++++++++++++++++++++++++++++++++++++++++++
// Plot functions
//+++++++++++++++++++++++++++++++++++++++++++++++++

// updateChart takes a single settings objecti and recognizes the following parameters:
// 
// chartId - the html ID of the chart placeholder div without the preceding '#' (REQUIRED)
// data - a flot data object that must contain at a minimum an array containing series of flot error bar data.  E.g.,
// data: [ [ [millies, mean, sigma], [millies, mean, sigma]... ],
//           [ [millies,...], ...],
//            ...
//         ]
// flotOptions - any valid subset of a flot options object.  The values are used to overwrite the functions defuaults
// settings - an object containing other settings information.  Valid parameters include:
//     - colors: an array of color names for the series
//     - labels: an array of HTML strings that label the series (used in legend and as flotData.label)
//     - timeUnit: a string defining alternatively the distance between data points or the time span for which statistics are calculated.
//     - title: title of the chart
//     - tooltip: true or false to enable/disable tooltips.  Useful for setting up more complicated behavior

jlab.flotCharts.drawChart = function (chartId, data, flotOptions, settings) {
    // Add a title if specified - do this first so any error messages that get displayed have some context
    if (typeof settings.title !== "undefined") {
        jlab.flotCharts.addTitle(chartId, settings.title);
    }

    var exitFunc = function (msg) {
        $("#" + chartId).prepend(msg);
        window.console && console.log(msg);
        throw msg;
    };    

    // Required
    if (typeof chartId === "undefined") {
        exitFunc("Error: chartId required.");
    }
    if (!$.isArray(data)) {
        exitFunc("Error: Data format incorrect");
    } else if (typeof data[0] === "undefined" || !$.isArray(data[0].data)) {
        // Possible that no data exists.  Should return nicely, not throw an exception
        $("#" + chartId).prepend("No data available");
        return;
    }
    if (typeof settings === "undefined") {
        exitFunc("Error: Settings required");
    }
    if (typeof settings.colors === 'undefined' ) {
        exitFunc("Error: settings.colors required");
    }

    var options = {
        xaxis: {
            mode: "time",
            timeformat: "%b %d<br />%Y",
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5
        },
        yaxis: {
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5
        },
        grid: {
            hoverable: true,
            clickable: true,
            borderWidth: 1
        },
        legend: {
            show: false,
            labelBoxBorderColor: "none",
            position: "right"
        },
        series: {
            shadowSize: 1
        },
        zoom: {
            interactive: false
        },
        pan: {
            interactive: false
        }
    };

    // Bar plot customizations to options
    if (typeof settings.chartType !== "undefined" && settings.chartType === "bar") {
        var tickOpts = jlab.flotCharts.getSmallDataXAxisTickOptions(data, settings.timeUnit);
        // tickOpts will be undefined if the data size is large enough
        if (typeof tickOpts !== "undefined") {
            options.xaxis.ticks = tickOpts.ticks;
            options.xaxis.tickSize = tickOpts.tickSize;
        }
    }

    // Do a deep, recursive overwrite of the supplied flotOptions onto the default options object
    if (typeof flotOptions !== 'undefined' & flotOptions !== null) {
        $.extend(true, options, flotOptions);
    }

    // Setup the data object
    var plotData = [];
    if (typeof settings.chartType !== "undefined" && settings.chartType === "bar") {
        // Bar plot data settings

        // Days / number of bars * (1-(%gap_between_datapoints/100))
        var numSeries = data.length;
        var dataPointWidth = jlab.flotCharts.getMinDataWidth(data);
        var barWidth = dataPointWidth / numSeries * (1 - 0.2);
        for (i = 0; i < data.length; i++) {
            plotData[i] = {
                label: settings.labels[i],
                data: data[i].data,
                bars: {
                    order: i + 1,
                    show: true,
                    lineWidth: 1,
                    fill: true
                }
            };

            plotData[i].color = settings.colors[i];
            plotData[i].bars.fillColor = settings.colors[i];
            if (barWidth !== Number.MAX_SAFE_INTEGER) {
                plotData[i].bars.barWidth = barWidth;
            }
        }
    } else if (typeof settings.chartType !== "undefined" && settings.chartType === "errorBar") {
        for (i = 0; i < data.length; i++) {
            plotData[i] = {
                points: {
                    show: true,
                    // The scale factor helps small datasets look better
                    radius: 3 + 10 / (data[i].data.length),
                    errorbars: "y",
                    // The scale factor helps small datasets look better
                    yerr: {show: true, upperCap: "-", lowerCap: "-", radius: 5 + 10 / (data[i].data.length)}
                }
            };
        }
        options.zoom.interactive = true;
        options.pan.interactive = true;
    }else {
        // Default point plot data settings
        for (i = 0; i < data.length; i++) {
            plotData[i] = {points: {show: true}, color: settings.colors[i], label: settings.labels[i]};
        }
    }

    // Update the plotData objects with whatever the user supplied as part of the data parameter.
    $.extend(true, plotData, data);
    
    // Draw the flot plot
    var plot = $.plot($("#" + chartId), plotData, options);
    
    // Add a custom legend off to the side
    if ( settings.legend ) {
         jlab.flotCharts.addLegend(chartId, settings.colors, settings.labels);
     }

    // Add tooltips with detailed info
    if ( settings.tooltips ) {
        if ( settings.chartType === "errorBar" ) {
           jlab.flotCharts.addErrorBarToolTip(chartId, settings.tooltipX, settings.timeUnit);
        } else {
           jlab.flotCharts.addXYToolTip(chartId, settings.tooltipX, settings.tooltipY)();
        }
    }
    
    return plot;
};



//            // These prev_* variables track whether or not the plothover event is for the same bar -- removes the "flicker" effect.
//            var prev_point = null;
//            var prev_label = null;
//            
//            $('#' + chartId).bind("plothover", function (event, pos, item) {
//                if (item) {
//                    if ((prev_point != item.dataIndex) || (prev_label != item.series.label)) {
//                        prev_point = item.dataIndex;
//                        prev_label = item.series.label;
//
//                        $('#flot-tooltip').remove();
//                        // The item.datapoint is x coordinate of the bar, not of the original data point.  item.series.data contains
//                        // the original data, and item.dataIndex this item's index in the data.
//                        var timestamp = item.series.data[item.dataIndex][0];
//                        var d = new Date(timestamp);
//                        var time = jlab.triCharMonthNames[d.getMonth()] + " " + d.getDate();
//                        var y = item.datapoint[1];
//                        var borderColor = item.series.color;
//                        var content = "<b>Series:</b> " + item.series.label + "<br /><b>Date:</b> " + time + "<br /><b>Value:</b> " + y;
//                        jlab.showTooltip(item.pageX + 20, item.pageY - 20, content, borderColor);
//                    }
//                } else {
//                    $('#flot-tooltip').remove();
//                    prev_point = null;
//                    prev_label = null;
//                }
//            });


//+++++++++++++++++++++++++++++++++
// Tooltip Functions
//+++++++++++++++++++++++++++++++++

// NOTE: this is likely unused, but left here for future use cases.
// This function returns a closure that allows the inner function to keep track of whether or not the currently "hovered" point
// is new or has changed from before.  Call with extra set up parens.  E.g., addErrorBarToolTip(chartId)();
jlab.flotCharts.addErrorBarToolTip = function(chartId, xlabel, timeUnit) {
    // These prev_* variables track whether or not the plothover event is for the same bar -- removes the "flicker" effect.
    var prev_point = null;
    var prev_label = null;
    return function () {
        $('#' + chartId).bind("plothover", function (event, pos, item) {
            if (item) {
                if ((prev_point !== item.dataIndex) || (prev_label !== item.series.label)) {
                    prev_point = item.dataIndex;
                    prev_label = item.series.label;
                    $('#flot-tooltip').remove();
                    
                    var x;
                    if (xlabel === "Date") {
                        // The item.datapoint is x coordinate of the bar, not of the original data point.  item.series.data contains
                        // the original data, and item.dataIndex this item's index in the data.
                        var timestamp = item.series.data[item.dataIndex][0];
                        var start = new Date(timestamp);
                        var end;

                        if (timeUnit === "week") {
                            end = new Date(timestamp);
                            end.setDate(end.getDate() + 7);
                            end = jlab.triCharMonthNames[end.getMonth()] + " " + end.getDate();
                            x = start + " - " + end;
                        } else if (timeUnit === "day") {
                            end = new Date(timestamp);
                            end.setDate(end.getDate() + 1);
                            end = jlab.triCharMonthNames[end.getMonth()] + " " + end.getDate();
                            x = start + " - " + end;
                        } else {
                            x = "Begining " + start;
                        }
                        x = jlab.triCharMonthNames[start.getMonth()] + " " + start.getDate();
                    } else {
                        x = item.series.data[item.dataIndex][0];
                    }
                
                    var mean = item.datapoint[1];
                    var sigma = item.datapoint[2];
                    var borderColor = item.series.color;
                    var content = "<b>Series:</b> " + item.series.label
                            + "<br /><b>" + xlabel + ":</b> " + x
                            + "<br /><b>Mean:</b> " + mean
                            + "<br/><b>Sigma:</b> " + sigma;
                    jlab.showTooltip(item.pageX + 20, item.pageY - 20, content, borderColor);
                }
            } else {
                $('#flot-tooltip').remove();
                prev_point = null;
                prev_label = null;
            }
        });
    };
};

// This function returns a closure that allows the inner function to keep track of whether or not the currently "hovered" point
// is new or has changed from before.  Call with extra set up parens.  E.g., addErrorBarToolTip(chartId)();
jlab.flotCharts.addXYToolTip = function(chartId, xlabel, ylabel) {
    // These prev_* variables track whether or not the plothover event is for the same bar -- removes the "flicker" effect.
    var prev_point = null;
    var prev_label = null;
    return function () {
        $('#' + chartId).bind("plothover", function (event, pos, item) {
            if (item) {
                if ((prev_point !== item.dataIndex) || (prev_label !== item.series.label)) {
                    prev_point = item.dataIndex;
                    prev_label = item.series.label;
                    $('#flot-tooltip').remove();
                    
                    var x;
                    if (xlabel === "Date") {
                        // The item.datapoint is x coordinate of the bar, not of the original data point.  item.series.data contains
                        // the original data, and item.dataIndex this item's index in the data.
                        var timestamp = item.series.data[item.dataIndex][0];
                        var start = new Date(timestamp);
                        x = jlab.triCharMonthNames[start.getMonth()] + " " + start.getDate();
                    } else if (xlabel === "Time") {
                        var timestamp = new Date(item.series.data[item.dataIndex][0]);
                        x = jlab.formatPrettyTimestamp(timestamp);
                    }else {
                        x = item.series.data[item.dataIndex][0];
                    }
                
                    var y = item.datapoint[1].toFixed(2);
                    var borderColor = item.series.color;
                    var content = "<b>Series:</b> " + item.series.label
                            + "<br /><b>" + xlabel + ":</b> " + x
                            + "<br /><b>" + ylabel + ":</b> " + y;
                    jlab.showTooltip(item.pageX + 20, item.pageY - 20, content, borderColor);
                }
            } else {
                $('#flot-tooltip').remove();
                prev_point = null;
                prev_label = null;
            }
        });
    };
};


//+++++++++++++++++++++++++++++++++
// Title Functions
//+++++++++++++++++++++++++++++++++

jlab.flotCharts.addTitle = function(chartId, title) {
  $("#" + chartId + "-chart-wrap").prepend("<div id='" + chartId + "-chart-title' class='chart-title'>" + title + "</div>");  
};


//+++++++++++++++++++++++++++++++++
// Legend Functions
//+++++++++++++++++++++++++++++++++

/*
 * chartId - the placeholderId supplied to the chart-widget tag (no leading '#')
 * colors - array with fill colors that match the chart
 * labels - array with color labels that match the chart
 */
jlab.flotCharts.addLegend = function (chartId, colors, labels) {
    var legendString = "<div class=chart-legend id=" + chartId + '-chart-legend">\n';
    legendString += "<table>";
    if (colors.length !== labels.length) {
        console.log("Error: unequal number of colors and labels");
    }
    for (var i = 0; i < colors.length; i++) {
        legendString += '<tr><td><div class=color-box style="background-color: ' + colors[i] + ';"></div></td><td>' + labels[i] + '</td></tr>';
    }
    legendString += '</table></div>';
    $("#" +chartId + "-legend-panel").prepend(legendString);
};




//+++++++++++++++++++++++++++++++++
// Utility Functions
//+++++++++++++++++++++++++++++++++

jlab.flotCharts.getSmallDataXAxisTickOptions = function (data, timeUnit) {
    var options;
    // Handle custom tick placement sizing for small datasets
    if (data[0].length <= 3) {
        var tickSize;
        tickSize = [7, "day"];
        if (timeUnit === "day") {
            tickSize = [1, "day"];
        } else if (timeUnit === "month") {
            tickSize = [1, "month"];
        }
        options.tickSize = tickSize;
        // Use the sample dates as the tick locations if the numbers are small
        var ticks = [];
        for (i = 0; i < data[0].length; i++) {
            ticks[i] = data[0][i][0];
        }
        options.ticks = ticks;
    }
    
    return options;
};



/**
 * This calculates the minimum x distance between to points in a series in the array of flot data objects.  
 * @param {type} flotArray Array of flot data objects
 * @returns {Number|width|Number.MAX_SAFE_INTEGER} The minimum x-distance between two points in a series.
 */
jlab.flotCharts.getMinDataWidth = function (flotArray) {
    var min = Number.MAX_VALUE;
    var maxLength = 0;

    for (var i = 0; i < flotArray.length; i++) {
        if (flotArray[i].data.length > maxLength) {
            maxLength = flotArray[i].data.length;
        }
        for (var j = 1; j < flotArray[i].data.length; j++) {
            width = flotArray[i].data[j][0] - flotArray[i].data[j - 1][0];
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

var jlab = jlab || {};
jlab.errorBarChart = jlab.errorBarChart || {};


//
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
//     - labels: an array of HTML strings that label the series
//     - timeUnit: a string defining alternatively the distance between data points or the time span for which statistics are calculated.
//     - title: title of the chart





jlab.errorBarChart.drawChart = function (chartId, data, flotOptions, settings) {
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
    } else if (!$.isArray(data[0].data)) {
        // Possible that no data exists.  Should return nicely, not throw an exception
        $("#" + chartId).prepend("No data available");
        return;
    }
    if (typeof settings === "undefined") {
        exitFunc("Error: Settings required");
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
            interactive: true
        },
        pan: {
            interactive: true
        }
    };

    // Do a deep, recursive overwrite of the supplied flotOptions onto the default options object
    if (typeof flotOptions !== 'undefined' & flotOptions !== null) {
        $.extend(true, options, flotOptions);
    }

    var plotData = [];

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
    $.extend(true, plotData, data);
    $('#' + chartId + "-loader").hide();
    $.plot($("#" + chartId), plotData, options);
    if (typeof settings.title !== "undefined") {
        jlab.chartWidget.addTitle(chartId, "<strong>" + settings.title + "</strong>");
    }

    // Add a custom legend off to the side
    jlab.barChart.addLegend(chartId, settings.colors, settings.labels);
    // These prev_* variables track whether or not the plothover event is for the same bar -- removes the "flicker" effect.
    var prev_point = null;
    var prev_label = null;
    var timeUnit = settings.timeUnit;
    $('#' + chartId).bind("plothover", function (event, pos, item) {
        if (item) {
            if ((prev_point !== item.dataIndex) || (prev_label !== item.series.label)) {
                prev_point = item.dataIndex;
                prev_label = item.series.label;
                $('#flot-tooltip').remove();
                // The item.datapoint is x coordinate of the bar, not of the original data point.  item.series.data contains
                // the original data, and item.dataIndex this item's index in the data.
                var timestamp = item.series.data[item.dataIndex][0];
                var start = new Date(timestamp);
                start = jlab.triCharMonthNames[start.getMonth()] + " " + start.getDate();
                var end, dateRange;

                if (timeUnit === "week") {
                    end = new Date(timestamp);
                    end.setDate(end.getDate() + 7);
                    end = jlab.triCharMonthNames[end.getMonth()] + " " + end.getDate();
                    dateRange = start + " - " + end;
                } else if (timeUnit === "day") {
                    end = new Date(timestamp);
                    end.setDate(end.getDate() + 1);
                    end = jlab.triCharMonthNames[end.getMonth()] + " " + end.getDate();
                    dateRange = start + " - " + end;
                } else {
                    dateRange = "Begining " + start;
                }

                var mean = item.datapoint[1];
                var sigma = item.datapoint[2];
                var borderColor = item.series.color;
                var content = "<b>Series:</b> " + item.series.label
                        + "<br /><b>Date:</b> " + dateRange
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
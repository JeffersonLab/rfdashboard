/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.barChart = jlab.barChart || {};

/*
 * chartId - the placeholderId supplied to the chart-widget tag (no leading '#')
 * colors - array with fill colors that match the chart
 * labels - array with color labels that match the chart
 */
jlab.barChart.addLegend = function (chartId, colors, labels) {
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

jlab.barChart.updateChart = function (settings) {

    var exitFunc = function (msg) {
        $("#" + chartId).prepend(msg);
          $('#' + chartId + "-loader").hide();
        console.log(msg);
        throw msg;
    };

    // Only some of these are required
    var chartId, url, start, end, timeUnit, colors, yLabel, xMin, xMax, yMin, yMax, title, ajaxData, clickable;
    if (typeof settings === "undefined") {
        exitFunc("Error: Settings object required");
    }
    
    // Required
    if ( settings.chartId === "undefined") {
        exitFunc("Error: chartId required.");
    } else {
        chartId = settings.chartId;
    }
    if ( settings.url === "undefined") {
        exitFunc("Error: url required.");
    } else {
        url = settings.url;
    }
    if ( settings.ajaxData === "undefined" || typeof settings.ajaxData !== "object") {
        exitFunc("Error: ajaxData object required");
    } else {
        ajaxData = settings.ajaxData;
    }
    if ( ! $.isArray(settings.colors)) {
        exitFunc("Error: Incorrect or unsupplied color scheme");
    } else {
        colors = settings.colors;
    }
    if ( typeof settings.timeMode === "undefined") {
        exitFunc("Error: timeMode required");
    } else if (settings.timeMode === true) {
        timeMode = "time";
    } else {
        timeMode = null;
    }

    // Optional
    if ( typeof settings.clickable === true ) {
        clickable = true;
    } else {
        clickable = false;
    }
    if ( typeof settings.start !== 'undefined') {
        start = settings.start;
    }
    if ( typeof settings.end !== 'undefined') {
        end = settings.end;
    }
    if ( typeof settings.yLabel === 'undefined') {
        yLabel = "Value";
    } else {
        yLabel = settings.yLabel;
    }
    if ( typeof settings.yMin !== 'undefined') {
        yMin = settings.yMin;
    }
    if ( typeof settings.yMax !== 'undefined') {
        yMax = settings.yMax;
    }
    if ( typeof settings.xMin !== 'undefined') {
        xMin = settings.xMin;
    }
    if ( typeof settings.xMax !== 'undefined') {
        xMax = settings.xMax;
    }
    if ( typeof settings.title !== "undefined") {
        title = settings.title;
    }

    var plotData = [];
    $.ajax({
        beforeSend: function () {
            $('#' + chartId + "-loader").show();
        },
        complete: function () {
            $('#' + chartId + "-loader").hide();
        },
        url: url,
        data: ajaxData,
        dataType: "json",
        error: function (jqXHR, textStatus, errorThrown) {
            $('#' + chartId + "-loader").hide();
            $("#" + chartId).append("Error occurred querying data");
            console.log("Error occurred querying data from '" + url + "?start=" + start + "%end=" + end);
            console.log("  textStatus: " + textStatus);
            console.log("  errorThrown" + errorThrown);
        },
        success: function (jsonData, textStatus, jqXHR) {
            // The repsonse should be formated 
            // {
            //    labels:["label_1",...,"label_n"],
            //    data:[ 
            //              [[t_1,d_1], ..., [t_k,d_k]]_1, 
            //              ..., 
            //              [[t_1, d_1], ..., [t_k,d_k]]_n
            //           ]
            // }

            var lineWidth = 1;
            var fill = true;
            var show = true;
            var options = {
                xaxis: {
                    mode: timeMode,
                    timeformat: "%b %d<br />%Y",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
                    axisLabelPadding: 5
                },
                yaxis: {
                    axisLabel: yLabel,
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
                }
            };
            if ( typeof yMin !== "undefined" ) {
                options.yaxis.min = yMin;
            }
            if ( typeof yMax !== "undefined" ) {
                options.yaxis.max = yMax;
            }
            if ( typeof xMin !== "undefined" ) {
                options.xaxis.min = xMin;
            }
            if ( typeof xMax !== "undefined" ) {
                options.xaxis.max = xMax;
            }

            if (jsonData.data[0].length <= 15) {
                var tickSize;
                var ticks = [];
                tickSize = [7, "day"];
                if (timeUnit === "day") {
                    tickSize = [1, "day"];
                } else if (timeUnit === "month") {
                    tickSize = [1, "month"];
                }

                // Use the sample dates as the tick locations if the numbers are small
                for (i = 0; i < jsonData.data[0].length; i++) {
                    ticks[i] = jsonData.data[0][i][0];
                }
                options.xaxis.tickSize = tickSize;
                options.xaxis.ticks = ticks;
            }

            // Days / number of bars * (1-(%gap_between_datapoints/100))
            var numSeries = jsonData.data.length;
            var dataPointWidth = jlab.getMinDataWidth(jsonData.data);
            var barWidth = dataPointWidth / numSeries * (1 - 0.2);
            for (i = 0; i < jsonData.data.length; i++) {
                plotData[i] = {
                    label: jsonData.labels[i],
                    data: jsonData.data[i],
                    bars: {
                        order: i + 1,
                        show: show,
                        lineWidth: lineWidth,
                        fill: fill
                    }
                };
                if (typeof (colors) !== "undefined") {
                    plotData[i].color = colors[i];
                    plotData[i].bars.fillColor = colors[i];
                }

                if (barWidth !== Number.MAX_SAFE_INTEGER) {
                    plotData[i].bars.barWidth = barWidth;

                }
            }

            $('#' + chartId + "-loader").hide();
            $.plot($("#"+chartId), plotData, options);
            if (typeof title !== "undefined") {
                jlab.chartWidget.addTitle(chartId, "<strong>" + title + "</strong>");
            }

            // Add a custom legend off to the side
            jlab.barChart.addLegend(chartId, colors, jsonData.labels);

            // These prev_* variables track whether or not the plothover event is for the same bar -- removes the "flicker" effect.
            var prev_point = null;
            var prev_label = null;
            
            $('#' + chartId).bind("plothover", function (event, pos, item) {
                if (item) {
                    if ((prev_point != item.dataIndex) || (prev_label != item.series.label)) {
                        prev_point = item.dataIndex;
                        prev_label = item.series.label;

                        $('#flot-tooltip').remove();
                        // The item.datapoint is x coordinate of the bar, not of the original data point.  item.series.data contains
                        // the original data, and item.dataIndex this item's index in the data.
                        var timestamp = item.series.data[item.dataIndex][0];
                        var d = new Date(timestamp);
                        var time = jlab.triCharMonthNames[d.getMonth()] + " " + d.getDate();
                        var y = item.datapoint[1];
                        var borderColor = item.series.color;
                        var content = "<b>Series:</b> " + item.series.label + "<br /><b>Date:</b> " + time + "<br /><b>Value:</b> " + y;
                        jlab.showTooltip(item.pageX + 20, item.pageY - 20, content, borderColor);
                    }
                } else {
                    $('#flot-tooltip').remove();
                    prev_point = null;
                    prev_label = null;
                }
            });

        }
    });
};
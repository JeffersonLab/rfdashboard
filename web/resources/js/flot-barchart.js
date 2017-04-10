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

jlab.barChart.updateChart = function (chartId, url, start, end, factorBy) {
    console.log("In jlab.barChart.update.Chart");
        console.log("url: " + url);
    console.log("chartId: " + chartId);
    console.log("start: " + start);
    console.log("end: " + end);
    var lineWidth = 1;
    var fill = true;
    var show = true;
    var fillColor = ["#AA4643", "#89A54E", "#4572A7", "#80699B"];
    var options = {
        xaxis: {
            mode: "time",
            timeformat: "%b %d<br />%Y",
            tickSize: [1, "day"],
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5
        },
        yaxis: {
            axisLabel: 'Value',
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
            axisLabelPadding: 5
        },
        grid: {
            hoverable: true,
            clickable: false,
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
    var plotData = [];
    $.ajax({
        url: url + "?start=" + start + "%end=" + end,
        data:
                {
                    start: start,
                    end: end
                },
        dataType: "json",
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

            // Days / number of bars * (1-(%gap_between_datapoints/100))
            var numSeries = jsonData.data.length;
            var dataPointWidth = jlab.getMinDataWidth(jsonData.data);
            var barWidth = dataPointWidth / numSeries * (1 - 0.2);
            for (i = 0; i < jsonData.data.length; i++) {
                plotData[i] = {
                    label: jsonData.labels[i],
                    data: jsonData.data[i],
                    color: fillColor[i],
                    bars: {
                        order: i + 1,
                        show: show,
                        lineWidth: lineWidth,
                        fill: fill,
                        fillColor: fillColor[i]
                    }
                };
                if (barWidth !== Number.MAX_SAFE_INTEGER) {
                    plotData[i].bars.barWidth = barWidth;

                }
            }

            $.plot($("#"+chartId), plotData, options);

            // Add a custom legend off to the side
            jlab.barChart.addLegend(chartId, fillColor, jsonData.labels);

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
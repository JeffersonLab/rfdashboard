/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.energyReachUrl = "/RFDashboard/ajax/lem-scan";

jlab.energyReach = jlab.energyReach || {};
jlab.energyReach.loadCharts = function (url, start, end, timeUnit) {

    var settings1 = {
        chartId: 'lem-scan',
        url: url,
        date: end,
        // Grad the North, South, and Total colors
        colors: jlab.colors.linacs.slice(1, 4),
        yLabel: "C25 Trips/Hour"
    };
    jlab.energyReach.updateLemScanChart(settings1);

    var settings2 = {
        chartId: 'energy-reach',
        url: url,
        start: start,
        end: end,
        "timeUnit": timeUnit,
        // Grad the North, South, and Total colors
        colors: ["#666666"],
        yLabel: "Linac Energy (MeV)",
        yMin: 1000,
        yMax: 1190,
        timeMode: true,
        title: "Linac Energy Reach",
        ajaxData: {
            "start": start,
            "end": end,
            "type": "reach-scan"
        }
    };
    jlab.barChart.updateChart(settings2);
};

jlab.energyReach.updateLemScanChart = function (settings) {

    var exitFunc = function (msg) {
        $("#" + chartId).prepend(msg);
        $('#' + chartId + "-loader").hide();
        throw msg;
        console.log(msg);
        exit(1);
    };

    // Only some of these are required
    var chartId, url, date, colors, yLabel;
    if (typeof settings === "undefined") {
        exitFunc("Error: Settings object required");
    }

    // Required
    if (settings.chartId === "undefined") {
        exitFunc("Error: chartId required.");
    } else {
        chartId = settings.chartId;
    }
    if (settings.url === "undefined") {
        exitFunc("Error: url required.");
    } else {
        url = settings.url;
    }
    if (!$.isArray(settings.colors)) {
        exitFunc("Error: Incorrect or unsupplied color scheme");
    } else {
        colors = settings.colors;
    }

    // Optional
    if (typeof settings.date !== 'undefined') {
        date = settings.date;
    }
    if (typeof settings.yLabel === 'undefined') {
        yLabel = "Value";
    } else {
        yLabel = settings.yLabel;
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
        data:
                {
                    type: "day-scan",
                    date: date
                },
        dataType: "json",
        error: function (jqXHR, textStatus, errorThrown) {
            $('#' + chartId + "-loader").hide();
            $("#" + chartId).append("Error occurred querying data");
            console.log("Error occurred querying data from '" + url + "?type=day-scan" + "&date=" + date);
            console.log("  textStatus: " + textStatus);
            console.log("  errorThrown: " + errorThrown);
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
                    // not time mode
                    axisLabel: "Linac Energy (MeV)",
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
                    axisLabelPadding: 5,
                    min: 0,
                    max: 15
                },
                grid: {
                    hoverable: true,
                    clickable: false,
                    borderWidth: 1,
                    markings: [{yaxis: {from: 8, to: 8}, color: "#000000"},
                        {yaxis: {from: 4, to: 4}, color: "#000000"}]
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

            // Days / number of bars * (1-(%gap_between_datapoints/100))
            var numSeries = jsonData.data.length;
            var dataPointWidth = jlab.getMinDataWidth(jsonData.data);
            var barWidth = dataPointWidth / numSeries * (1 - 0.2);
            for (i = 0; i < jsonData.data.length; i++) {
                plotData[i] = {
                    label: jsonData.labels[i],
                    data: jsonData.data[i],
                    lines: {
                        show: true
                    }
                };
                if (typeof (colors) !== "undefined") {
                    plotData[i].color = colors[i];
                }
            }

            $('#' + chartId + "-loader").hide();
            var plot = $.plot($("#" + chartId), plotData, options);
            jlab.chartWidget.addTitle(chartId, "<strong>C25 Trip Rates</strong><br/><div style='font-size:smaller'>" + date + "</div>");

            var p8 = plot.pointOffset({x: 1010, y: 8});
            var p4 = plot.pointOffset({x: 1010, y: 4});
            $("#" + chartId).append("<div style='position:absolute;left:" + p8.left + "px;top:" + (p8.top - 17) + "px; color:#666;font-size:smaller'>8 trips/hr</div>");
            $("#" + chartId).append("<div style='position:absolute;left:" + p4.left + "px;top:" + (p4.top - 17) + "px; color:#666;font-size:smaller'>4 trips/hr</div>");

            // Add a caption that lists the energy reach of North, South, and Total
            var reaches = jlab.energyReach.getEnergyReach(jsonData, chartId);


            // Add a custom legend off to the side
            jlab.energyReach.addLegend(chartId, colors, jsonData.labels, reaches);

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
                        var energy = item.series.data[item.dataIndex][0];
                        var y = item.datapoint[1];
                        var borderColor = item.series.color;
                        var content = "<b>Series:</b> " + item.series.label + "<br /><b>Energy:</b> " + energy + "&nbspMeV" + "<br /><b>Value:</b> " + y;
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

// This returns an HTML table containing the energy reach (energy at which the machine has 8 trips/hr or linac has 4 trips/hr)
// data is the JSON object returned by the ajax request to /ajax/lem-scan?type=day-scan
jlab.energyReach.getEnergyReach = function (jsonData, chartId) {
    var tripData = {
        north: {energy: [], tripRate: []},
        south: {energy: [], tripRate: []},
        total: {energy: [], tripRate: []}
    };
    var data = jsonData.data;
    var labels = jsonData.labels;

    // Get the flot oriented data structure into something everpolate wants to use
    for (var i = 0; i < data.length; i++) {
        for (var j = 0; j < data[0].length; j++) {
            var energy = data[i][j][0];
            var tripRate = data[i][j][1];
            if (tripRate !== "null") {
                tripData[labels[i].toLowerCase()].energy[j] = Number(energy);
                tripData[labels[i].toLowerCase()].tripRate[j] = Number(tripRate);
            }
        }
    }

    // Everpolate should already exist - needs everpolate library to be included in the calling jsp
    var reaches = [];
    reaches[0] = everpolate.linear([8], tripData.north.tripRate, tripData.north.energy)[0].toFixed(1);
    reaches[1] = everpolate.linear([8], tripData.south.tripRate, tripData.south.energy)[0].toFixed(1);
    reaches[2] = everpolate.linear([8], tripData.total.tripRate, tripData.total.energy)[0].toFixed(1);

    return reaches;
};

jlab.energyReach.addLegend = function (chartId, colors, labels, reaches) {
    var legendString = '<div class="chart-legend" id=' + chartId + '-chart-legend">\n';
    legendString += "<table>";
    legendString += '<tr><th colspan="2">Linac</th><th>Reach</th><tr>';
    if (colors.length !== labels.length) {
        console.log("Error: unequal number of colors and labels");
    }
    for (var i = 0; i < colors.length; i++) {
        legendString += '<tr><td><div class=color-box style="background-color: ' + colors[i] + ';"></div></td><td>' + labels[i] + '</td><td>' + reaches[i] + '</td></tr>';
    }
    legendString += '</table></div>';
    $("#" + chartId + "-legend-panel").prepend(legendString);
};

/* 
 * Create a bypassed cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.energyReach.createTable = function (tableId, start, end) {

    jlab.cavity.getCavityData({
        "start": start,
        "end": end,
        timeUnit: "day",
        success: function (jsonData, textStatus, jqXHR) {
            var data = jsonData.data;
            var tableString = "<table class=\"tablesorter\">";
            tableString += "<thead><tr><th>Name</th><th>Module Type</th><th>Delta MAV (kV)</th><th>Delta GSET</th><th>Module Changed</th><th>Found Match</th></tr></thead>";
            tableString += "<tbody>";
            if (data[0].cavities === null || data[data.length - 1].cavities === null) {
                console.log("Error processing cavity service data. start:" + start + "  end: " + end);
                throw "Data service returned null data";
            }
            var cavEnd = data[0].cavities;
            var cavStart = data[data.length - 1].cavities;

            for (var i = 0; i < cavEnd.length; i++) {
                var foundMatch = false;
                for (var j = 0; j < cavStart.length; j++) {
                    if (cavEnd[i].name === cavStart[j].name) {
                        foundMatch = true;
                        var name, cmType, dGset, dMav, moduleChange;
                        name = cavEnd[i].name;
                        if ( cavEnd[i].moduleType === cavStart[j].moduleType ) {
                            moduleChange = false;
                            cmType = cavEnd[i].moduleType;
                        } else {
                            moduleChange = true;
                            cmType = cavStart[j].moduleType + "/" + cavEnd[i].moduleType;
                        }
                        if ( cavEnd[i].gset !== "" && cavStart[j].gset !== "" ) {
                            dGset = cavEnd[i].gset - cavStart[j].gset;
                        } else {
                            dGset = "N/A";
                        }
                        if ( cavEnd[i].modAnodeVoltage_kv !== "" && cavStart[j].modAnodeVoltage_kv !== "" ) {
                            dMav = cavEnd[i].modAnodeVoltage_kv - cavStart[j].modAnodeVoltage_kv;                        
                        } else {
                            dMav = "N/A";
                        }
                        tableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                                dMav.toFixed(2) + "</td><td>" + dGset.toFixed(2) + "</td><td>" + moduleChange + "</td><td>" + true + "</td></tr>";
                    }
                }
                if ( ! foundMatch ) {
                        tableString += "<tr><td>" + cavEnd[i].name + "</td><td>" + cavEnd[i].moduleType + "</td><td>" +
                                dMav + "</td><td>" + cavEnd[i].modAnodeVoltage_kv + "</td><td>" + "N/A" + "</td><td>" + false + "</td></tr>";                    
                }
            }
            tableString += "</tbody></table>";
            $("#" + tableId + "-table").append(tableString);
            // Setup the sortable cavity table
            $(".tablesorter")
                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
                    .tablesorterPager({container: $("#" + tableId + "-pager")});
        }
    });
};




$(function () {
    jlab.energyReach.createTable("diff-table", jlab.diffStart, jlab.diffEnd);
    jlab.energyReach.loadCharts(jlab.energyReachUrl, jlab.start, jlab.end, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
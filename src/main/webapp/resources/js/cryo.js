var jlab = jlab || {};
jlab.cryo = {};

// This request follows a different model than how other charts are handled.  Instead of directly querying the RFDashboard
// server for the data, the client performs an ajax JSONP request against the myaweb server.  This was done as the myaweb
// myStats request can take a while and will probably be changed to use some sort of websocket or async job processing
// method.  Also, querying the data currently requieres multiple multiple requests which are done sequentially here.
jlab.cryo.updateCryoPressureChart = function (chartId, start, end, timeUnit) {
    var numDays;
    switch (timeUnit) {
        case 'day':
            numDays = 1;
            break;
        case 'week':
            numDays = 7;
            break;
        default:
            window.console && console.log("Error: unknown timeUnit specified.  No cryo data request made.");
            return;
    }

    var dayDiff = jlab.daysBetweenDates(start, end);
    var numSteps = Math.floor(dayDiff / numDays) + 1;  // +1 to add back in the starting point

    var i = 0;
    // Get the cryo pressure data
    var cryoPromise = $.ajax({
        url: "https://myaweb.acc.jlab.org/myStatsSampler/data",
        timeout: 60000,  //in millis
        data: {
            b: start,
            n: numSteps,
            s: 1,
            sUnit: timeUnit, // supports second, day, week as of 2017-05-30
            l: "CPI4107B,CPI5107B"
        },
        dataType: "jsonp",
        jsonp: "jsonp",
        beforeSend: jlab.showChartLoading(chartId)
    });

    // Get the Energy Reach data
    var lemPromise = $.getJSON("/RFDashboard/ajax/lem-scan", {"start": start, "end": end, "type": "reach-scan"});

    $.when(cryoPromise, lemPromise).then(function (cryoAjax, lemAjax) {
        // First the success/done handler
        // The myStatsSampler service can respond in the following ways:
        //  - Return some non-OK HTTP status / server error without data (promise.fail - expected failure behavior)
        //  - Return some non-OK HTTP status and something totally unexpected (promise.fail - unexpected failure behavior)
        //  - Return some OK HTTP status, but each bin's object can contain an error parameter or data (promise.done - successful myStatsSampler
        //    run, but zero or more bins had errors)
        //    
        // Expecting {data: [ {start: 'yyyy-MM-dd[ HH:mm:ss]', output: [{name: "PVName", min: "##.###',...}, ..., {name: ...}]}
        //                          {start: ..., output: [...]}
        //                }
        // OR
        // {data: [], error: "<message>"}
        var cryoData = cryoAjax[0];
        var lemData = lemAjax[0].data[0]; // This now contains a single flot formated data series [ [x,y], [x,y], ...]
        var hasLemData = false;
        // It is possible that there is no LEM data returned without an error state - there just wasn't any scan data for that date
        if ( $.isArray(lemAjax) && $.isArray(lemData) && lemData.length > 0 ) {
            hasLemData = true;
        }
        var flotData;

        var labels = ["<b>North</b> (CPI4107B)", "<b>South</b> (CPI5107B)", "<b>Energy Reach</b>"];
        var labelMap = new Map();
        labelMap.set("CPI4107B", "<b>North</b> (CPI4107B)");
        labelMap.set("CPI5107B", "<b>South</b> (CPI5107B)");
        var colors = jlab.colors.linacs.slice(1, 4);  // North, South, Total (Total is used for Energy Reach)
        var colorMap = new Map();
        colorMap.set("CPI4107B", colors[0]);
        colorMap.set("CPI5107B", colors[1]);

        if (!Array.isArray(cryoData.data)) {
            jlab.hideChartLoading(chartId, "Unexpected error querying data service.");
        } else {
            if ( hasLemData ) {
                flotData = new Array(cryoData.data[0].output.length + 1); // Two linacs, plus energy reach data
            } else {
                // Don't include lem data in the flotData, labels, or colors.  The maps don't matter since nothing will call the key if
                // the lem data isn't included elsewhere.
                flotData = new Array(cryoData.data[0].output.length);
                labels = labels.slice(0,2);
                colors = colors.slice(0,2);
            }
            var d, mean, sigma;
            for (var i = 0; i < cryoData.data.length; i++) {
                d = new Date(cryoData.data[i].start); // treated as UTC, but thats how all of the dates are being displayed.
                for (var j = 0; j < cryoData.data[i].output.length; j++) {
                    if (typeof flotData[j] !== "object") {
                        flotData[j] = {
                            data: new Array(),
                            label: labelMap.get(cryoData.data[i].output[j].name),
                            color: colorMap.get(cryoData.data[i].output[j].name)
                        };
                    }
                    mean = cryoData.data[i].output[j].mean;
                    sigma = cryoData.data[i].output[j].sigma;
                    flotData[j].data.push([d.getTime(), mean, sigma]);
                }
            }
            if (hasLemData) {
                flotData[2] = {
                    data: lemData,
                    label: "Energy Reach",
                    color: colors[2], // the "Total" color
                    yaxis: 2,
                    // We want this to display as a line, not a series of error bars
                    points: {show: false, errorbars: "n"},
                    // This causes a weird bug where panning the graph to the left so that the right-most point of the line
                    // is no longer visible results in the entire canvas above the line being filled with the fillColor.  Weird ...
//                lines: {show: true, fill: true, fillColor: colors[2]}
                    lines: {show: true}
                };
            }
            jlab.hideChartLoading(chartId);
            var settings = {
                chartType: "errorBar",
                tooltips: false,
                timeUnit: timeUnit,
                colors: colors,
                labels: labels,
                title: "Linac Cryogen Pressure</strong><br/>(" + start + " to " + end + " by " + timeUnit + ")<strong>"
            };
            var flotOptions = {
                xaxes: [{mode: "time"}],
                yaxes: [{axisLabel: "He Pressure (Atm)"}]
            };
            if ( jlab.cryo.hasDataInRange(flotData, 0.03, 0.05) ) {
                flotOptions.yaxes[0].min = 0.03;
                flotOptions.yaxes[0].max = 0.05;
            }
            if (hasLemData) {
                flotOptions.yaxes.push({
                    axisLabel: "Energy Reach (MeV)",
                    min: 1000,
                    max: 1190,
                    position: "right",
                    alignTicksWithAxis: false,
                    zoomRange: false,
                    panRange: false
                });
            }
            // Expects an array of flot data arrays.  E.g.,
            // [ [millis, mean, sigma], [millis, mean, sigma], ...],
            //   [millis, mean, sigma], [millis, mean, sigma], ...],
            //   ...
            // ]
            jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);
            jlab.cryo.addCryoReachToolTip(chartId, timeUnit);

            $('#' + chartId).bind("plotclick", function (event, pos, item) {
                if (item) {
                    var url;
                    var timestamp = item.series.data[item.dataIndex][0];
                    var dateString = jlab.millisToDate(timestamp);
                    if (item.series.label === "Energy Reach") {
                        url = "/RFDashboard/cryo?start=" + jlab.start + "&end=" + jlab.end + "&diffStart=" +
                                jlab.addDays(dateString, -1) + "&diffEnd=" + dateString + "&timeUnit=" + jlab.timeUnit;
                    } else {
                        url = "/RFDashboard/cryo?start=" + jlab.start + "&end=" + jlab.end + "&diffStart=" +
                                dateString + "&diffEnd=" + jlab.addDays(dateString, numDays) + "&timeUnit=" + jlab.timeUnit;
                    }
                    window.location.href = url;
                }
            });
            if ( ! hasLemData ) {
                $("#" + chartId + "-chart-wrap").append("<small>* No LEM data available for " + jlab.start + " to " + jlab.end + "</small>");
            }
        }
    }, function( jqXHR, textStatus, errorThrown ) {
        // Wrap the error handler in another layer so we can add the chartId to the function call (default fail callbacks only get the
        // first three.
        jlab.cryo.pressureChartFail(jqXHR, textStatus, errorThrown, chartId);
    });
};

// Check if any series in the flotData array has a y-value in the specified range.
// flotData looks like [{data: [[x1,y1], [x2,y2] ... ], ...}, {data: [[x1,y1], ...], ...}, ... ]
jlab.cryo.hasDataInRange = function(flotData, min, max) {
    for ( var i = 0; i < flotData.length; i++) {
        for ( var j = 0; j < flotData[i].data.length; j++ ) {
            if ( flotData[i].data[j][1] >= min && flotData[i].data[j][1] <= max) {
                return true;
            }
        }
    }
    return false;
};

jlab.cryo.pressureChartFail = function (jqXHR, textStatus, errorThrown, chartId) {
    // Then add the error hanlder
    // 
    // Unless something went really wrong, the responseText should be a json object and should have an error parameter
    var json;
    var message;
    var reqDates = start + '-' + end;
    if (textStatus === 0) {
        message = "Error in HTTP request.  Received HTTP status '0'";
    } else if ( typeof jqXHR === "undefined" ) {
        message = "Error in request.  jqXHR undefined.";
    } else {
        try {
            if (typeof jqXHR.responseText === 'undefined' || jqXHR.responseText === '') {
                json = {};
            } else {
                json = $.parseJSON(jqXHR.responseText);
            }
        } catch (err) {
            message = reqDates + ' response is not JSON: ' + jqXHR.responseText;
            json = {};
        }
        message = json.error || message || 'Server did not handle request for ' + reqDates;
    }
    window.console && console.log(message);
    jlab.hideChartLoading(chartId, "Error querying data service");
};

jlab.cryo.addCryoReachToolTip = function (chartId, timeUnit) {
    var prev_point = null;
    var prev_label = null;
    $('#' + chartId).bind("plothover", function (event, pos, item) {
        if (item) {
            if ((prev_point !== item.dataIndex) || (prev_label !== item.series.label)) {
                prev_point = item.dataIndex;
                prev_label = item.series.label;
                $('#flot-tooltip').remove();
                
                var content, borderColor;
                if (item.series.label === "Energy Reach") {
                    var timestamp = item.series.data[item.dataIndex][0];
                    var start = new Date(timestamp);
                    start = jlab.triCharMonthNames[start.getMonth()] + " " + start.getDate();
                    var reach = item.datapoint[1];
                    borderColor = item.series.color;
                    content = "<b>Series:</b> " + item.series.label
                            + "<br /><b>Date:</b> " + start
                            + "<br /><b>Reach (MeV):</b> " + reach;
                } else {
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
                    borderColor = item.series.color;
                    content = "<b>Series:</b> " + item.series.label
                            + "<br /><b>Date:</b> " + dateRange
                            + "<br /><b>Mean:</b> " + mean + " Atm"
                            + "<br/><b>Sigma:</b> " + sigma + " Atm";
                }
                jlab.showTooltip(item.pageX + 20, item.pageY - 20, content, borderColor);
            }
        } else {
            $('#flot-tooltip').remove();
            prev_point = null;
            prev_label = null;
        }
    });
};

$(function () {

    // This enables the "Basic/Advanced" menu button to toggle between the two tables.  diff-table-advanced starts out with
    // display: none.
    $("#menu-toggle").click(function() {
        $('#diff-table-basic').toggle();
        $('#diff-table-advanced').toggle();
    });
    
    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd",
        showButtonPanel: true
    });

    jlab.cavity.createCavitySetPointTables("#diff-table-basic", "#diff-table-advanced", "#summary-table", jlab.cavityData, jlab.diffStart, jlab.diffEnd);
    jlab.cryo.updateCryoPressureChart('cryo-linac-pressure', jlab.start, jlab.end, jlab.timeUnit);

    jlab.util.initCalendarStartEnd("#main-calendar");
    jlab.util.initCalendarStartEnd("#delta-calendar");
});
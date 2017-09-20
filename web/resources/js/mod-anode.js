/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};


jlab.mod_anode = jlab.mod_anode || {};
jlab.mod_anode.mavUrl = "/RFDashboard/mod-anode";
jlab.mod_anode.mavAjaxUrl = "/RFDashboard/ajax/mod-anode";
jlab.mod_anode.linacAjaxUrl = "/RFDashboard/ajax/linac";

jlab.mod_anode.loadLEMSimChart = function (chartId, start, end, url, timeUnit) {
    jlab.showChartLoading(chartId);
    var lemScan = $.getJSON(url, {start: start, end: end, out: "flot", timeUnit: timeUnit});
    lemScan.done(function (json) {
        var settings = {
            colors: jlab.colors.modAnodeHarvester.slice(0, 4), // Grab the North, South, and Total colors
            labels: json.labels,
            timeUnit: timeUnit,
            title: "<strong>LEMSim Estimated Trip Impact of Mod Anode Voltage</strong>",
            tooltips: true,
            tooltipX: "Date",
            tooltipY: "Trips/Hr",
            legend: true,
            chartType: "bar"
        };
        var flotOptions = {
            xaxis: {mode: "time"},
            yaxis: {axisLabel: "Trips / Hour"},
            grid: {clickable: true}
        };

        var flotData = [];
        for (i = 0; i < json.data.length; i++) {
            flotData[i] = {data: json.data[i], points: {show: false}};
        }

        jlab.hideChartLoading(chartId);
        var plot = jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);
        $("#" + chartId).bind("plotclick", function (event, pos, item) {
            if (item) {
                var timestamp = item.series.data[item.dataIndex][0];
                var dateString = jlab.millisToDate(timestamp);
                var url = jlab.mod_anode.mavUrl + "?start=" + start + "&end=" + end + "&tableDate=" + dateString + "&timeUnit=" + timeUnit;
                window.location.href = url;
            }
        });
    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.hideChartLoading(chartId, "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
    });
};

jlab.mod_anode.loadMAVCountByFactorChart = function (chartId, start, end, url, timeUnit, factor) {
    jlab.showChartLoading(chartId);
    var lemScan = $.getJSON(url, {start: start, end: end, out: "flot", timeUnit: timeUnit, factor: factor});
    lemScan.done(function (json) {
        var settings = {
            colors: jlab.colors.cmtypes, // Grab the North, South, and Total colors
            labels: json.labels,
            timeUnit: timeUnit,
            title: "<strong>Cavities With Mod Anode Voltage<br>(by " + factor + ")</strong>",
            tooltips: true,
            tooltipX: "Date",
            tooltipY: "Value",
            legend: true,
            chartType: "bar"
        };
        var flotOptions = {
            xaxis: {mode: "time"},
            yaxis: {axisLabel: "# Cavities w/ M.A.V."},
            grid: {clickable: true}
        };

        var flotData = [];
        for (i = 0; i < json.data.length; i++) {
            flotData[i] = {data: json.data[i], points: {show: false}};
        }

        jlab.hideChartLoading(chartId);
        var plot = jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);
        $("#" + chartId).bind("plotclick", function (event, pos, item) {
            if (item) {
                var timestamp = item.series.data[item.dataIndex][0];
                var dateString = jlab.millisToDate(timestamp);
                var url = jlab.mod_anode.mavUrl + "?start=" + start + "&end=" + end + "&tableDate=" + dateString + "&timeUnit=" + timeUnit;
                window.location.href = url;
            }
        });
    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.hideChartLoading(chartId, "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
        ;
    });
};

/* 
 * Create a cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.mod_anode.createCavityTable = function (widgetId, date) {

    jlab.util.showTableLoading(widgetId);
    jlab.cavity.getCavityData({
        asRange: false,
        dates: [date],
        timeUnit: "day",
        success: function (jsonData, textStatus, jqXHR) {
            jlab.util.hideTableLoading(widgetId);
            var data = jsonData.data;
            var tableArray = new Array();
            tableArray.push(["Name", "Module Type", "Mod Anode Voltage (kV)", "GSET"]);

            for (var i = 0; i < data.length; i++) {
                var cavities = data[i].cavities;
                for (var j = 0; j < cavities.length; j++) {
                    var gset = "N/A";
                    if (cavities[j].gset !== "") {
                        gset = cavities[j].gset.toFixed(3);
                    }
                    tableArray.push([cavities[j].name, cavities[j].moduleType, cavities[j].modAnodeVoltage_kv, gset]);
                }
            }
            jlab.util.createTableSorterTable(widgetId, {data: tableArray});
        },
        error: function (jqXHR, textStatus, errorThrown) {
            jlab.util.hideTableLoading(widgetId, "Error querying data");
            console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
        }
    });
};


jlab.mod_anode.createModAnodeHarvesterTable = function (widgetId, date) {

    jlab.util.showTableLoading(widgetId);
    jlab.cavity.getCavityData({
        asRange: false,
        dates: [date],
        timeUnit: "day",
        success: function (jsonData, textStatus, jqXHR) {
            jlab.util.hideTableLoading(widgetId);
            var data = jsonData.data;
            var tableArray = new Array();
            tableArray.push([
                "Name", "Module Type", "EPICS Date", "Mod Anode Voltage (kV)",
                "GSET (1050 MeV)", "GSET No M.A.V. (1050 MeV)", "Delta GSET (1050 MeV)",
                "GSET (1090 MeV)", "GSET No M.A.V. (1090 MeV)", "Delta GSET (1090 MeV)"
            ]);

            for (var i = 0; i < data.length; i++) {
                let rowArray = new Array();
                var cavities = data[i].cavities;
                for (var j = 0; j < cavities.length; j++) {
                    let cav = cavities[j];
                    if (cav.hasOwnProperty("modAnodeHarvester")) {
                        var gset1050 = parseFloat(cav.modAnodeHarvester.gset1050).toFixed(3);
                        var gsetNoMav1050 = parseFloat(cav.modAnodeHarvester.gsetNoMav1050).toFixed(3);
                        var gset1090 = parseFloat(cav.modAnodeHarvester.gset1090).toFixed(3);
                        var gsetNoMav1090 = parseFloat(cav.modAnodeHarvester.gsetNoMav1090).toFixed(3);

                        tableArray.push([
                            cav.name, cav.moduleType, cav.modAnodeHarvester.epicsDate, cav.modAnodeHarvester.modAnodeVoltage_kv,
                            gset1050, gsetNoMav1050, (gsetNoMav1050 - gset1050).toFixed(3),
                            gset1090, gsetNoMav1090, (gsetNoMav1090 - gset1090).toFixed(3)
                        ]);
                    }
                }
            }
            console.log(tableArray);
            jlab.util.createTableSorterTable(widgetId, {data: tableArray});
        },
        error: function (jqXHR, textStatus, errorThrown) {
            jlab.util.hideTableLoading(widgetId, "Error querying data");
            console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
        }
    });
};


$(function () {

    // Load the tables
    jlab.mod_anode.createCavityTable("#mav-table", jlab.tableDate);
    jlab.mod_anode.createModAnodeHarvesterTable("#mav-mah-table", jlab.tableDate);

    // Load the charts
    jlab.mod_anode.loadMAVCountByFactorChart("mav-count-by-linac", jlab.start, jlab.end, jlab.mod_anode.mavAjaxUrl, jlab.timeUnit, "linac");
    jlab.mod_anode.loadMAVCountByFactorChart("mav-count-by-cmtype", jlab.start, jlab.end, jlab.mod_anode.mavAjaxUrl, jlab.timeUnit, "cmtype");
    jlab.mod_anode.loadLEMSimChart('mav-mah-trip-impact', jlab.start, jlab.end, jlab.mod_anode.linacAjaxUrl, jlab.timeUnit);

    // Setup the date picker(s)
    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });

});
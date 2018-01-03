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

jlab.mod_anode.loadLEMSimChart = function (chartId, start, end, chartData, timeUnit) {

    var title = "<strong>LEMSim Estimated Trip Impact of Mod Anode Voltage</strong>";
    if (typeof chartData === "undefined") {
        $("#" + chartId).append(title + "<br>No data available");
        return;
    }

    var settings = {
        colors: jlab.colors.modAnodeHarvester.slice(0, 4), // Grab the North, South, and Total colors
        labels: chartData.labels,
        timeUnit: timeUnit,
        title: title,
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
    for (i = 0; i < chartData.data.length; i++) {
        flotData[i] = {data: chartData.data[i], points: {show: false}};
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
};

jlab.mod_anode.loadMAVCountByFactorChart = function (chartId, start, end, countData, timeUnit, factor) {

    var title = "<strong>Cavities With Mod Anode Voltage<br>(by " + factor + ")</strong>";
    if (typeof countData === "undefined") {
        $("#" + chartId).append(title + "<br>No data available");
        return;
    }

    var settings = {
        colors: jlab.colors.cmtypes, // Grab the North, South, and Total colors
        labels: countData.labels,
        timeUnit: timeUnit,
        title: title,
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
    for (i = 0; i < countData.data.length; i++) {
        flotData[i] = {data: countData.data[i], points: {show: false}};
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
};

/* 
 * Create a cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.mod_anode.createCavityTable = function (widgetId, tableData) {

    if (typeof tableData === "undefined") {
        $("#" + widgetId).append("<br>No Data Available");
        return;
    }

    jlab.util.hideTableLoading(widgetId);
    var data = tableData.data;
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
};


jlab.mod_anode.createModAnodeHarvesterTable = function (widgetId, tableData) {

    if (typeof tableData === "undefined" || tableData.length === 0) {
        $(widgetId + " div.table-panel").append("<br>No Data Available");
        return;
    }

    jlab.util.hideTableLoading(widgetId);
    var data = tableData.data;
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

    jlab.util.createTableSorterTable(widgetId, {data: tableArray});
};


$(function () {
    // Load the tables
    jlab.mod_anode.createCavityTable("#mav-table", jlab.tableData);
    jlab.mod_anode.createModAnodeHarvesterTable("#mav-mah-table", jlab.tableData);

    // Load the charts
    jlab.mod_anode.loadMAVCountByFactorChart("mav-count-by-linac", jlab.start, jlab.end, jlab.MAVCountLinac, jlab.timeUnit, "linac");
    jlab.mod_anode.loadMAVCountByFactorChart("mav-count-by-cmtype", jlab.start, jlab.end, jlab.MAVCountCMType, jlab.timeUnit, "cmtype");
    jlab.mod_anode.loadLEMSimChart('mav-mah-trip-impact', jlab.start, jlab.end, jlab.mahChartData, jlab.timeUnit);

    // Setup the date picker(s)
    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });

});
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};


jlab.bypassed = jlab.bypassed || {};
jlab.bypassed.ajaxUrl = "/RFDashboard/ajax/bypassed";
jlab.bypassed.bypassedUrl = "/RFDashboard/bypassed";


jlab.bypassed.loadBypassedCountByFactorChart = function (chartId, start, end, bypassedData, timeUnit, factor) {
    jlab.showChartLoading(chartId);
    
    var settings = {
        colors: jlab.colors.cmtypes, // Grab the North, South, and Total colors
        labels: bypassedData.labels,
        timeUnit: timeUnit,
        title: "<strong>Bypassed Cavities<br>(by " + factor + ")</strong>",
        tooltips: true,
        tooltipX: "Date",
        tooltipY: "Value",
        legend: true,
        chartType: "bar"
    };
    var flotOptions = {
        xaxis: {mode: "time"},
        yaxis: {axisLabel: "# Bypassed Cavities"},
        grid: {clickable: true}
    };

    var flotData = [];
    for (i = 0; i < bypassedData.data.length; i++) {
        flotData[i] = {data: bypassedData.data[i], points: {show: false}};
    }

    jlab.hideChartLoading(chartId);
    var plot = jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);
    $("#" + chartId).bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = jlab.bypassed.bypassedUrl + "?start=" + start + "&end=" + end + "&tableDate=" + dateString + "&timeUnit=" + timeUnit;
            window.location.href = url;
        }
    });
};


/* 
 * Create a bypassed cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.bypassed.createTable = function (widgetId, tableData) {
    var data = tableData.data;
    var tableArray = new Array();
    tableArray.push(["Name", "Module Type", "Mod Anode Voltage (kV)", "GSET"]);
    for (var i = 0; i < data.length; i++) {
        var cavities = data[i].cavities;
        for (var j = 0; j < cavities.length; j++) {
            if (cavities[j].gset < 0.00001) {
                tableArray.push([cavities[j].name, cavities[j].moduleType, cavities[j].modAnodeVoltage_kv, cavities[j].gset]);
            }
        }
    }
    jlab.util.createTableSorterTable(widgetId, {data: tableArray});
};


$(function () {
    jlab.bypassed.createTable("#bypassed-table", jlab.tableData);
    jlab.bypassed.loadBypassedCountByFactorChart("bypassed-count-by-linac", jlab.start, jlab.end, jlab.bypassedLinac, jlab.timeUnit, "linac");
    jlab.bypassed.loadBypassedCountByFactorChart("bypassed-count-by-cmtype", jlab.start, jlab.end, jlab.bypassedCMType, jlab.timeUnit, "cmtype");

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd",
        showButtonPanel: true
    });

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });

    jlab.util.initCalendarStartEnd("#main-calendar");
});
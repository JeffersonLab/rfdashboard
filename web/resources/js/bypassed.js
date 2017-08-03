/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.bypassedUrl = "/RFDashboard/ajax/bypassed";

jlab.bypassed = jlab.bypassed || {};
jlab.bypassed.loadCharts = function (url, start, end, timeUnit) {

    var settings1 = {
        chartId: 'bypassed-count-by-linac',
        url: url,
        start: start,
        end: end,
        timeUnit: timeUnit,
        colors: jlab.colors.linacs,
        yLabel: "# Cavities Bypassed",
        title: "Bypassed Cavities<br>(By Linac)",
        timeMode: true,
        ajaxData: {
            "start": start,
            "end": end,
            "factor": "linac",
            "timeUnit": timeUnit
        }
    };
    jlab.barChart.updateChart(settings1);
    $('#bypassed-count-by-linac').bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = "/RFDashboard/bypassed?start=" + jlab.start + "&end=" + jlab.end + "&tableDate=" + dateString;
            console.log("Linking to " + url);
            window.location.href = url;
        }
    });

    var settings2 = {
        chartId: 'bypassed-count-by-cmtype',
        url: url,
        start: start,
        end: end,
        timeUnit: timeUnit,
        colors: jlab.colors.cmtypes,
        yLabel: "# Cavities Bypassed",
        timeMode: true,
        title: "Bypassed Cavities<br>(By Module Type)",
        ajaxData: {
            "start": start,
            "end": end,
            "factor": "cmtype",
            "timeUnit": timeUnit
        }
    };
    jlab.barChart.updateChart(settings2);
    $('#bypassed-count-by-cmtype').bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = "/RFDashboard/bypassed?start=" + jlab.start + "&end=" + jlab.end + "&tableDate=" + dateString;
            console.log("Linking to " + url);
            window.location.href = url;
        }
    });
};

/* 
 * Create a bypassed cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.bypassed.createTable = function (tableId, date) {

    jlab.cavity.getCavityData({
        asRange: false,
        dates: [date],
        timeUnit: "day",
        success: function (jsonData, textStatus, jqXHR) {
            console.log(jsonData);
            var data = jsonData.data;
            var tableString = "<table class=\"tablesorter\">";
            tableString += "<thead><tr><th>Name</th><th>Module Type</th><th>Mod Anode Voltage (kV)</th><th>GSET</th></tr></thead>";
            tableString += "<tbody>";
            for (var i = 0; i < data.length; i++) {
                var cavities = data[i].cavities;
                for (var j = 0; j < cavities.length; j++) {
                    if (cavities[j].gset < 0.0001) {
                        tableString += "<tr><td>" + cavities[j].name + "</td><td>" + cavities[j].moduleType + "</td><td>" +
                                cavities[j].modAnodeVoltage_kv + "</td><td>" + cavities[j].gset + "</tr>";
                    }
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
    jlab.bypassed.createTable("bypassed-table", jlab.tableDate);
    jlab.bypassed.loadCharts(jlab.bypassedUrl, jlab.start, jlab.end, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function() {
       $("#page-details-dialog").dialog("open");
    });

});
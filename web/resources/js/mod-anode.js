/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};


jlab.mod_anode = jlab.mod_anode || {};
jlab.mod_anode.loadCharts = function (start, end, timeUnit) {
    mavUrl = "/RFDashboard/ajax/mod-anode";
    linacUrl = "/RFDashboard/ajax/linac";

    var settings1 = {
        chartId: 'mav-count-by-linac',
        url: mavUrl,
        start: start,
        end: end,
        timeUnit: timeUnit,
        colors: jlab.colors.linacs,
        yLabel: "# Cavities w/ M.A.V.",
        title: "Cavities With Mod Anode Voltage<br>(By Linac)",
        timeMode: true,
        ajaxData: {
            start: start,
            end: end,
            factor: "linac",
            "timeUnit": timeUnit
        }
    };
    jlab.barChart.updateChart(settings1);
    $('#mav-count-by-linac').bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = "/RFDashboard/mod-anode?start=" + jlab.start + "&end=" + jlab.end + "&tableDate=" + dateString;
            console.log("Linking to " + url);
            window.location.href = url;
        }
    });

    var settings2 = {
        chartId: 'mav-count-by-cmtype',
        url: mavUrl,
        start: start,
        end: end,
        timeUnit: timeUnit,
        colors: jlab.colors.cmtypes,
        yLabel: "# Cavities w/ M.A.V.",
        title: "Cavities With Mod Anode Voltage<br>(By Module Type)",
        timeMode: true,
        ajaxData: {
            start: start,
            end: end,
            factor: "cmtype",
            "timeUnit": timeUnit
        }
    };
    jlab.barChart.updateChart(settings2);
    $('#mav-count-by-cmtype').bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = mavUrl + "?start=" + jlab.start + "&end=" + jlab.end + "&tableDate=" + dateString;
            console.log("Linking to " + url);
            window.location.href = url;
        }
    });
    
        var settings3 = {
        chartId: 'mav-mah-trip-impact',
        url: linacUrl,
        start: start,
        end: end,
        timeUnit: timeUnit,
        colors: jlab.colors.modAnodeHarvester,
        yLabel: "C25 Trips / Hour",
        title: "LEMSim Estimated Trip Impact of Mod Anode Voltage",
        timeMode: true,
        ajaxData: {
            start: start,
            end: end,
            out: "flot",
            "timeUnit": timeUnit
        }
    };
    jlab.barChart.updateChart(settings3);
    $('#mav-mah-trip-impact').bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = mavUrl + "?start=" + jlab.start + "&end=" + jlab.end + "&tableDate=" + dateString;
            console.log("Linking to " + url);
            window.location.href = url;
        }
    });

};

/* 
 * Create a cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.mod_anode.createTable = function (tableId, date) {

    jlab.cavity.getCavityData({
        start: jlab.addDays(date, -1),
        end: date,
        timeUnit: "day",
        success: function (jsonData, textStatus, jqXHR) {
            var data = jsonData.data;
            var tableString = "<table id=" + tableId + " class=\"tablesorter\">";
            tableString += "<thead><tr><th>Name</th><th>Module Type</th><th>Mod Anode Voltage (kV)</th><th>GSET</th></tr></thead>";
            tableString += "<tbody>";
            for (var i = 0; i < data.length; i++) {
                var cavities = data[i].cavities;
                for (var j = 0; j < cavities.length; j++) {
                    if (cavities[j].modAnodeVoltage_kv > 0) {
                        tableString += "<tr><td>" + cavities[j].name + "</td><td>" + cavities[j].moduleType + "</td><td>" +
                                cavities[j].modAnodeVoltage_kv + "</td><td>" + cavities[j].gset + "</tr>";
                    }
                }
            }
            tableString += "</tbody></table>";
            $("#" + tableId + "-table").append(tableString);
            // Setup the sortable cavity table
            $("#" + tableId)
                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
                    .tablesorterPager({container: $("#" + tableId + "-pager")});
        }
    });
};


/* 
 * Create a per-cavity ModAnodeharvester/LEMSim GSET data table.
 * tableId - the tablesorter tag tableId
 * date - the scan timestamp date for which to query data
 * */
jlab.mod_anode.createModAnodeHarvesterTable = function (tableId, date) {

    jlab.cavity.getCavityData({
        start: jlab.addDays(date, -1),
        end: date,
        timeUnit: "day",
        success: function (jsonData, textStatus, jqXHR) {
            var data = jsonData.data;
            var tableString = "<table id=" + tableId + " class=\"tablesorter\">";
            tableString += "<thead><tr><th>Name</th><th>Module Type</th><th>Mod Anode Voltage (kV)</th>"
                    + "<th>GSET<br>(1050 MeV)</th><th>GSET No M.A.V.<br>(1050 MeV)</th><th>Delta GSET<br>(1050 MeV)</th>"
                    + "<th>GSET<br>(1090 MeV)</th><th>GSET No M.A.V.<br>(1090 MeV)</th><th>Delta GSET<br>(1090 MeV)</th>"
                    + "</tr></thead>";
            tableString += "<tbody>";
            for (var i = 0; i < data.length; i++) {
                var cavities = data[i].cavities;
                for (var j = 0; j < cavities.length; j++) {
                    if (cavities[j].hasOwnProperty("modAnodeHarvester")) {
                        if (cavities[j].modAnodeHarvester.modAnodeVoltage_kv > 0) {
                            var gset1050 = Number.parseFloat(cavities[j].modAnodeHarvester.gset1050).toFixed(3);
                            var gsetNoMav1050 = Number.parseFloat(cavities[j].modAnodeHarvester.gsetNoMav1050).toFixed(3);
                            var gset1090 = Number.parseFloat(cavities[j].modAnodeHarvester.gset1090).toFixed(3);
                            var gsetNoMav1090 = Number.parseFloat(cavities[j].modAnodeHarvester.gsetNoMav1090).toFixed(3);
                            
                            tableString += "<tr><td>" + cavities[j].name + "</td><td>" + cavities[j].moduleType + "</td><td>"
                                    + cavities[j].modAnodeHarvester.modAnodeVoltage_kv + "</td><td>"
                                    + gset1050 + "</td><td>" + gsetNoMav1050 + "</td><td>" + (gsetNoMav1050 - gset1050).toFixed(3) + "</td><td>"
                                    + gset1090 + "</td><td>" + gsetNoMav1090 + "</td><td>" + (gsetNoMav1090 - gset1090).toFixed(3) + "</td>"
                                    + "</tr>";
                        }
                    }
                }
            }
            tableString += "</tbody></table>";
            $("#" + tableId + "-table").append(tableString);
            // Setup the sortable cavity table
            $("#" + tableId)
                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
                    .tablesorterPager({container: $("#" + tableId + "-pager")});
        }
    });
};


$(function () {

    jlab.mod_anode.createTable("mav-table", jlab.tableDate);
    jlab.mod_anode.createModAnodeHarvesterTable("mav-mah-table", jlab.tableDate);
    jlab.mod_anode.loadCharts(jlab.start, jlab.end, jlab.timeUnit);

    // Setup the date picker(s)
    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
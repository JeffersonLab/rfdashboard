/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.mavUrl = "/RFDashboard/ajax/mod-anode";

jlab.mod_anode = jlab.mod_anode || {};
jlab.mod_anode.loadCharts = function (url, start, end, timeUnit) {
    var settings1 = {
        chartId: 'mav-count-by-linac',
        url: url,
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

    var settings2 = {
        chartId: 'mav-count-by-cmtype',
        url: url,
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
};

/* 
 * Create a cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.mod_anode.createTable = function (tableId, date) {

    jlab.cavity.getCavityData({
        start: date,
        end: jlab.addDays(date, 1),
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
                    if (cavities[j].modAnodeVoltage_kv > 0) {
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

    jlab.mod_anode.createTable("mav-table", jlab.tableDate);

    jlab.mod_anode.loadCharts(jlab.mavUrl, jlab.start, jlab.end, jlab.timeUnit);

    // Setup the date picker(s)
    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
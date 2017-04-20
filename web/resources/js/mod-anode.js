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
        title: "Cavities With Mod Anode Voltage By Linac",
        timeMode: true,
        ajaxData: {
            start: start,
            end: end,
            factor: "linac"
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
        title: "Cavities With Mod Anode Voltage By Module Type",
        timeMode: true,
        ajaxData: {
            start: start,
            end: end,
            factor: "cmtype"
        }
    };
    jlab.barChart.updateChart(settings2);
};

$(function () {
    console.log("mavUrl: " + jlab.mavUrl + " - start: " + jlab.start + " - end: " + jlab.end + " - timeUnit - " + jlab.timeUnit);
    jlab.mod_anode.loadCharts(jlab.mavUrl, jlab.start, jlab.end, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
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
        title: "Bypassed Cavities By Linac",
        timeMode: true,
        ajaxData: {
            "start": start,
            "end": end,
            "factor": "linac"
        }
    };
    jlab.barChart.updateChart(settings1);

    var settings2 = {
        chartId: 'bypassed-count-by-cmtype',
        url: url,
        start: start,
        end: end,
        timeUnit: timeUnit,
        colors: jlab.colors.cmtypes,
        yLabel: "# Cavities Bypassed",
        timeMode: true,
        title: "Bypassed Cavities By Module Type",
        ajaxData: {
            "start": start,
            "end": end,
            "factor": "cmtype"
        }
    };
    jlab.barChart.updateChart(settings2);
    
    //jlab.barChart.updateChart('bypassed-count-by-linac', url, start, end, timeUnit, jlab.colors.linacs, "# Cavities Bypassed");
};

$(function () {
    console.log("bypassedUrl: " + jlab.bypassedUrl + " - start: " + jlab.start + " - end: " + jlab.end + " - timeUnit - " + jlab.timeUnit);
    jlab.bypassed.loadCharts(jlab.bypassedUrl, jlab.start, jlab.end, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
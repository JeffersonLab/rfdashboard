/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.bypassedUrl = "/RFDashboard/ajax/bypassed";

jlab.bypassed = jlab.bypassed || {};
jlab.bypassed.loadCharts = function (url, start, end, timeUnit) {
    jlab.barChart.updateChart('bypassed-count-by-linac', url, start, end, timeUnit);
};

$(function () {
    console.log("bypassedUrl: " + jlab.bypassedUrl + " - start: " + jlab.start + " - end: " + jlab.end + " - timeUnit - " + jlab.timeUnit);
    jlab.bypassed.loadCharts(jlab.bypassedUrl, jlab.start, jlab.end, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
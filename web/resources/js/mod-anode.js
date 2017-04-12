/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.mavUrl = "/RFDashboard/ajax/mod-anode";

jlab.mod_anode = jlab.mod_anode || {};
jlab.mod_anode.loadCharts = function (url, start, end, timeUnit) {
    jlab.barChart.updateChart('mav-count-by-linac', url, start, end, timeUnit);
};

$(function () {
    console.log("mavUrl: " + jlab.mavUrl + " - start: " + jlab.start + " - end: " + jlab.end + " - timeUnit - " + jlab.timeUnit);
    jlab.mod_anode.loadCharts(jlab.mavUrl, jlab.start, jlab.end, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
});
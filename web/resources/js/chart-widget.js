/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.chartWidget = jlab.chartWidget || {};

jlab.chartWidget.addTitle = function(chartId, title) {
  $("#" + chartId + "-chart-wrap").prepend("<div id='" + chartId + "-chart-title' class='chart-title'>" + title + "</div>");  
};

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.lemScanUrl = "/RFDashboard/ajax/lem-scan";

jlab.cavity = jlab.cavity || {};
jlab.cavity.loadCharts = function (url, start, end, diffEnd, timeUnit) {

    jlab.showChartLoading("lem-scan");
    var lemScan = $.getJSON(url, {type: "day-scan", date: diffEnd, out: "flot"});
    lemScan.done(function(json) {
        var settings = {
            colors: jlab.colors.linacs.slice(1, 4),          // Grab the North, South, and Total colors
            labels: json.labels,
            timeUnit: "day",
            title: "C25 Trips/Hour",
            tooltips: true,
            tooltipX: "Energy",
            tooltipY: "Trips/Hr",
            legend: true
        };
        var flotOptions = {
            xaxis: { mode: null, min: 1000, max: 1190 },
            yaxis: { min: 0, max: 15}
        };
        var flotData = [];
        for(i=0; i < json.data.length; i++) {
            flotData[i] = {data: json.data[i], points:{show: false}, lines:{show:true} };
        }

        jlab.hideChartLoading("lem-scan");
        jlab.flotCharts.drawChart("lem-scan", flotData, flotOptions, settings);
    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.hideChartLoading("lem-scan", "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);;
    });
};

// This returns an array of energy reaches for the machine and linac defined as the energy where each linac or the whole machine
// experience 8 C25 trips/hour.  Data is the JSON object returned by the ajax request to /ajax/lem-scan?type=day-scan&...
// results rounded to one decimal place.
jlab.cavity.getEnergyReach = function (jsonData, chartId) {
    var tripData = {
        north: {energy: [], tripRate: []},
        south: {energy: [], tripRate: []},
        total: {energy: [], tripRate: []}
    };
    var data = jsonData.data;
    var labels = jsonData.labels;

    // Get the flot oriented data structure into something everpolate wants to use
    for (var i = 0; i < data.length; i++) {
        for (var j = 0; j < data[0].length; j++) {
            var energy = data[i][j][0];
            var tripRate = data[i][j][1];
            if (tripRate !== "null") {
                tripData[labels[i].toLowerCase()].energy[j] = Number(energy);
                tripData[labels[i].toLowerCase()].tripRate[j] = Number(tripRate);
            }
        }
    }

    // Everpolate should already exist - needs everpolate library to be included in the calling jsp
    var reaches = [];
    if (tripData.north.tripRate.length > 2) {
        reaches[0] = everpolate.linear([8], tripData.north.tripRate, tripData.north.energy)[0].toFixed(1);
    } else {
        reaches[0] = "N/A";
    }
    if (tripData.south.tripRate.length > 2) {
        reaches[1] = everpolate.linear([8], tripData.south.tripRate, tripData.south.energy)[0].toFixed(1);
    } else {
        reaches[1] = "N/A";
    }
    if (tripData.total.tripRate.length > 2) {
    reaches[2] = everpolate.linear([8], tripData.total.tripRate, tripData.total.energy)[0].toFixed(1);
    } else {
        reaches[2] = "N/A";
    }

    return reaches;
};

jlab.cavity.addLegend = function (chartId, colors, labels, reaches) {
    var legendString = '<div class="chart-legend" id=' + chartId + '-chart-legend">\n';
    legendString += "<table>";
    legendString += '<tr><th colspan="2">Linac</th><th>Reach</th><tr>';
    if (colors.length !== labels.length) {
        console.log("Error: unequal number of colors and labels");
    }
    for (var i = 0; i < colors.length; i++) {
        legendString += '<tr><td><div class=color-box style="background-color: ' + colors[i] + ';"></div></td><td>' + labels[i] + '</td><td>' + reaches[i] + '</td></tr>';
    }
    legendString += '</table></div>';
    $("#" + chartId + "-legend-panel").prepend(legendString);
};

$(function () {
    // The "diff-table-*" names need to be be manually kept in sync with the IDs given in the JSP.  This shouldn't need to
    // often, but if it does, we should put them in variables, etc.
   // jlab.cavity.createBasicAdvTable("diff-table-basic", "diff-table-advanced", jlab.diffStart, jlab.diffEnd);
   // jlab.cavity.createTotalsTable("summary-table", jlab.diffStart, jlab.diffEnd);
    jlab.cavity.loadCharts(jlab.lemScanUrl, jlab.start, jlab.end, jlab.diffEnd, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });
    
    // This enables the "Basic/Advanced" menu button to toggle between the two tables.  diff-table-advanced starts out with
    // display: none.
    $("#menu-toggle").click(function() {
        $('#diff-table-basic-wrap').toggle();
        $('#diff-table-advanced-wrap').toggle();
    });
    
    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function() {
       $("#page-details-dialog").dialog("open");
    });
});
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.energyReach = jlab.energyReach || {};

// Create and show the single day, lem-scan trip curve plot
jlab.energyReach.loadLemScanChart = function (chartId, date, scanData) {


    var title = "<strong>LEM Estimated Trip Rates</strong><br/><div style='font-size:smaller'>" + date + "</div>";
    if (typeof scanData === "undefined") {
        $("#" + chartId).append(title + "<br>No data available");
        return;
    }

    var reachRates = jlab.energyReach.getReachTripRates(new Date(date));
    var linacRate = reachRates[0];
    var totalRate = reachRates[1];

    var settings = {
        colors: jlab.colors.linacs.slice(1, 4), // Grab the North, South, and Total colors
        labels: scanData.labels,
        timeUnit: "day",
        title: title,
        tooltips: true,
        tooltipX: "Energy",
        tooltipY: "Trips/Hr",
        legend: false
    };
    var flotOptions = {
        xaxis: {axisLabel: "Linac Energy (MeV)", mode: null, min: 1000, max: 1190},
        yaxis: {axisLabel: "Trips / Hour", min: 0, max: 15},
        grid: {clickable: false, markings: [{yaxis: {from: totalRate, to: totalRate}, color: "#000000"}, {yaxis: {from: linacRate, to: linacRate}, color: "#000000"}]}
    };

    var flotData = [];
    for (i = 0; i < scanData.data.length; i++) {
        flotData[i] = {data: scanData.data[i], points: {show: false}, lines: {show: true}};
    }

    var plot = jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);

    // Add a caption that lists the energy reach of North, South, and Total
    var reaches = jlab.energyReach.getEnergyReach(scanData);
    // Add a custom legend off to the side
    jlab.energyReach.addLegend(chartId, settings.colors, scanData.labels, reaches);

    // Add the horizontal lines for 4 and 8 trips/hr with annotations
    var pTot = plot.pointOffset({x: 1010, y: totalRate});
    var pLin = plot.pointOffset({x: 1010, y: linacRate});
    $("#" + chartId).append("<div style='position:absolute;left:" + pTot.left + "px;top:" + (pTot.top - 17) + "px; color:#666;font-size:smaller'>" + totalRate +" trips/hr</div>");
    $("#" + chartId).append("<div style='position:absolute;left:" + pLin.left + "px;top:" + (pLin.top - 17) + "px; color:#666;font-size:smaller'>" + linacRate + " trips/hr</div>");
};

// The creates and shows the energy reach barchart
jlab.energyReach.loadEnergyReachChart = function (chartId, start, end, reachData) {

    var title = "<strong>Linac Energy Reach</strong><br/><div style='font-size:smaller'>" + start + " to " + end + "</div>";
    if (typeof reachData === "undefined") {
        console.log(chartId);
        $("#" + chartId).append("<br>" + title + "<br>No data available");
        return;
    }

    var timeUnit = "day";
    var settings = {
        colors: jlab.colors.energyReach,
        labels: reachData.labels,
        timeUnit: timeUnit,
        title: title,
        tooltips: true,
        tooltipX: "Date",
        tooltipY: "Energy",
        legend: true,
        chartType: "bar"
    };

    // Flot wants times in milliseconds from UTC
    var xmin = new Date(start).getTime();
    var xmax = new Date(end).getTime();
    var flotOptions = {
        xaxis: {mode: "time", min: xmin, max: xmax},
        yaxis: {axisLabel: "Linac Energy (MeV)", min: 1000, max: 1190},
        grid: {clickable: true}
    };

    var flotData = [];
    for (i = 0; i < reachData.data.length; i++) {
        flotData[i] = {data: reachData.data[i], points: {show: false}};
    }

    jlab.hideChartLoading(chartId);
    var plot = jlab.flotCharts.drawChart(chartId, flotData, flotOptions, settings);

    $('#energy-reach').bind("plotclick", function (event, pos, item) {
        if (item) {
            var timestamp = item.series.data[item.dataIndex][0];
            var dateString = jlab.millisToDate(timestamp);
            var url = jlab.contextPath + "/energy-reach?start=" + start + "&end=" + end + "&diffStart=" +
                    jlab.addDays(dateString, -1) + "&diffEnd=" + dateString;
            window.location.href = url;
        }
    });
};


/*
 * jsonData - ajax response from energy-reach end point
 * date - Date object for which we calculate the energy reach (reflects changes in LEM modeling of C100)
 * Note: This returns an array of energy reaches for the machine and linac defined as the energy where each linac or the whole machine
 * experience 8 C25 trips/hour.  Data is the JSON object returned by the ajax request to /ajax/lem-scan?type=day-scan&...
 * results rounded to one decimal place.
 */
jlab.energyReach.getEnergyReach = function (jsonData, date) {
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
    
    var rates = jlab.energyReach.getReachTripRates(date);
    var linacRate = rates[0];
    var totalRate = rates[1];

    // Everpolate should already exist - needs everpolate library to be included in the calling jsp
    var reaches = [];
    if (tripData.north.tripRate.length > 2) {
        reaches[0] = everpolate.linear([linacRate], tripData.north.tripRate, tripData.north.energy)[0].toFixed(1);
    } else {
        reaches[0] = "N/A";
    }
    if (tripData.south.tripRate.length > 2) {
        reaches[1] = everpolate.linear([linacRate], tripData.south.tripRate, tripData.south.energy)[0].toFixed(1);
    } else {
        reaches[1] = "N/A";
    }
    if (tripData.total.tripRate.length > 2) {
        reaches[2] = everpolate.linear([totalRate], tripData.total.tripRate, tripData.total.energy)[0].toFixed(1);
    } else {
        reaches[2] = "N/A";
    }

    return reaches;
};


// "Target" trip rates.  The most we want is 10 total trips / hour.  The reach trip rate changes over time depending on what
// is modeled in LEM.  Before 2018, C100s were not in LEM and accounted for about 2 trips / hour, leaving 8 total/hr for LEM.
// Starting 2018, most C100s had trip models in LEM, and LEM appears to be doing a good job of accounting for most RF trips.
// date - date object for which to return the trip rates
jlab.energyReach.getReachTripRates = function (date) {
    var linacRate = 4;
    var totalRate = 8;
    if (typeof date !== "undefined") {
        var c100Date = new Date(2018, 0, 1);
        if (date > c100Date) {
            linacRate = 5;
            totalRate = 10;
        }
    }
    return [linacRate, totalRate];
};



jlab.energyReach.addLegend = function (chartId, colors, labels, reaches) {
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

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });
    // 
    // This enables the "Basic/Advanced" menu button to toggle between the two tables.  diff-table-advanced starts out with
    // display: none.
    $("#menu-toggle").click(function () {
        $('#diff-table-basic').toggle();
        $('#diff-table-advanced').toggle();
    });
    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd",
        showButtonPanel: true
    });

    jlab.energyReach.loadLemScanChart("lem-scan", jlab.diffEnd, jlab.dayScanData);
    jlab.energyReach.loadEnergyReachChart("energy-reach", jlab.start, jlab.end, jlab.energyReachData);
    jlab.cavity.createCavitySetPointTables("#diff-table-basic", "#diff-table-advanced", "#summary-table", jlab.cavityData, jlab.diffStart, jlab.diffEnd);

    jlab.util.initCalendarStartEnd("#main-calendar");
    jlab.util.initCalendarStartEnd("#delta-calendar");

});
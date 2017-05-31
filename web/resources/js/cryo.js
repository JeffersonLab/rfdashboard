var jlab = jlab || {};
jlab.cryo = {};

// This request follows a different model than how other charts are handled.  Instead of directly querying the RFDashboard
// server for the data, the client performs an ajax JSONP request against the myaweb server.  This was done as the myaweb
// myStats request can take a while and will probably be changed to use some sort of websocket or async job processing
// method.  Also, querying the data currently requieres multiple multiple requests which are done sequentially here.
jlab.cryo.updateCryoPressureChart = function (chartId, linac, start, end, timeUnit) {
    var pv;
    switch (linac.toLowerCase()) {
        case 'south':
            pv = 'CPI5107B';
            break;
        case 'north':
            pv = 'CPI4107B';
    }

    var numDays;
    switch (timeUnit) {
        case 'day':
            numDays = 1;
            break;
        case 'week':
            numDays = 7;
            break;
        default:
            window.console && console.log("Error: unknown timeUnit specified.  No cryo data request made.");
            return;
    }

    var dayDiff = jlab.daysBetweenDates(start, end);
    var numSteps = Math.floor(dayDiff / numDays);

    var i = 0;
    var promise = $.ajax({
        url: "http://myaweb.acc.jlab.org/myStatsSampler/data",
        data: {
            b: start,
            n: numSteps,
            s: 1,
            sUnit: timeUnit,  // supports second, day, week as of 2017-05-30
            l: "CPI4107B,CPI5107B"
//            l: pv
        },
        dataType: "jsonp",
        jsonp: "jsonp",
        beforeSend: jlab.showChartLoading(chartId)
    });
    
    // The myStatsSampler service can respond in the following ways:
    //  - Return some non-OK HTTP status / server error without data (promise.fail - expected failure behavior)
    //  - Return some non-OK HTTP status and something totally unexpected (promise.fail - unexpected failure behavior)
    //  - Return some OK HTTP status, but each bin's object can contain an error parameter or data (promise.done - successful myStatsSampler
    //    run, but zero or more bins had errors)
    //    
    // Expecting {data: [ {start: 'yyyy-MM-dd[ HH:mm:ss]', output: [{name: "PVName", min: "##.###',...}, ..., {name: ...}]}
    //                          {start: ..., output: [...]}
    //                }
    // OR
    // {data: [], error: "<message>"}
    promise.done(function (json) {
        var flotData;

        var labels = ["<b>North</b> (CPI4107B)", "<b>South</b> (CPI5107B)"];
        var colors = jlab.colors.linacs.slice(1,3);

        if ( ! Array.isArray(json.data) ) {
            jlab.hideChartLoading(chartId, "Unexpected error querying data service.");
        } else {
            flotData = new Array(json.data[0].output.length);
            var d, mean, sigma;
            for (var i = 0; i < json.data.length; i++) {
                d = new Date(json.data[i].start); // treated as UTC, but thats how all of the dates are being displayed.
                for (var j = 0; j < json.data[i].output.length; j++) {
                    if ( typeof flotData[j] !== "object" ) {
                        flotData[j] = {
                            data: new Array(),
                            label: labels[j],
                            color: colors[j]
                        };
                    }
                    mean = json.data[i].output[j].mean;
                    sigma = json.data[i].output[j].sigma;
                    flotData[j].data.push( [d.getTime(), mean, sigma]);
                }
            }
        }
        jlab.hideChartLoading(chartId);
        var settings = {
            timeUnit: timeUnit,
            colors: colors,
            labels: labels,
            title: "Linac Cryogen Pressure</strong><br/>(" + start + " to " + end + " by " + timeUnit + ")<strong>"
        };
        var flotOptions = {yaxis: {axisLabel: "Pressure"}};
        // Expects an array of flot data arrays.  E.g.,
        // [ [millis, mean, sigma], [millis, mean, sigma], ...],
        //   [millis, mean, sigma], [millis, mean, sigma], ...],
        //   ...
        // ]
     
        jlab.errorBarChart.drawChart(chartId, flotData, flotOptions, settings);
    });
    
    promise.fail(function (jqXHR) {
        // Unless something went really wrong, the responseText should be a json object and should have an error parameter
        var json;
        var message;
        var reqDates = start + '-' + end;
        try {
            if (typeof jqXHR.responseText === 'undefined' || jqXHR.responseText === '') {
                json = {};
            } else {
                json = $.parseJSON(jqXHR.responseText);
            }
        } catch (err) {
            message = reqDates + ' response is not JSON: ' + jqXHR.responseText;
            json = {};
        }

        message = json.error || message || 'Server did not handle request for ' + reqDates;
        window.console && console.log(message);
        jlab.hideChartLoading(chartId, "Error querying data service");
    });
};

$(function() {
    
    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });

    jlab.cryo.updateCryoPressureChart('cryo-pressure-north', 'north', jlab.start, jlab.end, jlab.timeUnit);
    

});
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.cavity = jlab.cavity || {};


jlab.cavity.getCavityData = function(settings) {
    
     var start, end, timeUnit, success;
    if (typeof settings === "undefined" || typeof settings !== "object") {
        exitFunc("Error: Settings object required");
    }
    
    // Required
    if ( typeof settings.start === "undefined" ) {
        exitFunc("Error: settings.start required");
    } else {
        start = settings.start;
    }
    if ( typeof settings.end === "undefined" ) {
        exitFunc("Error: settings.end required");
    } else {
        end = settings.end;
    }
    if ( typeof settings.timeUnit === "undefined" ) {
        exitFunc("Error: settings.timeUnit required");
    } else {
        timeUnit = settings.timeUnit;
    }
    if ( typeof settings.success !== "function" ) {
        exitFunc("Error: settings.success function required");
    } else {
        success = settings.success;
    }
    
    var out = {
        error: null
    };
    var ajaxSettings = {
        url: "/RFDashboard/ajax/cavity",
        "data": {
            "start": start,
            "end": end,
            "timeUnit": timeUnit
        },
        dataType: "json",
        error: function (jqXHR, textStatus, errorThrown) {
            out.error = {
                "textStatus": textStatus,
                "errorThrown": errorThrown
            };
            console.log("Error querying cavity service", ajaxSettings, textStatus, errorThrown);
        },
        "success": success
    };
    $.ajax(ajaxSettings);
    console.log("launched ajax request");
};


/* 
 * Create a cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
jlab.cavity.createTable = function (tableId, date) {

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
                    tableString += "<tr><td>" + cavities[j].name + "</td><td>" + cavities[j].moduleType + "</td><td>" +
                            cavities[j].modAnodeVoltage_kv + "</td><td>" + cavities[j].gset + "</tr>";
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

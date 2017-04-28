/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.cavity = jlab.cavity || {};


jlab.cavity.getCavityData = function(settings) {
    
    var exitFunc = function (msg) {
        console.log(msg);
        throw msg;
    };

     var start, end, timeUnit, success, dates;
     var asRange = true;
    if (typeof settings === "undefined" || typeof settings !== "object") {
        exitFunc("Error: Settings object required");
    }
    
    // Required alwyas
    if ( typeof settings.success !== "function" ) {
        exitFunc("Error: settings.success function required");
    } else {
        success = settings.success;
    }
    
    // Different parameters are required based on value of asRange
    if ( typeof settings.asRange !== "undefined" && settings.asRange === false ) {
        asRange = false;
        // must supply a date array with asRange == false
        if (typeof settings.dates !== "object") {
            exitFunc("Error: settings.dates required when asRange is false");
        } else {
            dates = settings.dates;
        }
    } else {
        if (typeof settings.start === "undefined") {
            exitFunc("Error: settings.start required");
        } else {
            start = settings.start;
        }
        if (typeof settings.end === "undefined") {
            exitFunc("Error: settings.end required");
        } else {
            end = settings.end;
        }
        if (typeof settings.timeUnit === "undefined") {
            exitFunc("Error: settings.timeUnit required");
        } else {
            timeUnit = settings.timeUnit;
        }

    }
    
    var ajaxData;
    if ( asRange ) {
        ajaxData = {
            "start": start,
            "end": end,
            "timeUnit": timeUnit
        };
    } else {
        ajaxData = {
            "date": dates
        };
    }
    
    var out = {
        error: null
    };
    var ajaxSettings = {
        traditional: true,
        url: "/RFDashboard/ajax/cavity",
        "data": ajaxData,
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
    console.log(ajaxSettings);
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

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


/* 
 * Create a two tables showing advanced/basic changes on cavity-by-cavity basis between start and end dates.
 * Only one cavity is shown at a time.  Which one is displayed is changed by the on-screen button toggle.
 * basicTableId - the tablesorter tag tableId for the basic table 
 * advTableId - the tablesorter tag tableId for the advanced
 * date - the date for which to query data
 * */
jlab.cavity.createBasicAdvTable = function (basicTableId, advTableId, start, end) {

    jlab.cavity.getCavityData({
        asRange: false,
        dates: [start, end],
        success: function (jsonData, textStatus, jqXHR) {
            var data = jsonData.data;
            var advTableString = "<table id=\"" + advTableId + "\" class=\"tablesorter\">";
            var basicTableString = "<table id=\"" + basicTableId + "\" class=\"tablesorter\">";
            advTableString += "<thead><tr><th>Name</th><th>Module Type</th>" + 
                    "<th>Old MAV</th><th>New MAV</th><th>Delta MAV (kV)</th>" + 
                    "<th>Old GSET</th><th>New GSET</th><th>Delta GSET</th>" +
                    "<th>Old ODHV</th><th>New ODHV</th><th>Delta ODHV</th>" +
                    "<th>Module Changed</th><th>Found Match</th></tr></thead>";
            advTableString += "<tbody>";
            basicTableString += "<thead><tr><th>Name</th><th>Module Type</th>" + 
                    "<th>Old GSET</th><th>New GSET</th><th>Delta GSET</th>" +
                    "<th>Old ODHV</th><th>New ODHV</th><th>Delta ODHV</th>" +
                    "</tr></thead>";
            basicTableString += "<tbody>";
            if (data[0].cavities === null || data[1].cavities === null) {
                console.log("Error processing cavity service data. start:" + start + "  end: " + end);
                throw "Data service returned null data";
            }
            
            console.log(data);
            var cavStart = data[0].cavities;
            var cavEnd = data[1].cavities;

            for (var i = 0; i < cavEnd.length; i++) {
                var foundMatch = false;
                for (var j = 0; j < cavStart.length; j++) {
                    
                    if (cavEnd[i].name === cavStart[j].name) {
                        foundMatch = true;
                        
                        var name, cmType, dGset, dMav, dOdvh, moduleChange;
                        var oldGset, oldMav, newGset, newMav, oldOdvh, newOdvh;
                        name = cavEnd[i].name;
                        if ( cavEnd[i].moduleType === cavStart[j].moduleType ) {
                            moduleChange = false;
                            cmType = cavEnd[i].moduleType;
                        } else {
                            moduleChange = true;
                            cmType = cavStart[j].moduleType + "/" + cavEnd[i].moduleType;
                        }
                        
                        // Process gsets - the cavity data service hands back one of three things
                        // a number (data existed, all is well)
                        // an empty string (data was not returned from the gset service)
                        // nothing (somthing went really wrong. this isn't expected, but still good to check)
                        if (typeof cavStart[j].gset !== "undefined") {
                            oldGset = cavStart[j].gset;
                        } else {
                            oldGset = "";
                        }
                        if (typeof cavEnd[i].gset !== "undefined") {
                            newGset = cavEnd[i].gset;
                        } else {
                            newGset = "";
                        }
                        if ( newGset !== "" && oldGset !== ""  && newGset !== null && oldGset !== null) {
                            dGset = newGset - oldGset;
                        } else {
                            dGset = "N/A";
                        }
                        
                        // Process MAVs
                        if (typeof cavStart[j].modAnodeVoltage_kv !== "undefined") {
                            oldMav = cavStart[j].modAnodeVoltage_kv;
                        } else {
                            oldMav = "";
                        }
                        if (typeof cavEnd[i].modAnodeVoltage_kv !== "undefined") {
                            newMav = cavEnd[i].modAnodeVoltage_kv;
                        } else {
                            newMav = "";
                        }
                        if ( newMav !== "" && oldMav !== "" && newMav !== null && oldMav !== null) {
                            dMav = newMav - oldMav;
                        } else {
                            dMav = "N/A";
                        }
                        
                        // Process Ops Drive Highs
                        if (typeof cavStart[j].odvh !== "undefined") {
                            oldOdvh = cavStart[j].odvh;
                        } else {
                            oldOdvh = "";
                        }
                        if (typeof cavEnd[i].odvh !== "undefined") {
                            newOdvh = cavEnd[i].odvh;
                        } else {
                            newOdvh = "";
                        }
                        if ( newOdvh !== "" && oldOdvh !== "" && newOdvh !== null && oldOdvh !== null) {
                            dOdvh = newOdvh - oldOdvh;
                        } else {
                            dOdvh = "N/A";
                        }
                        
                        //console.log([name, cmType, oldMav, newMav, dMav, oldGset, newGset, dGset, moduleChange]);
                        // Add a row to the table for this cavity
                        advTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                                oldMav.toFixed(2)  + "</td><td>" + newMav.toFixed(2)  + "</td><td>" + dMav.toFixed(2) + "</td><td>" +
                                oldGset.toFixed(2)  + "</td><td>" + newGset.toFixed(2) +"</td><td>" + dGset.toFixed(2) + "</td><td>" +
                                oldOdvh.toFixed(2)  + "</td><td>" + newOdvh.toFixed(2) +"</td><td>" + dOdvh.toFixed(2) + "</td><td>" +
                                moduleChange + "</td><td>" + true + "</td></tr>";
                        basicTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                                oldGset.toFixed(2)  + "</td><td>" + newGset.toFixed(2) +"</td><td>" + dGset.toFixed(2) + "</td><td>" +
                                oldOdvh.toFixed(2)  + "</td><td>" + newOdvh.toFixed(2) +"</td><td>" + dOdvh.toFixed(2) + "</td>" +
                                "</tr>";
                    }
                }
                if ( ! foundMatch ) {
                    advTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                            oldMav.toFixed(2)  + "</td><td>" + newMav.toFixed(2)  + "</td><td>" + dMav.toFixed(2) + "</td><td>" +
                            oldGset.toFixed(2) + "</td><td>" + newGset.toFixed(2) + "</td><td>" + dGset.toFixed(2) + "</td><td>" +
                            oldOdvh.toFixed(2)  + "</td><td>" + newOdvh.toFixed(2) +"</td><td>" + dOdvh.toFixed(2) + "</td><td>" +
                            "N/A" + "</td><td>" + true + "</td></tr>";
                    basicTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                            oldGset.toFixed(2) + "</td><td>" + newGset.toFixed(2) + "</td><td>" + dGset.toFixed(2) + "</td>" +
                            oldOdvh.toFixed(2)  + "</td><td>" + newOdvh.toFixed(2) +"</td><td>" + dOdvh.toFixed(2) + "</td>" +
                            "</tr>";
                }
            }
            advTableString += "</tbody></table>";
            basicTableString += "</tbody></table>";
            $("#" + advTableId + "-table").append(advTableString);
            $("#" + basicTableId + "-table").append(basicTableString);
            // Setup the sortable cavity tables
            $("#" + advTableId)
                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
                    .tablesorterPager({container: $("#" + advTableId + "-pager")});
            $("#" + basicTableId)
                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
                    .tablesorterPager({container: $("#" + basicTableId + "-pager")});
        }
    });
};

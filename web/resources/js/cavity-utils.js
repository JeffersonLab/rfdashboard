/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.cavity = jlab.cavity || {};


jlab.cavity.getCavityData = function (settings) {

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
    if (typeof settings.success !== "function") {
        exitFunc("Error: settings.success function required");
    } else {
        success = settings.success;
    }

    // Different parameters are required based on value of asRange
    if (typeof settings.asRange !== "undefined" && settings.asRange === false) {
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
    if (asRange) {
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
    $.ajax(ajaxSettings);
};

// Takes a single date response of the AJAX cavity service and turns it into a map keyed on cavity name
jlab.cavity.createCavityMap = function (cavityData) {
    var map = new Map();
    var cavities = cavityData.cavities;
    for (var i = 0; i < cavities.length; i++) {
        map.set(cavities[i].name, cavities[i]);
    }
    return map;
};

/*
 * This takes the json response from a AJAX cavity service query and returns an array of cavity map objects.
 * This function assumes that only two dates were queried from the cavity service.  Returns null if date in the object
 * don't match the specified dates.
 * @param {type} cavityJson The JSON object returned from the cavity AJAX request
 * @param {type} start The intended "start" date
 * @param {type} end The intedned "end" date
 * @returns {Array}
 */
jlab.cavity.getStartEndMaps = function (cavityJson, start, end) {
    var cavityMaps = [];

    if (cavityJson.data[0].date === start && cavityJson.data[1].date === end) {
        cavityMaps[0] = jlab.cavity.createCavityMap(cavityJson.data[0]);
        cavityMaps[1] = jlab.cavity.createCavityMap(cavityJson.data[1]);
    } else if (cavityJson.data[1].date === start && cavityJson.data[0].date === end) {
        cavityMaps[0] = jlab.cavity.createCavityMap(cavityJson.data[1]);
        cavityMaps[1] = jlab.cavity.createCavityMap(cavityJson.data[0]);
    } else {
        console.log("Error: received unexpected AJAX cavity service repsonse", cavityJson);
        return null;
    }

    return cavityMaps;
};

// This turns two cavity maps into a 2D array that can be used to create an HTML table.  The table
// includes the request parameters and the delta for those values between start and end.
jlab.cavity.cavityMapsTo2DArray = function (startMap, endMap, linacs, cmtypes, properties) {

    // Check that the two maps contain identical cavity sets
    if (startMap.keys().length !== endMap.keys().length) {
        console.log("Error: start and end maps contain different cavities");
        return null;
    }
    for (let sName of startMap.keys()) {
        var match = false;
        for (let eName of endMap.keys()) {
            if (sName == eName) {
                match = true; }
        }
        if (!match) {
            console.log("Error: start and end maps contain different cavities");
            return null;
        }
    }

    // Setup the 2D cavity array with the header row
    var cavArray = new Array();

    var hArray = new Array();

    hArray.push("Name");
    if (properties.includes("cmtype")) {
        hArray.push("Module Type");
    }
    if (properties.includes("linac")) {
        hArray.push("Linac");
    }
    for (let prop of properties) {
        switch (prop) {
            case "cmtype":
                break; //handled explicitly
            case "linac":
                break; //handled explicitly
            case "comments":
                break; //handled explicitly
            case "length":
                hArray.push("Length");
                break;
            case "modAnode":
                hArray.push("Old M.A.V.");
                hArray.push("New M.A.V.");
                hArray.push("Delta M.A.V.");
                break;
            case "gset":
                hArray.push("Old GSET");
                hArray.push("New GSET");
                hArray.push("Delta GSET");
                break;
            case "odvh":
                hArray.push("Old ODVH");
                hArray.push("New ODVH");
                hArray.push("Delta ODVH");
                break;
            case "opsGsetMax":
                hArray.push("Old OpsGsetMax");
                hArray.push("New OpsGsetMax");
                hArray.push("Delta OpsGsetMax");
                break;
            case "maxGset":
                hArray.push("Old MaxGSET");
                hArray.push("New MaxGSET");
                hArray.push("Delta MaxGSET");
                break;
            case "q0":
                hArray.push("Old Q0");
                hArray.push("New Q0");
                hArray.push("Delta Q0");
                break;
            case "qExternal":
                hArray.push("Old Q External");
                hArray.push("New Q External");
                hArray.push("Delta Q External");
                break;
            case "tripOffset":
                hArray.push("Old Trip Offset");
                hArray.push("New Trip Offset");
                hArray.push("Delta  Trip Offset");
                break;
            case "tripSlope":
                hArray.push("Old Trip Slope");
                hArray.push("New Trip Slope");
                hArray.push("Delta Trip Slope");
                break;
            default:
                hArray.push(prop);
        }
    }
    if (properties.includes("comments")) {
        hArray.push("Comments");
    }
    cavArray.push(hArray);

    // Now process the cavities and create the data rows of the cavArray
    var startCav, endCav;
    var j = 0;
    for (let name of startMap.keys()) {

        startCav = startMap.get(name);
        endCav = endMap.get(name);

        // Filter out the unwanted cavities
        if (!linacs.includes(startCav.linac.toLowerCase())) {
            continue;
        }
        if (!cmtypes.includes(startCav.moduleType.toUpperCase())) {
            continue;
        }

        rowArray = new Array();
        rowArray.push(startCav.name);
        if (properties.includes("cmtype")) {
            var cmtype = startCav.moduleType;
            if (startCav.moduleType != endCav.moduleType) {
                cmtype += "/" + endCav.moduleType;
            }
            rowArray.push(cmtype);
        }
        if (properties.includes("linac")) {
            rowArray.push(startCav.linac);
            ;
        }
        for (let prop of properties) {
            switch (prop) {
                case "cmtype":
                    break; //handled explicitly
                case "linac":
                    break; //handled explicitly
                case "comments":
                    break; //handled explicitly
                case "length":
                    var length = startCav.length.toFixed(2);
                    if (startCav.length != endCav.length) {
                        length += "/" + endCav.length.toFixed(2);
                    }
                    rowArray.push(length);
                    break;
                case "modAnode":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "modAnodeVoltage_kv"));
                    break;
                case "odvh":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "odvh"));
                    break;
                case "opsGsetMax":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "opsGsetMax"));
                    break;
                case "maxGset":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "maxGset"));
                    break;
                case "q0":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "q0", true));
                    break;
                case "qExternal":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "qExternal", true));
                    break;
                case "tripOffset":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "tripOffset"));
                    break;
                case "tripSlope":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "tripSlope"));
                    break;
                default:
                    rowArray.push(startCav.prop);
            }
        }
        if (properties.includes("comments")) {
            rowArray.push("");
        }
        cavArray.push(rowArray);
    }

    console.log(cavArray);
    return cavArray;
}


/*
 * 
 * @param {type} startCav The cavity object with the earlier timestamp
 * @param {type} endCav The cavity object with the later timestamp
 * @param {type} prop The property to be processed
 * @param {type} scientific A boolean as to whether the property is a string representing a number in scientific notation
 * @returns {undefined} An array containing the the start ("Old") string, the end ("New") string, and the delta string
 */
jlab.cavity.processNumericTableEntry = function (startCav, endCav, prop, scientific) {
    var out = new Array();
    var sp = startCav[prop];
    var ep = endCav[prop];

    if (sp === "") {
        out.push("");
    } else if (scientific) {
        sp = Number(sp);
        out.push(sp.toExponential(2));
    } else {
        out.push(sp.toFixed(2));
    }

    if (ep === "") {
        out.push("");
    } else if (scientific) {
        ep = Number(ep);
        out.push(ep.toExponential(2));
    } else {
        out.push(ep.toFixed(2));
    }

    if (sp === "" || ep === "") {
        out.push("N/A");
    } else if (scientific) {
        out.push((ep - sp).toExponential(2));
    } else {
        out.push((ep - sp).toFixed(2));
    }

    return out;
}

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

/* This function relies on the tablesorter tag,  jquery.tablesorter (JS/CSS), and jquery.tablesorter.pager (JS/CSS).  It creates
 * a sortable table of totals, and their differences between start and end.
 */
jlab.cavity.createTotalsTable = function (tableId, start, end) {
    jlab.cavity.getCavityData({
        asRange: false,
        dates: [start, end],
        success: function (jsonData, textStatus, jqXHR) {
            var data = jsonData.data;
            var tableString = "<table id=\"" + tableId + "\" class=\"tablesorter\">";
            tableString += "<thead><tr><th>Subset</th>" +
                    "<th>Old GSET</th><th>New GSET</th><th>Delta GSET</th>" +
                    "<th>Old ODHV</th><th>New ODHV</th><th>Delta ODHV</th>" +
                    "</tr></thead>";
            tableString += "<tbody>";
            if (data[0].cavities === null || data[1].cavities === null) {
                var msg = "Error processing data.";
                console.log("Error processing cavity service data. start:" + start + "  end: " + end);
                $("#" + tableId + "-table").append(msg);
            }
            var cavStart = data[0].cavities;
            var cavEnd = data[1].cavities;

            // The array counts are [GSET, ODVH]
            var startTotals = {Total: [0, 0], QTR: [0, 0], C25: [0, 0], C50: [0, 0], C100: [0, 0]};
            var endTotals = {Total: [0, 0], QTR: [0, 0], C25: [0, 0], C50: [0, 0], C100: [0, 0]};
            var diffTotals = {Total: [0, 0], QTR: [0, 0], C25: [0, 0], C50: [0, 0], C100: [0, 0]};

            // Tally up totals for the start period, then the end period, then calculate the difference in the totals
            for (var i = 0; i < cavStart.length; i++) {
                if (cavStart[i].gset !== "") {
                    startTotals.Total[0] += cavStart[i].gset;
                    startTotals[cavStart[i].moduleType][0] += cavStart[i].gset;
                }
                if (cavStart[i].gset !== "") {
                    startTotals.Total[1] += cavStart[i].odvh;
                    startTotals[cavStart[i].moduleType][1] += cavStart[i].odvh;
                }
            }
            for (var i = 0; i < cavEnd.length; i++) {
                if (cavEnd[i].gset !== "") {
                    endTotals.Total[0] += cavEnd[i].gset;
                    endTotals[cavEnd[i].moduleType][0] += cavEnd[i].gset;
                }
                if (cavEnd[i].odvh !== "") {
                    endTotals.Total[1] += cavEnd[i].odvh;
                    endTotals[cavEnd[i].moduleType][1] += cavEnd[i].odvh;
                }
            }
            // All three of the totals objects should have the same properties
            for (var prop in startTotals) {
                if (startTotals.hasOwnProperty(prop)) {
                    for (var i = 0; i < diffTotals[prop].length; i++) {
                        diffTotals[prop][i] = endTotals[prop][i] - startTotals[prop][i];
                    }
                }
            }

            for (var prop in startTotals) {
                if (startTotals.hasOwnProperty(prop)) {
                    tableString += "<tr><td>" + prop + "</td><td>" +
                            startTotals[prop][0].toFixed(2) + "</td><td>" + endTotals[prop][0].toFixed(2) + "</td><td>" +
                            diffTotals[prop][0].toFixed(2) + "</td><td>" + startTotals[prop][1].toFixed(2) + "</td><td>" +
                            endTotals[prop][1].toFixed(2) + "</td><td>" + diffTotals[prop][1].toFixed(2) + "</td></tr>";
                }
            }

            $("#" + tableId + "-table").append(tableString);
            // Setup the sortable cavity tables
            $("#" + tableId)
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
                    "</tr></thead>";
            advTableString += "<tbody>";
            basicTableString += "<thead><tr><th>Name</th><th>Module Type</th>" +
                    "<th>Old GSET</th><th>New GSET</th><th>Delta GSET</th>" +
                    "<th>Old ODHV</th><th>New ODHV</th><th>Delta ODHV</th>" +
                    "</tr></thead>";
            basicTableString += "<tbody>";
            if (data[0].cavities === null || data[1].cavities === null) {
                var msg = "Error processing data.";
                console.log("Error processing cavity service data. start:" + start + "  end: " + end);
                $("#" + advTableId + "-table").append(msg);
                $("#" + basicTableId + "-table").append(msg);
            }

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
                        if (cavEnd[i].moduleType === cavStart[j].moduleType) {
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
                        if (typeof cavStart[j].gset !== "undefined" && cavStart[j].gset !== "") {
                            oldGset = cavStart[j].gset;
                        } else {
                            oldGset = "N/A";
                        }
                        if (typeof cavEnd[i].gset !== "undefined" && cavEnd[j].gset !== "") {
                            newGset = cavEnd[i].gset;
                        } else {
                            newGset = "N/A";
                        }
                        if (newGset !== "N/A" && oldGset !== "N/A" && newGset !== null && oldGset !== null) {
                            dGset = (newGset - oldGset).toFixed(2);
                            newGset = newGset.toFixed(2);
                            oldGset = oldGset.toFixed(2);
                        } else {
                            dGset = "N/A";
                        }

                        // Process MAVs
                        if (typeof cavStart[j].modAnodeVoltage_kv !== "undefined" && cavStart[j].modAnodeVoltage_kv !== "") {
                            oldMav = cavStart[j].modAnodeVoltage_kv;
                        } else {
                            oldMav = "N/A";
                        }
                        if (typeof cavEnd[i].modAnodeVoltage_kv !== "undefined" && cavEnd[j].modAnodeVoltage_kv !== "") {
                            newMav = cavEnd[i].modAnodeVoltage_kv;
                        } else {
                            newMav = "N/A";
                        }
                        if (newMav !== "N/A" && oldMav !== "N/A" && newMav !== null && oldMav !== null) {
                            dMav = (newMav - oldMav).toFixed(2);
                        } else {
                            dMav = "N/A";
                            newMav = newMav.toFixed(2);
                            oldMav = oldMav.toFixed(2);
                        }

                        // Process Ops Drive Highs
                        if (typeof cavStart[j].odvh !== "undefined" && cavStart[j].odvh !== "") {
                            oldOdvh = cavStart[j].odvh;
                        } else {
                            oldOdvh = "N/A";
                        }
                        if (typeof cavEnd[i].odvh !== "undefined" && cavEnd[j].odvh !== "") {
                            newOdvh = cavEnd[i].odvh;
                        } else {
                            newOdvh = "N/A";
                        }
                        if (newOdvh !== "N/A" && oldOdvh !== "N/A" && newOdvh !== null && oldOdvh !== null) {
                            dOdvh = (newOdvh - oldOdvh).toFixed(2);
                            newOdvh = newOdvh.toFixed(2);
                            oldOdvh = oldOdvh.toFixed(2);
                        } else {
                            dOdvh = "N/A";
                        }

                        // Add a row to the table for this cavity
                        advTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                                oldMav + "</td><td>" + newMav + "</td><td>" + dMav + "</td><td>" +
                                oldGset + "</td><td>" + newGset + "</td><td>" + dGset + "</td><td>" +
                                oldOdvh + "</td><td>" + newOdvh + "</td><td>" + dOdvh + "</td></tr>";
                        basicTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                                oldGset + "</td><td>" + newGset + "</td><td>" + dGset + "</td><td>" +
                                oldOdvh + "</td><td>" + newOdvh + "</td><td>" + dOdvh + "</td>" +
                                "</tr>";
                    }
                }
                if (!foundMatch) {
                    console.log("No matching cavity found for: ", cavEnd[i]);

                    advTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                            oldMav + "</td><td>" + newMav + "</td><td>" + dMav + "</td><td>" +
                            oldGset + "</td><td>" + newGset + "</td><td>" + dGset + "</td><td>" +
                            oldOdvh + "</td><td>" + newOdvh + "</td><td>" + dOdvh + "</td></tr>";
                    basicTableString += "<tr><td>" + name + "</td><td>" + cmType + "</td><td>" +
                            oldGset + "</td><td>" + newGset + "</td><td>" + dGset + "</td>" +
                            oldOdvh + "</td><td>" + newOdvh + "</td><td>" + dOdvh + "</td>" +
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

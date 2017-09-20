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
jlab.cavity.cavityMapsToTableArray = function (startMap, endMap, linacs, cmtypes, properties) {

    // Check that the two maps contain identical cavity sets
    var match = jlab.util.compareMapKeySets(startMap, endMap);
    if (!match) {
        console.log("Error: start and end maps contain different cavities");
        return null;
    }

    // Setup the 2D cavity array with the header row
    var cavArray = new Array();
    var hArray = new Array();

    hArray.push("Name");
    if (jlab.util.arrayIncludes(properties, "cmtype")) {
        hArray.push("Module Type");
    }
    if (jlab.util.arrayIncludes(properties, "linac")) {
        hArray.push("Linac");
    }
    for (let i = 0; i < properties.length; i++) {
        let prop = properties[i];
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
    if (jlab.util.arrayIncludes(properties, "comments")) {
        hArray.push("Comments");
    }
    cavArray.push(hArray);

    // Now process the cavities and create the data rows of the cavArray
    var startCav, endCav;
    var j = 0;
    startMap.forEach(function (value, key) {
        let name = key;

        startCav = startMap.get(name);
        endCav = endMap.get(name);

        // Filter out the unwanted cavities
        if (!jlab.util.arrayIncludes(linacs, startCav.linac.toLowerCase())) {
            console.log(linacs, startCav.linac);
            return;
            //continue;
        }
        if (!jlab.util.arrayIncludes(cmtypes, startCav.moduleType.toUpperCase())) {
            console.log(cmtypes, startCav.moduleType);
            return;
            //continue;
        }

        rowArray = new Array();
        rowArray.push(startCav.name);
        if (jlab.util.arrayIncludes(properties, "cmtype")) {
            var cmtype = startCav.moduleType;
            if (startCav.moduleType !== endCav.moduleType) {
                cmtype += "/" + endCav.moduleType;
            }
            rowArray.push(cmtype);
        }
        if (jlab.util.arrayIncludes(properties, "linac")) {
            rowArray.push(startCav.linac);
        }
        for (let i = 0; i < properties.length; i++) {
            let prop = properties[i];
            switch (prop) {
                case "cmtype":
                    break; //handled explicitly
                case "linac":
                    break; //handled explicitly
                case "comments":
                    break; //handled explicitly
                case "length":
                    var length = startCav.length.toFixed(2);
                    if (startCav.length !== endCav.length) {
                        length += "/" + endCav.length.toFixed(2);
                    }
                    rowArray.push(length);
                    break;
                case "modAnode":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "modAnodeVoltage_kv"));
                    break;
                case "gset":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "gset"));
                    break;
                case "odvh":
                    rowArray = rowArray.concat(jlab.cavity.processNumericTableEntry(startCav, endCav, "odvh"));
                    break;
                case "opsGsetMax":
                    var temp = jlab.cavity.processNumericTableEntry(startCav, endCav, "opsGsetMax");
                    temp[0] += "<span class='ui-icon ui-icon-comment comment-dialog' data-jlab-cavity='" + startCav.name +
                            "' data-jlab-cav-property='OpsGSETMax'></span>";
                    rowArray = rowArray.concat(temp);
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
        if (jlab.util.arrayIncludes(properties, "comments")) {
            rowArray.push("");
        }
        cavArray.push(rowArray);
    });

    return cavArray;
};


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
};

/* 
 * Create a cavity data table.
 * tableId - the tablesorter tag tableId
 * date - the date for which to query data
 * */
//jlab.cavity.createTable = function (tableId, date) {
//
//    jlab.cavity.getCavityData({
//        start: date,
//        end: jlab.addDays(date, 1),
//        timeUnit: "day",
//        success: function (jsonData, textStatus, jqXHR) {
//            console.log(jsonData);
//            var data = jsonData.data;
//            var tableString = "<table class=\"tablesorter\">";
//            tableString += "<thead><tr><th>Name</th><th>Module Type</th><th>Mod Anode Voltage (kV)</th><th>GSET</th></tr></thead>";
//            tableString += "<tbody>";
//            for (var i = 0; i < data.length; i++) {
//                var cavities = data[i].cavities;
//                for (var j = 0; j < cavities.length; j++) {
//                    tableString += "<tr><td>" + cavities[j].name + "</td><td>" + cavities[j].moduleType + "</td><td>" +
//                            cavities[j].modAnodeVoltage_kv + "</td><td>" + cavities[j].gset + "</tr>";
//                }
//            }
//            tableString += "</tbody></table>";
//            $("#" + tableId + "-table").append(tableString);
//            // Setup the sortable cavity table
//            $(".tablesorter")
//                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
//                    .tablesorterPager({container: $("#" + tableId + "-pager")});
//        }
//    });
//};

///* This function relies on the tablesorter tag,  jquery.tablesorter (JS/CSS), and jquery.tablesorter.pager (JS/CSS).  It creates
// * a sortable table of totals, and their differences between start and end.
// */
//jlab.cavity.createTotalsTable = function (tableId, start, end) {
//    jlab.cavity.getCavityData({
//        asRange: false,
//        dates: [start, end],
//        success: function (jsonData, textStatus, jqXHR) {
//            var data = jsonData.data;
//            var tableString = "<table id=\"" + tableId + "\" class=\"tablesorter\">";
//            tableString += "<thead><tr><th>Subset</th>" +
//                    "<th>Old GSET</th><th>New GSET</th><th>Delta GSET</th>" +
//                    "<th>Old ODHV</th><th>New ODHV</th><th>Delta ODHV</th>" +
//                    "</tr></thead>";
//            tableString += "<tbody>";
//            if (data[0].cavities === null || data[1].cavities === null) {
//                var msg = "Error processing data.";
//                console.log("Error processing cavity service data. start:" + start + "  end: " + end);
//                $("#" + tableId + "-table").append(msg);
//            }
//            var cavStart = data[0].cavities;
//            var cavEnd = data[1].cavities;
//
//            // The array counts are [GSET, ODVH]
//            var startTotals = {Total: [0, 0], QTR: [0, 0], C25: [0, 0], C50: [0, 0], C100: [0, 0]};
//            var endTotals = {Total: [0, 0], QTR: [0, 0], C25: [0, 0], C50: [0, 0], C100: [0, 0]};
//            var diffTotals = {Total: [0, 0], QTR: [0, 0], C25: [0, 0], C50: [0, 0], C100: [0, 0]};
//
//            // Tally up totals for the start period, then the end period, then calculate the difference in the totals
//            for (var i = 0; i < cavStart.length; i++) {
//                if (cavStart[i].gset !== "") {
//                    startTotals.Total[0] += cavStart[i].gset;
//                    startTotals[cavStart[i].moduleType][0] += cavStart[i].gset;
//                }
//                if (cavStart[i].gset !== "") {
//                    startTotals.Total[1] += cavStart[i].odvh;
//                    startTotals[cavStart[i].moduleType][1] += cavStart[i].odvh;
//                }
//            }
//            for (var i = 0; i < cavEnd.length; i++) {
//                if (cavEnd[i].gset !== "") {
//                    endTotals.Total[0] += cavEnd[i].gset;
//                    endTotals[cavEnd[i].moduleType][0] += cavEnd[i].gset;
//                }
//                if (cavEnd[i].odvh !== "") {
//                    endTotals.Total[1] += cavEnd[i].odvh;
//                    endTotals[cavEnd[i].moduleType][1] += cavEnd[i].odvh;
//                }
//            }
//            // All three of the totals objects should have the same properties
//            for (var prop in startTotals) {
//                if (startTotals.hasOwnProperty(prop)) {
//                    for (var i = 0; i < diffTotals[prop].length; i++) {
//                        diffTotals[prop][i] = endTotals[prop][i] - startTotals[prop][i];
//                    }
//                }
//            }
//
//            for (var prop in startTotals) {
//                if (startTotals.hasOwnProperty(prop)) {
//                    tableString += "<tr><td>" + prop + "</td><td>" +
//                            startTotals[prop][0].toFixed(2) + "</td><td>" + endTotals[prop][0].toFixed(2) + "</td><td>" +
//                            diffTotals[prop][0].toFixed(2) + "</td><td>" + startTotals[prop][1].toFixed(2) + "</td><td>" +
//                            endTotals[prop][1].toFixed(2) + "</td><td>" + diffTotals[prop][1].toFixed(2) + "</td></tr>";
//                }
//            }
//
//            $("#" + tableId + "-table").append(tableString);
//            // Setup the sortable cavity tables
//            $("#" + tableId)
//                    .tablesorter({sortList: [[0, 0]]}) // sort on the first column (asc)
//                    .tablesorterPager({container: $("#" + tableId + "-pager")});
//        }
//    });
//};


/* This function relies on the tablesorter tag,  jquery.tablesorter (JS/CSS), and jquery.tablesorter.pager (JS/CSS).  It creates
 * a sortable table of totals, and their differences between start and end.
 */
jlab.cavity.getTotalsByCMType = function (startMap, endMap) {

    var match = jlab.util.compareMapKeySets(startMap, endMap);
    if (!match) {
        console.log("Error: start and end maps contain different cavities");
        return null;
    }

    var outArray = new Array();
    outArray.push(["Subset",
        "Old GSET Count", "Old GSET Total", "Old GSET Mean", "New GSET Count", "New GSET Total", "New GSET Mean", "Delta GSET Total",
        "Old ODVH Count", "Old ODVH Total", "Old ODVH Mean", "New ODVH Count", "New ODVH Total", "New ODVH  Mean", "Delta ODVH Total"
    ]);

    // Setup objects to hold the tallies for all of this
    var startTotals = {
        Total: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        QTR: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C25: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C50: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0}
    };
    var endTotals = {
        Total: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        QTR: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C25: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C50: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0}
    };
    var diffTotals = {
        Total: {gset: 0, odvh: 0},
        QTR: {gset: 0, odvh: 0},
        C25: {gset: 0, odvh: 0},
        C50: {gset: 0, odvh: 0},
        C100: {gset: 0, odvh: 0}
    };

    startMap.forEach(function (value, key) {
        let name = key;
        let startCav = startMap.get(name);
        let endCav = endMap.get(name);

        if (startCav.gset !== "") {
            startTotals.Total.gset += startCav.gset;
            startTotals.Total.nGset++;
            startTotals[startCav.moduleType].gset += startCav.gset;
            startTotals[startCav.moduleType].nGset++;
        }
        if (startCav.odvh !== "") {
            startTotals.Total.odvh += startCav.odvh;
            startTotals.Total.nOdvh++;
            startTotals[startCav.moduleType].odvh += startCav.odvh;
            startTotals[startCav.moduleType].nOdvh++;
        }
        if (endCav.gset !== "") {
            endTotals.Total.gset += endCav.gset;
            endTotals.Total.nGset++;
            endTotals[endCav.moduleType].gset += endCav.gset;
            endTotals[endCav.moduleType].nGset++;
        }
        if (endCav.odvh !== "") {
            endTotals.Total.odvh += endCav.odvh;
            endTotals.Total.nOdvh++;
            endTotals[endCav.moduleType].odvh += endCav.odvh;
            endTotals[endCav.moduleType].nOdvh++;
        }
    });


    // The diffTotals will have a subset of the properties since the counts don't make sense for the diffs
    for (let total in startTotals) {
        if (diffTotals.hasOwnProperty(total)) {
            for (let prop in diffTotals[total]) {
                diffTotals[total][prop] = endTotals[total][prop] - startTotals[total][prop];
            }
        }
    }

    // total (cmtype) and each represents a single row of the table
    for (let total in startTotals) {
        if (startTotals.hasOwnProperty(total)) {
            let sGMean = (startTotals[total].gset / startTotals[total].nGset).toFixed(2) || "N/A";
            let eGMean = (endTotals[total].gset / endTotals[total].nGset).toFixed(2) || "N/A";
            let sOMean = (startTotals[total].odvh / startTotals[total].nOdvh).toFixed(2) || "N/A";
            let eOMean = (endTotals[total].odvh / endTotals[total].nOdvh).toFixed(2) || "N/A";
            outArray.push([
                total, // The label
                startTotals[total].nGset, startTotals[total].gset.toFixed(2), sGMean, endTotals[total].nGset, endTotals[total].gset.toFixed(2), eGMean, diffTotals[total].gset.toFixed(2),
                startTotals[total].nOdvh, startTotals[total].odvh.toFixed(2), sOMean, endTotals[total].nOdvh, endTotals[total].odvh.toFixed(2), eOMean, diffTotals[total].odvh.toFixed(2)
            ]);

        }
    }

    return outArray;
};


jlab.cavity.createCavitySetPointTables = function (basicId, advId, summaryId, start, end) {
    var linacs = ["north", "south", "injector"];
    var cmtypes = ["C25", "C50", "C100", "QTR"];
    var basicProps = ["cmtype", "gset", "odvh"];
    var advProps = ["cmtype", "modAnode", "gset", "odvh"];
    jlab.util.showTableLoading(basicId);
    jlab.util.showTableLoading(summaryId);
    var cavityData = $.ajax({
        traditional: true,
        url: jlab.util.cavityAjaxUrl,
        data: {
            date: [start, end],
            out: "json"
        },
        dataType: "json"
    });
    cavityData.done(function (json) {
        jlab.util.hideTableLoading(basicId);
        jlab.util.hideTableLoading(summaryId);
        var startMap, endMap;
        var maps = jlab.cavity.getStartEndMaps(json, start, end);
        if (maps === null) {
            jlab.util.hideTableLoading(basicId, "Error querying data");
            jlab.util.hideTableLoading(summaryId, "Error querying data");
            console.log("Error: received unexpected AJAX cavity service repsonse", json);
            return;
        } else {
            startMap = maps[0];
            endMap = maps[1];
        }

        var summaryArray = jlab.cavity.getTotalsByCMType(startMap, endMap);
        jlab.util.createTableSorterTable(summaryId, {data: summaryArray});

        var basicTableArray = jlab.cavity.cavityMapsToTableArray(startMap, endMap, linacs, cmtypes, basicProps);
        var advTableArray = jlab.cavity.cavityMapsToTableArray(startMap, endMap, linacs, cmtypes, advProps);

        jlab.util.createTableSorterTable(basicId, {data: basicTableArray});

        // The tablesorter plugin has issues initializing style to elements with display: none.  IDK... this happens fast enough that
        // users won't notice.
        $(advId).show();
        jlab.util.createTableSorterTable(advId, {data: advTableArray});
        $(advId).hide();

    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.util.hideTableLoading(basicId, "Error querying data");
        jlab.util.hideTableLoading(advId, "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
    });
};
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

/**
 * Creates a map of cavity objects keyed on cavity name.  If commentData is supplied, the most recent comment for each
 * cavity is attached to appropriate cavity object
 * @param {type} cavityData The json object returned by the ajax/cavity service for a single date
 * @param {type} commentData The json object returned by the ajax/comments service categorized by topic (by=topic)
 * @returns {Map|jlab.cavity.createCavityMap.map}
 */
jlab.cavity.createCavityMap = function (cavityData, commentData) {
    var map = new Map();
    var cavities = cavityData.cavities;
    var cavity;

    for (var i = 0; i < cavities.length; i++) {
        cavity = cavities[i];
        if (typeof commentData !== "undefined") {
            if (commentData.hasOwnProperty(cavity.name)) {
                cavity.comment = commentData[cavity.name][0];
            }
        }
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
 * @param {type} commentData The comment data to be added to the end cavity map.
 * @returns {Array}
 */
jlab.cavity.getStartEndMaps = function (cavityJson, start, end, commentData) {
    var cavityMaps = [];
    if (cavityJson.data[0].date === start && cavityJson.data[1].date === end) {
        cavityMaps[0] = jlab.cavity.createCavityMap(cavityJson.data[0]); // start
        cavityMaps[1] = jlab.cavity.createCavityMap(cavityJson.data[1], commentData); // end
    } else if (cavityJson.data[1].date === start && cavityJson.data[0].date === end) {
        cavityMaps[0] = jlab.cavity.createCavityMap(cavityJson.data[1], commentData); // end
        cavityMaps[1] = jlab.cavity.createCavityMap(cavityJson.data[0]); //start
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
            case "tunerBad":
                hArray.push("Old Tuner Bad");
                hArray.push("New Tuner Bad");
                break;
            case "bypassed":
                hArray.push("Old Bypassed");
                hArray.push("New Bypassed");
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
            return;
        }
        if (!jlab.util.arrayIncludes(cmtypes, startCav.moduleType.toUpperCase())) {
            return;
        }

        rowArray = new Array();
        rowArray.push("<div class=nobr>" + startCav.name + "<a class=cell-link href='https://ced.acc.jlab.org/elem/" + name + "' target='_blank'><span class='ui-icon ui-icon-extlink'></span></a><div>");
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
                case "tunerBad":
                    rowArray.push(startCav.tunerBad + "<span class='ui-icon ui-icon-comment comment-dialog' data-jlab-cavity='"
                            + startCav.name + "' data-jlab-cav-property='tunerBad'></span>");
                    rowArray.push(endCav.tunerBad);
                    break;
                case "bypassed":
                    rowArray.push(startCav.bypassed + "<span class='ui-icon ui-icon-comment comment-dialog' data-jlab-cavity='"
                            + startCav.name + "' data-jlab-cav-property='bypassed'></span>");
                    rowArray.push(endCav.bypassed);
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
            cavComment = jlab.cavity.formatCavityComment(endCav);
            rowArray.push(cavComment);
        }
        cavArray.push(rowArray);
    });

    return cavArray;
};

jlab.cavity.formatCavityComment = function (cavity) {
    var commentString;

    var commentIcon = "<a class=cell-link href='" + jlab.util.newCommentUrl + "?topic=" + cavity.name.encodeXml() + "' ><span title='Add Comment' class='ui-icon ui-icon-extlink cavity-comment-icon'></span></a>";

    if (cavity.hasOwnProperty("comment")) {
        commentString = "<div class='nobr cavity-comment-header'>" + cavity.comment.timestamp + "  --  " + cavity.comment.username
                + commentIcon + "</div><div class=pre-wrap>" + cavity.comment.content.encodeXml() + "</div>";
    } else {
        commentString = "<div class='nobr cavity-comment-header'>" + commentIcon + "</div>";
    }
    return commentString;
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
        C100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C50T: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C75: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        F100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        Booster: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0}
    };
    var endTotals = {
        Total: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        QTR: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C25: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C50: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C50T: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C75: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        F100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        Booster: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0}
    };
    var diffTotals = {
        Total: {gset: 0, odvh: 0},
        QTR: {gset: 0, odvh: 0},
        C25: {gset: 0, odvh: 0},
        C50: {gset: 0, odvh: 0},
        C100: {gset: 0, odvh: 0},
        C50T: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        C75: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        F100: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0},
        Booster: {gset: 0, odvh: 0, nGset: 0, nOdvh: 0}
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


jlab.cavity.createCavitySetPointTables = function (basicId, advId, summaryId, cavityData, start, end) {
    var linacs = ["north", "south", "injector"];
    var cmtypes = ["C25", "C50", "C100", "QTR"];
    var basicProps = ["cmtype", "gset", "odvh"];
    var advProps = ["cmtype", "modAnode", "gset", "odvh"];

    jlab.util.hideTableLoading(basicId);
    jlab.util.hideTableLoading(summaryId);
    var startMap, endMap;
    var maps = jlab.cavity.getStartEndMaps(cavityData, start, end);
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

};
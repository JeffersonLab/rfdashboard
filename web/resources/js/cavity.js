/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.cavity = jlab.cavity || {};
jlab.cavity.ajaxUrl = "/RFDashboard/ajax/cavity";

jlab.cavity = jlab.cavity || {};

jlab.cavity.loadDetailedTable = function (widgetId, start, end, linacs, cmtypes, properties) {


    jlab.util.showTableLoading(widgetId);
    var cavityData = $.ajax({
        traditional: true,
        url: jlab.cavity.ajaxUrl,
        data: {
            date: [start, end],
            out: "json"
        },
        dataType: "json"
    });

    // Query the cavity data.  If we get that data, then try to get comments data.
    cavityData.done(function (jsonCavity) {

        var comments = $.ajax({
            traditional: true,
            url: jlab.util.commentsAjaxUrl,
            data: {
                "end": end,
                "by": "topic"
            },
            dataType: "json"
        });

        comments.always(function (jsonComments) {
            jlab.util.hideTableLoading(widgetId);
            var startMap, endMap;
            var maps = jlab.cavity.getStartEndMaps(jsonCavity, start, end, jsonComments);
            if (maps === null) {
                jlab.util.hideTableLoading(widgetId, "Error querying data");
                console.log("Error: received unexpected AJAX cavity service repsonse", jsonCavity);
                return;
            } else {
                startMap = maps[0];
                endMap = maps[1];
            }

            var tableArray = jlab.cavity.cavityMapsToTableArray(startMap, endMap, linacs, cmtypes, properties);
            jlab.util.createTableSorterTable(widgetId, {data: tableArray});
        });

        comments.fail(function (jqXHR, textStatus, errorThrown) {
            console.log("Error querying comments.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
        });

    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.util.hideTableLoading(widgetId, "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
    });

};

$(function () {

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });

    jlab.cavity.loadDetailedTable("#details-table", jlab.start, jlab.end, jlab.linacs, jlab.cmtypes, jlab.properties);
});
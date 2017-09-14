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

    cavityData.done(function (json) {

        jlab.util.hideTableLoading(widgetId);
        var startMap, endMap;
        if (json.data[0].date === start && json.data[1].date === end) {
            startMap = jlab.cavity.createCavityMap(json.data[0]);
            endMap = jlab.cavity.createCavityMap(json.data[1]);
        } else if (json.data[1].date === start && json.data[0].date === end) {
            startMap = jlab.cavity.createCavityMap(json.data[1]);
            endMap = jlab.cavity.createCavityMap(json.data[0]);
        } else {
            jlab.util.hideTableLoading(widgetId, "Error querying data");
            console.log("Error: received unexpected AJAX cavity service repsonse", json);
            return;
        }

        var tableArray = jlab.cavity.cavityMapsTo2DArray(startMap, endMap, linacs, cmtypes, properties);
        jlab.util.createTableSorterTable(widgetId, {data: tableArray});

    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.util.hideTableLoading(widgetId, "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
        ;
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
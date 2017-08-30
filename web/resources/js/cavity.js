/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};

jlab.cavity = jlab.cavity || {};
jlab.cavity.ajaxUrl = "/RFDashboard/ajax/cavity";

jlab.cavity = jlab.cavity || {};

jlab.cavity.loadDetailedTable = function (tableId, start, end, linacs, cmtypes, properties) {

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

        var startMap, endMap;
        if (json.data[0].date === start && json.data[1].date === end) {
            startMap = jlab.cavity.createCavityMap(json.data[0]);
            endMap = jlab.cavity.createCavityMap(json.data[1]);
        } else if (json.data[1].date === start && json.data[0].date === end) {
            startMap = jlab.cavity.createCavityMap(json.data[1]);
            endMap = jlab.cavity.createCavityMap(json.data[0]);
        } else {
            jlab.hideChartLoading(tableId, "Error querying data");
            console.log("Error: received unexpected AJAX cavity service repsonse", json);
            return;
        }

        var tableArray = jlab.cavity.cavityMapsTo2DArray(startMap, endMap, linacs, cmtypes, properties);
        jlab.util.createTableSorterTable("summary-table", {data: tableArray});


    }).fail(function (jqXHR, textStatus, errorThrown) {
        jlab.hideChartLoading(tableId, "Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
        ;
    });

};

$(function () {
    // The "diff-table-*" names need to be be manually kept in sync with the IDs given in the JSP.  This shouldn't need to
    // often, but if it does, we should put them in variables, etc.
    // jlab.cavity.createBasicAdvTable("diff-table-basic", "diff-table-advanced", jlab.diffStart, jlab.diffEnd);
    // jlab.cavity.createTotalsTable("summary-table", jlab.diffStart, jlab.diffEnd);
//    jlab.cavity.loadCharts(jlab.lemScanUrl, jlab.start, jlab.end, jlab.diffEnd, jlab.timeUnit);

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd"
    });

    // This enables the "Basic/Advanced" menu button to toggle between the two tables.  diff-table-advanced starts out with
    // display: none.
//    $("#menu-toggle").click(function() {
//        $('#diff-table-basic-wrap').toggle();
//        $('#diff-table-advanced-wrap').toggle();
//    });

    jlab.cavity.loadDetailedTable("summary-table", jlab.start, jlab.end, jlab.linacs, jlab.cmtypes, jlab.properties);

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });
});
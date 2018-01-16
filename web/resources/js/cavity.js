var jlab = jlab || {};

//jlab.cavity = jlab.cavity || {};

//jlab.cavity.loadDetailedTable = function (widgetId, cavityData, start, end, linacs, cmtypes, properties) {
//
//};

$(function () {

    $("#page-details-dialog").dialog(jlab.dialogProperties);
    $("#page-details-opener").click(function () {
        $("#page-details-dialog").dialog("open");
    });

    $(".date-field").datepicker({
        dateFormat: "yy-mm-dd",
        showButtonPanel: true
    });

//    jlab.cavity.loadDetailedTable("#details-table", jlab.cavityData, jlab.start, jlab.end, jlab.linacs, jlab.cmtypes, jlab.properties);

    var startMap, endMap;
    var maps = jlab.cavity.getStartEndMaps(jlab.cavityData, jlab.start, jlab.end);
    startMap = maps[0];
    endMap = maps[1];

    var tableArray = jlab.cavity.cavityMapsToTableArray(startMap, endMap, jlab.linacs, jlab.cmtypes, jlab.properties);
    jlab.util.createTableSorterTable("#details-table", {data: tableArray});
    jlab.util.initCalendarStartEnd("#main-calendar");
});
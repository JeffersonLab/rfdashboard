/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.tableSorter = jlab.tableSorter || {};

jlab.tableSorter.initTable = function (widgetId) {

    $table = $(widgetId + " table");
    var pagerOptions = {
        // target the pager markup - see the HTML block below
        container: $(widgetId + " .pager"),
        // output string - default is '{page}/{totalPages}';
        // possible variables: {size}, {page}, {totalPages}, {filteredPages}, {startRow}, {endRow}, {filteredRows} and {totalRows}
        // also {page:input} & {startRow:input} will add a modifiable input in place of the value
        output: '{startRow} - {endRow} / {filteredRows} ({totalRows})',
        // if true, the table will remain the same height no matter how many records are displayed. The space is made up by an empty
        // table row set to a height to compensate; default is false
        fixedHeight: false,
        // remove rows from the table to speed up the sort of large tables.
        // setting this to false, only hides the non-visible rows; needed if you plan to add/remove rows with the pager enabled.
        removeRows: false,
        // go to page selector - select dropdown that sets the current page
        cssGoto: '.gotoPage'
    };

    $table.tablesorter({
        headerTemplate: '{content} {icon}', // new in v2.7. Needed to add the bootstrap icon!
        theme: "default",
        sortList: [[1,0]],
        widthFixed: false,
        sortReset: true,
        showProcessing: true,
        widgets: ['zebra', 'stickyHeaders', 'output', 'filter'],
        headers: {0: {sorter: 'checkbox', filter: false}},
        widgetOptions: {
            output_includeHeader: true,
            output_ignoreColumns: [0],
            stickyHeaders_addResizeEvent: true,
            stickyHeaders_attachTo: widgetId + " .table-panel"
        }
    });
    $table.tablesorterPager(pagerOptions);

    jlab.tableSorter.initOutputWidget(widgetId);
    jlab.tableSorter.initHelpDialog(widgetId);
    jlab.tableSorter.initCommentDialogs(widgetId);

    $(widgetId).tooltip();
};

jlab.tableSorter.initCommentDialogs = function (widgetId) {
    $(widgetId + " span.comment-dialog").each(function () {

        var name = $(this).data("jlab-cavity");
        var prop = $(this).data("jlab-cav-property");
        var dialogId = name + "-" + prop + "-dialog";
        var dialogProperties = jlab.dialogProperties;
        dialogProperties.height = 400;
        dialogProperties.maxHeight = 1000;

        $(this).click(function () {

            if ($("#" + dialogId).length === 0) {
                if (prop === "comments") {
                    $("body").append("<div id='" + dialogId + "' class='dialog update-history-dialog' title='" + name + " CED Comments'</div>");
                    var promise = $.getJSON(jlab.util.commentsAjaxUrl, {"topic": name});
                    promise.done(function (json) {
                        var tableArray = jlab.tableSorter.rfdCommentsToArray(json);
                        $("#" + dialogId).append(jlab.util.createHTMLTable(tableArray));
                    }).fail(function (jqXHR, textStatus, errorThrown) {
                        $("#" + dialogId).append("Error querying data");
                        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
                    });
                    $("#" + dialogId).dialog(dialogProperties);
                } else {
                    $("body").append("<div id='" + dialogId + "' class='dialog update-history-dialog' title='" + name + " " + prop + " Update History'</div>");
                    var promise = $.getJSON(jlab.util.cedUpdateHistoryAjaxUrl, {"elem": name, "prop": prop});
                    promise.done(function (json) {
                        var tableArray = jlab.tableSorter.updateHistoryToArray(json);
                        $("#" + dialogId).append(jlab.util.createHTMLTable(tableArray));
                    }).fail(function (jqXHR, textStatus, errorThrown) {
                        $("#" + dialogId).append("Error querying data");
                        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
                        console.log("responseText: " + jqXHR.responseText);
                    });
                    $("#" + dialogId).dialog(dialogProperties);
                }
            }
            $("#" + dialogId).dialog("open");
        });
    });
};

/*
 * Turn a JSON response from ajax/ced-update-history into an array, one update per-line.  The element's comment field should
 * be handled slightly differently.  Use 'isComments' to control this behavior.
 * @param {type} history The JSON response.  Assumes that the response has only one property
 * @returns {undefined}
 */
jlab.tableSorter.updateHistoryToArray = function (history) {
    var array = new Array();
    $.each(history.updates, function (index, value) {
        var update = value;
        var date = update.date;
        var updateValue, username, comment, property;
        $.each(update.properties, function (key, value) {
            property = key;
            updateValue = value.value;
            username = value.username;
            comment = value.comment;
        });
        array.push([date, property, updateValue, username, "<div class=pre-wrap>" + comment + "</div>"]);
    });
    array.push(["Date", "Property", "Value", "User", "Comment"]);

    // The history is originally sorted with oldest first.  Other way.
    return array.reverse();
};

/*
 * Function to process the results of a query to the RFD comment service from JSON object to a 2D JSON array.
 * @param {type} comments
 * @returns {Array|jlab.tableSorter.rfdCommentsToArray.array}
 */
jlab.tableSorter.rfdCommentsToArray = function (comments) {
    var array = new Array();
    array.push(["Timestamp", "User", "Comment"]);
    $.each(comments.data, function (index, value) {
        array.push([value.timestamp, value.username, value.content.encodeXml()]);
    });
    return array;
};

jlab.tableSorter.initOutputWidget = function (outputId) {
    var $this = $(outputId);

    $this.find('.dropdown-toggle').click(function (e) {
        // this is needed because clicking inside the dropdown will close
        // the menu with only bootstrap controlling it.
        $this.find('.dropdown-menu').toggle();
        return false;
    });
    // make separator & replace quotes buttons update the value
    $this.find('.output-separator').click(function () {
        $this.find('.output-separator').removeClass('active');
        var txt = $(this).addClass('active').html();
        $this.find('.output-separator-input').val(txt);
        $this.find('.output-filename').val(function (i, v) {
            // change filename extension based on separator
            var filetype = (txt === 'json' || txt === 'array') ? 'js' :
                    txt === ',' ? 'csv' : 'txt';
            return v.replace(/\.\w+$/, '.' + filetype);
        });
        return false;
    });
    $this.find('.output-quotes').click(function () {
        $this.find('.output-quotes').removeClass('active');
        $this.find('.output-replacequotes').val($(this).addClass('active').text());
        return false;
    });
    // clicking the download button; all you really need is to
    // trigger an "output" event on the table
    $this.find('.download').click(function () {
        var typ,
                $table = $this.find('table'),
                wo = $table[0].config.widgetOptions,
                val = $this.find('.output-filter-all :checked').attr('class');
        wo.output_saveRows = val === 'output-filter' ? 'f' :
                val === 'output-visible' ? 'v' :
                // checked class name, see table.config.checkboxClass
                val === 'output-selected' ? '.checked' :
                val === 'output-sel-vis' ? '.checked:visible' :
                'a';
        val = $this.find('.output-download-popup :checked').attr('class');
        wo.output_delivery = val === 'output-download' ? 'd' : 'p';
        wo.output_separator = $this.find('.output-separator-input').val();
        wo.output_replaceQuote = $this.find('.output-replacequotes').val();
        wo.output_trimSpaces = $this.find('.output-trim').is(':checked');
        wo.output_wrapQuotes = $this.find('.output-wrap').is(':checked');
        wo.output_saveFileName = $this.find('.output-filename').val();

        $table.trigger('outputTable');
        return false;
    });

};

/*
 * This causes a dialog containing help information about basic tablesorter functionality to be displayed on 'element' click events.
 */
jlab.tableSorter.initHelpDialog = function (widgetId) {

    var dialogProperties = jlab.dialogProperties;
    dialogProperties.width = 1000;
    $(widgetId + "-help-dialog").dialog(dialogProperties);
    $(widgetId).find(".help-launcher").click(function () {
        $(widgetId + "-help-dialog").dialog("open");
    });
};
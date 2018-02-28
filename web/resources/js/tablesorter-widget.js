/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var jlab = jlab || {};
jlab.tableSorter = jlab.tableSorter || {};

jlab.tableSorter.dialogTracker = jlab.tableSorter.dialogTracker || {};

jlab.tableSorter.initTable = function (widgetId) {

    $table = $(widgetId + " table");
    var numRows = $(widgetId + " table tbody tr").length;
    if (numRows < 1) {
        var title = $(widgetId + " .table-title").contents();
        $(widgetId).prepend("<div><br><br><center>No Data Available</center><br></div>");
        $(widgetId).prepend(title);
        $(widgetId + " .ui-icon").css("display", "inline-block");
        $(widgetId).css("height", 200);
        $(widgetId + ' .table-wrap').hide();
    }

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
        sortList: [[1, 0]],
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

// widgetId  - jQuery selector for narrowing the affected elements
jlab.tableSorter.initCommentDialogs = function (widgetId) {
    $(widgetId + " span.comment-dialog").each(function () {

        var name = $(this).data("jlab-cavity");
        var prop = $(this).data("jlab-cav-property");
        var dialogId = name + "-" + prop + "-dialog";
        var dialogProperties = jlab.dialogProperties;
        dialogProperties.height = 400;
        dialogProperties.maxHeight = 1000;

        $(this).click(function () {

            if (!jlab.tableSorter.dialogTracker.hasOwnProperty(dialogId)) {
                // The comments dialog uses a different datasource, has a JSP-staged div, and provides "write" functionality
                if (prop === "comments") {
                    // Bind the filter functionality to the comment filter button
                    $("#" + dialogId).on("click", ".comment-filter-button", function () {
                        jlab.tableSorter.filterComments(dialogId, name);
                    });

                    // Add the latest comment table to the .history-panel
                    jlab.tableSorter.refreshRFDCommentHistory(dialogId, name);

                    // Launch the dialog window
                    $("#" + dialogId).dialog(dialogProperties);
                } else {
                    // All other property "comment" (i.e., update histories) dialogs use the CED datasource, do not have a JSP-staged div, 
                    // and do not provide "write" functionality

                    // Update the tracker
                    jlab.tableSorter.dialogTracker[dialogId] = true;

                    // Create a div for the dialog
                    $("body").append("<div id='" + dialogId + "' class='dialog update-history-dialog' title='" + name + " " + prop + " Update History'</div>");

                    // Get a fresh copy of the update history data
                    var promise = $.getJSON(jlab.util.cedUpdateHistoryAjaxUrl, {"elem": name, "prop": prop});

                    // If successful, add a table with the data
                    promise.done(function (json) {
                        var tableArray = jlab.tableSorter.updateHistoryToArray(json);
                        console.log(tableArray);
                        if (tableArray.length === 1) {
                            $("#" + dialogId).append("No Updates Found");
                        } else {
                            $("#" + dialogId).append(jlab.util.createHTMLTable(tableArray));
                            $("#" + dialogId + " " + "table").addClass("comments-table");
                        }
                    });

                    // If it fails, add an error message
                    promise.fail(function (jqXHR, textStatus, errorThrown) {
                        $("#" + dialogId).append("Error querying data");
                        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
                        console.log("responseText: " + jqXHR.responseText);
                    });

                    // Launch the dialog window
                    $("#" + dialogId).dialog(dialogProperties);
                }
            }

            $("#" + dialogId).dialog("open");
        });
    });
};

/**
 * This method is used to refresh the RFD Comment History in the RFD Comment dialogs.  Can be used to "initialize" as well
 * since this drops all of the content in the $("#" + dialogId + " .history-panel") div, then adds the content.
 * @param {type} dialogId The ID of the dialog div
 * @param {type} name The name of the cavity/topic that is being commented upon
 * @returns {undefined} Nothing
 */
jlab.tableSorter.refreshRFDCommentHistory = function (dialogId, topic) {

    // Don't update the tracker.  Instead drop the comment table and recreate it
    $("#" + dialogId + " .history-panel").html("");

    // Get a fresh copy of the comment history
    var promise = $.getJSON(jlab.util.commentsAjaxUrl, {"topic": topic});

    // If successful, add a table with comments
    promise.done(function (json) {
        var tableArray = jlab.tableSorter.rfdCommentsToArray(json);
        $("#" + dialogId + " .history-panel").append(jlab.util.createHTMLTable(tableArray));
        $("#" + dialogId + " .history-panel table").addClass("comments-table");
    })
            ;
    // If it fails, add an error mesage
    promise.fail(function (jqXHR, textStatus, errorThrown) {
        $("#" + dialogId + " .history-panel").append("Error querying data");
        console.log("Error querying data.\n  textStatus: " + textStatus + "\n  errorThrown: " + errorThrown);
    });

    // The comment dialog also includes a form for adding a new comment.  Add the listener that submits the comments
    $("#" + dialogId).on("click", "#" + topic + "-comment-button", function () {
        jlab.tableSorter.submitComment(topic + "-comments-dialog");
    });

};

// This function is used to submit comments from the RFD Comments dialog window launched from tablesorter table.
jlab.tableSorter.submitComment = function (dialogId) {

    // This prevents impatient "clicky" people from submitting the same comment multiple times.
    if (jlab.isRequest()) {
        window.console && console.log("Ajax in progress for " + dialogId + " comment");
        return;
    }
    jlab.requestStart();

    var topic = $("#" + dialogId + " input").val();
    var comment = $("#" + dialogId + " textarea").val();

    var status = $.ajax({
        url: jlab.util.commentsAjaxUrl,
        type: "POST",
        data: {
            topic: topic,
            comment: comment
        },
        dataType: "json"
    });

    // If it works, throw an alert about success and refresh the comment history to show the recent addition
    status.done(function (data, textStatus, jqXHR) {
        alert("Comment submitted");
        jlab.tableSorter.refreshRFDCommentHistory(dialogId, topic);
    });

    // If it fails, throw an alert, put some error details on the console, but don't refresh the history since that might fail too.
    status.fail(function (jqXHR, textStatus, errorThrown) {
        console.log("Comment submission failed: " + textStatus + " - " + errorThrown);
        console.log("Response text: " + jqXHR.responseText);
        alert("Comment submission failed.");
    });

    // Clear the comment box and end the request
    status.always(function () {
        $("#" + dialogId + " textarea").val("");
        jlab.requestEnd();
    });
};

// This submits the filters to the comment-filter controller which updates the sessions with the specified filters.  Future comment
// request should reflect this change.
jlab.tableSorter.filterComments = function (dialogId, topic) {
    if (jlab.isRequest()) {
        window.console && console.log("Filter ajax in progress");
        return;
    }

    jlab.requestStart();
    var include = $("#" + dialogId + " .include-select").val();
    var exclude = $("#" + dialogId + " .exclude-select").val();

    var data = {};

    // Some versions of jQuery return null if nothing is selected, others return null
    if (include !== null && include.length > 0) {
        data.include = include;
    }
    if (exclude !== null && exclude.length > 0) {
        data.exclude = exclude;
    }

    var filter = $.ajax({
        url: jlab.util.commentFilterAjaxUrl,
        data: data,
        method: "POST",
        dataType: "json",
        traditional: true
    });

    // If it works, then the user now has a session with a comment filters.  Refresh the comment history to reflect.
    filter.done(function (data, textStatus, jqXHR) {
        jlab.tableSorter.refreshRFDCommentHistory(dialogId, topic);
    });

    filter.fail(function (jqXHR, textStatus, errorThrown) {
        window.console && console.log("Filter submission failed.  textStatus:  " + textStatus + " -- errorThrown: " + errorThrown);
        alert("Error submitting filter");
    });

    filter.always(function () {
        jlab.requestEnd();
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
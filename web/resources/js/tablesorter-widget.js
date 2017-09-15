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
        widthFixed: true,
        sortReset: true,
        showProcessing: true,
        widgets: ['zebra', 'stickyHeaders', 'output', 'filter'],
        headers: {0: {sorter: 'checkbox', filter: false}},
        widgetOptions: {
            output_includeHeader: true,
            stickyHeaders_addResizeEvent: true,
            stickyHeaders_attachTo: widgetId + " .table-panel"
        }
    });
    $table.tablesorterPager(pagerOptions);

    jlab.tableSorter.initOutputWidget(widgetId);
    
    jlab.tableSorter.initHelpDialog(widgetId);

    $(widgetId).tooltip();
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
jlab.tableSorter.initHelpDialog = function(widgetId) {
    console.log(widgetId);
    var dialogHTML = `<h4>Table Functionality</h4>
    <p>The RF Dashboard uses the jQuery Tablesorter 2.0 plugin and widget library.  These tables support advanced functionality
    beyond simple HTML tables.</p>
    <h5>Sorting</h5>
    <p>Clicking on the header cell of any column will sort the entire table according to that column.  A multi-column sort can be
    achieved by "Shift-Clicking" on the columns to be sorted.  This sorting functionality extends to the checkbox column as well.</p>
    <h5>Table Controls</h5>
    <p>Each table contains a set of pagination and data export controls.  The pagination controls allow the user to select the
    number of rows per page and the page that is displayed.  Data is exported via the "Output" button.  This output function supports
    a number of different formats and data filters which can be controlled through the "&#9660" / Output Options dropdown menu button.
    <h5>Filtering</h5>
    <p>The first cell of each column contains a text input field that can be used to filter the displayed data.  The following syntax
    rules can be used to support this filtering.</p>
    <img src="/RFDashboard/resources/img/jquery.tablesorter/filter-syntax.png"/>`;

//    $(widgetId + " .table-header").append(dialogHTML);
var dialogProperties = jlab.dialogProperties;
dialogProperties.width = 1000;
console.log($(widgetId));
    $(widgetId + "-help-dialog").append(dialogHTML);
    $(widgetId + "-help-dialog").dialog(dialogProperties);
    $(widgetId).find(".help-launcher").click(function() {
        $(widgetId + "-help-dialog").dialog("open");
    });
};
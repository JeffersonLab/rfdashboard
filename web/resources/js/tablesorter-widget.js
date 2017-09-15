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
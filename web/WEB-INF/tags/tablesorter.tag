<%-- 
    Document   : tablesorter-new
    Created on : Sep 6, 2017, 11:12:45 AM
    Author     : adamc
--%>

<%@tag description="A jQuery tablesorter 2.0 widget" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="widgetId"%>
<%@attribute name="tableTitle" %>
<%@attribute name="filename" %>

<%-- any content can be specified here e.g.: --%>
<div id="${widgetId}" class="rfd-tablesorter-widget">

    <div class="table-wrap">
        <div class="table-header">
            <div id="${widgetId}-help-dialog" title="Table Help"></div>
            <div class="table-title"><strong>${tableTitle}</strong><span class="help-launcher ui-icon ui-icon-help" title="Table Help"></span></div>
            <div class="table-controls">
                <div class="pager">
                    Page: <select class="gotoPage"></select>
                    <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/first.png" class="first"/>
                    <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/prev.png" class="prev"/>
                    <span class="pagedisplay"></span>
                    <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/next.png" class="next"/>
                    <img src="${pageContext.request.contextPath}/resources/img/jquery.tablesorter/last.png" class="last"/>
                    <select class="pagesize">
                        <option selected="selected"  value="10">10</option>
                        <option value="25">25</option>
                        <option value="50">50</option>
                        <option  value="100">100</option>
                        <option  value="10000">All</option>
                    </select>
                </div>
                <div class="output-group">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default download" title="Output data">Output</button>
                        <button type="button" class="btn btn-default dropdown-toggle" title="Output Options" data-toggle="dropdown">&#9660</button>
                    </div>
                    <ul class="dropdown-menu" role="menu">
                        <li><h5><strong>Output Options</strong></h5></li>
                        <li>
                            <label>Separator: <input class="output-separator-input" size="2" value="," type="text"></label>
                            <div class="btn-group">
                                <button type="button" class="output-separator btn btn-default btn-xs active" title="comma">,</button>
                                <button type="button" class="output-separator btn btn-default btn-xs" title="semi-colon">;</button>
                                <button type="button" class="output-separator btn btn-default btn-xs" title="tab">  </button>
                                <button type="button" class="output-separator btn btn-default btn-xs" title="space"> </button>
                                <button type="button" class="output-separator btn btn-default btn-xs" title="output JSON">json</button>
                                <button type="button" class="output-separator btn btn-default btn-xs" title="output Array">array</button>
                            </div>
                        </li>
                        <li>
                            <label>Send to:</label>
                            <div class="btn-group toggles output-download-popup" title="Download file or open in Popup window">
                                <input id="${widgetId}-popup" name="${widgetId}-delivery1" class="output-popup" type="radio" checked> 
                                <label class="btn btn-default btn-sm active" for="${widgetId}-popup">Popup</label>
                                <input id="${widgetId}-download" name="${widgetId}-delivery1" class="output-download" type="radio"> 
                                <label class="btn btn-default btn-sm" for="${widgetId}-download">Download</label>
                            </div>
                        </li>
                        <li>
                            <label>Include:</label>
                            <div class="btn-group toggles output-filter-all" data-toggle="buttons" title="Output only filtered, visible, selected, selected+visible or all rows">
                                <input id="${widgetId}-filtered" name="${widgetId}-getrows1" class="output-filter" checked="checked" type="radio">
                                <label class="btn btn-default btn-sm active" for="${widgetId}-filtered">Filtered</label>
                                <input id="${widgetId}-visible" name="${widgetId}-getrows1" class="output-visible" type="radio"> 
                                <label class="btn btn-default btn-sm" for="${widgetId}-visible">Visible</label>
                                <input id="${widgetId}-selected" name="${widgetId}-getrows1" class="output-selected" type="radio"> 
                                <label class="btn btn-default btn-sm" for="${widgetId}-selected">Selected</label>
                                <input id="${widgetId}-selvis" name="${widgetId}-getrows1" class="output-sel-vis" type="radio">
                                <label class="btn btn-default btn-sm" for="${widgetId}-selvis">Sel+Vis</label>
                                <input id="${widgetId}-all" name="${widgetId}-getrows1" class="output-all" type="radio"> 
                                <label class="btn btn-default btn-sm" for="${widgetId}-all">All</label>
                            </div>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <label>Replace quotes: <input class="output-replacequotes" size="2" value="'" type="text"></label>
                            <div class="btn-group">
                                <button type="button" class="output-quotes btn btn-default btn-xs active" title="single quote">'</button>
                                <button type="button" class="output-quotes btn btn-default btn-xs" title="left double quote">“</button>
                                <button type="button" class="output-quotes btn btn-default btn-xs" title="escaped quote">\"</button>
                            </div>
                        </li>
                        <li><label title="Remove extra white space from each cell">Trim spaces: <input class="output-trim" checked="" type="checkbox"></label></li>
                        <li><label title="Wrap all values in quotes">Wrap in Quotes: <input class="output-wrap" type="checkbox"></label></li>
                        <li><label title="Choose a download filename">Filename: <input class="output-filename" size="15" value="${filename}" type="text"></label></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="table-panel">
            <div class="ajax-loader" style="display:none;"><img src="${pageContext.request.contextPath}/resources/img/ajax_loader_gray_48.gif"/></div>
            <table class="tablesorter"></table>
        </div>
    </div>
</div>
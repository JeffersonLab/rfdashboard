<%-- 
    Document   : calendar-start-end
    Created on : Jan 16, 2018, 2:30:14 PM
    Author     : adamc
--%>

<%@tag description="put the tag description here" pageEncoding="UTF-8"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="id" required="true"%>
<%@attribute name="start"%>
<%@attribute name="end"%>
<%@attribute name="startLabel"%>
<%@attribute name="endLabel"%>

<%-- any content can be specified here e.g.: --%>
<div class="calendar-start-end" id="${id}">
    <div id="calendar-panel">
        <div class="li-key">
            <label class="required-field" for="start">${startLabel}</label>
        </div>
        <div class="li-value">
            <input type="text" class="date-field" id="start" name="start" placeholder="YYYY-MM-DD" value="${start}"/>
        </div>
        <div class="li-key">
            <label class="required-field" for="end">${endLabel}</label>
        </div>
        <div class="li-value">
            <input type="text" class="date-field nowable-field" id="end" name="end" placeholder="YYYY-MM-DD" value="${end}"/>
        </div>
    </div>
    <div class="calendar-presets">
        Presets:<button type="button" class="year-button">1Y</button><button type="button" class="month-button">1M</button><button type="button" class="week-button">1W</button>
    </div>
</div>
function loadJQueryDatePicker(inputField, allowEmptyDate, value)
{
    if (typeof $.fn.button.noConflict === typeof Function)
    {
        // bootstrap has taken over the button() function, assign it back to jquery ui
        // to resolve conflicts with the timepicker plugin
        $.fn.button.noConflict();
    }

    localDatePicker(
    {
        input: '#' + inputField,
        useTime: 1,
        parseFormat: 'YYYY-MM-DD HH:mm:ss',
        allowEmptyDate: allowEmptyDate,
        val: value,
        ashidden: { iso8601: inputField + 'ISO8601' }
    });
}

function loadJQueryDateOnlyPicker(inputField, allowEmptyDate, value)
{
    localDatePicker(
    {
        input: '#' + inputField,
        useTime: 0,
        parseFormat: 'YYYY-MM-DD',
        allowEmptyDate: allowEmptyDate,
        val: value,
        ashidden: { iso8601: inputField + 'ISO8601' }
    });
}

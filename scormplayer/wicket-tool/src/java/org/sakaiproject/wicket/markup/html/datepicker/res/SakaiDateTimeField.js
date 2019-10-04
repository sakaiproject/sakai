function loadJQueryDatePicker(inputField, allowEmptyDate, value)
{
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

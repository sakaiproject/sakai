function loadJQueryDatePicker(inputField, value)
{
    localDatePicker(
    {
        input: '#'+inputField,
        useTime: 1,
        parseFormat: 'YYYY-MM-DD HH:mm:ss',
        allowEmptyDate: false,
        val: value,
        ashidden: { iso8601: inputField+'ISO8601' }
    });
}

function loadJQueryDateOnlyPicker(inputField, value)
{
	localDatePicker(
    {
        input: '#'+inputField,
        useTime: 0,
        parseFormat: 'YYYY-MM-DD',
        allowEmptyDate: false,
        val: value,
        ashidden: { iso8601: inputField+'ISO8601' }
    });
}
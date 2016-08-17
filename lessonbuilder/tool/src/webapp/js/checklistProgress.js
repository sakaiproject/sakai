(function (checklistProgress, $, undefined) {

    checklistProgress.init = function () {
        // initialize table sorter
        $(".checklistProgressTable").tablesorter({
            sortList: [[0, 0]]
        });

        $('.headerNum').on("click", function () {
            var itemTitle = $(this).find('span.itemText').html();
            $('#descRowTip').html(itemTitle).show();
            $('.headerNum').removeClass('selectedChItem');
            $(this).addClass('selectedChItem');
        });
    };
}(window.checklistProgress = window.checklistProgress || {}, jQuery));
// Wait for DOM to be fully loaded
document.addEventListener('DOMContentLoaded', () => {
    const initDatePickers = () => {
        document.querySelectorAll('.pasystem-body .datepicker').forEach(datepicker => {
            const initDate = datepicker.dataset.initDate;
            console.log('Raw initDate from dataset:', initDate);
            console.log('Parsed initDate:', parseInt(initDate));
            console.log('Date object:', new Date(parseInt(initDate)));

            // setup date-time picker
            localDatePicker({
                input: '#' + datepicker.id,
                useTime: 1,
                allowEmptyDate: true,
                val: (initDate && initDate > 0) ? new Date(parseInt(initDate)) : undefined,
                ashidden: { iso8601: datepicker.id + '_selected_datetime' },
            });

            // add clear action if present
            datepicker.parentElement.querySelector(".clear-datepicker-btn")?.addEventListener('click', () => {
                datepicker.value = "";
            });
        });
    };

    const initDeleteConfirmation = () => {
        document.querySelectorAll(".pasystem-delete-btn").forEach(btn => {
            btn.addEventListener("click", function(event) {
                event.preventDefault();
                event.stopPropagation();

                const template = document.getElementById("pasystemDeleteConfirmationModalTemplate")?.innerHTML.trim().toString();
                const trimPathTemplate = TrimPath.parseTemplate(template, "pasystemDeleteConfirmationModalTemplate");

                const modal = trimPathTemplate.process({
                    recordType: this.dataset.recordType,
                    deleteURL: this.href
                });

                this.closest(".portletBody").insertAdjacentHTML('beforeend', modal);

                const modalId = document.getElementById("pasystemDeleteConfirmationModal");
                (new bootstrap.Modal(modalId)).show();
            });
        });
    };

    const addPreviewHandlers = () => {
        document.addEventListener('click', (event) => {
            const previewBtn = event.target.closest('.preview-btn');
            if (!previewBtn) return;

            event.preventDefault();
            const url = previewBtn.href;

            fetch(url)
                .then(response => response.text())
                .then(data => {
                    document.getElementById('popup-container-content').innerHTML = data;
                    new PASystemPopup('preview', 'preview');
                });
        });
    };

    const addFormHandlers = () => {
        const openCampaignRadio = document.getElementById('open-campaign-radio');

        if (openCampaignRadio) {
            const distribution = document.getElementById('distribution');

            document.querySelectorAll('.campaign-visibility').forEach(radio => {
                radio.addEventListener('change', () => {
                    distribution.disabled = (radio.id === openCampaignRadio.id);
                });
            });
        }

        document.querySelectorAll('.pasystem-cancel-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                window.location.replace(btn.dataset.target);
            });
        });
    };

    // Initialize all functionality
    initDatePickers();
    initDeleteConfirmation();
    addFormHandlers();
    addPreviewHandlers();
});

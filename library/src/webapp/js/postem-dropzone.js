/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2021 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

// Load dropzone library
includeWebjarLibrary('dropzone');

$(document).ready(function() {
    const uploadMax = document.getElementById('uploadMax').value;

    Dropzone.options.dropzoneForm = {
        autoProcessQueue: false,
        acceptedFiles: ".csv",
        maxFiles: 1,
        maxFilesize: uploadMax, // MB
        addRemoveLinks: true,
        init: function () {
            var dz = this;
            var status;
            var errorMessage;
            var submitButton = document.querySelector("#gradebook-continue-button");

            this.on('maxfilesexceeded', function(file) {
                this.removeFile(file);
            });

            this.on("removedfile", function (file) {
                submitButton.disabled = false;
                document.getElementById('uploading-failed').style.display = 'none';
                document.getElementById('uploading-failed').textContent = '';
            });

            // Client side copyright check
            submitButton.addEventListener("click", function(e) {
                // Make sure that the form isn't actually being sent.
                e.preventDefault();
                e.stopPropagation();

                if (dz.getQueuedFiles().length > 0) {
                        dz.processQueue();  
                } else {
                    document.getElementById('addContentForm').submit();
                }
            });

            this.on('success', function(file, responseText, e) {
                status = file.status;
            });

            this.on("error", function(file, message, xhr) {
                errorMessage = xhr.response;
                status = file.status;
            });

            this.on('queuecomplete', function() {
                if (status === Dropzone.SUCCESS) {
                    document.getElementById('addContentForm').submit();
                } else {
                    submitButton.disabled = true;
                    document.getElementById('uploading-failed').style.display = 'block';
                    document.getElementById('uploading-failed').textContent = errorMessage;
                }
            });
        }
    };
});

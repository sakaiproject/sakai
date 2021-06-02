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

function cancelVerify () {
    window.location.assign("add");
}

function cancelAction () {
    window.location.assign("../");
}

function processDelete (id) {
    window.location.assign("../processDelete/"+id);
}

function showStudent () {
    const selectedParticipant = document.getElementById('selectedParticipant').value;
    document.getElementById('student-view-form').action = document.getElementById('student-view-form').action+'/'+selectedParticipant;
    document.getElementById('student-view-form').submit();
}

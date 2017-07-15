-- SAM-3012 Update samigo events 
-- Update camel case events
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit' WHERE EVENT = 'sam.assessmentSubmitted';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.graded.auto' WHERE EVENT = 'sam.assessmentAutoGraded';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.auto"' WHERE EVENT = 'sam.assessmentAutoSubmitted';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.timer.thrd' WHERE EVENT = 'sam.assessmentTimedSubmitted';
UPDATE SAKAI_EVENT SET EVENT = 'sam.pubassessment.remove' WHERE EVENT = 'sam.pubAssessment.remove';

-- Update name of submission events
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.from_last' WHERE EVENT = 'sam.submit.from_last_page';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.from_toc' WHERE EVENT = 'sam.submit.from_toc';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.thread' WHERE EVENT = 'sam.assessment.thread_submit';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.timer' WHERE EVENT = 'sam.assessment.timer_submit';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.timer.url' WHERE EVENT = 'sam.assessment.timer_submit.url';

-- END SAM-3012 Update samigo events

-- SAM-787 Incorrect internationalization in survey questions
-- MAKE SURE YOUR DATABASE CONNECTION IS USING THE UTF-8 CHARACTER SET!!!!
-- 
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'نعم');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'لا');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'موافق');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير موافق');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'متردد');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'دون المعدل');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'معدل');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'فوق المتوسط');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أختلف بشدة');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أوافق بشدة');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير مقبول');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'ممتاز');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'نعم');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'لا');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'موافق');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير موافق');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'متردد');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'دون المعدل');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'معدل');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'فوق المتوسط');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أختلف بشدة');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أوافق بشدة');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير مقبول');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'ممتاز');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'D''acord');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'en desacord');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indecís');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sota de la mitjana');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Dins de la mitjana');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sobre de la mitjana');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt en desacord');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt d''acord');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excel·lent');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'D''acord');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'en desacord');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indecís');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sota de la mitjana');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Dins de la mitjana');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sobre de la mitjana');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt en desacord');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt d''acord');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excel·lent');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Yes');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Agree');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Disagree');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Undecided');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Below Average ');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Average ');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Above Average');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Disagree');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Agree');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Unacceptable');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Yes');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Agree');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Disagree');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Undecided');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Below Average ');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Average ');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Above Average');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Disagree');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Agree');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Unacceptable');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acuerdo');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En desacuerdo');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'NS/NC');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por debajo de la media');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Media');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por encima de la media');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente en desacuerdo');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente de acuerdo');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceptable');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acuerdo');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En desacuerdo');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'NS/NC');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por debajo de la media');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Media');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por encima de la media');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente en desacuerdo');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente de acuerdo');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceptable');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bai');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ados');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez ados');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Erabaki gabe');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Behean batez bestekoa ');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batez bestekoa');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Goian batez bestekoa');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batere ez ados');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oso ados');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onartezin');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bikain');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bai');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ados');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez ados');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Erabaki gabe');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Behean batez bestekoa ');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batez bestekoa');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Goian batez bestekoa');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batere ez ados');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oso ados');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onartezin');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bikain');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totallement en accord');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totallement en accord');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en accord');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en accord');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'はい');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'いいえ');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '認めます');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '認められません');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'まだ分かりません');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より下');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より上');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '全く認められません');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '完全に認めます');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '許容できません');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '優秀');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'はい');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'いいえ');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '認めます');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '認められません');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'まだ分かりません');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より下');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より上');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '全く認められません');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '完全に認めます');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '許容できません');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '優秀');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Тийн');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үгүй');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрч байна');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрөхгүй байна');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Шийдээгүй');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас доош');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундаж');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас дээш');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үл Зөвшөөрөхгүй байна');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Баттай Зөвшөөрч байна');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Хүлээн авах боломжгүй');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Онц');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Тийн');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үгүй');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрч байна');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрөхгүй байна');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Шийдээгүй');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас доош');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундаж');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас дээш');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үл Зөвшөөрөхгүй байна');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Баттай Зөвшөөрч байна');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Хүлээн авах боломжгүй');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Онц');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nee');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Akkoord');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oneens');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onbeslist');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onder het gemiddelde');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Gemiddeld');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bovengemiddeld');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig oneens');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig eens');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onaanvaardbaar');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Uitmuntend');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nee');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Akkoord');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oneens');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onbeslist');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onder het gemiddelde');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Gemiddeld');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bovengemiddeld');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig oneens');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig eens');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onaanvaardbaar');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Uitmuntend');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indeciso ');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da Média');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da Média');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda Fortemente ');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda Fortemente');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável ');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente ');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indeciso ');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da Média');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da Média');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda Fortemente ');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda Fortemente');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável ');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente ');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordar');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sem decisão');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da média');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da média');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discordo plenamente');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordo plenamente');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordar');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sem decisão');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da média');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da média');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discordo plenamente');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordo plenamente');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Да');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Нет');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Согласен');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Не согласен');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Затрудняюсь ответить');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ниже среднего ');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Средне ');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Выше среднего');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Категорически не согласен');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Полностью согласен');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Неприемлемо');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Отлично');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Да');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Нет');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Согласен');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Не согласен');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Затрудняюсь ответить');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ниже среднего ');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Средне ');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Выше среднего');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Категорически не согласен');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Полностью согласен');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Неприемлемо');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Отлично');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nej');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller delvis med');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller inte med');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Tveksam');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Under medel ');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Medel');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Över medel');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut inte med');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut med');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oacceptabelt');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Utmärkt');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nej');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller delvis med');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller inte med');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Tveksam');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Under medel ');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Medel');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Över medel');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut inte med');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut med');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oacceptabelt');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Utmärkt');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Evet');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Hayır');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılıyorum');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılmıyorum');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kararsızım');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Altında');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalama');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Üstünde');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılmıyorum');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılıyorum');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kabul Edilemez');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Çok İyi');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Evet');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Hayır');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılıyorum');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılmıyorum');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kararsızım');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Altında');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalama');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Üstünde');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılmıyorum');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılıyorum');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kabul Edilemez');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Çok İyi');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '是');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '否');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不确定');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '低于平均水平');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均水平');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '高于平均水平');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不可接受');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '优秀');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '是');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '否');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不确定');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '低于平均水平');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均水平');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '高于平均水平');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不可接受');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '优秀');
update SAM_ANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '是 ');
update SAM_ANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '否 ');
update SAM_ANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意 ');
update SAM_ANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意 ');
update SAM_ANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不確定 ');
update SAM_ANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '低於平均值 ');
update SAM_ANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均 ');
update SAM_ANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '高於平均值 ');
update SAM_ANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意 ');
update SAM_ANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意 ');
update SAM_ANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不接受 ');
update SAM_ANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_ITEM_T i, (select * from SAM_ANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常好 ');
update SAM_PUBLISHEDANSWER_T set text='st_yes'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '是 ');
update SAM_PUBLISHEDANSWER_T set text='st_no'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '否 ');
update SAM_PUBLISHEDANSWER_T set text='st_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意 ');
update SAM_PUBLISHEDANSWER_T set text='st_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意 ');
update SAM_PUBLISHEDANSWER_T set text='st_undecided'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不確定 ');
update SAM_PUBLISHEDANSWER_T set text='st_below_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '低於平均值 ');
update SAM_PUBLISHEDANSWER_T set text='st_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均 ');
update SAM_PUBLISHEDANSWER_T set text='st_above_average'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '高於平均值 ');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_disagree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意 ');
update SAM_PUBLISHEDANSWER_T set text='st_strongly_agree'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意 ');
update SAM_PUBLISHEDANSWER_T set text='st_unacceptable'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '不接受 ');
update SAM_PUBLISHEDANSWER_T set text='st_excellent'
where answerid in
(select a.answerid from SAM_PUBLISHEDITEM_T i, (select * from SAM_PUBLISHEDANSWER_T) a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常好 ');

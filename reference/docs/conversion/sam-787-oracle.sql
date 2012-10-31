-- SAM-787 Incorrect internationalization in survey questions
-- MAKE SURE YOUR DATABASE CONNECTION IS USING THE UTF-8 CHARACTER SET!!!!
-- 
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'نعم');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'لا');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'موافق');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير موافق');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'متردد');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'دون المعدل');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'معدل');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'فوق المتوسط');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أختلف بشدة');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أوافق بشدة');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير مقبول');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'ممتاز');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'نعم');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'لا');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'موافق');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير موافق');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'متردد');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'دون المعدل');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'معدل');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'فوق المتوسط');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أختلف بشدة');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'أوافق بشدة');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'غير مقبول');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'ممتاز');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'D''acord');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'en desacord');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indecís');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sota de la mitjana');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Dins de la mitjana');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sobre de la mitjana');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt en desacord');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt d''acord');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excel·lent');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'D''acord');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'en desacord');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indecís');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sota de la mitjana');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Dins de la mitjana');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Per sobre de la mitjana');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt en desacord');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Molt d''acord');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excel·lent');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Yes');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Agree');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Disagree');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Undecided');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Below Average ');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Average ');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Above Average');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Disagree');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Agree');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Unacceptable');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Yes');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Agree');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Disagree');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Undecided');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Below Average ');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Average ');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Above Average');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Disagree');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Strongly Agree');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Unacceptable');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acuerdo');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En desacuerdo');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'NS/NC');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por debajo de la media');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Media');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por encima de la media');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente en desacuerdo');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente de acuerdo');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceptable');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sí');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'No');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acuerdo');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En desacuerdo');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'NS/NC');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por debajo de la media');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Media');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Por encima de la media');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente en desacuerdo');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalmente de acuerdo');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceptable');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bai');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ados');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez ados');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Erabaki gabe');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Behean batez bestekoa ');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batez bestekoa');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Goian batez bestekoa');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batere ez ados');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oso ados');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onartezin');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bikain');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bai');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ados');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ez ados');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Erabaki gabe');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Behean batez bestekoa ');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batez bestekoa');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Goian batez bestekoa');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Batere ez ados');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oso ados');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onartezin');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bikain');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totallement en accord');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totallement en accord');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en accord');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oui');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Non');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Accepter');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'En désaccord');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indécis');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sous la moyenne');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Moyenne');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Au-dessus de la moyenne');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en désaccord');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Totalement en accord');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inacceptable');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excellent');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'はい');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'いいえ');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '認めます');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '認められません');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'まだ分かりません');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より下');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より上');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '全く認められません');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '完全に認めます');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '許容できません');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '優秀');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'はい');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'いいえ');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '認めます');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '認められません');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'まだ分かりません');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より下');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均より上');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '全く認められません');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '完全に認めます');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '許容できません');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '優秀');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Тийн');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үгүй');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрч байна');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрөхгүй байна');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Шийдээгүй');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас доош');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундаж');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас дээш');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үл Зөвшөөрөхгүй байна');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Баттай Зөвшөөрч байна');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Хүлээн авах боломжгүй');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Онц');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Тийн');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үгүй');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрч байна');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Зөвшөөрөхгүй байна');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Шийдээгүй');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас доош');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундаж');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Дундажаас дээш');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Үл Зөвшөөрөхгүй байна');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Баттай Зөвшөөрч байна');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Хүлээн авах боломжгүй');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Онц');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nee');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Akkoord');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oneens');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onbeslist');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onder het gemiddelde');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Gemiddeld');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bovengemiddeld');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig oneens');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig eens');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onaanvaardbaar');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Uitmuntend');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nee');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Akkoord');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oneens');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onbeslist');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onder het gemiddelde');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Gemiddeld');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Bovengemiddeld');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig oneens');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Volledig eens');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Onaanvaardbaar');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Uitmuntend');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indeciso ');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da Média');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da Média');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda Fortemente ');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda Fortemente');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável ');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente ');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Indeciso ');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da Média');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da Média');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda Fortemente ');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concorda Fortemente');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável ');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente ');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordar');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sem decisão');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da média');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da média');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discordo plenamente');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordo plenamente');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sim');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Não');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordar');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discorda');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Sem decisão');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Abaixo da média');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Média');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Acima da média');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Discordo plenamente');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Concordo plenamente');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Inaceitável');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Excelente');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Да');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Нет');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Согласен');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Не согласен');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Затрудняюсь ответить');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ниже среднего ');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Средне ');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Выше среднего');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Категорически не согласен');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Полностью согласен');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Неприемлемо');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Отлично');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Да');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Нет');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Согласен');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Не согласен');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Затрудняюсь ответить');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ниже среднего ');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Средне ');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Выше среднего');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Категорически не согласен');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Полностью согласен');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Неприемлемо');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Отлично');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nej');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller delvis med');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller inte med');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Tveksam');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Under medel ');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Medel');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Över medel');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut inte med');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut med');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oacceptabelt');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Utmärkt');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ja');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Nej');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller delvis med');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller inte med');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Tveksam');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Under medel ');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Medel');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Över medel');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut inte med');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Håller absolut med');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Oacceptabelt');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Utmärkt');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Evet');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Hayır');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılıyorum');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılmıyorum');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kararsızım');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Altında');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalama');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Üstünde');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılmıyorum');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılıyorum');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kabul Edilemez');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Çok İyi');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Evet');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Hayır');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılıyorum');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Katılmıyorum');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kararsızım');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Altında');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalama');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Ortalamanın Üstünde');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılmıyorum');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kesinlikle Katılıyorum');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Kabul Edilemez');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = 'Çok İyi');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '是');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '否');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不确定');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '低于平均水平');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均水平');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '高于平均水平');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不可接受');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '优秀');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '是');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '否');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不确定');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '低于平均水平');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均水平');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '高于平均水平');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不可接受');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '优秀');
update sam_answer_t set text='st_yes'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '是 ');
update sam_answer_t set text='st_no'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '否 ');
update sam_answer_t set text='st_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意 ');
update sam_answer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意 ');
update sam_answer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不確定 ');
update sam_answer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '低於平均值 ');
update sam_answer_t set text='st_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均 ');
update sam_answer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '高於平均值 ');
update sam_answer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意 ');
update sam_answer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意 ');
update sam_answer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不接受 ');
update sam_answer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_item_t i, sam_answer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常好 ');
update sam_publishedanswer_t set text='st_yes'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '是 ');
update sam_publishedanswer_t set text='st_no'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '否 ');
update sam_publishedanswer_t set text='st_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '同意 ');
update sam_publishedanswer_t set text='st_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不同意 ');
update sam_publishedanswer_t set text='st_undecided'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不確定 ');
update sam_publishedanswer_t set text='st_below_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '低於平均值 ');
update sam_publishedanswer_t set text='st_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '平均 ');
update sam_publishedanswer_t set text='st_above_average'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '高於平均值 ');
update sam_publishedanswer_t set text='st_strongly_disagree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常不同意 ');
update sam_publishedanswer_t set text='st_strongly_agree'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常同意 ');
update sam_publishedanswer_t set text='st_unacceptable'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '不接受 ');
update sam_publishedanswer_t set text='st_excellent'
where answerid in
(select a.answerid from sam_publisheditem_t i, sam_publishedanswer_t a where i.typeid=3 and a.itemid=i.itemid and a.text = '非常好 ');

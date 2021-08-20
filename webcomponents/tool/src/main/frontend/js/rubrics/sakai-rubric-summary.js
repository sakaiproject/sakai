import {RubricsElement} from "./rubrics-element.js";
import {html} from "/webcomponents/assets/lit-element/lit-element.js";
import "./sakai-rubric-sort-name.js";

export class SakaiRubricSummary extends RubricsElement {

    constructor() {
        super();
    }

    static get properties() {
        return {
            token: String,
            toolId: { attribute: "tool-id", type: String },
            entityId: { attribute: "entity-id", type: String },
            evaluatedItemId: { attribute: "evaluated-item-id", type: String },
            evaluatedItemOwnerId: { attribute: "evaluated-item-owner-id", type: String },
            summarytype: {attribute: "summarytype", type:String},
            // Non attribute
            evaluation: { type: Object },
            totalPoints: Number,
            translatedTotalPoints: { type: Number },
            selectedRatings: { type: Array },
            criteria: { type: Array },
            rubric: { type: Object },
            allEvaluations: { type: Array },
            columncounter: { type: Number }
        };
    }

    attributeChangedCallback(name, oldVal, newVal) {
        super.attributeChangedCallback(name, oldVal, newVal);
        if (this.entityId && this.toolId && this.token) {
            this.getAssociation();
            this.getAllEvaluations();
        }
    }

    render() {
        if(this.summarytype === 'criteria'){
            return html`
                <div class="pull-right collapse-toggle-buttons">
                    <button type="button" @click=${this.expandAll}><sr-lang key="expand_all">expand all</sr-lang></button>
                    <button type="button" @click=${this.collapseAll}><sr-lang key="collapse_all">collapse all</sr-lang></button>
                </div>
                <h3><sr-lang key="criteria_summary">CRITERIA</sr-lang></h3>
                ${this.allEvaluations.length === 0 ? html`
                    <div class="sak-banner-warn">
                        <sr-lang key="no_evaluations_warning">WARN</sr-lang>
                    </div>
                ` : html`
                    ${this.criteria.map((c) => html`
                        <div class="panel-group">
                            <div class="panel panel-default">
                                <div class="panel-heading">
                                    <h4 class="panel-title">
                                        <a class="collapse-toggle collapsed" data-toggle="collapse" href="#collapse${c.id}">${c.title}</a>
                                    </h4>
                                </div>
                                <div id="collapse${c.id}" class="panel-collapse collapse">
                                    <div class="panel-body">
                                        <div class="table-responsive">
                                <table class="rubrics-summary-table table table-bordered table-condensed">
                                    <tr>
                                        ${c.ratings.map((r) => html`
                                            <th class="rubrics-summary-table-cell">
                                                <div>${r.points} <sr-lang key="points">points</sr-lang></div>
                                                <div class="summary-rating-name" title="${r.title}">${this.limitCharacters(r.title, 20)}</div>
                                            </th>
                                            ${this.association.parameters.fineTunePoints && this.getCustomCount(c.id, r.points)>0 ? html`
                                                <th class="rubrics-summary-table-cell"><sr-lang key="adjusted_score">adjustedscore</sr-lang></th>
                                            ` : html``}
                                        `)}
                                        <th style="display:none" class="rubrics-summary-table-cell rubrics-summary-average-cell"><sr-lang key="average">average</sr-lang></th>
                                        <th style="display:none" class="rubrics-summary-table-cell "><sr-lang key="median">median</sr-lang></th>
                                        <th style="display:none" class="rubrics-summary-table-cell "><sr-lang key="stdev">stdev</sr-lang></th>
                                    </tr>
                                    <tr>
                                        ${c.ratings.map((r) => html`
                                            <td class="points-${r.points} rubrics-summary-table-cell pointCell-${c.id}" >${this.getACount(c.id, r.id)}</td>
                                            ${this.association.parameters.fineTunePoints && this.getCustomCount(c.id, r.points)>0 ? html`
                                                <td class="rubrics-summary-table-cell">${this.getCustomCount(c.id, r.points)}</td>
                                            ` : html``}
                                        `)}
                                        <td style="display:none" class="rubrics-summary-table-cell rubrics-summary-average-cell">${this.getPointsAverage(c.id)}</td>
                                        <td style="display:none" class="rubrics-summary-table-cell">${this.getPointsMedian(c.id)}</td>
                                        <td style="display:none" class="rubrics-summary-table-cell">${this.getPointsStdev(c.id)}</td>
                                    </tr>
                                </table>
                            </div>
                            <dl class="dl-horizontal">
                                <dt><sr-lang key="average">average</sr-lang></dt>
                                <dd>
                                    ${this.getPointsAverage(c.id)}
                                </dd>
                                <dt><sr-lang key="median">median</sr-lang></dt>
                                <dd>
                                    ${this.getPointsMedian(c.id)}
                                </dd>
                                <dt><sr-lang key="stdev">stdev</sr-lang></dt>
                                <dd>
                                    ${this.getPointsStdev(c.id)}
                                </dd>
                            </dl>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `)}
                    <div><sr-lang key="adjusted_score_warning">adjustedscorewarning</sr-lang></div>
                ` }
            `;
        } else {
            return html`
            <h3><sr-lang key="student_summary">studentsummary</sr-lang></h3>
            ${this.allEvaluations.length === 0 ? html`
                <div class="sak-banner-warn">
                    <sr-lang key="no_evaluations_warning">WARN</sr-lang>
                </div>
            ` : html`
                <div class="table-responsive">
                    <table id="student-summary" class="rubrics-summary-table table table-bordered table-condensed">
                        <thead>
                        <tr>
                            <th class="rubrics-summary-table-cell rubrics-summary-table-cell-wide"><sr-lang key="student_name">studentname</sr-lang></th>
                            ${this.criteria.map((c) => html`<th class="rubrics-summary-table-cell" >${c.title}</th>`)}
                            <th class="rubrics-summary-table-cell rubrics-summary-average-cell"><sr-lang key="score">score</sr-lang></th>
                        </tr>
                        </thead>
                        <tbody>
                        ${this.allEvaluations.map((e) => html`
                            <tr>
                                <td class="rubrics-summary-table-cell rubrics-summary-table-cell-wide nameColumn" >
                                    <sakai-rubric-sort-name user-id="${e.evaluatedItemOwnerId}"></sakai-rubric-sort-name>
                                </td>
                                ${e.criterionOutcomes.map((o) => html`
                                    <td class="rubrics-summary-table-cell rubrics-summary-table-cell pointCell-${o.criterionId}" >${o.points}</td>
                                `)}
                                <td class="rubrics-summary-table-cell rubrics-summary-average-cell pointCell-score" >${this.getCriteriaTotal(e.criterionOutcomes)}</td>
                            </tr>
                        `)}
                        </tbody>
                        <tfoot>
                        <tr>
                            <th class="rubrics-summary-table-cell rubrics-summary-table-cell-wide rubrics-summary-average-row " ><sr-lang key="average">average</sr-lang></th>
                            ${this.criteria.map((c) => html`<td class="rubrics-summary-table-cell rubrics-summary-average-row">${this.getColumnAverage(c.id)}</td>`)}
                            <td class="rubrics-summary-table-cell rubrics-summary-average-cell rubrics-summary-average-row" >${this.getColumnAverage('score')}</td>
                        </tr>
                        </tfoot>
                    </table>
                </div>
            ` }
        `;
        }
    }

    getAssociation() {
        $.ajax({
            url: `/rubrics-service/rest/rubric-associations/search/by-tool-and-assignment?toolId=${this.toolId}&itemId=${this.entityId}`,
            headers: { "authorization": this.token }
        }).done((data) => {
            this.association = data._embedded['rubric-associations'][0];
            var rubricId = data._embedded['rubric-associations'][0].rubricId;
            this.getRubric(rubricId);
        }).fail((jqXHR, textStatus, errorThrown) => {
            console.log(textStatus);
            console.log(errorThrown);
        });
    }

    getRubric(rubricId) {
        $.ajax({
            url: `/rubrics-service/rest/rubrics/${rubricId}?projection=inlineRubric`,
            headers: { "authorization": this.token }
        }).done((rubric) => {
            $.ajax({
                url: `/rubrics-service/rest/evaluations/search/by-tool-and-assignment-and-submission?toolId=${this.toolId}&itemId=${this.entityId}&evaluatedItemId=${this.evaluatedItemId}`,
                headers: { "authorization": this.token }
            }).done((data) => {
                this.evaluation = data._embedded.evaluations[0] || { criterionOutcomes: [] };
                this.selectedRatings = this.evaluation.criterionOutcomes.map((ed) => ed.selectedRatingId);
                this.existingEvaluation = true;
                this.rubric = rubric;
                this.criteria = this.rubric.criterions;
                this.criteria.forEach((c) => {
                    if (!c.selectedvalue) {
                        c.selectedvalue = 0;
                    }
                    c.pointrange = this.getHighLow(c.ratings, "points");
                });
            }).fail((jqXHR, textStatus, errorThrown) => {
                console.log(textStatus);console.log(errorThrown);
            });
        }).fail((jqXHR, textStatus, errorThrown) => {
            console.log(textStatus);console.log(errorThrown);
        });
    }

    getAllEvaluations(){
        $.ajax({
            url: `/rubrics-service/rest/evaluations/search/by-tool-item-and-associated-item-ids?toolId=${this.toolId}&itemId=${this.entityId}`,
            headers: { "authorization": this.token }
        }).done((data) => {
            this.allEvaluations = data._embedded.evaluations;
        }).fail((jqXHR, textStatus, errorThrown) => {
            console.log(textStatus);
            console.log(errorThrown);
        });
    }

    getCriteriaTotal(criteriaOutcomes){
        var total = 0;
        for (var i=0; i<criteriaOutcomes.length; i++){
            total = total + criteriaOutcomes[i].points;
        }
        return total;
    }

    limitCharacters(string, chars){
        if(string.length > parseInt(chars)){
            return string.substring(0, parseInt(chars)) + '...';
        }
        return string;
    }

    getColumnAverage(columnnumber){
        var total = 0;
        var column = document.getElementsByClassName('pointCell-'+columnnumber);
        var count = 0;
        while (count<column.length){
            total = total + parseInt(column[count].textContent);
            count++;
        }
        return (total / count).toFixed(2);
    }

    getStudentName(studentid){
        $.ajax({
            url: `/rubrics-service/rest/evaluations/search/user-name?userId=${studentid}`,
            headers: { "authorization": this.token }
        }).done((result) => {return result.toString();});
    }

    getACount(criterion, rating){
        var total = 0;
        for (var i=0; i<this.allEvaluations.length; i++) {
            for (var j=0; j<this.allEvaluations[i].criterionOutcomes.length; j++){
                if(this.allEvaluations[i].criterionOutcomes[j].criterionId === parseInt(criterion)
                    && this.doesScoreMatchRating(this.allEvaluations[i].criterionOutcomes[j].points, criterion, rating)){
                    total = total + 1;
                }
            }
        }
        return total;
    }

    doesScoreMatchRating(score, criterionId, ratingId){
        for(var count=0; count<this.criteria.length; count++){
            if(parseInt(criterionId)===this.criteria[count].id){
                for(var count2=0; count2<this.criteria[count].ratings.length; count2++){
                    if(parseInt(score)===this.criteria[count].ratings[count2].points
                        && parseInt(ratingId)===this.criteria[count].ratings[count2].id){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    getPointsAverage(criterion){
        var total = 0;
        var count = 0;
        while (count < this.allEvaluations.length){
            for (var count2=0; count2<this.allEvaluations[count].criterionOutcomes.length; count2++){
                if(this.allEvaluations[count].criterionOutcomes[count2].criterionId === criterion){
                    total = total + this.allEvaluations[count].criterionOutcomes[count2].points;
                }
            }
            count++;
        }
        return (total / count).toFixed(2);
    }

    getPointsMedian(criterion){
        var values = [];
        var count = 0;
        while (count < this.allEvaluations.length){
            for (var count2=0; count2<this.allEvaluations[count].criterionOutcomes.length; count2++){
                if(this.allEvaluations[count].criterionOutcomes[count2].criterionId === criterion){
                    values.push(this.allEvaluations[count].criterionOutcomes[count2].points);
                }
            }
            count++;
        }
        if(values.length ===0){
            return 0;
        }
        values.sort(function(a,b){
            return a-b;
        });
        var half = Math.floor(values.length / 2);
        if (values.length % 2){
            return values[half];
        }
        return ((values[half - 1] + values[half]) / 2.0).toFixed(2);
    }

    getPointsStdev(criterion){
        var average = this.getPointsAverage(criterion);
        var values = [];
        var count = 0;
        while (count < this.allEvaluations.length){
            for (var count2=0; count2<this.allEvaluations[count].criterionOutcomes.length; count2++){
                if(this.allEvaluations[count].criterionOutcomes[count2].criterionId === criterion){
                    values.push(this.allEvaluations[count].criterionOutcomes[count2].points);
                }
            }
            count++;
        }
        var total = 0;
        for (var count3=0; count3<values.length; count3++){
            total = total + (average - values[count3]) * (average - values[count3]);
        }
        total = total / values.length;
        return Math.sqrt(total).toFixed(2);
    }

    getCustomCount(criterion, floorPoints){
        var ceilingPoints = 5000;
        for (var count=0; count<this.criteria.length; count++){
            if(this.criteria[count].id === criterion){
                for(var count2=0; count2<this.criteria[count].ratings.length; count2++){
                    if(this.criteria[count].ratings[count2].points > parseInt(floorPoints)){
                        ceilingPoints = this.criteria[count].ratings[count2].points;
                        break;
                    }
                }
                break;
            }
        }
        var total = 0;
        for (var i=0; i<this.allEvaluations.length; i++) {
            for (var j=0; j<this.allEvaluations[i].criterionOutcomes.length; j++){
                if(this.allEvaluations[i].criterionOutcomes[j].criterionId === parseInt(criterion)
                    && this.allEvaluations[i].criterionOutcomes[j].points > parseInt(floorPoints)
                    && this.allEvaluations[i].criterionOutcomes[j].points < ceilingPoints){
                    total = total + 1;
                }
            }
        }
        return total;
    }

    expandAll() {
        $(".collapse-toggle.collapsed").click();
    }

    collapseAll() {
        $(".collapse-toggle").not(".collapsed").click();
    }
}
customElements.define("sakai-rubric-summary", SakaiRubricSummary);
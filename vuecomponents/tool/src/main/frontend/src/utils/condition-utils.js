export const CONDITION_TOOL_ID = "sakai.conditions";

export const ConditionOperator = {
    SMALLER_THAN: "SMALLER_THAN",
    SMALLER_THAN_OR_EQUAL_TO: "SMALLER_THAN_OR_EQUAL_TO",
    EQUAL_TO: "EQUAL_TO",
    GREATER_THAN_OR_EQUAL_TO: "GREATER_THAN_OR_EQUAL_TO",
    GREATER_THAN: "GREATER_THAN",
    AND: "AND",
    OR: "OR",
};

export const CONDITION_OPERATORS = [
    ConditionOperator.SMALLER_THAN,
    ConditionOperator.SMALLER_THAN_OR_EQUAL_TO,
    ConditionOperator.EQUAL_TO,
    ConditionOperator.GREATER_THAN_OR_EQUAL_TO,
    ConditionOperator.GREATER_THAN,
];

export const PARENT_CONDITION_OPERATORS = [
    ConditionOperator.AND,
    ConditionOperator.OR,
];

export const ConditionType = {
    COMPLETED: "COMPLETED",
    PARENT: "PARENT",
    SCORE: "SCORE",
    ROOT: "ROOT",
};

export const CONDITION_TYPES = [
    ConditionType.COMPLETED,
    ConditionType.SCORE,
];

export const LessonItemType = {
    QUESTION: 11,
};

export const CONDITION_BUNDLE_NAME = "condition";

export function formatOperator(conditionI18n, operator) {
    return conditionI18n[operator.toLowerCase()];
}

function formatCondition(formatter, conditionI18n, condition, item) {
    switch(condition.type) {
        case ConditionType.SCORE:
            const commonInserts = [ formatOperator(conditionI18n, condition.operator), condition.argument ];
            if (item) {
                return formatter(conditionI18n["display_the_item_score"], item, ...commonInserts);
            } else {
                return formatter(conditionI18n["display_this_item_score"], ...commonInserts);
            }
        case ConditionType.COMPLETED:
            if (item) {
                return formatter(conditionI18n["display_the_item_completed"], item);
            } else {
                return formatter(conditionI18n["display_this_item_completed"]);
            }
        default:
            console.error(`Formatting of condition with type '${condition.type}' is not implemented`);
            return conditionI18n["unknown_condition"];
    }
}

function formatText(template, ...inserts) {
    let formattedText = template;

    inserts?.forEach((insert, index) => {
        formattedText = formattedText?.replace(`{${index}}`, insert);
    });

    return formattedText;
}

export function formatConditionText(conditionI18n, condition, item) {
    return formatCondition(formatText, conditionI18n, condition, item);
}

function formatHtml(template, ...inserts) {
    let formattedHtml = template;

    inserts?.forEach((insert, index) => {
        const boldInsert = `<b>${insert}</b>`;

        formattedHtml = formattedHtml?.replace(`{${index}}`, boldInsert);
    });

    return formattedHtml;
}

export function formatConditionHtml(conditionI18n, condition, item) {
    return formatCondition(formatHtml, conditionI18n, condition, item);
}

export function makeParentCondition(siteId, operator = ConditionOperator.OR) {
    return {
        type: ConditionType.PARENT,
        siteId,
        toolId: CONDITION_TOOL_ID,
        itemId: null,
        operator,
        argument: null,
        subConditions:[],
    };
}

export function makeRootCondition(siteId, toolId, itemId) {
    return {
        type: ConditionType.ROOT,
        siteId,
        toolId,
        itemId,
        operator: ConditionOperator.AND,
        argument: null,
        subConditions:[],
    };
}

export function nonRootConditionFilter(condition) {
    return condition.type !== ConditionType.ROOT;
}

export function nonParentConditionFilter(condition) {
    return condition.type !== ConditionType.PARENT;
}

export function andParentConditionFilter(condition) {
    return condition.type === ConditionType.PARENT
            && condition.operator === ConditionOperator.AND;
}

export function orParentConditionFilter(condition) {
    return condition.type === ConditionType.PARENT
            && condition.operator === ConditionOperator.OR;
}

export function lessonItemName(lessonItem) {
    switch(lessonItem.type) {
        case LessonItemType.QUESTION:
            return lessonItem.questionText ?? lessonItem.name;
        default:
            return lessonItem.name;
    }
}

export default "ConditionsUtils";

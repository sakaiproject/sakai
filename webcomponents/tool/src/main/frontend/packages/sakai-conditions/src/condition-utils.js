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

export const ConditionType = {
  COMPLETED: "COMPLETED",
  PARENT: "PARENT",
  SCORE: "SCORE",
  ROOT: "ROOT",
};

export const LessonItemType = {
  QUESTION: 11,
};

export function formatOperator(conditionI18n, operator) {
  return conditionI18n[operator.toLowerCase()];
}

export function conditionMessage(i18n, condition, item) {
  if (condition.type === ConditionType.SCORE) {
    const inserts = [ formatOperator(i18n, condition.operator), condition.argument ];
    return item
      ? { template: i18n.display_the_item_score, inserts: [ item, ...inserts ] }
      : { template: i18n.display_this_item_score, inserts };
  }
  if (condition.type === ConditionType.COMPLETED) {
    return item
      ? { template: i18n.display_the_item_completed, inserts: [ item ] }
      : { template: i18n.display_this_item_completed, inserts: [] };
  }
  return { template: i18n.unknown_condition, inserts: [] };
}

export function formatConditionText(i18n, condition, item) {
  const { template, inserts } = conditionMessage(i18n, condition, item);
  return template.replace(/\{(\d+)\}/g, (match, index) => inserts[Number(index)] ?? match);
}

export function plainText(value) {
  return value ? new DOMParser().parseFromString(value, "text/html").body.textContent.trim() : "";
}

export function makeParentCondition(siteId, operator = ConditionOperator.OR) {
  return {
    type: ConditionType.PARENT,
    siteId,
    toolId: CONDITION_TOOL_ID,
    itemId: null,
    operator,
    argument: null,
    subConditions: [],
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
    subConditions: [],
  };
}

export function nonRootConditionFilter(condition) {
  return condition.type !== ConditionType.ROOT;
}

export function nonParentConditionFilter(condition) {
  return condition.type !== ConditionType.PARENT;
}

export function lessonItemName(lessonItem) {
  switch (lessonItem.type) {
    case LessonItemType.QUESTION:
      return lessonItem.questionText ?? lessonItem.name;
    default:
      return lessonItem.name;
  }
}

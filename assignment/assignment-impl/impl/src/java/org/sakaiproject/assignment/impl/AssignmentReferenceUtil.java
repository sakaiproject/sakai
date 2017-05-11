package org.sakaiproject.assignment.impl;

import static org.sakaiproject.assignment.api.AssignmentServiceConstants.APPLICATION_ID;
import static org.sakaiproject.assignment.api.AssignmentServiceConstants.REFERENCE_ROOT;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;

import lombok.Setter;

/**
 * Created by enietzel on 5/10/17.
 */
public class AssignmentReferenceUtil {

    @Setter private ServerConfigurationService serverConfigurationService;

    public String makeStringReference(Reference reference, boolean relative) {
        if (relative) {
            return makeRelativeStringReference(reference.getContext(), reference.getSubType(), reference.getId(), reference.getContainer());
        } else {
            return makeUrlStringReference(reference.getContext(), reference.getSubType(), reference.getId(), reference.getContainer());
        }
    }

    public String makeStringReference(Assignment assignment, boolean relative) {
        if (relative) {
            return makeRelativeStringReference(assignment.getContext(), "a", assignment.getId(), null);
        } else {
            return makeUrlStringReference(assignment.getContext(), "a", assignment.getId(), null);
        }
    }

    public String makeRelativeStringReference(String context, String type, String id, String container) {
        return getAccessPoint(true) + AssignmentReferenceReckoner.reckoner().context(context).subtype(type).id(id).container(container).reckon();
    }

    public String makeUrlStringReference(String context, String type, String id, String container) {
        return getAccessPoint(false) + AssignmentReferenceReckoner.reckoner().context(context).subtype(type).id(id).container(container).reckon();
    }

    public String getAccessPoint(boolean relative) {
        return (relative ? "" : serverConfigurationService.getAccessUrl());
    }

    public String makeRelativeAssignmentContextStringReference(String context) {
        return makeRelativeStringReference(context, "a", null, null);
    }

    public String getSubTypeFromStringReference(String reference) {
        return AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getSubtype();
    }

    public String getContextFromStringReference(String reference) {
        return AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getContext();
    }

    public String getIdFromStringReference(String reference) {
        return AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getId();
    }

    public String getContainerFromStringReference(String reference) {
        return AssignmentReferenceReckoner.reckoner().reference(reference).reckon().getContainer();
    }

//    private String makeStringReference(String context, String type, String id, String container) {
//        if (StringUtils.isBlank(context)) return "";
//        String reference = Entity.SEPARATOR;
//
//        switch (type) {
//            case "s":
//                // submission type
//                reference = reference + "s";
//                break;
//            case "grades":
//                // grades type
//                reference = reference + "grades";
//                break;
//            case "submissions":
//                // submissions type
//                reference = reference + "submissions";
//                break;
//            case "a":
//                // assignment type
//            case "c":
//                // assignment content type
//                // deprecated using assignment type
//            default:
//                // using assignment type as default when no matching type found
//                reference = reference + "a";
//        }
//
//        reference = reference + Entity.SEPARATOR + context;
//
//        if (StringUtils.isNotBlank(id)) {
//            if ("s".equals(type)) {
//                if (StringUtils.isNotBlank(container)) {
//                    reference = Entity.SEPARATOR + container;
//                }
//            }
//            reference = Entity.SEPARATOR + id;
//        }
//
//        return reference;
//    }

    public void updateReferenceWithStringReference(String stringReference, Reference reference) {
        AssignmentReferenceReckoner.AssignmentReference reckoner = AssignmentReferenceReckoner.reckoner().reference(stringReference).reckon();
        // update the reference
        reference.set(APPLICATION_ID, reckoner.getSubtype(), reckoner.getId(), reckoner.getContainer(), reckoner.getContext());
    }
}

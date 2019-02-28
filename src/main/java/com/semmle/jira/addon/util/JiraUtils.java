package com.semmle.jira.addon.util;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.semmle.jira.addon.workflow.LgtmDismissAlertFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class JiraUtils {

  private static final String COM_LGTM_NOTIFICATION = "com.lgtm.notification";

  public static void addIssueTypeToProject(Project project, IssueType newIssueType) {
    IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();

    Collection<IssueType> currentIssueTypes =
        issueTypeSchemeManager.getIssueTypesForProject(project);
    Set<String> issueTypeIds = new LinkedHashSet<String>();
    for (IssueType issueType : currentIssueTypes) {
      issueTypeIds.add(issueType.getId());
    }

    issueTypeIds.add(newIssueType.getId());

    FieldConfigScheme issueTypeScheme = issueTypeSchemeManager.getConfigScheme(project);
    issueTypeSchemeManager.update(issueTypeScheme, issueTypeIds);
  }

  public static IssueType getIssueTypeByName(String issueTypeName) {
    ComponentAccessor.getConstantsManager();
    return (IssueType)
        ComponentAccessor.getConstantsManager()
            .getIssueConstantByName(
                ConstantsManager.CONSTANT_TYPE.ISSUE_TYPE.getType(), issueTypeName);
  }

  public static void addWorkflowToProject(
      Project project, JiraWorkflow workflow, IssueType issueType) throws GenericEntityException {
    WorkflowSchemeManager workflowSchemeManager = ComponentAccessor.getWorkflowSchemeManager();
    GenericValue workflowScheme = workflowSchemeManager.getWorkflowScheme(project);
    workflowSchemeManager.addWorkflowToScheme(
        workflowScheme, workflow.getName(), issueType.getId());
  }

  public static void addPostFunctionsToWorkflow(JiraWorkflow workflow) {
    FunctionDescriptor suppress = LgtmDismissAlertFactory.suppress();
    suppress.setName(COM_LGTM_NOTIFICATION);
    FunctionDescriptor unsuppress = LgtmDismissAlertFactory.unsuppress();
    unsuppress.setName(COM_LGTM_NOTIFICATION);

    for (ActionDescriptor transition : workflow.getAllActions()) {
      if (Constants.WORKFLOW_USER_SUPPRESS_TRANSITION_NAME.equalsIgnoreCase(transition.getName())) {
        addFunction(transition, suppress);
      } else if (Constants.WORKFLOW_USER_UNSUPPRESS_TRANSITION_NAME.equalsIgnoreCase(
              transition.getName())
          || Constants.WORKFLOW_USER_UNSUPPRESS_TRANSITION_NAME.equalsIgnoreCase(
              transition.getName())) {
        addFunction(transition, unsuppress);
      }
    }
  }

  private static void addFunction(ActionDescriptor transition, FunctionDescriptor function) {
    @SuppressWarnings("unchecked")
    List<FunctionDescriptor> functions = transition.getUnconditionalResult().getPostFunctions();
    // remove existing functions with the same name
    String name = function.getName();
    if (name != null) {
      Iterator<?> iter = functions.iterator();
      while (iter.hasNext()) {
        Object obj = iter.next();
        if (obj instanceof FunctionDescriptor) {
          FunctionDescriptor fun = (FunctionDescriptor) obj;
          if (name.equals(fun.getName())) {
            iter.remove();
          }
        }
      }
    }
    functions.add(function);
  }

  public static IssueType getLgtmIssueType() {
    return getIssueTypeByName(Constants.ISSUE_TYPE_NAME);
  }

  public static Status getLgtmWorkflowStatus(JiraWorkflow workflow, String statusName)
      throws StatusNotFoundException {

    List<Status> allStatuses = workflow.getLinkedStatusObjects();
    for (Status status : allStatuses) {
      if (status.getName().equals(statusName)) {
        return status;
      }
    }

    throw new StatusNotFoundException();
  }

  public static JiraWorkflow getLgtmWorkflow() throws WorkflowNotFoundException {
    WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
    JiraWorkflow workflow = workflowManager.getWorkflow(Constants.WORKFLOW_NAME);
    if (workflow == null) {
      throw new WorkflowNotFoundException();
    }
    return workflow;
  }
}

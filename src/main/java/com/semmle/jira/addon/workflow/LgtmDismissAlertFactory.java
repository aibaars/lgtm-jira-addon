package com.semmle.jira.addon.workflow;

import java.util.LinkedHashMap;
import java.util.Map;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import com.semmle.jira.addon.Request.Transition;

/**
 * This is the factory class responsible for dealing with the UI for the post-function. This is
 * typically where you put default values into the velocity context and where you store user input.
 */
public class LgtmDismissAlertFactory extends AbstractWorkflowPluginFactory
    implements WorkflowPluginFunctionFactory {

  private static final Transition DEFAULT_TRANSITION = Transition.SUPPRESS;

  public LgtmDismissAlertFactory() {}

  @Override
  protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
    velocityParams.put(LgtmDismissAlert.FIELD_TRANSITION, DEFAULT_TRANSITION.value);
  }

  @Override
  protected void getVelocityParamsForEdit(
      Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
    getVelocityParamsForInput(velocityParams);
    getVelocityParamsForView(velocityParams, descriptor);
  }

  @Override
  protected void getVelocityParamsForView(
      Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
    if (!(descriptor instanceof FunctionDescriptor)) {
      throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
    }

    FunctionDescriptor function = (FunctionDescriptor) descriptor;

    String transition = (String) function.getArgs().get(LgtmDismissAlert.FIELD_TRANSITION);
    if (transition == null) transition = DEFAULT_TRANSITION.value;
    velocityParams.put(LgtmDismissAlert.FIELD_TRANSITION, transition);
  }

  public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
    Map<String, String> params = new LinkedHashMap<>();

    String transition = extractSingleParam(formParams, LgtmDismissAlert.FIELD_TRANSITION);
    if (transition == null) transition = DEFAULT_TRANSITION.value;
    params.put(LgtmDismissAlert.FIELD_TRANSITION, transition);

    return params;
  }

  private static FunctionDescriptor forTransition(Transition transition) {
    FunctionDescriptor result = DescriptorFactory.getFactory().createFunctionDescriptor();
    result.setType("class");
    @SuppressWarnings("unchecked")
    Map<String, String> args = result.getArgs();
    args.put("full.module.key", LgtmDismissAlert.FULL_MODULE_KEY);
    args.put("class.name", LgtmDismissAlert.class.getName());
    args.put(LgtmDismissAlert.FIELD_TRANSITION, transition.value);
    return result;
  }

  public static FunctionDescriptor suppress() {
    return forTransition(Transition.SUPPRESS);
  }

  public static FunctionDescriptor unsuppress() {
    return forTransition(Transition.UNSUPPRESS);
  }
}

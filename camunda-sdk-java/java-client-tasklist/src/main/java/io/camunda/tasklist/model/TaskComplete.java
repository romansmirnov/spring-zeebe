package io.camunda.tasklist.model;

import java.util.List;

public class TaskComplete {
  private List<VariableInput> variables;

  public List<VariableInput> getVariables() {
    return variables;
  }

  public void setVariables(List<VariableInput> variables) {
    this.variables = variables;
  }

}

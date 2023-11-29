package io.camunda.tasklist.model;

import java.time.OffsetDateTime;
import java.util.List;

public class TaskSearchResult extends Task {
  private TaskState taskState;
  private List<String> sortValues;
  private Boolean isFirst;
  public TaskState getTaskState() {
    return taskState;
  }

  public void setTaskState(TaskState taskState) {
    this.taskState = taskState;
  }

  public List<String> getSortValues() {
    return sortValues;
  }

  public void setSortValues(List<String> sortValues) {
    this.sortValues = sortValues;
  }

  public Boolean getFirst() {
    return isFirst;
  }

  public void setFirst(Boolean first) {
    isFirst = first;
  }
}

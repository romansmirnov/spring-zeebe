package io.camunda.tasklist.model;

public enum Field {
  COMPLETION_TIME,
  CREATION_TIME,
  FOLLOW_UP_DATE,
  DUE_DATE;

  public String nameLowercase() {
    return name().toLowerCase();
  }
}

package io.camunda.tasklist.model;

public enum Operator {
  EQ;

  public String nameLowercase() {
    return name().toLowerCase();
  }
}

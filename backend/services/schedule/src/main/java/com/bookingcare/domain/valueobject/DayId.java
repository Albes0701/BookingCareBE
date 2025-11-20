package com.bookingcare.domain.valueobject;

public enum DayId {
  MON("Monday"),
  TUE("Tuesday"),
  WED("Wednesday"),
  THU("Thursday"),
  FRI("Friday"),
  SAT("Saturday"),
  SUN("Sunday"); 

  private String displayName;

  DayId(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}

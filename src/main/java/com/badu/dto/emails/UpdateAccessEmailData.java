package com.badu.dto.emails;

public class UpdateAccessEmailData {
  public String chatName;
  public boolean isRevoked;
  public String role;
  public String customerName;

  public UpdateAccessEmailData() {
  }

  public UpdateAccessEmailData(String chatName, boolean isRevoked, String role, String customerName) {
    this.chatName = chatName;
    this.isRevoked = isRevoked;
    this.role = role;
    this.customerName = customerName;
  }
}

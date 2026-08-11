package com.jerzymaj.major.exceptions;

public class WebhookEventNotFoundException extends RuntimeException {
  public WebhookEventNotFoundException(String message) {
    super(message);
  }
}

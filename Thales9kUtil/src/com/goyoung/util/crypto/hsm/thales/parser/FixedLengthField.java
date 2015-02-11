package com.goyoung.util.crypto.hsm.thales.parser;

public class FixedLengthField
  implements Field
{
  private final int length;
  private String value;
  
  public FixedLengthField(int length)
  {
    this.length = length;
  }
  
  public int getLength()
  {
    return this.length;
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public int parse(String message)
  {
    if (message.length() < this.length) {
      throw new IllegalArgumentException("Insufficient data provided - need " + this.length + " characters from value: " + this.value);
    }
    this.value = message.substring(0, this.length);
    return this.length;
  }
  
  public void setValue(String value)
  {
    if (value.length() != this.length) {
      throw new IllegalArgumentException("Invalid value specified: " + value);
    }
    this.value = value;
  }
}

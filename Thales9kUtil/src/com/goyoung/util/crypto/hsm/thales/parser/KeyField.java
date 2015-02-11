package com.goyoung.util.crypto.hsm.thales.parser;

public class KeyField
  implements Field
{
  private final boolean allowUnprefixedDoubleLengthKeys;
  private int length;
  private String value;
  
  public KeyField()
  {
    this.allowUnprefixedDoubleLengthKeys = false;
  }
  
  public KeyField(boolean allowUnprefixedDoubleLengthKeys)
  {
    this.allowUnprefixedDoubleLengthKeys = allowUnprefixedDoubleLengthKeys;
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
    this.length = determineKeyLength(message);
    this.value = message.substring(0, this.length);
    return this.length;
  }
  
  public void setValue(String value)
  {
    int length = value.length();
    if (length != determineKeyLength(value)) {
      throw new IllegalArgumentException("Invalid key value specified: " + value);
    }
    this.value = value;
  }
  
  private int determineKeyLength(String data)
  {
    String keyScheme = data.substring(0, 1);
    if ("U".equals(keyScheme)) {
      return 33;
    }
    if ("T".equals(keyScheme)) {
      return 49;
    }
    if ("X".equals(keyScheme)) {
      return 33;
    }
    if ("Y".equals(keyScheme)) {
      return 49;
    }
    if (!this.allowUnprefixedDoubleLengthKeys) {
      return 16;
    }
    if (data.length() >= 32) {
      return 32;
    }
    return 16;
  }
}

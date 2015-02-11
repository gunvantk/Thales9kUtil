package com.goyoung.util.crypto.hsm.thales.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

@SuppressWarnings("unused")
public class Message
{
  private static final int COMMAND_CODE_LENGTH = 2;
  private final Logger log = Logger.getLogger(getClass());
  private final Map<String, Field> fields = new LinkedHashMap<String, Field>();
  private final String commandCode;
  private final int headerLength;
  
  public Message(String commandCode, int headerLength)
  {
    if ((commandCode == null) || (commandCode.length() != 2)) {
      throw new IllegalArgumentException("Invalid command code: " + commandCode);
    }
    this.commandCode = commandCode;
    this.headerLength = headerLength;
  }
  
  public String getCommandCode()
  {
    return this.commandCode;
  }
  
  public void addField(String name, Field field)
  {
    this.fields.put(name, field);
  }
  
  public void parse(String message)
  {
    String work = message;
    String header = work.substring(0, this.headerLength);
    work = work.substring(this.headerLength);
    String commandCode = work.substring(0, 2);
    if (!this.commandCode.equals(commandCode)) {
      throw new RuntimeException("Invalid command code in message data - expected '" + this.commandCode + "' but found '" + commandCode + "'");
    }
    work = work.substring(2);
    for (Field field : this.fields.values())
    {
      int length = field.parse(work);
      work = work.substring(length);
      if (work.length() == 0) {
        break;
      }
    }
  }
  
  public String build()
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.headerLength; i++) {
      sb.append("x");
    }
    return build(sb.toString());
  }
  
  public String build(String header)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(header);
    sb.append(this.commandCode);
    for (Field field : this.fields.values()) {
      if (field.getValue() != null) {
        sb.append(field.getValue());
      }
    }
    this.log.trace("Message: " + sb.toString());
    return sb.toString();
  }
  
  public void setFieldValue(String fieldName, String value)
  {
    getField(fieldName).setValue(value);
  }
  
  public String getFieldValue(String fieldName)
  {
    return getField(fieldName).getValue();
  }
  
  private Field getField(String fieldName)
  {
    Field field = this.fields.get(fieldName);
    if (field == null) {
      throw new RuntimeException("Undefined field: " + fieldName);
    }
    return field;
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("Message Code:");
    sb.append(this.commandCode);
    for (Map.Entry<String, Field> entry : this.fields.entrySet())
    {
      sb.append(",");
      sb.append((String)entry.getKey());
      sb.append(":");
      sb.append(((Field)entry.getValue()).getValue());
    }
    return sb.toString();
  }
}

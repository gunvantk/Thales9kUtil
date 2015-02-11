package com.goyoung.util.crypto.hsm.thales.parser;

public class MessageFactory
{
  private static int headerLength = 4;
  
  public static int getHeaderLength()
  {
    return headerLength;
  }
  
  public static void setHeaderLength(int headerLength)
  {
    headerLength = MessageFactory.headerLength;
  }
  
  public static Message create(String commandCode)
  {
    Message m = new Message(commandCode, headerLength);
    addFields(m);
    return m;
  }
  
  private static void addFields(Message m)
  {
    String code = m.getCommandCode();
    if ("A6".equals(code))
    {
      m.addField("keyType", new FixedLengthField(3));
      m.addField("zmk", new KeyField(true));
      m.addField("key", new KeyField());
      m.addField("keyScheme", new FixedLengthField(1));
    }
    else if ("A7".equals(code))
    {
      m.addField("errorCode", new FixedLengthField(2));
      m.addField("key", new KeyField());
      m.addField("kcv", new FixedLengthField(6));
    }
    else if ("BU".equals(code))
    {
      m.addField("keyType", new FixedLengthField(2));
      m.addField("keyLength", new FixedLengthField(1));
      m.addField("key", new KeyField());
    }
    else if ("BV".equals(code))
    {
      m.addField("errorCode", new FixedLengthField(2));
      m.addField("kcv", new FixedLengthField(6));
    }
  }
}

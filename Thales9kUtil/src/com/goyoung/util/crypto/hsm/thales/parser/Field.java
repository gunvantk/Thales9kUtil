package com.goyoung.util.crypto.hsm.thales.parser;

public abstract interface Field
{
  public abstract int getLength();
  
  public abstract int parse(String paramString);
  
  public abstract void setValue(String paramString);
  
  public abstract String getValue();
}

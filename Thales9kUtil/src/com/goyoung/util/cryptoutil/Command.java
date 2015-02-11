package com.goyoung.util.cryptoutil;

abstract interface Command
{
  public abstract void execute(String[] paramArrayOfString)
    throws Exception;
  
  public abstract void showUsage();
}

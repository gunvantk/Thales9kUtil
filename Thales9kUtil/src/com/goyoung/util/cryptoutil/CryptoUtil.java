package com.goyoung.util.cryptoutil;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class CryptoUtil
{
  static final Logger TRAN_LOG = Logger.getLogger("tran-trace");
  private static final Logger LOG = Logger.getLogger(CryptoUtil.class);
  @SuppressWarnings("rawtypes")
private static final Map<String, Class> COMMAND_MAP = new HashMap<String, Class>();
  
  static
  {
    COMMAND_MAP.put("convert", ConvertCommand.class);
    COMMAND_MAP.put("convkeys", ConvKeysCommand.class);
    COMMAND_MAP.put("genkey", GenKeyCommand.class);
    COMMAND_MAP.put("genpin", GenPINCommand.class);
    COMMAND_MAP.put("gencvv", GenCVVCommand.class);
    COMMAND_MAP.put("runcmd", RunCmdCommand.class);
   
  }
  
  @SuppressWarnings("rawtypes")
public static void main(String[] args)
    throws Exception
  {
    if (args.length == 0)
    {
      showUsage();
      return;
    }
    String command = null;
    

    command = args[0];
    Class commandClass = COMMAND_MAP.get(command);
    LOG.debug("Command: " + command + " Class: " + commandClass);
    if (commandClass == null)
    {
      System.out.println("Invalid command: " + command);
      showUsage();
      return;
    }
    Command theCommand = (Command)commandClass.newInstance();
    
    String[] theArgs = new String[args.length - 1];
    System.arraycopy(args, 1, theArgs, 0, theArgs.length);
    try
    {
      theCommand.execute(theArgs);
    }
    catch (Exception e)
    {
      LOG.error("Terminating due to error: " + e.getMessage());
      System.out.println("Terminating due to error: " + e.getMessage());
    }
  }
  
  private static void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar <command> <options>");
    out.println("Commands - use command -? for help:");
    out.println(" convert  - convert key file");
    out.println(" convkeys - convert key file");
    out.println(" genkey   - generate keys");
    out.println(" genpin   - generate PIN");
    out.println(" gencvc   - generate CVC");
    out.println(" runcmd   - run arbitrary command");
    out.println(" cuets    - process cuets key file");
  }
}

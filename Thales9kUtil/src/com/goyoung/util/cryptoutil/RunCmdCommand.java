package com.goyoung.util.cryptoutil;

import java.io.PrintStream;
import org.apache.log4j.Logger;

class RunCmdCommand
  implements Command
{
  private static final Logger LOG = Logger.getLogger(RunCmdCommand.class);
  
  public void execute(String[] args)
    throws Exception
  {
    String cmd = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-cmd"))
      {
        cmd = args[(++i)];
      }
      else
      {
        if (args[i].equals("-?"))
        {
          showUsage();
          return;
        }
        System.out.println("Invalid argument: " + args[i]);
        showUsage();
        return;
      }
    }
    if (cmd == null)
    {
      showUsage();
      return;
    }
    runCmd(cmd);
  }
  
  public void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar runcmd <options>");
    out.println("- Sends a command to the HSM and displays the response.");
    out.println("Options:");
    out.println("-cmd <cmd> - The command data.");
  }
  
  private void runCmd(String cmd)
    throws Exception
  {
    String reqMsg = cmd;
    
    String respMsg = HSMIO.sendHSMCommand(reqMsg);
    
    CryptoUtil.TRAN_LOG.info("Request: " + reqMsg + " Response: " + respMsg);
    
    LOG.debug("Request: " + reqMsg + " Response: " + respMsg);
    
    System.out.println("HSM Response: " + respMsg);
  }
}

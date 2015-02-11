package com.goyoung.util.cryptoutil;

import java.io.PrintStream;

import org.apache.log4j.Logger;
@SuppressWarnings("unused")
class GenCVVCommand
  implements Command
{
  private static final String REQ_HEADER = "xxxx";
  private static final String REQ_COMMAND = "CW";
  private static final String CMD_DELIM = ";";
  private static final String RESP_COMMAND = "CX";
  private static final Logger LOG = Logger.getLogger(GenCVVCommand.class);
  
  public void execute(String[] args)
    throws Exception
  {
    String cvka = null;
    String cvkb = null;
    String account = null;
    String expDate = null;
    String svcCode = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-cvka"))
      {
        cvka = args[(++i)];
      }
      else if (args[i].equals("-cvkb"))
      {
        cvkb = args[(++i)];
      }
      else if (args[i].equals("-account"))
      {
        account = args[(++i)];
      }
      else if (args[i].equals("-exp"))
      {
        expDate = args[(++i)];
      }
      else if (args[i].equals("-svc"))
      {
        svcCode = args[(++i)];
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
    if ((cvka == null) || (cvkb == null) || (account == null) || (expDate == null))
    {
      showUsage();
      return;
    }
    genCVV(cvka, cvkb, account, expDate, svcCode);
  }
  
  public void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar gencvv <options>");
    out.println("- Generates and prints the CVV for the account.");
    out.println("Options:");
    out.println("-cvka <cvka>              - Card Verification Key A");
    out.println("-cvkb <cvka>              - Card Verification Key B");
    out.println("-account <account_number> - Account Number");
    out.println("-exp <expiry_date>        - Card Expiry Date (MMYY)");
    out.println("-svc <service_code>       - Service Code (CVV = 101, CVV2 = 000)");
  }
  
  private void genCVV(String cvka, String cvkb, String account, String expDate, String svcCode)
    throws Exception
  {
    if (svcCode == null) {
      svcCode = "101";
    }
    StringBuffer reqBuf = new StringBuffer();
    reqBuf.append("xxxx");
    reqBuf.append("CW");
    reqBuf.append(cvka);
    reqBuf.append(cvkb);
    reqBuf.append(account);
    reqBuf.append(";");
    reqBuf.append(expDate);
    reqBuf.append(svcCode);
    String reqMsg = reqBuf.toString();
    
    String respMsg = HSMIO.sendHSMCommand(reqMsg);
    
    CryptoUtil.TRAN_LOG.info("Request: " + reqMsg + " Response: " + respMsg);
    
    LOG.debug("Request: " + reqMsg + " Response: " + respMsg);
    
    int pos = "xxxx".length();
    String respCommand = respMsg.substring(pos, pos + 2);
    pos += 2;
    String errorCode = respMsg.substring(pos, pos + 2);
    pos += 2;
    if (("CX".equals(respCommand)) && ("00".equals(errorCode)))
    {
      LOG.info("CVV: " + respMsg.substring(pos));
      System.out.println("CVV: " + respMsg.substring(pos));
    }
    else
    {
      LOG.error("Bad return code from HSM");
      LOG.error("CVKA:      " + cvka);
      LOG.error("CVKB:      " + cvkb);
      LOG.error("Account:  " + account);
      LOG.error("Exp Date: " + expDate);
      LOG.error("Request:  " + reqMsg);
      LOG.error("Response: " + respMsg);
      System.out.println("Bad return code from HSM");
      System.out.println("CVKA:      " + cvka);
      System.out.println("CVKB:      " + cvkb);
      System.out.println("Account:  " + account);
      System.out.println("Exp Date: " + expDate);
      System.out.println("Request:  " + reqMsg);
      System.out.println("Response: " + respMsg);
      throw new Exception("Bad return code from HSM");
    }
  }
}

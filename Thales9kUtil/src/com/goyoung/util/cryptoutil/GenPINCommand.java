package com.goyoung.util.cryptoutil;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

@SuppressWarnings("unused")
class GenPINCommand
  implements Command
{
  private static final String REQ_HEADER = "xxxx";
  private static final String GEN_REQ_COMMAND = "EE";
  private static final String OFFSET_PADDING = "FFFFFFFF";
  private static final String CHECK_LENGTH = "04";
  private static final String DEC_TABLE = "0123456789012345";
  private static final String GEN_RESP_COMMAND = "EF";
  private static final String DECRYPT_REQ_COMMAND = "NG";
  private static final String DECRYPT_RESP_COMMAND = "NH";
  private static final Logger LOG = Logger.getLogger(GenPINCommand.class);
  
  public void execute(String[] args)
    throws Exception
  {
    String pvk = null;
    String offset = null;
    String account = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-pvk"))
      {
        pvk = args[(++i)];
      }
      else if (args[i].equals("-offset"))
      {
        offset = args[(++i)];
      }
      else if (args[i].equals("-account"))
      {
        account = args[(++i)];
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
    if ((pvk == null) || (offset == null) || (account == null))
    {
      showUsage();
      return;
    }
    genPIN(pvk, offset, account);
  }
  
  public void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar genpin <options>");
    out.println("- Generates and prints the *clear* PIN for the account.");
    out.println("Options:");
    out.println("-pvk <pvk>                - PIN Verification Key");
    out.println("-offset <offset>          - PIN Offset");
    out.println("-account <account_number> - Account Number");
  }
  
  private void genPIN(String pvk, String offset, String account)
    throws Exception
  {
    String encryptedPIN = getEncryptedPIN(pvk, offset, account);
    Map<String, String> result = decryptPIN(account, encryptedPIN);
    LOG.info("PIN: " + result.get("pin") + " Ref. Num: " + result.get("reference-number"));
    
    System.out.println("PIN: " + result.get("pin") + " Ref. Num: " + result.get("reference-number"));
  }
  
  private String getEncryptedPIN(String pvk, String offset, String account)
    throws Exception
  {
    StringBuffer reqBuf = new StringBuffer();
    reqBuf.append("xxxx");
    reqBuf.append("EE");
    reqBuf.append(pvk);
    reqBuf.append(offset + "FFFFFFFF");
    reqBuf.append("04");
    reqBuf.append(getAccount12(account));
    reqBuf.append("0123456789012345");
    reqBuf.append(getValidationData(account));
    String reqMsg = reqBuf.toString();
    
    String respMsg = HSMIO.sendHSMCommand(reqMsg);
    
    CryptoUtil.TRAN_LOG.info("Request: " + reqMsg + " Response: " + respMsg);
    
    LOG.debug("Request: " + reqMsg + " Response: " + respMsg);
    
    int pos = "xxxx".length();
    String respCommand = respMsg.substring(pos, pos + 2);
    pos += 2;
    String errorCode = respMsg.substring(pos, pos + 2);
    pos += 2;
    if (("EF".equals(respCommand)) && ("00".equals(errorCode))) {
      return respMsg.substring(pos);
    }
    LOG.error("Bad return code from HSM");
    LOG.error("PVK:      " + pvk);
    LOG.error("Offset:   " + offset);
    LOG.error("Account:  " + account);
    LOG.error("Request:  " + reqMsg);
    LOG.error("Response: " + respMsg);
    System.out.println("Bad return code from HSM");
    System.out.println("PVK:      " + pvk);
    System.out.println("Offset:   " + offset);
    System.out.println("Account:  " + account);
    System.out.println("Request:  " + reqMsg);
    System.out.println("Response: " + respMsg);
    throw new Exception("Bad return code from HSM");
  }
  
  private Map<String, String> decryptPIN(String account, String encryptedPIN)
    throws Exception
  {
    StringBuffer reqBuf = new StringBuffer();
    reqBuf.append("xxxx");
    reqBuf.append("NG");
    reqBuf.append(getAccount12(account));
    reqBuf.append(encryptedPIN);
    String reqMsg = reqBuf.toString();
    
    String respMsg = HSMIO.sendHSMCommand(reqMsg);
    
    CryptoUtil.TRAN_LOG.info("Request: " + reqMsg + " Response: " + respMsg);
    
    LOG.debug("Request: " + reqMsg + " Response: " + respMsg);
    
    int pos = "xxxx".length();
    String respCommand = respMsg.substring(pos, pos + 2);
    pos += 2;
    String errorCode = respMsg.substring(pos, pos + 2);
    pos += 2;
    if (("NH".equals(respCommand)) && ("00".equals(errorCode)))
    {
      Map<String, String> result = new HashMap<String, String>();
      result.put("pin", respMsg.substring(pos, pos + 4));
      pos += 5;
      result.put("reference-number", respMsg.substring(pos));
      return result;
    }
    LOG.error("Bad return code from HSM");
    LOG.error("Account:  " + account);
    LOG.error("Enc PIN:  " + encryptedPIN);
    LOG.error("Request:  " + reqMsg);
    LOG.error("Response: " + respMsg);
    System.out.println("Bad return code from HSM");
    System.out.println("Account:  " + account);
    System.out.println("Enc PIN:  " + encryptedPIN);
    System.out.println("Request:  " + reqMsg);
    System.out.println("Response: " + respMsg);
    throw new Exception("Bad return code from HSM");
  }
  
  private String getAccount12(String account)
  {
    return account.substring(account.length() - 13, account.length() - 1);
  }
  
  private String getValidationData(String account)
  {
    return account.substring(0, 10) + "N" + account.charAt(account.length() - 1);
  }
}

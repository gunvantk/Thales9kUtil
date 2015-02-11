package com.goyoung.util.cryptoutil;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

@SuppressWarnings("unused")
class GenKeyCommand
  implements Command
{
  private static final String REQ_HEADER = "xxxx";
  private static final String REQ_COMMAND = "A0";
  private static final String RESP_COMMAND = "A1";
  private static final Logger LOG = Logger.getLogger(GenKeyCommand.class);
  
  public void execute(String[] args)
    throws Exception
  {
    String keyType = "009";
    String keyScheme = "U";
    boolean encryptUnderZMK = false;
    String zmk = null;
    String zmkScheme = null;
    String outFileName = null;
    String kcvOutFileName = null;
    int count = 1;
    int startSeq = 1;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-keytype"))
      {
        keyType = args[(++i)];
      }
      else if (args[i].equals("-keyscheme"))
      {
        keyScheme = args[(++i)];
      }
      else if (args[i].equals("-zmkenc"))
      {
        encryptUnderZMK = true;
      }
      else if (args[i].equals("-zmk"))
      {
        zmk = args[(++i)];
      }
      else if (args[i].equals("-zmkscheme"))
      {
        zmkScheme = args[(++i)];
      }
      else if (args[i].equals("-out"))
      {
        outFileName = args[(++i)];
      }
      else if (args[i].equals("-kcvout"))
      {
        kcvOutFileName = args[(++i)];
      }
      else if (args[i].equals("-count"))
      {
        try
        {
          count = Integer.parseInt(args[(++i)]);
        }
        catch (NumberFormatException e)
        {
          showUsage();
          return;
        }
      }
      else if (args[i].equals("-startseq"))
      {
        try
        {
          startSeq = Integer.parseInt(args[(++i)]);
        }
        catch (NumberFormatException e)
        {
          showUsage();
          return;
        }
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
    if ((encryptUnderZMK) && ((zmk == null) || (zmkScheme == null)))
    {
      showUsage();
      return;
    }
    genKeys(keyType, keyScheme, encryptUnderZMK, zmk, zmkScheme, outFileName, kcvOutFileName, count, startSeq);
  }
  
  public void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar genkey <options>");
    out.println("- Generates one or more keys, optionally exporting them");
    out.println("- under a ZMK.");
    out.println("Options:");
    out.println("-keytype <keytype>        - 3 digit key type - defaults to 009");
    
    out.println("-keyscheme <keyscheme>    - key scheme - defaults to U");
    out.println("-zmkenc                   - also encrypt under ZMK");
    out.println("-zmk <zmkey>              - ZMK to encrypt under");
    out.println("-zmkscheme <zmkeyscheme>  - key scheme for encrypting key for under ZMK");
    
    out.println("-out <filename>           - filename for key output");
    out.println("-kcvout <filename>        - filename for key output with KCVs");
    
    out.println("-count <# of keys>        - number of keys to generate - defaults to 1");
    
    out.println("-startseq <start #>       - starting sequence number - defaults to 1");
  }
  
  private void genKeys(String keyType, String keyScheme, boolean encryptUnderZMK, String zmk, String zmkScheme, String outFileName, String kcvOutFileName, int count, int startSeq)
    throws Exception
  {
    PrintWriter out = null;
    PrintWriter kcvout = null;
    boolean showFeedback = outFileName != null;
    if (outFileName == null) {
      out = new PrintWriter(System.out, true);
    } else {
      out = new PrintWriter(new FileOutputStream(outFileName), true);
    }
    if (kcvOutFileName != null)
    {
      kcvout = new PrintWriter(new FileOutputStream(kcvOutFileName));
      kcvout.println("Parameters");
      kcvout.println("----------");
      kcvout.println("Key Type: " + keyType + ", Key Scheme: " + keyScheme);
      
      kcvout.println("Count: " + count + ", Start Seq: " + startSeq);
      if (encryptUnderZMK) {
        kcvout.println("ZMK: " + zmk + ", ZMK Key Scheme: " + zmkScheme);
      }
      kcvout.println("Key Output: " + (outFileName == null ? "stdout" : outFileName));
      
      kcvout.println();
    }
    try
    {
      for (int currentSeq = startSeq; currentSeq < startSeq + count; currentSeq++)
      {
        String respMsg = genKey(keyType, keyScheme, encryptUnderZMK, zmk, zmkScheme);
        

        int pos = "xxxx".length() + 4;
        String keyLMK = respMsg.substring(pos, pos + getKeyLength(keyScheme));
        
        pos += getKeyLength(keyScheme);
        String keyZMK = null;
        if (encryptUnderZMK)
        {
          keyZMK = respMsg.substring(pos, pos + getKeyLength(zmkScheme));
          
          pos += getKeyLength(zmkScheme);
        }
        String kcv = respMsg.substring(pos, pos + 6);
        pos += 6;
        if (showFeedback) {
          System.out.print(".");
        }
        if (out != null) {
          out.println(keyLMK + (encryptUnderZMK ? "," + keyZMK : ""));
        }
        if (kcvout != null) {
          kcvout.println(currentSeq + "," + keyLMK + (encryptUnderZMK ? "," + keyZMK : "") + "," + kcv);
        }
      }
    }
    finally
    {
      if (showFeedback) {
        System.out.println();
      }
      if (out != null)
      {
        out.flush();
        out.close();
      }
      if (kcvout != null)
      {
        kcvout.flush();
        kcvout.close();
      }
    }
  }
  
  private int getKeyLength(String keyScheme)
    throws Exception
  {
    if ("Z".equals(keyScheme)) {
      return 16;
    }
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
    throw new Exception("Invalid key scheme: " + keyScheme);
  }
  
  private String genKey(String keyType, String keyScheme, boolean encryptUnderZMK, String zmk, String zmkScheme)
    throws Exception
  {
    StringBuffer reqBuf = new StringBuffer();
    reqBuf.append("xxxx");
    reqBuf.append("A0");
    reqBuf.append(encryptUnderZMK ? "1" : "0");
    reqBuf.append(keyType);
    reqBuf.append(keyScheme);
    if (encryptUnderZMK)
    {
      reqBuf.append(zmk);
      reqBuf.append(zmkScheme);
    }
    String reqMsg = reqBuf.toString();
    
    String respMsg = HSMIO.sendHSMCommand(reqMsg);
    
    CryptoUtil.TRAN_LOG.info("Request: " + reqMsg + " Response: " + respMsg);
    
    LOG.debug("Request: " + reqMsg + " Response: " + respMsg);
    
    int pos = "xxxx".length();
    String respCommand = respMsg.substring(pos, pos + 2);
    pos += 2;
    String errorCode = respMsg.substring(pos, pos + 2);
    pos += 2;
    if (("A1".equals(respCommand)) && ("00".equals(errorCode))) {
      return respMsg;
    }
    LOG.error("Bad return code from HSM - " + errorCode);
    LOG.error("Request:  " + reqMsg);
    LOG.error("Response: " + respMsg);
    System.out.println("Bad return code from HSM - " + errorCode);
    System.out.println("Request:  " + reqMsg);
    System.out.println("Response: " + respMsg);
    throw new Exception("Bad return code from HSM");
  }
}

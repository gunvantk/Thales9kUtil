package com.goyoung.util.cryptoutil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

@SuppressWarnings("unused")
abstract class FileConversionCommand
  implements Command
{

private static final String REQ_HEADER = "xxxx";
private static final String REQ_COMMAND = "BW";
private static final String REQ_KEY_SINGLE = "0";
private static final String REQ_KEY_DOUBLE = "1";
private static final String REQ_KEY_TRIPLE = "3";
private static final String RESP_COMMAND = "BX";
  
static final Logger LOG = Logger.getLogger(FileConversionCommand.class);
  
  public void execute(String[] args)
    throws Exception
  {
    String inputFileName = null;
    String outputFileName = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-in"))
      {
        inputFileName = args[(++i)];
      }
      else if (args[i].equals("-out"))
      {
        outputFileName = args[(++i)];
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
    if ((inputFileName == null) || (outputFileName == null))
    {
      System.out.println("Invalid arguments provided");
      showUsage();
      return;
    }
    convert(inputFileName, outputFileName);
  }
  
  public abstract void showUsage();
  
  protected void convert(String inputFileName, String outputFileName)
    throws Exception
  {
    System.out.println("Convert process starting.");
    System.out.println("Input:  " + inputFileName);
    System.out.println("Output: " + outputFileName);
    LOG.info("Convert process starting.");
    LOG.info("Input:  " + inputFileName);
    LOG.info("Output: " + outputFileName);
    
    BufferedReader in = new BufferedReader(new FileReader(inputFileName));
    PrintWriter out = new PrintWriter(new FileWriter(outputFileName));
    int lineCount = 0;
    int validLineCount = 0;
    int invalidLineCount = 0;
    String line;
    while ((line = in.readLine()) != null)
    {
      lineCount++;
      String output = convertLine(line);
      if ((output == null) || (output.length() == 0))
      {
        invalidLineCount++;
      }
      else
      {
        out.println(output);
        out.flush();
        validLineCount++;
      }
    }
    out.flush();
    out.close();
    in.close();
    System.out.println("Convert process ending. Records read: " + lineCount + ", processed: " + validLineCount + ", rejected: " + invalidLineCount);
    

    LOG.info("Convert process ending. Records read: " + lineCount + ", processed: " + validLineCount + ", rejected: " + invalidLineCount);
  }
  
  protected abstract String convertLine(String paramString)
    throws Exception;
  
  protected String reencryptKey(String keyType, String key)
    throws Exception
  {
    String keyLenInd = "0";
    int keyLen = key.length();
    if ((keyLen == 32) || (keyLen == 33)) {
      keyLenInd = "1";
    } else if (keyLen == 49) {
      keyLenInd = "3";
    }
    StringBuffer reqBuf = new StringBuffer();
    reqBuf.append("xxxx");
    reqBuf.append("BW");
    reqBuf.append(keyType);
    reqBuf.append(keyLenInd);
    reqBuf.append(key);
    String reqMsg = reqBuf.toString();
    
    String respMsg = HSMIO.sendHSMCommand(reqMsg);
    
    CryptoUtil.TRAN_LOG.info("Request: " + reqMsg + " Response: " + respMsg);
    
    LOG.debug("Request: " + reqMsg + " Response: " + respMsg);
    
    String newKey = null;
    
    int pos = "xxxx".length();
    String respCommand = respMsg.substring(pos, pos + 2);
    pos += 2;
    String errorCode = respMsg.substring(pos, pos + 2);
    pos += 2;
    if (("BX".equals(respCommand)) && ("00".equals(errorCode)))
    {
      newKey = respMsg.substring(pos);
    }
    else
    {
      LOG.warn("Bad return code from HSM");
      LOG.warn("Error Code: " + errorCode);
      LOG.warn("Key Type:   " + keyType);
      LOG.warn("Key:        " + key);
      LOG.warn("Request:    " + reqMsg);
      LOG.warn("Response:   " + respMsg);
      System.out.println("Bad return code from HSM");
      System.out.println("Error Code: " + errorCode);
      System.out.println("Key Type:   " + keyType);
      System.out.println("Key:        " + key);
      System.out.println("Request:    " + reqMsg);
      System.out.println("Response:   " + respMsg);
    }
    return newKey;
  }
}

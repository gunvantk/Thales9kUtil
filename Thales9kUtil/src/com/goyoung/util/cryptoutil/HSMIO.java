package com.goyoung.util.cryptoutil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.log4j.Logger;

@SuppressWarnings("unused")
class HSMIO
{
  private static final Logger log = Logger.getLogger(HSMIO.class);
  private static final int LENGTH_BYTES = 2;
  private static final String HSM_CHARSET = "ASCII";
  
  public static String sendHSMCommand(String command)
    throws Exception
  {
    Properties props = CryptoProperties.getInstance();
    
    byte[] commandBytes = command.getBytes("ASCII");
    short commandLength = (short)commandBytes.length;
    byte[] requestBytes = new byte[2 + commandLength];
    

    requestBytes[0] = ((byte)(commandLength >> 8 & 0xFF));
    requestBytes[1] = ((byte)(commandLength & 0xFF));
    

    System.arraycopy(commandBytes, 0, requestBytes, 2, commandBytes.length);
    

    Socket socket = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    
    String response = null;
    try
    {
      socket = new Socket(props.getProperty("hsm.address"), Integer.parseInt(props.getProperty("hsm.port")));
      
      out = new DataOutputStream(socket.getOutputStream());
      in = new DataInputStream(socket.getInputStream());
      
      out.write(requestBytes);
      out.flush();
      

      long startTime = System.currentTimeMillis();
      while (in.available() < 2)
      {
        log.trace("Waiting for first 2 bytes (length) from HSM...");
        if (System.currentTimeMillis() - startTime > 6000L)
        {
          log.error("No response received from HSM in time");
          throw new Exception("No response received from HSM in time");
        }
        Thread.sleep(100L);
      }
      byte len1 = in.readByte();
      byte len2 = in.readByte();
      short responseLength = (short)(len1 << 8 | len2);
      

      startTime = System.currentTimeMillis();
      while (in.available() < responseLength)
      {
        log.trace("Waiting for " + responseLength + " bytes response from HSM...");
        if (System.currentTimeMillis() - startTime > 6000L)
        {
          log.error("Full response was not received from HSM in time");
          
          throw new Exception("Full response was not received from HSM in time");
        }
        Thread.sleep(100L);
      }
      byte[] responseBytes = new byte[responseLength];
      for (int i = 0; i < responseLength; i++) {
        responseBytes[i] = in.readByte();
      }
      response = new String(responseBytes, "ASCII");
    }
    finally
    {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
      if (socket != null) {
        socket.close();
      }
    }
    return response;
  }
}

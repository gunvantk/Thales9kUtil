package com.goyoung.util.cryptoutil;

import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

class CryptoProperties
{
  private static final Logger LOG = Logger.getLogger(CryptoProperties.class);
  private static Properties props = null;
  
  public static synchronized Properties getInstance()
    throws Exception
  {
    if (props == null)
    {
      props = new Properties();
      InputStream inStream = CryptoProperties.class.getClassLoader().getResourceAsStream("cryptoutil.properties");
      if (inStream == null)
      {
        LOG.error("cryptoutil.properties file not found");
        throw new Exception("cryptoutil.properties file not found");
      }
      props.load(inStream);
      inStream.close();
    }
    return props;
  }
}

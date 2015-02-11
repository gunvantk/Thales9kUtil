package com.goyoung.util.cryptoutil;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

class ConvKeysCommand
  extends FileConversionCommand
{
  static final Logger LOG = Logger.getLogger(ConvKeysCommand.class);
  private static final Pattern FILE_REGEX = Pattern.compile("(\\w+),(\\w+)");
  
  public void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar convkeys <options>");
    out.println(" - Reads csv file of key_type,key encrypted under the");
    out.println(" - old LMK and writes out a file of keys under the");
    out.println("new LMK.");
    out.println("Options:");
    out.println("-in <input_file>   - input file name");
    out.println("-out <output_file> - output file name");
  }
  
  protected String convertLine(String line)
    throws Exception
  {
    Matcher matcher = FILE_REGEX.matcher(line);
    String newKey = null;
    if (matcher.matches())
    {
      String keyType = matcher.group(1);
      String oldKey = matcher.group(2);
      newKey = reencryptKey(keyType, oldKey);
    }
    else
    {
      LOG.warn("Invalid input: " + line);
      System.out.println("Invalid input: " + line);
    }
    return newKey;
  }
}

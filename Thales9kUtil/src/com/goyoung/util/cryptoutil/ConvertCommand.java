package com.goyoung.util.cryptoutil;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

class ConvertCommand
  extends FileConversionCommand
{
  static final Logger LOG = Logger.getLogger(ConvertCommand.class);
  @SuppressWarnings("unused")
private static final String KEYTYPE_PVK = "02";
  @SuppressWarnings("unused")
private static final String KEYTYPE_CVK = "42";
  @SuppressWarnings("unused")
private static final String ZERO_KEY = "0000000000000000";
  private static final Pattern FILE_REGEX = Pattern.compile("([0-9]{6}),([0-9a-fA-F]{16}),([0-9a-fA-F]{16}),([0-9a-fA-F]{16})");
  private static final MessageFormat SQL_FMT = new MessageFormat("UPDATE binkeys SET pvkey = ''{4}'', cvkeya = ''{5}'', cvkeyb = ''{6}'', lastuser = 1003, lastupdate = SYSDATE WHERE BIN = ''{0}'' AND pvkey = ''{1}'' AND cvkeya = ''{2}'' AND cvkeyb = ''{3}'';");
  
  public void showUsage()
  {
    PrintStream out = System.out;
    out.println("Usage: java -jar Thales9kUtil.jar convert <options>");
    out.println(" - Reads csv file of bin,pvk,cvka,cvkb and writes out");
    out.println(" - sql statements to update Solspark binkeys table.");
    out.println("Options:");
    out.println("-in <input_file>   - input file name");
    out.println("-out <output_file> - output file name");
  }
  
  protected String convertLine(String line)
    throws Exception
  {
    Matcher matcher = FILE_REGEX.matcher(line);
    String sql = null;
    if (matcher.matches())
    {
      String bin = matcher.group(1);
      String oldPVKey = matcher.group(2);
      String oldCVKeyA = matcher.group(3);
      String oldCVKeyB = matcher.group(4);
      if (("0000000000000000".equals(oldPVKey)) || ("0000000000000000".equals(oldCVKeyA)) || ("0000000000000000".equals(oldCVKeyB)))
      {
        System.out.println("Zero keys - record ignored: " + line);
      }
      else
      {
        LOG.debug("Processing BIN: " + bin);
        String newPVKey = reencryptKey("02", oldPVKey);
        String newCVKeyA = reencryptKey("42", oldCVKeyA);
        String newCVKeyB = reencryptKey("42", oldCVKeyB);
        if ((null == newPVKey) || (null == newCVKeyA) || (null == newCVKeyB))
        {
          System.out.println("Key conversion failed - ignored: " + line);
        }
        else
        {
          Object[] arguments = { bin, oldPVKey, oldCVKeyA, oldCVKeyB, newPVKey, newCVKeyA, newCVKeyB };
          
          sql = SQL_FMT.format(arguments);
        }
      }
    }
    else
    {
      LOG.warn("Invalid input - ignored: " + line);
      System.out.println("Invalid input - ignored: " + line);
    }
    return sql;
  }
}

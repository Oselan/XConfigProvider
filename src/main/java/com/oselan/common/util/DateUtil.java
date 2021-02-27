/**
 * 
 */
package com.oselan.common.util;

import java.text.*;
import java.util.*;

import org.apache.commons.lang.*;

/**
 * @author fadi
 *
 */
public class DateUtil
{
	
	public static final String SIMPLE_DATE_FORMAT = "MM/dd/yyyy";
   /**
    * Parses the provided date string and returns a date object.  Will return null
    * if the date string is whitespace, empty (""), null, or the actual string "null".
    * @param dateStr
    * @param dateFormat
    * @return
    */
   public static Date parseDate(String dateStr, String dateFormat) throws ParseException
   {
      if(StringUtils.isBlank(dateStr) || dateStr.toLowerCase().equals("null"))
         return null;      
         
      return new SimpleDateFormat(dateFormat).parse(dateStr);
   }
   
   /**
    * formats the provided date into a string
    * 
    * @param date
    * @param dateFormat
    * @return
    */
   public static String formatDate(Date date, String dateFormat)
   {
      if(date == null)
         return "";
      
      return new SimpleDateFormat(dateFormat).format(date);
   }  
   
   /**
    * returns true if the DOS falls between the effective and term date or DOS is equal to term date
    * otherwise returns false
    * @param dosStr
    * @param termDateStr
    * @param effDateStr
    * @param defaultDate
    * @return
    * @throws ParseException
    */
   public static boolean isWithinDateRange(String dosStr, String termDateStr, String effDateStr, Date defaultDate) throws ParseException
   {
      Date dos = parseDate(dosStr, SIMPLE_DATE_FORMAT);
      Date termDate = null;
      Date effDate = null;
      if(StringUtils.isNotBlank(termDateStr)) 
    	  termDate = parseDate(termDateStr, SIMPLE_DATE_FORMAT);
      if(StringUtils.isNotBlank(effDateStr)) 
    	  effDate = parseDate(effDateStr, SIMPLE_DATE_FORMAT);

      if(termDate == null && effDate == null)
    	  return true;      
      
      if(dos != null && effDate!= null && dos.after(effDate))
      {
         if(termDate == null || termDate.equals(defaultDate))
         {            
            return true;
         }
         else
         {
            if(dos.before(termDate) || dos.compareTo(termDate) == 0)
            {
               return true;
            }
            else
            {
               return false;
            }
         }
      }
      else
         if(dos != null && effDate != null && dos.compareTo(effDate) == 0)
         {
            return true;
         }
         else
         {
            return false;
         }
   }
}

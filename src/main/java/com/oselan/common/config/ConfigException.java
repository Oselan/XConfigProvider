package com.oselan.common.config;

/***
 * A ConfigException occurs during load of the application. 
 * typically should not occur and if it is thrown the applicaiton should halt.
 * @author Ahmad
 *
 */
public class ConfigException extends RuntimeException
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ConfigException()
   {
      super(); 
   }

   public ConfigException(String message, Throwable cause)
   {
      super(message, cause);
       
   }

   public ConfigException(String message)
   {
      super(message); 
   }

   public ConfigException(Throwable cause)
   {
      super(cause); 
   }

   
}

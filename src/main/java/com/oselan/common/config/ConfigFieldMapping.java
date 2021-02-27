package com.oselan.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Anotation used to define the path of a field used to fill a property on that class
 * path is required and is the path to read config values from. 
 * parserMethod a method that accepts a string parameter
 *  called to set parse the value from string otherwise field will be set based on its type.
 * defaultValue if available the value will be used if property is missing. 
 * @author Ahmad 
 */
@Target({ElementType.FIELD}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigFieldMapping { 
   public static final String NULL_VALUE = "null";
   public static final String UNDEFINED_VALUE = "$U_N_D_E_F_I_N_E_D";
/***
 * path of the field
 * @return
 */
   String value(); 
   /***
    * void parserMethod (String value)
    * @return
    */
   String parserMethod() default UNDEFINED_VALUE; 
   /***
    * String, "null" is === null  , otherwise must be a string value, e.g. boolean "true" or "false"
    * @return
    */
   String defaultValue()  default UNDEFINED_VALUE;
   
   
}

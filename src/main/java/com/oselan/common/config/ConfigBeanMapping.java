package com.oselan.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Anotation used to define the path of a class and the key used to fill a property on that class
 * @author Ahmad 
 */
@Inherited
@Target({ElementType.TYPE  }) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigBeanMapping {
   /***
    * path of the value
    * @return
    */
   String value();
    
}

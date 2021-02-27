package com.oselan.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger; 
 
 /***
  * Simple Utility to perform common reflection based operations 
  */
public class ReflectionUtil
{
	private static final Logger log = LogManager.getLogger(ReflectionUtil.class);
   public final static String DEFAULT_DATE_FORMAT="MM/dd/yyyy";
   
   /***
    * Creates a new instance of Class and fills its fields with values from map
    * @param beanClass
    * @param map
    * @return
    * @throws Exception
    */
   public static <T> T parseMapIntoBean(Class<T> beanClass, Map<String, String[]> map) throws Exception
   {
      T beanObject = null;
      try
      {
         // create the bean
         beanObject = beanClass.newInstance();
      }
      catch(InstantiationException e)
      {
         throw new Exception("Class " + beanClass.getName() + " could not be instantiated ", e);
      }
      catch(IllegalAccessException e)
      {
         throw new Exception("Class " + beanClass.getName() + " is inaccessible   ", e);
      }

      return parseMapIntoBean(beanObject, map);

   }

   /***
    * Fills beanObject declaredFields with available values from Map
    * @param beanObject
    * @param map
    * @return
    * @throws Exception
    */
   public static <T> T parseMapIntoBean(T beanObject, Map<String, String[]> map) throws Exception
   {

      boolean privateMember = false;
      @SuppressWarnings("unchecked")
      Class<T> beanClass = (Class<T>) beanObject.getClass();

      Map<String,Field> fieldsMap = getFields(beanClass);
    //Looping over map and trying to find the field in the bean is faster than looping over fields
      // because we resolve control case sensitivity from this method
      // and if we use declared fields we would get all fields from super classes 
      for (Entry<String, String[]> dataElement : map.entrySet())
         {
         Field field = null;
         String fieldName = dataElement.getKey();
         String fieldValue =  dataElement.getValue()!=null? dataElement.getValue()[0]: null;
         field = fieldsMap.get(fieldName);
          // skip map field if field not found
         if (field == null) {
             log.warn("Field " + fieldName + " not found on " + beanClass.getName() );
             continue;
         }
         privateMember = !field.isAccessible();
         if (privateMember) field.setAccessible(true);


         Object objValue = getObjectValue(field.getType(), fieldValue);
         // System.out.println("Field extracted from map:" + field.getName() +"
         // value: " + value);
         if(objValue != null || fieldValue == null)
         {
            try
            {
               field.set(beanObject, objValue);
            }
            catch(Exception e)
            {
               // log.error(" Field " + columnName + " in bean " +
               // beanClass.getName() + " can not be set to " + objValue + " : "
               // + e.getMessage());
               continue;// this is not a breaking error we continue on the
            }
         }

         if(privateMember)
         {
            field.setAccessible(false);
         }

      }

      return beanObject;
   }

   /***
    * Returns a caseInsensitive-key map of all the fields of this class and all its classes hierarchy
    * This is to allow searching for fields in a case insensitive manner
    * @param clazz
    * @return
    */
   public static Map<String,Field> getFields(Class<?> clazz)
   {   
       Map<String,Field> fields = new TreeMap<String,Field>(String.CASE_INSENSITIVE_ORDER);
       if (/*clazz.equals(Object.class) || */ clazz.getCanonicalName().startsWith("java.")) return null;
       Map<String,Field> superClassFields =  getFields(clazz.getSuperclass());
       if(superClassFields!=null)
          fields.putAll(superClassFields);
       
       Field[] fieldsArray = clazz.getDeclaredFields();
       for (Field field: fieldsArray)
           fields.put(field.getName(), field); 
       return fields;
   }
   
  
   /**
    * returns the Field object from the class hierarchy
    * @param clazz
    * @param fieldName case sensitive name
    * @return
    */
   public static Field getField(Class<?> clazz, String fieldName)
   {
      if (clazz.equals(Object.class)) return null;
      
      Field field = null;
      try { 
        field = clazz.getDeclaredField(fieldName);
      }
      catch(NoSuchFieldException e){
          field = getField(clazz.getSuperclass(),fieldName);
      }
      return field;
   }

   /***
    * Attempts to convert a string value to a class object based 
    * @param fieldType
    * @param value
    * @return
    * @throws ParseException
    */
   @SuppressWarnings("unchecked")
   public static <T> T getObjectValue(Class<T> fieldType, String value) throws ParseException
   {
      Object objValue = null;

      if(value != null && value.length() != 0)
      {
         if(fieldType.isEnum())
            objValue = getEnumConstant(fieldType, value);
         else
            if(fieldType.equals(String.class))
            {
               objValue = value;
            }
            else
               if(fieldType.equals(Integer.class) || fieldType.equals(int.class))
               {
                  objValue = Integer.parseInt(value);
               }
               else
                  if(fieldType.equals(Float.class) || fieldType.equals(float.class))
                  {
                     objValue = Float.parseFloat(value);
                  }
                  else
                     if(fieldType.equals(Double.class) || fieldType.equals(double.class))
                     {
                        objValue = Double.parseDouble(value);
                     }
                     else
                        if(fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
                        {
                           objValue = Boolean.valueOf(value);

                        }
                     else
                        if(fieldType.equals(Date.class))
                        {
                           Date date = DateUtil.parseDate(value, DEFAULT_DATE_FORMAT);
                           Calendar cal = Calendar.getInstance();
                           int hours = cal.get(Calendar.HOUR_OF_DAY);
                           int mins = cal.get(Calendar.MINUTE);
                           int secs = cal.get(Calendar.SECOND);
                           cal.setTime(date);
                           cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hours, mins, secs);
                           objValue = cal.getTime();// dateFormat.parse(value);
                        }  else
                              if(fieldType.equals(char.class) || fieldType.equals(Character.class))
                              {
                                 objValue = value.charAt(0);
                              }
      }
      return (T) objValue;
   }

   /***
    * Generic method that returns the object element of the enum from the name 
    * @param clazz
    * @param name
    * @return
    */
   @SuppressWarnings({"unchecked", "rawtypes" })
   public static Object getEnumConstant(Class<?> clazz, String name)
   {
      if(clazz == null || name == null || name.isEmpty())
      {
         return null;
      }
      return Enum.valueOf((Class<Enum>) clazz, name);
   }
   
   
   /***
    * A generic createInstance of a class that creates an instances using the default constructor.  
    * The primary purpose of this is to support inner classes and normal classes 
    * Recall that inner classes can not be instantiated before creating an instance of the outer class 
    * @param clazz
    * @return
    * @throws Exception 
    */
   public static <T> T createInstance(final Class<T> clazz) throws Exception 
   {

      T instanceToReturn = null;
      Class<?> enclosingClass = clazz.getEnclosingClass();
      try{
         if(enclosingClass != null)
         {
            Object instanceOfEnclosingClass = createInstance(enclosingClass);
   
            Constructor<T> ctor = clazz.getDeclaredConstructor( enclosingClass);
            ctor.setAccessible(true);
            if(ctor != null)
            {
               instanceToReturn = ctor.newInstance(instanceOfEnclosingClass);
            }
         }
         else
         {
            instanceToReturn = clazz.newInstance();
         }
      }
      catch(Exception e)
      { 
         throw new Exception("could not instantiate " + clazz);
      }
      return instanceToReturn;
   }
   
   /***
    * A generic createInstance of a class that creates an instances using the constructor that is suitable for those args  
    * The primary purpose of this is to support inner classes and normal classes 
    * Recall that inner classes can not be instantiated before creating an instance of the outer class 
    * @param clazz
    * @param constructor args
    * @return
    * @throws Exception 
    */ 
   public static <T> T createInstance(final Class<T> clazz, Object[] args) throws Exception 
   {

      T instanceToReturn = null;
      Class<?> enclosingClass = clazz.getEnclosingClass();
      try{
         if(enclosingClass != null)
         {
            Object instanceOfEnclosingClass = createInstance(enclosingClass );
   
            Constructor<T> ctor = clazz.getDeclaredConstructor( enclosingClass);
            ctor.setAccessible(true);
            if(ctor != null)
            {
               instanceToReturn = ctor.newInstance(instanceOfEnclosingClass);
            }
         }
         else
         { 
            Class<?>[] argTypes = new Class[args.length] ;
            for (int i=0; i < args.length ; i++)
               argTypes[i] = args[i].getClass();
            Constructor<T> ctor = clazz.getConstructor(argTypes); 
            instanceToReturn =  ctor.newInstance(args);
         }
      }
      catch(Exception e)
      { 
         throw new Exception("could not instantiate " + clazz);
      }
      return instanceToReturn;
   }
}

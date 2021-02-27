package com.oselan.common.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;

import com.oselan.common.util.ReflectionUtil;
 
 
/***
 * An abstract class provides common implementation of the IConfig interface
 * A config bean should be extended for every section in the configuration
 * Supports single configuration or multiple configuration sections .
 * e.g. 
 * <MailConfig >
 * <subConfig details>
 * </MailConfig>
 * @author Ahmad hamid 
 */  
public abstract class ConfigBean implements IConfig 
{
   //internal configuration section reference 
   private transient HierarchicalConfiguration config;
   /***
    * Holds references for  Configbeans injected after initializations of this bean
    */
   private List<IConfig> configBeansList= new ArrayList<IConfig>();
   
   /**
    * Sets the configuration of this configbean and initiates read Configuration
    * @param config
    * @return 
    */ 
   @Override
   public void loadConfiguration(HierarchicalConfiguration config)  
   { 
      this.config = config;
      if( this.config== null)
         throw new ConfigException("Configuration not found."); 
      //read this config bean attributes
      readConfiguration(); 
   } 
   
   /***
   * The config hierarchical of this configBean  
   * @return
   */
   public HierarchicalConfiguration getConfig()
   {
      return config;
   }
 
   
   /***
    * Attempts to read path from annotation on class @ConfigMapping(path="")
    * Override if annotation is not present to provide the path for the bean configuration.
    */
   private String path;
   @Override
   public String getPath()
   {  
      //attempt to read path from @ConfigMapping(path="") if found
      if (path==null && this.getClass().isAnnotationPresent(ConfigBeanMapping.class) ) {
         ConfigBeanMapping annotation =(ConfigBeanMapping) this.getClass().getAnnotation(ConfigBeanMapping.class);
         path =annotation.value();
      }
      return path;
   }
   
   /***
    * Allows internally setting path of the bean.
    * @param path
    */
   private void setPath(String path)
   {
      this.path = path;
   }

   /*** 
    * Sometimes its required to inject configBeans to be loaded and reloaded at runtime.
    * e.g. readng a new been relative to subconfiguration when there is no field for that particular bean. 
    * Adds a config Bean to the list to be provided with configuration on load 
    * To be monitored for changes and reloaded if a file was changed
    * @param configBean
    * @return parentBean
    */
    public <T extends IConfig>  ConfigBean withConfigBean(T configBean)
    {  
         configBeansList.add(configBean);
         //read the configuration if the file is already loaded
         if ( config!=null)
            readSubconfigBean( configBean);
         return this; 
    }
    
    /***
     * Writes a value at the specified path or key
     * @param path
     * @param value
     */
    public void writeProperty(String path, Object value )
    {
       if (config!=null)
          config.setProperty(path,value); 
    }

   /***
    * Is Called to start reading the configuration into the appropriate bean properties 
    * from config.
    * Override this method to 
    */
   public  void readConfiguration(){
        //read all fields with @ConfigFieldMapping(path="") or fields of type iconfig 
        readFieldsConfigurations();
        //reload configuration that were injected after load
        readInjectedConfigurations();
   }
   
  

   /***
     * Reloads all subConfiguration beans injected.  
     */
    private void readInjectedConfigurations()
    {
      for (IConfig configBean: configBeansList)
      {
         readSubconfigBean( configBean);
      }
    }

   /***
    * Reads all fields of types that implement iconfig representing subconfigurations.
    * Reads all annotated fields of basic types automatically from configuration  
    * @throws ConfigurationException 
    */
   private void readFieldsConfigurations()  {
      IConfig parentBeanObj = this;
      //get all fields with @ConfigMapping(path="") 
      Field[] fields = parentBeanObj.getClass().asSubclass(parentBeanObj.getClass()).getDeclaredFields();
      for(Field field : fields) { 
         if (IConfig.class.isAssignableFrom(field.getType()) && !field.isSynthetic())
         {
            if(field.isAnnotationPresent(ConfigFieldMapping.class))
            {
               FieldAnnotationParser annotation = new FieldAnnotationParser((ConfigFieldMapping) field.getAnnotation(ConfigFieldMapping.class));
               if(!config.configurationsAt( annotation.getFieldPath()).isEmpty() )
               {   
                  // The field is a configuration bean and annotation is on
                  // classlevel
                  IConfig fieldBean = initSubconfigField(field, parentBeanObj);
                  ((ConfigBean)fieldBean).setPath(annotation.getFieldPath());
                  readSubconfigBean(fieldBean); 
               }
               else if (annotation.isRequired()) 
               {  
                  throw new ConfigException("Missing required configuration key : " + annotation.getFieldPath());
               }
            }
            else
            {
               // The field is a configuration bean and annotation is on
               // classlevel
               IConfig fieldBean = initSubconfigField(field, parentBeanObj);
               readSubconfigBean(fieldBean); 
            }
         }
         else if (field.isAnnotationPresent(ConfigFieldMapping.class)) 
         {   
             //any field with FieldAnnotation
             //configBean, primitive, enum or other parseable fields
             readConfigField(field); 
         }  
      }   
   }
   
   /***
    * Attempts to initialize field and sets value of field if needd 
    * and returns IConfig bean object in that field
    * @param field field to initialize 
    * @param fieldParentBean parentBean that contains that field ie. this
    * @return 
    */
   private IConfig initSubconfigField(Field field,IConfig fieldParentBean)  
   {
      IConfig beanObject = null;
      try
      {
         boolean privateMember = false;
         if(!field.isAccessible())
         {   //if field is private then will make it accessible before reading it.
            field.setAccessible(true);
            privateMember = true;
         }
         beanObject = (IConfig) field.get(fieldParentBean);
         if (beanObject==null)
         {  //attempt to initialize field and set it if null
            beanObject =  (IConfig) ReflectionUtil.createInstance(field.getType()); 
            field.set(fieldParentBean, beanObject); 
         } 
         if (privateMember)
            field.setAccessible(false);
      }  
      catch(Exception e)
      {
         throw new ConfigException("Failed initialize or set field " + field,e);
      } 
      return beanObject;
   }

   /***
    * Attempts to load subconfiguration bean by calling its getPath and attempting to extract it from this beans configuration object.
    * Initializes subconfiguration bean if it is not initialized
    * @param bean
    */
   private void readSubconfigBean(IConfig subConfigBean )
   {   
      if(subConfigBean.getPath() == null)
      { 
         throw new ConfigException("Config bean missing configuration path:" + subConfigBean.getClass().getName());
      } 
      HierarchicalConfiguration config = this.config.configurationAt(subConfigBean.getPath());  
      if( config!= null)
         subConfigBean.loadConfiguration(config);  
   
   }
  
   /**
    * Reads field based on annotation and sets its values.
    * @param field
    */
   private void readConfigField(Field field)
   {
      FieldAnnotationParser annotation =new FieldAnnotationParser((ConfigFieldMapping) field.getAnnotation(ConfigFieldMapping.class));
      
      if ( StringUtils.isNotBlank(annotation.getFieldParserMethod()) )
      {  
       //parser methods only expect String 
         Object value = getObjectValue(String.class, annotation.getFieldPath(), annotation.getDefaultValue(),annotation.isRequired()); 
         try
         { 
             Method method = field.getDeclaringClass().getMethod(annotation.getFieldParserMethod(), String.class); 
             boolean privateMethod ;
             if(privateMethod = !method.isAccessible()) 
                method.setAccessible(privateMethod); 
             
             method.invoke(this, value);
             if(privateMethod) method.setAccessible(!privateMethod);
         }
         catch(Exception e)
         { 
            throw new ConfigException("Parser method " + annotation.getFieldParserMethod() + " not found on " + field.getDeclaringClass());
         } 
      }
      else
      {  
         // set the field manually 
         Object value = getObjectValue(field.getType(),annotation.getFieldPath(),annotation.getDefaultValue(),annotation.isRequired());
         try
         {
            boolean privateMember = false;
            if(!field.isAccessible())
            {
               field.setAccessible(true);
               privateMember = true;
            }
            try
            {
               field.set(this, value);
            }
            catch(Exception e)
            {
               field.set(this, field.getType().cast(value));
            }
            if(privateMember) field.setAccessible(false);
         }
         catch(Exception e)
         {
            throw new ConfigException("Failed to set value of config to field: " + field, e);
         }
      } 
   }
   
   
   
   /***
    * Attempts to extract the field value from configuration and convert it to the appropriate field type.
    * @param fieldType
    * @param fieldPath
    * @param isRequired
    * @param defaultValue
    * @return
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   private Object getObjectValue(Class<?> fieldType, String fieldPath,  String defaultValue , boolean isRequired)  
   {
     
      SimpleDateFormat dateFormat=null;
      Object objValue = null;  
      if (List.class.isAssignableFrom(fieldType))
         objValue =  isRequired? config.getList( fieldPath) :  config.getList(fieldPath,defaultValue==null? null :Arrays.asList(defaultValue.split(",")) );
      else if (fieldType.equals(Boolean.class)|| fieldType.equals(boolean.class))
         objValue =isRequired?  config.getBoolean(fieldPath ) : config.getBoolean(fieldPath,Boolean.valueOf(defaultValue) ); 
      else if (Integer.class.isAssignableFrom(fieldType) || fieldType.equals(int.class)) 
         objValue = isRequired?config.getInt(fieldPath):config.getInteger(fieldPath, Integer.valueOf(defaultValue));
      else if (Long.class.isAssignableFrom(fieldType)|| fieldType.equals(long.class)) 
         objValue =isRequired? config.getLong(fieldPath):config.getLong(fieldPath,Long.valueOf(defaultValue)); 
      else if (Float.class.isAssignableFrom(fieldType)|| fieldType.equals(float.class))
         objValue =isRequired? config.getFloat(fieldPath):config.getFloat(fieldPath,Float.valueOf(defaultValue)); 
      else if (Double.class.isAssignableFrom(fieldType)|| fieldType.equals(double.class))
         objValue = isRequired?config.getDouble(fieldPath):config.getDouble(fieldPath,Double.valueOf(defaultValue));
      else if (String.class.isAssignableFrom(fieldType)) 
         objValue =isRequired? config.getString(fieldPath):config.getString(fieldPath,defaultValue);  
      else if(Character.class.isAssignableFrom(fieldType) || fieldType.equals(char.class))
            {
               objValue =isRequired?  config.getString(fieldPath).charAt(0):config.getString(fieldPath,defaultValue).charAt(0);  
            }
      else if (fieldType.isEnum())  
      {  
         objValue= isRequired?config.getString(fieldPath):config.getString(fieldPath,defaultValue);
         objValue = Enum.valueOf((Class<Enum>)fieldType, (String) objValue)  ;  
      }
      
      else if(fieldType.equals(Date.class))
      {
         if (dateFormat==null) dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         String strValue= isRequired?config.getString(fieldPath):config.getString(fieldPath,defaultValue);
         Date date;
         try
         {
            date = dateFormat.parse( strValue);
         }
         catch(ParseException e)
         {
            throw new ConfigException("Failed to parse date"+ strValue,e);
         }
         Calendar cal = Calendar.getInstance();
         int hours = cal.get(Calendar.HOUR_OF_DAY);
         int mins = cal.get(Calendar.MINUTE);
         int secs = cal.get(Calendar.SECOND);
         cal.setTime(date);
         cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), hours, mins, secs);
         objValue = cal.getTime();// dateFormat.parse(value);
      }  
      else  
      {
         //  log.warn("Unexpected field data tpye " + fieldType.getName() + " for " + fieldPath);
         objValue = isRequired?  config.getString(fieldPath):config.getString(fieldPath,defaultValue) ;
      }
      
      return objValue; 
   }
   
   /***
    * returns a toString value of this bean all the subBeans attached to it.
    * @return
    */
   public String deepToString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(this.toString());
      for (IConfig config: this.configBeansList)
      {
         sb = sb.append("\r\n").append(config.toString());
      } 
      return sb.toString();
   }
   
   /***
    * Class to reuse annotation field parsing logic 
    */
   private class FieldAnnotationParser
   {
      private String fieldPath;
      private String fieldParserMethod;
      private boolean isRequired;
      private String defaultValue;

      public FieldAnnotationParser(ConfigFieldMapping fieldMappingAnnotation)
      { 
         fieldPath = fieldMappingAnnotation.value();
         fieldParserMethod = fieldMappingAnnotation.parserMethod().equals(ConfigFieldMapping.UNDEFINED_VALUE) ? null
            : fieldMappingAnnotation.parserMethod().trim();
         isRequired = fieldMappingAnnotation.defaultValue().equals(ConfigFieldMapping.UNDEFINED_VALUE);
         defaultValue = fieldMappingAnnotation.defaultValue().equals(ConfigFieldMapping.NULL_VALUE) ? null : fieldMappingAnnotation.defaultValue();
      }

      public String getFieldPath()
      {
         return fieldPath;
      }
      public String getFieldParserMethod()
      {
         return fieldParserMethod;
      }
      public boolean isRequired()
      {
         return isRequired;
      }
      public String getDefaultValue()
      {
         return defaultValue;
      }

   }
   
}

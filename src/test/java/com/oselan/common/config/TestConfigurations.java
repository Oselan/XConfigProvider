 package com.oselan.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.oselan.common.util.DateUtil;

 
 

@FixMethodOrder(MethodSorters.JVM)
public class TestConfigurations
{
    
     
    /***
     * used to ttest file based configuration .
     */
  private static final String FILE_PARENTCHILD = "test/parentchild.xml"; 
  private static final String CONFIG_PARENT_CHILD_XML  = "<Config>" + 
                                                        "<ParentConfig>" + 
                                                        "<ChildConfig>" + 
                                                              "<PropertyX>Test Field of Type Config Bean</PropertyX>  " + 
                                                              "<PropertyY>true</PropertyY>  " + 
                                                        "</ChildConfig>" +  
                                                           "<PropertyA>false</PropertyA> " + 
                                                           "<lastRun>20171115091203</lastRun>   " + 
                                                     "</ParentConfig>" + 
                                                   "</Config>";   
   private static final String CONFIG_LIST_ITEMS_XML ="<Config>" + 
                                                   "<ListConfig listkey=\"app1\">" + 
                                                   "        <ListItemConfig id=\"TestProcess1\">" + 
                                                    "           <ListItemConfigProperty>test1</ListItemConfigProperty> " + 
                                                   "        </ListItemConfig>  " + 
                                                   "        <ListItemConfig id=\"TestProcess2\">" + 
                                                   "           <ListItemConfigProperty>test2</ListItemConfigProperty>" + 
                                                   "        </ListItemConfig>  " + 
                                                   "        <ListItemConfig id=\"TestProcess3\">" + 
                                                   "            <ListItemConfigProperty>test3</ListItemConfigProperty>" + 
                                                   "        </ListItemConfig>" + 
                                                   "</ListConfig>"
                                                   + "</Config>" ;
   
   private static final String CONFIG_LIST_XML ="<Config>" + 
      "<ListConfig >" +  
      "          <ListItemConfigProperty>test1</ListItemConfigProperty>  "  +
      "           <ListItemConfigProperty>test2</ListItemConfigProperty> "  +
      "           <ListItemConfigProperty>test3</ListItemConfigProperty> " + 
      "</ListConfig>" + "</Config>" ;
   
   private static final String CONFIG_DATATYPE_XML = "<Config>" + 
                                                         "<DataTypesConfig>" + 
                                                         "           <booleanValue>true</booleanValue>" + 
                                                         "           <integerValue>3</integerValue>" + 
                                                         "           <longValue>3000000000000</longValue>" + 
                                                         "           <doubleValue>2.12345678910</doubleValue>" + 
                                                         "           <floatValue>6.5</floatValue>" + 
                                                         "           <StringValue>Test</StringValue>" +     
                                                         "           <enumValue>SAMPLE</enumValue>" + 
                                                         "           <dateValue>12/12/2017</dateValue>" + 
                                                         "           <parsableDateTime>20171109155731</parsableDateTime>   " + 
                                                         "</DataTypesConfig>" + 
                                                      "</Config>";
   
   @BeforeClass 
   public static void createFiles() throws IOException 
   {  
         File file = new File(FILE_PARENTCHILD);
         file.getParentFile().mkdirs();
         assertTrue(file.createNewFile());
         FileWriter fwriter = new FileWriter(file);
         fwriter.write(CONFIG_PARENT_CHILD_XML);
         fwriter.close(); 
   }
   
   @AfterClass
  public static void cleanFiles(){
      File file = new File(FILE_PARENTCHILD);
      if (file.exists()) file.delete(); 
      
  }
   
   /***
    * Tests
    * File based configuration 
    * Loading parentBean config
    * Initializing and loading child beans 
    * Annotation  
    */
   @Test 
   public void testFileReadingAnnotation()
   { 
      @ConfigBeanMapping("ParentConfig")
      class ConfigParentA extends ConfigBean 
      {
         //fields
         @ConfigFieldMapping("PropertyA")
         private String propertyA;  
         @ConfigFieldMapping("lastRun")
         private String lastRunDate; 
         private ConfigChild configChild ;  
         //properties
         public String getPropertyA()
         {
            return propertyA;
         } 
         public String getLastRunDate()
         {
            return lastRunDate;
         } 
         public ConfigChild getConfigChild()
         {
            return configChild;
         }   
      }
      ConfigParentA configParent = new ConfigParentA();
      new ConfigProvider().withConfigBean(configParent).loadConfiguration(FILE_PARENTCHILD);  
      assertNotNull(configParent.getLastRunDate());
      assertNotNull(configParent.getPropertyA());
      assertNotNull(configParent.getConfigChild());
      assertNotNull(configParent.getConfigChild().getPropertyX()); 
      assertTrue(configParent.getConfigChild().isPropertyY());
   }
   
   
   /***
    * Testing bean filling manually mixed with child bean based on Annotations
    */
   @Test
   public void testFileReadingManual()
   {
      class ConfigParentM  extends ConfigBean 
      {
         //paths and property keys 
         private static final String CONFIG_PATH = "ParentConfig"; 
         private static final String PROPERTY_A = "PropertyA"; 
         private static final String LAST_RUN = "lastRun";
         //fields
         private String propertyA; 
         private String lastRunDate;
         private ConfigChild configChild ;  
         //read configuration manually .
         @Override
         public void readConfiguration()
         { 
            super.readConfiguration();
            propertyA =  getConfig().getString(PROPERTY_A);  
            lastRunDate = getConfig().getString(LAST_RUN); 
         } 
         //get the path of this config.
         @Override
         public String getPath()
         {
            return CONFIG_PATH;
         }
           
         public String getPropertyA()
         {
            return propertyA;
         } 
         public String getLastRunDate()
         {
            return lastRunDate;
         }
         public ConfigChild getConfigChild()
         {
            return configChild;
         } 
         
      }
      
      ConfigParentM configParent = new ConfigParentM();
      new ConfigProvider().withConfigBean(configParent).loadConfiguration(FILE_PARENTCHILD);  
      assertNotNull(configParent.getLastRunDate());
      assertNotNull(configParent.getPropertyA());
      assertNotNull(configParent.getConfigChild());
      assertNotNull(configParent.getConfigChild().getPropertyX()); 
      assertTrue(configParent.getConfigChild().isPropertyY());
   }
   
   
   
   /***
    * Testing configuration reloading and writing 
    */
   @Test 
   @Ignore
   public void testFileWriting_Reloading()
   { 
      @ConfigBeanMapping("ParentConfig")
      class ConfigParentA extends ConfigBean 
      {
         //fields
         @ConfigFieldMapping("PropertyA")
         private String propertyA;  
        
         //properties
         public String getPropertyA()
         {
            return propertyA;
         } 
         public void setPropertyA(String value)
         {
            writeProperty("PropertyA", value);
         }
         @Override
         public String toString()
         {
            return "ConfigParentA [propertyA=" + propertyA + "]";
         } 
         
      } 
      ConfigParentA configParentWritable = new ConfigParentA();
        new ConfigProvider().withConfigBean(configParentWritable).loadConfiguration(FILE_PARENTCHILD,false,true);  
      assertNotNull(configParentWritable.getPropertyA()); 
      
      ConfigParentA configParentReloadable = new ConfigParentA();
       new ConfigProvider().withConfigBean(configParentReloadable).loadConfiguration(FILE_PARENTCHILD,true,true);  
      assertNotNull(configParentReloadable.getPropertyA()); 
      
      
      //Since both providers read from same source , both values should be equal
      assertEquals(configParentWritable.getPropertyA() , configParentReloadable.getPropertyA());
      //change the value through the first bean
      String anyValue = "dummyValue";
      configParentWritable.setPropertyA(anyValue);
      //check if the reloadable object automatically detected the change.
      System.out.println(configParentReloadable );
      assertNotEquals(configParentReloadable.getPropertyA(), anyValue);
      //wait for reloading to occur
      waitFor(ConfigProvider.REFRESH_DELAY+5000);
      System.out.println(configParentReloadable );
      //reloading occurred check propertyA is now equal
      assertEquals(anyValue,configParentReloadable.getPropertyA());
      
      //Trying the other way around will not work since the first bean provider is not reloadable.
      configParentReloadable.setPropertyA("true"); 
      //wait for reloading to occur
      waitFor(ConfigProvider.REFRESH_DELAY+5000);
      //still not equal
      assertNotEquals(configParentWritable.getPropertyA(), configParentReloadable.getPropertyA());
   }
   
   
   @Test 
   public void testXMLStringReading()
   { 
      @ConfigBeanMapping("ParentConfig")
      class ConfigParentA extends ConfigBean 
      {
         //fields
         @ConfigFieldMapping("PropertyA")
         private String propertyA;  
         @ConfigFieldMapping("lastRun")
         private String lastRunDate; 
         private ConfigChild configChild ;  
         //properties
         public String getPropertyA()
         {
            return propertyA;
         } 
         public String getLastRunDate()
         {
            return lastRunDate;
         } 
         public ConfigChild getConfigChild()
         {
            return configChild;
         }   
      }
      ConfigParentA configParent = new ConfigParentA();
      new ConfigProvider().withConfigBean(configParent).loadConfiguration(CONFIG_PARENT_CHILD_XML);  
      assertNotNull(configParent.getLastRunDate());
      assertNotNull(configParent.getPropertyA());
      assertNotNull(configParent.getConfigChild());
      assertNotNull(configParent.getConfigChild().getPropertyX()); 
      assertTrue(configParent.getConfigChild().isPropertyY());
   }

   /***
    * ConfigProvider supports bean injection of ParentBean and childBean
    * after loadConfiguration has been called
    */
   @Test
   public void testBeanInjectionPostLoading()
   {
      @ConfigBeanMapping("ParentConfig")
      class ConfigParentA extends ConfigBean 
      {
         //fields
         @ConfigFieldMapping("PropertyA")
         private String propertyA;  
         @ConfigFieldMapping("lastRun")
         private String lastRunDate;  
         //properties
         public String getPropertyA()
         {
            return propertyA;
         } 
         public String getLastRunDate()
         {
            return lastRunDate;
         } 
          
      }
      ConfigParentA configParent = new ConfigParentA();
      ConfigChild configChild = new ConfigChild();
      ConfigProvider c1 = new ConfigProvider().loadConfiguration(CONFIG_PARENT_CHILD_XML );
       
      assertNull(configParent.getLastRunDate());
      assertNull(configParent.getPropertyA()); 
      assertNull(configChild.getPropertyX()); 
      assertFalse(configChild.isPropertyY());
      //inject beans
      c1.withConfigBean(configParent);
      configParent.withConfigBean(configChild);
      //values change
      assertNotNull(configParent.getLastRunDate());
      assertNotNull(configParent.getPropertyA()); 
      assertNotNull(configChild.getPropertyX()); 
      assertTrue(configChild.isPropertyY());
   }
   
   /***
    *   This loading a list of beans using ConfigList subclass 
    */
   @Test
   public void testBeanListLoading()
   {  
      //list item config
       @ConfigBeanMapping("ListItemConfig")
       class ConfigListItem extends ConfigBean
       {  
         @ConfigFieldMapping( "[@id]")
         private String id;
         @ConfigFieldMapping( "ListItemConfigProperty")
         private String listItemConfigProperty;
         public String getId()
         {
            return id;
         }
         public String getListItemConfigProperty()
         {
            return listItemConfigProperty;
         }
         @Override
         public String toString()
         {
            return "ConfigListItem [id=" + id + ", listItemConfigProperty=" + listItemConfigProperty + "]";
         }
         
       }
      
       //list config
       @ConfigBeanMapping("ListConfig")
       class ConfigList extends ConfigBeanList<ConfigListItem>
       {   
         @ConfigFieldMapping( "[@listkey]")
         private String listKey;
          
         @Override
         protected Class<ConfigListItem> getElementClass()
         {
            return ConfigListItem.class;
         }
   
         public String getListKey()
         {
            return listKey;
         }
   
       }
        
       ConfigList configList = new ConfigList();
       new ConfigProvider().withConfigBean(configList).loadConfiguration(CONFIG_LIST_ITEMS_XML);
       assertNotNull( configList.getListKey()); 
       assertTrue( configList.size() > 0 );
       assertNotNull(configList.get(0).getId());
       assertNotNull(configList.get(0).getListItemConfigProperty());
       for (ConfigListItem item: configList)
          System.out.println(item);
   }
   
   @Test
   public void testStringListLoading()
   {  
      @ConfigBeanMapping("ListConfig")
      class ConfigList  extends ConfigBean 
      {
         
         private List<Object> listItems;   
         @SuppressWarnings("unchecked")
		@Override
         public void readConfiguration()
         { 
            super.readConfiguration();
            listItems = new ArrayList<Object>();
            listItems  =  getConfig().getList("ListItemConfigProperty");
            System.out.println(listItems);
         } 
        
           
      }
      ConfigList list = new ConfigList();
//       ConfigBeanList<ConfigBean> configList = new ConfigBeanList();
      new ConfigProvider().withConfigBean(list).loadConfiguration(CONFIG_LIST_XML);
       
     
   }
   
   /****
    * Tests data type mapping to various accepted data types
    * Tests parsing methods  
    */
   @Test
   public void testDataTypeMapping()
   {
      @ConfigBeanMapping("DataTypesConfig")
       class ConfigDataTypes extends ConfigBean
      {  
         //Fieldsv
        @ConfigFieldMapping("booleanValue")
        private boolean booleanValue;
        @ConfigFieldMapping("integerValue")
        private int integerValue;
        @ConfigFieldMapping("longValue")
        private long longValue;
        @ConfigFieldMapping("doubleValue")
        private double doubleValue;
        @ConfigFieldMapping("floatValue")
        private float floatValue;
        @ConfigFieldMapping("StringValue")
        private String stringValue;
        @ConfigFieldMapping("enumValue") 
        private MyEnum enumValue;
        @ConfigFieldMapping("dateValue")//MM/dd/yyyy
        private Date date; 
        @ConfigFieldMapping(value="parsableDateTime",parserMethod="parseDateTime")
        private Date datetime;
        //Boxed Objects mapping
        @ConfigFieldMapping("booleanValue")
        private Boolean booleanObj;
        @ConfigFieldMapping("integerValue")
        private Integer integerObj;
        @ConfigFieldMapping("longValue")
        private Long longObj;
        @ConfigFieldMapping("doubleValue")
        private Double doubleObj;
        @ConfigFieldMapping("floatValue")
        private Float floatObj;
        /***
         * Parser for datetime
         * @param value
         */
        @SuppressWarnings("unused")
        public void parseDateTime(String value)
        {
           try
           {
              datetime = new SimpleDateFormat("yyyyMMddHHmmss").parse(value);
           }
           catch(ParseException e)
           {
              // TODO Auto-generated catch block
              e.printStackTrace();
           }
        }

      @Override
      public String toString()
      {
         return "ConfigDataTypes [booleanValue=" + booleanValue + ", integerValue=" + integerValue + ", longValue=" + longValue + ", doubleValue="
            + doubleValue + ", floatValue=" + floatValue + ", stringValue=" + stringValue + ", enumValue=" + enumValue + ", date=" + date
            + ", datetime=" + datetime + ", booleanObj=" + booleanObj + ", integerObj=" + integerObj + ", longObj=" + longObj + ", doubleObj="
            + doubleObj + ", floatObj=" + floatObj + "]";
      }
        
     }
      
      ConfigDataTypes configDataTypes = new ConfigDataTypes();
      new ConfigProvider().withConfigBean(configDataTypes).loadConfiguration(CONFIG_DATATYPE_XML);
      System.out.println(configDataTypes.toString());
      assertTrue(configDataTypes.booleanValue);
      assertTrue(configDataTypes.integerValue>0);
      assertTrue(configDataTypes.longValue>0);
      assertTrue(configDataTypes.doubleValue>0);
      assertTrue(configDataTypes.floatValue>0);
      assertNotNull(configDataTypes.enumValue );
      assertNotNull(configDataTypes.date );
      assertNotNull(configDataTypes.datetime );
      
      assertNotNull(configDataTypes.stringValue ); 
      assertNotNull(configDataTypes.booleanObj );
      assertNotNull(configDataTypes.integerObj );
      assertNotNull(configDataTypes.longObj );
      assertNotNull(configDataTypes.doubleObj );
      assertNotNull(configDataTypes.floatObj );
   }
   
   
  
   private static final String CONFIG_RECURSION_XML  = "<config>" + 
      "<filter className=\"fClass1\" >"   
      + "<success  command=\"command1\" actor=\"actor1\" />"
      + "<fail >"
      +    "<filter className=\"fClass1\" >"
      +        "<success command=\"command2\" actor=\"actor2\"/>"
      +        "<fail command=\"command3\" actor=\"actor3\"/>"
      +    "</filter>"
      + "</fail>"
   + "</filter></config>";
  
   @Test
   public void testFileReadingAnnotationRecurrsion()
   { 
      
      FilterConfig configFilter = new FilterConfig();
      new ConfigProvider().withConfigBean(configFilter).loadConfiguration(CONFIG_RECURSION_XML);
      System.out.println(configFilter);
   }
    /***
    * filter_config 
    * <filter className="..." >
    *  <success  command="..." actor="..." >
    *  <fail >
    *     <filter className="..." >
    *        <success command="..." actor="...">
    *        <fail command="..." actor="...">
    *     </filter>
    *  </fail>
    * </filter> 
    *
    */   
   @ConfigBeanMapping("filter")
   public class FilterConfig extends ConfigBean 
   {
      @ConfigFieldMapping("[@className]") 
      private String className;
      @ConfigFieldMapping(value="success",defaultValue="null")
      private branchConfig success;
      @ConfigFieldMapping(value="fail",defaultValue="null")
      private branchConfig fail;
      @Override
      public String toString()
      {
         StringBuilder builder = new StringBuilder();
         builder.append("FilterConfig [className=").append(className).append(", success=").append(success).append(", fail=").append(fail).append("]");
         return builder.toString();
      }
      
   }
   
   public class branchConfig extends ConfigBean 
   {
      @ConfigFieldMapping(value="[@command]",defaultValue="null") 
      private String command;
      @ConfigFieldMapping(value="[@actor]",defaultValue="null")
      private String actor;
      @ConfigFieldMapping(value="filter",defaultValue="null")
      private FilterConfig filter;
      @Override
      public String toString()
      {
         StringBuilder builder = new StringBuilder();
         builder.append("branchConfig [command=").append(command).append(", actor=").append(actor).append(", filter=").append(filter).append("]");
         return builder.toString();
      }
      
   }
    
   /****
    * Tests data type mapping with default values 
    * Test default null value
    */
   @Test
   public void testDataTypeMappingDefaultValues()
   {
      @ConfigBeanMapping("DataTypesConfig")
       class ConfigDataTypes extends ConfigBean
      {  
         //Fields with non existing mappings 
        @ConfigFieldMapping(value="missingValue",defaultValue="true")
        private boolean booleanValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="22")
        private int integerValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="300000000")
        private long longValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="3.62222")
        private double doubleValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="3.6")
        private float floatValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="stringValue")
        private String stringValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="SAMPLE2") 
        private MyEnum enumValue;
        @ConfigFieldMapping(value="missingValue",defaultValue="12/10/2017")
        private Date date; 
        @ConfigFieldMapping(value="missingValue",parserMethod="parseDateTime",defaultValue="20171109155731")
        private Date datetime;
        //Objects mapping
        @ConfigFieldMapping(value="missingValue",defaultValue="true")
        private Boolean booleanObj;
        @ConfigFieldMapping(value="missingValue",defaultValue="22")
        private Integer integerObj;
        @ConfigFieldMapping(value="missingValue",defaultValue="3000000000")
        private Long longObj;
        @ConfigFieldMapping(value="missingValue",defaultValue="3.6222222")
        private Double doubleObj;
        @ConfigFieldMapping(value="missingValue",defaultValue="3.6")
        private Float floatObj;
       
        @ConfigFieldMapping(value="missingValue",defaultValue="null")
        private String nullValue;
        /***
         * Parser for datetime
         * @param value
         */
        @SuppressWarnings("unused")
        public void parseDateTime(String value)
        {
           try
           {
              datetime = new SimpleDateFormat("yyyyMMddHHmmss").parse(value);
           }
           catch(ParseException e)
           {
              // TODO Auto-generated catch block
              e.printStackTrace();
           }
        }
   
      @Override
      public String toString()
      {
         return "ConfigDataTypes [booleanValue=" + booleanValue + ", integerValue=" + integerValue + ", longValue=" + longValue + ", doubleValue="
            + doubleValue + ", floatValue=" + floatValue + ", stringValue=" + stringValue + ", enumValue=" + enumValue + ", date=" + date
            + ", datetime=" + datetime + ", booleanObj=" + booleanObj + ", integerObj=" + integerObj + ", longObj=" + longObj + ", doubleObj="
            + doubleObj + ", floatObj=" + floatObj + "]";
      }
        
     }
      
      ConfigDataTypes configDataTypes = new ConfigDataTypes();
      new ConfigProvider().withConfigBean(configDataTypes).loadConfiguration(CONFIG_DATATYPE_XML);
      System.out.println(configDataTypes.toString());
      assertTrue(configDataTypes.booleanValue);
      assertTrue(configDataTypes.integerValue>0);
      assertTrue(configDataTypes.longValue>0);
      assertTrue(configDataTypes.doubleValue>0);
      assertTrue(configDataTypes.floatValue>0);
      assertNotNull(configDataTypes.enumValue );
      assertNotNull(configDataTypes.date );
      assertNotNull(configDataTypes.datetime ); 
      assertNotNull(configDataTypes.stringValue ); 
      assertNotNull(configDataTypes.booleanObj );
      assertNotNull(configDataTypes.integerObj );
      assertNotNull(configDataTypes.longObj );
      assertNotNull(configDataTypes.doubleObj );
      assertNotNull(configDataTypes.floatObj );
      assertNull(configDataTypes.nullValue);
   }




   /**
     * Test Manual configuration read
     */
  public class ConfigParent  extends ConfigBean 
  {
     //paths and property keys 
     private static final String CONFIG_PATH = "ParentConfig"; 
     private static final String PROPERTY_A = "PropertyA"; 
     private static final String LAST_RUN = "lastRun";
     //fields
     private boolean propertyA; 
     private Date lastRunDate;
     
   //read configuration manually .
     @Override
     public void readConfiguration()
     { 
        super.readConfiguration();
        propertyA =  getConfig().getBoolean(PROPERTY_A); 
        //parse filelast run date.
        lastRunDate = readFileLastRunDate(); 
     } 
     //get the path of this config.
     @Override
     public String getPath()
     {
        return CONFIG_PATH;
     }
       
     //properties
     public boolean isPropertyA()
     {
        return propertyA;
     } 
     public Date getLastRunDate()
     {
        return lastRunDate;
     } 
     
     protected static final String SYSTEM_DATE_TIME_FORMAT = "yyyyMMddHHmmss"; 
     /***
      * Test writing data back to config
      * @param date
      */
     public void setLastRunDate(Date date)
     { 
           lastRunDate = date;
           writeProperty(LAST_RUN, DateUtil.formatDate(date, SYSTEM_DATE_TIME_FORMAT));

     } 
     public Date readFileLastRunDate()
     {
        Date lastRunDate = null; 
        String lastRun =  getConfig().getString(LAST_RUN, "");
        if(!lastRun.isEmpty()) try
        {
           lastRunDate = DateUtil.parseDate(lastRun, SYSTEM_DATE_TIME_FORMAT);
        }
        catch(ParseException e)
        { 
        }  
        return lastRunDate;
        
     }
      
  }
  
  
  @ConfigBeanMapping("ChildConfig")
   public class ConfigChild extends ConfigBean
     {  //fields 
        @ConfigFieldMapping("PropertyX")
        private String propertyX;
        @ConfigFieldMapping(value="PropertyY",defaultValue="false")
        private boolean propertyY;
        
        //properties
        public String getPropertyX()
        {
           return propertyX;
        } 
        public boolean isPropertyY()
        {
           return propertyY;
        } 
        @Override
        public String toString()
        {
           return "ConfigChild [propertyX=" + propertyX + ", propertyY=" + propertyY + "]";
        }  
     }
  
  /***
    * TEST Used for enumeration field
    * @author Ahmad
    *
    */
  public enum MyEnum
  {
     SAMPLE,
     SAMPLE2,
     SAMPLE3;
  }
/***
 * Waits for x milliseconds 
 * @param millis
 */
public static void waitFor(long millis)
{
   try
   { 
      Thread.sleep(millis);
   }
   catch(InterruptedException e)
   { 
      e.printStackTrace();
   }
}

   
}

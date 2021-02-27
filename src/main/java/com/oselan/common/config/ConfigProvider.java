package com.oselan.common.config;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.oselan.common.file.FileUtil;

/**
 * Class that handles loading xml configuration from either file or xml string  
 * initializing implementations of AbstractConfiguration subclasses of configuration property beans 
 * and provides save feature if a property has been changed
 * Example usage:  
       ConfigCommon common = new  ConfigCommon();
       new ConfigProvider().withConfigSection(common).loadConfiguration("Config.xml"); 
 * @author Ahmad Hamid
 * 
 * TODO supply writer implementation to be used on save xml to implement save to external configuration source.
 *  
 */
public class ConfigProvider implements  ConfigurationListener
{ 

   protected static final  Logger log = LogManager.getLogger(ConfigProvider.class);
   /***
    * Delay to refresh or reload the file 
    */
   public static final int REFRESH_DELAY = 10000;
   /***
    * used to check if a string is an xml
    */
   private static final String  XML_PATTERN_STR = "<\\s*(\\S+?)(.*?)\\s*>(.*?)</\\s*\\1\\s*>";
   /***
    * Root xml configuration
    */
   private XMLConfiguration xmlConfig;
   
   /**
    * ConfigBeans register to trigger their reloading on file changes
    */
   private List<IConfig> configBeansList; 
   public ConfigProvider()
   {
      configBeansList= new ArrayList<IConfig>(4);
   }
   
   /***
   * loads configuration from a file 
   * File can be relative or absolute path file or inside jar
   * set reloadable to monitor file for changes and trigger config reload
   * set writable to monitor config for changes and write back to file.
   * @param config configuration file path or configuration xml.  
   */
   public synchronized ConfigProvider loadConfiguration(String fileName, boolean reloadable, boolean writable)
   {
      
       loadConfiguration(fileName);
       
       setFileConfigOptions(reloadable, writable);
       return this;
   }
   
   /**
    * Loads xml from file or xml string. 
    * constructs base beans 
    * @param fileNameorXML
    * @return
    */
   public synchronized ConfigProvider  loadConfiguration(String fileNameorXML)
   {
      log.debug("Initiates loading configuration");
      xmlConfig =new XMLConfiguration() ;
      if (StringUtils.isNotBlank(fileNameorXML))
      {   
         try{
            //process properties could be either xml configuration or filename holding xml configuration 
            if (fileNameorXML.trim().startsWith("<") && fileNameorXML.trim().matches(XML_PATTERN_STR)) 
               xmlConfig.load(new StringReader(fileNameorXML));  
            else //attempt load as a file
            {
               String path = FileUtil.getFilePath(fileNameorXML);
               //set the file path 
               this.configFile = path;
               xmlConfig.load(path);  
            }
            xmlConfig.setThrowExceptionOnMissing(true); 
            //disable list of values until properly supported. can use parserMethod when needed
            xmlConfig.setDelimiterParsingDisabled(true);
            readConfigurations();
         }
         catch(ConfigurationException e)
         {
            log.fatal("Error loading configuration", e);
            throw new ConfigException("Failed to load configuration");
         }
      } 
      log.debug("Configuration loaded");
      return this;
   }
   
   
   public synchronized ConfigProvider  loadConfiguration(InputStream xmlStream)
   {
      log.debug("Initiates loading configuration");
      xmlConfig =new XMLConfiguration() ; 
      if (xmlStream != null)
      {   
         try{
            //process properties could be either xml configuration or filename holding xml configuration 
          
            xmlConfig.load(xmlStream);   
            xmlConfig.setThrowExceptionOnMissing(true); 
            //disable list of values until properly supported. can use parserMethod when needed
            xmlConfig.setDelimiterParsingDisabled(true);
            readConfigurations();
         }
         catch(ConfigurationException e)
         {
            log.fatal("Error loading configuration", e);
            throw new ConfigException("Failed to load configuration");
         }
      } 
      log.debug("Configuration loaded");
      return this;
   }
   
 /***
  * Sets whether the file configurations are reloadable and writables 
  * if Reloadable then a daemon will be setup to monitor file for changes.
  * if Writable then listener will be added to detect config changes and call save method.
  * @param configFile
  * @param reloadable
  * @param writable
  * @return
  */
   protected void setFileConfigOptions(boolean reloadable, boolean writable)
   {
      this.isReloadable = reloadable;
      this.isWritable = writable;
      if(reloadable)
      {
         reloadStrategy = new FileChangedReloadingStrategy();
         // You can set the refresh relay, default to 10 seconds
         reloadStrategy.setRefreshDelay(REFRESH_DELAY);
         xmlConfig.setReloadingStrategy(reloadStrategy);
         if(reloadDeamon == null || !reloadDeamon.isAlive())
         {
            reloadDeamon = new ReloadDeamon();
            reloadDeamon.start();
         }
      } 
      if(writable)
      {
         xmlConfig.addConfigurationListener(this);
      }

   }

   /***
    * Adds a config Bean to the list to be provided with configuration on load 
    * To be monitored for changes and reloaded if a file was changed
    * @param configBean
    * @return
    */
      public <T extends IConfig>  ConfigProvider withConfigBean(T configBean)
      {  
         configBeansList.add(configBean);
         //read the configuration if the file is already loaded
         if ( xmlConfig!=null)
            readConfiguration(configBean);
         return this; 
      }
      
      
 
   
   /***
    * Loops over the configuration bean implementations to refill the data from the appropriate configuration path.
    */
   public void readConfigurations()  
   { 
      for (IConfig bean: configBeansList)
      {
         readConfiguration(bean); 
      }  
   }

   /***
    * Loads a single configuration bean implementation  
    * @param bean
    */
   private void readConfiguration(IConfig bean)  
   { 
         if(bean.getPath() == null)
         {  
            throw new ConfigException("Config bean missing configuration path:" + bean.getClass().getName());
         } 
         HierarchicalConfiguration config = xmlConfig.configurationAt(bean.getPath());//,isWritable);  
         if( config!= null)
               bean.loadConfiguration(config); 
        
   }
   
    
   
   
   /********  configuration file related code ******/
   private String configFile = null ;
   /**
    * @return the configFile
    */
   public   String getConfigFile()
   {
      return configFile;
   }


   /**
    * @param configFile the configFile to set causes a reload of the configuration 
    */
   public   void setConfigFile(String configFile)
   { 
      //reload configuration 
      loadConfiguration(configFile );
   }


   /***
    * Saves the current configuration to the file 
    * could be overriden by subclasses to save to different sources.
    * @throws ConfigurationException
    */
   public synchronized void save()  
   {
         log.info("Updating Configuration file");
         try
         { 
            xmlConfig.save(configFile); 
            //prevent reloading as this is an internal file change
            if (xmlConfig.getReloadingStrategy()!=null)
               xmlConfig.getReloadingStrategy().reloadingPerformed();
         }
         catch(ConfigurationException e)
         {
            throw new ConfigException("",e);
         } 
   }

   
   @Override
   public void configurationChanged(ConfigurationEvent configEvent)
   {  
      // save if a configuration property was set or subnot changed
      if(configEvent.getType() == HierarchicalConfiguration.EVENT_SUBNODE_CHANGED  && !configEvent.isBeforeUpdate()) 
      {  
         log.info("Configuration Changed  ... saving configuration!");
         save(); 
      }
      
   } 
   
   private boolean isWritable;
   private boolean isReloadable; 
   /***
    * if this configuration changes will be written to the file
    * @return
    */
   public boolean isWritable()
   {
      return isWritable;
   }

   /***
    * if changes on this configuration file will be reloaded  
    * @return
    */
   public boolean isReloadable()
   {
      return isReloadable;
   }


   private FileChangedReloadingStrategy reloadStrategy; 
   private ReloadDeamon reloadDeamon;
 
   /***
    * Deamon to handle monitoring and reloading configuration from file
    * @author Ahmad
    *
    */
   class ReloadDeamon extends Thread
   {
       
      public ReloadDeamon()
      {
         super("Configuration_Monitor");
         this.setDaemon(true);
         this.setPriority(MIN_PRIORITY); 
      }


      @Override
      public void run()
      {
         while (true)
         {
            if (reloadStrategy!= null && reloadStrategy.reloadingRequired()  ) 
            { 
               log.info("Configuration Source Changed  ... reloading configuration!");
               readConfigurations(); 
            }
           
            try
            {
               Thread.sleep(REFRESH_DELAY);
            }
            catch(InterruptedException e)
            { 
               e.printStackTrace();
            }
         }
      }
      
   }

    
 
}

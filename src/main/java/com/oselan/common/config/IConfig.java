package com.oselan.common.config;

import org.apache.commons.configuration.HierarchicalConfiguration;
/***
 * A config interface representing a section of xml to be loaded and filled by a ConfigProvider
 * @author Ahmad 
 */
interface IConfig
{ 
   /***
    * loads the configuration bean
    * @param config
    */
   public void loadConfiguration(HierarchicalConfiguration config)   ;
   
   /***
    * The path to this configuration section usually just the name of the root element of this section
    */ 
   public abstract String getPath() ;
    
}

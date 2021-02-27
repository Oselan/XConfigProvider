package com.oselan.common.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.oselan.common.util.ReflectionUtil;
 
/***
 * An abstract class provides common implementation of the IConfig interface for an list of similar
 * configurations. 
 * e.g. 
 * <DBConfigs rtrt >
 * <dbConfig ...  />
 * <dbConfig ...  />
 * <dbConfig ...  /> 
 * </DBConfigs>
 * @author Ahmad hamid
 */
public  abstract class ConfigBeanList<T extends ConfigBean> extends ConfigBean implements  List<T>
{ 
    
   private final List<T> childConfigs = new ArrayList<T>();
     
   
   @Override
   public void readConfiguration()
   { 
      super.readConfiguration(); 
      readConfigurationstoList();
   }
  
   /***
    * Returns the class of the subtype 
    * Note that it has to be accessible e.g. public inner class 
    * @return
    */
   protected abstract Class<T> getElementClass();
   
   
    
   /***
    * Reads a configurations list and builds list of configBeans of type bean class
    * @param configs
    * @param beanList
    */
   protected  void readConfigurationstoList( )  
   {  //unfortunately we can not get T.class to class has be passed as variable 
      try
      {
         childConfigs.clear();
         T beanObject = ReflectionUtil.createInstance(getElementClass());
         @SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> configs = getConfig().configurationsAt(beanObject.getPath());
         for(HierarchicalConfiguration hConfig : configs)
         {
            beanObject.loadConfiguration(hConfig);
            childConfigs.add(beanObject);
            beanObject = ReflectionUtil.createInstance(getElementClass());
         }
      }
      catch(Exception e)
      {
         throw new ConfigException(e);
      }
   }
   
   

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("ConfigBeanList [childConfigs=");
      builder.append(childConfigs);
      builder.append("]");
      return builder.toString();
   }

   @Override
   public Iterator<T> iterator()
   { 
      return childConfigs.iterator();
   }
    
   public int size()
   { 
      return childConfigs.size();
   }
 
   public boolean isEmpty()
   { 
      return childConfigs.isEmpty();
   }
 
   public boolean contains(Object o)
   { 
      return childConfigs.contains(o);
   }
 
   public int indexOf(Object o)
   { 
      return childConfigs.indexOf(o);
   }
 
   public int lastIndexOf(Object o)
   { 
      return childConfigs.lastIndexOf(o);
   }
 
    
   public Object[] toArray()
   { 
      return childConfigs.toArray();
   }
 
   
 
   public T get(int index)
   { 
      return childConfigs.get(index);
   }
 
   public T set(int index, T element)
   { 
      return childConfigs.set(index, element);
   }
 
   public boolean add(T e)
   { 
      return childConfigs.add(e);
   }
 
   public void add(int index, T element)
   { 
      childConfigs.add(index, element);
   }
 
   public T remove(int index)
   { 
      return childConfigs.remove(index);
   }
 
   public boolean remove(Object o)
   { 
      return childConfigs.remove(o);
   }
 
   public void clear()
   { 
      childConfigs.clear();
   }
 
   public boolean addAll(Collection<? extends T> c)
   { 
      return childConfigs.addAll(c);
   }
 
   public boolean addAll(int index, Collection<? extends T> c)
   { 
      return childConfigs.addAll(index, c);
   }

   @Override
   public <X> X[] toArray(X[] a)
   {
      return childConfigs.toArray(a);
   }

   @Override
   public boolean containsAll(Collection<?> c)
   { 
      return childConfigs.containsAll(c);
   }

   @Override
   public boolean removeAll(Collection<?> c)
   { 
      return childConfigs.removeAll(c);
   }

   @Override
   public boolean retainAll(Collection<?> c)
   {
    
      return childConfigs.retainAll(c);
   }

   @Override
   public ListIterator<T> listIterator()
   { 
      return childConfigs.listIterator();
   }

   @Override
   public ListIterator<T> listIterator(int index)
   { 
      return childConfigs.listIterator(index);
   }

   @Override
   public List<T> subList(int fromIndex, int toIndex)
   {
      
      return childConfigs.subList(fromIndex, toIndex);
   }
   
   
   
}

package com.oselan.common.file;

import java.io.File;
import java.io.IOException;

public class FileUtil
{
   /***
    * Attempts to get the absolute path based on the passed path parameter
    * 
    * @param path
    * @return absolute path to file or null if not found
    */
   public static String getFilePath(String path)
   {
      File tmp = new File(path);
      try
      {
         if(tmp.exists())
            path = tmp.getCanonicalPath();
         else
         {// attempt this being a relative path
            tmp = new File(".");
            tmp = new File(tmp.getAbsoluteFile().getParentFile(), path);
            if (tmp.exists())
               path = tmp.getCanonicalPath();
         }
      }
      catch(IOException e)
      {
         path = null;
      }
      System.out.println(path);
      return path;
   }
   
   
   /***
    * Returns the size of file in bytes if it exists -1 if not
    * @param file
    * @return
    */
   public static long getFileSize(File file)
   {

      if(!file.exists() || !file.isFile())
      {
         return -1;
      }
      // Here we get the actual size
      return file.length();
   }
   
   
   /****
    * rolls a file by adding an index before the extension
    * If new filename_<index>.extension exists index is incremented
    * @param file
    */
   public static void backupFile(File file)
   { 
      if (file.exists())
      {
         int count = 1; 
         String[] fileNameParts = file.getName().split("\\.");  
         String newFullPath = file.getParent() + File.separator + fileNameParts[0] + "_%d." + fileNameParts[1];
         File newFile = new File(String.format(newFullPath,count++) ); 
         while(newFile.exists()) 
         {
            newFile = new File(String.format(newFullPath,count++) ); 
         }
         file.renameTo(newFile);
      }
   }
   
   
   /***
    * 
    * Appends a value at the end of the file name but before the extension if it exists
    * @param fileName
    * @return
    */
   public static String appendFileNameSuffix(String fileName,String suffix)
   {
      int lastIndexofSeparator = fileName.lastIndexOf(File.separatorChar);
      String newFileName ;
      if (lastIndexofSeparator > 0 )
      {
         String extension = fileName.substring(lastIndexofSeparator  , fileName.length());
         String simpleFileName = fileName.substring(0 , lastIndexofSeparator); 
         newFileName = simpleFileName + suffix + extension;
      }
      else //not extension
         newFileName = fileName + suffix;
      return newFileName;
   }

}

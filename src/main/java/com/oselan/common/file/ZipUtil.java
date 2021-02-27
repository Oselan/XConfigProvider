package com.oselan.common.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 
 * @author ahmad
 * 
 */
public class ZipUtil
{
   /**
    * Size of the buffer to read/write data
    */
   private static final int BUFFER_SIZE = 4096;

   /**
    * Determine whether a file is a ZIP File.
    */
   public static boolean isZipFile(File file) throws IOException
   {
      DataInputStream in = null;
      try
      {
         if(file.isDirectory())
         {
            return false;
         }
         if(!file.canRead())
         {
            throw new IOException("Cannot read file " + file.getAbsolutePath());
         }
         if(file.length() < 4)
         {
            return false;
         }
         in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
         int test = in.readInt();

         return test == 0x504b0304;
      }
      finally
      {
         if(in != null) in.close();
      }

   }

   /**
    * Extracts a zip file specified by the zipFilePath to same directory as the specified file
    * 
    * 
    * @param zipFilePath
    * @param destDirectory
    * @throws IOException
    */
   public static List<File> unzip(String zipFilePath) throws IOException
   {
      return unzip(zipFilePath, new File(zipFilePath).getParent());
   }

   /**
    * Extracts a zip file specified by the zipFilePath to a directory specified
    * by destDirectory (will be created if does not exists)
    * 
    * @param zipFilePath
    * @param destDirectory
    * @throws IOException
    */
   public static List<File> unzip(String zipFilePath, String destDirectory) throws IOException
   {
      List<File> filesList = new ArrayList<File>();
      ZipInputStream zipIn = null;
      try
      {
         File destDir = new File(destDirectory);
         if(!destDir.exists())
         {
            destDir.mkdir();
         }
         zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
         ZipEntry entry = zipIn.getNextEntry();
         // iterates over entries in the zip file
         while(entry != null)
         {
            String filePath = destDirectory + File.separator + entry.getName();
            if(!entry.isDirectory())
            {
               // if the entry is a file, extracts it
               extractFile(zipIn, filePath);
               filesList.add(new File(filePath));
            }
            else
            {
               // if the entry is a directory, make the directory
               File dir = new File(filePath);
               dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
         }
         
      }
      finally
      {
         if(zipIn != null)
            zipIn.close();
      }
      return filesList;
   }

   /**
    * Extracts a zip entry (file entry)
    * 
    * @param zipIn
    * @param filePath
    * @throws IOException
    */
   private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException
   {
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
      byte[] bytesIn = new byte[BUFFER_SIZE];
      int read = 0;
      while((read = zipIn.read(bytesIn)) != -1)
      {
         bos.write(bytesIn, 0, read);
      }
      bos.close();
   }
   
   
    
    
   
   public static void main(String[] args)
   {
      try
      {
         String file = "c:\\users\\toshiba\\desktop\\ABF028FILE_20150902_1359_MONROE.zip";
         boolean isZipFile = ZipUtil.isZipFile(new File(file));
         if(isZipFile)
         {
            List<File> filesList = ZipUtil.unzip(file);
            for(File f : filesList)
            {
               System.out.println(f.getAbsolutePath());
            }
         }
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}

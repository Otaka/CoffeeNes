package com.smalljnes.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * @author Dmitry
 */
public class LineComparator {
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Line comparator");
        File file1=new File("c:\\temp\\cpuJava.log");
        File file2=new File("f:\\temp\\nes\\cpu_laines.log");
        
        Scanner scanner1=new Scanner(file1);
        Scanner scanner2=new Scanner(file2);
        
        int lineNumber=0;
        while(true){
            lineNumber++;
            if(scanner1.hasNextLine()&&!scanner1.hasNextLine()){
                System.out.println("File ["+file1.getAbsolutePath()+"] is bigger than second file"); 
                return;
            }
            if(!scanner1.hasNextLine()&&scanner1.hasNextLine()){
                System.out.println("File ["+file2.getAbsolutePath()+"] is bigger than first file"); 
                return;
            }
            if(!scanner1.hasNextLine()&&!scanner2.hasNextLine()){
                break;
            }
            String line1=scanner1.nextLine().trim();
            String line2=scanner2.nextLine().trim();
            if(lineNumber<39){
                continue;
            }
            if(!line1.equals(line2)){
                System.out.println("Difference at line "+lineNumber);
                System.out.println("Line 1:"+line1);
                System.out.println("Line 2:"+line2);
                return;
            }
            
        }
        System.out.println("No difference");
    }
}

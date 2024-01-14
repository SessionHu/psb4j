/*
* MIT License
* 
* Copyright (c) 2024 SessionHu
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/


package tk.xhuoffice.psb4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

 

public class Builder {

    String buildpath;
    File sourcepath;
    String pwd;

    public Builder(String buildpath, String sourcepath, String pwd) {
        this.buildpath = buildpath;
        this.sourcepath = new File(sourcepath);
        if(pwd.endsWith("\\") || pwd.endsWith("/")) {
            pwd = pwd.substring(0,pwd.length()-1);
        }
        this.pwd = pwd;
    }

    private List<String> addClassPath(List<String> cmdargs) {
        cmdargs.add("-cp");
        List<String> cp = new ArrayList<>();
        // base cp
        cp.add(this.pwd);
        cp.add(this.sourcepath.getAbsolutePath());
        cp.add(this.buildpath);
        // public lib
        File[] ls;
        if((ls=new File(System.getProperty("user.home")+"/.sessx/lib/").listFiles())!=null) {
            for(File file : ls) {
                if(file.isFile() && isZip(file)) {
                    // add
                    try {
                        cp.add(file.getCanonicalPath());
                    } catch(IOException e) {
                        cp.add(file.getAbsolutePath());
                    }
                }
            }
        }
        // work dir lib
        if((ls=new File(this.pwd+"/lib").listFiles())!=null) {
            for(File file : ls) {
                if(file.isFile() && isZip(file)) {
                    // add
                    try {
                        cp.add(file.getCanonicalPath());
                    } catch(IOException e) {
                        cp.add(file.getAbsolutePath());
                    }
                }
            }
        }
        // add to cmdargs
        StringBuilder classpathstr = new StringBuilder();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        for(String path : cp) {
            classpathstr.append(path);
            if(isWindows) {
                classpathstr.append(';');
            } else {
                classpathstr.append(':');
            }
        }
        cmdargs.add(classpathstr.deleteCharAt(classpathstr.length()-1).toString());
        return cmdargs;
    }

    /**
     * Detect if file is {@code ZIP} archive through file header.
     * @param file  file
     * @return      Result.
     */
    public static boolean isZip(File file) {
        if(file==null || file.isDirectory()) {
            return false;
        }
        try(InputStream in = new FileInputStream(file)) {
            if(in.read()!='P') {
                return false;
            }
            return in.read()=='K';
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> addSources(File sourcepath, List<String> cmdargs) {
        File[] ls = sourcepath.listFiles();
        if(ls==null) {
            return cmdargs;
        }
        for(File file : ls) {
            if(file.isDirectory()) {
                cmdargs = addSources(file,cmdargs);
            }
            if(file.isFile()) {
                try {
                    cmdargs.add(file.getCanonicalPath());
                } catch(IOException e) {
                    cmdargs.add(file.getAbsolutePath());
                }
            }
        }
        return cmdargs;
    }

    public static void rm(String path) {
        File file = new File(path);
        if(!file.exists()) {
            return;
        }
        if(file.isDirectory()) {
            File[] ls = file.listFiles();
            for(File f : ls) {
                try {
                    rm(f.getCanonicalPath());
                } catch(IOException e) {
                    rm(f.getAbsolutePath());
                }
            }
        } else {
            file.delete();
        }
    }

    /**
     * Run command {@code javac}.
     * @return  Exit code of command {@code javac}.
     */
    public int javac() {
        // base args
        new File(this.buildpath).mkdirs();
        String[] baseargs = new String[]{
            "javac",
            "-encoding","UTF-8",
            "-Xlint:deprecation","-XDignore.symbol.file","-Xdiags:verbose",
            "-d",buildpath,
            "-sourcepath",this.sourcepath.getAbsolutePath()
        };
        List<String> cmdargs = new ArrayList<>();
        for(String arg : baseargs) {
            cmdargs.add(arg);
        }
        // other args
        cmdargs = addSources(this.sourcepath,cmdargs);
        cmdargs = addClassPath(cmdargs);
        // new command
        Command javac = new Command("javac",cmdargs.toArray(new String[0]),this.pwd);
        // run
        Main.printDividingLine();
        System.out.print(javac.status());
        Main.printDividingLine();
        try {
            javac.run();
        } catch(IOException e) {
            e.printStackTrace();
        }
        Main.printDividingLine();
        System.out.print(javac.status());
        return javac.getExitCode();
    }

    /**
     * Copy file to target directory.
     * @param sourceFile  paths of source files
     * @param targetDir   target directory
     */
    public static void copyFile(String[] sourceFile, String targetDir) {
        // mkdirs
        if(targetDir.endsWith("\\") || targetDir.endsWith("/")) {
            targetDir = targetDir.substring(0,targetDir.length()-1);
        }
        Downloader.checkParentDir(targetDir+"/empty");
        // copy
        for(String filepath : sourceFile) {
            File file = new File(filepath);
            try(InputStream in = new FileInputStream(file);
                OutputStream out = new FileOutputStream(targetDir+"/"+file.getName())) {
                while(in.available()>0) {
                    out.write(in.read());
                }
            } catch(java.io.FileNotFoundException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Package the files in the {@code build} directory into a {@code JAR} package with command {@code jar}.
     * @param jarpath  path of JAR package
     * @param manifet  path of manifest
     * @return         Exit code of command {@code jar}.
     */
    public int jar(String jarpath, String manifest) {
        // rm jar
        new File(jarpath).delete();
        // if manifest exists
        String[] cmd;
        if(new File(manifest).exists()) {
            cmd = new String[]{
                "jar","-cvfm",jarpath,manifest,"-C",this.buildpath,"."
            };
        } else {
            cmd = new String[]{
                "jar","-cvf",jarpath,"-C",this.buildpath,"."
            };
        }
        // pack
        Command jar = new Command(cmd,this.pwd);
        Main.printDividingLine();
        System.out.print(jar.status());
        Main.printDividingLine();
        try {
            jar.run();
        } catch(IOException e) {
            e.printStackTrace();
        }
        Main.printDividingLine();
        System.out.print(jar.status());
        return jar.getExitCode();
    }

    public static void download(String[] urls, String targetDir) {
        for(String url : urls) {
            try {
                Downloader downloader = new Downloader(url,targetDir);
                downloader.download();
            } catch(IOException e) {
                e.printStackTrace();
            } catch(IllegalStateException e) {
                // do nothing...
            }
        }
    }

}
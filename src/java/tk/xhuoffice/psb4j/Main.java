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

import java.util.ArrayList;
import java.util.List;



public class Main {

    public static final String NAME;
    public static final String VERSION;
    static {
        Package pkg = Main.class.getClass().getPackage();
        NAME = pkg.getImplementationTitle();
        VERSION = pkg.getImplementationVersion();
    }

    private Main() {}

    /**
     * Program entrance.
     * @param args  command arguments
     */
    public static void main(String[] args) {
        long starttime = System.currentTimeMillis();
        // arguments
        cmdargs(args);
        // download libs
        Builder.download(remote,System.getProperty("user.home")+"/.sessx/lib/");
        // new Builder
        Builder builder = new Builder(buildpath,sourcepath,pwd);
        // javac
        int javac = builder.javac();
        // pack
        if(javac==0) {
            Builder.copyFile(resources, buildpath, false);
            Builder.copyFile(extra,buildpath);
            builder.jar(jarpath,manifest);
            printDividingLine();
            System.out.println("Done! ("+(System.currentTimeMillis()-starttime)+"ms)");
        } else {
            printDividingLine();
            System.out.println("Failed! ("+(System.currentTimeMillis()-starttime)+"ms)");
        }
    }

    private static final String DIVIDING_LINE = "================================================\n";

    private static void printVersionInfo() {
        System.out.printf("%s %s\n", NAME, VERSION);
    }

    private static void printHelpInfo() {
        System.out.print(
            "    --version        Print version message\n"+
            "    --help           Print this help message\n"+
            "    --jar            Path of output JAR\n"+
            "    --manifest       Manifest for JAR\n"+
            "    --pwd            Set work dirctory\n"+
            "    --build-dirctory Set output dirctory\n"+
            "    --sourcepath     Where to find .java code\n"+
            "    --resources      Copy files in resources to JAR\n"+
            "    --remote-lib     Remote library URL\n"+
            "    --extra-packin   Extra files added to JAR\n"+
            "    --clear          Remove files at output dirctory\n"
        );
    }

    public static void printDividingLine() {
        System.out.print(DIVIDING_LINE);
    }

    static String jarpath = "./build/build.jar";
    static String manifest = "./manifest";
    static String pwd = System.getProperty("user.dir");
    static String buildpath = "./build";
    static String sourcepath = "./src/java";
    static String[] resources = {"./src/resources"};
    static String[] remote = new String[0];
    static String[] extra = new String[0];

    private static void cmdargs(String[] argv) {
        if(argv==null || argv.length==0) {
            return;
        }
        // to list
        List<String> args = new ArrayList<>();
        for(String arg : argv) {
            args.add(arg);
        }
        int i;
        // process
        if((i=args.indexOf("--version"))>-1) {
            args.remove(i);
            printVersionInfo();
            System.exit(0);
        }
        if((i=args.indexOf("--help"))>-1) {
            args.remove(i);
            printVersionInfo();
            printHelpInfo();
            System.exit(0);
        }
        if((i=args.indexOf("--jar"))>-1) {
            args.remove(i);
            jarpath = args.get(i);
            args.remove(i);
        }
        if((i=args.indexOf("--manifest"))>-1) {
            args.remove(i);
            manifest = args.get(i);
            args.remove(i);
        }
        if((i=args.indexOf("--pwd"))>-1) {
            args.remove(i);
            pwd = args.get(i);
            args.remove(i);
        }
        if((i=args.indexOf("--build-directory"))>-1) {
            args.remove(i);
            buildpath = args.get(i);
            args.remove(i);
        }
        if((i=args.indexOf("--sourcepath"))>-1) {
            args.remove(i);
            sourcepath = args.get(i);
            args.remove(i);
        }
        if((i=args.indexOf("--resources"))>-1) {
            args.remove(i);
            resources = args.get(i).split(",");
            args.remove(i);
        }
        if((i=args.indexOf("--remote-lib"))>-1) {
            args.remove(i);
            remote = args.get(i).split(",");
            args.remove(i);
        }
        if((i=args.indexOf("--extra-packin"))>-1) {
            args.remove(i);
            extra = args.get(i).split(",");
            args.remove(i);
        }
        if((i=args.indexOf("--clear"))>-1) {
            args.remove(i);
            Builder.rm(buildpath);
        }
    }

}

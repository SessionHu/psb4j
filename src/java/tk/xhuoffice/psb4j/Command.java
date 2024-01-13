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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class Command {

    private String name;

    /**
     * Get name of command.
     * @return name of command
     */
    public String getName() {
        return this.name;
    }

    private String[] command;

    /**
     * Get command.
     * @return command
     */
    public String[] getCommand() {
        return this.command;
    }

    private File pwd;

    /**
     * Get command.
     * @return command
     */
    public File getPwd() {
        return this.pwd;
    }

    private Process process = null;
    
    public Command(String name, String[] command, String pwd) {
        if(command==null) {
            throw new NullPointerException("command");
        }
        if(command.length==0) {
            throw new IllegalArgumentException("command length was 0");
        }
        if(pwd==null || pwd.trim().isEmpty()) {
            pwd = System.getProperty("user.dir");
        }
        if(name==null || name.trim().isEmpty()) {
            name = command[0];
        }
        this.name = name;
        this.command = command;
        this.pwd = new File(pwd);
        this.pwd.mkdirs();
    }
    
    public Command(String[] command, String pwd) {
        this(null,command,pwd);
    }
    
    public Command(String[] command) {
        this(null,command,null);
    }

    /**
     * Run command.
     * @throws IOException  if an I/O error occurs.
     */
    public void run() throws IOException {
        // process
        this.process = new ProcessBuilder(command).directory(pwd).start();
        // stream
        InputStream in = this.process.getInputStream();
        InputStream err = this.process.getErrorStream();
        OutputStream out = this.process.getOutputStream();
        // read & write
        while(this.process.isAlive()) {
            if(System.in.available()>0) {
                out.write(System.in.read());
            }
            if(in.available()>0) {
                System.out.write(in.read());
            }
            if(err.available()>0) {
                System.err.write(err.read());
            }
        }
        if(this.process.exitValue()!=0) {
            System.err.print("Process exit with code ");
            System.err.print(this.process.exitValue());
            System.err.println("!");
        }
    }

    /**
     * Get process exit code.
     * @return Exit code.
     */
    public int getExitCode() {
        return this.process.exitValue();
    }

    private static final String DIVIDING_LINE = "------------------------------------------------\n";

    /**
     * Report command status.
     * @return Formatted command status.
     */
    public String status() {
        StringBuilder sb = new StringBuilder();
        sb.append("Command Information: \n");
        sb.append(DIVIDING_LINE);
        // command
        {
            // name
            sb.append("Name: ");
            sb.append(this.name);
            sb.append('\n');
            // command
            sb.append("Command: ");
            for(String subcmd : this.command) {
                sb.append(subcmd);
                sb.append(" ");
            }
            sb.append("\n");
        }
        if(this.process==null) {
            return sb.toString();
        }
        sb.append(DIVIDING_LINE);
        // process
        {
            // process
            sb.append("Process: ");
            sb.append(this.process);
            sb.append(" \n");
            // isalive
            sb.append("Running: ");
            sb.append(this.process.isAlive());
            sb.append(" \n");
            // exit code
            sb.append("Exit code: ");
            if(!this.process.isAlive()) {
                sb.append(this.process.exitValue());
            } else {
                sb.append("not finished yet");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
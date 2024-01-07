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
    
    public Command(String name, String[] command) {
        if(command==null) {
            throw new NullPointerException("command was null");
        }
        if(command.length==0) {
            throw new IllegalArgumentException("command length was 0");
        }
        if(name==null || name.trim().isEmpty()) {
            name = command[0];
        }
        this.name = name;
        this.command = command;
    }
    
    public Command(String[] command) {
        this(null,command);
    }

}
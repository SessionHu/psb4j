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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;


public class Downloader {
    
    private File file = null;
    private String path = null;
    private String fname = null;
    private HttpURLConnection conn = null;
    private OutputStream out = null;

    private String status = "init";
    
    private String contentType = null;
    
    public Downloader(String url, String fileDirPath) throws IOException, URISyntaxException {
        // file & path
        if(!fileDirPath.endsWith("/")) {
            fileDirPath += "/";
        }
        URL url0 = new URI(url).toURL();
        this.fname = Paths.get(url0.getPath()).getFileName().toString();
        this.path = fileDirPath + this.fname;
        this.file = new File(this.path);
        if(this.file.exists()) {
            throw new IllegalStateException("File already exists");
        } else {
            checkParentDir(this.file.getAbsolutePath());
        }
        this.out = new FileOutputStream(this.file);
        // create connection
        this.conn = setGetConnURL(url0);
        // return
        this.status = "ready";
        return;
    }
    
    /**
     * Set {@code HttpURLConnection} with request method GET.
     * @param url  URL.
     * @return     {@code HttpURLConnection} with request method GET.
     */
    public static HttpURLConnection setGetConnURL(URL url) throws IOException {
        // print debug log
        System.out.println("设置请求到 "+url);
        // 打开连接
        HttpURLConnection conn = null;
        conn = (HttpURLConnection)url.openConnection();
        // 设置请求方法为 GET
        conn.setRequestMethod("GET");
        // 设置连接超时时间
        conn.setConnectTimeout(5000);
        // 设置读取超时时间
        conn.setReadTimeout(10000);
        // 设置 User-Agent 请求头
        conn.setRequestProperty("User-Agent",USER_AGENT);
        // return
        return conn;
    }

    public static final String USER_AGENT = String.format("Psb4j/%s (%s %s %s) Java/%s",
                                                            Main.VERSION,
                                                            System.getProperty("os.name"),
                                                            System.getProperty("os.version"),
                                                            System.getProperty("os.arch"),
                                                            System.getProperty("java.version"));
    
    public static void checkParentDir(String path) {
        File parentDir = new File(path).getParentFile();
        if(parentDir!=null) {
            if(!parentDir.exists()) { // 当父目录不存在时
                String parentPath;
                try {
                    parentPath = parentDir.getCanonicalPath();
                } catch(IOException e) {
                    e.printStackTrace();
                    parentPath = parentDir.getAbsolutePath();
                }
                checkParentDir(parentPath);
                System.out.println("正在创建目录 "+parentDir.getName());
                parentDir.mkdir();
                if(parentDir.getName().startsWith(".")&&System.getProperty("os.name").toLowerCase().contains("windows")) {
                    // 仅在 Windows 下隐藏目录  (类Unix无隐藏属性)
                    System.out.printf("正在设置 %s 隐藏属性\n",parentDir.getName());
                    try {
                        new Command(new String[]{"attrib", "+H", parentPath}).run();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private long length = -1;
    public long getLength() {
        return this.length;
    }

    private long progress = 0;
    public long getProgress() {
        return this.progress;
    }
    
    /**
     * Download file.
     * @return Downloaded             file
     * @throws IllegalStateException  When file has already been downloaded or file could not be created
     */
    public File download() {
        // check if can download
        if(this.out==null||this.status.equals("init")) {
            throw new IllegalStateException("Connection could not be created");
        }
        if(this.conn==null||this.status.equals("finished")) {
            throw new IllegalStateException("File "+this.fname+" has already been downloaded");
        }
        // connect
        this.status = "downloading";
        try {
            conn.connect();
        } catch(IOException e) {
            e.printStackTrace();
            this.status = "failed";
            return this.file;
        }
        // file length
        this.length = this.conn.getContentLengthLong();
        System.out.println("File length: " + length);
        // download
        InputStream in = null;
        try {
            in = new BufferedInputStream(conn.getInputStream());
            // content type
            this.contentType = HttpURLConnection.guessContentTypeFromStream(in);
            if(this.contentType==null) {
                this.contentType = conn.getContentType();
            }
            System.out.println("File type: "+this.contentType);
            // download
            int bufferSize = 0;
            byte[] buffer = new byte[1024];
            this.progressReporter.start();
            while((bufferSize=in.read(buffer,0,1024))!=-1) {
                this.out.write(buffer,0,bufferSize);
                this.progress+=bufferSize;
            }
            try {
                this.progressReporter.join();
            } catch(InterruptedException e) {}
            System.out.println("文件 "+this.fname+" 下载完毕");
            this.status = "finished";
        } catch(IOException e) {
            e.printStackTrace();
            this.status = "failed";
        } finally {
            try {
                this.out.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
            try {
                if(in!=null) {
                    in.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            this.conn = null;
        }
        return this.file;
    }

    private Thread progressReporter = new Thread(() -> {
        while(!(this.progress>=this.length||this.conn==null)) {
            System.out.printf("Download progress: %d/%d (%d%s)\n", this.progress, this.length, this.progress*100L/this.length, "%");
            try {
                Thread.sleep(1000L);
            } catch(InterruptedException e) {}
        }
    },  "DownloadProgressReporter-"+this.fname);
    
    @Override
    public String toString() {
        return "Downloader-"+this.fname;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        long result = 1454;
        result = prime * result + this.path.hashCode();
        result = prime * result + this.fname.hashCode();
        result = prime * result + this.file.hashCode();
        if(this.conn!=null) {
            result = prime * result + this.conn.hashCode();
        }
        if(this.out!=null) {
            result = prime * result + this.out.hashCode();
        }
        if(this.contentType!=null) {
            result = prime * result + this.contentType.hashCode();
        }
        result = prime * result + this.toString().hashCode();
        result = prime * result + this.length;
        result = prime * result + this.progress;
        result = prime * result + this.status.hashCode();
        return (int)result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj==this) {
            return true;
        }
        if(obj==null) {
            return false;
        }
        if(obj instanceof Downloader) {
            Downloader dlr = (Downloader)obj;
            return dlr.hashCode()==this.hashCode();
        }
        return false;
    }
    
}

package org.xjy.android.nova.common.net;

import android.text.TextUtils;

import org.xjy.android.nova.common.io.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AtomProxy {



    public static class RequestHandler {
//        private Socket mClientSocket;
//        private InputStream mClientInputStream;
//        private OutputStream mClientOutputStream;
//        private Socket mServerSocket;
//        private InputStream mServerInputStream;
//        private OutputStream mServerOutputStream;
//
//        public RequestHandler(Socket clientSocket) {
//            mClientSocket = clientSocket;
//        }
//
//        @Override
//        public void run() {
//            try {
//                mClientInputStream = mClientSocket.getInputStream();
//                mClientOutputStream = mClientSocket.getOutputStream();
//                int length = 0;
//                byte[] buffer = new byte[8192];
//                String line = IoUtils.readLine(mClientInputStream);
//                String[] statusEntries = line.split(" ");
//                String method = statusEntries[0];
//                String uri = statusEntries[1];
//                if ("CONNECT".equalsIgnoreCase(method)) {
//                    while(true) {
//                        line = IoUtils.readLine(mClientInputStream);
//                        if (TextUtils.isEmpty(line)) {
//                            break;
//                        }
//                    }
//                    uri = uri.replace("https://", "");
//                    int pathIndex = uri.indexOf("/");
//                    if (pathIndex != -1) {
//                        uri = uri.substring(0, pathIndex);
//                    }
//                    String[] authority = uri.split(":");
//                    String host = authority[0];
//                    int port = authority.length > 1 ? Integer.parseInt(authority[1]) : 443;
//
//                    mServerSocket = new Socket(host, port);
//                    mServerInputStream = mServerSocket.getInputStream();
//                    mServerOutputStream = mServerSocket.getOutputStream();
//
//                    mClientOutputStream.write("HTTP/1.1 200 Connection established\r\nProxy-Agent: webviewproxy\r\n\r\n".getBytes());
//
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            int length = 0;
//                            byte[] buffer = new byte[8192];
//                            try {
//                                while ((length = mClientInputStream.read(buffer)) != -1) {
//                                    mServerOutputStream.write(buffer, 0, length);
//                                }
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    while ((length = mServerInputStream.read(buffer)) != -1) {
//                        mClientOutputStream.write(buffer, 0, length);
//                    }
//                    mClientOutputStream.close();
//                } else if ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
//                    boolean isPost = "POST".equalsIgnoreCase(method);
//                    HttpRequestBase request = isPost ? new HttpPost(uri) : new HttpGet(uri);
//                    while ((line = readLine(mClientInputStream)) != null) {
//                        if (TextUtils.isEmpty(line)) {
//                            break;
//                        }
//                        int separatorIndex = line.indexOf(":");
//                        String name = line.substring(0, separatorIndex);
//                        if (name.equalsIgnoreCase("Proxy-Connection")) {
//                            continue;
//                        }
//                        request.addHeader(name, line.substring(separatorIndex + 1).trim());
//                    }
//                    if(mClientSocket.isOutputShutdown())	mClientOutputStream = mClientSocket.getOutputStream();
//                    if(mClientSocket.isInputShutdown())	mClientInputStream = mClientSocket.getInputStream();
//                    if (isPost) {
//                        long contentLength = 0;
//                        Header contentLengthHeader = request.getFirstHeader("Content-Length");
//                        if (contentLengthHeader != null) {
//                            contentLength = Long.parseLong(contentLengthHeader.getValue());
//                        }
//                        System.out.println(">>>content-length: " + contentLength);
//                        request.removeHeaders("Content-Length");
//                        ((HttpPost) request).setEntity(new InputStreamEntity(mClientInputStream, contentLength));
//                    }
//
//                    DefaultHttpClient httpClient = HttpClientFactory.createSingletonHttpClient();
//                    HttpResponse response = httpClient.execute(request);
//                    mServerInputStream = response.getEntity().getContent();
//
//                    mClientOutputStream.write((response.getStatusLine().toString() + "\r\n").getBytes());
//                    Header[] headers = response.getAllHeaders();
//                    for (int i = 0; i < headers.length; i++) {
//                        if (headers[i].getName().equalsIgnoreCase("Transfer-Encoding")) {
//                            continue;
//                        }
//                        mClientOutputStream.write((headers[i].getName() + ": " + headers[i].getValue() + "\r\n").getBytes());
//                    }
//                    mClientOutputStream.write("\r\n".getBytes());
//                    while ((length = mServerInputStream.read(buffer)) != -1) {
//                        mClientOutputStream.write(buffer, 0, length);
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (mServerSocket != null) {
//                    try {
//                        IoUtils.closeSilently(mServerSocket.getOutputStream());
//                        IoUtils.closeSilently(mServerSocket.getInputStream());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    IoUtils.closeSilently(mServerOutputStream);
//                    IoUtils.closeSilently(mServerInputStream);
//                }
//                IoUtils.closeSilently(mServerSocket);
//                try {
//                    IoUtils.closeSilently(mClientSocket.getOutputStream());
//                    IoUtils.closeSilently(mClientSocket.getInputStream());
//                    IoUtils.closeSilently(mClientSocket);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }


}

package com.fgt.commons;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DownLoad {

    public static void down(InputStream inputStream, BufferedOutputStream outputStream) {
        try {
            byte[] b = new byte[1024];
            int i;
            while ((i = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {

                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }


}

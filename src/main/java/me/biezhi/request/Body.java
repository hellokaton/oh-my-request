package me.biezhi.request;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Body {

    private InputStream inputStream;

    public Body(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getString() throws IOException {
        if(null != inputStream){
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuffer body = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                body.append(line).append("\r\n");
            }
            rd.close();
            return body.toString();
        }
        return null;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}

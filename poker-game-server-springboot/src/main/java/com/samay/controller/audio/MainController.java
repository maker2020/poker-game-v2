package com.samay.controller.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/audios")
@RestController
public class MainController {
    
    @RequestMapping("/{audioName}")
    public Object getAudio(@PathVariable("audioName") String fileName,HttpServletResponse response){
        URL resource = getClass().getClassLoader().getResource("audio/"+fileName);
        try (InputStream openStream = resource.openStream()) {
            byte[] data=openStream.readAllBytes();
            OutputStream out=response.getOutputStream();
            out.write(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

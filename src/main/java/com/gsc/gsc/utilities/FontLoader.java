package com.gsc.gsc.utilities;

import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.commons.io.IOUtils; // Add this library to read InputStream to byte array

public class FontLoader {

    public static Font  loadArabicFont() throws IOException, FontFormatException {
        // Load the font as a resource from the classpath
        InputStream fontStream = FontLoader.class.getResourceAsStream("/font/NotoNaskhArabic-Regular.ttf");
        InputStream fontStream2 = FontLoader.class.getClassLoader().getResourceAsStream("font/NotoNaskhArabic-Regular.ttf");

        URL resourceUrl = FontLoader.class.getClassLoader().getResource("font/NotoNaskhArabic-Regular.ttf");
        System.out.println("Resource URL: " + resourceUrl);
        if (fontStream == null ) {
            if( fontStream2==null) {
                throw new IOException("Font file not found in JAR");
            }else{
                System.out.println("===========loaded from FontStream2=============");
                fontStream = fontStream2;
            }
        }

        // Create the font from the InputStream
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(font);
        return font;
    }
}

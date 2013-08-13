package ru.vagafonova.example.NasaDailyImage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class test extends DefaultHandler {
    private static final String TAG = IotdHandler.class.getSimpleName();
    Bitmap image;
    String chars;
    boolean itemFlag = false;
    private String url = null;
    private StringBuffer title = new StringBuffer();
    private StringBuffer description = new StringBuffer(5000);
    private String date = null;

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if (qName.equals("item")) {
            itemFlag = true;
        }
        if (qName.equals("enclosure")) {
            this.url = attributes.getValue("url");
            fetchImage(this.url);
        }
    }

    protected Bitmap fetchImage(String url) {

        try {
            System.out.println("Image URL = " + url);
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            this.image = BitmapFactory.decodeStream(input);
            input.close();
            return image;
        } catch (IOException ioe) {
            Log.e("", ioe.toString());
            ioe.printStackTrace();
            return null;
        }

    }

    public void characters(char ch[], int start, int length) {
        //System.out.println(new String(ch));
        chars = new String(ch, start, length);

    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {

        if (itemFlag) {
            if (element.equals("title")) {
                title.append(chars);
            }
            if (element.equals("pubDate")) {
                //Example: Tue, 21 Dec 2010 00:00:00 EST
                String rawDate = chars;
                try {
                    SimpleDateFormat parseFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                    Date sourceDate = parseFormat.parse(rawDate);

                    SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
                    date = outputFormat.format(sourceDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (element.equals("description")) {
                description.append(chars);
            }
        }
    }

    public void processFeed(String url) {
        try {

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            xr.parse(new InputSource(new URL(url).openStream()));

        } catch (IOException e) {
            Log.e("", e.toString());
            e.printStackTrace();
        } catch (SAXException e) {
            Log.e("", e.toString());
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            Log.e("", e.toString());
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title.toString();
    }

    public String getDescription() {
        return description.toString();
    }

    public String getDate() {
        return date;
    }

    public Bitmap getImage() {
        return this.image;
    }

}
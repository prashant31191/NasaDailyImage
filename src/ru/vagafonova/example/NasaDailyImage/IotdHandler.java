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

public class IotdHandler extends DefaultHandler {
    private static final String TAG = IotdHandler.class.getSimpleName();
    Bitmap image;
    private boolean inTitle = false;
    private boolean inDescription = false;
    private boolean inItem = false;
    private boolean inDate = false;

    private String url = null;
    private StringBuffer title = new StringBuffer();
    private StringBuffer description = new StringBuffer();
    private String date = null;

    private IotdHandlerListener listener;

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {



        if (localName.equals("enclosure")) {
            this.url = attributes.getValue("url");
            fetchImage(this.url);
        }

        if (localName.startsWith("item")) {
            inItem = true;
        } else {
            if (inItem) {
                if (localName.equals("title")) {
                    inTitle = true;
                } else {
                    inTitle = false;
                }

                if (localName.equals("description")) {
                    inDescription = true;
                } else {
                    inDescription = false;
                }

                if (localName.equals("pubDate")) {
                    inDate = true;
                } else {
                    inDate = false;
                }
            }
        }


    }

    public void characters(char ch[], int start, int length) {
        String chars = (new String(ch).substring(start, start + length));

        if (inTitle) {
            title.append(chars);
        }

        if (inDescription) {
            description.append(chars);
        }

        if (inDate && date == null) {
            //Example: Tue, 21 Dec 2010 00:00:00 EST
            String rawDate = chars;
            try {
                SimpleDateFormat parseFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
                Date sourceDate = parseFormat.parse(rawDate);

                SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
                date = outputFormat.format(sourceDate);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

    }

    public void processFeed() {
        try {
            URL url = new URL("http://www.nasa.gov/rss/image_of_the_day.rss");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            xr.parse(new InputSource(url.openStream()));

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (SAXException e) {
            Log.e(TAG, e.toString());
        } catch (ParserConfigurationException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void endElement(String uri, String localName, String qName) {
        if (url != null && title != null && description != null && date != null) {
            if (listener != null) {
                listener.iotdParsed(url, title.toString(), description.toString(), date);
                listener = null;
            }
        }
    }

    protected Bitmap fetchImage(String url) {
        try {
            HttpURLConnection connection =  (HttpURLConnection) new URL(url).openConnection();
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

    public Bitmap getImage() {
        return image;
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


    public IotdHandlerListener getListener() {
        return listener;
    }

    public void setListener(IotdHandlerListener listener) {
        this.listener = listener;
    }


}
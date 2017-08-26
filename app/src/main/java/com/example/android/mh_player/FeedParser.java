package com.example.android.mh_player;

import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

public class FeedParser {
    //Gets an input stream of a podcast RSS feed.
    //Returns an ArrayList of Item objects, each holding an episode's data.

    // Constants indicting XML element names that we're interested in
//    private static final int TAG_DESCRIPTION = 1;
//    private static final int TAG_TITLE       = 2;
//    private static final int TAG_PUBDATE     = 3;
//    private static final int TAG_LINK        = 4;
//    private static final int TAG_MP3URL      = 5;
//    private static final int TAG_DURATION    = 6;


    // We don't use XML namespaces
    private static final String ns = null;

    public ArrayList<Item> parse(InputStream in) {
        //Get an input stream of the RSS feed. Initialize it and
        //call readFeed().

        //Basic XML Structure:
        //<element (=start tag)>Text</element (=end tag)>

        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null); //InputEncoding -> null
            parser.nextTag(); //skip <xml>
            parser.nextTag(); //skip <rss>
            return readFeed(parser);
        } catch (XmlPullParserException e) {
            Log.d("FeedParser", "XmlPullParserException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("FeedParser", "nextTag(): IOException");
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }

        return null; //If readFeed fails -> null.
    }

    private ArrayList<Item> readFeed(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {
        //Looks for <item> tags and processes them.

        ArrayList<Item> entries = new ArrayList<Item>();

        //Podcast Feed Structure that we're looking for:
        //<channel>
        //  <item>
        //      <title>..</title>
        //      <link>..</link>
        //      <pubDate>..</pubDate>
        //      <description>..</description
        //      <enclosure>..<enclosure>
        //  </item>
        //  ..
        //<channel>

        //We assume we're in <channel>
        parser.require(XmlPullParser.START_TAG, ns, "channel");

        //next() moves the cursor to the next event (start/text/end/end_doc)
        while (parser.next() != XmlPullParser.END_TAG) {

            //we move the cursor forward until we find a start tag.
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            //We're looking for an <item> tag. When we find it we read it.
            if (name.equals("item")) {
                entries.add(readItem(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    private Item readItem(XmlPullParser parser)
            throws XmlPullParserException, IOException, ParseException {

        parser.require(XmlPullParser.START_TAG, ns, "item");

        String title        = null;
        String link         = null;
        String description  = null;
        String mp3URL       = null;
        String pubDate      = null;
        String duration     = null;

        while (parser.next() != XmlPullParser.END_TAG) {

            //We're looking for a start tag.
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();

            if (name.equals("title")) {
                // Example: <title>Article title</title>
                title = readBasicTag(parser, "title");

            } else if (name.equals("description")) {

                description = readBasicTag(parser, "description");

            } else if (name.equals("enclosure")){

                mp3URL = readEnclosure(parser);

            } else if (name.equals("link")) {

                link = readBasicTag(parser, "link");

            } else if (name.equals("itunes:duration")) {

                duration = readBasicTag(parser, "itunes:duration");

            } else if (name.equals("pubDate")) {
                //Example: <pubDate>Sun, 23 Jul 2017 11:14:13 +0000</pubDate>

                pubDate = readBasicTag(parser, "pubDate");

            } else {
                skip(parser);
            }
        }
        return new Item(title, link, description, mp3URL, pubDate, duration);
    }

    /**
     * Reads the body of a basic XML tag, which is guaranteed not to contain any nested elements.
     **/
    private String readBasicTag(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return result;
    }

    // Processes link tags in the feed.
    private String readEnclosure(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, ns, "enclosure");
        //String tag = parser.getName();
        String mp3Link = parser.getAttributeValue(null, "url");
        parser.nextTag();
        parser.require(XmlPullParser.END_TAG, ns, "enclosure");
        return mp3Link;
    }

    /**
     * For the tags title and summary, extracts their text values.
     */
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag(); //Move cursor next event, throw exception if not END_TAG.
        }
        return result;
    }

    /**
     * Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
     * if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
     * finds the matching END_TAG (as indicated by the value of "depth" being 0).
     */
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    //This class represents a single item (episode) in the XML feed.
    public static class Item {
        public final String title;
        public final String link;
        public final String description;
        public final String mp3URL;
        public final String pubDate;
        public final String duration;

        Item( String title,
              String link,
              String description,
              String mp3URL,
              String published,
              String duration) {
            this.title = title;
            this.link = link;
            this.pubDate = published;
            this.description = description;
            this.mp3URL = mp3URL;
            this.duration = duration;
        }
    }
}
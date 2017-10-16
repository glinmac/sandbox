package ltd.cylleneworks.sandbox.beam.twitter;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;
import java.util.List;

// A reduced version of a Tweet
@DefaultCoder(AvroCoder.class)
//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY )
public class TweetData implements Serializable {

    public String id;
    public String text;
    public String lang;
    public String created_at;
    public List<String> hashtags;

    TweetData() {}

}
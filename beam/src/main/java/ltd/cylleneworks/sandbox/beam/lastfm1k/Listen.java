package ltd.cylleneworks.sandbox.beam.lastfm1k;


import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
public class Listen implements Serializable {

    // userid \t timestamp \t musicbrainz-artist-id \t artist-name \t musicbrainz-track-id \t track-name
    String userId;
    @Nullable String timestamp;
    @Nullable String mbArtistId;
    @Nullable String artistName;
    @Nullable String mbTrackId;
    @Nullable String trackName;

    Listen() {}

    Listen(String userId) {
        this.userId = userId;
    }

    Listen(String userId, String timestamp, String mbArtistId, String artistName, String mbTrackId, String trackName) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.mbArtistId = mbArtistId;
        this.artistName = artistName;
        this.mbTrackId = mbTrackId;
        this.trackName = trackName;
    }

    public String getTimestamp() {  return timestamp; }
    public void setTimestamp(String timestamp) {  this.timestamp = timestamp; }

    public String getMbArtistId() { return mbArtistId; }
    public void setMbArtistId(String mbArtistId) { this.mbArtistId = mbArtistId; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getMbTrackId() { return mbTrackId; }
    public void setMbTrackId(String mbTrackId) { this.mbTrackId = mbTrackId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    static Listen Parse(String line) {
        String data[] = line.split("\t");
        if (data.length == 6) {
            return new Listen(data[0], data[1], data[2], data[3], data[4], data[5]);
        } else {
            return new Listen(data[0]);
        }
    }

    @Override
    public String toString() {
        return "Listen{" +
            "userId='" + userId + '\'' +
            ", timestamp='" + timestamp + '\'' +
            ", mbArtistId='" + mbArtistId + '\'' +
            ", artistName='" + artistName + '\'' +
            ", mbTrackId='" + mbTrackId + '\'' +
            ", trackName='" + trackName + '\'' +
            '}';
    }
}
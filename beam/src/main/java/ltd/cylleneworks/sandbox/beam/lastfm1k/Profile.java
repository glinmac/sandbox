package ltd.cylleneworks.sandbox.beam.lastfm1k;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

import java.io.Serializable;

@DefaultCoder(AvroCoder.class)
public class Profile implements Serializable {

    public static final Profile NULL = new Profile("NULL");

    private String userId;
    @Nullable private String gender;
    @Nullable private String age;
    @Nullable private String country;
    @Nullable private String registered;

    public Profile() {
    }

    public Profile(String userId) {
        this.userId = userId;
        this.gender = gender;
        this.age = age;
        this.country = country;
        this.registered = registered;
    }

    public Profile(String userId, String gender, String age, String country, String registered) {
        this.userId = userId;
        this.gender = gender;
        this.age = age;
        this.country = country;
        this.registered = registered;
    }

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getGender() {return gender;}
    public void setGender(String gender) {this.gender = gender;}

    public String getCountry() {return country;}
    public void setCountry(String country) {this.country = country;}

    public String getAge() { return age;}
    public void setAge(String age) {this.age = age;}

    public String getRegistered() {return registered;}
    public void setRegistered(String registered) {this.registered = registered;}

    static Profile Parse(String line) {
        String data[] = line.split("\t");
        if (data.length == 5) {
            return new Profile(data[0], data[1], data[2], data[3], data[4]);
        } else {
            return new Profile(data[0]);
        }
    }

    @Override
    public String toString() {
        return "Profile{" +
            "userId='" + userId + '\'' +
            ", gender='" + gender + '\'' +
            ", age='" + age + '\'' +
            ", country='" + country + '\'' +
            ", registered='" + registered + '\'' +
            '}';
    }
}
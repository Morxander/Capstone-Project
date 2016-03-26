package morxander.sexualharassmentreporter.items;

/**
 * Created by morxander on 3/20/16.
 */
public class Harassment {

    int id, city_id;
    String title, body,time;
    Double lat, lon;

    public Harassment() {
    }

    public Harassment(int id, int city_id, String title, String body, Double lat, Double lon, String time) {
        this.id = id;
        this.city_id = city_id;
        this.title = title;
        this.body = body;
        this.lat = lat;
        this.lon = lon;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}

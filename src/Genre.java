public class Genre {
    private String XMLid;
    private String id;
    private String name;

    public Genre() {
        name = "";
    }

    public Genre(String g) {
        name = g;
    }

    public void setXMLid(String xmlId) {
        this.XMLid = xmlId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXMLid(){
        return XMLid;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Genre Details - ");
        sb.append("name: " + getName());
        sb.append(".");

        return sb.toString();
    }
}
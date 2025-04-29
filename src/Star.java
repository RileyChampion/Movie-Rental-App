public class Star {
    private String XMLid;
    private String id;
    private String name;
    private int birthYear;

    public Star() {
        name = "";
        birthYear = -1;
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

    public void setBirthYear(int year) {
        this.birthYear = year;
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

    public int getBirthYear() {
        return birthYear;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Star Details - ");
        sb.append("id: " + getId());
        sb.append(", ");
        sb.append("Star Name: " + getName());
        sb.append(", ");
        sb.append("Birth Year:" + getBirthYear());
        sb.append(".");

        return sb.toString();
    }
}
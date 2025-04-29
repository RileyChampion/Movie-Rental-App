public class XMLParser {

    public static void main(String[] args) {
        StarParser spe = new StarParser();
        spe.runExample();
        spe.finishConnection();
        MovieParser spe2 = new MovieParser();
        spe2.runExample();
        spe2.runExample2();
        spe2.finishConnection();
    }
}

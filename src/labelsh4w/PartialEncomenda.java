package labelsh4w;

import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 *
 * @author tiago
 */
public class PartialEncomenda {

    private int ID;
    private String date;
    private String boxes;

    public PartialEncomenda() {
        this.ID = -1;
        this.date = "";
        this.boxes = "";
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBoxes() {
        return boxes;
    }

    public void setBoxes(String boxes) {
        this.boxes = boxes;
    }

    /**
     * This funcion returns a Map of the Boxes (keys) and the respectives sizes
     * and quantities (values)
     *
     * @param rest
     * @return HashMap<Integer, HashMap<String, Integer>>
     */
    public HashMap<Integer, HashMap<String, Integer>> getMapBoxes(HashMap<String, Integer> rest) {

        HashMap<Integer, HashMap<String, Integer>> mapBoxes = new HashMap<>();

        int IdBox = 1;
        var boxesArray = this.boxes.replace("\n", "").split("/");

        for (String boxe : boxesArray) {
            HashMap<String, Integer> mapSizes = new HashMap<>();
            var sizesQuantitiesArray = boxe.split(":");
            for (String entry : sizesQuantitiesArray) {
                if (entry.contains("-")) {
                    String[] par = entry.split("-");
                    String size = par[0];
                    if (rest.containsKey(size)) {
                        int qt = Integer.valueOf(par[1]);
                        if (qt <= rest.get(par[0])) {
                            try {
                                mapSizes.put(par[0], qt);
                                rest.put(par[1], rest.get(par[0]) + qt);
                            } catch (NumberFormatException e) {
                                System.out.println("Error readind: " + entry);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Não exitem pares suficientes para o tamanho " + size);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Não exite o tamanho " + size);
                    }
                }
            }
            mapBoxes.put(IdBox, mapSizes);
            IdBox++;
        }

        return mapBoxes;
    }

    public int getQuantityBySize(String size) {
        var sizesQuantitiesArray = this.boxes.replace("\n", "").replace(":", "/").split("/");
        int qt = 0;

        for (String entry : sizesQuantitiesArray) {
            if (entry.contains("-")) {
                String[] par = entry.split("-");
                try {
                    if (par[0].equals(size)) {
                        qt += Integer.valueOf(par[1]);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        return qt;
    }
}

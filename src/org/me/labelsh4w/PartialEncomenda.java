package org.me.labelsh4w;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import java.util.UUID;

/**
 *
 * @author tiago
 */
public class PartialEncomenda {

    private String ID;
    private String date;
    private String boxes;
    private boolean dataOk;

    public PartialEncomenda() {
        this.ID = UUID.randomUUID().toString().replaceAll("_", "");
        this.date = "";
        this.boxes = "";
        this.dataOk = true;

    }

    public PartialEncomenda(String boxes) {
        this.ID = UUID.randomUUID().toString().replaceAll("_", "");
        this.boxes = boxes;
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy [HH:mm:ss]");
        this.date = LocalDateTime.now().format(formatDate);
    }

    public String getDate() {
        return date;
    }

    public void updateDate() {
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd-MM-yyyy [HH:mm:ss]");
        this.date = LocalDateTime.now().format(formatDate);
        this.date = date;
    }

    public String getID() {
        return ID;
    }

    public String getBoxes() {
        return boxes;
    }

    public void setBoxes(String boxes) {
        this.boxes = boxes;
    }

    public boolean isDataOk(HashMap<String, Integer> rest) {
        this.getMapBoxes(rest);
        return dataOk;
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
        this.dataOk = true;
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
                                System.out.println("Error reading: " + entry);
                                this.dataOk = false;
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Não exitem pares suficientes para o tamanho " + size);
                            this.dataOk = false;
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Não existe o tamanho " + size);
                        this.dataOk = false;
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

    public TreeMap<String, TreeMap<Integer, Integer>> getMapSizes() {

        TreeMap<String, TreeMap<Integer, Integer>> mapSizes = new TreeMap<>();

        String[] caixas = this.getBoxes().replace("\n", "").split("/");
        int box = 1;
        String tam;
        int qt;
        for (String c : caixas) {
            if (c.contains(":")) {
                String[] par = c.split(":");
                for (String p : par) {
                    try {
                        String[] size = p.split("-");
                        tam = size[0];
                        qt = Integer.parseInt(size[1]);
                        mapSizes.putIfAbsent(tam, new TreeMap<>());
                        mapSizes.get(tam).put(box, qt);

                    } catch (NumberFormatException e) {
                    }
                }
            } else {
                try {
                    String[] size = c.split("-");
                    tam = size[0];
                    qt = Integer.parseInt(size[1]);
                    mapSizes.putIfAbsent(tam, new TreeMap<>());
                    mapSizes.get(tam).put(box, qt);

                } catch (NumberFormatException e) {
                }
            }
            box++;
        }

        return mapSizes;
    }
}

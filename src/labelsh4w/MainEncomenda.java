package labelsh4w;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;

/**
 *
 * @author tiago
 */
public class MainEncomenda {

    private String specs;
    private String name;
    private String commande;
    private String comments;
    private String image;
    private HashMap<Integer, PartialEncomenda> mapPartialEncomendas;
    private String sizesAndQuantities;
    private int totalQuantities;

    public MainEncomenda() {
        this.sizesAndQuantities = "";
        this.commande = "";
        this.specs = "";
        this.comments = "";
        this.image = "";
        this.name = "";
        this.mapPartialEncomendas = new HashMap<>();
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommande() {
        return commande;
    }

    public void setCommande(String commande) {
        this.commande = commande;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public HashMap<Integer, PartialEncomenda> getMapPartialEncomendas() {
        return mapPartialEncomendas;
    }

    public void setMapPartialEncomendas(HashMap<Integer, PartialEncomenda> mapPartialEncomendas) {
        this.mapPartialEncomendas = mapPartialEncomendas;
    }

    public String getSizesAndQuantities() {
        return sizesAndQuantities;
    }

    public void setSizesAndQuantities(String sizesAndQuantities) {
        this.sizesAndQuantities = sizesAndQuantities;
    }

    public int getTotalQuantities() {
        return totalQuantities;
    }

    public void setTotalQuantities(int totalQuantities) {
        this.totalQuantities = totalQuantities;
    }

    /**
     * This funcion returns a Map of the sizes (keys) and the respectives
     * quantities (values)
     *
     * @return HashMap<String,Integer>
     */
    public HashMap<String, Integer> getMapSizesQuantities() {
        HashMap<String, Integer> mapSizes = new HashMap<>();
        var sizesQuantitiesArray = this.sizesAndQuantities.replace("\n", "").split("/");
        this.setTotalQuantities(0);
        for (String entry : sizesQuantitiesArray) {
            if (entry.contains("-")) {
                String[] par = entry.split("-");
                // par[0] - size : par[1] - quantity
                try {
                    mapSizes.put(par[0], Integer.valueOf(par[1]));
                    this.setTotalQuantities(this.getTotalQuantities() + Integer.valueOf(par[1]));
                } catch (NumberFormatException e) {
                    System.out.println("Error readind: " + entry);
                }
            }
        }
        return mapSizes;
    }

    /**
     * Get total already of a size packet in previous actions Go to all partial
     * encomendas and get the values
     *
     * @param size
     * @return rest
     */
    public int getRestOfSize(String size) {
        int rest = this.getMapSizesQuantities().get(size);
        for (PartialEncomenda penc : this.mapPartialEncomendas.values()) {
            rest -= penc.getQuantityBySize(size);
        }
        return rest;
    }

    /**
     * Get total already of a size packet in previous actions Go to all partial
     * encomendas and get the values
     *
     * @param size
     * @return rest
     */
    public int getRest() {
        int rest = this.totalQuantities;
        for (String size : getMapSizesQuantities().keySet()) {
            rest -= this.getRestOfSize(size);
        }
        return rest;
    }

    /**
     * Get total already of a size packet in previous actions Go to all partial
     * encomendas and get the values
     *
     * @param size
     * @return rest
     */
    public HashMap<String, Integer> getMapOfRest() {
        HashMap<String, Integer> mapRest = new HashMap<>();
        for (String size : this.getMapSizesQuantities().keySet()) {
            mapRest.put(size, this.getRestOfSize(size));
        }       
        return  mapRest;
    }
}

package org.me.labelsh4w;

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.jar.Pack200;
import javax.crypto.interfaces.PBEKey;

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
    private HashMap<String, PartialEncomenda> mapPartialEncomendas;
    private String sizesAndQuantities;

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

    public HashMap<String, PartialEncomenda> getMapPartialEncomendas() {
        return mapPartialEncomendas;
    }

    public String getSizesAndQuantities() {
        return sizesAndQuantities;
    }

    public void setSizesAndQuantities(String sizesAndQuantities) {
        this.sizesAndQuantities = sizesAndQuantities;
    }

    public PartialEncomenda getPartialEncomenda(String id) {
        return this.mapPartialEncomendas.get(id);
    }

    /**
     * This funcion returns a Map of the sizes (keys) and the respectives
     * quantities (values)
     *
     * @return HashMap<String,Integer>
     */
    public TreeMap<String, Integer> getMapSizesQuantities() {
        TreeMap<String, Integer> mapSizes = new TreeMap<>();
        var sizesQuantitiesArray = this.sizesAndQuantities.replace("\n", "").split("/");
        for (String entry : sizesQuantitiesArray) {
            if (entry.contains("-")) {
                String[] par = entry.split("-");
                // par[0] - size : par[1] - quantity
                try {
                    mapSizes.put(par[0], Integer.valueOf(par[1]));
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
     * @param exceptID
     * @return rest
     */
    public int getRestOfSize(String size, String exceptID) {
        String exceptID_str = "";
        if (exceptID != null) {
            exceptID_str =exceptID;
        }
        int rest = this.getMapSizesQuantities().get(size);
        for (PartialEncomenda penc : this.mapPartialEncomendas.values()) {
           if(!exceptID_str.equals(penc.getID())){
                rest -= penc.getQuantityBySize(size);
           }
        }
        return rest;
    }

    /**
     * Get total already of a size packet in previous actions Go to all partial
     * encomendas and get the values
     *
     * @return rest
     */
    public int getRest() {
        int rest = getTotalQuantities();
        for (String size : getMapSizesQuantities().keySet()) {
            rest -= this.getRestOfSize(size, "");
        }
        return rest;
    }

    public int getTotalQuantities() {
        int qt = 0;
        var sizesQuantitiesArray = this.sizesAndQuantities.replace("\n", "").split("/");
        for (String entry : sizesQuantitiesArray) {
            if (entry.contains("-")) {
                String[] par = entry.split("-");
                try {
                    qt += Integer.valueOf(par[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Error readind: " + entry);
                }
            }
        }

        return qt;
    }

    /**
     * Get total already of a size packet in previous actions Go to all partial
     * encomendas and get the values
     *
     * @param exceptID
     * @return rest
     */
    public HashMap<String, Integer> getMapOfRest(String exceptID) {
        HashMap<String, Integer> mapRest = new HashMap<>();
        for (String size : this.getMapSizesQuantities().keySet()) {
            mapRest.put(size, this.getRestOfSize(size,exceptID));
            System.out.println(size+"-"+this.getRestOfSize(size,exceptID));
        }
        return mapRest;
        
    }

    public PartialEncomenda addPartialEncomenda(PartialEncomenda penc) {
        return this.mapPartialEncomendas.put(penc.getID(), penc);
    }
    
    public PartialEncomenda deletePartialEncomenda(String id) {
        return this.mapPartialEncomendas.remove(id);
    }
}

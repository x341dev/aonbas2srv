package dev.x341.aonbas2srv.dto;

import java.util.List;

public class MetroDto {

    // --- Metro Lines y Features ---
    private String type;
    private List<Feature> features;
    private long timestamp; // desde TmbStation
    private List<Linia> linies; // desde TmbStation

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<Feature> getFeatures() { return features; }
    public void setFeatures(List<Feature> features) { this.features = features; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<Linia> getLinies() { return linies; }
    public void setLinies(List<Linia> linies) { this.linies = linies; }

    // --- Feature ---
    public static class Feature {
        private String type;
        private String id;
        private Geometry geometry;
        private String geometry_name;
        private Properties properties;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Geometry getGeometry() { return geometry; }
        public void setGeometry(Geometry geometry) { this.geometry = geometry; }
        public String getGeometry_name() { return geometry_name; }
        public void setGeometry_name(String geometry_name) { this.geometry_name = geometry_name; }
        public Properties getProperties() { return properties; }
        public void setProperties(Properties properties) { this.properties = properties; }
    }

    // --- Geometry ---
    public static class Geometry {
        private String type;
        private Object coordinates; // puede ser List anidada o número

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Object getCoordinates() { return coordinates; }
        public void setCoordinates(Object coordinates) { this.coordinates = coordinates; }
    }


    // --- Properties ---
    public static class Properties {
        // MetroLinesDto
        private int ID_LINIA; private int CODI_LINIA; private String NOM_LINIA; private String DESC_LINIA;
        private String ORIGEN_LINIA; private String DESTI_LINIA; private int NUM_PAQUETS;
        private int ID_OPERADOR; private String NOM_OPERADOR; private String NOM_TIPUS_TRANSPORT;
        private int CODI_FAMILIA; private String NOM_FAMILIA; private int ORDRE_FAMILIA; private int ORDRE_LINIA;
        private String CODI_TIPUS_CALENDARI; private String NOM_TIPUS_CALENDARI; private String DATA;
        private String COLOR_LINIA; private String COLOR_AUX_LINIA; private String COLOR_TEXT_LINIA;
        // MetroLineDto
        private int ID_ESTACIO_LINIA; private int CODI_ESTACIO_LINIA; private int ID_ESTACIO; private String NOM_ESTACIO;
        private int ORDRE_ESTACIO; private String DESC_SERVEI; private String ORIGEN_SERVEI; private String DESTI_SERVEI;
        private String NOM_TIPUS_ACCESSIBILITAT; private String NOM_TIPUS_ESTAT; private String DATA_INAUGURACIO;
        private String PICTO;

        public int getID_LINIA() { return ID_LINIA; } public void setID_LINIA(int ID_LINIA) { this.ID_LINIA = ID_LINIA; }
        public int getCODI_LINIA() { return CODI_LINIA; } public void setCODI_LINIA(int CODI_LINIA) { this.CODI_LINIA = CODI_LINIA; }
        public String getNOM_LINIA() { return NOM_LINIA; } public void setNOM_LINIA(String NOM_LINIA) { this.NOM_LINIA = NOM_LINIA; }
        public String getDESC_LINIA() { return DESC_LINIA; } public void setDESC_LINIA(String DESC_LINIA) { this.DESC_LINIA = DESC_LINIA; }
        public String getORIGEN_LINIA() { return ORIGEN_LINIA; } public void setORIGEN_LINIA(String ORIGEN_LINIA) { this.ORIGEN_LINIA = ORIGEN_LINIA; }
        public String getDESTI_LINIA() { return DESTI_LINIA; } public void setDESTI_LINIA(String DESTI_LINIA) { this.DESTI_LINIA = DESTI_LINIA; }
        public int getNUM_PAQUETS() { return NUM_PAQUETS; } public void setNUM_PAQUETS(int NUM_PAQUETS) { this.NUM_PAQUETS = NUM_PAQUETS; }
        public int getID_OPERADOR() { return ID_OPERADOR; } public void setID_OPERADOR(int ID_OPERADOR) { this.ID_OPERADOR = ID_OPERADOR; }
        public String getNOM_OPERADOR() { return NOM_OPERADOR; } public void setNOM_OPERADOR(String NOM_OPERADOR) { this.NOM_OPERADOR = NOM_OPERADOR; }
        public String getNOM_TIPUS_TRANSPORT() { return NOM_TIPUS_TRANSPORT; } public void setNOM_TIPUS_TRANSPORT(String NOM_TIPUS_TRANSPORT) { this.NOM_TIPUS_TRANSPORT = NOM_TIPUS_TRANSPORT; }
        public int getCODI_FAMILIA() { return CODI_FAMILIA; } public void setCODI_FAMILIA(int CODI_FAMILIA) { this.CODI_FAMILIA = CODI_FAMILIA; }
        public String getNOM_FAMILIA() { return NOM_FAMILIA; } public void setNOM_FAMILIA(String NOM_FAMILIA) { this.NOM_FAMILIA = NOM_FAMILIA; }
        public int getORDRE_FAMILIA() { return ORDRE_FAMILIA; } public void setORDRE_FAMILIA(int ORDRE_FAMILIA) { this.ORDRE_FAMILIA = ORDRE_FAMILIA; }
        public int getORDRE_LINIA() { return ORDRE_LINIA; } public void setORDRE_LINIA(int ORDRE_LINIA) { this.ORDRE_LINIA = ORDRE_LINIA; }
        public String getCODI_TIPUS_CALENDARI() { return CODI_TIPUS_CALENDARI; } public void setCODI_TIPUS_CALENDARI(String CODI_TIPUS_CALENDARI) { this.CODI_TIPUS_CALENDARI = CODI_TIPUS_CALENDARI; }
        public String getNOM_TIPUS_CALENDARI() { return NOM_TIPUS_CALENDARI; } public void setNOM_TIPUS_CALENDARI(String NOM_TIPUS_CALENDARI) { this.NOM_TIPUS_CALENDARI = NOM_TIPUS_CALENDARI; }
        public String getDATA() { return DATA; } public void setDATA(String DATA) { this.DATA = DATA; }
        public String getCOLOR_LINIA() { return COLOR_LINIA; } public void setCOLOR_LINIA(String COLOR_LINIA) { this.COLOR_LINIA = COLOR_LINIA; }
        public String getCOLOR_AUX_LINIA() { return COLOR_AUX_LINIA; } public void setCOLOR_AUX_LINIA(String COLOR_AUX_LINIA) { this.COLOR_AUX_LINIA = COLOR_AUX_LINIA; }
        public String getCOLOR_TEXT_LINIA() { return COLOR_TEXT_LINIA; } public void setCOLOR_TEXT_LINIA(String COLOR_TEXT_LINIA) { this.COLOR_TEXT_LINIA = COLOR_TEXT_LINIA; }
        public int getID_ESTACIO_LINIA() { return ID_ESTACIO_LINIA; } public void setID_ESTACIO_LINIA(int ID_ESTACIO_LINIA) { this.ID_ESTACIO_LINIA = ID_ESTACIO_LINIA; }
        public int getCODI_ESTACIO_LINIA() { return CODI_ESTACIO_LINIA; } public void setCODI_ESTACIO_LINIA(int CODI_ESTACIO_LINIA) { this.CODI_ESTACIO_LINIA = CODI_ESTACIO_LINIA; }
        public int getID_ESTACIO() { return ID_ESTACIO; } public void setID_ESTACIO(int ID_ESTACIO) { this.ID_ESTACIO = ID_ESTACIO; }
        public String getNOM_ESTACIO() { return NOM_ESTACIO; } public void setNOM_ESTACIO(String NOM_ESTACIO) { this.NOM_ESTACIO = NOM_ESTACIO; }
        public int getORDRE_ESTACIO() { return ORDRE_ESTACIO; } public void setORDRE_ESTACIO(int ORDRE_ESTACIO) { this.ORDRE_ESTACIO = ORDRE_ESTACIO; }
        public String getDESC_SERVEI() { return DESC_SERVEI; } public void setDESC_SERVEI(String DESC_SERVEI) { this.DESC_SERVEI = DESC_SERVEI; }
        public String getORIGEN_SERVEI() { return ORIGEN_SERVEI; } public void setORIGEN_SERVEI(String ORIGEN_SERVEI) { this.ORIGEN_SERVEI = ORIGEN_SERVEI; }
        public String getDESTI_SERVEI() { return DESTI_SERVEI; } public void setDESTI_SERVEI(String DESTI_SERVEI) { this.DESTI_SERVEI = DESTI_SERVEI; }
        public String getNOM_TIPUS_ACCESSIBILITAT() { return NOM_TIPUS_ACCESSIBILITAT; } public void setNOM_TIPUS_ACCESSIBILITAT(String NOM_TIPUS_ACCESSIBILITAT) { this.NOM_TIPUS_ACCESSIBILITAT = NOM_TIPUS_ACCESSIBILITAT; }
        public String getNOM_TIPUS_ESTAT() { return NOM_TIPUS_ESTAT; } public void setNOM_TIPUS_ESTAT(String NOM_TIPUS_ESTAT) { this.NOM_TIPUS_ESTAT = NOM_TIPUS_ESTAT; }
        public String getDATA_INAUGURACIO() { return DATA_INAUGURACIO; } public void setDATA_INAUGURACIO(String DATA_INAUGURACIO) { this.DATA_INAUGURACIO = DATA_INAUGURACIO; }
        public String getPICTO() { return PICTO; } public void setPICTO(String PICTO) { this.PICTO = PICTO; }
    }

    // --- TmbStation Linia ---
    public static class Linia {
        private int codi_linia; private String nom_linia; private String nom_familia; private int codi_familia; private String color_linia;
        private List<Estacio> estacions;

        public int getCodi_linia() { return codi_linia; } public void setCodi_linia(int codi_linia) { this.codi_linia = codi_linia; }
        public String getNom_linia() { return nom_linia; } public void setNom_linia(String nom_linia) { this.nom_linia = nom_linia; }
        public String getNom_familia() { return nom_familia; } public void setNom_familia(String nom_familia) { this.nom_familia = nom_familia; }
        public int getCodi_familia() { return codi_familia; } public void setCodi_familia(int codi_familia) { this.codi_familia = codi_familia; }
        public String getColor_linia() { return color_linia; } public void setColor_linia(String color_linia) { this.color_linia = color_linia; }
        public List<Estacio> getEstacions() { return estacions; } public void setEstacions(List<Estacio> estacions) { this.estacions = estacions; }
    }

    public static class Estacio {
        private int codi_via; private int id_sentit; private int codi_estacio; private List<LiniaTrajecte> linies_trajectes;

        public int getCodi_via() { return codi_via; } public void setCodi_via(int codi_via) { this.codi_via = codi_via; }
        public int getId_sentit() { return id_sentit; } public void setId_sentit(int id_sentit) { this.id_sentit = id_sentit; }
        public int getCodi_estacio() { return codi_estacio; } public void setCodi_estacio(int codi_estacio) { this.codi_estacio = codi_estacio; }
        public List<LiniaTrajecte> getLinies_trajectes() { return linies_trajectes; } public void setLinies_trajectes(List<LiniaTrajecte> linies_trajectes) { this.linies_trajectes = linies_trajectes; }
    }

    public static class LiniaTrajecte {
        private int codi_linia; private String nom_linia; private String color_linia; private String codi_trajecte; private String desti_trajecte; private List<ProximTren> propers_trens;

        public int getCodi_linia() { return codi_linia; } public void setCodi_linia(int codi_linia) { this.codi_linia = codi_linia; }
        public String getNom_linia() { return nom_linia; } public void setNom_linia(String nom_linia) { this.nom_linia = nom_linia; }
        public String getColor_linia() { return color_linia; } public void setColor_linia(String color_linia) { this.color_linia = color_linia; }
        public String getCodi_trajecte() { return codi_trajecte; } public void setCodi_trajecte(String codi_trajecte) { this.codi_trajecte = codi_trajecte; }
        public String getDesti_trajecte() { return desti_trajecte; } public void setDesti_trajecte(String desti_trajecte) { this.desti_trajecte = desti_trajecte; }
        public List<ProximTren> getPropers_trens() { return propers_trens; } public void setPropers_trens(List<ProximTren> propers_trens) { this.propers_trens = propers_trens; }
    }

    public static class ProximTren {
        private String codi_servei; private long temps_arribada;

        public String getCodi_servei() { return codi_servei; } public void setCodi_servei(String codi_servei) { this.codi_servei = codi_servei; }
        public long getTemps_arribada() { return temps_arribada; } public void setTemps_arribada(long temps_arribada) { this.temps_arribada = temps_arribada; }
    }
}

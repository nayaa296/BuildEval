package model;

public class Material {
    private String idMaterial;
    private String namaMaterial;
    private String satuan;
    private double hargaSatuan;

    public Material() {
    }

    public Material(String idMaterial, String namaMaterial, String satuan, double hargaSatuan) {
        this.idMaterial = idMaterial;
        this.namaMaterial = namaMaterial;
        this.satuan = satuan;
        setHargaSatuan(hargaSatuan);
    }

    public String getIdMaterial() {
        return idMaterial;
    }

    public void setIdMaterial(String idMaterial) {
        this.idMaterial = idMaterial;
    }

    public String getNamaMaterial() {
        return namaMaterial;
    }

    public void setNamaMaterial(String namaMaterial) {
        this.namaMaterial = namaMaterial;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public double getHargaSatuan() {
        return hargaSatuan;
    }

    public void setHargaSatuan(double hargaSatuan) {
        if (hargaSatuan <= 0) {
            throw new IllegalArgumentException("Harga satuan harus lebih besar dari 0!");
        }
        this.hargaSatuan = hargaSatuan;
    }
}


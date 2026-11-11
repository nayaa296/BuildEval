package model;

public class DetailItem {
    private int idDetail;
    private String idRAB;
    private Material material;
    private double volume;
    private double subtotal;

    public DetailItem() {
    }

    public DetailItem(Material material, double volume) {
        this.material = material;
        setVolume(volume);
    }

    public DetailItem(int idDetail, String idRAB, Material material, double volume) {
        this.idDetail = idDetail;
        this.idRAB = idRAB;
        this.material = material;
        setVolume(volume);
    }

    public int getIdDetail() {
        return idDetail;
    }

    public void setIdDetail(int idDetail) {
        this.idDetail = idDetail;
    }

    public String getIdRAB() {
        return idRAB;
    }

    public void setIdRAB(String idRAB) {
        this.idRAB = idRAB;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
        hitungSubtotal();
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("Volume harus lebih besar dari 0!");
        }
        this.volume = volume;
        hitungSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double hitungSubtotal() {
        if (this.material != null) {
            this.subtotal = this.volume * this.material.getHargaSatuan();
        } else {
            this.subtotal = 0;
        }
        return this.subtotal;
    }
}


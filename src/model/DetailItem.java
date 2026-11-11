package model;

public class DetailItem {
    private int idDetail;
    private String idRAB;
    private Material material;
    private int jumlah;
    private double volume; // Dijaga untuk kompatibilitas
    private double subtotal;

    public DetailItem() {
    }

    public DetailItem(Material material, int jumlah) {
        setMaterial(material);
        setJumlah(jumlah);
    }

    public DetailItem(Material material, double volume) {
        setMaterial(material);
        setVolume(volume);
    }

    public DetailItem(int idDetail, String idRAB, Material material, int jumlah) {
        this.idDetail = idDetail;
        this.idRAB = idRAB;
        setMaterial(material);
        setJumlah(jumlah);
    }

    public DetailItem(int idDetail, String idRAB, Material material, double volume) {
        this.idDetail = idDetail;
        this.idRAB = idRAB;
        setMaterial(material);
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
        if (material == null) {
            throw new IllegalArgumentException("Material tidak boleh null.");
        }
        this.material = material;
        hitungSubtotal();
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah item pembelian harus lebih dari 0.");
        }
        this.jumlah = jumlah;
        this.volume = jumlah;
        hitungSubtotal();
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        if (volume <= 0) {
            throw new IllegalArgumentException("Jumlah item pembelian harus lebih dari 0.");
        }
        this.volume = volume;
        this.jumlah = (int) Math.round(volume);
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
            double qty = this.jumlah > 0 ? this.jumlah : this.volume;
            this.subtotal = qty * this.material.getHargaSatuan();
        } else {
            this.subtotal = 0;
        }
        return this.subtotal;
    }
}

package model;

import exception.BudgetOverlimitException;
import java.util.ArrayList;
import java.util.List;

public class RencanaAnggaran {
    private String idRAB;
    private String namaProyek;
    private double batasBudget; // Alias batasPagu
    private Manajer manajer;
    private List<DetailItem> listMaterial; // Alias listDetail

    public RencanaAnggaran() {
        this.listMaterial = new ArrayList<>();
    }

    public RencanaAnggaran(String idRAB, String namaProyek, double batasBudget, Manajer manajer) {
        if (batasBudget <= 0) {
            throw new IllegalArgumentException("Batas budget harus lebih besar dari 0.");
        }
        this.idRAB = idRAB;
        this.namaProyek = namaProyek;
        this.batasBudget = batasBudget;
        this.manajer = manajer;
        this.listMaterial = new ArrayList<>();
    }

    public String getIdRAB() {
        return idRAB;
    }

    public void setIdRAB(String idRAB) {
        this.idRAB = idRAB;
    }

    public String getNamaProyek() {
        return namaProyek;
    }

    public void setNamaProyek(String namaProyek) {
        this.namaProyek = namaProyek;
    }

    public double getBatasBudget() {
        return batasBudget;
    }

    public void setBatasBudget(double batasBudget) {
        if (batasBudget <= 0) {
            throw new IllegalArgumentException("Batas budget harus lebih besar dari 0.");
        }
        this.batasBudget = batasBudget;
    }

    // Kompatibilitas dengan kode sebelumnya
    public double getBatasPagu() {
        return batasBudget;
    }

    public void setBatasPagu(double batasPagu) {
        setBatasBudget(batasPagu);
    }

    public Manajer getManajer() {
        return manajer;
    }

    public void setManajer(Manajer manajer) {
        this.manajer = manajer;
    }

    public List<DetailItem> getListMaterial() {
        return listMaterial;
    }

    public void setListMaterial(List<DetailItem> listMaterial) {
        this.listMaterial = listMaterial;
    }

    // Kompatibilitas dengan getListDetail
    public List<DetailItem> getListDetail() {
        return listMaterial;
    }

    public void setListDetail(List<DetailItem> listDetail) {
        this.listMaterial = listDetail;
    }

    public void tambahItem(DetailItem item) throws BudgetOverlimitException {
        if (item == null) {
            throw new IllegalArgumentException("Detail item tidak boleh null.");
        }

        // Pengecekan SEBELUM mengubah state listMaterial
        double totalSekarang = hitungTotalBiaya();
        double subtotalItemBaru = item.hitungSubtotal();
        double estimasiTotal = totalSekarang + subtotalItemBaru;

        if (estimasiTotal > this.batasBudget) {
            double selisih = estimasiTotal - this.batasBudget;
            throw new BudgetOverlimitException(String.format(
                "Penambahan item '%s' ditolak! Estimasi total biaya (Rp %,.2f) melebihi batas budget (Rp %,.2f) sebesar Rp %,.2f.",
                item.getMaterial() != null ? item.getMaterial().getNamaMaterial() : "Unknown",
                estimasiTotal,
                this.batasBudget,
                selisih
            ));
        }

        // listMaterial.add(item) HANYA dieksekusi jika seluruh pemeriksaan berhasil
        item.setIdRAB(this.idRAB);
        this.listMaterial.add(item);
    }

    // Kompatibilitas tambahDetailItem
    public void tambahDetailItem(DetailItem item) {
        try {
            tambahItem(item);
        } catch (BudgetOverlimitException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public double hitungTotalBiaya() {
        double total = 0.0;
        for (DetailItem item : listMaterial) {
            total += item.hitungSubtotal();
        }
        return total;
    }

    public String evaluasiKelayakan() {
        double total = hitungTotalBiaya();
        if (total <= batasBudget) {
            return "LAYAK (Dalam Batas Pagu Anggaran)";
        } else {
            return "TIDAK LAYAK (Over Budget / Melebihi Batas Pagu)";
        }
    }

    public void cetakLaporanRAB() {
        System.out.println("==========================================================================================");
        System.out.println("                                  LAPORAN RENCANA ANGGARAN BIAYA (RAB)                    ");
        System.out.println("==========================================================================================");
        System.out.println("ID RAB       : " + idRAB);
        System.out.println("Nama Proyek  : " + namaProyek);
        if (manajer != null) {
            System.out.println("Manajer      : " + manajer.getNamaManajer() + " (ID: " + manajer.getIdManajer() + ")");
        }
        System.out.printf("Batas Budget : Rp %,.2f%n", batasBudget);
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-25s | %-12s | %-15s | %-18s%n", "No", "Nama Material", "Jumlah", "Harga Satuan", "Subtotal");
        System.out.println("------------------------------------------------------------------------------------------");

        int no = 1;
        for (DetailItem item : listMaterial) {
            Material m = item.getMaterial();
            String namaMat = (m != null) ? m.getNamaMaterial() : "-";
            int qty = item.getJumlah() > 0 ? item.getJumlah() : (int) Math.round(item.getVolume());
            String jmlSat = qty + " " + ((m != null) ? m.getSatuan() : "");
            double hrg = (m != null) ? m.getHargaSatuan() : 0.0;
            double sub = item.getSubtotal();

            System.out.printf("%-5d | %-25s | %-12s | Rp %,12.2f | Rp %,15.2f%n", 
                no++, namaMat, jmlSat, hrg, sub);
        }

        double totalBiaya = hitungTotalBiaya();
        double selisih = batasBudget - totalBiaya;

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("Total Biaya RAB      : Rp %,.2f%n", totalBiaya);
        System.out.printf("Sisa / Selisih Pagu  : Rp %,.2f%n", selisih);
        System.out.println("Status Evaluasi      : " + evaluasiKelayakan());
        System.out.println("==========================================================================================");
    }
}

package model;

import exception.BudgetOverlimitException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RencanaAnggaran {
    private String idRAB;
    private String namaProyek;
    private double batasBudget; // Alias batasPagu
    private Manajer manajer;
    private List<DetailItem> listMaterial = new ArrayList<>();

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

    /**
     * Mengembalikan listMaterial yang bersifat Unmodifiable
     * agar collection internal aman dari modifikasi langsung di luar kelas.
     */
    public List<DetailItem> getListMaterial() {
        return Collections.unmodifiableList(listMaterial);
    }

    public void setListMaterial(List<DetailItem> listMaterial) {
        this.listMaterial = (listMaterial != null) ? new ArrayList<>(listMaterial) : new ArrayList<>();
    }

    // Kompatibilitas dengan getListDetail
    public List<DetailItem> getListDetail() {
        return getListMaterial();
    }

    public void setListDetail(List<DetailItem> listDetail) {
        setListMaterial(listDetail);
    }

    /**
     * Menambahkan item baru ke dalam RAB dengan validasi batas budget.
     */
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

    /**
     * Mencari item dalam listMaterial berdasarkan kode material (case-insensitive).
     * @param kodeItem kode material yang dicari
     * @return objek DetailItem jika ditemukan, atau null jika tidak ada
     */
    public DetailItem cariItem(String kodeItem) {
        if (kodeItem == null || kodeItem.trim().isEmpty()) {
            return null;
        }

        for (DetailItem item : listMaterial) {
            if (item.getMaterial() != null && item.getMaterial().getIdMaterial() != null) {
                if (item.getMaterial().getIdMaterial().equalsIgnoreCase(kodeItem.trim())) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Menghapus material dari listMaterial berdasarkan kode material (case-insensitive).
     * @param kodeItem kode material yang akan dihapus
     * @return true jika berhasil dihapus, false jika item tidak ditemukan
     */
    public boolean hapusItem(String kodeItem) {
        if (kodeItem == null || kodeItem.trim().isEmpty()) {
            return false;
        }

        return this.listMaterial.removeIf(item -> 
            item.getMaterial() != null && 
            item.getMaterial().getIdMaterial() != null && 
            item.getMaterial().getIdMaterial().equalsIgnoreCase(kodeItem.trim())
        );
    }

    /**
     * Menambahkan item langsung ke internal list (misal saat rekonstruksi dari DB).
     */
    public void tambahItemInternal(DetailItem item) {
        if (item != null) {
            item.setIdRAB(this.idRAB);
            this.listMaterial.add(item);
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
        System.out.printf("%-5s | %-12s | %-25s | %-12s | %-15s | %-18s%n", "No", "Kode Item", "Nama Material", "Jumlah", "Harga Satuan", "Subtotal");
        System.out.println("------------------------------------------------------------------------------------------");

        int no = 1;
        for (DetailItem item : listMaterial) {
            Material m = item.getMaterial();
            String kode = (m != null && m.getIdMaterial() != null) ? m.getIdMaterial() : "-";
            String namaMat = (m != null) ? m.getNamaMaterial() : "-";
            int qty = item.getJumlah() > 0 ? item.getJumlah() : (int) Math.round(item.getVolume());
            String jmlSat = qty + " " + ((m != null) ? m.getSatuan() : "");
            double hrg = (m != null) ? m.getHargaSatuan() : 0.0;
            double sub = item.getSubtotal();

            System.out.printf("%-5d | %-12s | %-25s | %-12s | Rp %,12.2f | Rp %,15.2f%n", 
                no++, kode, namaMat, jmlSat, hrg, sub);
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

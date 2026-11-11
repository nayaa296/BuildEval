package model;

import java.util.ArrayList;
import java.util.List;

public class RencanaAnggaran {
    private String idRAB;
    private String namaProyek;
    private double batasPagu;
    private Manajer manajer;
    private List<DetailItem> listDetail;

    public RencanaAnggaran() {
        this.listDetail = new ArrayList<>();
    }

    public RencanaAnggaran(String idRAB, String namaProyek, double batasPagu, Manajer manajer) {
        this.idRAB = idRAB;
        this.namaProyek = namaProyek;
        setBatasPagu(batasPagu);
        this.manajer = manajer;
        this.listDetail = new ArrayList<>();
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

    public double getBatasPagu() {
        return batasPagu;
    }

    public void setBatasPagu(double batasPagu) {
        if (batasPagu <= 0) {
            throw new IllegalArgumentException("Batas pagu harus lebih besar dari 0!");
        }
        this.batasPagu = batasPagu;
    }

    public Manajer getManajer() {
        return manajer;
    }

    public void setManajer(Manajer manajer) {
        this.manajer = manajer;
    }

    public List<DetailItem> getListDetail() {
        return listDetail;
    }

    public void setListDetail(List<DetailItem> listDetail) {
        this.listDetail = listDetail;
    }

    public void tambahDetailItem(DetailItem item) {
        if (item != null) {
            item.setIdRAB(this.idRAB);
            this.listDetail.add(item);
        }
    }

    public double hitungTotalBiaya() {
        double total = 0.0;
        for (DetailItem item : listDetail) {
            total += item.hitungSubtotal();
        }
        return total;
    }

    public String evaluasiKelayakan() {
        double total = hitungTotalBiaya();
        if (total <= batasPagu) {
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
            System.out.println("Manajer      : " + manajer.getNamaManajer() + " (ID: " + manajer.getIdManajer() + ", Kontak: " + manajer.getKontak() + ")");
        }
        System.out.printf("Batas Pagu   : Rp %,.2f%n", batasPagu);
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-20s | %-10s | %-15s | %-18s%n", "No", "Nama Material", "Volume", "Harga Satuan", "Subtotal");
        System.out.println("------------------------------------------------------------------------------------------");

        int no = 1;
        for (DetailItem item : listDetail) {
            Material m = item.getMaterial();
            String namaMat = (m != null) ? m.getNamaMaterial() : "-";
            String volSat = item.getVolume() + " " + ((m != null) ? m.getSatuan() : "");
            double hrg = (m != null) ? m.getHargaSatuan() : 0.0;
            double sub = item.getSubtotal();

            System.out.printf("%-5d | %-20s | %-10s | Rp %,12.2f | Rp %,15.2f%n", 
                no++, namaMat, volSat, hrg, sub);
        }

        double totalBiaya = hitungTotalBiaya();
        double selisih = batasPagu - totalBiaya;

        System.out.println("------------------------------------------------------------------------------------------");
        System.out.printf("Total Biaya RAB      : Rp %,.2f%n", totalBiaya);
        System.out.printf("Sisa / Selisih Pagu  : Rp %,.2f%n", selisih);
        System.out.println("Status Evaluasi      : " + evaluasiKelayakan());
        System.out.println("==========================================================================================");
    }
}


package model;

public class Manajer {
    private String idManajer;
    private String namaManajer;
    private String kontak;

    public Manajer() {
    }

    public Manajer(String idManajer, String namaManajer, String kontak) {
        this.idManajer = idManajer;
        this.namaManajer = namaManajer;
        this.kontak = kontak;
    }

    public String getIdManajer() {
        return idManajer;
    }

    public void setIdManajer(String idManajer) {
        this.idManajer = idManajer;
    }

    public String getNamaManajer() {
        return namaManajer;
    }

    public void setNamaManajer(String namaManajer) {
        this.namaManajer = namaManajer;
    }

    public String getKontak() {
        return kontak;
    }

    public void setKontak(String kontak) {
        this.kontak = kontak;
    }
}


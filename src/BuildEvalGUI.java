import dao.BuildEvalFileRepository;
import dao.RencanaAnggaranDAO;
import exception.BudgetOverlimitException;
import model.DetailItem;
import model.Manajer;
import model.Material;
import model.RencanaAnggaran;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BuildEvalGUI extends JFrame {

    // Model Pengguna Terdaftar
    public enum Role {
        MANAGER,
        ESTIMATOR
    }

    public static class UserAccount {
        private final String username;
        private final String password;
        private final String fullName;
        private final String jobTitle;
        private final Role role;

        public UserAccount(String username, String password, String fullName, String jobTitle, Role role) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.jobTitle = jobTitle;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getFullName() { return fullName; }
        public String getJobTitle() { return jobTitle; }
        public Role getRole() { return role; }
    }

    private static final Map<String, UserAccount> DAFTAR_USER = new HashMap<>();
    static {
        DAFTAR_USER.put("dafa", new UserAccount("dafa", "123", "Dafa Putra Riyanto", "Manajer Proyek", Role.MANAGER));
        DAFTAR_USER.put("naya", new UserAccount("naya", "123", "Hafizhah Inayah Sya'bania", "Quantity Surveyor / Estimator", Role.ESTIMATOR));
    }

    // Palet Warna Modern UI
    private static final Color BG_DARK = new Color(0x0F, 0x17, 0x2A);          // #0F172A Dark Slate
    private static final Color HEADER_BG = new Color(0x1E, 0x29, 0x3B);         // #1E293B Deep Slate Blue
    private static final Color BG_PAGE = new Color(0xF8, 0xFA, 0xFC);           // #F8FAFC
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(0xCB, 0xD5, 0xE1);       // #CBD5E1 Slate 300
    private static final Color TEXT_MAIN = new Color(0x0F, 0x17, 0x2A);
    private static final Color TEXT_MUTED = new Color(0x47, 0x55, 0x69);        // #475569
    private static final Color ACCENT_BLUE = new Color(0x25, 0x63, 0xEB);       // #2563EB Primary Blue
    private static final Color ACCENT_BLUE_HOVER = new Color(0x1D, 0x4E, 0xD8); // #1D4ED8
    private static final Color COLOR_LAYAK = new Color(0x05, 0x96, 0x69);       // #059669 Emerald 600
    private static final Color COLOR_OVER = new Color(0xDC, 0x26, 0x26);        // #DC2626 Crimson 600
    private static final Color COLOR_OVER_LIGHT = new Color(0xFE, 0xF2, 0xF2);

    private final RencanaAnggaran rab;
    private final RencanaAnggaranDAO dao;
    private final Path defaultFilePath = Paths.get("buildeval_backup.txt");
    private final DecimalFormat formatRupiah;

    // State Pengguna Aktif
    private UserAccount currentUser = null;

    // Komponen Header
    private JLabel lblUserLoginInfo;

    // Komponen Tabbed Pane
    private JTabbedPane tabbedPane;

    // Metric Cards di Beranda
    private JLabel valCardPagu;
    private JLabel valCardPengeluaran;
    private JLabel valCardSisa;
    private JLabel valCardStatus;
    private JLabel valTotalItemBeranda;
    private JPanel cardStatusPanel;

    // Form Input di Tab Kelola Material
    private JTextField txtKode;
    private JTextField txtNama;
    private JTextField txtSatuan;
    private JTextField txtHarga;
    private JTextField txtJumlah;
    private JButton btnTambahMaterial;

    // Tabel Material
    private JTable tabelMaterial;
    private DefaultTableModel modelTabel;
    private JButton btnHapusMaterial;

    // Tombol di Tab Backup & DB
    private JButton btnSimpanDB;
    private JButton btnSimpanFile;
    private JButton btnMuatFile;

    // Label Status Ringkas di Bawah Tabel
    private JLabel lblRingkasanTabel;

    public BuildEvalGUI() {
        // Konfigurasi Currency Formatter (IDR)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        formatRupiah = new DecimalFormat("Rp #,##0.00", symbols);

        // Inisialisasi Domain Model & DAO
        Manajer manajer = new Manajer("M001", "Dafa Putra Riyanto", "081234567890");
        rab = new RencanaAnggaran("RAB-2026-F15", "Pembangunan Fasilitas Publik", 50000000.0, manajer);
        dao = new RencanaAnggaranDAO();

        // Konfigurasi Jendela Utama
        setTitle("BuildEval - Sistem Evaluasi Kelayakan Proyek");
        setSize(1040, 740);
        setMinimumSize(new Dimension(940, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_PAGE);
        setLayout(new BorderLayout());

        // Header Modern
        initHeaderPanel();

        // Main Tabbed Navigation
        initTabbedPane();

        // Muat data default jika berkas backup sudah ada
        if (defaultFilePath.toFile().exists()) {
            try {
                BuildEvalFileRepository.muatDariFile(defaultFilePath, rab);
            } catch (Exception ignored) {
            }
        }

        // Tampilkan dialog login
        tampilkanDialogLogin();
    }

    // -------------------------------------------------------------------------
    // 1. SISTEM LOGIN MULTI-ROLE (JDialog)
    // -------------------------------------------------------------------------
    private void tampilkanDialogLogin() {
        JDialog dialogLogin = new JDialog(this, "Login - BuildEval Enterprise", true);
        dialogLogin.setSize(440, 400);
        dialogLogin.setResizable(false);
        dialogLogin.setLocationRelativeTo(this);
        dialogLogin.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(BG_PAGE);

        // Header Login Dialog
        JPanel header = new JPanel(new GridLayout(2, 1, 2, 2));
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel lblTitle = new JLabel("BuildEval Authentication");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Silakan masuk menggunakan akun terdaftar");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(0x94, 0xA3, 0xB8));

        header.add(lblTitle);
        header.add(lblSub);
        contentPane.add(header, BorderLayout.NORTH);

        // Form Login Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(16, 28, 16, 28));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        JTextField txtUser = buatStylingTextField();
        txtUser.setPreferredSize(new Dimension(200, 32));

        JPasswordField txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPass.setForeground(Color.BLACK);
        txtPass.setCaretColor(Color.BLACK);
        txtPass.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x94, 0xA3, 0xB8), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        txtPass.setPreferredSize(new Dimension(200, 32));

        JLabel lblHint = new JLabel("<html><font color='#475569' size='2'>Akun Demo:<br>• <b>dafa</b> / <b>123</b> (Manajer Proyek - Akses Penuh)<br>• <b>naya</b> / <b>123</b> (Quantity Surveyor - Akses Terbatas)</font></html>");

        JLabel lblUserText = new JLabel("Username:");
        lblUserText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserText.setForeground(TEXT_MAIN);

        JLabel lblPassText = new JLabel("Password:");
        lblPassText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPassText.setForeground(TEXT_MAIN);

        g.gridx = 0; g.gridy = 0; g.weightx = 0.3;
        body.add(lblUserText, g);
        g.gridx = 1; g.gridy = 0; g.weightx = 0.7;
        body.add(txtUser, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0.3;
        body.add(lblPassText, g);
        g.gridx = 1; g.gridy = 1; g.weightx = 0.7;
        body.add(txtPass, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        body.add(lblHint, g);

        contentPane.add(body, BorderLayout.CENTER);

        // Button Login & Keluar
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btnPanel.setOpaque(false);

        // Tombol Batal (Sekunder: Background Abu-abu terang, Teks Hitam tegas)
        JButton btnBatal = buatModernButton("Batal & Keluar", new Color(0xE2, 0xE8, 0xF0), new Color(0xCB, 0xD5, 0xE1), new Color(0x1E, 0x29, 0x3B));
        btnBatal.addActionListener(e -> {
            dialogLogin.dispose();
            System.exit(0);
        });

        // Tombol Masuk (Primer: Background Biru, Teks Putih)
        JButton btnMasuk = buatModernButton("Masuk Sistem", ACCENT_BLUE, ACCENT_BLUE_HOVER, Color.WHITE);
        btnMasuk.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword());

            if (DAFTAR_USER.containsKey(u) && DAFTAR_USER.get(u).getPassword().equals(p)) {
                currentUser = DAFTAR_USER.get(u);
                terapkanHakAksesRole(currentUser);
                dialogLogin.dispose();
            } else {
                JOptionPane.showMessageDialog(dialogLogin,
                        "Username atau password salah!\nPeriksa kembali kredensial Anda.",
                        "Login Gagal",
                        JOptionPane.ERROR_MESSAGE);
                txtPass.setText("");
                txtPass.requestFocus();
            }
        });

        txtPass.addActionListener(e -> btnMasuk.doClick());
        txtUser.addActionListener(e -> txtPass.requestFocus());

        btnPanel.add(btnBatal);
        btnPanel.add(btnMasuk);
        contentPane.add(btnPanel, BorderLayout.SOUTH);

        dialogLogin.setContentPane(contentPane);
        dialogLogin.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // 2. KONTROL HAK AKSES PER ROLE
    // -------------------------------------------------------------------------
    private void terapkanHakAksesRole(UserAccount user) {
        if (user == null) {
            lblUserLoginInfo.setText("Belum Login");
            tabbedPane.setEnabledAt(1, false);
            tabbedPane.setEnabledAt(2, false);
            return;
        }

        lblUserLoginInfo.setText(String.format("User: %s  |  Role: %s", user.getFullName(), user.getRole().name()));

        tabbedPane.setEnabledAt(1, true);
        tabbedPane.setEnabledAt(2, true);

        if (user.getRole() == Role.MANAGER) {
            btnHapusMaterial.setEnabled(true);
            btnHapusMaterial.setToolTipText("Hapus item material dari daftar RAB");
            btnSimpanDB.setEnabled(true);
            btnSimpanDB.setToolTipText("Sinkronkan seluruh data ke MySQL XAMPP Database");
            btnSimpanFile.setEnabled(true);
            btnMuatFile.setEnabled(true);
        } else if (user.getRole() == Role.ESTIMATOR) {
            btnHapusMaterial.setEnabled(false);
            btnHapusMaterial.setToolTipText("Hanya Manajer Proyek yang memiliki otoritas menghapus item material.");
            btnSimpanDB.setEnabled(false);
            btnSimpanDB.setToolTipText("Hanya Manajer Proyek yang memiliki otoritas sinkronisasi ke MySQL database.");
            btnSimpanFile.setEnabled(true);
            btnMuatFile.setEnabled(true);
        }

        perbaruiSeluruhTampilan();
    }

    private void aksiLogout() {
        int konfirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin logout dan mengunci kembali aplikasi?",
                "Konfirmasi Logout",
                JOptionPane.YES_NO_OPTION);

        if (konfirm == JOptionPane.YES_OPTION) {
            currentUser = null;
            tabbedPane.setSelectedIndex(0);
            tabbedPane.setEnabledAt(1, false);
            tabbedPane.setEnabledAt(2, false);
            tampilkanDialogLogin();
        }
    }

    // -------------------------------------------------------------------------
    // 3. HEADER PANEL (DENGAN IDENTITAS USER & LOGOUT)
    // -------------------------------------------------------------------------
    private void initHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel leftBrand = new JPanel(new GridLayout(2, 1, 2, 2));
        leftBrand.setOpaque(false);

        JLabel brandTitle = new JLabel("BUILDEVAL Enterprise");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Sistem Manajemen & Evaluasi Kelayakan Rencana Anggaran Biaya");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setForeground(new Color(0x94, 0xA3, 0xB8));

        leftBrand.add(brandTitle);
        leftBrand.add(brandSub);

        JPanel rightBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBox.setOpaque(false);

        JPanel userCard = new JPanel(new GridLayout(2, 1, 2, 2));
        userCard.setBackground(BG_DARK);
        userCard.setBorder(new EmptyBorder(6, 14, 6, 14));

        lblUserLoginInfo = new JLabel("User: Memuat... | Role: -");
        lblUserLoginInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUserLoginInfo.setForeground(new Color(0x38, 0xBD, 0xF8));

        JLabel lblProj = new JLabel("Proyek: " + rab.getNamaProyek());
        lblProj.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblProj.setForeground(new Color(0xCB, 0xD5, 0xE1));

        userCard.add(lblUserLoginInfo);
        userCard.add(lblProj);

        // Tombol Logout: Merah gelap, teks PUTIH kontras
        JButton btnLogout = buatModernButton("Logout", new Color(0xDC, 0x26, 0x26), new Color(0xB9, 0x1C, 0x1C), Color.WHITE);
        btnLogout.addActionListener(e -> aksiLogout());

        rightBox.add(userCard);
        rightBox.add(btnLogout);

        header.add(leftBrand, BorderLayout.WEST);
        header.add(rightBox, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // -------------------------------------------------------------------------
    // 4. TABBED PANE UTAMA
    // -------------------------------------------------------------------------
    private void initTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.setBackground(BG_PAGE);
        tabbedPane.setBorder(new EmptyBorder(8, 15, 12, 15));

        tabbedPane.addTab("[ Beranda (Executive Overview) ]", initTabBeranda());
        tabbedPane.addTab("[ Kelola Material (RAB Items) ]", initTabKelolaMaterial());
        tabbedPane.addTab("[ Backup & Database ]", initTabBackupDatabase());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    // 5. TAB 1: BERANDA / DASHBOARD OVERVIEW
    // -------------------------------------------------------------------------
    private JPanel initTabBeranda() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(BG_PAGE);
        panel.setBorder(new EmptyBorder(14, 12, 14, 12));

        // A. Project Banner
        JPanel banner = new JPanel(new BorderLayout(15, 0));
        banner.setBackground(CARD_BG);
        banner.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel bannerText = new JPanel(new GridLayout(3, 1, 3, 3));
        bannerText.setOpaque(false);

        JLabel bannerTag = new JLabel("INFORMASI PROYEK AKTIF");
        bannerTag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        bannerTag.setForeground(ACCENT_BLUE);

        JLabel bannerTitle = new JLabel(rab.getNamaProyek());
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        bannerTitle.setForeground(TEXT_MAIN);

        JLabel bannerDetail = new JLabel("Penanggung Jawab: " + rab.getManajer().getNamaManajer() + 
                "  •  Kontak: " + rab.getManajer().getKontak() + "  •  Kode RAB: " + rab.getIdRAB());
        bannerDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bannerDetail.setForeground(TEXT_MUTED);

        bannerText.add(bannerTag);
        bannerText.add(bannerTitle);
        bannerText.add(bannerDetail);
        banner.add(bannerText, BorderLayout.CENTER);

        // B. Metric Cards (3 Kolom)
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsGrid.setOpaque(false);

        valCardPagu = new JLabel("Rp 0,00");
        JPanel card1 = buatMetricCard("PAGU ANGGARAN (BUDGET LIMIT)", valCardPagu, "Batas maksimal pagu proyek", new Color(0x25, 0x63, 0xEB));

        valCardPengeluaran = new JLabel("Rp 0,00");
        JPanel card2 = buatMetricCard("TOTAL PENGELUARAN RAB", valCardPengeluaran, "Akumulasi subtotal seluruh item", new Color(0x7C, 0x3A, 0xED));

        valCardSisa = new JLabel("Rp 0,00");
        valCardStatus = new JLabel("STATUS: LAYAK");
        cardStatusPanel = buatMetricCardStatus(valCardSisa, valCardStatus);

        cardsGrid.add(card1);
        cardsGrid.add(card2);
        cardsGrid.add(cardStatusPanel);

        JPanel topContent = new JPanel(new BorderLayout(0, 14));
        topContent.setOpaque(false);
        topContent.add(banner, BorderLayout.NORTH);
        topContent.add(cardsGrid, BorderLayout.CENTER);

        panel.add(topContent, BorderLayout.NORTH);

        // C. Quick Action Panel & Total Item
        JPanel actionContainer = new JPanel(new BorderLayout(0, 10));
        actionContainer.setBackground(CARD_BG);
        actionContainer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JPanel actionHeader = new JPanel(new BorderLayout());
        actionHeader.setOpaque(false);

        JLabel lblActionTitle = new JLabel("Navigasi Cepat & Manajemen Operasional");
        lblActionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblActionTitle.setForeground(TEXT_MAIN);

        valTotalItemBeranda = new JLabel("Total Material Terdaftar: 0 Item");
        valTotalItemBeranda.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        valTotalItemBeranda.setForeground(ACCENT_BLUE);

        actionHeader.add(lblActionTitle, BorderLayout.WEST);
        actionHeader.add(valTotalItemBeranda, BorderLayout.EAST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnRow.setOpaque(false);

        JButton btnGoKelola = buatModernButton("Buka Kelola Material", ACCENT_BLUE, ACCENT_BLUE_HOVER, Color.WHITE);
        btnGoKelola.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        JButton btnGoBackup = buatModernButton("Buka Backup & DB", new Color(0x33, 0x41, 0x55), new Color(0x1E, 0x29, 0x3B), Color.WHITE);
        btnGoBackup.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        JButton btnCetakKonsol = buatModernButton("Cetak ke Konsol", new Color(0x0F, 0x76, 0x6E), new Color(0x11, 0x5E, 0x59), Color.WHITE);
        btnCetakKonsol.addActionListener(e -> {
            rab.cetakLaporanRAB();
            JOptionPane.showMessageDialog(this,
                    "Laporan RAB berhasil dicetak ke konsol terminal sistem.",
                    "Cetak Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        btnRow.add(btnGoKelola);
        btnRow.add(btnGoBackup);
        btnRow.add(btnCetakKonsol);

        actionContainer.add(actionHeader, BorderLayout.NORTH);
        actionContainer.add(btnRow, BorderLayout.CENTER);

        panel.add(actionContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buatMetricCard(String judul, JLabel labelNilai, String infoSub, Color stripColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel topStrip = new JPanel();
        topStrip.setPreferredSize(new Dimension(40, 4));
        topStrip.setBackground(stripColor);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(TEXT_MUTED);
        titlePanel.add(topStrip, BorderLayout.NORTH);
        titlePanel.add(lblJudul, BorderLayout.SOUTH);

        labelNilai.setFont(new Font("Segoe UI", Font.BOLD, 19));
        labelNilai.setForeground(TEXT_MAIN);

        JLabel lblSub = new JLabel(infoSub);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(labelNilai, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buatMetricCardStatus(JLabel labelSisa, JLabel labelStatus) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblJudul = new JLabel("SISA BUDGET & KELAYAKAN");
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblJudul.setForeground(TEXT_MUTED);

        labelSisa.setFont(new Font("Segoe UI", Font.BOLD, 19));
        labelSisa.setForeground(COLOR_LAYAK);

        labelStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelStatus.setForeground(COLOR_LAYAK);

        card.add(lblJudul, BorderLayout.NORTH);
        card.add(labelSisa, BorderLayout.CENTER);
        card.add(labelStatus, BorderLayout.SOUTH);

        return card;
    }

    // -------------------------------------------------------------------------
    // 6. TAB 2: KELOLA MATERIAL (FORM & TABEL)
    // -------------------------------------------------------------------------
    private JPanel initTabKelolaMaterial() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_PAGE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Form Input Card (Grid 2 Kolom)
        JPanel formCard = new JPanel(new BorderLayout(0, 8));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel formTitle = new JLabel("Tambah Item Material ke RAB");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formTitle.setForeground(TEXT_MAIN);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel gridInput = new JPanel(new GridBagLayout());
        gridInput.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtKode = buatStylingTextField();
        txtNama = buatStylingTextField();
        txtSatuan = buatStylingTextField();
        txtHarga = buatStylingTextField();
        txtJumlah = buatStylingTextField();

        // Baris 1: Kode Item | Nama Material
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        gridInput.add(new JLabel("Kode Item:"), g);
        g.gridx = 1; g.gridy = 0; g.weightx = 0.3;
        gridInput.add(txtKode, g);

        g.gridx = 2; g.gridy = 0; g.weightx = 0;
        gridInput.add(new JLabel("Nama Material:"), g);
        g.gridx = 3; g.gridy = 0; g.weightx = 0.7;
        gridInput.add(txtNama, g);

        // Baris 2: Satuan | Harga Satuan
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        gridInput.add(new JLabel("Satuan:"), g);
        g.gridx = 1; g.gridy = 1; g.weightx = 0.3;
        gridInput.add(txtSatuan, g);

        g.gridx = 2; g.gridy = 1; g.weightx = 0;
        gridInput.add(new JLabel("Harga Satuan (Rp):"), g);
        g.gridx = 3; g.gridy = 1; g.weightx = 0.7;
        gridInput.add(txtHarga, g);

        // Baris 3: Jumlah | Tombol Tambah
        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        gridInput.add(new JLabel("Jumlah:"), g);
        g.gridx = 1; g.gridy = 2; g.weightx = 0.3;
        gridInput.add(txtJumlah, g);

        btnTambahMaterial = buatModernButton("Tambah ke RAB", ACCENT_BLUE, ACCENT_BLUE_HOVER, Color.WHITE);
        btnTambahMaterial.addActionListener(e -> aksiTambahMaterial());

        g.gridx = 2; g.gridy = 2; g.gridwidth = 2; g.weightx = 0.7;
        gridInput.add(btnTambahMaterial, g);

        formCard.add(gridInput, BorderLayout.CENTER);
        panel.add(formCard, BorderLayout.NORTH);

        // Tabel Container Card
        JPanel tableCard = new JPanel(new BorderLayout(0, 8));
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Header Table Card & Action Buttons
        JPanel tableCardHeader = new JPanel(new BorderLayout());
        tableCardHeader.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Daftar Rincian Material Proyek");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTableTitle.setForeground(TEXT_MAIN);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnActions.setOpaque(false);

        JButton btnCari = buatModernButton("Cari Item", new Color(0x33, 0x41, 0x55), new Color(0x1E, 0x29, 0x3B), Color.WHITE);
        btnCari.addActionListener(e -> aksiCariMaterial());

        btnHapusMaterial = buatModernButton("Hapus Terpilih", new Color(0xDC, 0x26, 0x26), new Color(0xB9, 0x1C, 0x1C), Color.WHITE);
        btnHapusMaterial.addActionListener(e -> aksiHapusMaterial());

        btnActions.add(btnCari);
        btnActions.add(btnHapusMaterial);

        tableCardHeader.add(lblTableTitle, BorderLayout.WEST);
        tableCardHeader.add(btnActions, BorderLayout.EAST);
        tableCard.add(tableCardHeader, BorderLayout.NORTH);

        // JTable Setup
        String[] kolom = {"No", "Kode Item", "Nama Material", "Jumlah", "Harga Satuan", "Subtotal"};
        modelTabel = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelMaterial = new JTable(modelTabel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF8, 0xFA, 0xFC));
                } else {
                    c.setBackground(new Color(0xDB, 0xEA, 0xFE));
                }
                return c;
            }
        };

        tabelMaterial.setRowHeight(28);
        tabelMaterial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabelMaterial.setShowGrid(false);
        tabelMaterial.setIntercellSpacing(new Dimension(0, 0));

        // JTableHeader Styling: Dark Slate Background (#1E293B), Foreground PUTIH (Color.WHITE), setOpaque(true)
        JTableHeader th = tabelMaterial.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(0x1E, 0x29, 0x3B));
        th.setForeground(Color.WHITE);
        th.setOpaque(true);
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 36));

        // Custom Header Renderer agar teks Putih di atas Dark Slate pasti tampil di semua Look and Feel
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setOpaque(true);
                lbl.setBackground(new Color(0x1E, 0x29, 0x3B));
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(column == 0 || column == 1 || column == 3 ? JLabel.CENTER : 
                                          (column >= 4 ? JLabel.RIGHT : JLabel.LEFT));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(0x33, 0x41, 0x55)),
                        new EmptyBorder(6, 8, 6, 8)
                ));
                return lbl;
            }
        };
        th.setDefaultRenderer(headerRenderer);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(JLabel.RIGHT);

        tabelMaterial.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabelMaterial.getColumnModel().getColumn(0).setCellRenderer(centerRender);
        tabelMaterial.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabelMaterial.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        tabelMaterial.getColumnModel().getColumn(2).setPreferredWidth(260);
        tabelMaterial.getColumnModel().getColumn(3).setPreferredWidth(95);
        tabelMaterial.getColumnModel().getColumn(3).setCellRenderer(centerRender);
        tabelMaterial.getColumnModel().getColumn(4).setPreferredWidth(130);
        tabelMaterial.getColumnModel().getColumn(4).setCellRenderer(rightRender);
        tabelMaterial.getColumnModel().getColumn(5).setPreferredWidth(140);
        tabelMaterial.getColumnModel().getColumn(5).setCellRenderer(rightRender);

        JScrollPane scrollPane = new JScrollPane(tabelMaterial);
        scrollPane.setBorder(new LineBorder(CARD_BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        lblRingkasanTabel = new JLabel("Total: 0 Item | Total Pengeluaran: Rp 0,00");
        lblRingkasanTabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        lblRingkasanTabel.setForeground(TEXT_MUTED);
        tableCard.add(lblRingkasanTabel, BorderLayout.SOUTH);

        panel.add(tableCard, BorderLayout.CENTER);
        return panel;
    }

    // -------------------------------------------------------------------------
    // 7. TAB 3: BACKUP & DATABASE
    // -------------------------------------------------------------------------
    private JPanel initTabBackupDatabase() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 0));
        panel.setBackground(BG_PAGE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Card Kiri: File Repository
        JPanel cardFile = new JPanel(new BorderLayout(0, 14));
        cardFile.setBackground(CARD_BG);
        cardFile.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel headerFile = new JPanel(new GridLayout(2, 1, 3, 3));
        headerFile.setOpaque(false);
        JLabel titleFile = new JLabel("Berkas Backup Teks (Local File)");
        titleFile.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleFile.setForeground(TEXT_MAIN);
        JLabel subFile = new JLabel("Penyimpanan offline berbasis berkas teks berformat delimiter '|'.");
        subFile.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subFile.setForeground(TEXT_MUTED);
        headerFile.add(titleFile);
        headerFile.add(subFile);
        cardFile.add(headerFile, BorderLayout.NORTH);

        JTextArea descFile = new JTextArea(
                "Fitur ini memungkinkan Anda mencadangkan seluruh data item material ke dalam " +
                "berkas teks lokal 'buildeval_backup.txt' atau memuat kembali data dari berkas yang ada.\n\n" +
                "Kelebihan:\n" +
                "• Tidak memerlukan koneksi database.\n" +
                "• Rekonstruksi state objek aman dengan validasi penuh sebelum penimpaan data."
        );
        descFile.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descFile.setForeground(TEXT_MUTED);
        descFile.setLineWrap(true);
        descFile.setWrapStyleWord(true);
        descFile.setEditable(false);
        descFile.setOpaque(false);
        cardFile.add(descFile, BorderLayout.CENTER);

        JPanel btnFileRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnFileRow.setOpaque(false);

        btnSimpanFile = buatModernButton("Simpan ke File", ACCENT_BLUE, ACCENT_BLUE_HOVER, Color.WHITE);
        btnSimpanFile.addActionListener(e -> aksiSimpanFile());

        btnMuatFile = buatModernButton("Muat dari File", new Color(0x33, 0x41, 0x55), new Color(0x1E, 0x29, 0x3B), Color.WHITE);
        btnMuatFile.addActionListener(e -> aksiMuatFile());

        btnFileRow.add(btnSimpanFile);
        btnFileRow.add(btnMuatFile);
        cardFile.add(btnFileRow, BorderLayout.SOUTH);

        // Card Kanan: MySQL Database XAMPP
        JPanel cardDB = new JPanel(new BorderLayout(0, 14));
        cardDB.setBackground(CARD_BG);
        cardDB.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel headerDB = new JPanel(new GridLayout(2, 1, 3, 3));
        headerDB.setOpaque(false);
        JLabel titleDB = new JLabel("MySQL Database XAMPP");
        titleDB.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleDB.setForeground(TEXT_MAIN);
        JLabel subDB = new JLabel("Integrasi database relasional 'buildeval_db' via JDBC Driver.");
        subDB.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subDB.setForeground(TEXT_MUTED);
        headerDB.add(titleDB);
        headerDB.add(subDB);
        cardDB.add(headerDB, BorderLayout.NORTH);

        JTextArea descDB = new JTextArea(
                "Sinkronisasi data RAB langsung ke database MySQL XAMPP (localhost:3306/buildeval_db).\n\n" +
                "Karakteristik Transaksi:\n" +
                "• Menerapkan mekanisme Transaksi ACID (AutoCommit false, Commit, Rollback).\n" +
                "• Menyimpan entitas Manajer, Master Material, Rencana Anggaran, dan Detail Item secara utuh."
        );
        descDB.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descDB.setForeground(TEXT_MUTED);
        descDB.setLineWrap(true);
        descDB.setWrapStyleWord(true);
        descDB.setEditable(false);
        descDB.setOpaque(false);
        cardDB.add(descDB, BorderLayout.CENTER);

        JPanel btnDBRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnDBRow.setOpaque(false);

        btnSimpanDB = buatModernButton("Sinkronkan ke MySQL", new Color(0x05, 0x96, 0x69), new Color(0x04, 0x78, 0x57), Color.WHITE);
        btnSimpanDB.addActionListener(e -> aksiSimpanDatabase());

        btnDBRow.add(btnSimpanDB);
        cardDB.add(btnDBRow, BorderLayout.SOUTH);

        panel.add(cardFile);
        panel.add(cardDB);
        return panel;
    }

    // -------------------------------------------------------------------------
    // 8. HELPER STYLING (BUTTONS & TEXTFIELDS)
    // -------------------------------------------------------------------------
    private JButton buatModernButton(String text, Color bgNormal, Color bgHover, Color fgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fgColor);
        btn.setBackground(bgNormal);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(bgHover.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgHover);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgNormal);
                }
            }
        });

        return btn;
    }

    private JTextField buatStylingTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setForeground(Color.BLACK);
        tf.setCaretColor(Color.BLACK);
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x94, 0xA3, 0xB8), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        return tf;
    }

    // -------------------------------------------------------------------------
    // 9. LOGIKA EVENT & OPERASI DOMAIN
    // -------------------------------------------------------------------------
    private void aksiTambahMaterial() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String satuan = txtSatuan.getText().trim();
        String strHarga = txtHarga.getText().trim();
        String strJumlah = txtJumlah.getText().trim();

        if (kode.isEmpty() || nama.isEmpty() || satuan.isEmpty() || strHarga.isEmpty() || strJumlah.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Semua kolom input (Kode, Nama, Satuan, Harga, Jumlah) wajib diisi!",
                    "Input Kosong",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double harga = Double.parseDouble(strHarga.replace(',', '.'));
            int jumlah = Integer.parseInt(strJumlah);

            Material material = new Material(kode, nama, satuan, harga);
            DetailItem detail = new DetailItem(material, jumlah);

            rab.tambahItem(detail);

            txtKode.setText("");
            txtNama.setText("");
            txtSatuan.setText("");
            txtHarga.setText("");
            txtJumlah.setText("");
            txtKode.requestFocus();

            perbaruiSeluruhTampilan();
            JOptionPane.showMessageDialog(this,
                    "Material '" + nama + "' berhasil ditambahkan ke RAB.",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Format angka untuk Harga atau Jumlah tidak valid!\nPastikan memasukkan angka bulat untuk jumlah dan angka desimal/bulat untuk harga.",
                    "Format Angka Salah",
                    JOptionPane.ERROR_MESSAGE);
        } catch (BudgetOverlimitException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Peringatan Anggaran (Overbudget)",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "Validasi Gagal",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void aksiCariMaterial() {
        String inputKode = JOptionPane.showInputDialog(this,
                "Masukkan Kode Material yang ingin dicari (contoh: MAT01):",
                "Cari Material",
                JOptionPane.QUESTION_MESSAGE);

        if (inputKode == null || inputKode.trim().isEmpty()) {
            return;
        }

        DetailItem item = rab.cariItem(inputKode.trim());
        if (item != null) {
            Material m = item.getMaterial();
            String info = String.format(
                    "Informasi Material Ditemukan:\n\n" +
                    "• Kode Item     : %s\n" +
                    "• Nama Material : %s\n" +
                    "• Kuantitas     : %d %s\n" +
                    "• Harga Satuan  : %s\n" +
                    "• Subtotal      : %s",
                    m.getIdMaterial(),
                    m.getNamaMaterial(),
                    item.getJumlah(),
                    m.getSatuan(),
                    formatRupiah.format(m.getHargaSatuan()),
                    formatRupiah.format(item.getSubtotal())
            );

            for (int i = 0; i < modelTabel.getRowCount(); i++) {
                if (modelTabel.getValueAt(i, 1).toString().equalsIgnoreCase(m.getIdMaterial())) {
                    tabelMaterial.setRowSelectionInterval(i, i);
                    tabelMaterial.scrollRectToVisible(tabelMaterial.getCellRect(i, 0, true));
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, info, "Material Ditemukan", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Material dengan kode '" + inputKode + "' tidak terdaftar dalam RAB.",
                    "Tidak Ditemukan",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void aksiHapusMaterial() {
        if (currentUser != null && currentUser.getRole() != Role.MANAGER) {
            JOptionPane.showMessageDialog(this,
                    "Akses Ditolak! Hanya Manajer Proyek yang memiliki izin menghapus item material.",
                    "Hak Akses Terbatas",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = tabelMaterial.getSelectedRow();
        String kodeHapus;

        if (selectedRow >= 0) {
            kodeHapus = modelTabel.getValueAt(selectedRow, 1).toString();
        } else {
            kodeHapus = JOptionPane.showInputDialog(this,
                    "Pilih baris pada tabel terlebih dahulu atau ketik Kode Material yang ingin dihapus:",
                    "Hapus Material",
                    JOptionPane.QUESTION_MESSAGE);
        }

        if (kodeHapus == null || kodeHapus.trim().isEmpty()) {
            return;
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus material dengan kode '" + kodeHapus + "' dari RAB?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (konfirmasi == JOptionPane.YES_OPTION) {
            boolean terhapus = rab.hapusItem(kodeHapus.trim());
            if (terhapus) {
                perbaruiSeluruhTampilan();
                JOptionPane.showMessageDialog(this,
                        "Material '" + kodeHapus + "' berhasil dihapus dari RAB.",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Material dengan kode '" + kodeHapus + "' tidak ditemukan dalam daftar.",
                        "Gagal",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void aksiSimpanFile() {
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setSelectedFile(defaultFilePath.toFile());
        fileChooser.setDialogTitle("Simpan Data RAB ke Berkas Backup");

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                BuildEvalFileRepository.simpanKeFile(fileToSave.toPath(), rab);
                JOptionPane.showMessageDialog(this,
                        "Data RAB berhasil disimpan ke berkas:\n" + fileToSave.getAbsolutePath(),
                        "Simpan Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan ke file: " + e.getMessage(),
                        "Error Simpan",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aksiMuatFile() {
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setSelectedFile(defaultFilePath.toFile());
        fileChooser.setDialogTitle("Muat Data RAB dari Berkas Backup");

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                BuildEvalFileRepository.muatDariFile(fileToOpen.toPath(), rab);
                perbaruiSeluruhTampilan();
                JOptionPane.showMessageDialog(this,
                        "Data RAB berhasil dimuat dari berkas:\n" + fileToOpen.getAbsolutePath(),
                        "Muat Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this,
                        "Gagal memuat dari file: " + e.getMessage(),
                        "Error Muat",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aksiSimpanDatabase() {
        if (currentUser != null && currentUser.getRole() != Role.MANAGER) {
            JOptionPane.showMessageDialog(this,
                    "Akses Ditolak! Hanya Manajer Proyek yang memiliki izin melakukan sinkronisasi database.",
                    "Hak Akses Terbatas",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int konfirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menyinkronkan data RAB ini ke MySQL Database ('buildeval_db')?",
                "Konfirmasi Sinkronisasi Database",
                JOptionPane.YES_NO_OPTION);

        if (konfirm == JOptionPane.YES_OPTION) {
            try {
                dao.simpanRAB(rab);
                JOptionPane.showMessageDialog(this,
                        "Sinkronisasi berhasil! Data RAB dan seluruh detail item tersimpan di MySQL database 'buildeval_db'.",
                        "Sinkronisasi Berhasil",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan ke database MySQL:\n" + e.getMessage() + 
                        "\n\nPastikan MySQL di XAMPP Control Panel sudah aktif di port 3306.",
                        "Koneksi Database Gagal",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // -------------------------------------------------------------------------
    // 10. REFRESH DATA & METRIK
    // -------------------------------------------------------------------------
    private void perbaruiSeluruhTampilan() {
        modelTabel.setRowCount(0);
        int no = 1;
        for (DetailItem item : rab.getListMaterial()) {
            Material m = item.getMaterial();
            String kode = (m != null) ? m.getIdMaterial() : "-";
            String nama = (m != null) ? m.getNamaMaterial() : "-";
            String jml = item.getJumlah() + " " + ((m != null) ? m.getSatuan() : "");
            String harga = (m != null) ? formatRupiah.format(m.getHargaSatuan()) : "-";
            String subtotal = formatRupiah.format(item.getSubtotal());

            modelTabel.addRow(new Object[]{no++, kode, nama, jml, harga, subtotal});
        }

        double pagu = rab.getBatasBudget();
        double total = rab.hitungTotalBiaya();
        double sisa = pagu - total;
        int totalItem = rab.getListMaterial().size();

        valCardPagu.setText(formatRupiah.format(pagu));
        valCardPengeluaran.setText(formatRupiah.format(total));
        valCardSisa.setText(formatRupiah.format(sisa));
        valTotalItemBeranda.setText("Total Material Terdaftar: " + totalItem + " Item");

        if (total <= pagu) {
            valCardStatus.setText("STATUS: LAYAK (Dalam Pagu)");
            valCardStatus.setForeground(COLOR_LAYAK);
            valCardSisa.setForeground(COLOR_LAYAK);
            if (cardStatusPanel != null) {
                cardStatusPanel.setBackground(CARD_BG);
            }
        } else {
            valCardStatus.setText("STATUS: OVERBUDGET (Melebihi Pagu)");
            valCardStatus.setForeground(COLOR_OVER);
            valCardSisa.setForeground(COLOR_OVER);
            if (cardStatusPanel != null) {
                cardStatusPanel.setBackground(COLOR_OVER_LIGHT);
            }
        }

        lblRingkasanTabel.setText("Total: " + totalItem + " Item Material | Total Pengeluaran: " + 
                formatRupiah.format(total) + " | Sisa Budget: " + formatRupiah.format(sisa));
    }

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        Font fontUtama = new Font("Segoe UI", Font.PLAIN, 12);
        Font fontBold = new Font("Segoe UI", Font.BOLD, 12);
        UIManager.put("Button.font", fontBold);
        UIManager.put("Label.font", fontUtama);
        UIManager.put("TabbedPane.font", fontBold);
        UIManager.put("Table.font", fontUtama);
        UIManager.put("TableHeader.font", fontBold);

        SwingUtilities.invokeLater(() -> {
            BuildEvalGUI gui = new BuildEvalGUI();
            gui.setVisible(true);
        });
    }
}

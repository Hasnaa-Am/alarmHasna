//Bagian ini mengimpor berbagai perpustakaan Java yang diperlukan untuk membangun aplikasi StudyTimer.
import javax.sound.sampled.*; //untuk memutar suara notifikasi
import javax.swing.*; //-awt digunakan untuk membuat komponen antarmuka pengguna (GUI)
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*; //menangani operasi input/output file
import java.text.SimpleDateFormat; //untuk mengimpor kelas SimpleDateFormat dari paket java.text dalam Java
import java.util.ArrayList; //utilitas untuk format tanggal/waktu, daftar, dan timer.
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
//Bagian ini mendeklarasikan kelas utama StudyTimer

public class StudyTimer {
    private JFrame frame;
    private JTextField timeInput;
    private JButton startButton;
    private JButton stopButton;
    private JButton resetButton;
    private JButton resetHistoryButton; 
    private JLabel statusLabel;
    private Timer timer;
    private ArrayList<Integer> studyHistory;
    private JTextArea historyArea;

    public StudyTimer() {
        frame = new JFrame("Pengingat Waktu Belajar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 204, 255)); // Warna latar belakang pink muda

        //Bagian ini mengatur panel input di mana pengguna dapat memasukkan waktu belajar dalam detik dan
        // mengelola timer
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.setBackground(new Color(255, 204, 255));

        timeInput = new JTextField(5);
        timeInput.setDocument(new JTextFieldLimit(3));
        timeInput.setHorizontalAlignment(JTextField.RIGHT);

        startButton = new JButton("Mulai");
        stopButton = new JButton("Berhenti");
        resetButton = new JButton("Reset");
        resetHistoryButton = new JButton("Reset Riwayat"); // Tombol baru untuk mereset riwayat

        statusLabel = new JLabel("Masukkan waktu (detik):");
        statusLabel.setForeground(new Color(102, 0, 102));

        inputPanel.add(statusLabel);
        inputPanel.add(timeInput);
        inputPanel.add(startButton);
        inputPanel.add(stopButton);
        inputPanel.add(resetButton);
        inputPanel.add(resetHistoryButton); // Tambahkan tombol "Reset Riwayat" ke panel input

        frame.add(inputPanel, BorderLayout.NORTH);

        //Bagian ini membuat panel untuk menampilkan riwayat belajar
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(new Color(255, 204, 255));

        historyArea = new JTextArea(10, 40);
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(255, 229, 255));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        historyPanel.add(new JLabel("Riwayat Waktu Belajar:"), BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(historyPanel, BorderLayout.CENTER);

        //Kustomisasi Tombol ketika tombol di klik
        startButton.setBackground(new Color(255, 153, 204));
        stopButton.setBackground(new Color(255, 153, 204));
        resetButton.setBackground(new Color(255, 153, 204));
        resetHistoryButton.setBackground(new Color(255, 153, 204)); 

        startButton.setForeground(Color.BLACK);
        stopButton.setForeground(Color.BLACK);
        resetButton.setForeground(Color.BLACK);
        resetHistoryButton.setForeground(Color.BLACK); // Warna teks hitam

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int seconds = Integer.parseInt(timeInput.getText());
                startTimer(seconds);
            }
        });
//// Action listener untuk tombol "Berhenti"
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer != null) {
                    timer.cancel();
                    statusLabel.setText("Timer dihentikan.");
                }
            }
        });
//// Action listener untuk tombol "Reset"
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer != null) {
                    timer.cancel();
                }
                statusLabel.setText("Timer direset.");
                timeInput.setText("");
            }
        });
//// Action listener untuk tombol "Reset Riwayat"
        resetHistoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetHistory(); 
            }
        });
//// Menampilkan frame utama
        frame.setVisible(true);
        studyHistory = new ArrayList<>(); //Inisialisasi ArrayList untuk menyimpan riwayat waktu belajar
    }
//Fungsi Memulai Timer
    private void startTimer(int seconds) {
        if (seconds <= 0) {
            JOptionPane.showMessageDialog(frame, "Masukkan waktu belajar yang valid (lebih dari 0 detik)!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            int remainingSeconds = seconds;
            int intervalSeconds = 2; // Ganti interval notifikasi menjadi setiap 2 detik
            boolean notificationSent = false;

            @Override
            public void run() {
                if (remainingSeconds <= 0) {
                    playSound("lagu (1).wav");
                    statusLabel.setText("Waktu habis!");
                    JOptionPane.showMessageDialog(frame, "Waktu belajar telah habis!", "Pemberitahuan", JOptionPane.INFORMATION_MESSAGE);
                    timer.cancel();

                    studyHistory.add(seconds);
                    saveHistory();

                    showHistory();
                } else {
                    int minutes = remainingSeconds / 60;
                    int secs = remainingSeconds % 60;
                    String timeString = String.format("%02d:%02d", minutes, secs);
                    statusLabel.setText("Sisa waktu: " + timeString);
                    remainingSeconds--;

                    if (remainingSeconds % intervalSeconds == 0 && !notificationSent) {
                        JOptionPane.showMessageDialog(frame, "Anda telah belajar selama " + (seconds - remainingSeconds) + " detik", "Notifikasi", JOptionPane.PLAIN_MESSAGE);
                        notificationSent = true;
                    }
                }
            }
        }, 0, 1000);
        statusLabel.setText("Timer berjalan...");
    }

    //Fungsi Memutar Suara
    private void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    //Fungsi Menyimpan Riwayat waktu belajar ke file
    private void saveHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("study_history.txt", true))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = dateFormat.format(new Date());
            writer.write(timestamp + " - Durasi belajar: " + studyHistory.get(studyHistory.size() - 1) + " detik");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Fungsi menampilkan riwayat waktu belajar
    private void showHistory() {
        StringBuilder historyMessage = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("study_history.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                historyMessage.append(line).append("\n");
            }
            historyMessage.append("\nTerus belajar! Semangat! \uD83D\uDCD6\uD83D\uDCAA");
            historyArea.setText(historyMessage.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Tidak ada riwayat waktu belajar tersimpan.", "Riwayat Waktu Belajar", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //Fungsi Mereset Riwayat waktu belajar
    private void resetHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("study_history.txt"))) {
            writer.write(""); // Menulis ulang file kosong
            historyArea.setText(""); // Mengosongkan tampilan riwayat di JTextArea
            studyHistory.clear(); // Mengosongkan riwayat dari ArrayList
            JOptionPane.showMessageDialog(frame, "Riwayat waktu belajar telah di-reset.", "Reset Riwayat", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Gagal mereset riwayat waktu belajar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //menjelaskan tentang kelas JTextFieldLimit yang berfungsi membatasi input pada JTextField 
    //agar hanya menerima angka dan tidak melebihi jumlah karakter tertentu. 
    //Kelas ini menggunakan PlainDocument untuk mengatur model data teks dan 
    //mengimplementasikan metode insertString untuk memvalidasi dan memasukkan teks ke dalam komponen teks
    class JTextFieldLimit extends PlainDocument {
        private int limit;

        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null)
                return;

            if ((getLength() + str.length()) <= limit) {
                try {
                    Integer.parseInt(str);
                    super.insertString(offset, str, attr);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    //menggambarkan metode main yang merupakan titik awal eksekusi program, 
    //mengatur tampilan antarmuka pengguna agar sesuai dengan sistem operasi yang digunakan, 
    //dan memulai aplikasi StudyTimer dalam thread event-dispatching untuk memastikan keamanan dalam manipulasi GUI.

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new StudyTimer();
            }
        });
    }
}
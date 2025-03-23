import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class CarRacingGuessingGame extends JFrame {
    private JPanel mainPanel;
    private RoadPanel roadPanel;
    private Image whiteCarImage;
    private Image whiteCarOneImage;
    private Image whiteCarTwoImage;
    private Image flagImage;
    private JButton topButton;
    private Timer gameTimer;
    private boolean isGameRunning = false;
    private Random random = new Random();
    private long startTime = 0;
    private long elapsedTime = 0;
    private int selectedCar = 0; // Seçilen araba (1: üst, 2: orta, 3: alt)
    private int countdown = 0; // Geri sayım için
    private boolean isCountingDown = false; // Geri sayım durumu
    
    // Araç pozisyonları ve hızları
    private int car1X = 113;
    private int car2X = 113;
    private int car3X = 113;
    private int car1Speed = 0;
    private int car2Speed = 0;
    private int car3Speed = 0;
    
    public CarRacingGuessingGame() {
        // Pencere başlığı ve boyutu
        setTitle("Modern Masaüstü Uygulaması");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Resimleri yükle
        try {
            whiteCarImage = ImageIO.read(new File("image/beyaz.png"));
            whiteCarOneImage = ImageIO.read(new File("image/beyaz-bir.png"));
            whiteCarTwoImage = ImageIO.read(new File("image/beyaz-iki.png"));
            flagImage = ImageIO.read(new File("image/bayrak.png"));
            // Resimleri uygun boyuta getir
            whiteCarImage = whiteCarImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            whiteCarOneImage = whiteCarOneImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            whiteCarTwoImage = whiteCarTwoImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            flagImage = flagImage.getScaledInstance(200, 100, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Resim yüklenemedi: " + e.getMessage());
        }
        
        // Ana panel oluşturma
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);
        
        // Yol paneli oluşturma
        roadPanel = new RoadPanel();
        
        // Ana panele yol panelini ekleme
        mainPanel.add(roadPanel, BorderLayout.CENTER);
        
        // Ana pencereye panel ekleme
        add(mainPanel);
        
        // Timer oluşturma
        gameTimer = new Timer();
    }
    
    // Yol paneli sınıfı
    private class RoadPanel extends JPanel {
        public RoadPanel() {
            setBackground(new Color(30, 30, 30)); // Daha koyu gri yol rengi
            setPreferredSize(new Dimension(1920, 1080)); // Tam ekran yol
            setLayout(null); // Mutlak konumlandırma için
            
            // Buton oluşturma
            topButton = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Arka planı yeşil yap (oval)
                    g2d.setColor(new Color(0, 150, 0)); // Koyu yeşil
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // 20 piksel yuvarlak köşeler
                    
                    // Bayrak resmini çiz (daha küçük)
                    if (flagImage != null) {
                        int flagWidth = getWidth() - 20; // 10 piksel kenar boşluğu
                        int flagHeight = getHeight() - 20;
                        int flagX = (getWidth() - flagWidth) / 2;
                        int flagY = (getHeight() - flagHeight) / 2;
                        g2d.drawImage(flagImage, flagX, flagY, flagWidth, flagHeight, null);
                    }
                }
            };
            topButton.setSize(200, 100);
            topButton.setLocation(860, 50); // Üst şeridin ortasına yerleştir
            topButton.setContentAreaFilled(false);
            topButton.setBorderPainted(false);
            
            // Butona tıklama olayı ekleme
            topButton.addActionListener(e -> {
                if (!isGameRunning) {
                    startGame();
                }
            });
            
            add(topButton);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Geri sayımı çiz
            if (isCountingDown && countdown > 0) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 150));
                String countStr = String.valueOf(countdown);
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(countStr);
                int textHeight = fm.getHeight();
                g2d.drawString(countStr, 
                    (getWidth() - textWidth) / 2,
                    (getHeight() + textHeight / 2) / 2);
            }
            
            // Sayacı çiz
            if (isGameRunning || elapsedTime > 0) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                long currentTime = isGameRunning ? System.currentTimeMillis() - startTime : elapsedTime;
                String timeStr = String.format("Süre: %.2f sn", currentTime / 1000.0);
                g2d.drawString(timeStr, 20, 40);
            }
            
            // Yol kenarları
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3.0f)); // Kenar çizgilerini kalınlaştır
            g2d.drawLine(0, 0, getWidth(), 0);
            g2d.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            
            // Yatay şerit çizgileri (düz ve kalın)
            int laneHeight = getHeight() / 3;
            g2d.setStroke(new BasicStroke(3.0f)); // Şerit çizgilerini kalınlaştır
            for (int i = 1; i < 3; i++) {
                int y = laneHeight * i;
                g2d.drawLine(0, y, getWidth(), y); // Düz çizgi
            }

            // Kesikli orta şeritler
            g2d.setStroke(new BasicStroke(2.0f)); // Kesikli çizgileri biraz daha ince yap
            float[] dashPattern = {30, 20}; // 30 piksel çizgi, 20 piksel boşluk
            g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
            
            // Her şeridin ortasına kesikli çizgi çiz
            for (int i = 0; i < 3; i++) {
                int y = (laneHeight * i) + (laneHeight / 2);
                g2d.drawLine(0, y, getWidth(), y);
            }
            
            // Kırmızı dikey bitiş çizgisi (sağdan 3 cm içeride)
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(5.0f)); // Çizgi kalınlığı
            int finishLineX = getWidth() - 113; // 3 cm = 113 piksel
            g2d.drawLine(finishLineX, 0, finishLineX, getHeight());
            g2d.setStroke(new BasicStroke(1.0f)); // Çizgi kalınlığını sıfırla

            // Dama deseni (bitiş çizgisinin sağında)
            int squareSize = 20; // Her bir karenin boyutu
            int patternWidth = 100; // Dama deseninin toplam genişliği (60'tan 100'e çıkardım)
            int startX = finishLineX + 5; // Bitiş çizgisinden 5 piksel sonra başla
            
            for (int x = 0; x < patternWidth; x += squareSize) {
                for (int y = 0; y < getHeight(); y += squareSize) {
                    if ((x / squareSize + y / squareSize) % 2 == 0) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(Color.BLACK);
                    }
                    g2d.fillRect(startX + x, y, squareSize, squareSize);
                }
            }
            
            // Beyaz araç resmini çiz (üst şerit)
            if (whiteCarImage != null) {
                int carY = laneHeight / 2 - 50; // Şeridin ortasına yerleştir
                g2d.drawImage(whiteCarImage, car1X, carY, null);
            }
            
            // İkinci beyaz araç resmini çiz (orta şerit)
            if (whiteCarOneImage != null) {
                int carY = laneHeight + (laneHeight / 2) - 50; // Orta şeridin ortasına yerleştir
                g2d.drawImage(whiteCarOneImage, car2X, carY, null);
            }
            
            // Üçüncü beyaz araç resmini çiz (alt şerit)
            if (whiteCarTwoImage != null) {
                int carY = (laneHeight * 2) + (laneHeight / 2) - 50; // Alt şeridin ortasına yerleştir
                g2d.drawImage(whiteCarTwoImage, car3X, carY, null);
            }
        }
    }
    
    private void startGame() {
        // Araba seçimi için dialog göster
        String[] options = {
            "Üst şeritteki beyaz araba",
            "Orta şeritteki beyaz araba",
            "Alt şeritteki beyaz araba"
        };
        
        int choice = JOptionPane.showOptionDialog(
            this,
            "Hangi arabanın kazanacağını düşünüyorsunuz?",
            "Araba Seçimi",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );
        
        // Eğer kullanıcı bir seçim yapmadan pencereyi kapatırsa
        if (choice == -1) {
            return;
        }
        
        // Seçilen arabayı kaydet (1: üst, 2: orta, 3: alt)
        selectedCar = choice + 1;
        
        // Butonu gizle
        topButton.setVisible(false);
        
        // Geri sayımı başlat
        countdown = 4; // 4'ten başla
        isCountingDown = true;
        
        // Geri sayım için timer
        Timer countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (countdown > 0) {
                    countdown--;
                    roadPanel.repaint();
                    if (countdown > 0) {
                        // Ses çal (beep)
                        Toolkit.getDefaultToolkit().beep();
                    }
                } else {
                    countdownTimer.cancel();
                    isCountingDown = false;
                    // Yarışı başlat
                    startRace();
                }
            }
        }, 0, 1000); // Her saniye
    }
    
    private void startRace() {
        isGameRunning = true;
        // Sayacı sıfırla ve başlat
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        
        // Arabaları başlangıç pozisyonuna getir
        car1X = 113;
        car2X = 113;
        car3X = 113;
        
        // Yeni Timer oluştur (eski timer iptal edildiyse)
        gameTimer = new Timer();
        
        // Rastgele hızlar atama (10-30 piksel/saniye)
        car1Speed = random.nextInt(21) + 10;
        car2Speed = random.nextInt(21) + 10;
        car3Speed = random.nextInt(21) + 10;
        
        // Timer'ı başlat
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Arabaları hareket ettir
                car1X += car1Speed;
                car2X += car2Speed;
                car3X += car3Speed;
                
                // Çarpışma kontrolü
                int finishLineX = 1920 - 113;
                if (car1X + 100 >= finishLineX || car2X + 100 >= finishLineX || car3X + 100 >= finishLineX) {
                    gameTimer.cancel();
                    isGameRunning = false;
                    
                    // Süreyi kaydet
                    elapsedTime = System.currentTimeMillis() - startTime;
                    
                    // Kazanan arabayı belirle
                    int winningCar = 0;
                    String winner = "";
                    if (car1X + 100 >= finishLineX) {
                        winner = "Üst şeritteki beyaz araba";
                        winningCar = 1;
                    } else if (car2X + 100 >= finishLineX) {
                        winner = "Orta şeritteki beyaz araba";
                        winningCar = 2;
                    } else {
                        winner = "Alt şeritteki beyaz araba";
                        winningCar = 3;
                    }
                    
                    // Butonu tekrar göster
                    topButton.setVisible(true);
                    
                    // Tahmin sonucunu kontrol et
                    String result = selectedCar == winningCar ? "KAZANDINIZ!" : "KAYBETTİNİZ!";
                    
                    // Mesaj kutusunu göster
                    JOptionPane.showMessageDialog(CarRacingGuessingGame.this, 
                        String.format("%s\n%s kazandı!\nGeçen süre: %.2f saniye", 
                            result, winner, elapsedTime / 1000.0), 
                        "Oyun Bitti", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Arabaları başlangıç pozisyonuna döndür ve hızları sıfırla
                    car1X = 113;
                    car2X = 113;
                    car3X = 113;
                    car1Speed = 0;
                    car2Speed = 0;
                    car3Speed = 0;
                    selectedCar = 0; // Seçimi sıfırla
                    roadPanel.repaint();
                }
                
                // Ekranı yenile
                roadPanel.repaint();
            }
        }, 0, 50); // Her 50 milisaniyede bir güncelle (20 FPS)
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new CarRacingGuessingGame().setVisible(true);
            }
        });
    }
} 
import java.awt.*;
import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import javax.swing.JFileChooser.*;
import javax.swing.filechooser.*;
import javax.imageio.ImageIO.*;
import java.awt.image.*;

public class FractalExplorer
{
    // Размер экрана в пикселях
    private int displaySize;

    private JImageDisplay display;

    private FractalGenerator fractal;

    private Rectangle2D.Double range;

    public FractalExplorer(int size)
    {
        // Задаём размер дисплея
        displaySize = size;

        // Инициализация FractalGenerator
        fractal = new Mandelbrot();
        // Задаём диапазон
        range = new Rectangle2D.Double();
        fractal.getInitialRange(range);
        // Создаём новый дисплей
        display = new JImageDisplay(displaySize, displaySize);
    }

    // Создание окна
    public void createAndShowGUI()
    {
        display.setLayout(new BorderLayout());
        JFrame myFrame = new JFrame("Fractal Explorer");

        myFrame.add(display, BorderLayout.CENTER);

        JButton resetButton = new JButton("Reset");

        /** Инициализация событий для кнопок **/
        ButtonHandler resetHandler = new ButtonHandler();
        resetButton.addActionListener(resetHandler);

        /** Инициализация событий для нажатия мыши. **/
        MouseHandler click = new MouseHandler();
        display.addMouseListener(click);

        /** Устанавливаем операцию закрытия фрейма по умолчанию на обычный выход. **/
        myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /** Создание поля со списком. **/
        JComboBox myComboBox = new JComboBox();

        /** Добавление фракталов в список. **/
        FractalGenerator mandelbrotFractal = new Mandelbrot();
        myComboBox.addItem(mandelbrotFractal);
        FractalGenerator tricornFractal = new Tricorn();
        myComboBox.addItem(tricornFractal);
        FractalGenerator burningShipFractal = new BurningShip();
        myComboBox.addItem(burningShipFractal);

        /** Инициализация событий при выборе фрактала. **/
        ButtonHandler fractalChooser = new ButtonHandler();
        myComboBox.addActionListener(fractalChooser);

        JPanel myPanel = new JPanel();
        JLabel myLabel = new JLabel("Fractal:");
        myPanel.add(myLabel);
        myPanel.add(myComboBox);
        myFrame.add(myPanel, BorderLayout.NORTH);

        /**
         * Создание кнопки сохранения и добавление её в JPanel
         * в позиции BorderLayout.SOUTH вместе с кнопкой сброса.
         */
        JButton saveButton = new JButton("Save");
        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(saveButton);
        myBottomPanel.add(resetButton);
        myFrame.add(myBottomPanel, BorderLayout.SOUTH);

        /** Экземпляр ButtonHandler для кнопки сохранения. **/
        ButtonHandler saveHandler = new ButtonHandler();
        saveButton.addActionListener(saveHandler);


        myFrame.pack();
        myFrame.setVisible(true);
        myFrame.setResizable(false);
    }

    private void drawFractal()
    {
        // Смотрим каждый пиксель на дисплее
        for (int x=0; x<displaySize; x++)
        {
            for (int y=0; y<displaySize; y++)
            {
                double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, x);
                double yCoord = fractal.getCoord(range.y, range.y + range.height, displaySize, y);

                int iteration = fractal.numIterations(xCoord, yCoord);

                if (iteration == -1){
                    display.drawPixel(x, y, 0);
                }
                else {
                    float hue = 0.7f + (float) iteration / 200f;
                    int rgbColor = Color.HSBtoRGB(hue, 1f, 1f);

                    // Окрашивает пиксель в выбранный цвет
                    display.drawPixel(x, y, rgbColor);
                }

            }
        }
        
        display.repaint();
    }
    // Внутренний класс для реализации конструктора ActionListener и сброса значений.
    private class ResetHandler implements ActionListener
    {
        // Функция сбрасывает диапазон до начального значения, заданного генератором, а затем рисует фрактал.
        public void actionPerformed(ActionEvent e)
        {
            fractal.getInitialRange(range);
            drawFractal();
        }
    }

    // Обработчик нажатия кновки мыши
    private class MouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            // Перевод позиции мыши в Х координату
            int x = e.getX();

            double xCoord = fractal.getCoord(range.x, range.x + range.width, displaySize, x);

            int y = e.getY();
            double yCoord = fractal.getCoord(range.y, range.y + range.height, displaySize, y);

            fractal.recenterAndZoomRange(range, xCoord, yCoord, 0.5);

            drawFractal();
        }
    }

    private class ButtonHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            /** Получение команды. **/
            String command = e.getActionCommand();

            /** Если команда на изменение фрактала. **/
            if (e.getSource() instanceof JComboBox) {
                JComboBox mySource = (JComboBox) e.getSource();
                fractal = (FractalGenerator) mySource.getSelectedItem();
                fractal.getInitialRange(range);
                drawFractal();

            }
            /** Если команда на сброс. **/
            else if (command.equals("Reset")) {
                fractal.getInitialRange(range);
                drawFractal();
            }
            /** Если команда на сохранение. **/
            else if (command.equals("Save")) {

                /** Allow the user to choose a file to save the image to. **/
                JFileChooser myFileChooser = new JFileChooser();

                /** Save only PNG images. **/
                FileFilter extensionFilter = new FileNameExtensionFilter("PNG Images", ".png");
                myFileChooser.setFileFilter(extensionFilter);
                /** Разрешить пользователю выбрать файл для сохранения изображения. **/
                myFileChooser.setAcceptAllFileFilterUsed(false);

                /** Всплывает окно «Сохранить файл», в котором пользователь
                 * может выбрать каталог и файл для сохранения. **/
                int userSelection = myFileChooser.showSaveDialog(display);

                /** Если результатом операции выбора файла является APPROVE_OPTION,
                 * продолжите операцию сохранения файла. **/
                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    /** Get the file and file name. **/
                    java.io.File file = myFileChooser.getSelectedFile();

                    String file_name = file.getName();

                    /** Попытка сохранить изображение. **/
                    try {
                        BufferedImage displayImage = display.getImage();
                        javax.imageio.ImageIO.write(displayImage, "png", file);
                    }
                    /** Получение всех ошибок и вывод их в сообщении. **/
                    catch (Exception exception) {
                        JOptionPane.showMessageDialog(display,
                                exception.getMessage(), "Невозможно сохранить картинку",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                /** Если результат операции сохранения файла не APPROVE_OPTION - выход из функции. **/
                else return;
            }
        }
    }

    // Точка входа
    public static void main(String[] args)
    {
        FractalExplorer displayExplorer = new FractalExplorer(600);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
}
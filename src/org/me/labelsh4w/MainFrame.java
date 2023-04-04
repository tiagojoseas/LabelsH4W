package org.me.labelsh4w;

import com.google.gson.Gson;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.crypto.interfaces.PBEKey;
import javax.swing.JFrame;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author tiago
 */
public final class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    private MainEncomenda encomenda;
    private PartialEncomenda partialEncomenda;
    private String pathFileEnc;
    private final Gson gson = new Gson();
    private Scanner myReader;
    private boolean editingPartial = false;

    public MainFrame() {
        initComponents();
        this.pathFileEnc = "";
        this.newEncomenda();
        this.addWindowListener(exit);
    }

    private final WindowAdapter exit = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            /*FileWriter fw = new FileWriter(configFolder + configFile);
            System.out.println("CONFIG FILE: " + pathFileEnc);
            fw.write(gson.toJson(history.mapHistory));
            fw.close();*/

            JFrame frame = new JFrame("Sair");
            if (pathFileEnc.isEmpty()) {
                int res = JOptionPane.showConfirmDialog(
                        frame,
                        "Pretende Guardar o Novo Documento?",
                        "Sair",
                        JOptionPane.YES_NO_OPTION);

                if (res == JOptionPane.YES_OPTION) {
                    save();
                }
            } else {
                save();
            }
            System.exit(0);
        }
    };

    /**
     * This funcion inserts a Map of the sizes (keys) and the respectives
     * quantities (values) into the table
     */
    private void addToMainTable(MainEncomenda enc) {
        DefaultTableModel model = (DefaultTableModel) this.tableMain.getModel();
        model.setRowCount(0);

        String sortedSizesQuantities = "";
        for (Map.Entry<String, Integer> entry : enc.getMapSizesQuantities().entrySet()) {
            String size = entry.getKey();
            Integer value = entry.getValue();
            sortedSizesQuantities += size + "-" + Integer.toString(value) + "/";
            Object row[] = {size, value, enc.getRestOfSize(size, null)};
            model.addRow(row);
        }
        // Sort Sizes and Quantites String
        this.encomenda.setSizesAndQuantities(sortedSizesQuantities);
        this.taMain.setText(sortedSizesQuantities);

        this.labelRest.setText(enc.getRest() + " de " + this.encomenda.getTotalQuantities());
    }

    private void addToPartialTable(MainEncomenda enc, PartialEncomenda penc) {
        DefaultTableModel model = (DefaultTableModel) this.tablePartial.getModel();
        model.setRowCount(0);

        for (Map.Entry<Integer, HashMap<String, Integer>> box : penc.getMapBoxes(enc.getMapOfRest(penc.getID())).entrySet()) {
            Integer IdBox = box.getKey();
            HashMap<String, Integer> mapSizesQuantities = box.getValue();
            for (Map.Entry<String, Integer> entry : mapSizesQuantities.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                Object row[] = {IdBox, key, value};
                model.addRow(row);
            }
        }
    }

    private void addToPartialTablePDF(MainEncomenda enc, PartialEncomenda penc) {
        DefaultTableModel model = (DefaultTableModel) this.tablePDF.getModel();
        model.setRowCount(0);

        for (String size : enc.getMapSizesQuantities().keySet()) {
            int qt = penc.getQuantityBySize(size);
            if (qt != 0) {
                Object row[] = {size, qt};
                model.addRow(row);
            }
        }
    }

    private void addToTablesPartial(MainEncomenda enc, PartialEncomenda penc) {
        this.addToPartialTable(enc, penc);
        this.addToPartialTablePDF(enc, penc);
    }

    private void addToTableHistoryPartialEncomendas(MainEncomenda enc) {
        DefaultTableModel model = (DefaultTableModel) this.tableHistoryPartial.getModel();
        model.setRowCount(0);

        for (PartialEncomenda penc : enc.getMapPartialEncomendas().values()) {
            Object row[] = {penc.getID(), penc.getDate(), penc.getBoxes()};
            model.addRow(row);
        }
    }

    public void clear() {
        this.tfComments.setText("");
        this.tfNote.setText("");
        this.tfName.setText("");
        this.tfSpec.setText("");
        this.taMain.setText("");
        this.taPartial.setText("");
        this.putImage(null);

        this.labelRest.setText("");
        this.tfNameFile.setText("");

        DefaultTableModel model = (DefaultTableModel) tablePartial.getModel();
        model.setRowCount(0);
        model = (DefaultTableModel) tableMain.getModel();
        model.setRowCount(0);
    }

    public void save() {

        if (pathFileEnc.isEmpty()) {
            JFileChooser fc = new JFileChooser();
            int r = fc.showDialog(null, "Guardar");
            if (r == JFileChooser.APPROVE_OPTION) {
                pathFileEnc = fc.getSelectedFile() + ".json";
            }
        }

        Thread t1 = new Thread(() -> {
            int minimum = progressBar.getMinimum();
            int maximum = progressBar.getMaximum();
            for (int i = minimum; i < maximum; i++) {
                try {
                    int value = progressBar.getValue();
                    progressBar.setValue(value + 1);
                    Thread.sleep(15);
                } catch (InterruptedException ignoredException) {
                }
            }
            progressBar.setValue(0);
        });
        t1.start();
        try (FileWriter fw = new FileWriter(pathFileEnc)) {
            this.encomenda.setCommande(tfNote.getText());
            this.encomenda.setName(tfName.getText());
            this.encomenda.setSpecs(tfSpec.getText());
            this.encomenda.setSizesAndQuantities(taMain.getText());
            this.encomenda.setComments(tfComments.getText());

            System.out.println(gson.toJson(this.encomenda));
            fw.write(gson.toJson(this.encomenda));
            this.tfNameFile.setText(this.encomenda.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ficheiro não foi salvo!");
        }
    }

    public void open(String filePath) {
        this.clear();
        try {
            this.pathFileEnc = filePath;
            this.myReader = new Scanner(new File(filePath));
            String data = "";
            while (this.myReader.hasNextLine()) {
                data = this.myReader.nextLine();
            }
            this.myReader.close();
            this.encomenda = gson.fromJson(data, MainEncomenda.class);
            this.tfNote.setText(this.encomenda.getCommande());
            this.tfSpec.setText(this.encomenda.getSpecs());
            this.tfName.setText(this.encomenda.getName());
            this.taMain.setText(this.encomenda.getSizesAndQuantities());
            this.tfComments.setText(this.encomenda.getComments());

            this.addToMainTable(this.encomenda);
            this.addToTableHistoryPartialEncomendas(this.encomenda);
            this.putImage(this.encomenda.getImage());
            this.tfNameFile.setText(this.encomenda.getName());
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Ficheiro " + filePath + " não existe!");
        }
    }

    public void newEncomenda() {
        this.encomenda = new MainEncomenda();
        this.clear();
        this.tfNameFile.setText("*Novo");
        this.addToTableHistoryPartialEncomendas(this.encomenda);
        this.addToMainTable(this.encomenda);
        this.newPartial();
    }

    public void newPartial() {
        this.partialEncomenda = new PartialEncomenda();
        this.taPartial.setText("");
        this.addToTablesPartial(this.encomenda, this.partialEncomenda);
        this.labelIdPartial.setText("*Nova encomenda");
    }

    public void putImage(String imgPath) {
        if (imgPath == null) {
            this.labelImage.setIcon(null);
        } else {
            ImageIcon img = new ImageIcon(imgPath);
            int a = this.labelImage.getHeight() - 10;
            int l = this.labelImage.getWidth() - 10;

            if (img.getIconHeight() > img.getIconWidth()) {
                l = (int) img.getIconWidth() * a / img.getIconHeight();
            } else if (img.getIconHeight() < img.getIconWidth()) {
                a = (int) img.getIconHeight() * l / img.getIconWidth();
            }
            img = new ImageIcon(img.getImage().getScaledInstance(l, a, Image.SCALE_DEFAULT));

            this.labelImage.setIcon(img);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        tfNote = new javax.swing.JTextField();
        tfName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableMain = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        taMain = new javax.swing.JTextArea();
        labelRest = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        tfSpec = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        tfComments = new javax.swing.JTextField();
        progressBar = new javax.swing.JProgressBar();
        tfNameFile = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        taPartial = new javax.swing.JTextArea();
        btHelp = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablePartial = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablePDF = new javax.swing.JTable();
        btPrintLabels = new javax.swing.JButton();
        btPackingList = new javax.swing.JButton();
        btPDF = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btClosePartial = new javax.swing.JButton();
        labelIdPartial = new javax.swing.JLabel();
        btRemoveImage = new javax.swing.JButton();
        btAddImage = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableHistoryPartial = new javax.swing.JTable();
        btNewPartial = new javax.swing.JButton();
        btDeletePartial = new javax.swing.JButton();
        labelImage = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        optSave = new javax.swing.JMenuItem();
        optOpen = new javax.swing.JMenuItem();
        optNew = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Nota da Encomenda:");

        jLabel2.setText("Nome da Sola:");

        tableMain.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tamanho", "Quantidade", "Restantes"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tableMain);
        if (tableMain.getColumnModel().getColumnCount() > 0) {
            tableMain.getColumnModel().getColumn(0).setResizable(false);
            tableMain.getColumnModel().getColumn(1).setResizable(false);
            tableMain.getColumnModel().getColumn(2).setResizable(false);
        }

        taMain.setColumns(20);
        taMain.setRows(5);
        taMain.setWrapStyleWord(true);
        taMain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                taMainKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(taMain);

        labelRest.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Pares Restantes:");

        jLabel3.setFont(new java.awt.Font("Segoe UI Symbol", 1, 24)); // NOI18N
        jLabel3.setText("H4W");

        jLabel7.setText("Especificações:");

        jLabel8.setText("Comentários:");

        tfNameFile.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Partial"));

        taPartial.setColumns(20);
        taPartial.setRows(5);
        taPartial.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                taPartialKeyTyped(evt);
            }
        });
        jScrollPane3.setViewportView(taPartial);

        btHelp.setText("?");
        btHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btHelpActionPerformed(evt);
            }
        });

        tablePartial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nº Caixa", "Tamanho", "Qt. Pares"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(tablePartial);
        if (tablePartial.getColumnModel().getColumnCount() > 0) {
            tablePartial.getColumnModel().getColumn(0).setResizable(false);
            tablePartial.getColumnModel().getColumn(1).setResizable(false);
            tablePartial.getColumnModel().getColumn(2).setResizable(false);
        }

        tablePDF.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Tamanho", "Qt. Pares"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tablePDF);
        if (tablePDF.getColumnModel().getColumnCount() > 0) {
            tablePDF.getColumnModel().getColumn(0).setResizable(false);
            tablePDF.getColumnModel().getColumn(1).setResizable(false);
        }

        btPrintLabels.setText("Imprimir Etiquetas");
        btPrintLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPrintLabelsActionPerformed(evt);
            }
        });

        btPackingList.setText("Packing List");
        btPackingList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPackingListActionPerformed(evt);
            }
        });

        btPDF.setText("PDF");
        btPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPDFActionPerformed(evt);
            }
        });

        btClosePartial.setText("Fechar Encomenda");
        btClosePartial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btClosePartialActionPerformed(evt);
            }
        });

        labelIdPartial.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
                    .addComponent(btClosePartial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btPrintLabels)
                        .addGap(18, 18, 18)
                        .addComponent(btPackingList, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btPDF, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addGap(12, 12, 12)
                        .addComponent(btHelp))
                    .addComponent(labelIdPartial, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelIdPartial, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btHelp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btPrintLabels)
                    .addComponent(btPackingList)
                    .addComponent(btPDF))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btClosePartial)
                .addContainerGap())
        );

        btRemoveImage.setText("Eliminar Imagem");
        btRemoveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRemoveImageActionPerformed(evt);
            }
        });

        btAddImage.setText("Adicionar Imagem");
        btAddImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddImageActionPerformed(evt);
            }
        });

        tableHistoryPartial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Data", "Informação"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableHistoryPartial.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableHistoryPartialMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tableHistoryPartial);
        if (tableHistoryPartial.getColumnModel().getColumnCount() > 0) {
            tableHistoryPartial.getColumnModel().getColumn(0).setResizable(false);
            tableHistoryPartial.getColumnModel().getColumn(1).setResizable(false);
            tableHistoryPartial.getColumnModel().getColumn(2).setResizable(false);
        }

        btNewPartial.setText("Novo");
        btNewPartial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btNewPartialActionPerformed(evt);
            }
        });

        btDeletePartial.setText("Eliminar");
        btDeletePartial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeletePartialActionPerformed(evt);
            }
        });

        labelImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jMenu1.setText("Ficheiro");

        optSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        optSave.setText("Guardar");
        optSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optSaveActionPerformed(evt);
            }
        });
        jMenu1.add(optSave);

        optOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        optOpen.setText("Abrir");
        optOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optOpenActionPerformed(evt);
            }
        });
        jMenu1.add(optOpen);

        optNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        optNew.setText("Novo");
        optNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optNewActionPerformed(evt);
            }
        });
        jMenu1.add(optNew);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tfNameFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(140, 140, 140))
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(8, 8, 8))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btAddImage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 207, Short.MAX_VALUE)
                                .addComponent(btRemoveImage))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tfName)
                                    .addComponent(tfSpec)
                                    .addComponent(tfComments)
                                    .addComponent(tfNote)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelRest, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2))
                            .addComponent(labelImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btNewPartial)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btDeletePartial, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tfNameFile, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(tfNote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(tfName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tfSpec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(tfComments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelRest, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6)))
                            .addComponent(jScrollPane2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelImage, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btRemoveImage)
                            .addComponent(btAddImage)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btNewPartial)
                            .addComponent(btDeletePartial))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void taMainKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taMainKeyTyped
        if ((evt.getKeyChar() == '\n' || evt.getKeyChar() == '/') || evt.getKeyChar() == ':') {
            this.taMain.setText(this.taMain.getText().replace("\n", "").replace(" ", ""));
            this.encomenda.setSizesAndQuantities(this.taMain.getText());
            this.addToMainTable(this.encomenda);
        }
    }//GEN-LAST:event_taMainKeyTyped

    private void taPartialKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taPartialKeyTyped
        if ((evt.getKeyChar() == '\n' || evt.getKeyChar() == '/') || evt.getKeyChar() == ':') {
            this.taPartial.setText(this.taPartial.getText().replace("\n", "").replace(" ", ""));
            String last_state = this.partialEncomenda.getBoxes();
            this.partialEncomenda.setBoxes(this.taPartial.getText());
            if (this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(this.partialEncomenda.getID()))) {
                this.encomenda.addPartialEncomenda(this.partialEncomenda);
                this.addToTablesPartial(this.encomenda, this.partialEncomenda);
            } else {
                this.partialEncomenda.setBoxes(last_state);
            }
        }
    }//GEN-LAST:event_taPartialKeyTyped

    private void optSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optSaveActionPerformed
        if (this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(null))) {
            this.save();
        }
    }//GEN-LAST:event_optSaveActionPerformed

    private void optOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optOpenActionPerformed
        JFileChooser fc = new JFileChooser();
        int r = fc.showDialog(null, "Abrir");
        if (r == JFileChooser.APPROVE_OPTION) {
            this.open(fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_optOpenActionPerformed

    private void btHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btHelpActionPerformed
        JOptionPane.showMessageDialog(this, "Deverá por o tamanho separando "
                + "por um hífen(-) a quantidade e\n separar os tamanhos pelo simbolo de "
                + "dois pontos(:) (tamanho-quantidade). \n Quando quiser separar caixas deve colocar "
                + "uma barra (/)  Exemplo: 37.4-4:38-6/39-10/37-4:38.5-3:39-3/ (...) "
                + "\n   Caixa 1:"
                + "\n       -> Tamannho 37.4 - 4 pares"
                + "\n       -> Tamannho 38 - 6 pares"
                + "\n   Caixa 2:"
                + "\n       -> Tamannho 39 - 10 pares"
                + "\n   Caixa 3:"
                + "\n       -> Tamannho 37 - 4 pares"
                + "\n       -> Tamannho 38.5 - 3 pares"
                + "\n       -> Tamannho 39 - 3 pares");
    }//GEN-LAST:event_btHelpActionPerformed

    private void optNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optNewActionPerformed
        if (this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(null))) {
            this.newEncomenda();
        }
    }//GEN-LAST:event_optNewActionPerformed

    private void btClosePartialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btClosePartialActionPerformed
        if (this.editingPartial) {
            this.partialEncomenda.setBoxes(this.taPartial.getText());
        } else {
            this.partialEncomenda = new PartialEncomenda(this.taPartial.getText());
        }

        if (this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(this.partialEncomenda.getID()))) {
            this.taPartial.setText("");
            this.addToTablesPartial(this.encomenda, this.partialEncomenda);

            this.encomenda.addPartialEncomenda(this.partialEncomenda);
            this.addToTableHistoryPartialEncomendas(this.encomenda);
            editingPartial = false;
            this.partialEncomenda.updateDate();
            this.save();
            this.open(this.pathFileEnc);
            this.newPartial();
        }
    }//GEN-LAST:event_btClosePartialActionPerformed

    private void btAddImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddImageActionPerformed
        JFileChooser fc = new JFileChooser();
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());

        fc.setFileFilter(imageFilter);

        int r = fc.showDialog(null, "Adicionar Imagem");

        if (r == JFileChooser.APPROVE_OPTION) {
            this.encomenda.setImage(fc.getSelectedFile().getAbsolutePath());
        }
        this.save();
        this.open(this.pathFileEnc);
    }//GEN-LAST:event_btAddImageActionPerformed

    private void btRemoveImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRemoveImageActionPerformed
        this.encomenda.setImage(null);
        this.save();
        this.open(this.pathFileEnc);
    }//GEN-LAST:event_btRemoveImageActionPerformed

    private void tableHistoryPartialMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableHistoryPartialMouseClicked
        if (evt.getClickCount() == 2) {
            try {
                String id = (String) this.tableHistoryPartial.getValueAt(this.tableHistoryPartial.getSelectedRow(), 0);
                if (!id.equals(this.partialEncomenda.getID())) {
                    this.partialEncomenda = this.encomenda.getPartialEncomenda(id);
                    if (this.partialEncomenda != null) {
                        this.editingPartial = true;
                        this.taPartial.setText(this.partialEncomenda.getBoxes());
                        this.addToTablesPartial(this.encomenda, this.partialEncomenda);
                        this.labelIdPartial.setText("A editar encomenda: " + this.partialEncomenda.getID());
                    }
                }
            } catch (Exception ex) {
            }
        }
    }//GEN-LAST:event_tableHistoryPartialMouseClicked

    private void btPrintLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPrintLabelsActionPerformed
        if (!this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(null))) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro!");
            return;
        }

        String path;
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int r = fc.showDialog(null, "Criar");

        if (r == JFileChooser.APPROVE_OPTION) {
            path = fc.getSelectedFile().getAbsolutePath() + "/" + this.encomenda.getName() + ".pdf";

            try {
                HashMap<Integer, HashMap<String, Integer>> mapBoxes = this.partialEncomenda.getMapBoxes(this.encomenda.getMapOfRest(null));

                BufferedImage bi = ImageIO.read(getClass().getResource("/org/me/labelapp/res/logoH4W.PNG"));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bi, "png", baos);
                byte[] byteLogo = baos.toByteArray();

                byte[] byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-Bold.ttf"));
                BaseFont bold = BaseFont.createFont("Oswald-Demibold.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-DemiBold.ttf"));
                BaseFont demibold = BaseFont.createFont("Oswald-Bold.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-Regular.ttf"));
                BaseFont normal = BaseFont.createFont("Oswald-Regular.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                int width = 288, height = 288;

                Document document = new Document(new Rectangle(width, height));
                document.setMargins(10, 10, 10, 10);
                PdfWriter.getInstance(document, new FileOutputStream(path));
                document.open();

                PdfPCell cell;

                for (Map.Entry<Integer, HashMap<String, Integer>> entry : mapBoxes.entrySet()) {
                    Integer idBox = entry.getKey();
                    HashMap<String, Integer> mapTam = entry.getValue();

                    document.newPage();
                    document.setMargins(10, 10, 10, 10);

                    PdfPTable table = new PdfPTable(6); // 2 columns.
                    table.setWidthPercentage(100);

                    Paragraph p;
                    com.itextpdf.text.Image img;

                    img = com.itextpdf.text.Image.getInstance(byteLogo);
                    img.scaleToFit(130, 50);
                    cell = new PdfPCell(img);
                    cell.setColspan(3);
                    cell.setFixedHeight(50);
                    cell.setBorderColor(BaseColor.WHITE);
                    table.addCell(cell);

                    p = new Paragraph(new Phrase(this.encomenda.getComments(), new Font(demibold, 11)));
                    cell = new PdfPCell(p);
                    cell.setColspan(3);
                    cell.setBorderColor(BaseColor.WHITE);
                    cell.setFixedHeight(20);
                    table.addCell(cell);

                    p = new Paragraph(new Phrase("Commande: ", new Font(demibold, 14)));
                    p.add(new Phrase(this.encomenda.getCommande(), new Font(normal, 12)));
                    cell = new PdfPCell(p);
                    cell.setColspan(6);
                    cell.setBorderColor(BaseColor.WHITE);
                    cell.setFixedHeight(20);
                    table.addCell(cell);

                    cell = new PdfPCell(new Paragraph(this.encomenda.getName().toUpperCase(), new Font(bold, 14)));
                    cell.setColspan(6);
                    cell.setBorderColor(BaseColor.WHITE);
                    table.addCell(cell);

                    cell = new PdfPCell(new Paragraph(this.encomenda.getSpecs(), new Font(normal, 12)));
                    cell.setColspan(6);
                    cell.setBorderColor(BaseColor.WHITE);
                    table.addCell(cell);

                    if (new File(this.encomenda.getImage()).exists()) {
                        img = com.itextpdf.text.Image.getInstance(this.encomenda.getImage());
                        img.scaleToFit(178, 90);
                        cell = new PdfPCell(img);
                    } else {
                        cell = new PdfPCell(new Phrase(""));
                    }
                    cell.setColspan(4);
                    cell.setFixedHeight(110);
                    cell.setPadding(10);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                    cell.setBorderColor(BaseColor.WHITE);
                    table.addCell(cell);

                    String tamStr = "";

                    for (Map.Entry<String, Integer> entry1 : mapTam.entrySet()) {
                        String tam = entry1.getKey();
                        Integer qt = entry1.getValue();

                        if (mapTam.entrySet().size() == 1) {
                            tamStr += tam + "\n\n" + Integer.toString(qt) + "P\n\n";
                            p = new Paragraph(new Phrase(tamStr, new Font(bold, 24)));
                        } else if (mapTam.entrySet().size() > 1) {
                            tamStr += tam + "-" + Integer.toString(qt) + "P\n";
                            p = new Paragraph(new Phrase(tamStr, new Font(bold, 20)));
                        }
                    }

                    cell = new PdfPCell(p);
                    cell.setColspan(2);
                    cell.setFixedHeight(40);
                    cell.setBorderColor(BaseColor.WHITE);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(cell);

                    String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    cell = new PdfPCell(new Phrase(hoje, new Font(demibold, 12)));
                    cell.setBorderColor(BaseColor.WHITE);
                    cell.setColspan(2);
                    cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(cell);

                    cell = new PdfPCell(new Phrase("\n" + Integer.toString(idBox) + "/" + Integer.toString(mapBoxes.size()), new Font(bold, 25)));
                    cell.setBorderColor(BaseColor.WHITE);
                    cell.setColspan(4);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setVerticalAlignment(Element.ALIGN_TOP);
                    table.addCell(cell);

                    document.add(table);
                }
                document.close();
                Desktop.getDesktop().open(new File(path));
            } catch (IOException | DocumentException ex) {
                JOptionPane.showMessageDialog(this, ex);
            }
        }
    }//GEN-LAST:event_btPrintLabelsActionPerformed

    private void btPackingListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPackingListActionPerformed
        if (!this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(null))) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro!");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int r = fc.showDialog(null, "Criar");

        if (r == JFileChooser.APPROVE_OPTION) {
            try {
                String file = fc.getSelectedFile().getAbsolutePath() + "/" + this.encomenda.getName() + "_packing_list.pdf";
                Document document = new Document();
                document.setPageSize(PageSize.A4.rotate());
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                byte[] byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-Bold.ttf"));
                BaseFont bold = BaseFont.createFont("Oswald-Demibold.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-DemiBold.ttf"));
                BaseFont demibold = BaseFont.createFont("Oswald-Bold.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-Regular.ttf"));
                BaseFont normal = BaseFont.createFont("Oswald-Regular.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                TreeMap<String, Integer> mapSizes = this.encomenda.getMapSizesQuantities();
                int number_collumns = mapSizes.size() * 2;

                int total = 0;
                for (Map.Entry<String, Integer> entry : mapSizes.entrySet()) {
                    total += entry.getValue();
                }

                PdfPTable table = new PdfPTable(number_collumns); // 2 columns.
                table.setWidthPercentage(100);

                PdfPCell cell;

                Paragraph p = new Paragraph("Packet List " + this.encomenda.getName().toUpperCase() + " " + this.encomenda.getCommande() + " - " + total + " Pares", new Font(demibold, 14));
                cell = new PdfPCell(p);
                cell.setColspan(number_collumns);
                cell.setFixedHeight(30);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                p = new Paragraph("SOLA " + this.encomenda.getName().toUpperCase() + " - " + this.encomenda.getSpecs(), new Font(normal, 12));
                cell = new PdfPCell(p);
                cell.setColspan(number_collumns / 2);
                cell.setFixedHeight(20);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                p = new Paragraph(total + " Pares", new Font(normal, 12));
                cell = new PdfPCell(p);
                cell.setColspan(number_collumns / 2);
                cell.setFixedHeight(20);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setVerticalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);

                for (String tam : mapSizes.keySet()) {
                    p = new Paragraph(tam, new Font(demibold, 14));
                    cell = new PdfPCell(p);
                    cell.setColspan(2);
                    cell.setFixedHeight(20);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }

                for (String tam : mapSizes.keySet()) {
                    p = new Paragraph("Box", new Font(demibold, 12));
                    cell = new PdfPCell(p);
                    cell.setFixedHeight(20);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);

                    p = new Paragraph("Quantity", new Font(demibold, 12));
                    cell = new PdfPCell(p);
                    cell.setFixedHeight(20);
                    cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }

                int max = 0;
                TreeMap<String, TreeMap<Integer, Integer>> mapSizesBoxes = this.partialEncomenda.getMapSizes();
                for (Map.Entry<String, TreeMap<Integer, Integer>> entry : mapSizesBoxes.entrySet()) {
                    if (max < entry.getValue().size()) {
                        max = entry.getValue().size();
                    }
                }

                boolean isGray = false;

                for (int i = 0; i < max; i++) {
                    for (Map.Entry<String, TreeMap<Integer, Integer>> entry : mapSizesBoxes.entrySet()) {
                        String c = "";
                        String q = "";

                        try {
                            c = Integer.toString(new ArrayList<>(entry.getValue().keySet()).get(i));
                            q = Integer.toString(new ArrayList<>(entry.getValue().values()).get(i));
                        } catch (Exception e) {
                        }

                        p = new Paragraph(c, new Font(normal, 12));
                        cell = new PdfPCell(p);
                        cell.setFixedHeight(20);
                        if (isGray) {
                            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);

                        p = new Paragraph(q, new Font(normal, 12));
                        cell = new PdfPCell(p);
                        cell.setFixedHeight(20);
                        if (isGray) {
                            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        }
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cell);
                    }
                    isGray = !isGray;
                }

                for (Map.Entry<String, TreeMap<Integer, Integer>> entry : mapSizesBoxes.entrySet()) {
                    int qtTotal = 0;

                    for (Integer qt : entry.getValue().values()) {
                        qtTotal += qt;
                    }

                    p = new Paragraph(Integer.toString(qtTotal), new Font(normal, 12));
                    cell = new PdfPCell(p);
                    cell.setFixedHeight(20);
                    cell.setColspan(2);
                    if (isGray) {
                        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    }
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }

                document.add(table);

                String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                p = new Paragraph("\n" + hoje, new Font(demibold, 12));
                document.add(p);

                document.close();

                Desktop.getDesktop().open(new File(file));

            } catch (DocumentException | FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex);
            }
        }
    }//GEN-LAST:event_btPackingListActionPerformed

    private void btPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPDFActionPerformed
        if (!this.partialEncomenda.isDataOk(this.encomenda.getMapOfRest(null))) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro!");
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int r = fc.showDialog(null, "Criar");

        if (r == JFileChooser.APPROVE_OPTION) {
            try {
                String file = fc.getSelectedFile().getAbsolutePath() + "/" + this.encomenda.getName() + ".pdf";
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                byte[] byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-DemiBold.ttf"));
                BaseFont demibold = BaseFont.createFont("Oswald-Bold.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                byteFont = IOUtils.toByteArray(getClass().getResourceAsStream("/org/me/labelapp/res/Oswald-Regular.ttf"));
                BaseFont normal = BaseFont.createFont("Oswald-Regular.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED, false, byteFont, null);

                Paragraph p = new Paragraph(new Phrase("Commande: ", new Font(demibold, 14)));
                p.add(new Phrase(this.encomenda.getCommande(), new Font(normal, 12)));
                document.add(p);

                p = new Paragraph(new Phrase("Nome: ", new Font(demibold, 14)));
                p.add(new Phrase(this.encomenda.getName(), new Font(normal, 12)));
                document.add(p);

                p = new Paragraph(new Phrase("Especificações: ", new Font(demibold, 14)));
                p.add(new Phrase(this.encomenda.getSpecs() + "\n\n", new Font(normal, 12)));
                document.add(p);

                PdfPTable table = new PdfPTable(2); // 2 columns.
                table.setWidthPercentage(30);

                PdfPCell cell;

                p = new Paragraph("Tamanho", new Font(demibold, 14));
                cell = new PdfPCell(p);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                table.addCell(cell);

                p = new Paragraph("Quantidade", new Font(demibold, 14));
                cell = new PdfPCell(p);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                table.addCell(cell);

                TreeMap<String, Integer> mapSizes = this.encomenda.getMapSizesQuantities();
                for (Map.Entry<String, Integer> entry : mapSizes.entrySet()) {
                    String tam = entry.getKey();
                    Integer qt = entry.getValue();

                    p = new Paragraph(tam, new Font(normal, 12));
                    cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(cell);

                    p = new Paragraph(Integer.toString(qt), new Font(normal, 12));
                    cell = new PdfPCell(p);
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                    table.addCell(cell);

                }

                document.add(table);

                int total = 0;
                for (Map.Entry<String, Integer> entry : mapSizes.entrySet()) {
                    total += entry.getValue();
                }
                p = new Paragraph(new Phrase("Quantidade Total: ", new Font(demibold, 14)));
                p.add(new Phrase(total + "\n\n", new Font(normal, 12)));
                document.add(p);

                String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                p = new Paragraph("\n\n" + hoje, new Font(demibold, 12));
                document.add(p);

                document.close();

                Desktop.getDesktop().open(new File(file));

            } catch (DocumentException | FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, ex);
            }
        }
    }//GEN-LAST:event_btPDFActionPerformed

    private void btNewPartialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btNewPartialActionPerformed
        this.newPartial();
    }//GEN-LAST:event_btNewPartialActionPerformed

    private void btDeletePartialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeletePartialActionPerformed
        if (this.encomenda.deletePartialEncomenda(this.partialEncomenda.getID()) != null) {
            this.addToMainTable(this.encomenda);
            this.addToTableHistoryPartialEncomendas(this.encomenda);
            this.newPartial();
            this.save();
            this.open(this.pathFileEnc);
        }
    }//GEN-LAST:event_btDeletePartialActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddImage;
    private javax.swing.JButton btClosePartial;
    private javax.swing.JButton btDeletePartial;
    private javax.swing.JButton btHelp;
    private javax.swing.JButton btNewPartial;
    private javax.swing.JButton btPDF;
    private javax.swing.JButton btPackingList;
    private javax.swing.JButton btPrintLabels;
    private javax.swing.JButton btRemoveImage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelIdPartial;
    private javax.swing.JLabel labelImage;
    private javax.swing.JLabel labelRest;
    private javax.swing.JMenuItem optNew;
    private javax.swing.JMenuItem optOpen;
    private javax.swing.JMenuItem optSave;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextArea taMain;
    private javax.swing.JTextArea taPartial;
    private javax.swing.JTable tableHistoryPartial;
    private javax.swing.JTable tableMain;
    private javax.swing.JTable tablePDF;
    private javax.swing.JTable tablePartial;
    private javax.swing.JTextField tfComments;
    private javax.swing.JTextField tfName;
    private javax.swing.JLabel tfNameFile;
    private javax.swing.JTextField tfNote;
    private javax.swing.JTextField tfSpec;
    // End of variables declaration//GEN-END:variables
}

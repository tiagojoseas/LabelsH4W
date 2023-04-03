package labelsh4w;

import com.google.gson.Gson;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author tiago
 */
public class MainFrame extends javax.swing.JFrame {

    /**
     * Creates new form MainFrame
     */
    private MainEncomenda encomenda;
    private PartialEncomenda partialEncomenda;
    private String pathFileEnc;
    private final Gson gson = new Gson();
    private Scanner myReader;

    public MainFrame() {

        /*KeyStroke ctrl_n = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        this.optOpen.setAccelerator(ctrl_n);

        KeyStroke ctrl_s = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        this.optSave.setAccelerator(ctrl_s);*/

        initComponents();
        this.encomenda = new MainEncomenda();
        this.partialEncomenda = new PartialEncomenda();
        this.pathFileEnc = "";
    }

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
            Object row[] = {size, value, enc.getRestOfSize(size)};
            model.addRow(row);
        }
        // Sort Sizes and Quantites String
        this.encomenda.setSizesAndQuantities(sortedSizesQuantities);
        this.taMain.setText(sortedSizesQuantities);

        this.labelRest.setText(enc.getRest() + " de " + this.encomenda.getTotalQuantities());
    }

    private void addToPartialTable(PartialEncomenda penc) {
        DefaultTableModel model = (DefaultTableModel) this.tablePartial.getModel();
        model.setRowCount(0);

        for (Map.Entry<Integer, HashMap<String, Integer>> box : penc.getMapBoxes(this.encomenda.getMapOfRest()).entrySet()) {
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

    public void clear() {
        this.tfComments.setText("");
        this.tfNote.setText("");
        this.tfName.setText("");
        this.tfSpec.setText("");
        this.taMain.setText("");
        this.taPartial.setText("");
        //this.taMainimgPath = "";
        //putImage(null);

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
                pathFileEnc = fc.getSelectedFile() + ".txt";

                this.progressBar.setIndeterminate(true);
            }
        }

        this.progresssBarOn();
        try (FileWriter fw = new FileWriter(pathFileEnc)) {
            this.encomenda.setCommande(tfNote.getText());
            this.encomenda.setName(tfName.getText());
            this.encomenda.setSpecs(tfSpec.getText());
            this.encomenda.setSizesAndQuantities(taMain.getText());
            this.encomenda.setComments(tfComments.getText());
            //this.encomenda.setImagem(imgPath);

            System.out.println(gson.toJson(this.encomenda));
            fw.write(gson.toJson(this.encomenda));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex);
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
            //this.imgPath = enc.getImagem();

            this.addToMainTable(this.encomenda);
            //this.addToTablePartialEncomendas(this.encomenda);
            //this.putImage(imgPath);

            this.tfNameFile.setText(new File(filePath).getName());

            //this.history.add(filePath);
            //this.updateHistory();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Ficheiro " + filePath + " não existe!");
        }
    }
    
    public void newEncomenda() {
        this.encomenda= new MainEncomenda();
        this.clear();
        this.tfNameFile.setName("*Novo");
        
        this.addToMainTable(this.encomenda);
        //this.addToTablePartialEncomendas(this.encomenda);
    }

    public void novoPartial(){
        this.partialEncomenda = new PartialEncomenda();
        this.taPartial.setText("");
        //addToTables(pEnc);
        //idPartialEncomenda.setText("Nova encomenda");
    }

    public void progresssBarOn() {
        progressBar.setIndeterminate(true);
        Timer timer = new Timer();
        timer.schedule(new RemindTask(), 3 * 1000);

    }

    class RemindTask extends TimerTask {

        @Override
        public void run() {
            progressBar.setIndeterminate(false);
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
        jPanel1 = new javax.swing.JPanel();
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
        jTable1 = new javax.swing.JTable();
        btPrintLabels = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
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
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 251, Short.MAX_VALUE)
        );

        jLabel3.setFont(new java.awt.Font("Segoe UI Symbol", 1, 24)); // NOI18N
        jLabel3.setText("H4W");

        jLabel7.setText("Especificações:");

        jLabel8.setText("Comentários:");

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
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
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

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
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
        jScrollPane4.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
        }

        btPrintLabels.setText("Imprimir Etiquetas");

        jButton1.setText("Packing List");

        jButton2.setText("PDF");

        jButton3.setText("Fechar Encomenda");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btHelp))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE))
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btPrintLabels)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btHelp)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btPrintLabels)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap())
        );

        jButton4.setText("jButton4");

        jButton5.setText("jButton5");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Data", "Outros"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(jTable2);
        if (jTable2.getColumnModel().getColumnCount() > 0) {
            jTable2.getColumnModel().getColumn(0).setResizable(false);
            jTable2.getColumnModel().getColumn(1).setResizable(false);
            jTable2.getColumnModel().getColumn(2).setResizable(false);
        }

        jButton6.setText("jButton6");

        jButton7.setText("jButton7");

        jMenu1.setText("File");

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
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton4))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(tfName)
                                            .addComponent(tfNote, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(tfSpec)
                                            .addComponent(tfComments)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(labelRest, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane6)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jButton6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jButton7)))
                                        .addGap(2, 2, 2))))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(tfNameFile, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(tfNameFile, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelRest, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6)))
                            .addComponent(jScrollPane2))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton4)
                            .addComponent(jButton5)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton6)
                            .addComponent(jButton7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            this.partialEncomenda.setBoxes(this.taPartial.getText());
            this.addToPartialTable(this.partialEncomenda);
        }
    }//GEN-LAST:event_taPartialKeyTyped

    private void optSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optSaveActionPerformed
        this.save();
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
        this.newEncomenda();        
    }//GEN-LAST:event_optNewActionPerformed

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
                if ("FlatLaf Dark".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btHelp;
    private javax.swing.JButton btPrintLabels;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JLabel labelRest;
    private javax.swing.JMenuItem optNew;
    private javax.swing.JMenuItem optOpen;
    private javax.swing.JMenuItem optSave;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextArea taMain;
    private javax.swing.JTextArea taPartial;
    private javax.swing.JTable tableMain;
    private javax.swing.JTable tablePartial;
    private javax.swing.JTextField tfComments;
    private javax.swing.JTextField tfName;
    private javax.swing.JLabel tfNameFile;
    private javax.swing.JTextField tfNote;
    private javax.swing.JTextField tfSpec;
    // End of variables declaration//GEN-END:variables
}

import java.sql.*;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;


public class Main extends javax.swing.JFrame {
    
    Connection con;
    Statement stmt;
    Statement stmtUpdate;
    ResultSet rsAdmin;
    ResultSet rsStaff;
    ResultSet rsHoliday;
    ResultSet rsDelay;
    ResultSet rsAbsence;
    ResultSet rsDate;
    
    String currentAdmin;

    DateFormat dateFormat = new SimpleDateFormat("d-M-yyyy",new Locale("en"));
    Date date = new Date();
    String today = dateFormat.format(date);
 
   
 
    DefaultTableModel model;
    
    Boolean isFinish = false;
    
    String lastCheck ="1-1-2020";
    int lastSelect=-1;
    
    public Main() {
        initComponents();
        DBConnection();
        
        /*ImageIcon icon = new ImageIcon("src\\img\\logo.png");
        this.setIconImage(icon.getImage());*/
        
        Image icon = Toolkit.getDefaultToolkit().getImage(("src\\img\\logo2.png"));
        this.setIconImage(icon);
        
        addImageToLoginPanel();
        ToolTip();
        
        tableStyel(table,false);
        tableStyel(table2,false);
        tableStyel(holiday_tabel_1,true);
        tableStyel(abcsence_tabel_1,false);
        tableStyel(lateness_tabel_1,false);
        tableStyel(holiday_tabel_3,true);
        tableStyel(abcsence_tabel_3,false);
        tableStyel(lateness_tabel_3,false);
        
        
        ((JLabel)jComboBox1.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        ((JLabel)jComboBox2.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        ((JLabel)jComboBox3.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        ((JLabel)jComboBox4.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        
        jComboBox5.setRenderer(new MyComboBoxRenderer("اضافة"));
        jComboBox5.setSelectedIndex(-1); 
        ((JLabel)jComboBox5.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        ((JLabel)jComboBox6.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        ((JLabel)jComboBox7.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
        
        jTextField7.setText("");
        jTextField11.setText("");
        
        checkHoliDays();
        reFreshTable();
        
        
        setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }
    
    public void checkHoliDays(){
        
        String DBQ ="SELECT * FROM M7MAD.LASTCHECK";
        try {
            
            rsDate = stmt.executeQuery(DBQ);
            rsDate.absolute(1);
            lastCheck = rsDate.getString("DATE");
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex);
        }
        
        
        if(!lastCheck.equals(today)){
            
           try {
            DBQ = "SELECT * FROM M7MAD.HOLIDAY WHERE CHECKED = false";
            rsHoliday = stmtUpdate.executeQuery(DBQ);
            rsHoliday.absolute(0);
           while (rsHoliday.next()) {
                String endingDay = rsHoliday.getString("ENDING");
                String ID = rsHoliday.getString("ID");
                
                 
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                LocalDate dateBefore = LocalDate.parse(today, formatter);
                LocalDate dateAfter = LocalDate.parse(endingDay, formatter);
                long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
               
                if(noOfDaysBetween<0){
                   
                    DBQ="INSERT INTO M7MAD.ABSENCE VALUES ('"+ID+"','"+today+"','عدم الالتحاق بالعمل بعد انتهاء العطلة','The Automatic System')";
                    stmt.executeUpdate(DBQ);
                  
                }
                
            }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
            
            DBQ ="UPDATE M7MAD.LASTCHECK SET DATE = '"+today+"'";
            try {
                stmt.executeUpdate(DBQ);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
    }
    
    public void printExcel(JTable printTable){
        
        model = (DefaultTableModel) printTable.getModel();
        
        int columns = model.getColumnCount();
        int rows = model.getRowCount();
        
        ArrayList<String> id = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> rank = new ArrayList<>();
        ArrayList<String> interest = new ArrayList<>();
        ArrayList<String> status = new ArrayList<>();
        ArrayList<String> Bplace = new ArrayList<>();
        ArrayList<String> Bdate = new ArrayList<>();
        ArrayList<String> phone = new ArrayList<>();
        
        for(int i=0;i<rows;i++){
            id.add((String) printTable.getValueAt(i, 7));
        }
        for(int i=0;i<rows;i++){
            name.add((String) printTable.getValueAt(i, 6));
        }
        for(int i=0;i<rows;i++){
            rank.add((String) printTable.getValueAt(i, 5));
        }
        for(int i=0;i<rows;i++){
            interest.add((String) printTable.getValueAt(i, 4));
        }
        for(int i=0;i<rows;i++){
            status.add((String) printTable.getValueAt(i, 3));
        }
        for(int i=0;i<rows;i++){
            Bplace.add((String) printTable.getValueAt(i, 2));
        }
        for(int i=0;i<rows;i++){
            Bdate.add((String) printTable.getValueAt(i, 1));
        }
        for(int i=0;i<rows;i++){
            phone.add((String) printTable.getValueAt(i, 0));
        }
        
        
         /// create workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        
        
        /// Create Cell Style
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        
        /// create Spread sheet
        XSSFSheet sheet = workbook.createSheet("Result");
        
        
        /// right 2 left sheet
        sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);
        
        /// craete a raw object
        XSSFRow row;
        
        
        /// create cell & set values
        row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        Cell cell3 = row.createCell(3);
        Cell cell4 = row.createCell(4);
        Cell cell5 = row.createCell(5);
        Cell cell6 = row.createCell(6);
        Cell cell7 = row.createCell(7);
        
        cell0.setCellStyle(style);
        cell1.setCellStyle(style);
        cell2.setCellStyle(style);
        cell3.setCellStyle(style);
        cell4.setCellStyle(style);
        cell5.setCellStyle(style);
        cell6.setCellStyle(style);
        cell7.setCellStyle(style);
        
        cell0.setCellValue("رمز الموظف");
        cell1.setCellValue("اسم الموظف");
        cell2.setCellValue("الرتبة");
        cell3.setCellValue("المصلحة");
        cell4.setCellValue("الوضعية");
        cell5.setCellValue("مكان الازدياد");
        cell6.setCellValue("تاريخ الازدياد");
        cell7.setCellValue("رقم الهاتف");
        
        
        // creat cell & raw for the data
        
        for(int i=0;i<rows;i++){
            
            row = sheet.createRow(i+1);
            
            for(int j=0;j<columns;j++){
                
                Cell cell = row.createCell(j);
                cell.setCellStyle(style);
                
                if(cell.getColumnIndex()==0){
                    cell.setCellValue(id.get(i));
                }
                else if(cell.getColumnIndex()==1){
                    cell.setCellValue(name.get(i));
                }
                else if(cell.getColumnIndex()==2){
                    cell.setCellValue(rank.get(i));
                }
                else if(cell.getColumnIndex()==3){
                    cell.setCellValue(interest.get(i));
                }
                else if(cell.getColumnIndex()==4){
                    cell.setCellValue(status.get(i));
                }
                else if(cell.getColumnIndex()==5){
                    cell.setCellValue(Bplace.get(i));
                }
                else if(cell.getColumnIndex()==6){
                    cell.setCellValue(Bdate.get(i));
                }
                else if(cell.getColumnIndex()==7){
                    cell.setCellValue(phone.get(i));
                }
            }
        }
        
        /// Auto resize columns
        for(int i=0; i<10; i++){
            sheet.autoSizeColumn(i);
        }
        
        /// write the created Excel file
        try{
            
            FileOutputStream out = new FileOutputStream(new File("Result.xlsx"));
            workbook.write(out);
            out.close();
             
            
        }
        catch(FileNotFoundException e){
            System.out.println(e);
        }
        catch(IOException e){
            System.out.println(e);
        }
        
        /// run Excel file
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(new File("Result.xlsx"));
        } catch (IOException e) {
            System.out.println(e);
        }
        
    }
    
    public void toDetailPanel(){
            
        boolean isRowSelected = table.isColumnSelected(0)|table.isColumnSelected(1)|table.isColumnSelected(2)|table.isColumnSelected(3)|table.isColumnSelected(4)|table.isColumnSelected(5)|table.isColumnSelected(6)|table.isColumnSelected(7);
        
        if(isRowSelected){
            int selectedRow = table.getSelectedRow();
            String idStaffSelected = (String) table.getValueAt(selectedRow, 7);
            
            try{
                
                String DBQ ="SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+idStaffSelected+"'";
                rsStaff = stmt.executeQuery(DBQ);
                
                rsStaff.absolute(0);
                rsStaff.next();
                
                jTextField1.setText(rsStaff.getString("ID"));
                jTextField2.setText(rsStaff.getString("NAME"));
                jTextField3.setText(rsStaff.getString("NICKNAME"));
                jTextField5.setText(rsStaff.getString("BIRTHPLACE"));
                jTextField6.setText(rsStaff.getString("BIRTHDAY"));
                jTextField8.setText(rsStaff.getString("PHONENUMBER"));
                jTextField30.setText(rsStaff.getString("RANK"));
                
                jComboBox4.setSelectedItem(rsStaff.getString("INTEREST"));
                jComboBox3.setSelectedItem(rsStaff.getString("STATUS"));
                
                jLabel40.setText(rsStaff.getString("ID"));
                jLabel39.setText(rsStaff.getString("PICTUER"));
                
                try {  
                    BufferedImage image = ImageIO.read(new URL("file:\\"+rsStaff.getString("PICTUER"))); 
                    if (image != null) {  
                        
                        ImageIcon i = new ImageIcon(rsStaff.getString("PICTUER"));
                        Image im = i.getImage();
                        Image fi = im.getScaledInstance(150,170, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(fi);
                        scaledIcon = new ImageIcon(fi);
                        staffPicLabel.setIcon(scaledIcon);
                        staffPicLabel3.setIcon(scaledIcon);
                        
                    }else{
                        
                        ImageIcon i = new ImageIcon("src\\img\\000.png");
                        Image im = i.getImage();
                        Image fi = im.getScaledInstance(150,170, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(fi);
                        scaledIcon = new ImageIcon(fi);
                        staffPicLabel.setIcon(scaledIcon);
                        staffPicLabel3.setIcon(scaledIcon);
                    }
                }catch(Exception e){
                        ImageIcon i = new ImageIcon("src\\img\\000.png");
                        Image im = i.getImage();
                        Image fi = im.getScaledInstance(150,170, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(fi);
                        scaledIcon = new ImageIcon(fi);
                        staffPicLabel.setIcon(scaledIcon);
                        staffPicLabel3.setIcon(scaledIcon);
                }
                
                reFreshStaffDetails(rsStaff.getString("ID"));
                
                
            }catch(HeadlessException | SQLException e){
                JOptionPane.showMessageDialog(null,e);
        }
            admin_1DetailPanel.setVisible(true);
            admin_1TabelPanel.setVisible(false);
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
        
    }
    
    
    public void toUserDetailPanel(){
            
        boolean isRowSelected = table2.isColumnSelected(0)|table2.isColumnSelected(1)|table2.isColumnSelected(2)|table2.isColumnSelected(3)|table2.isColumnSelected(4)|table2.isColumnSelected(5)|table2.isColumnSelected(6)|table2.isColumnSelected(7);
        
        if(isRowSelected){
            int selectedRow = table2.getSelectedRow();
            String idStaffSelected = (String) table2.getValueAt(selectedRow, 7);
            
            try{
                
                String DBQ ="SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+idStaffSelected+"'";
                rsStaff = stmt.executeQuery(DBQ);
                
                rsStaff.absolute(0);
                rsStaff.next();
                
                jTextField19.setText(rsStaff.getString("ID"));
                jTextField20.setText(rsStaff.getString("NAME"));
                jTextField31.setText(rsStaff.getString("NICKNAME"));
                jTextField22.setText(rsStaff.getString("BIRTHPLACE"));
                jTextField23.setText(rsStaff.getString("BIRTHDAY"));
                jTextField24.setText(rsStaff.getString("PHONENUMBER"));
                jTextField21.setText(rsStaff.getString("RANK"));
                
                jTextField26.setText(rsStaff.getString("INTEREST"));
                jTextField25.setText(rsStaff.getString("STATUS"));
                
                try {  
                    BufferedImage image = ImageIO.read(new URL("file:\\"+rsStaff.getString("PICTUER"))); 
                    if (image != null) {  
                        
                        ImageIcon i = new ImageIcon(rsStaff.getString("PICTUER"));
                        Image im = i.getImage();
                        Image fi = im.getScaledInstance(150,170, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(fi);
                        scaledIcon = new ImageIcon(fi);
                        staffPicLabel.setIcon(scaledIcon);
                        staffPicLabel3.setIcon(scaledIcon);
                        
                    }else{
                        
                        ImageIcon i = new ImageIcon("src\\img\\000.png");
                        Image im = i.getImage();
                        Image fi = im.getScaledInstance(150,170, Image.SCALE_SMOOTH);
                        ImageIcon scaledIcon = new ImageIcon(fi);
                        scaledIcon = new ImageIcon(fi);
                        staffPicLabel.setIcon(scaledIcon);
                        staffPicLabel3.setIcon(scaledIcon);
                    }
                }catch(Exception e){
                    ImageIcon i = new ImageIcon("src\\img\\000.png");
                    Image im = i.getImage();
                    Image fi = im.getScaledInstance(150,170, Image.SCALE_SMOOTH);
                    ImageIcon scaledIcon = new ImageIcon(fi);
                    scaledIcon = new ImageIcon(fi);
                    staffPicLabel.setIcon(scaledIcon);
                    staffPicLabel3.setIcon(scaledIcon);
                }
                
                reFreshStaffDetails(rsStaff.getString("ID"));
                
                
            }catch(HeadlessException | SQLException e){
                JOptionPane.showMessageDialog(null,e);
        }
            userTabelPanel.setVisible(false);
            userDetailPanel.setVisible(true);
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
        
    }
  
    
    public Boolean checkValidDate(String str){
        SimpleDateFormat sdfrmt = new SimpleDateFormat("d-M-yyyy");
	sdfrmt.setLenient(false);
        
        try {
            Date d = sdfrmt.parse(str);
            return true;
            
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid date \"d-m-yyyy\" ");
            return false;
        }
    }
    
    
    public void reFreshStaffDetails(String staffID){
        
        model = (DefaultTableModel) holiday_tabel_1.getModel();
        model.setRowCount(0);
        
        
        model = (DefaultTableModel) holiday_tabel_3.getModel();
        model.setRowCount(0);
        
        model = (DefaultTableModel) lateness_tabel_1.getModel();
        model.setRowCount(0);
        
        
        model = (DefaultTableModel) lateness_tabel_3.getModel();
        model.setRowCount(0);
        
        model = (DefaultTableModel) abcsence_tabel_1.getModel();
        model.setRowCount(0);
        
        
        model = (DefaultTableModel) abcsence_tabel_3.getModel();
        model.setRowCount(0);
        
        try{
            
            String DBQ ="SELECT * FROM M7MAD.HOLIDAY WHERE ID='"+staffID+"'";
            rsHoliday = stmt.executeQuery(DBQ);
            rsHoliday.absolute(0);
            while(rsHoliday.next()){
                String DB_TYPE = rsHoliday.getString("TYPE");
                String DB_BEGINING = rsHoliday.getString("BEGINNING");
                String DB_ENDING = rsHoliday.getString("ENDING");
                Boolean DB_CHECKED = rsHoliday.getBoolean("CHECKED");
                String byAdmin = rsHoliday.getString("DETAILS");
                
                model = (DefaultTableModel) holiday_tabel_1.getModel();
                model.addRow(new Object[]{DB_CHECKED,DB_ENDING,DB_BEGINING,DB_TYPE,byAdmin});
                
                
                model = (DefaultTableModel) holiday_tabel_3.getModel();
                model.addRow(new Object[]{DB_CHECKED,DB_ENDING,DB_BEGINING,DB_TYPE,byAdmin});
                
            }
            
            DBQ ="SELECT * FROM M7MAD.DELAY WHERE ID='"+staffID+"'";
            rsDelay = stmt.executeQuery(DBQ);
            rsDelay.absolute(0);
            while(rsDelay.next()){
                String DB_DATE = rsDelay.getString("DATE");
                String DB_TIME = rsDelay.getString("DELAYTIME");
                String DB_NOTE = rsDelay.getString("NOTE");
                String byAdmin = rsDelay.getString("DETAILS");
                
                model = (DefaultTableModel) lateness_tabel_1.getModel();
                model.addRow(new Object[]{DB_NOTE,DB_DATE,DB_TIME,byAdmin});
                
                
                model = (DefaultTableModel) lateness_tabel_3.getModel();
                model.addRow(new Object[]{DB_NOTE,DB_DATE,DB_TIME,byAdmin});
                
            }
            
            DBQ ="SELECT * FROM M7MAD.ABSENCE WHERE ID='"+staffID+"'";
            rsAbsence = stmt.executeQuery(DBQ);
            rsAbsence.absolute(0);
            while(rsAbsence.next()){
                String DB_DATE = rsAbsence.getString("DATE");
                String DB_NOTE = rsAbsence.getString("NOTE");
                String byAdmin = rsAbsence.getString("DETAILS");
                
                model = (DefaultTableModel) abcsence_tabel_1.getModel();
                model.addRow(new Object[]{DB_NOTE,DB_DATE,byAdmin});
                
                
                model = (DefaultTableModel) abcsence_tabel_3.getModel();
                model.addRow(new Object[]{DB_NOTE,DB_DATE,byAdmin});
                
            }
            
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }
        
    }
    
    
    public void reFreshTable(){
        
        model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        
        model = (DefaultTableModel) table2.getModel();
        model.setRowCount(0);
        
        if(jComboBox6.getSelectedIndex()==0 || jComboBox7.getSelectedIndex()==0){
            
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            try{
            
            String DBQ ="SELECT * FROM M7MAD.THESTAFFS";
            rsStaff = stmt.executeQuery(DBQ);
            
            rsStaff.absolute(0);
            while(rsStaff.next()) {                
                String DB_ID = rsStaff.getString("ID");
                String DB_NAME = rsStaff.getString("NAME");
                String DB_RANK = rsStaff.getString("RANK");
                String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                String DB_INTEREST = rsStaff.getString("INTEREST");
                String DB_STATUS = rsStaff.getString("STATUS");
                String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                String DB_NICKNAME = rsStaff.getString("NICKNAME");
                String fullName = DB_NAME+" "+DB_NICKNAME;
                
                    
                model = (DefaultTableModel) table.getModel();
                model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                
                model = (DefaultTableModel) table2.getModel();
                model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                
            }
            
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }
        }
        if(jComboBox6.getSelectedIndex()==1 || jComboBox7.getSelectedIndex()==1){
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            ArrayList<String> IDList = new ArrayList<>();
            ///
            
            try {
                
                String DBQ = "SELECT * FROM M7MAD.HOLIDAY";
                rsHoliday = stmtUpdate.executeQuery(DBQ);
                
                rsHoliday.absolute(0);
                while (rsHoliday.next()) {                    
                    String end = rsHoliday.getString("ENDING");
                    String ID = rsHoliday.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(end, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween>0){
                       IDList.add(ID);
                    }
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }
            
            ///
            try {
                
                String DBQ = "SELECT * FROM M7MAD.ABSENCE";
                rsAbsence = stmtUpdate.executeQuery(DBQ);
                
                rsAbsence.absolute(0);
                while (rsAbsence.next()) {                    
                    String absenceDate = rsAbsence.getString("DATE");
                    String ID = rsAbsence.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(absenceDate, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween==0){
                        IDList.add(ID);
                    }
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }
            ///
            
                try {
                    
                    String DBQ = "SELECT * FROM M7MAD.THESTAFFS";
                    rsStaff = stmt.executeQuery(DBQ);
                    rsStaff.absolute(0);
                    while (rsStaff.next()) {                        
                        
                        String DB_ID = rsStaff.getString("ID");
                        String DB_NAME = rsStaff.getString("NAME");
                        String DB_RANK = rsStaff.getString("RANK");
                        String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                        String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                        String DB_INTEREST = rsStaff.getString("INTEREST");
                        String DB_STATUS = rsStaff.getString("STATUS");
                        String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                        String DB_NICKNAME = rsStaff.getString("NICKNAME");
                        String fullName = DB_NAME+" "+DB_NICKNAME;

                        if(IDList.contains(DB_ID)){

                        }
                        else if(!"توقيف".equals(DB_STATUS)){
                            model = (DefaultTableModel) table.getModel();
                            model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                            model = (DefaultTableModel) table2.getModel();
                            model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                        }

                        
                    }
                    
                    
                } catch (SQLException ex) {
                    
                    JOptionPane.showMessageDialog(null, ex);
                }
              
            
            /////////////////////////////////////
        }
        if(jComboBox6.getSelectedIndex()==2 || jComboBox7.getSelectedIndex()==2){
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            try {
                
                String DBQ = "SELECT * FROM M7MAD.HOLIDAY WHERE TYPE='عطلة سنوية'";
                rsHoliday = stmtUpdate.executeQuery(DBQ);
                
                rsHoliday.absolute(0);
                while (rsHoliday.next()) {                    
                    String end = rsHoliday.getString("ENDING");
                    String ID = rsHoliday.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(end, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween>0){
                        DBQ = "SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                        rsStaff = stmt.executeQuery(DBQ);
                        rsStaff.first();
                        
                        String DB_ID = rsStaff.getString("ID");
                        String DB_NAME = rsStaff.getString("NAME");
                        String DB_RANK = rsStaff.getString("RANK");
                        String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                        String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                        String DB_INTEREST = rsStaff.getString("INTEREST");
                        String DB_STATUS = rsStaff.getString("STATUS");
                        String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                        String DB_NICKNAME = rsStaff.getString("NICKNAME");
                        String fullName = DB_NAME+" "+DB_NICKNAME;


                        model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                        model = (DefaultTableModel) table2.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                        
                    }
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
        if(jComboBox6.getSelectedIndex()==3 || jComboBox7.getSelectedIndex()==3){
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            try {
                
                String DBQ = "SELECT * FROM M7MAD.HOLIDAY WHERE TYPE='راحة تعويضية'";
                rsHoliday = stmtUpdate.executeQuery(DBQ);
                
                rsHoliday.absolute(0);
                while (rsHoliday.next()) {                    
                    String end = rsHoliday.getString("ENDING");
                    String ID = rsHoliday.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(end, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween>0){
                        DBQ = "SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                        rsStaff = stmt.executeQuery(DBQ);
                        rsStaff.first();
                        
                        String DB_ID = rsStaff.getString("ID");
                        String DB_NAME = rsStaff.getString("NAME");
                        String DB_RANK = rsStaff.getString("RANK");
                        String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                        String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                        String DB_INTEREST = rsStaff.getString("INTEREST");
                        String DB_STATUS = rsStaff.getString("STATUS");
                        String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                        String DB_NICKNAME = rsStaff.getString("NICKNAME");
                        String fullName = DB_NAME+" "+DB_NICKNAME;


                        model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                        model = (DefaultTableModel) table2.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                        
                    }
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
        if(jComboBox6.getSelectedIndex()==4 || jComboBox7.getSelectedIndex()==4){
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            try {
                
                String DBQ = "SELECT * FROM M7MAD.HOLIDAY WHERE TYPE='عطلة استثنائية'";
                rsHoliday = stmtUpdate.executeQuery(DBQ);
                
                rsHoliday.absolute(0);
                while (rsHoliday.next()) {                    
                    String end = rsHoliday.getString("ENDING");
                    String ID = rsHoliday.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(end, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween>0){
                        DBQ = "SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                        rsStaff = stmt.executeQuery(DBQ);
                        rsStaff.first();
                        
                        String DB_ID = rsStaff.getString("ID");
                        String DB_NAME = rsStaff.getString("NAME");
                        String DB_RANK = rsStaff.getString("RANK");
                        String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                        String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                        String DB_INTEREST = rsStaff.getString("INTEREST");
                        String DB_STATUS = rsStaff.getString("STATUS");
                        String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                        String DB_NICKNAME = rsStaff.getString("NICKNAME");
                        String fullName = DB_NAME+" "+DB_NICKNAME;


                        model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                        model = (DefaultTableModel) table2.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                        
                    }
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
        if(jComboBox6.getSelectedIndex()==5 || jComboBox7.getSelectedIndex()==5){
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            try {
                
                String DBQ = "SELECT * FROM M7MAD.HOLIDAY WHERE TYPE='عطلة مرضية'";
                rsHoliday = stmtUpdate.executeQuery(DBQ);
                
                rsHoliday.absolute(0);
                while (rsHoliday.next()) {                    
                    String end = rsHoliday.getString("ENDING");
                    String ID = rsHoliday.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(end, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween>0){
                        DBQ = "SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                        rsStaff = stmt.executeQuery(DBQ);
                        rsStaff.first();
                        
                        String DB_ID = rsStaff.getString("ID");
                        String DB_NAME = rsStaff.getString("NAME");
                        String DB_RANK = rsStaff.getString("RANK");
                        String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                        String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                        String DB_INTEREST = rsStaff.getString("INTEREST");
                        String DB_STATUS = rsStaff.getString("STATUS");
                        String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                        String DB_NICKNAME = rsStaff.getString("NICKNAME");
                        String fullName = DB_NAME+" "+DB_NICKNAME;


                        model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                        model = (DefaultTableModel) table2.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                        
                    }
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
        if(jComboBox6.getSelectedIndex()==6 || jComboBox7.getSelectedIndex()==6){
            model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            model = (DefaultTableModel) table2.getModel();
            model.setRowCount(0);
            
            try {
                
                String DBQ = "SELECT * FROM M7MAD.ABSENCE";
                rsAbsence = stmtUpdate.executeQuery(DBQ);
                
                rsAbsence.absolute(0);
                while (rsAbsence.next()) {                    
                    String absenceDate = rsAbsence.getString("DATE");
                    String ID = rsAbsence.getString("ID");
                    
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                    LocalDate dateBefore = LocalDate.parse(today, formatter);
                    LocalDate dateAfter = LocalDate.parse(absenceDate, formatter);
                    long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                    
                    if(noOfDaysBetween==0){
                        DBQ = "SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                        rsStaff = stmt.executeQuery(DBQ);
                        rsStaff.first();
                        
                        String DB_ID = rsStaff.getString("ID");
                        String DB_NAME = rsStaff.getString("NAME");
                        String DB_RANK = rsStaff.getString("RANK");
                        String DB_BIRTHDAY = rsStaff.getString("BIRTHDAY");
                        String DB_BIRTHPLACE = rsStaff.getString("BIRTHPLACE");
                        String DB_INTEREST = rsStaff.getString("INTEREST");
                        String DB_STATUS = rsStaff.getString("STATUS");
                        String DB_PHONENUMBER = rsStaff.getString("PHONENUMBER");
                        String DB_NICKNAME = rsStaff.getString("NICKNAME");
                        String fullName = DB_NAME+" "+DB_NICKNAME;


                        model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});

                        model = (DefaultTableModel) table2.getModel();
                        model.addRow(new Object[]{DB_PHONENUMBER,DB_BIRTHDAY,DB_BIRTHPLACE,DB_STATUS,DB_INTEREST,DB_RANK,fullName,DB_ID});
                        
                    }
                }
                
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, ex);
            }
        }
        jLabel20.setText(" العدد : "+model.getRowCount());
        jLabel21.setText(" العدد : "+model.getRowCount());
    }
    
    public void DBConnection(){
        String dir = System.getProperty("user.dir");
        try{
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            con = DriverManager.getConnection("jdbc:derby:"+dir+"\\DB", "m7mad", "m7mad");
            //con = DriverManager.getConnection("jdbc:derby:C:\\Users\\97059\\AppData\\Roaming\\NetBeans\\Derby\\DB", "m7mad", "m7mad");
            stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            stmtUpdate = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
            
            String DBQ ="SELECT * FROM M7MAD.ADMINS";
            rsAdmin = stmt.executeQuery(DBQ);
            
            DBQ ="SELECT * FROM M7MAD.THESTAFFS";
            rsStaff = stmt.executeQuery(DBQ);
            
            DBQ ="SELECT * FROM M7MAD.HOLIDAY";
            rsHoliday = stmt.executeQuery(DBQ);
            
            DBQ ="SELECT * FROM M7MAD.DELAY";
            rsDelay = stmt.executeQuery(DBQ);
            
            DBQ ="SELECT * FROM M7MAD.ABSENCE";
            rsAbsence = stmt.executeQuery(DBQ);
            
            DBQ ="SELECT * FROM M7MAD.LASTCHECK";
            rsDate = stmt.executeQuery(DBQ);
            
            
        }catch(ClassNotFoundException | SQLException e){
            JOptionPane.showMessageDialog(null, e);
        }
    }
    
    public void ToolTip(){
        goToDetail.setToolTipText("التفاصيل");
        changePassButton.setToolTipText("تغيير كلمة المرور");
        goToPrint.setToolTipText("طباعة");
        newStaff.setToolTipText("اضافة موظف");
        backToAdmin_1Tabel.setToolTipText("رجوع");
        backToAdmin_1Tabel_2.setToolTipText("رجوع");
        deleteButton.setToolTipText("حذف الموظف");
        updateDataButton.setToolTipText("تحديث بيانات الموظف");
        staffPicLabel.setToolTipText("تغيير الصورة");
    }
    
    
    public void addImageToLoginPanel(){
        
        ImageIcon i = new ImageIcon("src\\img\\logo.png");
        Image im = i.getImage();
        Image fi = im.getScaledInstance(300,300, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(fi);
        admin_1Enter.setIcon(scaledIcon);
        
        i = new ImageIcon("src\\img\\002.png");
        im = i.getImage();
        fi = im.getScaledInstance(105,109, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        goToDetail.setIcon(scaledIcon);
        goToDetail3.setIcon(scaledIcon);
        
        i = new ImageIcon("src\\img\\003.png");
        im = i.getImage();
        fi = im.getScaledInstance(105,109, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        goToPrint.setIcon(scaledIcon);
        goToPrint3.setIcon(scaledIcon);
        
        i = new ImageIcon("src\\img\\001.png");
        im = i.getImage();
        fi = im.getScaledInstance(105,109, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        newStaff.setIcon(scaledIcon);
        
        i = new ImageIcon("src\\img\\006.png");
        im = i.getImage();
        fi = im.getScaledInstance(34,34, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        backToAdmin_1Tabel.setIcon(scaledIcon);
        backToAdmin_1Tabel_2.setIcon(scaledIcon);
        backToUserTabel.setIcon(scaledIcon);
        backToLogin.setIcon(scaledIcon);
        backToLogin3.setIcon(scaledIcon);
        
        
        
        i = new ImageIcon("src\\img\\007.png");
        im = i.getImage();
        fi = im.getScaledInstance(30,30,Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        updateDataButton.setIcon(scaledIcon);
        
        
        
        i = new ImageIcon("src\\img\\008.png");
        im = i.getImage();
        fi = im.getScaledInstance(30,30, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        deleteButton.setIcon(scaledIcon);
        
        i = new ImageIcon("src\\img\\000.png");
        im = i.getImage();
        fi = im.getScaledInstance(150,200, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        imgChossed.setIcon(scaledIcon);
        
        
        i = new ImageIcon("src\\img\\004.png");
        im = i.getImage();
        fi = im.getScaledInstance(105,109, Image.SCALE_SMOOTH);
        scaledIcon = new ImageIcon(fi);
        changePassButton.setIcon(scaledIcon);
        
        
        
    }
    
    public void tableStyel(JTable table,boolean checkB){
            model = (DefaultTableModel) table.getModel();
            
            DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
            rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
            if(checkB){
                for(int i=1;i<table.getColumnCount();i++){
                table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
                }
            }else{
                for(int i=0;i<table.getColumnCount();i++){
                table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
                }
            }
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)
            table.getTableHeader().getDefaultRenderer();
            renderer.setHorizontalAlignment(JLabel.CENTER);
            
            table.setRowHeight(30);
            
        }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        loginPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        admin_1Enter = new javax.swing.JLabel();
        userEnter = new javax.swing.JButton();
        userEnter1 = new javax.swing.JButton();
        userNameField = new javax.swing.JTextField();
        userPasswordField = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        admin_1TabelPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        newStaff = new javax.swing.JLabel();
        goToPrint = new javax.swing.JLabel();
        goToDetail = new javax.swing.JLabel();
        backToLogin = new javax.swing.JLabel();
        changePassButton = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox<>();
        jLabel20 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        admin_1DetailPanel = new javax.swing.JPanel();
        backToAdmin_1Tabel = new javax.swing.JLabel();
        staffPicLabel = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        absence_Button_1 = new javax.swing.JLabel();
        holiday_Button_1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        holiday_panel_1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        holiday_tabel_1 = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jComboBox5 = new javax.swing.JComboBox<>();
        abcsence_panel_1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        abcsence_tabel_1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        lateness_panel_1 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        lateness_tabel_1 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        updateDataButton = new javax.swing.JLabel();
        deleteButton = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jTextField30 = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        addStaffPanel = new javax.swing.JPanel();
        backToAdmin_1Tabel_2 = new javax.swing.JLabel();
        imgChooserButton = new javax.swing.JButton();
        imgChossed = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jTextField10 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextField14 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jTextField16 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        URLChossed = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jTextField29 = new javax.swing.JTextField();
        userTabelPanel = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        table2 = new javax.swing.JTable();
        goToDetail3 = new javax.swing.JLabel();
        goToPrint3 = new javax.swing.JLabel();
        backToLogin3 = new javax.swing.JLabel();
        jComboBox7 = new javax.swing.JComboBox<>();
        jLabel21 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        userDetailPanel = new javax.swing.JPanel();
        backToUserTabel = new javax.swing.JLabel();
        staffPicLabel3 = new javax.swing.JLabel();
        jTextField19 = new javax.swing.JTextField();
        jLabel28 = new javax.swing.JLabel();
        jTextField20 = new javax.swing.JTextField();
        jTextField21 = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jTextField22 = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jTextField23 = new javax.swing.JTextField();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jTextField24 = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        absence_Button_3 = new javax.swing.JLabel();
        holiday_Button_3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        holiday_panel_3 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        holiday_tabel_3 = new javax.swing.JTable();
        jButton13 = new javax.swing.JButton();
        abcsence_panel_3 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        abcsence_tabel_3 = new javax.swing.JTable();
        jButton14 = new javax.swing.JButton();
        lateness_panel_3 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        lateness_tabel_3 = new javax.swing.JTable();
        jButton10 = new javax.swing.JButton();
        jTextField25 = new javax.swing.JTextField();
        jTextField26 = new javax.swing.JTextField();
        jLabel41 = new javax.swing.JLabel();
        jTextField31 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("تسيير الموظفين");
        setPreferredSize(new java.awt.Dimension(1035, 550));
        setResizable(false);

        jPanel1.setLayout(new java.awt.CardLayout());

        loginPanel.setBackground(new java.awt.Color(226, 226, 226));
        loginPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("PT Bold Heading", 1, 36)); // NOI18N
        jLabel1.setText("تسجيل الدخول");
        loginPanel.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 40, 230, 50));

        admin_1Enter.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        loginPanel.add(admin_1Enter, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 120, 310, 280));

        userEnter.setBackground(new java.awt.Color(0, 129, 194));
        userEnter.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        userEnter.setText("الدخول كمشاهد");
        userEnter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userEnterMouseClicked(evt);
            }
        });
        loginPanel.add(userEnter, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 460, 167, 38));

        userEnter1.setBackground(new java.awt.Color(0, 129, 194));
        userEnter1.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        userEnter1.setText("تسجيل الدخول");
        userEnter1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                userEnter1MouseClicked(evt);
            }
        });
        loginPanel.add(userEnter1, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 400, 167, 38));

        userNameField.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        loginPanel.add(userNameField, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 190, 260, 50));

        userPasswordField.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        loginPanel.add(userPasswordField, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 300, 260, 50));

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel3.setText("كلمة المرور");
        loginPanel.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 260, 80, 40));

        jLabel36.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jLabel36.setText("الاسم");
        loginPanel.add(jLabel36, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 150, 30, 40));

        jLabel22.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(51, 51, 51));
        jLabel22.setText("يرمجة : سالم سليمان");
        loginPanel.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 500, -1, -1));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/999.jpg"))); // NOI18N
        loginPanel.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 1020, -1));

        jPanel1.add(loginPanel, "card2");

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "رقم الهاتف", "تاريخ الازدياد", "مكان الازدياد", "الوضعية", "المصلحة", "الرتبة", "اسم الموظف", "رمز الموظف"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(table);
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(6).setResizable(false);
        }

        newStaff.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        newStaff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newStaffMouseClicked(evt);
            }
        });

        goToPrint.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        goToPrint.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goToPrintMouseClicked(evt);
            }
        });

        goToDetail.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        goToDetail.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goToDetailMouseClicked(evt);
            }
        });

        backToLogin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backToLoginMouseClicked(evt);
            }
        });

        changePassButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        changePassButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                changePassButtonMouseClicked(evt);
            }
        });

        jComboBox6.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox6.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jComboBox6.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "جميع الموظفين", "المتواجدون حاليا", "المتواجدون في عطلة سنوية", "المتواجدون في راحة تعويضية", "المتواجدون في عطلة استثنائية", "المتواجدون في عطلة مرضية", "المتواجدون في الغيابات" }));
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox6ActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("العدد : 0");

        jLabel23.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel23.setText("اسم الموظف");

        jTextField7.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField7.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jButton2.setText("بحث");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout admin_1TabelPanelLayout = new javax.swing.GroupLayout(admin_1TabelPanel);
        admin_1TabelPanel.setLayout(admin_1TabelPanelLayout);
        admin_1TabelPanelLayout.setHorizontalGroup(
            admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(admin_1TabelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(admin_1TabelPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1017, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(admin_1TabelPanelLayout.createSequentialGroup()
                        .addComponent(goToPrint, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(admin_1TabelPanelLayout.createSequentialGroup()
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35)
                                .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(27, 27, 27)
                        .addComponent(changePassButton, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(goToDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(newStaff, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backToLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))))
        );
        admin_1TabelPanelLayout.setVerticalGroup(
            admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1TabelPanelLayout.createSequentialGroup()
                .addGroup(admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, admin_1TabelPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(goToPrint, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, admin_1TabelPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(newStaff, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(goToDetail, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(changePassButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1TabelPanelLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(admin_1TabelPanelLayout.createSequentialGroup()
                                .addComponent(backToLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(admin_1TabelPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(admin_1TabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.add(admin_1TabelPanel, "card9");

        backToAdmin_1Tabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToAdmin_1Tabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backToAdmin_1TabelMouseClicked(evt);
            }
        });

        staffPicLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        staffPicLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                staffPicLabelMouseClicked(evt);
            }
        });

        jTextField1.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel4.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("رمز الموظف");

        jTextField2.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel5.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("اسم الموظف");

        jTextField3.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel6.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("لقب الموظف");

        jLabel7.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("المصلحة");

        jTextField5.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel8.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("مكان الازدياد");

        jTextField6.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel9.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("تاريخ الازدياد");

        jLabel10.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("الوضعية");

        jTextField8.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField8.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel11.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("رقم الهاتف");

        absence_Button_1.setBackground(new java.awt.Color(255, 255, 255));
        absence_Button_1.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        absence_Button_1.setForeground(new java.awt.Color(0, 0, 0));
        absence_Button_1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        absence_Button_1.setText("الغيابات");
        absence_Button_1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        absence_Button_1.setOpaque(true);
        absence_Button_1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                absence_Button_1MouseClicked(evt);
            }
        });

        holiday_Button_1.setBackground(new java.awt.Color(104, 185, 216));
        holiday_Button_1.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        holiday_Button_1.setForeground(new java.awt.Color(0, 0, 0));
        holiday_Button_1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        holiday_Button_1.setText("العطلات");
        holiday_Button_1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        holiday_Button_1.setOpaque(true);
        holiday_Button_1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                holiday_Button_1MouseClicked(evt);
            }
        });

        jPanel2.setLayout(new java.awt.CardLayout());

        holiday_tabel_1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "تأكيد الانتهاء", "تاريخ النهاية", "تاريخ البداية", "نوع العطة", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(holiday_tabel_1);
        if (holiday_tabel_1.getColumnModel().getColumnCount() > 0) {
            holiday_tabel_1.getColumnModel().getColumn(0).setMaxWidth(80);
            holiday_tabel_1.getColumnModel().getColumn(4).setMinWidth(0);
            holiday_tabel_1.getColumnModel().getColumn(4).setMaxWidth(0);
        }

        jButton3.setText("حذف");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        jButton8.setText("التفاصيل");
        jButton8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton8MouseClicked(evt);
            }
        });

        jComboBox5.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox5.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "عطلة سنوية", "عطلة مرضية", "عطلة استثنائية", "راحة تعويضية" }));
        jComboBox5.setToolTipText("");
        jComboBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout holiday_panel_1Layout = new javax.swing.GroupLayout(holiday_panel_1);
        holiday_panel_1.setLayout(holiday_panel_1Layout);
        holiday_panel_1Layout.setHorizontalGroup(
            holiday_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, holiday_panel_1Layout.createSequentialGroup()
                .addGroup(holiday_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        holiday_panel_1Layout.setVerticalGroup(
            holiday_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(holiday_panel_1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton8)
                .addContainerGap(108, Short.MAX_VALUE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jPanel2.add(holiday_panel_1, "card2");

        abcsence_tabel_1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ملاحظات", "التاريخ", ""
            }
        ));
        jScrollPane4.setViewportView(abcsence_tabel_1);
        if (abcsence_tabel_1.getColumnModel().getColumnCount() > 0) {
            abcsence_tabel_1.getColumnModel().getColumn(2).setMinWidth(0);
            abcsence_tabel_1.getColumnModel().getColumn(2).setMaxWidth(0);
        }

        jButton4.setText("اضافة");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });

        jButton5.setText("حذف");
        jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton5MouseClicked(evt);
            }
        });

        jButton11.setText("التفاصيل");
        jButton11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton11MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout abcsence_panel_1Layout = new javax.swing.GroupLayout(abcsence_panel_1);
        abcsence_panel_1.setLayout(abcsence_panel_1Layout);
        abcsence_panel_1Layout.setHorizontalGroup(
            abcsence_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, abcsence_panel_1Layout.createSequentialGroup()
                .addGroup(abcsence_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(abcsence_panel_1Layout.createSequentialGroup()
                        .addGroup(abcsence_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE))
                        .addGap(6, 6, 6))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, abcsence_panel_1Layout.createSequentialGroup()
                        .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        abcsence_panel_1Layout.setVerticalGroup(
            abcsence_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(abcsence_panel_1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton11)
                .addContainerGap(112, Short.MAX_VALUE))
        );

        jPanel2.add(abcsence_panel_1, "card3");

        lateness_tabel_1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ملاحظات", "التاريخ", "مدة التأخر", ""
            }
        ));
        jScrollPane5.setViewportView(lateness_tabel_1);
        if (lateness_tabel_1.getColumnModel().getColumnCount() > 0) {
            lateness_tabel_1.getColumnModel().getColumn(3).setMinWidth(0);
            lateness_tabel_1.getColumnModel().getColumn(3).setMaxWidth(0);
        }

        jButton6.setText("اضافة");
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });

        jButton7.setText("حذف");
        jButton7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton7MouseClicked(evt);
            }
        });

        jButton12.setText("التفاصيل");
        jButton12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton12MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout lateness_panel_1Layout = new javax.swing.GroupLayout(lateness_panel_1);
        lateness_panel_1.setLayout(lateness_panel_1Layout);
        lateness_panel_1Layout.setHorizontalGroup(
            lateness_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lateness_panel_1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(lateness_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        lateness_panel_1Layout.setVerticalGroup(
            lateness_panel_1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
            .addGroup(lateness_panel_1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.add(lateness_panel_1, "card4");

        updateDataButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateDataButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                updateDataButtonMouseClicked(evt);
            }
        });

        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteButtonMouseClicked(evt);
            }
        });

        jComboBox3.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox3.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "عامل", "عزل", "توقيف", "انتداب", "استيداع" }));

        jComboBox4.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox4.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "الاحتباس", "اعادة الادماج", "المقتصدة", "الصحة والمساعدة الاجتماعية", "مصلحة الأمن", "مصلحة الادارة العامة", "مصلحة التقيم و التوجيه" }));

        jTextField30.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField30.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel38.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel38.setText("الرتبة");

        javax.swing.GroupLayout admin_1DetailPanelLayout = new javax.swing.GroupLayout(admin_1DetailPanel);
        admin_1DetailPanel.setLayout(admin_1DetailPanelLayout);
        admin_1DetailPanelLayout.setHorizontalGroup(
            admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19)
                                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(absence_Button_1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(holiday_Button_1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(updateDataButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 890, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(37, 37, 37))
                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                        .addComponent(staffPicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(21, 21, 21))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(15, 15, 15))
                                            .addComponent(jComboBox4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jComboBox3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(9, 9, 9))))
                                    .addComponent(jTextField3))
                                .addGap(18, 18, 18)
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(37, 37, 37))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                .addComponent(backToAdmin_1Tabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25))))))
        );
        admin_1DetailPanelLayout.setVerticalGroup(
            admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(staffPicLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(backToAdmin_1Tabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel4))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel38)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField30, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addGap(41, 41, 41))
                                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(2, 2, 2)))
                                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                        .addGap(44, 44, 44)
                                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel11)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel10))))))))
                .addGap(18, 18, 18)
                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(admin_1DetailPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(holiday_Button_1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(absence_Button_1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(admin_1DetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, admin_1DetailPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(updateDataButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel1.add(admin_1DetailPanel, "card2");

        backToAdmin_1Tabel_2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToAdmin_1Tabel_2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backToAdmin_1Tabel_2MouseClicked(evt);
            }
        });

        imgChooserButton.setText("اختيار صورة");
        imgChooserButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imgChooserButtonMouseClicked(evt);
            }
        });

        jTextField9.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField9.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel12.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("رمز الموظف");

        jTextField12.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField12.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel15.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("اسم الموظف");

        jLabel16.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("لقب الموظف");

        jTextField13.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField13.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jTextField10.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField10.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel13.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("مكان الازدياد");

        jTextField14.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField14.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel17.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("تاريخ الازدياد");

        jLabel18.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel18.setText("المصلحة");

        jLabel14.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("الوضعية");

        jLabel19.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText("رقم الهاتف");

        jTextField16.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField16.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jButton1.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        jButton1.setText("اضافة");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });

        jComboBox1.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox1.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "عامل", "عزل", "توقيف", "انتداب", "استيداع" }));

        jComboBox2.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox2.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "الاحتباس", "اعادة الادماج", "المقتصدة", "الصحة والمساعدة الاجتماعية", "مصلحة الأمن", "مصلحة الادارة العامة", "مصلحة التقيم و التوجيه" }));

        jLabel37.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel37.setText("الرتبة");

        jTextField29.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField29.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout addStaffPanelLayout = new javax.swing.GroupLayout(addStaffPanel);
        addStaffPanel.setLayout(addStaffPanelLayout);
        addStaffPanelLayout.setHorizontalGroup(
            addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStaffPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStaffPanelLayout.createSequentialGroup()
                        .addComponent(backToAdmin_1Tabel_2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStaffPanelLayout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(419, 419, 419))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStaffPanelLayout.createSequentialGroup()
                        .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(imgChossed, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(addStaffPanelLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addStaffPanelLayout.createSequentialGroup()
                                        .addGap(53, 53, 53)
                                        .addComponent(URLChossed, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(imgChooserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(62, 138, Short.MAX_VALUE)
                        .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                                    .addComponent(jTextField29, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(51, 51, 51)
                        .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStaffPanelLayout.createSequentialGroup()
                                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(45, 45, 45)
                                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(99, 99, 99))))
        );
        addStaffPanelLayout.setVerticalGroup(
            addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addStaffPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(backToAdmin_1Tabel_2, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(addStaffPanelLayout.createSequentialGroup()
                        .addComponent(imgChossed, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imgChooserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addStaffPanelLayout.createSequentialGroup()
                        .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(addStaffPanelLayout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(addStaffPanelLayout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31)
                                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(addStaffPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(addStaffPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel17)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(addStaffPanelLayout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel37)
                                    .addGroup(addStaffPanelLayout.createSequentialGroup()
                                        .addGap(26, 26, 26)
                                        .addComponent(jTextField29, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(31, 31, 31)
                        .addGroup(addStaffPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(addStaffPanelLayout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBox2))
                            .addGroup(addStaffPanelLayout.createSequentialGroup()
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addStaffPanelLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 69, Short.MAX_VALUE)
                .addComponent(URLChossed, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(69, 69, 69))
        );

        jPanel1.add(addStaffPanel, "card9");

        table2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "رقم الهاتف", "تاريخ الازدياد", "مكان الازدياد", "الوضعية", "المصلحة", "الرتبة", "اسم الموظف", "رمز الموظف"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                table2MouseClicked(evt);
            }
        });
        jScrollPane9.setViewportView(table2);
        if (table2.getColumnModel().getColumnCount() > 0) {
            table2.getColumnModel().getColumn(6).setResizable(false);
        }

        goToDetail3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        goToDetail3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goToDetail3MouseClicked(evt);
            }
        });

        goToPrint3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        goToPrint3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                goToPrint3MouseClicked(evt);
            }
        });

        backToLogin3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToLogin3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backToLogin3MouseClicked(evt);
            }
        });

        jComboBox7.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox7.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
        jComboBox7.setForeground(new java.awt.Color(0, 0, 0));
        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "جميع الموظفين", "المتواجدون حاليا", "المتواجدون في عطلة سنوية", "المتواجدون في راحة تعويضية", "المتواجدون في عطلة استثنائية", "المتواجدون في عطلة مرضية", "المتواجدون في الغيابات" }));
        jComboBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox7ActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel21.setText("العدد : 0");

        jLabel24.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel24.setText("اسم الموظف");

        jTextField11.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField11.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jButton9.setText("بحث");
        jButton9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton9MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout userTabelPanelLayout = new javax.swing.GroupLayout(userTabelPanel);
        userTabelPanel.setLayout(userTabelPanelLayout);
        userTabelPanelLayout.setHorizontalGroup(
            userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userTabelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(userTabelPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane9)
                        .addContainerGap())
                    .addGroup(userTabelPanelLayout.createSequentialGroup()
                        .addComponent(goToPrint3, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 305, Short.MAX_VALUE)
                        .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(jLabel24)
                        .addGap(35, 35, 35)
                        .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 20, Short.MAX_VALUE)
                        .addComponent(goToDetail3, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(backToLogin3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))))
        );
        userTabelPanelLayout.setVerticalGroup(
            userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userTabelPanelLayout.createSequentialGroup()
                .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(userTabelPanelLayout.createSequentialGroup()
                        .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(userTabelPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(goToPrint3, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                                    .addComponent(goToDetail3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(userTabelPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(backToLogin3, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 70, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userTabelPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(userTabelPanelLayout.createSequentialGroup()
                                .addGroup(userTabelPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton9))
                            .addGroup(userTabelPanelLayout.createSequentialGroup()
                                .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21)))
                .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1.add(userTabelPanel, "card2");

        backToUserTabel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backToUserTabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                backToUserTabelMouseClicked(evt);
            }
        });

        jTextField19.setEditable(false);
        jTextField19.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField19.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel28.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel28.setText("رمز الموظف");

        jTextField20.setEditable(false);
        jTextField20.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField20.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jTextField21.setEditable(false);
        jTextField21.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField21.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel29.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel29.setText("اسم الموظف");

        jLabel30.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel30.setText("الرتبة");

        jLabel31.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel31.setText("المصلحة");

        jTextField22.setEditable(false);
        jTextField22.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField22.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel32.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel32.setText("مكان الازدياد");

        jTextField23.setEditable(false);
        jTextField23.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField23.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel33.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel33.setText("تاريخ الازدياد");

        jLabel34.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel34.setText("الوضعية");

        jTextField24.setEditable(false);
        jTextField24.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField24.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel35.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel35.setText("رقم الهاتف");

        absence_Button_3.setBackground(new java.awt.Color(255, 255, 255));
        absence_Button_3.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        absence_Button_3.setForeground(new java.awt.Color(0, 0, 0));
        absence_Button_3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        absence_Button_3.setText("الغيابات");
        absence_Button_3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        absence_Button_3.setOpaque(true);
        absence_Button_3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                absence_Button_3MouseClicked(evt);
            }
        });

        holiday_Button_3.setBackground(new java.awt.Color(104, 185, 216));
        holiday_Button_3.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        holiday_Button_3.setForeground(new java.awt.Color(0, 0, 0));
        holiday_Button_3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        holiday_Button_3.setText("العطلات");
        holiday_Button_3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        holiday_Button_3.setOpaque(true);
        holiday_Button_3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                holiday_Button_3MouseClicked(evt);
            }
        });

        jPanel4.setLayout(new java.awt.CardLayout());

        holiday_tabel_3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "تأكيد الانتهاء", "تاريخ النهاية", "تاريخ البداية", "نوع العطة", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane10.setViewportView(holiday_tabel_3);
        if (holiday_tabel_3.getColumnModel().getColumnCount() > 0) {
            holiday_tabel_3.getColumnModel().getColumn(0).setMaxWidth(80);
            holiday_tabel_3.getColumnModel().getColumn(4).setMinWidth(0);
            holiday_tabel_3.getColumnModel().getColumn(4).setMaxWidth(0);
        }

        jButton13.setText("التفاصيل");
        jButton13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton13MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout holiday_panel_3Layout = new javax.swing.GroupLayout(holiday_panel_3);
        holiday_panel_3.setLayout(holiday_panel_3Layout);
        holiday_panel_3Layout.setHorizontalGroup(
            holiday_panel_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, holiday_panel_3Layout.createSequentialGroup()
                .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        holiday_panel_3Layout.setVerticalGroup(
            holiday_panel_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(holiday_panel_3Layout.createSequentialGroup()
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(holiday_panel_3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton13)
                .addContainerGap(211, Short.MAX_VALUE))
        );

        jPanel4.add(holiday_panel_3, "card2");

        abcsence_tabel_3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ملاحظات", "التاريخ", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane11.setViewportView(abcsence_tabel_3);
        if (abcsence_tabel_3.getColumnModel().getColumnCount() > 0) {
            abcsence_tabel_3.getColumnModel().getColumn(2).setMinWidth(0);
            abcsence_tabel_3.getColumnModel().getColumn(2).setMaxWidth(0);
        }

        jButton14.setText("التفاصيل");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout abcsence_panel_3Layout = new javax.swing.GroupLayout(abcsence_panel_3);
        abcsence_panel_3.setLayout(abcsence_panel_3Layout);
        abcsence_panel_3Layout.setHorizontalGroup(
            abcsence_panel_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, abcsence_panel_3Layout.createSequentialGroup()
                .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        abcsence_panel_3Layout.setVerticalGroup(
            abcsence_panel_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
            .addGroup(abcsence_panel_3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton14)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(abcsence_panel_3, "card3");

        lateness_tabel_3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ملاحظات", "التاريخ", "مدة التأخر", ""
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane12.setViewportView(lateness_tabel_3);
        if (lateness_tabel_3.getColumnModel().getColumnCount() > 0) {
            lateness_tabel_3.getColumnModel().getColumn(3).setMinWidth(0);
            lateness_tabel_3.getColumnModel().getColumn(3).setMaxWidth(0);
        }

        jButton10.setText("التفاصيل");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout lateness_panel_3Layout = new javax.swing.GroupLayout(lateness_panel_3);
        lateness_panel_3.setLayout(lateness_panel_3Layout);
        lateness_panel_3Layout.setHorizontalGroup(
            lateness_panel_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, lateness_panel_3Layout.createSequentialGroup()
                .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 795, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        lateness_panel_3Layout.setVerticalGroup(
            lateness_panel_3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
            .addGroup(lateness_panel_3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jButton10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(lateness_panel_3, "card4");

        jTextField25.setEditable(false);
        jTextField25.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField25.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jTextField26.setEditable(false);
        jTextField26.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField26.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        jLabel41.setFont(new java.awt.Font("Calibri", 1, 16)); // NOI18N
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel41.setText("لقب الموظف");

        jTextField31.setEditable(false);
        jTextField31.setFont(new java.awt.Font("Dialog", 0, 16)); // NOI18N
        jTextField31.setHorizontalAlignment(javax.swing.JTextField.RIGHT);

        javax.swing.GroupLayout userDetailPanelLayout = new javax.swing.GroupLayout(userDetailPanel);
        userDetailPanel.setLayout(userDetailPanelLayout);
        userDetailPanelLayout.setHorizontalGroup(
            userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userDetailPanelLayout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(staffPicLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createSequentialGroup()
                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(userDetailPanelLayout.createSequentialGroup()
                                .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15)))
                        .addGap(18, 18, 18)
                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(userDetailPanelLayout.createSequentialGroup()
                                .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27))
                            .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createSequentialGroup()
                                    .addComponent(jTextField31)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                .addGroup(userDetailPanelLayout.createSequentialGroup()
                                    .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField25, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
                                    .addGap(18, 18, 18))))
                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createSequentialGroup()
                        .addComponent(backToUserTabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createSequentialGroup()
                        .addComponent(absence_Button_3, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(holiday_Button_3, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 900, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );
        userDetailPanelLayout.setVerticalGroup(
            userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userDetailPanelLayout.createSequentialGroup()
                .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(userDetailPanelLayout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(staffPicLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(userDetailPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(backToUserTabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(userDetailPanelLayout.createSequentialGroup()
                                .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(userDetailPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel29)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel28)
                                    .addGroup(userDetailPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel41)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField31, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(userDetailPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel32)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(userDetailPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel33)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jTextField23, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jTextField25, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                            .addGroup(userDetailPanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jTextField26, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel34))
                            .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, userDetailPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel30)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(userDetailPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel31)
                                    .addGap(59, 59, 59)
                                    .addComponent(jLabel35)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(jTextField24, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(18, 18, 18)
                .addGroup(userDetailPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(holiday_Button_3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(absence_Button_3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.add(userDetailPanel, "card2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newStaffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newStaffMouseClicked
        addStaffPanel.setVisible(true);
        admin_1TabelPanel.setVisible(false);
    }//GEN-LAST:event_newStaffMouseClicked

    private void goToDetailMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goToDetailMouseClicked
    
        toDetailPanel();
        
    }//GEN-LAST:event_goToDetailMouseClicked

    private void backToAdmin_1TabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backToAdmin_1TabelMouseClicked
        admin_1TabelPanel.setVisible(true);
        admin_1DetailPanel.setVisible(false);
    }//GEN-LAST:event_backToAdmin_1TabelMouseClicked

    private void holiday_Button_1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_holiday_Button_1MouseClicked
        holiday_Button_1.setBackground(new Color(104,185,216));
        absence_Button_1.setBackground(Color.WHITE);
        
        holiday_panel_1.setVisible(true);
        lateness_panel_1.setVisible(false);
        abcsence_panel_1.setVisible(false);
    }//GEN-LAST:event_holiday_Button_1MouseClicked

    private void absence_Button_1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_absence_Button_1MouseClicked
        holiday_Button_1.setBackground(Color.WHITE);
        absence_Button_1.setBackground(new Color(104,185,216));
        
        holiday_panel_1.setVisible(false);
        lateness_panel_1.setVisible(false);
        abcsence_panel_1.setVisible(true);
    }//GEN-LAST:event_absence_Button_1MouseClicked

    private void imgChooserButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imgChooserButtonMouseClicked
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);

        File f = chooser.getSelectedFile();
        String filePath = f.getAbsolutePath();

        ImageIcon i = new ImageIcon(filePath);
        Image im = i.getImage();
        Image fi = im.getScaledInstance(150,200, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(fi);
        imgChossed.setIcon(scaledIcon);
        URLChossed.setText(filePath);
    }//GEN-LAST:event_imgChooserButtonMouseClicked

    private void backToAdmin_1Tabel_2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backToAdmin_1Tabel_2MouseClicked
        admin_1TabelPanel.setVisible(true);
        addStaffPanel.setVisible(false);
    }//GEN-LAST:event_backToAdmin_1Tabel_2MouseClicked

    private void userEnterMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userEnterMouseClicked
        reFreshTable();
        loginPanel.setVisible(false);
        userTabelPanel.setVisible(true);
        lastSelect=-1;
    }//GEN-LAST:event_userEnterMouseClicked

    private void goToDetail3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goToDetail3MouseClicked
        toUserDetailPanel();
        
    }//GEN-LAST:event_goToDetail3MouseClicked

    private void backToUserTabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backToUserTabelMouseClicked
        userDetailPanel.setVisible(false);
        userTabelPanel.setVisible(true);
    }//GEN-LAST:event_backToUserTabelMouseClicked

    private void absence_Button_3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_absence_Button_3MouseClicked
        holiday_Button_3.setBackground(Color.WHITE);
        absence_Button_3.setBackground(new Color(104,185,216));
        
        holiday_panel_3.setVisible(false);
        lateness_panel_3.setVisible(false);
        abcsence_panel_3.setVisible(true);
    }//GEN-LAST:event_absence_Button_3MouseClicked

    private void holiday_Button_3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_holiday_Button_3MouseClicked
        holiday_Button_3.setBackground(new Color(104,185,216));
        absence_Button_3.setBackground(Color.WHITE);
        
        holiday_panel_3.setVisible(true);
        lateness_panel_3.setVisible(false);
        abcsence_panel_3.setVisible(false);
    }//GEN-LAST:event_holiday_Button_3MouseClicked

    private void backToLoginMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backToLoginMouseClicked
        loginPanel.setVisible(true);
        admin_1TabelPanel.setVisible(false);
        jComboBox6.setSelectedIndex(0);
        jComboBox7.setSelectedIndex(0);
    }//GEN-LAST:event_backToLoginMouseClicked

    private void backToLogin3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_backToLogin3MouseClicked
        loginPanel.setVisible(true);
        userTabelPanel.setVisible(false);
        jComboBox6.setSelectedIndex(0);
        jComboBox7.setSelectedIndex(0);
    }//GEN-LAST:event_backToLogin3MouseClicked

    private void changePassButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_changePassButtonMouseClicked
        
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        Object[] message = {
            "Username:", username,
            "Password:", password
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String userN = username.getText();
            String userP = password.getText();
            
            try{
                String DBQ ="UPDATE M7MAD.ADMINS SET USERNAME = '"+userN+"' WHERE USERNAME='"+currentAdmin+"'";
                stmt.executeUpdate(DBQ);
                
                DBQ ="UPDATE M7MAD.ADMINS SET PASSWORD = '"+userP+"' WHERE USERNAME='"+userN+"'";
                stmt.executeUpdate(DBQ);
                
                DBQ ="UPDATE M7MAD.HOLIDAY SET DETAILS = '"+userN+"' WHERE DETAILS='"+currentAdmin+"'";
                stmt.executeUpdate(DBQ);
                
                DBQ ="UPDATE M7MAD.DELAY SET DETAILS = '"+userN+"' WHERE DETAILS='"+currentAdmin+"'";
                stmt.executeUpdate(DBQ);
                
                DBQ ="UPDATE M7MAD.ABSENCE SET DETAILS = '"+userN+"' WHERE DETAILS='"+currentAdmin+"'";
                stmt.executeUpdate(DBQ);
                
                currentAdmin = userN;
                
                JOptionPane.showMessageDialog(null, "Username and password changed");
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Username is not available");
            }
            
        }
    }//GEN-LAST:event_changePassButtonMouseClicked

    private void userEnter1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_userEnter1MouseClicked
        String userName = userNameField.getText();
        String userPassword = userPasswordField.getText();
        try{
            String DBQ ="SELECT * FROM M7MAD.ADMINS WHERE USERNAME='"+userName+"' AND PASSWORD='"+userPassword+"'";
            rsAdmin = stmt.executeQuery(DBQ);
            rsAdmin.next();
            if(rsAdmin.getString("ID").equals("1")){
                userNameField.setText("");
                userPasswordField.setText("");
                loginPanel.setVisible(false);
                admin_1TabelPanel.setVisible(true);
                currentAdmin = userName;
            }else if(rsAdmin.getString("ID").equals("2")){
                userNameField.setText("");
                userPasswordField.setText("");
                loginPanel.setVisible(false);
                admin_1TabelPanel.setVisible(true);
                currentAdmin = userName;
            }
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Wrong user name or password");
        }
        lastSelect=-1;
        
    }//GEN-LAST:event_userEnter1MouseClicked

    private void staffPicLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_staffPicLabelMouseClicked
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);

        File f = chooser.getSelectedFile();
        String filePath = f.getAbsolutePath();

        ImageIcon i = new ImageIcon(filePath);
        Image im = i.getImage();
        Image fi = im.getScaledInstance(140,140, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(fi);
        staffPicLabel.setIcon(scaledIcon);
        jLabel39.setText(filePath);
    }//GEN-LAST:event_staffPicLabelMouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
       
    String new_date = jTextField14.getText();
    String new_id = jTextField9.getText();
    String new_name = jTextField12.getText();
    String new_phone = jTextField16.getText();
    String new_status =(String) jComboBox1.getSelectedItem();
    String new_interest =(String) jComboBox2.getSelectedItem();
    String new_rank = jTextField29.getText();
    String new_place = jTextField10.getText();
    String new_nickName = jTextField13.getText();
    String new_pic = URLChossed.getText();
    double new_restday = 0;
    
        
    if( !new_date.equals("") & !new_id.equals("") & !new_name.equals("") & !new_phone.equals("") & !new_rank.equals("") & !new_place.equals("") & !new_nickName.equals("")){
        if(checkValidDate(new_date)){
            
            try{
                
                String DBQ ="INSERT INTO M7MAD.THESTAFFS VALUES ('"+new_id+"','"+new_name+"','"+new_rank+"','"+new_date+"','"+new_place+"','"+new_interest+"','"+new_status+"','"+new_phone+"',0,'"+new_pic+"','"+new_nickName+"')";
                stmt.executeUpdate(DBQ);
                
                JOptionPane.showMessageDialog(null,"The staff adding done");
                
                reFreshTable();
                
                jTextField9.setText("");
                jTextField12.setText("");
                jTextField13.setText("");
                jTextField10.setText("");
                jTextField14.setText("");
                jTextField29.setText("");
                jTextField16.setText("");
                
                jComboBox1.setSelectedIndex(0);
                jComboBox2.setSelectedIndex(0);
                
                ImageIcon i = new ImageIcon("src\\img\\000.png");
                Image im = i.getImage();
                Image fi = im.getScaledInstance(150,200, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(fi);
                imgChossed.setIcon(scaledIcon);
                
            }catch(HeadlessException | SQLException e){
                JOptionPane.showMessageDialog(null,"رمز الموظف مستخدم");
            }
            
        }
    }else{
         JOptionPane.showMessageDialog(null,"Please enter data in all fields");
    }
    
    
    }//GEN-LAST:event_jButton1MouseClicked

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseClicked
        if(evt.getClickCount() == 2 && !evt.isConsumed()){
            evt.consume();
            toDetailPanel();
        }
    }//GEN-LAST:event_tableMouseClicked

    private void table2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_table2MouseClicked
        if(evt.getClickCount() == 2 && !evt.isConsumed()){
            evt.consume();
            toUserDetailPanel();
        }      
    }//GEN-LAST:event_table2MouseClicked

    private void deleteButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteButtonMouseClicked
        
        int i = JOptionPane.showConfirmDialog(null, "Delete employee");
        
        if(i == 0){
        String ID = jTextField1.getText();
        try{
                
                String DBQ ="DELETE FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                stmt.executeUpdate(DBQ);
                JOptionPane.showMessageDialog(null, "Deleted");
                
                DBQ="DELETE FROM M7MAD.HOLIDAY WHERE ID='"+ID+"'";
                stmt.executeUpdate(DBQ);
                DBQ="DELETE FROM M7MAD.DELAY WHERE ID='"+ID+"'";
                stmt.executeUpdate(DBQ);
                DBQ="DELETE FROM M7MAD.ABSENCE WHERE ID='"+ID+"'";
                stmt.executeUpdate(DBQ);
                
                reFreshTable();
                admin_1TabelPanel.setVisible(true);
                admin_1DetailPanel.setVisible(false);
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
                
            }
        }
        
    }//GEN-LAST:event_deleteButtonMouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        boolean isRowSelected = holiday_tabel_1.isColumnSelected(0) | holiday_tabel_1.isColumnSelected(1) | holiday_tabel_1.isColumnSelected(2) | holiday_tabel_1.isColumnSelected(3);
        if(isRowSelected){
            int selectedRow = holiday_tabel_1.getSelectedRow();
            
            model = (DefaultTableModel) holiday_tabel_1.getModel();
            model.removeRow(selectedRow);
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        model = (DefaultTableModel) abcsence_tabel_1.getModel();
        model.addRow(new Object[]{"",today,currentAdmin});
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
         boolean isRowSelected = abcsence_tabel_1.isColumnSelected(0) | abcsence_tabel_1.isColumnSelected(1);
        if(isRowSelected){
            int selectedRow = abcsence_tabel_1.getSelectedRow();
            
            model = (DefaultTableModel) abcsence_tabel_1.getModel();
            model.removeRow(selectedRow);
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        model = (DefaultTableModel) lateness_tabel_1.getModel();
        model.addRow(new Object[]{"",today,"",currentAdmin});
    }//GEN-LAST:event_jButton6MouseClicked

    private void jButton7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton7MouseClicked
        boolean isRowSelected = lateness_tabel_1.isColumnSelected(0) | lateness_tabel_1.isColumnSelected(1) | lateness_tabel_1.isColumnSelected(2);
        if(isRowSelected){
            int selectedRow = lateness_tabel_1.getSelectedRow();
            
            model = (DefaultTableModel) lateness_tabel_1.getModel();
            model.removeRow(selectedRow);
        }
        else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton7MouseClicked

    private void updateDataButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_updateDataButtonMouseClicked
        String old_ID = jLabel40.getText();
        String new_ID = jTextField1.getText();
        String new_Name = jTextField2.getText();
        String new_Rank = jTextField30.getText();
        String new_birthday = jTextField6.getText();
        String new_birthplace = jTextField5.getText();
        String new_Interest = (String) jComboBox4.getSelectedItem();
        String new_Status = (String) jComboBox3.getSelectedItem();
        String new_phone = jTextField8.getText();
        String new_Nickname = jTextField3.getText();
        String new_Pic = jLabel39.getText();
        
        
        Boolean validation = true;
        String date_D;
        for(int i=0;i<abcsence_tabel_1.getRowCount();i++){
                    date_D =(String) abcsence_tabel_1.getValueAt(i, 1);
                    validation = validation && checkValidDate(date_D);
        }
        for(int i=0;i<holiday_tabel_1.getRowCount();i++){
                    date_D =(String) holiday_tabel_1.getValueAt(i, 1);
                    validation = validation && checkValidDate(date_D);
                    date_D =(String) holiday_tabel_1.getValueAt(i, 2);
                    validation = validation && checkValidDate(date_D);
        }
        for(int i=0;i<lateness_tabel_1.getRowCount();i++){
                    date_D =(String) lateness_tabel_1.getValueAt(i, 1);
                    validation = validation && checkValidDate(date_D);
        }
        
        if(checkValidDate(new_birthday) && validation){
            String DBQ = "UPDATE M7MAD.THESTAFFS "
                    + "SET ID='"+new_ID+"',"
                    + "NAME = '"+new_Name+"',"
                    + "NICKNAME = '"+new_Nickname+"',"
                    + "BIRTHPLACE = '"+new_birthplace+"',"
                    + "BIRTHDAY = '"+new_birthday+"',"
                    + "PHONENUMBER = '"+new_phone+"',"
                    + "RANK = '"+new_Rank+"',"
                    + "INTEREST = '"+new_Interest+"',"
                    + "STATUS = '"+new_Status+"',"
                    + "PICTUER = '"+new_Pic+"'"
                    + "WHERE ID='"+old_ID+"'";
            
            
            try{
                
                
                
                stmt.executeUpdate(DBQ);
                
                DBQ="SELECT * FROM M7MAD.HOLIDAY WHERE ID='"+old_ID+"'";
                rsHoliday = stmt.executeQuery(DBQ);
                rsHoliday.absolute(0);
                
                
                for(int x=0;x<holiday_tabel_1.getRowCount();x++) {  
                    
                    
                    if(rsHoliday.next()){
                        if(!rsHoliday.getBoolean("CHECKED")){
                        
                            if((Boolean)holiday_tabel_1.getValueAt(x, 0)){
                                
                                int selectedRow = abcsence_tabel_1.getRowCount();
                                if(selectedRow>0){
                                    boolean b1 = today.equals((String)abcsence_tabel_1.getValueAt(selectedRow-1, 1));
                                    boolean b2 = "The Automatic System".equals((String)abcsence_tabel_1.getValueAt(selectedRow-1, 2));
                                    
                                    if(b1 && b2){
                                        model = (DefaultTableModel) abcsence_tabel_1.getModel();
                                        model.removeRow(selectedRow-1);
                                    }
                                    
                                }
                                
                                String endDate = (String)holiday_tabel_1.getValueAt(x, 1);


                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
                                LocalDate dateBefore = LocalDate.parse(today, formatter);
                                LocalDate dateAfter = LocalDate.parse(endDate, formatter);
                                long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
                                
                                noOfDaysBetween++;
                                if(noOfDaysBetween<0){
                                    
                                    noOfDaysBetween = noOfDaysBetween*(-1);
                                    int i = JOptionPane.showConfirmDialog(null,"الموظف تاخر "+noOfDaysBetween+" يوم/أيام عن الالتحاق بالعمل هل تريد طباعة استفسار كتابي","Confirm Message",JOptionPane.YES_NO_OPTION);
                                    if(i==0){
                                        printWord(noOfDaysBetween);
                                    }
                                }    
                            }
                        }
                    }
                    
                }
                
                
                DBQ="DELETE FROM M7MAD.HOLIDAY WHERE ID='"+old_ID+"'";
                stmt.executeUpdate(DBQ);
                DBQ="DELETE FROM M7MAD.DELAY WHERE ID='"+old_ID+"'";
                stmt.executeUpdate(DBQ);
                DBQ="DELETE FROM M7MAD.ABSENCE WHERE ID='"+old_ID+"'";
                stmt.executeUpdate(DBQ);
                
                
                String id = new_ID;
                Boolean check;
                String end;
                String begining;
                String type;
                String byAdmin;
                for(int i=0;i<holiday_tabel_1.getRowCount();i++){
                    check =(Boolean) holiday_tabel_1.getValueAt(i, 0);
                    end =(String) holiday_tabel_1.getValueAt(i, 1);
                    begining =(String) holiday_tabel_1.getValueAt(i, 2);
                    type =(String) holiday_tabel_1.getValueAt(i, 3);
                    byAdmin =(String) holiday_tabel_1.getValueAt(i, 4);
                    
                    
                        DBQ="INSERT INTO M7MAD.HOLIDAY VALUES ('"+id+"','"+type+"','"+begining+"','"+end+"','"+check+"','"+byAdmin+"')";
                        stmt.executeUpdate(DBQ);
                        
                }
                
                String date;
                String time;
                String note;
                for(int i=0;i<lateness_tabel_1.getRowCount();i++){
                    note =(String) lateness_tabel_1.getValueAt(i, 0);
                    date =(String) lateness_tabel_1.getValueAt(i, 1);
                    time =(String) lateness_tabel_1.getValueAt(i, 2);
                    byAdmin =(String) lateness_tabel_1.getValueAt(i, 3);
                   
                    DBQ="INSERT INTO M7MAD.DELAY VALUES ('"+id+"','"+date+"','"+time+"','"+note+"','"+byAdmin+"')";
                    stmt.executeUpdate(DBQ);
                    
                    
                }
                
                
                for(int i=0;i<abcsence_tabel_1.getRowCount();i++){
                    note =(String) abcsence_tabel_1.getValueAt(i, 0);
                    date =(String) abcsence_tabel_1.getValueAt(i, 1);
                    byAdmin =(String) abcsence_tabel_1.getValueAt(i, 2);
                    
                    DBQ="INSERT INTO M7MAD.ABSENCE VALUES ('"+id+"','"+date+"','"+note+"','"+byAdmin+"')";
                    stmt.executeUpdate(DBQ);
                    
                }
                
                reFreshTable();
                reFreshStaffDetails(id);
                JOptionPane.showMessageDialog(null, "Done");
            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
                JOptionPane.showMessageDialog(null, "رمز الموظف مستخدم");
                
            }
        }
    }//GEN-LAST:event_updateDataButtonMouseClicked

    private void jButton8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton8MouseClicked
        boolean isRowSelected = holiday_tabel_1.isColumnSelected(0) | holiday_tabel_1.isColumnSelected(1) | holiday_tabel_1.isColumnSelected(2) | holiday_tabel_1.isColumnSelected(3);
        if(isRowSelected){
            int selectedRow = holiday_tabel_1.getSelectedRow();
            
            model = (DefaultTableModel) holiday_tabel_1.getModel();
            String byAdmin = (String) model.getValueAt(selectedRow,4);
            JOptionPane.showMessageDialog(null, byAdmin+" تم اضافة العطلة بواسطة");
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
        
    }//GEN-LAST:event_jButton8MouseClicked

    private void jButton11MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton11MouseClicked
        boolean isRowSelected = abcsence_tabel_1.isColumnSelected(0) | abcsence_tabel_1.isColumnSelected(1);
        if(isRowSelected){
            int selectedRow = abcsence_tabel_1.getSelectedRow();
            
            model = (DefaultTableModel) abcsence_tabel_1.getModel();
            String byAdmin = (String) model.getValueAt(selectedRow,2);
            JOptionPane.showMessageDialog(null,byAdmin+" تم تسجيل الغياب بواسطة ");
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton11MouseClicked

    private void jButton12MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton12MouseClicked
        boolean isRowSelected = lateness_tabel_1.isColumnSelected(0) | lateness_tabel_1.isColumnSelected(1) | lateness_tabel_1.isColumnSelected(2);
        if(isRowSelected){
            int selectedRow = lateness_tabel_1.getSelectedRow();
            
            model = (DefaultTableModel) lateness_tabel_1.getModel();
            String byAdmin = (String) model.getValueAt(selectedRow,3);
            JOptionPane.showMessageDialog(null, byAdmin+" تم تسجيل التأخير بواسطة");
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton12MouseClicked

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        boolean isRowSelected = lateness_tabel_3.isColumnSelected(0) | lateness_tabel_3.isColumnSelected(1) | lateness_tabel_3.isColumnSelected(2);
        if(isRowSelected){
            int selectedRow = lateness_tabel_3.getSelectedRow();
            
            model = (DefaultTableModel) lateness_tabel_3.getModel();
            String byAdmin = (String) model.getValueAt(selectedRow,3);
            JOptionPane.showMessageDialog(null, byAdmin+" تم تسجيل التأخير بواسطة");
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        boolean isRowSelected = abcsence_tabel_3.isColumnSelected(0) | abcsence_tabel_3.isColumnSelected(1);
        if(isRowSelected){
            int selectedRow = abcsence_tabel_3.getSelectedRow();
            
            model = (DefaultTableModel) abcsence_tabel_3.getModel();
            String byAdmin = (String) model.getValueAt(selectedRow,2);
            JOptionPane.showMessageDialog(null, byAdmin+" تم تسجيل الغياب بواسطة ");
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
       if(isFinish){
           String selectedItem = (String)jComboBox5.getSelectedItem();
            
           if("راحة تعويضية".equals(selectedItem)){
               
               try {
                String ID = jTextField1.getText();
                String DBQ = "SELECT * FROM M7MAD.THESTAFFS WHERE ID='"+ID+"'";
                rsStaff = stmt.executeQuery(DBQ);
                rsStaff.first();
                String name = rsStaff.getString("NAME");
                String NickName = rsStaff.getString("NICKNAME");
                String rest = rsStaff.getString("RESTDAYS");
                String sValue = JOptionPane.showInputDialog(null,"الموظف "+name+" "+NickName+" يمتلك "+rest+" يوم/أيام مؤجلة من أيام الراحة التعويضية \n قم بإدخال القيمة الجديدة لايام الراحة التعويضية المؤجلة          ","Confirm Message",JOptionPane.INFORMATION_MESSAGE);

                if(sValue!=null && !"".equals(sValue)){
                    int newValue = Integer.parseInt(sValue);
                    DBQ = "UPDATE M7MAD.THESTAFFS SET RESTDAYS ="+newValue+" WHERE ID='"+ID+"'";
                    stmt.executeUpdate(DBQ);
                    JOptionPane.showMessageDialog(null, "القيمة الجديدة لأيام الراحة التعويضية المؤجلة : "+newValue);
                }

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, ex);
                }

           }
           
           model = (DefaultTableModel) holiday_tabel_1.getModel();
           model.addRow(new Object[]{false,"","",selectedItem,currentAdmin});
       }
       isFinish = true;
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jComboBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox6ActionPerformed
        reFreshTable();
    }//GEN-LAST:event_jComboBox6ActionPerformed

    private void jButton13MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton13MouseClicked
        boolean isRowSelected = holiday_tabel_3.isColumnSelected(0) | holiday_tabel_3.isColumnSelected(1) | holiday_tabel_3.isColumnSelected(2) | holiday_tabel_3.isColumnSelected(3);
        if(isRowSelected){
            int selectedRow = holiday_tabel_3.getSelectedRow();
            
            model = (DefaultTableModel) holiday_tabel_3.getModel();
            String byAdmin = (String) model.getValueAt(selectedRow,4);
            JOptionPane.showMessageDialog(null, byAdmin+" تم اضافة العطلة بواسطة");
            
        }else{
            JOptionPane.showMessageDialog(null, "الرجاء تحديد عنصر");
        }
    }//GEN-LAST:event_jButton13MouseClicked

    private void jComboBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox7ActionPerformed
        reFreshTable();
    }//GEN-LAST:event_jComboBox7ActionPerformed

    private void goToPrintMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goToPrintMouseClicked
        printExcel(table);
    }//GEN-LAST:event_goToPrintMouseClicked

    private void goToPrint3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_goToPrint3MouseClicked
        printExcel(table2);
    }//GEN-LAST:event_goToPrint3MouseClicked

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        
        String name = jTextField7.getText();
        search(name,table);
        
    }//GEN-LAST:event_jButton2MouseClicked

    private void jButton9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton9MouseClicked
        String name = jTextField11.getText();
        search(name,table2);
    }//GEN-LAST:event_jButton9MouseClicked

    public void search(String name , JTable stable) {
        
        if(!"".equals(name)){
            
            stable.setRowSelectionAllowed(true);
            stable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            String rowC = "";
            int x = stable.getRowCount();
            boolean found = false;


            for(int i= lastSelect+1;i<x;i++){
                rowC = (String) stable.getValueAt(i, 6);

                if(rowC.contains(name)){

                    stable.setRowSelectionInterval(i, i);

                    lastSelect=i;
                    
                    JViewport viewport = (JViewport)stable.getParent();
                    Rectangle rect = stable.getCellRect(0, 7, true);
                    Point pt = viewport.getViewPosition();
                    rect.setLocation(rect.x-pt.x, rect.y-pt.y);
                    stable.scrollRectToVisible(rect);
                    
                    viewport = (JViewport)stable.getParent();
                    rect = stable.getCellRect(i, 7, true);
                    pt = viewport.getViewPosition();
                    rect.setLocation(rect.x-pt.x, rect.y-pt.y);
                    stable.scrollRectToVisible(rect);

                    found = true;

                    break;

                }

            }

            if(!found){
                    lastSelect = -1;
                    search(name , stable);
                }
            
        }
        
    }
    public void printWord(long noOfDaysBetween) {
        
        String rank = jTextField30.getText();
        String name = jTextField2.getText();
        String nickname = jTextField3.getText();
        
        try{
            
            XWPFDocument document = new XWPFDocument();
            FileOutputStream out = new FileOutputStream(new File("Result.docx"));
            
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            run.setFontSize(20);
            run.setBold(true);
            run.setSubscript(VerticalAlign.SUBSCRIPT);run.setText("الجمهورية الجزائرية الديمقراطية الشعبية");
            run.addBreak();
            run.setText("وزارة العدل");
            run.addBreak();
            
            XWPFParagraph paragraph_2 = document.createParagraph();
            XWPFRun run_2 = paragraph_2.createRun();
            paragraph_2.setAlignment(ParagraphAlignment.LEFT);
            run_2.setFontSize(20);
            run_2.setBold(true);
            run_2.setSubscript(VerticalAlign.SUBSCRIPT);
            run_2.setText("المديرية العامة لإدارة السجون وإعادة الإدماج");
            run_2.addBreak();
            run_2.setText("مؤسسة إعادة التربية والتأهيل بالمنيعة");
            run_2.addBreak();
            run_2.setText("الرقم:....../2020/م إ ع/ م إت ت                                           المنيعة في  "+today);
            run_2.addBreak();
            
            XWPFParagraph paragraph_3 = document.createParagraph();
            XWPFRun run_3 = paragraph_3.createRun();
            paragraph_3.setAlignment(ParagraphAlignment.CENTER);
            run_3.setFontSize(40);
            run_3.setFontFamily("Arabic Typesetting");
            run_3.setBold(true);
            run_3.setSubscript(VerticalAlign.SUBSCRIPT);
            run_3.setText("استفسار كتـــابي ");
            run_3.addBreak();
            
            XWPFParagraph paragraph_4 = document.createParagraph();
            XWPFRun run_4 = paragraph_4.createRun();
            paragraph_4.setAlignment(ParagraphAlignment.LEFT);
            run_4.setFontSize(20);
            run_4.setBold(true);
            run_4.setSubscript(VerticalAlign.SUBSCRIPT);
            run_4.setText("إستفسار السيد )ة: ( "+name +" "+nickname);
            run_4.addBreak();
            run_4.setText("الرتبة   :"+rank);
            run_4.addBreak();
            
             XWPFTable table = document.createTable(2,2);
             
              widthCellsAcrossRow(table, 0, 0, 5000);
              widthCellsAcrossRow(table, 0, 1, 5000);
             
             table.getRow(1).setHeight((int)(40000*1/10));
             for(int x = 0;x < table.getNumberOfRows(); x++){
                XWPFTableRow row = table.getRow(x);
                
                
                int numberOfCell = row.getTableCells().size();
                    for(int y = 0; y < numberOfCell ; y++){
                    
                    XWPFParagraph p = row.getCell(y).addParagraph();
                    if(x==0 && y == 0){
                        
                        p.setAlignment(ParagraphAlignment.CENTER);
                        setRun(p.createRun() , "Calibre LIght" , 20 , "2b5079" ,"الجواب", false, false);
                      
                    }
                    else if(x==0 && y == 1){
                        
                        p.setAlignment(ParagraphAlignment.CENTER);
                        setRun(p.createRun() , "Calibre LIght" , 20 , "2b5079" ,"السؤال", false, false);
                        
                    }
                    else if(x==1 && y == 1){
                        
                        p.setAlignment(ParagraphAlignment.LEFT);
                        setRun(p.createRun() , "Calibre LIght" , 16 , "2b5079" ," بماذا تفسر بغيابك لمدة "+noOfDaysBetween+" ايام ؟", false, false);
                        
                    }
                    else{
                        
                        p.setAlignment(ParagraphAlignment.LEFT);
                        setRun(p.createRun() , "Calibre LIght" , 20 , "2b5079" ,"", false, false);
                        
                    }

                } 
            }
            
            
            
            XWPFParagraph paragraph_5 = document.createParagraph();
            XWPFRun run_5 = paragraph_5.createRun();
            paragraph_5.setAlignment(ParagraphAlignment.LEFT);
            run_5.setFontSize(20);
            run_5.setBold(true);
            run_5.setSubscript(VerticalAlign.SUBSCRIPT);
            run_5.addBreak();
            run_5.setText("إمضاء المعني                                                                                  مدير المؤسسة");
            run_5.addBreak();
            
            
            
            document.write(out);
            out.close();
            
            Desktop desktop = Desktop.getDesktop();
            desktop.open(new File("Result.docx"));
            
            
        }
        catch(Exception e){
            System.out.println(e);
        }
        
        
    }
    
    
    public static void widthCellsAcrossRow (XWPFTable table, int rowNum, int colNum, int width) {
        XWPFTableCell cell = table.getRow(rowNum).getCell(colNum);
        if (cell.getCTTc().getTcPr() == null)
            cell.getCTTc().addNewTcPr();
        if (cell.getCTTc().getTcPr().getTcW()==null)
            cell.getCTTc().getTcPr().addNewTcW();
        cell.getCTTc().getTcPr().getTcW().setW(BigInteger.valueOf((long) width));
    }
    
    
     public static void setRun (XWPFRun run , String fontFamily , int fontSize , String colorRGB , String text , boolean bold , boolean addBreak) {
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);
        //run.setColor(colorRGB);
        run.setText(text);
        run.setBold(bold);
        if (addBreak) run.addBreak();
    }
    
    
   
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel URLChossed;
    private javax.swing.JPanel abcsence_panel_1;
    private javax.swing.JPanel abcsence_panel_3;
    private javax.swing.JTable abcsence_tabel_1;
    private javax.swing.JTable abcsence_tabel_3;
    private javax.swing.JLabel absence_Button_1;
    private javax.swing.JLabel absence_Button_3;
    private javax.swing.JPanel addStaffPanel;
    private javax.swing.JPanel admin_1DetailPanel;
    private javax.swing.JLabel admin_1Enter;
    private javax.swing.JPanel admin_1TabelPanel;
    private javax.swing.JLabel backToAdmin_1Tabel;
    private javax.swing.JLabel backToAdmin_1Tabel_2;
    private javax.swing.JLabel backToLogin;
    private javax.swing.JLabel backToLogin3;
    private javax.swing.JLabel backToUserTabel;
    private javax.swing.JLabel changePassButton;
    private javax.swing.JLabel deleteButton;
    private javax.swing.JLabel goToDetail;
    private javax.swing.JLabel goToDetail3;
    private javax.swing.JLabel goToPrint;
    private javax.swing.JLabel goToPrint3;
    private javax.swing.JLabel holiday_Button_1;
    private javax.swing.JLabel holiday_Button_3;
    private javax.swing.JPanel holiday_panel_1;
    private javax.swing.JPanel holiday_panel_3;
    private javax.swing.JTable holiday_tabel_1;
    private javax.swing.JTable holiday_tabel_3;
    private javax.swing.JButton imgChooserButton;
    private javax.swing.JLabel imgChossed;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField22;
    private javax.swing.JTextField jTextField23;
    private javax.swing.JTextField jTextField24;
    private javax.swing.JTextField jTextField25;
    private javax.swing.JTextField jTextField26;
    private javax.swing.JTextField jTextField29;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField30;
    private javax.swing.JTextField jTextField31;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JPanel lateness_panel_1;
    private javax.swing.JPanel lateness_panel_3;
    private javax.swing.JTable lateness_tabel_1;
    private javax.swing.JTable lateness_tabel_3;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JLabel newStaff;
    private javax.swing.JLabel staffPicLabel;
    private javax.swing.JLabel staffPicLabel3;
    private javax.swing.JTable table;
    private javax.swing.JTable table2;
    private javax.swing.JLabel updateDataButton;
    private javax.swing.JPanel userDetailPanel;
    private javax.swing.JButton userEnter;
    private javax.swing.JButton userEnter1;
    private javax.swing.JTextField userNameField;
    private javax.swing.JPasswordField userPasswordField;
    private javax.swing.JPanel userTabelPanel;
    // End of variables declaration//GEN-END:variables
}
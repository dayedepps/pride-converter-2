/*
 * Created by JFormDesigner on Wed Feb 29 11:50:59 GMT 2012
 */

package uk.ac.ebi.pride.tools.converter.gui.forms;

import psidev.psi.tools.validator.MessageLevel;
import psidev.psi.tools.validator.ValidatorException;
import psidev.psi.tools.validator.ValidatorMessage;
import uk.ac.ebi.pride.tools.converter.gui.component.list.ShortFilePathListCellRenderer;
import uk.ac.ebi.pride.tools.converter.gui.component.table.ShortFilePathStringRenderer;
import uk.ac.ebi.pride.tools.converter.gui.component.table.TableCopyAction;
import uk.ac.ebi.pride.tools.converter.gui.component.table.TablePasteAction;
import uk.ac.ebi.pride.tools.converter.gui.model.ConverterData;
import uk.ac.ebi.pride.tools.converter.gui.model.FileBean;
import uk.ac.ebi.pride.tools.converter.gui.model.GUIException;
import uk.ac.ebi.pride.tools.converter.gui.util.IOUtilities;
import uk.ac.ebi.pride.tools.converter.report.io.ReportReaderDAO;
import uk.ac.ebi.pride.tools.converter.utils.config.Configurator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

/**
 * @author User #3
 */
public class SpectrumFileMappingForm extends AbstractForm {


    public SpectrumFileMappingForm() {
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        scrollPane1 = new JScrollPane();
        mappingTable = new JTable();

        //======== this ========

        //======== scrollPane1 ========
        {
            scrollPane1.setViewportView(mappingTable);
        }

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                .addContainerGap())
        );
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    private JTable mappingTable;
    // JFormDesigner - End of variables declaration  //GEN-END:variables


    @Override
    public Collection<ValidatorMessage> validateForm() throws ValidatorException {

        Collection<ValidatorMessage> msgs = new ArrayList<ValidatorMessage>();
        DefaultTableModel model = (DefaultTableModel) mappingTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 1) == null || "".equals(model.getValueAt(i, 1))) {
                msgs.add(new ValidatorMessage("No spectrum file set for " + model.getValueAt(i, 0).toString(), MessageLevel.WARN));
            }
        }
        return msgs;

    }

    @Override
    public void clear() {
        //reset table data model
        updateTableModel();
    }

    @Override
    public void save(ReportReaderDAO dao) {
        /* no op */
    }

    @Override
    public void load(ReportReaderDAO dao) {
        /* no op */
    }

    @Override
    public String getFormName() {
        return "File Selection - Spectra";
    }

    @Override
    public String getFormDescription() {
        return config.getString("fileselectionextra.form.description");
    }

    @Override
    public Icon getFormIcon() {
        return getFormIcon("fileselectionextra.form.icon");
    }

    @Override
    public String getHelpResource() {
        return "help.ui.fileextra";
    }

    @Override
    public void start() {
        //update table model for file mapping
        updateTableModel();
        //update validation listener
        validationListerner.fireValidationListener(true);
    }

    private void updateTableModel() {

        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        for (FileBean fileBean : ConverterData.getInstance().getDataFiles()) {
            Vector<Object> row = new Vector<Object>();
            row.add(fileBean.getInputFile());
            data.add(row);
        }

        Vector<Object> headers = new Vector<Object>();
        headers.add("Input File");
        headers.add("Spectrum File");

        mappingTable.setModel(new DefaultTableModel(data, headers) {

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                //only spectrum file will be editable
                return columnIndex == 1;
            }
        });

        //update table columns
        TableColumn col;

        //first column should render short file paths
        col = mappingTable.getColumnModel().getColumn(0);
        col.setCellRenderer(new ShortFilePathStringRenderer());

        //make sure that the spectrum files are sorted alphabetically
        Collections.sort(ConverterData.getInstance().getSpectrumFiles());
        //second column should render combobox for selection
        col = mappingTable.getColumnModel().getColumn(1);
        col.setCellEditor(new SpectrumComboBoxEditor(ConverterData.getInstance().getSpectrumFiles()));
        // If the cell should appear like a combobox in its
        // non-editing state, also set the combobox renderer
        col.setCellRenderer(new SpectrumComboBoxRenderer(ConverterData.getInstance().getSpectrumFiles()));

        mappingTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        //register keystrokes
        if (Configurator.getOSName().toLowerCase().contains("mac")) {
            mappingTable.getInputMap().put(TableCopyAction.MAC_COPY_KEYSTROKE, TableCopyAction.COPY);
            mappingTable.getActionMap().put(TableCopyAction.COPY, new TableCopyAction(mappingTable));
            mappingTable.getInputMap().put(TablePasteAction.MAC_PASTE_KEYSTROKE, TablePasteAction.PASTE);
            mappingTable.getActionMap().put(TablePasteAction.PASTE, new TablePasteAction(mappingTable));
        } else {
            mappingTable.getInputMap().put(TableCopyAction.COPY_KEYSTROKE, TableCopyAction.COPY);
            mappingTable.getActionMap().put(TableCopyAction.COPY, new TableCopyAction(mappingTable));
            mappingTable.getInputMap().put(TablePasteAction.PASTE_KEYSTROKE, TablePasteAction.PASTE);
            mappingTable.getActionMap().put(TablePasteAction.PASTE, new TablePasteAction(mappingTable));
        }

        mappingTable.setRowSelectionAllowed(true);
        mappingTable.setColumnSelectionAllowed(true);

    }

    @Override
    public void finish() throws GUIException {
        //update spectrum file mapping
        DefaultTableModel model = (DefaultTableModel) mappingTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String inputFile = model.getValueAt(i, 0).toString();
            String spectrumFile = (String) model.getValueAt(i, 1);
            if (spectrumFile != null && !"".equals(spectrumFile)) {
                //update file bean
                ConverterData.getInstance().getFileBeanByInputFileName(inputFile).setSpectrumFile(spectrumFile);
            }
        }

        //generate report files
        IOUtilities.generateReportFiles(ConverterData.getInstance().getOptions(), ConverterData.getInstance().getDataFiles(), true, true);

    }

    private class SpectrumComboBoxEditor extends DefaultCellEditor {

        public SpectrumComboBoxEditor(Collection<String> spectrumFiles) {
            super(new JComboBox(spectrumFiles.toArray()));
            setClickCountToStart(1);
            ((JComboBox) editorComponent).setRenderer(new ShortFilePathListCellRenderer());
        }
    }

    private class SpectrumComboBoxRenderer extends JComboBox implements TableCellRenderer {
        public SpectrumComboBoxRenderer(Collection<String> spectrumFiles) {
            super(spectrumFiles.toArray());
            setRenderer(new ShortFilePathListCellRenderer());
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setOpaque(false);
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setOpaque(true);
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }

}

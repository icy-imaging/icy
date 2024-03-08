/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package icy.action;

import icy.clipboard.Clipboard;
import icy.file.FileUtil;
import icy.file.xls.XLSXUtil;
import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.dialog.OpenDialog;
import icy.gui.dialog.SaveDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.main.MainFrame;
import icy.gui.toolbar.panel.RoisPanel;
import icy.main.Icy;
import icy.roi.*;
import icy.sequence.Sequence;
import icy.sequence.SequenceDataIterator;
import icy.sequence.edit.ROIAddSequenceEdit;
import icy.sequence.edit.ROIAddsSequenceEdit;
import icy.sequence.edit.ROIReplacesSequenceEdit;
import icy.system.IcyHandledException;
import icy.system.SystemUtil;
import icy.type.DataIteratorUtil;
import icy.type.dimension.Dimension3D;
import icy.util.ClassUtil;
import icy.util.ShapeUtil.BooleanOperator;
import icy.util.StringUtil;
import icy.util.XMLUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import plugins.kernel.roi.roi2d.ROI2DPoint;
import plugins.kernel.roi.roi2d.ROI2DRectangle;
import plugins.kernel.roi.roi3d.ROI3DBox;
import plugins.kernel.roi.roi4d.ROI4DStackRectangle;
import plugins.kernel.roi.roi5d.ROI5DStackRectangle;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Roi actions (open / save / copy / paste / merge...)
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class RoiActions {
    public static final String DEFAULT_ROI_DIR = "roi";
    public static final String DEFAULT_ROI_NAME = "roi.xml";

    public record SequenceRoiList(Sequence sequence, List<ROI> rois) {
    }

    public static final IcyAbstractAction loadAction = new IcyAbstractAction(
            "Load ROI(s)...",
            "Load ROI(s) from file",
            "Load ROI(s) from a XML file and add them to the active sequence"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final String filename = OpenDialog.chooseFile("Load ROI(s)...", DEFAULT_ROI_DIR, DEFAULT_ROI_NAME);
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((filename != null) && (sequence != null)) {
                final Document doc = XMLUtil.loadDocument(filename);

                if (doc != null) {
                    final List<ROI> rois = ROI.loadROIsFromXML(XMLUtil.getRootElement(doc));

                    sequence.addROIs(rois, false);

                    // add to undo manager
                    sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, rois) {
                        @Override
                        public String getPresentationName() {
                            if (getROIs().size() > 1)
                                return "ROIs loaded from XML file";

                            return "ROI loaded from XML file";
                        }
                    });

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction saveAction = new IcyAbstractAction(
            "Save ROI(s) as...",
            "Save selected ROI(s) to file",
            "Save the selected ROI(s) from active sequence into an XML file"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final String filename = SaveDialog.chooseFile("Save ROI(s)...", DEFAULT_ROI_DIR, DEFAULT_ROI_NAME);
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((filename != null) && (sequence != null)) {
                final List<ROI> rois = sequence.getSelectedROIs();

                if (rois.size() > 0) {
                    final Document doc = XMLUtil.createDocument(true);

                    if (doc != null) {
                        ROI.saveROIsToXML(XMLUtil.getRootElement(doc), rois);
                        XMLUtil.saveDocument(doc, filename);
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence seq = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (seq != null);
        }
    };

    public static final IcyAbstractAction copyAction = new IcyAbstractAction(
            "Copy",
            "Copy selected ROI to clipboard (Ctrl+C)",
            KeyEvent.VK_C,
            SystemUtil.getMenuCtrlMaskEx()
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI> rois = sequence.getSelectedROIs();

                if (rois.size() > 0) {
                    // need to get a copy of the ROI (as it can change meanwhile)
                    for (int i = 0; i < rois.size(); i++) {
                        final ROI roi = rois.get(i).getCopy();

                        if (roi != null)
                            rois.set(i, roi);
                    }

                    // save in the Icy clipboard
                    Clipboard.put(Clipboard.TYPE_SEQUENCEROILIST, new SequenceRoiList(sequence, rois));
                    // clear system clipboard
                    Clipboard.clearSystem();

                    pasteAction.setEnabled(true);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            return super.isEnabled() && (sequence != null) && (sequence.getSelectedROIs().size() > 0);
        }
    };

    public static final IcyAbstractAction copyLinkAction = new IcyAbstractAction(
            "Copy link",
            "Copy link of selected ROI to clipboard (Alt+C)",
            KeyEvent.VK_C,
            InputEvent.ALT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI> rois = sequence.getSelectedROIs();

                if (rois.size() > 0) {
                    // save in the Icy clipboard
                    Clipboard.put(Clipboard.TYPE_ROILINKLIST, rois);
                    // clear system clipboard
                    Clipboard.clearSystem();

                    pasteLinkAction.setEnabled(true);

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            return super.isEnabled() && (sequence != null) && (sequence.getSelectedROIs().size() > 0);
        }
    };

    public static final IcyAbstractAction pasteAction = new IcyAbstractAction(
            "Paste",
            "Paste ROI from clipboard (Ctrl+V)",
            KeyEvent.VK_V,
            SystemUtil.getMenuCtrlMaskEx()
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final SequenceRoiList sequenceRoiList = (SequenceRoiList) Clipboard.get(Clipboard.TYPE_SEQUENCEROILIST);
                if (sequenceRoiList != null) {
                    final Sequence sequenceSrc = sequenceRoiList.sequence;
                    final List<ROI> rois = sequenceRoiList.rois;

                    if ((rois != null) && (rois.size() > 0)) {
                        try {
                            final List<ROI> copyRois = new ArrayList<>();

                            sequence.beginUpdate();
                            try {
                                // unselect all rois
                                sequence.setSelectedROI(null);

                                // add copy to sequence (so we can do the paste operation severals time)
                                for (final ROI roi : rois) {
                                    // final ROI newROI = roi.getCopy();
                                    final ROI newROI = ROIUtil.adjustToSequence(roi, sequenceSrc, sequence, true, true, true);

                                    if (newROI != null) {
                                        copyRois.add(newROI);

                                        // select the ROI
                                        newROI.setSelected(true);
                                        // and add it
                                        sequence.addROI(newROI);
                                    }
                                }
                            }
                            finally {
                                sequence.endUpdate();
                            }

                            // add to undo manager
                            sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, copyRois) {
                                @Override
                                public String getPresentationName() {
                                    if (getROIs().size() > 1)
                                        return "ROIs added from clipboard";

                                    return "ROI added from clipboard";
                                }
                            });

                            return true;
                        }
                        catch (final InterruptedException ie) {
                            new FailedAnnounceFrame("ROI(s) paste operation canceled !");
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null) && Clipboard.getType().equals(Clipboard.TYPE_SEQUENCEROILIST);
        }
    };

    public static final IcyAbstractAction pasteLinkAction = new IcyAbstractAction(
            "Paste link",
            "Paste ROI link from clipboard (Alt+V)",
            KeyEvent.VK_V,
            InputEvent.ALT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                // TODO: 17/02/2023 Checked if this code do the same
                //@SuppressWarnings("unchecked")
                //final List<ROI> rois = (List<ROI>) Clipboard.get(Clipboard.TYPE_ROILINKLIST);
                final Object obj = Clipboard.get(Clipboard.TYPE_ROILINKLIST);
                if (obj instanceof final List<?> list) {
                    final List<ROI> rois = new ArrayList<>();

                    //if ((rois != null) && (rois.size() > 0)) {
                    if ((list.size() > 0)) {
                        sequence.beginUpdate();
                        try {

                            // add to sequence
                            /*for (ROI roi : rois)
                                sequence.addROI(roi);*/
                            for (final Object o : list) {
                                if (o instanceof ROI) {
                                    sequence.addROI((ROI) o);
                                    rois.add((ROI) o);
                                }
                            }
                        }
                        finally {
                            sequence.endUpdate();
                        }

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, rois) {
                            @Override
                            public String getPresentationName() {
                                if (getROIs().size() > 1)
                                    return "ROIs linked from clipboard";

                                return "ROI linked from clipboard";
                            }
                        });

                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null)
                    && Clipboard.getType().equals(Clipboard.TYPE_ROILINKLIST);
        }
    };

    // public static IcyAbstractAction clearClipboardAction = new IcyAbstractAction("Clear", new
    // IcyIcon(
    // ResourceUtil.ICON_CLIPBOARD_CLEAR), "Remove ROI saved in clipboard")
    // {
    // /**
    // *
    // */
    // private static final long serialVersionUID = 4878585451006567513L;
    //
    // @Override
    // public boolean doAction(ActionEvent e)
    // {
    // Clipboard.remove(ID_ROI_COPY_CLIPBOARD, false);
    // pasteAction.setEnabled(false);
    // }
    //
    // @Override
    // public boolean isEnabled()
    // {
    // return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null)
    // && Clipboard.hasObjects(RoiActions.ID_ROI_COPY_CLIPBOARD, false);
    // }
    // };

    public static final IcyAbstractAction selectAllAction = new IcyAbstractAction(
            "SelectAll",
            "Select all ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                sequence.setSelectedROIs(sequence.getROIs());
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && (sequence.getROIs().size() > 0);
        }
    };

    public static final IcyAbstractAction unselectAction = new IcyAbstractAction(
            "Unselect",
            "Unselect ROI(s)",
            KeyEvent.VK_ESCAPE
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                sequence.setSelectedROI(null);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction deleteAction = new IcyAbstractAction(
            "Delete",
            "Delete selected ROI(s)",
            "Delete selected ROI(s) from the active sequence",
            KeyEvent.VK_DELETE,
            0
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                sequence.removeSelectedROIs(true, true);
                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            return super.isEnabled() && (sequence != null) && (sequence.getSelectedROIs().size() > 0);
        }
    };

    public static final IcyAbstractAction boolNotAction = new IcyAbstractAction(
            "Inversion",
            "Boolean inversion operation",
            "Create a new ROI representing the inverse of selected ROI",
            true,
            "Computing inverse..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if ((sequence != null)) {
                    // NOT operation
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROI = sequence.getSelectedROIs();

                        // work only on single ROI
                        if (selectedROI.size() != 1)
                            return false;

                        final ROI roi = selectedROI.get(0);
                        final ROI seqRoi;

                        switch (roi.getDimension()) {
                            case 2:
                                final ROI2D roi2d = (ROI2D) roi;
                                final ROI2DRectangle seqRoi2d = new ROI2DRectangle(sequence.getBounds2D());
                                // set on same position
                                seqRoi2d.setZ(roi2d.getZ());
                                seqRoi2d.setT(roi2d.getT());
                                seqRoi2d.setC(roi2d.getC());
                                seqRoi = seqRoi2d;
                                break;

                            case 3:
                                final ROI3D roi3d = (ROI3D) roi;
                                final ROI3DBox seqRoi3d = new ROI3DBox(sequence.getBounds5D().toRectangle3D());
                                // set on same position
                                seqRoi3d.setT(roi3d.getT());
                                seqRoi3d.setC(roi3d.getC());
                                seqRoi = seqRoi3d;
                                break;

                            case 4: // FIXME Remove this
                                final ROI4D roi4d = (ROI4D) roi;
                                final ROI4DStackRectangle seqRoi4d = new ROI4DStackRectangle(sequence.getBounds5D().toRectangle4D());
                                // set on same position
                                seqRoi4d.setC(roi4d.getC());
                                seqRoi = seqRoi4d;
                                break;

                            case 5: // FIXME Remove this
                                seqRoi = new ROI5DStackRectangle(sequence.getBounds5D());
                                break;

                            default:
                                seqRoi = null;
                                break;
                        }

                        if (seqRoi != null) {
                            // we do the NOT operation by subtracting current ROI to sequence bounds ROI
                            final ROI mergeROI = ROIUtil.subtract(seqRoi, roi);

                            if (mergeROI != null) {
                                mergeROI.setName("Inverse");

                                sequence.addROI(mergeROI);
                                sequence.setSelectedROI(mergeROI);

                                // add to undo manager
                                sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Inverse"));
                            }
                        }
                        else
                            MessageDialog.showDialog("Operation not supported", "Input ROI has incorrect dimension !", MessageDialog.ERROR_MESSAGE);
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI inversion operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction boolOrAction = new IcyAbstractAction(
            "Union",
            "Boolean union operation",
            "Create a new ROI representing the union of selected ROIs",
            true,
            "Computing union..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((sequence != null)) {
                // OR operation
                sequence.beginUpdate();
                try {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final ROI mergeROI = ROIUtil.getUnion(selectedROIs);

                    if (mergeROI != null) {
                        mergeROI.setName("Union");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Union"));
                    }
                }
                catch (final InterruptedException e1) {
                    MessageDialog.showDialog("Operation interrupted", e1.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                }
                finally {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction boolAndAction = new IcyAbstractAction(
            "Intersection",
            "Boolean intersection operation",
            "Create a new ROI representing the intersection of selected ROIs",
            true,
            "Computing intersection..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((sequence != null)) {
                // AND operation
                sequence.beginUpdate();
                try {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final ROI mergeROI = ROIUtil.getIntersection(selectedROIs);

                    if (mergeROI != null) {
                        mergeROI.setName("Intersection");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Intersection"));
                    }
                }
                catch (final InterruptedException e1) {
                    MessageDialog.showDialog("Operation interrupted", e1.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                }
                finally {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction boolXorAction = new IcyAbstractAction(
            "Exclusive Union",
            "Boolean exclusive union operation",
            "Create a new ROI representing the exclusive union of selected ROIs",
            true,
            "Computing exclusive union..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if ((sequence != null)) {
                // XOR operation
                sequence.beginUpdate();
                try {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final ROI mergeROI = ROIUtil.getExclusiveUnion(selectedROIs);

                    if (mergeROI != null) {
                        mergeROI.setName("Exclusive union");

                        sequence.addROI(mergeROI);
                        sequence.setSelectedROI(mergeROI);

                        // add to undo manager
                        sequence.addUndoableEdit(new ROIAddSequenceEdit(sequence, mergeROI, "ROI Exclusive Union"));
                    }
                }
                catch (final InterruptedException e1) {
                    MessageDialog.showDialog("Operation interrupted", e1.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                }
                finally {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction boolSubtractAction = new IcyAbstractAction(
            "Subtraction",
            "Boolean subtraction",
            "Create 2 ROIs representing the result of (A - B) and (B - A)",
            true,
            "Computing subtraction..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if ((sequence != null)) {
                    // SUB operation
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROI = sequence.getSelectedROIs();
                        final List<ROI> generatedROIs = new ArrayList<>();

                        // Subtraction work only when 2 ROI are selected
                        if (selectedROI.size() != 2)
                            return false;

                        final ROI subtractAB = ROIUtil.subtract(selectedROI.get(0), selectedROI.get(1));
                        final ROI subtractBA = ROIUtil.subtract(selectedROI.get(1), selectedROI.get(0));

                        subtractAB.setName("Subtract A-B");
                        subtractBA.setName("Subtract B-A");

                        generatedROIs.add(subtractAB);
                        generatedROIs.add(subtractBA);

                        sequence.beginUpdate();
                        try {
                            for (final ROI roi : generatedROIs)
                                sequence.addROI(roi);

                            sequence.setSelectedROIs(generatedROIs);

                            // add to undo manager
                            sequence.addUndoableEdit(new ROIAddsSequenceEdit(sequence, generatedROIs, "ROI Subtraction"));
                        }
                        finally {
                            sequence.endUpdate();
                        }
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI subtraction operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction fillInteriorAction = new IcyAbstractAction(
            "Fill Interior",
            "Fill ROI(s) interior",
            "Fill interior of the selected ROI(s) with specified value",
            true,
            "Fill ROI(s) interior..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null) {
                    //final double value = mainFrame.getMainRibbon().getROIRibbonTask().getFillValue();
                    final double value = 1d; // TODO: 23/01/2023 Change this when ROI toolbar is set

                    try {
                        // create undo point
                        final boolean canUndo = sequence.createUndoDataPoint("ROI fill interior");

                        // cannot backup
                        if (!canUndo) {
                            // ask confirmation to continue
                            if (!IdConfirmDialog.confirm(
                                    "Not enough memory to undo the operation, do you want to continue ?",
                                    "ROIFillInteriorConfirm"
                            ))
                                return false;
                        }

                        for (final ROI roi : sequence.getSelectedROIs())
                            DataIteratorUtil.set(new SequenceDataIterator(sequence, roi, true), value);

                        sequence.dataChanged();

                        // no undo, clear undo manager after modification
                        if (!canUndo)
                            sequence.clearUndoManager();
                    }
                    catch (final InterruptedException e1) {
                        MessageDialog.showDialog("Operation interrupted", e1.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                    }

                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static final IcyAbstractAction fillExteriorAction = new IcyAbstractAction(
            "Fill Exterior",
            "Fill ROI(s) exterior",
            "Fill exterior of the selected ROI(s) with specified value",
            true,
            "Fill ROI(s) exterior..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null) {
                    //final double value = mainFrame.getMainRibbon().getROIRibbonTask().getFillValue();
                    final double value = 0d; // TODO: 23/01/2023 Change this when ROI toolbar is set
                    boolean canUndo = false;

                    try {
                        // create undo point
                        canUndo = sequence.createUndoDataPoint("ROI fill exterior");

                        // cannot backup
                        if (!canUndo) {
                            // ask confirmation to continue
                            if (!IdConfirmDialog.confirm(
                                    "Not enough memory to undo the operation, do you want to continue ?",
                                    "ROIFillExteriorConfirm"))
                                return false;
                        }

                        final ROI roiUnion = ROIUtil.merge(sequence.getSelectedROIs(), BooleanOperator.OR);
                        //final ROI roiSeq = new ROI5DStackRectangle(sequence.getBounds5D()); // TODO check if does the same
                        final ROI roiSeq = new ROI3DBox(sequence.getBounds5D().toRectangle3D());
                        final ROI roi = roiSeq.getSubtraction(roiUnion);

                        DataIteratorUtil.set(new SequenceDataIterator(sequence, roi), value);

                        sequence.dataChanged();

                        // no undo, clear undo manager after modification
                        if (!canUndo)
                            sequence.clearUndoManager();

                        return true;
                    }
                    catch (final InterruptedException e1) {
                        // undo operation if possible
                        if (canUndo)
                            sequence.undo();

                        MessageDialog.showDialog("Operation interrupted", e1.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);
                    }
                    catch (final UnsupportedOperationException ex) {
                        // undo operation if possible
                        if (canUndo)
                            sequence.undo();

                        MessageDialog.showDialog("Operation not supported", ex.getLocalizedMessage(), MessageDialog.ERROR_MESSAGE);

                        return false;
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null) && !sequence.isEmpty();
        }
    };

    public static final IcyAbstractAction xlsExportAction = new IcyAbstractAction(
            "Export ROI(s) as Excel...",
            "ROI Excel export",
            "Export the content of the ROI table into a XLS/CSV file",
            true,
            "Exporting ROI informations..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();
            final RoisPanel roisPanel = RoisPanel.getInstance();
            // FIXME It's not a good idea to get GUI object, we should get saved preferences

            if ((sequence != null) && (roisPanel != null)) {
                final String content = roisPanel.getCSVFormattedInfos();

                if (StringUtil.isEmpty(content) || roisPanel.getVisibleRois().isEmpty()) {
                    MessageDialog.showDialog("Nothing to export !", MessageDialog.INFORMATION_MESSAGE);
                    return true;
                }

                final String filename = SaveDialog.chooseFileForResult("Export ROIs...", "result", XLSXUtil.FILE_DOT_EXTENSION);

                if (filename != null) {
                    try {
                        // CSV format wanted ?
                        if (!FileUtil.getFileExtension(filename, false).toLowerCase().startsWith(XLSXUtil.FILE_EXTENSION)) {
                            // just write CSV content
                            final PrintWriter out = new PrintWriter(filename);
                            out.println(content);
                            out.close();
                        }
                        // XLSX export
                        else {
                            final Workbook workbook = XLSXUtil.createWorkbook();
                            final Sheet sheet = XLSXUtil.createNewPage(workbook, "ROIS");

                            if (XLSXUtil.setFromCSV(sheet, content))
                                XLSXUtil.saveAndClose(workbook, filename);
                            else {
                                MessageDialog.showDialog("Error", "Error while exporting ROIs table content to XLSX file.", MessageDialog.ERROR_MESSAGE);
                                return false;
                            }
                        }
                    }
                    catch (final Exception e1) {
                        MessageDialog.showDialog("Error", e1.getMessage(), MessageDialog.ERROR_MESSAGE);
                        return false;
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            return super.isEnabled() && (sequence != null);
        }
    };

    public static final IcyAbstractAction settingAction = new IcyAbstractAction(
            "Preferences",
            "ROI table preferences"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final RoisPanel roisPanel = RoisPanel.getInstance();

            if (roisPanel != null) {
                roisPanel.showSettingPanel();

                return true;
            }

            return false;
        }
    };

    public static final IcyAbstractAction convertTo3DAction = new IcyAbstractAction(
            "Convert to 3D ROI(s)",
            "Convert to 3D ROI(s)",
            "Convert selected 2D ROI(s) to 3D ROI(s) by extending it along the Z axis"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    final int sizeZ = sequence.getSizeZ();

                    // ROI Z stack conversion
                    sequence.beginUpdate();
                    try {
                        final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI2D roi : selectedROIs) {
                            final ROI stackedRoi = ROIUtil.convertTo3D(roi, 0d, sizeZ);

                            if (stackedRoi != null) {
                                // select it by default
                                stackedRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(stackedRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(stackedRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (addedROIs.size() > 1) ? "ROIs 3D stack conversion" : "ROI 3D stack conversion"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI 3D conversion operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction convertTo2DAction = new IcyAbstractAction(
            "Convert 2D ROI(s)",
            "Convert to 2D ROI(s)",
            "Convert selected 3D ROI(s) to 2D ROI(s) (3D stacks are converted to multiple 2D ROI(s))"
    ) {
        @Override
        protected boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    // ROI Z stack conversion
                    sequence.beginUpdate();
                    try {
                        final List<ROI3D> selectedROIs = sequence.getSelectedROI3Ds();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI3D roi : selectedROIs) {
                            final ROI[] unstackedRois = ROIUtil.convertTo2D(roi);

                            if (unstackedRois != null) {
                                sequence.removeROI(roi);
                                // add to undo manager
                                removedROIs.add(roi);
                                for (final ROI roi2d : unstackedRois) {
                                    // select it by default
                                    roi2d.setSelected(true);

                                    sequence.addROI(roi2d);
                                    // add to undo manager
                                    addedROIs.add(roi2d);
                                }
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (addedROIs.size() > 1) ? "3D ROIs unstack conversion" : "3D ROI unstack conversion"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI 2D conversion operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction convertToMaskAction = new IcyAbstractAction(
            "Convert to Mask",
            "Convert Shape ROI to Mask ROI",
            "Convert selected Shape ROI to Mask ROI by using their boolean mask"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    // ROI mask conversion
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs) {
                            final ROI maskRoi = ROIUtil.convertToMask(roi);

                            if (maskRoi != null) {
                                // select it by default
                                maskRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(maskRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(maskRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (addedROIs.size() > 1) ? "ROIs mask conversion" : "ROI mask conversion"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI mask conversion operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction convertToPointAction = new IcyAbstractAction(
            "to Point",
            "Convert ROI to Point ROI",
            "Converts selected ROI(s) to ROI Point (2D or 3D) representing the mass center of the input ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                // ROI point conversion
                sequence.beginUpdate();
                try {
                    final List<ROI> selectedROIs = sequence.getSelectedROIs();
                    final List<ROI> removedROIs = new ArrayList<>();
                    final List<ROI> addedROIs = new ArrayList<>();

                    for (final ROI roi : selectedROIs) {
                        final ROI roiPoint = ROIUtil.convertToPoint(roi);

                        if (roiPoint != null) {
                            // select it by default
                            roiPoint.setSelected(true);

                            sequence.removeROI(roi);
                            sequence.addROI(roiPoint);

                            // add to undo manager
                            removedROIs.add(roi);
                            addedROIs.add(roiPoint);
                        }
                    }

                    if (!addedROIs.isEmpty())
                        sequence.addUndoableEdit(
                                new ROIReplacesSequenceEdit(
                                        sequence,
                                        removedROIs,
                                        addedROIs,
                                        (addedROIs.size() > 1) ? "ROIs point conversion" : "ROI point conversion"
                                )
                        );
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                catch (final InterruptedException ex) {
                    MessageDialog.showDialog("Operation interrupted", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction convertToEllipseAction = new IcyAbstractAction(
            "Convert to Ellipse",
            "Convert ROI to Circle ROI",
            "Converts selected ROI(s) to Circle ROI centered on the mass center of the input ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null) {
                    final double radius = 1d; // TODO: 23/01/2023 Change this when ROI toolbar is set

                    // ROI point conversion
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs) {
                            final ROI resultRoi;

                            if (radius == 0)
                                resultRoi = ROIUtil.convertToPoint(roi);
                            else
                                resultRoi = ROIUtil.convertToEllipse(roi, radius, radius);

                            if (resultRoi != null) {
                                // select it by default
                                resultRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(resultRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(resultRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (addedROIs.size() > 1) ? "ROIs circle conversion" : "ROI circle conversion"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    catch (final InterruptedException ex) {
                        MessageDialog.showDialog("Operation interrupted", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction convertToRectangleAction = new IcyAbstractAction(
            "to Square",
            "Convert ROI to Square ROI",
            "Converts selected ROI(s) to Square ROI centered on the mass center of the input ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

                if (mainFrame != null) {
                    //final double size = mainFrame.getMainRibbon().getROIRibbonTask().getRadius() * 2;
                    final double size = 1d * 2; // TODO: 23/01/2023 Change '1d' when ROI toolbar is set

                    // ROI point conversion
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs) {
                            final ROI resultRoi;

                            if (size == 0)
                                resultRoi = ROIUtil.convertToPoint(roi);
                            else
                                resultRoi = ROIUtil.convertToRectangle(roi, size, size);

                            if (resultRoi != null) {
                                // select it by default
                                resultRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(resultRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(resultRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (addedROIs.size() > 1) ? "ROIs square conversion" : "ROI square conversion"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    catch (final InterruptedException ex) {
                        MessageDialog.showDialog("Operation interrupted", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction convertToShapeAction = new IcyAbstractAction(
            "Convert to Shape",
            "Convert Mask ROI to Polygon shape ROI",
            "Convert selected Mask ROI to Shape ROI using polygon approximation"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    // ROI shape conversion
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs) {
                            final ROI shapeRoi = ROIUtil.convertToShape(roi, -1);

                            if (shapeRoi != null) {
                                // select it by default
                                shapeRoi.setSelected(true);

                                sequence.removeROI(roi);
                                sequence.addROI(shapeRoi);

                                // add to undo manager
                                removedROIs.add(roi);
                                addedROIs.add(shapeRoi);
                            }
                        }

                        if (!addedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (addedROIs.size() > 1) ? "ROIs shape conversion" : "ROI shape conversion"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI shape conversion operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction separateObjectsAction = new IcyAbstractAction(
            "Separate Components",
            "Separate components from selected Mask ROI(s)",
            "Separate unconnected components from selected Mask ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> removedROIs = new ArrayList<>();
                        final List<ROI> addedROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs) {
                            final List<ROI> components = ROIUtil.getConnectedComponents(roi);

                            // nothing to do if we obtain only 1 component
                            if (components.size() > 1) {
                                sequence.removeROI(roi);
                                removedROIs.add(roi);

                                for (final ROI component : components) {
                                    sequence.addROI(component);
                                    // add to undo manager
                                    addedROIs.add(component);
                                }
                            }
                        }

                        if (!removedROIs.isEmpty())
                            sequence.addUndoableEdit(
                                    new ROIReplacesSequenceEdit(
                                            sequence,
                                            removedROIs,
                                            addedROIs,
                                            (removedROIs.size() > 1) ? "ROIs separate objects" : "ROI separate objects"
                                    )
                            );
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI objects separation operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction upscale2dAction = new IcyAbstractAction(
            "Scale x2 (2D)",
            "Create up scaled version of selected ROI(s) (2D)",
            "Create 2x factor up scaled version of selected ROI(s) (2D)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> newROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs)
                            newROIs.add(ROIUtil.getUpscaled(roi, false));

                        if (!newROIs.isEmpty()) {
                            for (final ROI roi : newROIs)
                                sequence.addROI(roi);

                            sequence.addUndoableEdit(new ROIAddsSequenceEdit(
                                    sequence,
                                    newROIs,
                                    (newROIs.size() > 1) ? "ROIs scale x2" : "ROI scale x2"
                            ));
                        }
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI 2x upscaling operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction upscaleAction = new IcyAbstractAction(
            "Scale x2",
            "Create x2 scaled version of selected ROI(s)",
            "Create x2 factor scaled version of selected ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> newROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs)
                            newROIs.add(ROIUtil.getUpscaled(roi, true));

                        if (!newROIs.isEmpty()) {
                            for (final ROI roi : newROIs)
                                sequence.addROI(roi);

                            sequence.addUndoableEdit(new ROIAddsSequenceEdit(
                                    sequence,
                                    newROIs,
                                    (newROIs.size() > 1) ? "ROIs scale x2" : "ROI scale x2"
                            ));
                        }
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI 2x upscaling operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction downscale2dAction = new IcyAbstractAction(
            "Scale /2 (2D)",
            "Create /2 scaled version of selected ROI(s) (2D)",
            "Create /2 factor scaled version of selected ROI(s) (2D)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> newROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs)
                            newROIs.add(ROIUtil.getDownscaled(roi, false));

                        if (!newROIs.isEmpty()) {
                            for (final ROI roi : newROIs)
                                sequence.addROI(roi);

                            sequence.addUndoableEdit(new ROIAddsSequenceEdit(
                                    sequence,
                                    newROIs,
                                    (newROIs.size() > 1) ? "ROIs scale /2" : "ROI scale /2"
                            ));
                        }
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI 2x downscaling operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction downscaleAction = new IcyAbstractAction(
            "Scale /2",
            "Create down scaled version of selected ROI(s)",
            "Create 2x factor down scaled version of selected ROI(s)"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                final Sequence sequence = Icy.getMainInterface().getActiveSequence();

                if (sequence != null) {
                    sequence.beginUpdate();
                    try {
                        final List<ROI> selectedROIs = sequence.getSelectedROIs();
                        final List<ROI> newROIs = new ArrayList<>();

                        for (final ROI roi : selectedROIs)
                            newROIs.add(ROIUtil.getDownscaled(roi, true));

                        if (!newROIs.isEmpty()) {
                            for (final ROI roi : newROIs)
                                sequence.addROI(roi);

                            sequence.addUndoableEdit(new ROIAddsSequenceEdit(
                                    sequence,
                                    newROIs,
                                    (newROIs.size() > 1) ? "ROIs scale /2" : "ROI scale /2"
                            ));
                        }
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    finally {
                        sequence.endUpdate();
                    }

                    return true;
                }
            }
            catch (final InterruptedException ie) {
                new FailedAnnounceFrame("ROI 2x downscaling operation canceled !");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction autoSplitAction = new IcyAbstractAction(
            "Auto split",
            "Automatic split selected ROI",
            "Automatic split selected ROI using shape and size information."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                sequence.beginUpdate();
                try {
                    // final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                    final List<ROI> removedROIs = new ArrayList<>();
                    final List<ROI> addedROIs = new ArrayList<>();

                    // // --> TODO
                    // for (ROI2D roi : selectedROIs)
                    // {
                    // final List<ROI> components = ROIUtil.split(roi);
                    //
                    // // nothing to do if we obtain only 1 component
                    // if (components.size() > 1)
                    // {
                    // sequence.removeROI(roi);
                    // removedROIs.add(roi);
                    //
                    // for (ROI component : components)
                    // {
                    // sequence.addROI(component);
                    // // add to undo manager
                    // addedROIs.add(component);
                    // }
                    // }
                    // }

                    if (!removedROIs.isEmpty())
                        sequence.addUndoableEdit(
                                new ROIReplacesSequenceEdit(
                                        sequence,
                                        removedROIs,
                                        addedROIs,
                                        (removedROIs.size() > 1) ? "ROIs automatic split" : "ROI automatic split"
                                )
                        );
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                }
                finally {
                    sequence.endUpdate();
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction computeDistanceMapAction = new IcyAbstractAction(
            "Compute Distance Map",
            "Compute distance map of selected ROIs",
            "Computes the inner distance transform of the selected ROIs."
    ) {
        @Override
        protected boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();

                final Sequence distanceMap;
                try {
                    final Dimension3D pixelSize = new Dimension3D.Double(1, 1, 1);
                    distanceMap = ROIUtil.computeDistanceMap(selectedROIs, sequence.getDimension5D(), pixelSize, false);
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    return false;
                }
                catch (final InterruptedException ex1) {
                    throw new IcyHandledException(ex1);
                }
                Icy.getMainInterface().addSequence(distanceMap);
            }
            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null); // TODO maybe rework enable condition
        }
    };

    public static final IcyAbstractAction computeWatershedSeparation = new IcyAbstractAction(
            "Separate by Watershed",
            "Separate in parts selected ROIs by using watersheds of the shape",
            "Computes the separation of the selected ROIs by applying a watershed approach on the distance map of the ROIs."
    ) {
        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        protected boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI2D> selectedRois = sequence.getSelectedROI2Ds();
                final List<ROI> seedRois = sequence.getSelectedROIs(ROI2DPoint.class, true);
                selectedRois.removeAll(new HashSet<>(seedRois));

                final Thread thrd = new Thread(() -> {
                    try {
                        final Dimension3D pixelSize = new Dimension3D.Double(1, 1, 1);
                        final List<ROI> separationRois;
                        List<ROI> usedSeeds = null;
                        if (seedRois.isEmpty()) {
                            usedSeeds = new ArrayList<>();
                            separationRois = ROIUtil.computeWatershedSeparation(selectedRois,
                                    sequence.getDimension5D(), pixelSize, usedSeeds);
                        }
                        else {
                            separationRois = ROIUtil.computeWatershedSeparation(selectedRois, seedRois,
                                    sequence.getDimension5D(), pixelSize);
                        }
                        sequence.beginUpdate();
                        if (usedSeeds != null) {
                            sequence.addROIs(usedSeeds, true);
                        }
                        sequence.removeROIs(selectedRois, true);
                        sequence.addROIs(separationRois, true);

                        sequence.endUpdate();
                    }
                    catch (final UnsupportedOperationException ex) {
                        MessageDialog.showDialog("Operation not supported", ex.toString(), MessageDialog.ERROR_MESSAGE);
                    }
                    catch (final InterruptedException ex1) {
                        throw new IcyHandledException(ex1);
                    }
                }, "ROIWatershed");
                thrd.start();
            }
            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null); // TODO maybe rework enable condition
        }
    };

    public static final IcyAbstractAction computeSkeleton = new IcyAbstractAction(
            "Compute Skeleton",
            "Computes the skeletons of selected ROIs",
            "Computes the skeletons of selected ROIs."
    ) {
        @Override
        protected boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                //MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
                //double distance = mainFrame.getMainRibbon().getROIRibbonTask().getDistance();
                final double distance = 1d; // TODO: 23/01/2023 Change this when ROI toolbar is set

                try {
                    final Dimension3D pixelSize = new Dimension3D.Double(1, 1, 1);
                    final List<ROI> skeletonRois = ROIUtil.computeSkeleton(selectedROIs, pixelSize, distance);
                    sequence.beginUpdate();
                    sequence.removeROIs(selectedROIs, true);
                    sequence.addROIs(skeletonRois, true);
                    sequence.endUpdate();
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.getMessage(), MessageDialog.ERROR_MESSAGE);
                    return false;
                }
                catch (final InterruptedException ex1) {
                    throw new IcyHandledException(ex1);
                }
            }
            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null); // TODO maybe rework enable condition
        }
    };

    public static final IcyAbstractAction dilateObjectsAction = new IcyAbstractAction(
            "Dilate",
            "Dilates selected 2D ROIs",
            "Computes the dilation of selected 2D ROIs using the provided distance."
    ) {
        @Override
        protected boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                //MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
                //double distance = mainFrame.getMainRibbon().getROIRibbonTask().getDistance();
                final double distance = 1d; // TODO: 23/01/2023 Change this when ROI toolbar is set

                try {
                    final Dimension3D pixelSize = new Dimension3D.Double(1, 1, 1);
                    final List<ROI> dilatedRois = ROIUtil.computeDilation(selectedROIs, pixelSize, distance);
                    sequence.beginUpdate();
                    sequence.removeROIs(selectedROIs, true);
                    sequence.addROIs(dilatedRois, true);
                    sequence.endUpdate();
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.getMessage(), MessageDialog.ERROR_MESSAGE);
                    return false;
                }
                catch (final InterruptedException ex1) {
                    throw new IcyHandledException(ex1);
                }
            }
            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null); // TODO maybe rework enable condition
        }
    };

    public static final IcyAbstractAction erodeObjectsAction = new IcyAbstractAction(
            "Erode",
            "Erodes selected 2D ROIs",
            "Computes the erotion of selected 2D ROIs using the provided distance."
    ) {
        @Override
        protected boolean doAction(final ActionEvent e) {
            final Sequence sequence = Icy.getMainInterface().getActiveSequence();

            if (sequence != null) {
                final List<ROI2D> selectedROIs = sequence.getSelectedROI2Ds();
                //MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
                //double distance = mainFrame.getMainRibbon().getROIRibbonTask().getDistance();
                final double distance = 1d; // TODO: 23/01/2023 Change this when ROI toolbar is set

                try {
                    final Dimension3D pixelSize = new Dimension3D.Double(1, 1, 1);
                    final List<ROI> dilatedRois = ROIUtil.computeErosion(selectedROIs, pixelSize, distance);
                    sequence.beginUpdate();
                    sequence.removeROIs(selectedROIs, true);
                    sequence.addROIs(dilatedRois, true);
                    sequence.endUpdate();
                }
                catch (final UnsupportedOperationException ex) {
                    MessageDialog.showDialog("Operation not supported", ex.getMessage(), MessageDialog.ERROR_MESSAGE);
                    return false;
                }
                catch (final InterruptedException ex1) {
                    throw new IcyHandledException(ex1);
                }
            }
            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null); // TODO maybe rework enable condition
        }
    };

    /**
     * Return all actions of this class
     */
    @Deprecated(forRemoval = true)
    public static @NotNull List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : RoiActions.class.getFields()) {
            final Class<?> type = field.getType();

            try {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (final Exception e) {
                // ignore
            }
        }

        return result;
    }

    public static @NotNull List<IcyAbstractAction> getAllActiveSequenceActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        result.add(loadAction);
        result.add(saveAction);
        result.add(copyAction);
        result.add(copyLinkAction);
        result.add(pasteAction);
        result.add(pasteLinkAction);
        result.add(selectAllAction);
        result.add(unselectAction);
        result.add(deleteAction);
        result.add(boolNotAction);
        result.add(boolOrAction);
        result.add(boolAndAction);
        result.add(boolXorAction);
        result.add(boolSubtractAction);
        result.add(fillInteriorAction);
        result.add(fillExteriorAction);
        result.add(xlsExportAction);
        result.add(convertTo3DAction);
        result.add(convertTo2DAction);
        result.add(convertToMaskAction);
        result.add(convertToPointAction);
        result.add(convertToEllipseAction);
        result.add(convertToRectangleAction);
        result.add(convertToShapeAction);
        result.add(separateObjectsAction);
        result.add(upscale2dAction);
        result.add(upscaleAction);
        result.add(downscale2dAction);
        result.add(downscaleAction);
        result.add(autoSplitAction);
        result.add(computeDistanceMapAction);
        result.add(computeWatershedSeparation);
        result.add(computeSkeleton);
        result.add(dilateObjectsAction);
        result.add(erodeObjectsAction);

        return result;
    }
}

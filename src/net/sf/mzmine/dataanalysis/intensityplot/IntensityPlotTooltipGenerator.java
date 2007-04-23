/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.dataanalysis.intensityplot;

import java.text.Format;

import net.sf.mzmine.data.AlignmentResultRow;
import net.sf.mzmine.io.OpenedRawDataFile;
import net.sf.mzmine.userinterface.mainwindow.MainWindow;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * 
 */
class IntensityPlotTooltipGenerator implements CategoryToolTipGenerator {

    /**
     * @see org.jfree.chart.labels.CategoryToolTipGenerator#generateToolTip(org.jfree.data.category.CategoryDataset,
     *      int, int)
     */
    public String generateToolTip(CategoryDataset dataset, int row, int column) {
        Format mzFormat = MainWindow.getInstance().getMZFormatProvider().getCurrentNumberFormat();
        Format timeFormat = MainWindow.getInstance().getRTFormatProvider().getCurrentNumberFormat();
        Format intensityFormat = MainWindow.getInstance().getIntensityFormatProvider().getCurrentNumberFormat();
        AlignmentResultRow alignmentRow = ((IntensityPlotDataset) dataset).getRow(row);
        OpenedRawDataFile dataFile = ((IntensityPlotDataset) dataset).getFile(column);
        return mzFormat.format(alignmentRow.getAverageMZ())
                + " m/z @"
                + timeFormat.format(alignmentRow.getAverageRT()) + ", "
                        + dataFile.toString() + ", value: "
                        + intensityFormat.format(dataset.getValue(row, column));
    }

}

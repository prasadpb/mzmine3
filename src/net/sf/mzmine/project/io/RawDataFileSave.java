/*
 * Copyright 2006-2009 The MZmine 2 Development Team
 *
 * This file is part of MZmine 2.
 *
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package net.sf.mzmine.project.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class RawDataFileSave {

	private int numOfScans;
	private ZipOutputStream zipOutputStream;
	private SaveFileUtils saveFileUtils;

	public RawDataFileSave(ZipOutputStream zipOutputStream) {
		this.zipOutputStream = zipOutputStream;
	}

	public void writeRawDataFiles(RawDataFile rawDataFile, String rawDataSavedName) {
		try {
			// step 1 - save scan file			
			zipOutputStream.putNextEntry(new ZipEntry(rawDataSavedName + ".scans"));
			FileInputStream fileStream = new FileInputStream(((RawDataFileImpl) rawDataFile).getScanDataFileasFile());
			saveFileUtils = new SaveFileUtils();
			saveFileUtils.saveFile(fileStream, zipOutputStream, ((RawDataFileImpl) rawDataFile).getScanDataFileasFile().length(), SaveFileUtilsMode.CLOSE_IN);
			Document document = this.saveRawDataInformation(rawDataFile);

			// step 2 - save raw data description			
			zipOutputStream.putNextEntry(new ZipEntry(rawDataSavedName + ".xml"));
			OutputStream finalStream = zipOutputStream;
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(finalStream, format);
			writer.write(document);

		} catch (Exception ex) {
			Logger.getLogger(RawDataFileSave.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public Document saveRawDataInformation(RawDataFile rawDataFile) throws IOException {
		numOfScans = rawDataFile.getNumOfScans();

		Document document = DocumentFactory.getInstance().createDocument();
		Element saveRoot = document.addElement(RawDataElementName.RAWDATA.getElementName());

		// <NAME>
		XMLUtils.fillXMLValues(saveRoot, RawDataElementName.NAME.getElementName(), null, null, rawDataFile.getName());

		// <QUANTITY>
		XMLUtils.fillXMLValues(saveRoot, RawDataElementName.QUANTITY_SCAN.getElementName(), null, null, String.valueOf(numOfScans));

		for (int scanNumber : rawDataFile.getScanNumbers()) {
			Element newElement = XMLUtils.fillXMLValues(saveRoot, RawDataElementName.SCAN.getElementName(), null, null, null);
			Scan scan = rawDataFile.getScan(scanNumber);
			this.fillScanElement(scan, newElement);
		}
		return document;
	}

	private void fillScanElement(Scan scan, Element element) {

		XMLUtils.fillXMLValues(element, RawDataElementName.SCAN_ID.getElementName(), null, null, String.valueOf(scan.getScanNumber()));
		XMLUtils.fillXMLValues(element, RawDataElementName.MS_LEVEL.getElementName(), null, null, String.valueOf(scan.getMSLevel()));

		if (scan.getParentScanNumber() > 0) {
			XMLUtils.fillXMLValues(element, RawDataElementName.PARENT_SCAN.getElementName(), null, null, String.valueOf(scan.getParentScanNumber()));
		}

		XMLUtils.fillXMLValues(element, RawDataElementName.PRECURSOR_MZ.getElementName(), null, null, String.valueOf(scan.getPrecursorMZ()));
		XMLUtils.fillXMLValues(element, RawDataElementName.RETENTION_TIME.getElementName(), null, null, String.valueOf(scan.getRetentionTime()));
		XMLUtils.fillXMLValues(element, RawDataElementName.CENTROIDED.getElementName(), null, null, String.valueOf(scan.isCentroided()));
		XMLUtils.fillXMLValues(element, RawDataElementName.QUANTITY_DATAPOINTS.getElementName(), null, null, String.valueOf((scan.getNumberOfDataPoints())));

		if (scan.getFragmentScanNumbers() != null) {
			int[] fragmentScans = scan.getFragmentScanNumbers();
			Element newElement = XMLUtils.fillXMLValues(element, RawDataElementName.QUANTITY_FRANGMENT_SCAN.getElementName(), RawDataElementName.QUANTITY.getElementName(), String.valueOf(fragmentScans.length), null);
			for (int i : fragmentScans) {
				XMLUtils.fillXMLValues(newElement, RawDataElementName.QUANTITY_DATAPOINTS.getElementName(), null, null, String.valueOf(i));
			}
		}
	}

	public double getProgress() {
		return saveFileUtils.progress;
	}
}

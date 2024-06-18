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

package org.bioimageanalysis.icy.model;

import loci.common.services.DependencyException;
import loci.common.services.ServiceFactory;
import loci.formats.MetadataTools;
import loci.formats.services.OMEXMLService;
import loci.formats.services.OMEXMLServiceImpl;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.quantity.Time;
import ome.xml.meta.MetadataRetrieve;
import ome.xml.meta.OMEXMLMetadata;
import ome.xml.model.OME;
import ome.xml.model.StructuredAnnotations;
import ome.xml.model.XMLAnnotation;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.TypeUtil;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.sequence.MetaDataUtil;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.w3c.dom.Document;

import java.awt.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class OMEUtil {
    private static OMEXMLService OMEService;

    static {
        final ServiceFactory factory;
        try {
            factory = new ServiceFactory();
            OMEService = factory.getInstance(OMEXMLService.class);
        }
        catch (final DependencyException e) {
            final String[] messages = new String[]{
                    "Error create OME Service:" + e.getLocalizedMessage(),
                    "Using default service implementation..."
            };
            IcyLogger.error(OMEUtil.class, e, messages);

            //factory = null;
            OMEService = new OMEXMLServiceImpl();
        }
    }

    /**
     * Safe integer evaluation from PositiveInteger object.<br>
     * Return defaultValue if specified object is null.
     */
    public static int getValue(final PositiveInteger obj, final int defaultValue) {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getInt(obj.getValue(), defaultValue);
    }

    /**
     * Safe integer evaluation from NonNegativeInteger object.<br>
     * Return defaultValue if specified object is null.
     */
    public static int getValue(final NonNegativeInteger obj, final int defaultValue) {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getInt(obj.getValue(), defaultValue);
    }

    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return <code>defaultValue</code> if <code>obj</code> is null or equal to infinite with <code>allowInfinite</code>
     * set to false.
     */
    public static double getValue(final PositiveFloat obj, final double defaultValue, final boolean allowInfinite) {
        if (obj == null)
            return defaultValue;

        return TypeUtil.getDouble(obj.getValue(), defaultValue, allowInfinite);
    }

    /**
     * Safe float evaluation from PositiveFloat object.<br>
     * Return defaultValue if specified object is null.
     */
    public static double getValue(final PositiveFloat obj, final double defaultValue) {
        return getValue(obj, defaultValue, true);
    }

    /**
     * Convert specified Length to double value in &micro;m (for backward compatibility).<br>
     * Return defaultValue if specified object is <code>null</code>.
     */
    public static double getValue(final Length obj, final double defaultValue) {
        if (obj == null)
            return defaultValue;

        final Number value = obj.value(UNITS.MICROMETER);
        if (value == null)
            return defaultValue;

        return value.doubleValue();
    }

    /**
     * Convert specified Time to double value in second (for backward compatibility).<br>
     * Return defaultValue if specified object is <code>null</code>.
     */
    public static double getValue(final Time obj, final double defaultValue) {
        if (obj == null)
            return defaultValue;

        final Number value = obj.value(UNITS.SECOND);
        if (value == null)
            return defaultValue;

        return value.doubleValue();
    }

    /**
     * Return a PositiveFloat object representing the specified value
     */
    public static PositiveFloat getPositiveFloat(final double value) {
        return new PositiveFloat(Double.valueOf(value));
    }

    /**
     * Return a PositiveInteger object representing the specified value
     */
    public static PositiveInteger getPositiveInteger(final int value) {
        return new PositiveInteger(Integer.valueOf(value));
    }

    /**
     * Return a NonNegativeInteger object representing the specified value
     */
    public static NonNegativeInteger getNonNegativeInteger(final int value) {
        return new NonNegativeInteger(Integer.valueOf(value));
    }

    /**
     * Return a Length object representing the specified value (in &micro;m)
     */
    public static Length getLength(final double value) {
        return new Length(Double.valueOf(value), UNITS.MICROMETER);
    }

    /**
     * Return a Time object representing the specified value (in second)
     */
    public static Time getTime(final double value) {
        return new Time(Double.valueOf(value), UNITS.SECOND);
    }

    /**
     * Return a java Color object from a OME Color object
     */
    public static Color getJavaColor(final ome.xml.model.primitives.Color value) {
        if (value == null)
            return null;

        return new Color(value.getRed(), value.getGreen(), value.getBlue(), value.getAlpha());
    }

    /**
     * Return a OME Color object from a java Color object
     */
    public static ome.xml.model.primitives.Color getOMEColor(final Color value) {
        return new ome.xml.model.primitives.Color(value.getRed(), value.getGreen(), value.getBlue(), value.getAlpha());
    }

    /**
     * Create a new empty OME Metadata object.
     */
    public synchronized static OMEXMLMetadata createOMEXMLMetadata() {
        try {
            return OMEService.createOMEXMLMetadata();
        }
        catch (final Exception e) {
            IcyLogger.error(OMEUtil.class, e, "Unable to create OME XML metada.");
            return null;
        }
    }

    /**
     * Create a new OME Metadata object from the specified Metadata object.<br>
     *
     * @param setUserName
     *        set the experimenter user name from current User Name environment var
     *        otherwise we preserve old user name (in case of simple image loading / duplicating / saving..)
     */
    public static OMEXMLMetadata createOMEXMLMetadata(final MetadataRetrieve metadata, final boolean setUserName) {
        final OMEXMLMetadata result = createOMEXMLMetadata();

        // TODO: remove that when annotations loading will be fixed in Bio-Formats
        if (metadata instanceof OMEXMLMetadata) {
            final OME root = (OME) ((OMEXMLMetadata) metadata).getRoot();
            final StructuredAnnotations annotations = root.getStructuredAnnotations();

            // clean up annotation
            if (annotations != null) {
                for (int i = annotations.sizeOfXMLAnnotationList() - 1; i >= 0; i--) {
                    final XMLAnnotation annotation = annotations.getXMLAnnotation(i);

                    if (StringUtil.isEmpty(annotation.getValue()))
                        annotations.removeXMLAnnotation(annotation);
                }
            }
        }

        synchronized (OMEService) {
            // need to cast to get rid of this old loci package stuff
            OMEService.convertMetadata((loci.formats.meta.MetadataRetrieve) metadata, (loci.formats.meta.MetadataStore) result);
        }

        // want to set user name here ?
        if (setUserName)
            MetaDataUtil.setUserName(result, SystemUtil.getUserName());

        return result;
    }

    /**
     * Create a new OME Metadata object from the specified Metadata object.<br>
     */
    public static OMEXMLMetadata createOMEXMLMetadata(final MetadataRetrieve metadata) {
        return createOMEXMLMetadata(metadata, false);
    }

    /**
     * Create a new single serie OME Metadata object from the specified Metadata object.
     *
     * @param serie
     *        Index of the serie we want to keep.
     */
    public static OMEXMLMetadata createOMEXMLMetadata(final MetadataRetrieve metadata, final int serie) {
        // generally used on loading so preserve user name here
        final OMEXMLMetadata result = OMEUtil.createOMEXMLMetadata(metadata, false);

        MetaDataUtil.keepSingleSerie(result, serie);

        // set the default id with correct serie number (for XML metadata)
        result.setImageID(MetadataTools.createLSID("Image", serie), 0);

        return result;
    }

    /**
     * Convert the specified Metadata object to OME Metadata.<br>
     * If the specified Metadata is already OME no conversion is done.
     */
    public static OMEXMLMetadata getOMEXMLMetadata(final MetadataRetrieve metadata) {
        if (metadata instanceof OMEXMLMetadata)
            return (OMEXMLMetadata) metadata;

        return createOMEXMLMetadata(metadata, false);
    }

    /**
     * Return a XML document from the specified Metadata object
     */
    public static Document getXMLDocument(final OMEXMLMetadata metadata) {
        try {
            return XMLUtil.createDocument(metadata.dumpXML());
        }
        catch (final Exception e) {
            IcyLogger.error(OMEUtil.class, e, "Unable to get XML document.");
        }

        // return empty document
        return XMLUtil.createDocument(false);
    }

    /**
     * Report and upload the specified filename to LOCI team.
     */
    public static boolean reportLociError(final String fileName, final String errorMessage) {
        // TODO: implement this when done in LOCI
        // final IssueReporter reporter = new IssueReporter();
        // return reporter.reportBug(fileName, errorMessage);

        return false;
    }
}

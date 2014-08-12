/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package uk.ac.rdg.resc.edal.wms;

import java.util.Set;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import uk.ac.rdg.resc.edal.dataset.Dataset;
import uk.ac.rdg.resc.edal.domain.Extent;
import uk.ac.rdg.resc.edal.exceptions.BadTimeFormatException;
import uk.ac.rdg.resc.edal.exceptions.EdalException;
import uk.ac.rdg.resc.edal.exceptions.IncorrectDomainException;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.graphics.formats.ImageFormat;
import uk.ac.rdg.resc.edal.graphics.style.Drawable.NameAndRange;
import uk.ac.rdg.resc.edal.util.Extents;
import uk.ac.rdg.resc.edal.util.GISUtils;
import uk.ac.rdg.resc.edal.util.PlottingDomainParams;
import uk.ac.rdg.resc.edal.util.TimeUtils;
import uk.ac.rdg.resc.edal.wms.util.WmsUtils;

/**
 * Object representing a request to the GetMap operation. This simply parses the
 * request and only does very basic sanity checking on the parameters (e.g.
 * checking for valid integers).
 * 
 * @author Guy Griffiths
 */
public class GetMapParameters {
    private String wmsVersion;
    private String imageFormatString;
    private boolean animation;

    protected PlottingDomainParams plottingDomainParams;
    private GetMapStyleParams styleParameters;

    /**
     * Creates a new instance of GetMapParameter from the given RequestParams
     * 
     * @param params
     *            A {@link RequestParams} object representing the URL parameters
     *            to parse
     * @param catalogue
     *            A {@link WmsCatalogue}. This is used to determine the
     *            {@link Chronology} of the dataset being referred to so that
     *            the time values can be parsed correctly
     * 
     * @throws EdalException
     *             if the request is invalid
     */
    public GetMapParameters(RequestParams params, WmsCatalogue catalogue) throws EdalException {
        this.wmsVersion = params.getMandatoryWmsVersion();
        if (!WmsUtils.SUPPORTED_VERSIONS.contains(this.wmsVersion)) {
            throw new EdalException("VERSION " + this.wmsVersion + " not supported");
        }
        imageFormatString = params.getString("format");
        animation = params.getBoolean("animation", false);
        styleParameters = new GetMapStyleParams(params);

        /*
         * Now we have the style parameters, we can find out which layers we
         * will be requesting, and hence which Chronology the time strings are
         * referring to.
         * 
         * All layers need to share the same chronology (otherwise the time
         * string may be ambiguous)
         */
        Chronology chronology = null;
        Set<NameAndRange> fieldsWithScales = styleParameters.getImageGenerator(catalogue)
                .getFieldsWithScales();
        for (NameAndRange nameAndRange : fieldsWithScales) {
            String wmsLayerName = nameAndRange.getFieldLabel();
            Dataset dataset = catalogue.getDatasetFromLayerName(wmsLayerName);
            if (chronology == null) {
                chronology = dataset.getDatasetChronology();
            } else {
                if (dataset.getDatasetChronology() != null) {
                    if (!chronology.equals(dataset.getDatasetChronology())) {
                        throw new IncorrectDomainException(
                                "All datasets referenced by a GetMap request must share the same chronology");
                    }
                }
            }
        }
        plottingDomainParams = parsePlottingParams(params, chronology);
    }

    public PlottingDomainParams getPlottingDomainParameters() {
        return plottingDomainParams;
    }

    public GetMapStyleParams getStyleParameters() {
        return styleParameters;
    }

    public String getWmsVersion() {
        return wmsVersion;
    }

    public boolean isAnimation() {
        return animation;
    }

    public ImageFormat getImageFormat() throws EdalException {
        if (imageFormatString == null) {
            throw new EdalException("Parameter FORMAT was not supplied");
        } else {
            return ImageFormat.get(imageFormatString);
        }
    }

    private PlottingDomainParams parsePlottingParams(RequestParams params, Chronology chronology)
            throws EdalException {
        String startTimeStr = null;
        String endTimeStr = null;
        String timeString = params.getString("time");
        if (timeString != null && !timeString.trim().equals("")) {
            String[] timeStrings = timeString.split("/");
            if (timeStrings.length == 1) {
                startTimeStr = timeStrings[0];
                endTimeStr = timeStrings[0];
            } else if (timeStrings.length == 2) {
                startTimeStr = timeStrings[0];
                endTimeStr = timeStrings[1];
            } else {
                throw new BadTimeFormatException("Time can either be a single value or a range");
            }
        }
        DateTime startTime = null;
        if (startTimeStr != null) {
            startTime = TimeUtils.iso8601ToDateTime(startTimeStr, chronology);
        }
        DateTime endTime = null;
        if (endTimeStr != null) {
            endTime = TimeUtils.iso8601ToDateTime(endTimeStr, chronology);
        }
        Extent<DateTime> tExtent = null;
        if (startTime != null && endTime != null) {
            tExtent = Extents.newExtent(startTime, endTime);
        }

        String targetTimeStr = params.getString("targettime");
        if (targetTimeStr == null && endTimeStr != null) {
            targetTimeStr = endTimeStr;
        }
        DateTime targetTime = null;
        if (targetTimeStr != null) {
            targetTime = TimeUtils.iso8601ToDateTime(targetTimeStr, chronology);
        }

        Extent<Double> zExtent = null;
        String depthString = params.getString("elevation");
        if (depthString != null && !depthString.trim().equals("")) {
            String[] depthStrings = depthString.split("/");
            if (depthStrings.length == 1) {
                try {
                    Double depth = Double.parseDouble(depthStrings[0]);
                    zExtent = Extents.newExtent(depth, depth);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Depth format is wrong: " + depthStrings[0]);
                }
            } else if (depthStrings.length == 2) {
                try {
                    zExtent = Extents.newExtent(Double.parseDouble(depthStrings[0]),
                            Double.parseDouble(depthStrings[1]));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Depth format is wrong: " + depthString);
                }
            } else {
                throw new IllegalArgumentException("Depth can either be a single value or a range");
            }
        }

        Double targetDepth = null;
        String targetDepthString = params.getString("targetelevation");
        if (targetDepthString != null) {
            try {
                targetDepth = Double.parseDouble(targetDepthString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("TARGETELEVATION format is wrong: "
                        + targetDepthString);
            }
        }
        if (targetDepth == null && zExtent != null) {
            targetDepth = zExtent.getHigh();
        }

        String crsCode;
        BoundingBox bbox;
        if (wmsVersion.equals("1.3.0")) {
            crsCode = params.getMandatoryString("CRS");
            if (crsCode.equalsIgnoreCase("EPSG:4326")) {
                crsCode = "CRS:84";
                bbox = GISUtils.parseBbox(params.getMandatoryString("bbox"), false, crsCode);
            } else {
                bbox = GISUtils.parseBbox(params.getMandatoryString("bbox"), true, crsCode);
            }
        } else {
            crsCode = params.getMandatoryString("SRS");
            if (crsCode.equalsIgnoreCase("EPSG:4326")) {
                crsCode = "CRS:84";
            }
            bbox = GISUtils.parseBbox(params.getMandatoryString("bbox"), true, crsCode);
        }

        return new PlottingDomainParams(params.getMandatoryPositiveInt("width"),
                params.getMandatoryPositiveInt("height"), bbox, zExtent, tExtent, null,
                targetDepth, targetTime);
    }

}

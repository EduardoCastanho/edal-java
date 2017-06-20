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

package uk.ac.rdg.resc.edal.wms.util;

import java.util.List;

/**
 * Encapsulates some server metadata for a WMS server
 *
 * @author Guy Griffiths
 */
public interface ServerInfo {
    /**
     * @return The name of this server
     */
    public String getName();
    
    /**
     * @return Short descriptive text about this server
     */
    public String getAbstract();
    
    /**
     * @return A list of keywords which apply to this server
     */
    public List<String> getKeywords();
    
    /**
     * @return The maximum number of layers which can be requested in the same
     *         image.
     */
    public int getMaxSimultaneousLayers();

    /**
     * @return The maximum image width this server supports
     */
    public int getMaxImageWidth();

    /**
     * @return The maximum image height this server supports
     */
    public int getMaxImageHeight();
    

    /**
     * @return Whether or not this server allows GetFeatureInfo requests
     */
    public boolean allowsFeatureInfo();
    
    /**
     * @return <code>true</code> if this server allows capabilities documents to
     *         be generated for all datasets
     */
    public abstract boolean allowsGlobalCapabilities();

    /**
     * @return The url for accessing to INSPIRE compliant service Metadata
     */
    public abstract String getInspireServiceMetadataURL();
}

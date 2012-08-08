package uk.ac.rdg.resc.edal.coverage.metadata.impl;

import uk.ac.rdg.resc.edal.coverage.metadata.RangeMetadata;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorComponent;
import uk.ac.rdg.resc.edal.coverage.metadata.VectorMetadata;

public class VectorMetadataImpl extends RangeMetadataImpl implements VectorMetadata {

    public VectorMetadataImpl(String name, String description) {
        super(name, description);
    }

    @Override
    public VectorComponent getMemberMetadata(String name) {
        return (VectorComponent) super.getMemberMetadata(name);
    }
    
    public void addMember(VectorComponent metadata) {
        super.addMember(metadata);
    }
    
    @Override
    public VectorMetadata clone() throws CloneNotSupportedException {
        VectorMetadataImpl vectorMetadata = new VectorMetadataImpl(getName(), getDescription());
        for(RangeMetadata member : members.values()){
            vectorMetadata.addMember(member.clone());
        }
        return vectorMetadata;
    }
}
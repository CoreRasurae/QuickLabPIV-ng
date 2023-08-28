// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-LiuShenWithLucasKanadeOpenCL")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel extends SubPixelInterpolationOptionsLiuShenWithLucasKanadeModel {    
    private boolean denseExport;

    public SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.LiuShenWithLucasKanadeOpenCL);
    }
    
    public void setDenseExport(boolean _denseExport) {
        boolean oldValue = denseExport;
        denseExport = _denseExport;
        pcs.firePropertyChange("denseExport", oldValue, denseExport);
    }
    
    @Override
    public boolean isDenseExport() {
        return denseExport;
    }
    
    @Override
    public SubPixelInterpolationOptionsModel copy() {
        SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel model = new SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel();
        
        super.copy(model);
        
        model.denseExport = denseExport;
        
        return model;
    }

    @Override
    public void accept(IPIVConfigurationVisitor visitor) {
       visitor.visit(this);        
    }
    
    @Override
    public boolean isChanged(SubPixelInterpolationOptionsModel another) {
        boolean changed = false;
        
        if (!(another instanceof SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel anotherLS = (SubPixelInterpolationOptionsLiuShenWithLucasKanadeOpenCLModel)another;
                
        if (super.isChanged(another)) {
            changed = true;
        }
        
        if (denseExport != anotherLS.denseExport) {
            changed = true;
        }

        return changed;
    }

}

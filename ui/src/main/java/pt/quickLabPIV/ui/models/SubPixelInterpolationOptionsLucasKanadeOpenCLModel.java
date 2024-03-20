// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.ui.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="interpolation-LucasKanadeOpenCL")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubPixelInterpolationOptionsLucasKanadeOpenCLModel extends SubPixelInterpolationOptionsLucasKanadeModel {
    
    private boolean denseExport = false;
    
    public SubPixelInterpolationOptionsLucasKanadeOpenCLModel() {
        super.setInterpolationMode(SubPixelInterpolationModeEnum.LucasKanadeOpenCL);
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
        SubPixelInterpolationOptionsLucasKanadeOpenCLModel model = new SubPixelInterpolationOptionsLucasKanadeOpenCLModel();
        
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
        
        if (!(another instanceof SubPixelInterpolationOptionsLucasKanadeOpenCLModel)) {
            return true;            
        }
        
        SubPixelInterpolationOptionsLucasKanadeOpenCLModel anotherLK = (SubPixelInterpolationOptionsLucasKanadeOpenCLModel)another;
                

        if (super.isChanged(another)) {
            changed = true;
        }
        
        if (denseExport != anotherLK.denseExport) {
            changed = true;
        }
        
        return changed;
    }

}
